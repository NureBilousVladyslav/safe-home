from app import db
from app.models.subscription_plan_stats import SubscriptionPlanStats
from datetime import datetime, timedelta, timezone

class SubscriptionPlanStatsRepository:
    @staticmethod
    def get_subscription_plan_stats_by_days_plan(days: int, plan_id: str = None):
        end_date = datetime.now(timezone.utc)
        start_date = end_date - timedelta(days=days)
        query = (SubscriptionPlanStats.query
                 .filter(SubscriptionPlanStats.stats_date >= start_date)
                 .filter(SubscriptionPlanStats.stats_date <= end_date))
        if plan_id:
            query = query.filter(SubscriptionPlanStats.plan_id == plan_id)
        return query.order_by(SubscriptionPlanStats.stats_date.asc()).all()

    @staticmethod
    def get_latest_stats_for_plan(plan_id: str):
        return (SubscriptionPlanStats.query
                .filter(SubscriptionPlanStats.plan_id == plan_id)
                .order_by(SubscriptionPlanStats.stats_date.desc())
                .first())

    @staticmethod
    def add_subscription_plan_stats(plan_id: str, user_count: int, avg_homes: float, avg_sensors: float):
        stats = SubscriptionPlanStats(
            plan_id=plan_id,
            user_count=user_count,
            avg_homes=avg_homes,
            avg_sensors=avg_sensors
        )
        db.session.add(stats)
        db.session.commit()
        return stats


