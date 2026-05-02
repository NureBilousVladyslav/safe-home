from app.utils.error_handler import handle_errors, UnprocessableError, ValidationError
from app.repositories.user_repo import UserRepository
from app.services.user_service import UserService
from app.services.subscription_service import SubscriptionService
from flask import jsonify, session, redirect, url_for, flash
from flask_login import login_user
from app import oauth
import os


google = oauth.register(
    name='google',
    client_id=os.getenv('GOOGLE_CLIENT_ID'),
    client_secret=os.getenv('GOOGLE_CLIENT_SECRET'),
    access_token_url='https://oauth2.googleapis.com/token',
    authorize_url='https://accounts.google.com/o/oauth2/auth',
    jwks_uri='https://www.googleapis.com/oauth2/v3/certs'
)


class GoogleAuthService:
    @staticmethod
    @handle_errors
    def initiate_google_login():
        nonce = os.urandom(16).hex()
        session['nonce'] = nonce
        redirect_uri = url_for('auth.google_callback', _external=True)
        return google.authorize_redirect(redirect_uri, nonce=nonce, scope=['openid', 'email', 'profile'])

    @staticmethod
    @handle_errors
    def handle_google_callback():
        token = google.authorize_access_token()
        nonce = session.pop('nonce', None)
        if not token or not nonce:
            raise ValidationError("Authorization failed.")

        user_info = google.parse_id_token(token, nonce=nonce)
        if not user_info:
            raise UnprocessableError("Failed to fetch user info.")

        user = UserRepository.get_by_email(user_info['email'])
        if user:
            if not user.google_id:
                UserService.add_google_data(user, user_info['sub'], token.get('refresh_token'))
                UserService.verify_email(user)
            login_user(user)
        else:
            data = {
                'name': user_info.get('given_name'),
                'email': user_info['email'],
                'google_id': user_info['sub'],
                'refresh_token': token.get('refresh_token'),
            }
            user = UserService.google_register_user(data, 'user')
            SubscriptionService.create_basic_subscription(user.user_id)
            UserService.verify_email(user)
            login_user(user)

        flash('Logged in with Google successfully.', 'success')
        
        if user.role.role_name != 'user':
            link = os.getenv('FRONTEND_LINK') + '/admin/customers'
        else:
            link = os.getenv('FRONTEND_LINK') + '/user/subscriptions'
            
        return redirect(link)
