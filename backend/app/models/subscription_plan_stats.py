from app import db
from sqlalchemy.sql import func
import uuid

class SubscriptionPlanStats(db.Model):
    __tablename__ = 'subscription_plan_stats'
    __table_args__ = (
        db.Index('idx_subscription_plan_stats_plan', 'plan_id'),
        db.Index('idx_subscription_plan_stats_date', 'stats_date'),
        db.Index('idx_subscription_plan_stats_date_plan', 'stats_date', 'plan_id'),
    )

    stats_id = db.Column(db.UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    stats_date = db.Column(db.DateTime(timezone=True), server_default=func.now())
    plan_id = db.Column(db.UUID(as_uuid=True), db.ForeignKey('subscription_plan.plan_id', ondelete='CASCADE'), nullable=False)
    user_count = db.Column(db.Integer, nullable=False)
    avg_homes = db.Column(db.Float, nullable=False)
    avg_sensors = db.Column(db.Float, nullable=False)

    plan = db.relationship('SubscriptionPlan', back_populates='stats')
