from app.models import Home
from app import db

class HomeRepository:
    @staticmethod
    def get_all_by_user(user_id):
        return Home.query.filter_by(user_id=user_id).all()

    @staticmethod
    def get_by_id(home_id):
        return Home.query.get(home_id)

    @staticmethod
    def get_by_user_and_id(user_id, home_id):
        return Home.query.filter_by(home_id=home_id, user_id=user_id).first()

    @staticmethod
    def get_by_user_and_id_archived(user_id, home_id, is_archived):
        return Home.query.filter_by(user_id=user_id, home_id=home_id, is_archived=is_archived).first()

    @staticmethod
    def count_active_by_user(user_id):
        return Home.query.filter_by(user_id=user_id, is_archived=False).count()

    @staticmethod
    def add(home):
        db.session.add(home)
        db.session.commit()

    @staticmethod
    def update(home):
        db.session.commit()

    @staticmethod
    def delete(home):
        db.session.delete(home)
        db.session.commit()
