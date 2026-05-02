from flask import Blueprint, jsonify
from app.services.default_security_mode_service import DefaultSecurityModeService
from app.utils.auth_decorator import auth_required, role_required
from app.utils.error_handler import handle_errors
from flasgger import swag_from

default_security_mode_bp = Blueprint('default_security_mode', __name__)


@default_security_mode_bp.route('/default-security-modes', methods=['GET'])
@swag_from({
    'tags': ['Default Security Mode'],
    'summary': 'Get all default security modes',
    'responses': {
        200: {
            'description': 'List of default security modes',
            'schema': {
                'type': 'object',
                'properties': {
                    'default_modes': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'mode_id': {'type': 'string'},
                                'mode_name': {'type': 'string'},
                                'description': {'type': 'string'},
                                'is_selectable': {'type': 'boolean'}
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
def get_all_default_modes():
    return DefaultSecurityModeService.get_all_default_modes()
