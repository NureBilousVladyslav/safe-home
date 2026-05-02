from app import db
from app.models.role import Role

class RoleRepository:
    @staticmethod
    def get_by_name(role_name):
        return Role.query.filter_by(role_name=role_name).first()
