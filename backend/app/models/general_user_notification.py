from app import db
from sqlalchemy.sql import func
import uuid

class GeneralUserNotification(db.Model):
    __tablename__ = 'general_user_notifications'
    __table_args__ = (
        db.Index('idx_general_notifications_user_id', 'user_id'),
        db.Index('idx_general_notifications_created_at', 'created_at'),
    )

    id = db.Column(db.UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = db.Column(db.UUID(as_uuid=True), db.ForeignKey('user.user_id', ondelete='CASCADE'), nullable=False)
    title = db.Column(db.String(255), nullable=False)
    body = db.Column(db.Text, nullable=False)
    importance = db.Column(db.Enum('low', 'medium', 'high', name='importance_enum'), nullable=False)
    created_at = db.Column(db.DateTime(timezone=True), server_default=func.now())
    type = db.Column(db.String(50), nullable=False)
    data = db.Column(db.JSON, nullable=True)

    user = db.relationship('User', back_populates='general_notifications')
