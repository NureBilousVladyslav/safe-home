from app.models import Sensor
from app import db

class SensorRepository:
    @staticmethod
    def get_all_by_home(home_id):
        return Sensor.query.filter_by(home_id=home_id).all()

    @staticmethod
    def get_all_by_user(user_id):
        return Sensor.query.filter_by(user_id=user_id).all()

    @staticmethod
    def get_all_by_user_and_home(user_id, home_id):
        return Sensor.query.filter_by(user_id=user_id, home_id=home_id).all()

    @staticmethod
    def get_by_id(sensor_id):
        return Sensor.query.get(sensor_id)

    @staticmethod
    def get_by_user_and_id(user_id, sensor_id):
        return Sensor.query.filter_by(sensor_id=sensor_id, user_id=user_id).first()

    @staticmethod
    def get_by_user_and_id_archived(user_id, sensor_id, is_archived):
        return Sensor.query.filter_by(sensor_id=sensor_id, user_id=user_id, is_archived=is_archived).first()

    @staticmethod
    def get_by_user_short_id_archived(user_id, short_id, is_archived):
        return Sensor.query.filter_by(short_id=short_id, user_id=user_id, is_archived=is_archived).first()

    @staticmethod
    def get_active_by_user(user_id):
        return Sensor.query.filter_by(user_id=user_id, is_archived=False).count()

    @staticmethod
    def count_active_by_user(user_id):
        return Sensor.query.filter_by(user_id=user_id, is_archived=False).count()

    @staticmethod
    def add(sensor):
        db.session.add(sensor)
        db.session.commit()

    @staticmethod
    def update(sensor):
        db.session.commit()

    @staticmethod
    def delete(sensor):
        db.session.delete(sensor)
        db.session.commit()
