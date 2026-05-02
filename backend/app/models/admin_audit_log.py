import uuid
from sqlalchemy.sql import func
from app import db


class AdminAuditLog(db.Model):
    __tablename__ = 'admin_audit_log'
    __table_args__ = (
        db.Index('idx_admin_audit_logs_admin_id', 'admin_id'),
        db.Index('idx_admin_audit_logs_created_at', 'created_at'),
        db.Index('idx_admin_audit_logs_admin_created', 'admin_id', 'created_at'),
    )

    log_id = db.Column(db.UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    admin_id = db.Column(db.UUID(as_uuid=True), db.ForeignKey('user.user_id', ondelete='CASCADE'), nullable=False)
    action = db.Column(db.String(255), nullable=False)
    method = db.Column(db.Enum('post', 'put', 'patch', 'delete', name='method_enum'), nullable=False)
    action_details = db.Column(db.JSON, nullable=True)
    created_at = db.Column(db.DateTime(timezone=True), server_default=func.now())

    admin = db.relationship('User', back_populates='admin_audit_logs')


