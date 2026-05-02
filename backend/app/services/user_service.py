from app.utils.error_handler import handle_errors, UnprocessableError, ValidationError
from app.repositories.user_repo import UserRepository
from app.repositories.role_repo import RoleRepository
from app.utils import Validator
from app.models.user import User
from flask import jsonify, g


class UserService:
    @staticmethod
    @handle_errors
    def get_all_users():
        users = UserRepository.get_all()
        users_list = [
            {
                "user_id": str(user.user_id),
                "name": user.name,
                "email": user.email,
                "role": user.role.role_name if user.role else None,
                "created_at": user.created_at.isoformat(),
                "email_confirmed": user.email_confirmed,
                "subscription_plan_name": user.subscription_plan_name
            } for user in users if user.role and user.role.role_name == "user"
        ]
        return jsonify({"users": users_list}), 200

    @staticmethod
    @handle_errors
    def get_all_admins():
        users = UserRepository.get_all()
        users_list = [
            {
                "user_id": str(user.user_id),
                "name": user.name,
                "email": user.email,
                "role": user.role.role_name if user.role else None,
                "created_at": user.created_at.isoformat(),
                "email_confirmed": user.email_confirmed,
            } for user in users if user.role and user.role.role_name == "admin"
        ]
        return jsonify({"admins": users_list}), 200

    @staticmethod
    @handle_errors
    def get_user(user_id):
        user = UserRepository.get_by_id(user_id)
        if not user:
            raise UnprocessableError("User not found.")

        user_data = {
            "user_id": str(user.user_id),
            "name": user.name,
            "email": user.email,
            "role": user.role.role_name if user.role else None,
            "created_at": user.created_at.isoformat(),
            "email_confirmed": user.email_confirmed,
            "subscription_plan_name": user.subscription_plan_name
        }
        return jsonify({"user": user_data}), 200

    @staticmethod
    @handle_errors
    def delete_user(user_id, role):
        if not user_id:
            raise ValidationError("'user_id' is a required parameter.")

        user = UserRepository.get_by_id(user_id)
        if not user:
            raise UnprocessableError("User not found.")

        if user.role.role_name != role:
            raise ValidationError("You cannot delete user with this role.")

        g.deleted_data = {
            'user_id': str(user.user_id),
            'name': user.name,
            'email': user.email,
            'role': user.role.role_name
        }

        UserRepository.delete(user)
        return jsonify({"message": "User deleted successfully."}), 200

    @staticmethod
    @handle_errors
    def update_user(user_id, data):
        Validator.validate_required_fields(data, ['name'])

        user = UserRepository.get_by_id(user_id)
        if not user:
            raise UnprocessableError(f"User with ID '{user_id}' not found.")

        user.name = data['name']
        UserRepository.update(user)
        return jsonify({'message': 'User data updated successfully.'}), 200

    @staticmethod
    @handle_errors
    def update_user_password(user_id, data):
        Validator.validate_required_fields(data, ['old_password', 'new_password'])
        old_password = data.get('old_password')
        new_password = data.get('new_password')

        user = UserRepository.get_by_id(user_id)
        if not user:
            raise UnprocessableError(f"User with ID '{user_id}' not found.")

        if user.check_password(old_password):
            user.set_password(new_password)
        else:
            raise ValidationError("Invalid old password.")

        UserRepository.update(user)
        return jsonify({'message': 'Password updated successfully.'}), 200

    @staticmethod
    def register_user(data, role_name="user"):
        Validator.validate_required_fields(data, ['name', 'email', 'password'])

        role = RoleRepository.get_by_name(role_name)
        if not role:
            raise UnprocessableError(f"Role '{role_name}' not found.")

        user = User(name=data['name'], email=data['email'], role_id=role.role_id)
        user.set_password(data['password'])

        UserRepository.add(user)
        return user

    @staticmethod
    def google_register_user(data, role_name="user"):
        Validator.validate_required_fields(data, ['name', 'email', 'google_id'])

        role = RoleRepository.get_by_name(role_name)
        if not role:
            raise UnprocessableError(f"Role '{role_name}' not found.")

        user = User(name=data['name'], email=data['email'], google_id=data['google_id'], role_id=role.role_id)
        UserRepository.add(user)

        refresh_token = data.get('refresh_token')
        if refresh_token:
            user.set_refresh_token(refresh_token)
            UserRepository.update(user)

        return user

    @staticmethod
    def add_user_data(user, data):
        user.name = data['name']
        user.set_password(data['password'])
        UserRepository.update(user)

    @staticmethod
    def add_google_data(user, google_id, refresh_token=None):
        user.google_id = google_id
        if refresh_token:
            user.set_refresh_token(refresh_token)
        UserRepository.update(user)

    @staticmethod
    def verify_email(user):
        user.email_confirmed = True
        UserRepository.update(user)

    @staticmethod
    def drop_email_verification(user):
        user.email_confirmed = False
        UserRepository.update(user)

    @staticmethod
    def reset_password(user, new_password):
        user.set_password(new_password)
        UserRepository.update(user)
