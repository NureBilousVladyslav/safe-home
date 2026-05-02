from app import db
import uuid


class Role(db.Model):
    __tablename__ = 'role'
    __table_args__ = (
        db.Index('idx_role_name', 'role_name'),
    )

    role_id = db.Column(db.UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    role_name = db.Column(db.String(100), nullable=False)
    description = db.Column(db.String(255), nullable=True)

    users = db.relationship(
        'User',
        back_populates='role',
        cascade="all, delete-orphan"
    )
