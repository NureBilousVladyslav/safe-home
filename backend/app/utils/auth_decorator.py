from functools import wraps
from flask import request, g
from app.utils import JwtUtils
from app.models import User
from flask_login import current_user
from app.utils.error_handler import UnauthorizedError, AuthError, handle_errors
import logging

logger = logging.getLogger(__name__)


def authenticate_user():
    """Common feature for checking authentication via JWT or session."""
    # Check JWT
    token = request.headers.get('Authorization')
    if token:
        try:
            if token.startswith("Bearer "):
                token = token.split(" ")[1]

            payload = JwtUtils.decode_jwt(token)
            user = User.query.get(payload['user_id'])
            if not user:
                logger.error(f"User with ID '{payload['user_id']}' not found during authentication")
                raise UnauthorizedError("User not found or invalid token")
            return user
        except Exception as e:
            raise

    # Session verification via Flask-Login
    if current_user.is_authenticated:
        return current_user

    logger.error("Authentication failed: No token or session provided")
    raise UnauthorizedError("Authentication required")

def auth_required(f):
    @wraps(f)
    @handle_errors
    def decorated(*args, **kwargs):
        user = authenticate_user()
        request.current_user = user
        g.user = user
        return f(*args, **kwargs)
    return decorated

def role_required(roles):
    def decorator(f):
        @wraps(f)
        @handle_errors
        def decorated(*args, **kwargs):
            user = authenticate_user()

            if user.role.role_name not in roles:
                logger.error(f"Access denied for user {user.user_id}: Required roles {roles}, user role {user.role.role_name}")
                raise AuthError(f"User does not have the required role. Required roles: {roles}")

            request.current_user = user
            g.user = user
            return f(*args, **kwargs)
        return decorated
    return decorator
