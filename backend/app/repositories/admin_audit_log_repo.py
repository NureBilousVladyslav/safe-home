from app.models.admin_audit_log import AdminAuditLog
from datetime import datetime, timedelta, timezone
from app import db

class AdminAuditLogRepository:
    @staticmethod
    def to_dict(log):
        return {
            'log_id': str(log.log_id),
            'admin_id': str(log.admin_id),
            'admin_email': log.admin.email,
            'admin_name': log.admin.name,
            'action': log.action,
            'method': log.method,
            'action_details': log.action_details,
            'created_at': log.created_at.isoformat()
        }

    @staticmethod
    def get_all():
        return AdminAuditLog.query.order_by(AdminAuditLog.created_at.desc()).all()

    @staticmethod
    def get_audit_logs_by_days(days: int):
        end_date = datetime.now(timezone.utc)
        start_date = end_date - timedelta(days=days)
        return (AdminAuditLog.query
                .filter(AdminAuditLog.created_at >= start_date)
                .filter(AdminAuditLog.created_at <= end_date)
                .order_by(AdminAuditLog.created_at.desc())
                .all())

    @staticmethod
    def get_by_admin_id(admin_id):
        return AdminAuditLog.query.filter_by(admin_id=admin_id).order_by(AdminAuditLog.created_at.desc()).all()

    @staticmethod
    def add(admin_id, action, method, action_details=None):
        log = AdminAuditLog(
            admin_id=admin_id,
            action=action,
            method=method,
            action_details=action_details
        )
        db.session.add(log)
        db.session.commit()
        return log
