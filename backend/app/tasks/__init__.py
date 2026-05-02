from .subscription_task import SubscriptionTask
from .user_stats_task import UserStatsTask
from .subscription_plan_stats_task import SubscriptionPlanStatsTask

def init_tasks(app, scheduler):
    scheduler.add_job(
        id='check_subscription_ending',
        func=lambda: SubscriptionTask.check_subscription_expiration(app),
        trigger='interval',
        minutes=20,
        max_instances=1
    )
    scheduler.add_job(
        id='notify_subscription_ending',
        func=lambda: SubscriptionTask.notify_subscription_expiration(app),
        trigger='cron',
        hour=12,
        minute=0,
        max_instances=1
    )
    scheduler.add_job(
        id='collect_user_stats',
        func=lambda: UserStatsTask.collect_user_stats(app),
        trigger='interval',
        hours=2,
        max_instances=1
    )
    scheduler.add_job(
        id='collect_subscription_plan_stats',
        func=lambda: SubscriptionPlanStatsTask.collect_subscription_plan_stats(app),
        trigger='interval',
        hours=2,
        max_instances=1
    )
