from app import db
from app.models.user_stats import UserStats
from datetime import datetime, timedelta, timezone

class UserStatsRepository:
    @staticmethod
    def get_user_stats_by_days(days: int):
        end_date = datetime.now(timezone.utc)
        start_date = end_date - timedelta(days=days)
        return (UserStats.query
                .filter(UserStats.stats_date >= start_date)
                .filter(UserStats.stats_date <= end_date)
                .order_by(UserStats.stats_date.asc())
                .all())

    @staticmethod
    def add_user_stats(user_count: int):
        stats = UserStats(user_count=user_count)
        db.session.add(stats)
        db.session.commit()
        return stats
