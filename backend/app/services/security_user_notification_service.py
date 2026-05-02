from app.utils.error_handler import handle_errors, UnprocessableError, ValidationError
from app.repositories.security_user_notification_repo import SecurityUserNotificationRepository
from app.utils import Validator
from app.models.security_user_notification import SecurityUserNotification
from flask import jsonify

class SecurityUserNotificationService:
    @staticmethod
    def create_notification(user_id, home_id, title, body, importance, type, sensor_id=None, data=None):
        if importance not in ['low', 'medium', 'high']:
            raise ValidationError("Importance must be 'low', 'medium', or 'high'.")

        new_notification = SecurityUserNotification(
            home_id=home_id,
            title=title,
            body=body,
            importance=importance,
            type=type,
            user_id=user_id,
            sensor_id=sensor_id,
            data=data
        )
        SecurityUserNotificationRepository.add(new_notification)

    @staticmethod
    def serialize_notifications(notifications):
        notifications_list = [
            {
                "id": str(notification.id),
                "home_id": str(notification.home_id),
                "sensor_id": str(notification.sensor_id) if notification.sensor_id else None,
                "title": notification.title,
                "body": notification.body,
                "importance": notification.importance,
                "created_at": notification.created_at.isoformat(),
                "type": notification.type,
                "data": notification.data
            } for notification in notifications
        ]
        return jsonify({"notifications": notifications_list}), 200

    @staticmethod
    @handle_errors
    def get_notifications_by_user_and_home(user_id, home_id):
        notifications = SecurityUserNotificationRepository.get_all_by_user_and_home(user_id, home_id)
        return SecurityUserNotificationService.serialize_notifications(notifications)

    @staticmethod
    @handle_errors
    def get_notifications_by_user(user_id):
        notifications = SecurityUserNotificationRepository.get_all_by_user(user_id)
        return SecurityUserNotificationService.serialize_notifications(notifications)
