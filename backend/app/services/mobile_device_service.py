from app.utils.error_handler import handle_errors, UnprocessableError, ValidationError
from app.repositories.mobile_device_repo import MobileDeviceRepository
from app.utils import Validator
from app.models.mobile_device import MobileDevice
from flask import jsonify

class MobileDeviceService:
    @staticmethod
    @handle_errors
    def get_user_devices(user_id):
        devices = MobileDeviceRepository.get_all_by_user(user_id)
        devices_list = [
            {
                "user_device_id": str(device.user_device_id),
                "device_token": str(device.device_token),
                "device_info": device.device_info,
                "created_at": device.created_at.isoformat()
            } for device in devices
        ]
        return jsonify({'devices': devices_list}), 200

    @staticmethod
    @handle_errors
    def add_user_device(user_id, body):
        Validator.validate_required_fields(body, ['device_token'])

        device_token = body['device_token']
        device_info = body.get('device_info')

        # Check for existing device at user's
        existing_device = MobileDeviceRepository.get_by_user_and_token(user_id, device_token)
        if existing_device:
            raise UnprocessableError("This device is already registered for the user.")

        # Checking for another user's device
        other_user_device = MobileDeviceRepository.get_by_token(device_token)
        if other_user_device:
            MobileDeviceRepository.delete(other_user_device)

        new_device = MobileDevice(
            user_id=user_id,
            device_token=device_token,
            device_info=device_info
        )
        MobileDeviceRepository.add(new_device)
        return jsonify({
            'message': "Device was successfully added to the user.",
            'device_id': str(new_device.user_device_id)
        }), 201

    @staticmethod
    @handle_errors
    def delete_device_by_token(user_id, body):
        Validator.validate_required_fields(body, ['device_token'])

        device_token = body['device_token']
        device = MobileDeviceRepository.get_by_user_and_token(user_id, device_token)
        if not device:
            raise UnprocessableError("Device not found for the specified user and token.")

        MobileDeviceRepository.delete(device)
        return jsonify({'message': "Device was successfully removed for the user."}), 200
