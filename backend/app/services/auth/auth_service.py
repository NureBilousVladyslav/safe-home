from app.utils.error_handler import handle_errors, UnprocessableError, ValidationError
from app.repositories.user_repo import UserRepository
from app.services.user_service import UserService
from app.services.subscription_service import SubscriptionService
from app.services.auth.email_confirm_service import EmailConfirmService
from app.utils.jwt_utils import JwtUtils
from app.utils import Validator
from flask import jsonify, g
from app import login_manager
from flask_login import login_user, logout_user, current_user

@login_manager.user_loader
def load_user(user_id):
    """
    Load a user by their ID for Flask-Login.

    Args:
        user_id (str): The ID of the user to load.

    Returns:
        User: The user object, or None if not found.
    """
    return UserRepository.get_by_id(user_id)

class AuthService:
    @staticmethod
    @handle_errors
    def register_user(data):
        Validator.validate_required_fields(data, ['name', 'email', 'password'])
        email = data.get('email')

        existing_user = UserRepository.get_by_email(email)
        if existing_user:
            if existing_user.google_id and not existing_user.password:
                UserService.drop_email_verification(existing_user)
                EmailConfirmService.send_email_confirmation(existing_user)
                UserService.add_user_data(existing_user, data)
                return jsonify({'message': 'User data updated successfully.'}), 200
            raise ValidationError('User already exists.')

        user = UserService.register_user(data, role_name="user")
        SubscriptionService.create_basic_subscription(user.user_id)
        if not user.email_confirmed:
            EmailConfirmService.send_email_confirmation(user)
        return jsonify({'message': 'User registered successfully.'}), 201

    @staticmethod
    @handle_errors
    def register_admin(data):
        Validator.validate_required_fields(data, ['name', 'email', 'password'])
        email = data.get('email')

        existing_user = UserRepository.get_by_email(email)
        if existing_user:
            raise ValidationError('User already exists.')

        user = UserService.register_user(data, role_name="admin")
        EmailConfirmService.send_user_registered_email(user, password=data.get('password'))
        g.created_data = {
            'user_id': str(user.user_id),
            'name': user.name,
            'email': user.email,
            'role': 'admin'
        }
        return jsonify({'message': 'Admin registered successfully.'}), 201

    @staticmethod
    @handle_errors
    def session_login_user(data):
        user = AuthService.login_user(data)
        if user:
            login_user(user)
            return jsonify({'message': 'Logged in successfully.'}), 200
        raise UnprocessableError('Invalid credentials.')

    @staticmethod
    @handle_errors
    def token_login_user(data):
        user = AuthService.login_user(data)
        if user:
            token = JwtUtils.generate_jwt({'user_id': str(user.user_id)})
            return jsonify({'message': 'Logged in successfully.', 'token': token}), 200
        raise UnprocessableError('Invalid credentials.')

    @staticmethod
    @handle_errors
    def logout_user():
        if current_user.is_authenticated:
            logout_user()
            return jsonify({'message': 'Logged out successfully.'}), 200
        raise UnprocessableError('No user logged in.')

    @staticmethod
    @handle_errors
    def verify_token(token):
        if not token:
            raise ValidationError("Token is missing")

        clean_token = token[7:] if token.startswith("Bearer ") else token
        if not clean_token:
            raise ValidationError("Invalid token format")

        payload = JwtUtils.decode_jwt(clean_token)
        return jsonify({
            "valid": True,
            "user_id": payload.get("user_id")
        }), 200

    @staticmethod
    def login_user(data):
        email = data.get('email')
        if not email:
            raise ValidationError("Email is required for login.")

        user = UserRepository.get_by_email(email)
        if not user:
            raise UnprocessableError('Invalid credentials.')

        if not user.email_confirmed:
            EmailConfirmService.send_email_confirmation(user)
            raise UnprocessableError("Please confirm your email first.")

        if user.password is None:
            raise UnprocessableError('Please login via Google or complete regular registration.')

        if user.check_password(data.get('password')):
            return user
        raise UnprocessableError('Invalid credentials.')
