from app.models.mobile_device import MobileDevice
from app import db

class MobileDeviceRepository:
    @staticmethod
    def get_all_by_user(user_id):
        return MobileDevice.query.filter_by(user_id=user_id).all()

    @staticmethod
    def get_by_id(device_id):
        return MobileDevice.query.get(device_id)

    @staticmethod
    def get_by_user_and_token(user_id, device_token):
        return MobileDevice.query.filter_by(user_id=user_id, device_token=device_token).first()

    @staticmethod
    def get_by_user_and_id(user_id, device_id):
        return MobileDevice.query.filter_by(user_id=user_id, user_device_id=device_id).first()

    @staticmethod
    def get_by_token(device_token):
        return MobileDevice.query.filter_by(device_token=device_token).first()

    @staticmethod
    def add(device):
        db.session.add(device)
        db.session.commit()

    @staticmethod
    def update(device):
        db.session.commit()

    @staticmethod
    def delete(device):
        db.session.delete(device)
        db.session.commit()
