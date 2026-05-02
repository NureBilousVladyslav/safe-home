from flask import Blueprint, request
from app.services.mobile_device_service import MobileDeviceService
from app.utils.auth_decorator import auth_required, role_required
from app.utils.error_handler import handle_errors
from flasgger import swag_from

mobile_device_bp = Blueprint('mobile_device', __name__)


@mobile_device_bp.route('/devices', methods=['GET'])
@swag_from({
    'tags': ['Mobile Device'],
    'summary': 'Get all devices for the authenticated user',
    'responses': {
        200: {
            'description': 'List of devices',
            'schema': {
                'type': 'object',
                'properties': {
                    'devices': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'user_device_id': {'type': 'string'},
                                'device_token': {'type': 'string'},
                                'device_info': {'type': 'string'},
                                'created_at': {'type': 'string'}
                            }
                        }
                    }
                }
            }
        },
        401: {'description': 'Unauthorized'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def get_user_devices():
    user_id = request.current_user.user_id
    return MobileDeviceService.get_user_devices(user_id)


@mobile_device_bp.route('/devices', methods=['POST'])
@swag_from({
    'tags': ['Mobile Device'],
    'summary': 'Add a new device for the authenticated user',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'device_token': {'type': 'string'},
                    'device_info': {'type': 'string'}
                },
                'required': ['device_token']
            }
        }
    ],
    'responses': {
        201: {'description': 'Device added successfully'},
        400: {'description': 'Validation error'},
        422: {'description': 'Unprocessable entity'},
        401: {'description': 'Unauthorized'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def add_user_device():
    user_id = request.current_user.user_id
    return MobileDeviceService.add_user_device(user_id, request.json)


@mobile_device_bp.route('/devices', methods=['DELETE'])
@swag_from({
    'tags': ['Mobile Device'],
    'summary': 'Delete a device for the authenticated user by token',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'device_token': {'type': 'string'}
                },
                'required': ['device_token']
            }
        }
    ],
    'responses': {
        200: {'description': 'Device removed successfully'},
        400: {'description': 'Validation error'},
        422: {'description': 'Unprocessable entity'},
        401: {'description': 'Unauthorized'},
        500: {'description': 'Internal server error'}
    }
})
@auth_required
@role_required(['user'])
@handle_errors
def delete_device_by_token():
    user_id = request.current_user.user_id
    return MobileDeviceService.delete_device_by_token(user_id, request.json)
