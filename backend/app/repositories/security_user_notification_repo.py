from app.models.security_user_notification import SecurityUserNotification
from app import db

class SecurityUserNotificationRepository:
    @staticmethod
    def get_all_by_home(home_id):
        return (SecurityUserNotification.query.filter_by(home_id=home_id).
                order_by(SecurityUserNotification.created_at.desc()).all())

    @staticmethod
    def get_all_by_user(user_id):
        return (SecurityUserNotification.query.filter_by(user_id=user_id).
                order_by(SecurityUserNotification.created_at.desc()).all())

    @staticmethod
    def get_all_by_user_and_home(user_id, home_id):
        return (SecurityUserNotification.query.filter_by(user_id=user_id, home_id=home_id).
                order_by(SecurityUserNotification.created_at.desc()).all())

    @staticmethod
    def get_by_id(notification_id):
        return SecurityUserNotification.query.get(notification_id)

    @staticmethod
    def get_by_user_and_id(user_id, notification_id):
        return SecurityUserNotification.query.filter_by(user_id=user_id, id=notification_id).first()

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
