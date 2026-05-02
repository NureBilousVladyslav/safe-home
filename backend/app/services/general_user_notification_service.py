from app.utils.error_handler import handle_errors, UnprocessableError, ValidationError
from app.repositories.general_user_notification_repo import GeneralUserNotificationRepository
from app.utils import Validator
from app.models.general_user_notification import GeneralUserNotification
from flask import jsonify

class GeneralUserNotificationService:
    @staticmethod
    def create_notification(user_id, title, body, importance, type, data=None):
        if importance not in ['low', 'medium', 'high']:
            raise ValidationError("Importance must be 'low', 'medium', or 'high'.")

        new_notification = GeneralUserNotification(
            user_id=user_id,
            title = title,
            body = body,
            importance = importance,
            type = type,
            data = data
        )
        GeneralUserNotificationRepository.add(new_notification)

    @staticmethod
    @handle_errors
    def get_notifications_by_user(user_id):
        notifications = GeneralUserNotificationRepository.get_all_by_user(user_id)
        notifications_list = [
            {
                "id": str(notification.id),
                "title": notification.title,
                "body": notification.body,
                "importance": notification.importance,
                "created_at": notification.created_at.isoformat(),
                "type": notification.type,
                "data": notification.data
            } for notification in notifications
        ]
        return jsonify({"notifications": notifications_list}), 200
