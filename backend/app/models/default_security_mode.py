from app import db
import uuid

class DefaultSecurityMode(db.Model):
    __tablename__ = 'default_security_mode'
    __table_args__ = (
        db.Index('idx_default_security_mode_name', 'mode_name'),
    )

    mode_id = db.Column(db.UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    mode_name = db.Column(db.String(100), nullable=False)
    description = db.Column(db.String(255), nullable=True)
    is_selectable = db.Column(db.Boolean, default=False)

    homes = db.relationship('Home', back_populates='default_mode')
