from flask import Blueprint, request
from app.services.auth import AuthService, GoogleAuthService, FirebaseAuthService, PasswordResetService, EmailConfirmService
from app.services import AdminAuditLogService
from app.utils.auth_decorator import auth_required, role_required
from app.utils.error_handler import handle_errors
from app.utils import success_trigger
from flasgger import swag_from

auth_bp = Blueprint('auth', __name__)


@auth_bp.route('/register', methods=['POST'])
@swag_from({
    'tags': ['Auth'],
    'summary': 'Register a new user',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'name': {'type': 'string'},
                    'email': {'type': 'string'},
                    'password': {'type': 'string'}
                },
                'required': ['name', 'email', 'password']
            }
        }
    ],
    'responses': {
        201: {'description': 'User registered successfully'},
        200: {'description': 'User data updated successfully (if Google user exists)'},
        400: {'description': 'Validation error'},
        500: {'description': 'Internal server error'}
    }
})
@handle_errors
def register_user():
    return AuthService.register_user(request.json)


@auth_bp.route('/register/admin', methods=['POST'])
@swag_from({
    'tags': ['Auth'],
    'summary': 'Register a new admin',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'name': {'type': 'string'},
                    'email': {'type': 'string'},
                    'password': {'type': 'string'}
                },
                'required': ['email']
            }
        }
    ],
    'responses': {
        201: {'description': 'Admin registered successfully'},
        400: {'description': 'Validation error'},
        401: {'description': 'Unauthorized'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['admin', 'super_admin'])
@handle_errors
@success_trigger(message='registered new admin.', handler=AdminAuditLogService.build_create_audit_log)
def register_admin():
    return AuthService.register_admin(request.json)


@auth_bp.route('/login/session', methods=['POST'])
@swag_from({
    'tags': ['Auth'],
    'summary': 'Login user and create a session',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'email': {'type': 'string'},
                    'password': {'type': 'string'}
                },
                'required': ['email', 'password']
            }
        }
    ],
    'responses': {
        200: {'description': 'Logged in successfully'},
        400: {'description': 'Validation error'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@handle_errors
def session_login():
    return AuthService.session_login_user(request.json)


@auth_bp.route('/login/token', methods=['POST'])
@swag_from({
    'tags': ['Auth'],
    'summary': 'Login user and return a JWT token',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'email': {'type': 'string'},
                    'password': {'type': 'string'}
                },
                'required': ['email', 'password']
            }
        }
    ],
    'responses': {
        200: {
            'description': 'Logged in successfully',
            'schema': {
                'type': 'object',
                'properties': {
                    'message': {'type': 'string'},
                    'token': {'type': 'string'}
                }
            }
        },
        400: {'description': 'Validation error'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@handle_errors
def token_login():
    return AuthService.token_login_user(request.json)


@auth_bp.route('/logout', methods=['POST'])
@swag_from({
    'tags': ['Auth'],
    'summary': 'Logout the current user',
    'responses': {
        200: {'description': 'Logged out successfully'},
        401: {'description': 'Unauthorized'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@auth_required
@handle_errors
def logout():
    return AuthService.logout_user()


@auth_bp.route('/verify-token', methods=['POST'])
@swag_from({
    'tags': ['Auth'],
    'summary': 'Verify a JWT token',
    'parameters': [
        {
            'name': 'Authorization',
            'in': 'header',
            'required': True,
            'type': 'string',
            'description': 'Bearer token'
        }
    ],
    'responses': {
        200: {
            'description': 'Token is valid',
            'schema': {
                'type': 'object',
                'properties': {
                    'valid': {'type': 'boolean'},
                    'user_id': {'type': 'string'}
                }
            }
        },
        400: {'description': 'Validation error'},
        500: {'description': 'Internal server error'}
    }
})
@handle_errors
def verify_token():
    token = request.headers.get('Authorization')
    return AuthService.verify_token(token)


@auth_bp.route('/login/google', methods=['GET'])
@swag_from({
    'tags': ['Auth'],
    'summary': 'Initiate Google OAuth login',
    'responses': {
        302: {'description': 'Redirect to Google OAuth page'},
        400: {'description': 'Validation error'},
        500: {'description': 'Internal server error'}
    }
})
@handle_errors
def initiate_google_login():
    return GoogleAuthService.initiate_google_login()


@auth_bp.route('/login/google/callback', methods=['GET'])
@swag_from({
    'tags': ['Auth'],
    'summary': 'Handle Google OAuth callback',
    'responses': {
        302: {'description': 'Redirect to frontend after successful login'},
        400: {'description': 'Validation error'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@handle_errors
def google_callback():
    return GoogleAuthService.handle_google_callback()


@auth_bp.route('/login/firebase', methods=['POST'])
@swag_from({
    'tags': ['Auth'],
    'summary': 'Authenticate user with Firebase token',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'firebase_id_token': {'type': 'string'}
                },
                'required': ['firebase_id_token']
            }
        }
    ],
    'responses': {
        200: {
            'description': 'Firebase login successful',
            'schema': {
                'type': 'object',
                'properties': {
                    'message': {'type': 'string'},
                    'token': {'type': 'string'}
                }
            }
        },
        400: {'description': 'Validation error'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@handle_errors
def firebase_login():
    return FirebaseAuthService.firebase_auth(request.json)


# --- PasswordResetService Routes ---
@auth_bp.route('/reset-password', methods=['POST'])
@swag_from({
    'tags': ['Auth'],
    'summary': 'Request a password reset',
    'description': 'Sends a password reset confirmation email to the specified email address.',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'email': {'type': 'string', 'format': 'email'}
                },
                'required': ['email']
            }
        }
    ],
    'responses': {
        200: {
            'description': 'Password reset confirmation email sent successfully',
            'schema': {
                'type': 'object',
                'properties': {
                    'message': {'type': 'string'}
                }
            }
        },
        400: {'description': 'Validation error - Missing or invalid email'},
        422: {'description': 'Unprocessable entity - User not found'},
        500: {'description': 'Internal server error'}
    }
})
@handle_errors
def reset_password_request():
    return PasswordResetService.request_reset_password(request.json)


@auth_bp.route('/confirm-reset-password/<token>', methods=['GET'])
@swag_from({
    'tags': ['Auth'],
    'summary': 'Confirm password reset',
    'description': 'Verifies the password reset token and sends a new password to the user\'s email. Returns an HTML confirmation page.',
    'parameters': [
        {
            'name': 'token',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'Password reset token received via email'
        }
    ],
    'responses': {
        200: {
            'description': 'HTML page with password reset confirmation',
            'content': {
                'text/html': {
                    'schema': {'type': 'string'}
                }
            }
        },
        422: {'description': 'Unprocessable entity - Invalid or expired token'},
        500: {'description': 'Internal server error'}
    }
})
@handle_errors
def confirm_reset_password(token):
    return PasswordResetService.verify_reset_password_token(token)


# --- EmailConfirmService Routes ---
@auth_bp.route('/confirm-email/<token>', methods=['GET'])
@swag_from({
    'tags': ['Auth'],
    'summary': 'Confirm email address',
    'description': 'Verifies the email confirmation token and marks the user\'s email as confirmed. Returns an HTML confirmation page.',
    'parameters': [
        {
            'name': 'token',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'Email confirmation token received via email'
        }
    ],
    'responses': {
        200: {
            'description': 'HTML page with email confirmation success',
            'content': {
                'text/html': {
                    'schema': {'type': 'string'}
                }
            }
        },
        422: {'description': 'Unprocessable entity - Invalid or expired token'},
        500: {'description': 'Internal server error'}
    }
})
@handle_errors
def confirm_email(token):
    return EmailConfirmService.verify_email_token(token)
