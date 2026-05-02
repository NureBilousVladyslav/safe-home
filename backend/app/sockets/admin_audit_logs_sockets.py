from flask_socketio import SocketIO, emit, join_room
from app.repositories import AdminAuditLogRepository
from app.utils.auth_socket_decorator import socketio_role_required
from app.utils import Validator
from app.utils.error_handler import socketio_handle_errors

def admin_audit_logs_sockets(socketio):
    @socketio.on('subscribe_admin_audit_logs')
    @socketio_handle_errors
    @socketio_role_required(['admin', 'super_admin'])
    def handle_subscribe_admin_audit_logs(data):
        days = data.get('days')

        if days is not None:
            Validator.validate_positive_integer(days, "days")
            logs = AdminAuditLogRepository.get_audit_logs_by_days(days)
        else:
            logs = AdminAuditLogRepository.get_all()

        join_room('admin_audit_logs')

        emit('admin_audit_logs_init', {
            'admin_audit_logs': [AdminAuditLogRepository.to_dict(log) for log in logs]
        })
