from app import db
from sqlalchemy.sql import func
import uuid

class Home(db.Model):
    __tablename__ = 'home'
    __table_args__ = (
        db.Index('idx_home_user_id', 'user_id'),
        db.Index('idx_home_default_mode_id', 'default_mode_id'),
        db.Index('idx_home_is_archived', 'is_archived'),
        db.Index('idx_home_user_is_archived', 'user_id', 'is_archived'),
    )

    home_id = db.Column(db.UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = db.Column(db.UUID(as_uuid=True), db.ForeignKey('user.user_id', ondelete='CASCADE'), nullable=False)
    default_mode_id = db.Column(db.UUID(as_uuid=True), db.ForeignKey('default_security_mode.mode_id'), nullable=False)
    name = db.Column(db.String(100), nullable=False)
    address = db.Column(db.String(255), nullable=False)
    created_at = db.Column(db.DateTime(timezone=True), server_default=func.now())
    is_archived = db.Column(db.Boolean, default=False)

    user = db.relationship('User', back_populates='homes')
    default_mode = db.relationship('DefaultSecurityMode', back_populates='homes')

    sensors = db.relationship(
        'Sensor',
        back_populates='home',
        cascade="all, delete-orphan"
    )

    security_notifications = db.relationship(
        'SecurityUserNotification',
        back_populates='home',
        cascade="all, delete-orphan"
    )
