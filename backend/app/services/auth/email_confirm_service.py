from app.utils.error_handler import handle_errors, UnprocessableError, ValidationError
from app.repositories.user_repo import UserRepository
from app.config import Config
from itsdangerous import URLSafeTimedSerializer
from flask import url_for, render_template_string
from flask_mail import Message
from app import mail

s = URLSafeTimedSerializer(Config.SECRET_KEY)

class EmailConfirmService:
    @staticmethod
    @handle_errors
    def send_email_confirmation(user):
        token = s.dumps(user.email, salt='email-confirm-salt')
        confirmation_url = url_for('auth.confirm_email', token=token, _external=True)

        with open("app/templates/email_confirmation.html", "r") as html_file:
            html_template = html_file.read()

        html_body = render_template_string(
            html_template,
            name=user.name,
            confirmation_url=confirmation_url
        )

        msg = Message(
            "Email Confirmation",
            recipients=[user.email],
            body=f"To confirm your email address, visit the following link: {confirmation_url}",
            html=html_body
        )

        mail.send(msg)
        return {'message': 'The email confirmation was sent successfully.'}

    @staticmethod
    @handle_errors
    def send_user_registered_email(user, password):
        token = s.dumps(user.email, salt='email-confirm-salt')
        confirmation_url = url_for('auth.confirm_email', token=token, _external=True)

        with open("app/templates/user_registered_notification.html", "r") as html_file:
            html_template = html_file.read()

        html_body = render_template_string(
            html_template,
            name=user.name,
            email=user.email,
            role=user.role.role_name,
            password=password,
            confirmation_url=confirmation_url
        )

        msg = Message(
            "User Registration Notification",
            recipients=[user.email],
            body=f"You were registered as {user.role.role_name} with password: {password}!\n"
                 f"To confirm your email address, visit the following link: {confirmation_url}",
            html=html_body
        )

        mail.send(msg)
        return {'message': 'User registered notification was sent successfully.'}

    @staticmethod
    @handle_errors
    def verify_email_token(token):
        email = s.loads(token, salt='email-confirm-salt', max_age=3600)
        user = UserRepository.get_by_email(email)

        if not user:
            raise UnprocessableError('The token is invalid or expired.')

        user.email_confirmed = True
        UserRepository.update(user)

        with open("app/templates/email_confirmation_success.html", "r") as html_file:
            success_html_template = html_file.read()

        return render_template_string(
            success_html_template,
            user=user
        )
