from firebase_admin import messaging
from app.repositories.mobile_device_repo import MobileDeviceRepository
from app.utils.error_handler import ValidationError

def send_notification(user_id, title, body, data):
    devices = MobileDeviceRepository.get_all_by_user(user_id)
    device_tokens = [device.device_token for device in devices if device.device_token]

    if not device_tokens:
        raise ValidationError("No device tokens found for the user.")

    for token in device_tokens:
        message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body
            ),
            data=data,
            token=token
        )

        response = messaging.send(message)
        print('Successfully sent message:', response)
