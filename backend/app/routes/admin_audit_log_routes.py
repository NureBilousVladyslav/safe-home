from flask import Blueprint, jsonify, request
from app.services.admin_audit_log_service import AdminAuditLogService
from app.utils.auth_decorator import auth_required, role_required
from app.utils.error_handler import handle_errors
from flasgger import swag_from

admin_audit_log_bp = Blueprint('admin_audit_log', __name__)

@admin_audit_log_bp.route('/admin-audit-logs', methods=['GET'])
@swag_from({
    'tags': ['Admin Audit Log'],
    'summary': 'Get all admin audit logs (admin only)',
    'description': 'Returns a list of all admin audit logs. Restricted to admin users.',
    'parameters': [
        {
            'name': 'days',
            'in': 'query',
            'type': 'integer',
            'required': False,
            'description': 'Number of days to filter logs (optional, e.g., ?days=5)'
        }
    ],
    'responses': {
        200: {
            'description': 'List of admin audit logs',
            'schema': {
                'type': 'object',
                'properties': {
                    'admin_audit_logs': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'log_id': {'type': 'string'},
                                'admin_id': {'type': 'string'},
                                'admin_email': {'type': 'string'},
                                'admin_name': {'type': 'string'},
                                'action': {'type': 'string'},
                                'method': {'type': 'string'},
                                'action_details': {'type': 'object'},
                                'created_at': {'type': 'string'}
                            }
                        }
                    }
                }
            }
        },
        401: {'description': 'Unauthorized - Admin role required'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['admin', 'super_admin'])
@handle_errors
def get_all_admin_audit_logs():
    days = request.args.get('days', type=int)
    return AdminAuditLogService.get_admin_audit_logs(days)
