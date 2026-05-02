import logging
from app.repositories.subscription_plan_stats_repo import SubscriptionPlanStatsRepository
from app.repositories.role_repo import RoleRepository
from app.models.user import User
from app.models.subscription import Subscription
from app.models.home import Home
from app.models.sensor import Sensor
from app import db
from sqlalchemy.sql import func
from sqlalchemy.exc import SQLAlchemyError

logger = logging.getLogger(__name__)


class SubscriptionPlanStatsTask:
    @staticmethod
    def collect_subscription_plan_stats(app):
        with app.app_context():
            logger.info("Starting subscription plan stats collection")
            try:
                # Get "user" role
                role = RoleRepository.get_by_name("user")
                if not role:
                    logger.error("Role 'user' not found, skipping subscription plan stats collection")
                    return

                # Subquery for users with "user" role and active subscriptions
                user_subquery = (db.session.query(User.user_id)
                                 .filter(User.role_id == role.role_id)
                                 .subquery().select())

                # Get all subscription plans with active subscriptions
                active_subscriptions = (db.session.query(
                    Subscription.plan_id,
                    func.count(Subscription.user_id).label('user_count')
                )
                                        .join(user_subquery, Subscription.user_id == user_subquery.c.user_id)
                                        .filter(Subscription.is_active == True)
                                        .group_by(Subscription.plan_id)
                                        .all())

                # For each plan, calculate averages
                for plan in active_subscriptions:
                    try:
                        plan_id = plan.plan_id
                        user_count = plan.user_count

                        if user_count > 0:
                            # Average homes per user
                            homes_subquery = (
                                db.session.query(Home.user_id, func.count(Home.home_id).label('home_count'))
                                .join(Subscription, Home.user_id == Subscription.user_id)
                                .filter(Subscription.plan_id == plan_id,
                                        Subscription.is_active == True,
                                        Home.is_archived == False,
                                        Home.user_id.in_(user_subquery))
                                .group_by(Home.user_id)
                                .subquery())
                            avg_homes = (db.session.query(func.avg(homes_subquery.c.home_count))
                                         .scalar()) or 0

                            # Average sensors per user (across all their homes)
                            sensors_subquery = (
                                db.session.query(Home.user_id, func.count(Sensor.sensor_id).label('sensor_count'))
                                .join(Home, Sensor.home_id == Home.home_id)
                                .join(Subscription, Home.user_id == Subscription.user_id)
                                .filter(Subscription.plan_id == plan_id,
                                        Subscription.is_active == True,
                                        Home.is_archived == False,
                                        Sensor.is_archived == False,
                                        Home.user_id.in_(user_subquery))
                                .group_by(Home.user_id)
                                .subquery())
                            avg_sensors = (db.session.query(func.avg(sensors_subquery.c.sensor_count))
                                           .scalar()) or 0

                            SubscriptionPlanStatsRepository.add_subscription_plan_stats(
                                plan_id=plan_id,
                                user_count=user_count,
                                avg_homes=avg_homes,
                                avg_sensors=avg_sensors
                            )
                            logger.info(
                                f"Collected stats for plan {plan_id}: {user_count} users, {avg_homes} avg homes, {avg_sensors} avg sensors")
                        else:
                            logger.info(f"No users with active subscriptions for plan {plan_id}")
                    except AttributeError as e:
                        logger.error(f"Attribute error for plan {plan.plan_id}, likely import issue: {str(e)}",
                                     exc_info=True)
                        logger.error(f"Check if 'db' is correctly imported from 'app'. Current db: {db}")
                        continue
                    except SQLAlchemyError as e:
                        logger.error(f"Database error for plan {plan.plan_id}: {str(e)}", exc_info=True)
                        db.session.rollback()
                        continue
                    except Exception as e:
                        logger.error(f"Unexpected error for plan {plan.plan_id}: {str(e)}", exc_info=True)
                        continue
            except SQLAlchemyError as e:
                logger.error(f"Database error in subscription plan stats collection: {str(e)}", exc_info=True)
                db.session.rollback()
            except Exception as e:
                logger.error(f"Unexpected error in subscription plan stats collection: {str(e)}", exc_info=True)
