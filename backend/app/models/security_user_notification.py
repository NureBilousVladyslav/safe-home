from app import db
import uuid
from sqlalchemy.sql import func

class SecurityUserNotification(db.Model):
    __tablename__ = 'security_user_notifications'
    __table_args__ = (
        db.Index('idx_security_user_notifications_home_id', 'home_id'),
        db.Index('idx_security_user_notifications_user_id', 'user_id'),
        db.Index('idx_security_user_notifications_home_created', 'home_id', 'created_at'),
        db.Index('idx_security_user_notifications_user_created', 'user_id', 'created_at'),
    )

    id = db.Column(db.UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    home_id = db.Column(db.UUID(as_uuid=True), db.ForeignKey('home.home_id', ondelete='CASCADE'), nullable=False)
    sensor_id = db.Column(db.UUID(as_uuid=True), nullable=True)
    user_id = db.Column(db.UUID(as_uuid=True), nullable=False)
    title = db.Column(db.String(255), nullable=False)
    body = db.Column(db.Text, nullable=False)
    importance = db.Column(db.Enum('low', 'medium', 'high', name='importance_enum'), nullable=False)
    created_at = db.Column(db.DateTime(timezone=True), server_default=func.now())
    type = db.Column(db.String(50), nullable=False)
    data = db.Column(db.JSON, nullable=True)

    home = db.relationship('Home', back_populates='security_notifications')
