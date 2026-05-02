from app import db
from sqlalchemy.sql import func
import uuid

class UserStats(db.Model):
    __tablename__ = 'user_stats'
    __table_args__ = (
        db.Index('idx_user_stats_date', 'stats_date'),
    )

    stats_id = db.Column(db.UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    stats_date = db.Column(db.DateTime(timezone=True), server_default=func.now())
    user_count = db.Column(db.Integer, nullable=False)
    