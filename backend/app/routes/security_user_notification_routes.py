from flask import Blueprint, request
from app.services.security_user_notification_service import SecurityUserNotificationService
from app.utils.auth_decorator import auth_required, role_required
from app.utils.error_handler import handle_errors
from flasgger import swag_from

security_notification_bp = Blueprint('security_notification', __name__)


@security_notification_bp.route('/security-notifications/<home_id>', methods=['GET'])
@swag_from({
    'tags': ['Notification'],
    'summary': 'Get security notifications for the authenticated user and home',
    'parameters': [
        {
            'name': 'home_id',
            'in': 'path',
            'required': True,
            'type': 'string'
        }
    ],
    'responses': {
        200: {
            'description': 'List of security notifications',
            'schema': {
                'type': 'object',
                'properties': {
                    'notifications': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'id': {'type': 'string'},
                                'home_id': {'type': 'string'},
                                'sensor_id': {'type': 'string'},
                                'title': {'type': 'string'},
                                'body': {'type': 'string'},
                                'importance': {'type': 'string'},
                                'created_at': {'type': 'string'},
                                'type': {'type': 'string'},
                                'data': {'type': 'object'}
                            }
                        }
                    }
                }
            }
        },
        401: {'description': 'Unauthorized'},
        500: {'description': 'Internal server error'}
    }
})
@auth_required
@role_required(['user'])
@handle_errors
def get_notifications_by_user_and_home(home_id):
    user_id = request.current_user.user_id
    return SecurityUserNotificationService.get_notifications_by_user_and_home(user_id, home_id)


@security_notification_bp.route('/security-notifications', methods=['GET'])
@swag_from({
    'tags': ['Notification'],
    'summary': 'Get all security notifications for the authenticated user',
    'responses': {
        200: {
            'description': 'List of security notifications',
            'schema': {
                'type': 'object',
                'properties': {
                    'notifications': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'id': {'type': 'string'},
                                'home_id': {'type': 'string'},
                                'sensor_id': {'type': 'string'},
                                'title': {'type': 'string'},
                                'body': {'type': 'string'},
                                'importance': {'type': 'string'},
                                'created_at': {'type': 'string'},
                                'type': {'type': 'string'},
                                'data': {'type': 'object'}
                            }
                        }
                    }
                }
            }
        },
        401: {'description': 'Unauthorized'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def get_security_notifications_by_user():
    user_id = request.current_user.user_id
    return SecurityUserNotificationService.get_notifications_by_user(user_id)
