from app import db
import uuid


class SubscriptionPlan(db.Model):
    __tablename__ = 'subscription_plan'
    __table_args__ = (
        db.Index('idx_subscription_plan_name', 'name'),
    )

    plan_id = db.Column(db.UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    name = db.Column(db.String(100), nullable=False)
    max_homes = db.Column(db.Integer, nullable=False)
    max_sensors = db.Column(db.Integer, nullable=False)
    price = db.Column(db.Float, nullable=False)
    duration_days = db.Column(db.Integer, nullable=False)

    subscriptions = db.relationship('Subscription', back_populates='plan')
    stats = db.relationship('SubscriptionPlanStats', back_populates='plan')
