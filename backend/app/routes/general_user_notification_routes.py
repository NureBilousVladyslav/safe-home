from flask import Blueprint, request
from app.services.general_user_notification_service import GeneralUserNotificationService
from app.utils.auth_decorator import auth_required, role_required
from app.utils.error_handler import handle_errors
from flasgger import swag_from

general_notification_bp = Blueprint('general_notification', __name__)


@general_notification_bp.route('/notifications', methods=['GET'])
@swag_from({
    'tags': ['Notification'],
    'summary': 'Get all notifications for the authenticated user',
    'responses': {
        200: {
            'description': 'List of notifications',
            'schema': {
                'type': 'object',
                'properties': {
                    'notifications': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'id': {'type': 'string'},
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
def get_notifications_by_user():
    user_id = request.current_user.user_id
    return GeneralUserNotificationService.get_notifications_by_user(user_id)
