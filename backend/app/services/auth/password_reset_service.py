from app.utils.error_handler import handle_errors, UnprocessableError, ValidationError
from app.repositories.user_repo import UserRepository
from app.services.user_service import UserService
from app.utils import Validator
from app.config import Config
from itsdangerous import URLSafeTimedSerializer
from flask import url_for, render_template_string
from flask_mail import Message
from app import mail

s = URLSafeTimedSerializer(Config.SECRET_KEY)

class PasswordResetService:
    @staticmethod
    @handle_errors
    def request_reset_password(data):
        Validator.validate_required_fields(data, ['email'])
        email = data['email']

        user = UserRepository.get_by_email(email)
        if not user:
            raise UnprocessableError(f"User with email '{email}' not found.")

        token = s.dumps(email, salt='reset-password-confirm-salt')
        confirmation_url = url_for('auth.confirm_reset_password', token=token, _external=True)

        with open("app/templates/reset_password_confirmation.html", "r") as html_file:
            html_template = html_file.read()

        html_body = render_template_string(
            html_template,
            name=user.name,
            confirmation_url=confirmation_url
        )

        msg = Message(
            "Reset password confirmation",
            recipients=[email],
            body=f"To confirm your password resetting, visit the following link: {confirmation_url}",
            html=html_body
        )

        mail.send(msg)
        return {'message': 'The confirmation was sent successfully.'}

    @staticmethod
    @handle_errors
    def verify_reset_password_token(token):
        email = s.loads(token, salt='reset-password-confirm-salt', max_age=3600)
        user = UserRepository.get_by_email(email)

        if not user:
            raise UnprocessableError('The token is invalid or expired.')

        PasswordResetService._send_reset_email(user)

        with open("app/templates/reset_password_confirmation_success.html", "r") as html_file:
            success_html_template = html_file.read()

        return render_template_string(
            success_html_template,
            user=user
        )

    @staticmethod
    def _send_reset_email(user):
        new_password = PasswordResetService._generate_random_password()
        UserService.reset_password(user, new_password)

        with open("app/templates/password_reset_email.html", "r") as html_file:
            html_template = html_file.read()

        html_body = render_template_string(
            html_template,
            name=user.name,
            new_password=new_password
        )

        msg = Message(
            "Your New Password",
            recipients=[user.email],
            body=f"Your new password is: {new_password}\n"
                 f"You can log in using this password. Please change it after logging in.",
            html=html_body
        )

        mail.send(msg)

    @staticmethod
    def _generate_random_password(length=8):
        import string
        import random
        characters = string.ascii_letters + string.digits
        return ''.join(random.choice(characters) for _ in range(length))
