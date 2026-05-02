from app import db
from sqlalchemy.sql import func
import uuid


class MobileDevice(db.Model):
    __tablename__ = 'mobile_device'
    __table_args__ = (
        db.Index('idx_mobile_device_user_id', 'user_id'),
        db.Index('idx_mobile_device_token', 'device_token'),
        db.Index('idx_mobile_device_user_token', 'user_id', 'device_token'),
    )

    user_device_id = db.Column(
        db.UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4
    )
    user_id = db.Column(
        db.UUID(as_uuid=True),
        db.ForeignKey('user.user_id', ondelete='CASCADE'),
        nullable=False
    )
    device_token = db.Column(db.String, nullable=False)
    device_info = db.Column(db.String(256), nullable=True)
    created_at = db.Column(db.DateTime(timezone=True), server_default=func.now())

    user = db.relationship('User', back_populates='devices')
