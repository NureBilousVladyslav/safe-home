import logging
from app.repositories.user_stats_repo import UserStatsRepository
from app.repositories.role_repo import RoleRepository
from app.models.user import User
from app import db
from sqlalchemy.sql import func
from sqlalchemy.exc import SQLAlchemyError

logger = logging.getLogger(__name__)


class UserStatsTask:
    @staticmethod
    def collect_user_stats(app):
        with app.app_context():
            logger.info("Starting user stats collection")
            try:
                # Get "user" role
                role = RoleRepository.get_by_name("user")
                if not role:
                    logger.error("Role 'user' not found, skipping user stats collection")
                    return

                user_count = db.session.query(func.count(User.user_id)).filter(User.role_id == role.role_id).scalar()
                UserStatsRepository.add_user_stats(user_count=user_count)
                logger.info(f"Collected user stats: {user_count} users")
            except SQLAlchemyError as e:
                logger.error(f"Database error in user stats collection: {str(e)}", exc_info=True)
                db.session.rollback()
            except Exception as e:
                logger.error(f"Unexpected error in user stats collection: {str(e)}", exc_info=True)
