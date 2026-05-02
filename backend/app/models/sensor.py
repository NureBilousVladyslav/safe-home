from app import db
from sqlalchemy.sql import func
import sqlalchemy as sa
import uuid
import base36

class Sensor(db.Model):
    __tablename__ = 'sensor'
    __table_args__ = (
        db.Index('idx_sensor_home_id', 'home_id'),
        db.Index('idx_sensor_is_active', 'is_active'),
        db.Index('idx_sensor_is_closed', 'is_closed'),
        db.Index('idx_sensor_is_breached', 'is_security_breached'),
        db.Index('idx_sensor_is_archived', 'is_archived'),
        db.Index('idx_sensor_home_is_archived', 'home_id', 'is_archived'),
        db.Index('idx_sensor_user_is_archived', 'user_id', 'is_archived'),
        db.Index('idx_sensor_short_id', 'short_id'),
        db.Index('idx_sensor_user_short_id', 'user_id', 'short_id'),
        db.Index('idx_sensor_user_short_id_archived', 'user_id', 'short_id', 'is_archived')
    )

    sensor_id = db.Column(db.UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    short_id = db.Column(db.Integer, nullable=False, unique=True, index=True,
                         server_default=sa.text("nextval('sensor_short_id_seq')"))
    home_id = db.Column(
        db.UUID(as_uuid=True),
        db.ForeignKey('home.home_id', ondelete='CASCADE'),
        nullable=False
    )
    user_id = db.Column(db.UUID(as_uuid=True), nullable=False)
    name = db.Column(db.String(100), nullable=False)
    type = db.Column(db.String(50), nullable=False)
    is_closed = db.Column(db.Boolean, default=False)
    is_active = db.Column(db.Boolean, default=False)
    is_security_breached = db.Column(db.Boolean, default=False)
    is_archived = db.Column(db.Boolean, default=False)
    created_at = db.Column(db.DateTime(timezone=True), server_default=func.now())

    home = db.relationship('Home', back_populates='sensors')

    @property
    def short_code(self):
        """Converts the short_id to a 6-character base36 code."""
        return base36.dumps(self.short_id).zfill(6).upper()[:6]