from functools import wraps
from flask_socketio import disconnect
from flask import request
from flask_login import current_user
from app.utils import JwtUtils
from app.models import User
from app.utils.error_handler import UnauthorizedError, AuthError, socketio_handle_errors
import logging

logger = logging.getLogger(__name__)


def socketio_authenticate_user():
    """Checks authentication via JWT or session."""
   # Checking JWT from Headers (for Mobile App)
    token = request.environ.get('HTTP_AUTHORIZATION', '').replace('Bearer ', '')
    if token:
        try:
            payload = JwtUtils.decode_jwt(token)
            user = User.query.get(payload['user_id'])
            if not user:
                logger.error(f"User with ID '{payload['user_id']}' not found during WebSocket authentication")
                raise UnauthorizedError("User not found or invalid token")
            return user
        except Exception as e:
            logger.error(f"WebSocket JWT authentication error: {str(e)}")
            raise UnauthorizedError("Invalid token")

    # Session verification via Flask-Login(for Web App)
    if current_user.is_authenticated:
        return current_user

    logger.error("WebSocket authentication failed: No token or session provided")
    raise UnauthorizedError("Authentication required")

def socketio_role_required(roles):
    def decorator(f):
        @wraps(f)
        @socketio_handle_errors
        def decorated(*args, **kwargs):
            user = socketio_authenticate_user()
            if user.role.role_name not in roles:
                error_msg = f"Access denied for user {user.user_id}: Required roles {roles}, user role {user.role.role_name}"
                logger.error(error_msg)
                raise AuthError(error_msg)
            request.current_user = user
            return f(*args, **kwargs)
        return decorated
    return decorator