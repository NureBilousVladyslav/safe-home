from app.models.default_security_mode import DefaultSecurityMode
from app import db

class DefaultSecurityModeRepository:
    @staticmethod
    def get_all():
        return DefaultSecurityMode.query.all()

    @staticmethod
    def get_by_id(mode_id):
        return DefaultSecurityMode.query.get(mode_id)

    @staticmethod
    def get_by_name(mode_name):
        return DefaultSecurityMode.query.filter_by(mode_name=mode_name).first()

    @staticmethod
    def add(mode):
        db.session.add(mode)
        db.session.commit()

    @staticmethod
    def update(mode):
        db.session.commit()

    @staticmethod
    def delete(mode):
        db.session.delete(mode)
        db.session.commit()
