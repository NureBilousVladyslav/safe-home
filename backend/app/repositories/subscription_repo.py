from app.models.subscription import Subscription
from app import db

class SubscriptionRepository:
    @staticmethod
    def get_all_by_user(user_id):
        return (Subscription.query.filter_by(user_id=user_id).
                order_by(Subscription.start_date.desc()).all())

    @staticmethod
    def get_by_id(subscription_id):
        return Subscription.query.get(subscription_id)

    @staticmethod
    def get_by_user_and_id(user_id, subscription_id):
        return Subscription.query.filter_by(user_id=user_id, subscription_id=subscription_id).first()

    @staticmethod
    def get_active_by_user(user_id):
        return Subscription.query.filter_by(user_id=user_id, is_active=True).first()

    @staticmethod
    def get_count_active_subscriptions_by_plan(plan_id):
        return Subscription.query.filter_by(is_active=True, plan_id=plan_id).count()

    @staticmethod
    def get_active_subscriptions_ending_after(date):
        return Subscription.query.filter(
            Subscription.is_active == True,
            Subscription.end_date > date
        ).all()

    @staticmethod
    def get_active_subscriptions_ending_before(date):
        return Subscription.query.filter(
            Subscription.is_active == True,
            Subscription.end_date <= date
        ).all()

    @staticmethod
    def add(subscription):
        db.session.add(subscription)
        db.session.commit()

    @staticmethod
    def update(subscription):
        db.session.commit()

    @staticmethod
    def delete(subscription):
        db.session.delete(subscription)
        db.session.commit()
