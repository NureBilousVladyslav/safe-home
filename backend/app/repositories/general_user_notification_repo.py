from app.models.general_user_notification import GeneralUserNotification
from app import db

class GeneralUserNotificationRepository:
    @staticmethod
    def get_all_by_user(user_id):
        return (GeneralUserNotification.query.filter_by(user_id=user_id).
                order_by(GeneralUserNotification.created_at.desc()).all())

    @staticmethod
    def get_by_id(notification_id):
        return GeneralUserNotification.query.get(notification_id)

    @staticmethod
    def get_by_user_and_id(user_id, notification_id):
        return GeneralUserNotification.query.filter_by(user_id=user_id, id=notification_id).first()

    @staticmethod
    def add(notification):
        db.session.add(notification)
        db.session.commit()

    @staticmethod
    def update(notification):
        db.session.commit()

    @staticmethod
    def delete(notification):
        db.session.delete(notification)
        db.session.commit()
