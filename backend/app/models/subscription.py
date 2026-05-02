from app import db
from sqlalchemy.sql import func
import uuid


class Subscription(db.Model):
    __tablename__ = 'subscription'
    __table_args__ = (
        db.Index('idx_subscription_user_active', 'user_id', 'is_active'),
        db.Index('idx_subscription_user_id', 'user_id'),
        db.Index('idx_subscription_plan_id', 'plan_id'),
        db.Index('idx_subscription_start_date', 'start_date'),
    )

    subscription_id = db.Column(db.UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = db.Column(db.UUID(as_uuid=True), db.ForeignKey('user.user_id', ondelete='CASCADE'), nullable=False)
    plan_id = db.Column(db.UUID(as_uuid=True), db.ForeignKey('subscription_plan.plan_id', ondelete='CASCADE'), nullable=False)
    start_date = db.Column(db.DateTime(timezone=True), server_default=func.now())
    end_date = db.Column(db.DateTime, nullable=True)
    is_active = db.Column(db.Boolean, default=True)

    user = db.relationship('User', back_populates='subscriptions')
    plan = db.relationship('SubscriptionPlan', back_populates='subscriptions')
