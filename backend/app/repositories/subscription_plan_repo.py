from app.models.subscription_plan import SubscriptionPlan
from app import db

class SubscriptionPlanRepository:
    @staticmethod
    def get_all():
        return (SubscriptionPlan.query.
                order_by(SubscriptionPlan.price.asc()).all())

    @staticmethod
    def get_by_id(plan_id):
        return SubscriptionPlan.query.get(plan_id)

    @staticmethod
    def get_by_name(plan_name):
        return SubscriptionPlan.query.filter_by(name=plan_name).first()

    @staticmethod
    def add(plan):
        db.session.add(plan)
        db.session.commit()

    @staticmethod
    def update(plan):
        db.session.commit()

    @staticmethod
    def delete(plan):
        db.session.delete(plan)
        db.session.commit()
