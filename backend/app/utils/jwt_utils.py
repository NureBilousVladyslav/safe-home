import jwt
from flask import current_app
from datetime import datetime, timedelta, timezone
from app.utils.error_handler import UnauthorizedError

class JwtUtils:
    @staticmethod
    def generate_jwt(payload, expires_in=3600):
        try:
            expiration = datetime.now(timezone.utc) + timedelta(seconds=expires_in)
            payload['exp'] = expiration
            token = jwt.encode(payload, current_app.config['SECRET_KEY'], algorithm='HS256')
            return token
        except Exception as e:
            raise

    @staticmethod
    def decode_jwt(token):
        try:
            payload = jwt.decode(token, current_app.config['SECRET_KEY'], algorithms=['HS256'])
            return payload
        except jwt.ExpiredSignatureError:
            raise UnauthorizedError("Token has expired")
        except jwt.InvalidTokenError:
            raise UnauthorizedError("Invalid token")
