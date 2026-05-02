from flask import jsonify
from app.utils.error_handler import handle_errors, UnprocessableError
from app.utils import Validator
from app.repositories.user_stats_repo import UserStatsRepository
from app.repositories.subscription_plan_stats_repo import SubscriptionPlanStatsRepository
from app.repositories.subscription_plan_repo import SubscriptionPlanRepository
from app.repositories.subscription_repo import SubscriptionRepository

class StatsService:
    @staticmethod
    @handle_errors
    def get_user_stats(days: int):
        Validator.validate_positive_integer(days, "days")
        stats = UserStatsRepository.get_user_stats_by_days(days) or []
        user_stats = [
            {
                'stats_id': str(stat.stats_id),
                'date': stat.stats_date.isoformat(),
                'user_count': stat.user_count
            } for stat in stats
        ]
        return jsonify({"user_stats": user_stats}), 200

    @staticmethod
    @handle_errors
    def get_subscription_plan_stats(days: int, plan_id: str = None):
        Validator.validate_positive_integer(days, "days")

        def build_plan_stats(plan):
            stats = SubscriptionPlanStatsRepository.get_subscription_plan_stats_by_days_plan(days, plan.plan_id) or []
            if not stats:
                return None

            return {
                "plan_id": str(plan.plan_id),
                "plan_name": plan.name,
                "stats": [
                    {
                        "stats_id": str(stat.stats_id),
                        "date": stat.stats_date.isoformat(),
                        "user_count": stat.user_count,
                        "avg_homes": stat.avg_homes,
                        "avg_sensors": stat.avg_sensors
                    } for stat in stats
                ]
            }

        subscription_plan_stats = []
        if plan_id:
            plan = SubscriptionPlanRepository.get_by_id(plan_id)
            if not plan:
                raise UnprocessableError("Subscription plan not found.")
            plan_stats = build_plan_stats(plan)
            if plan_stats:
                subscription_plan_stats.append(plan_stats)
        else:
            plans = SubscriptionPlanRepository.get_all()
            for plan in plans:
                plan_stats = build_plan_stats(plan)
                if plan_stats:
                    subscription_plan_stats.append(plan_stats)

        return jsonify({"subscription_plans_stats": subscription_plan_stats}), 200

    @staticmethod
    @handle_errors
    def get_latest_subscription_plan_stats():
        plans = SubscriptionPlanRepository.get_all()
        subscription_plans_stats = []

        for plan in plans:
            stat = SubscriptionPlanStatsRepository.get_latest_stats_for_plan(plan.plan_id) or None
            if stat:
                user_count = SubscriptionRepository.get_count_active_subscriptions_by_plan(plan.plan_id) or 0
                subscription_plans_stats.append({
                    "stats_id": str(stat.stats_id),
                    "date": stat.stats_date.isoformat(),
                    "plan_id": str(stat.plan_id),
                    "plan_name": stat.plan.name,
                    "user_count": user_count,
                    "plan_max_homes": stat.plan.max_homes,
                    "plan_max_sensors": stat.plan.max_sensors,
                    "avg_homes": stat.avg_homes,
                    "avg_sensors": stat.avg_sensors
                })

        return jsonify({"subscription_plans_stats": subscription_plans_stats}), 200
