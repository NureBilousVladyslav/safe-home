import logging
from datetime import datetime, timezone, timedelta
from app.services import HomeService, SubscriptionService
from app.repositories import SubscriptionRepository, SubscriptionPlanRepository, HomeRepository
from app.services.notification import SubscriptionNotificationService, SubscriptionEmailService
from app import db
from sqlalchemy.exc import SQLAlchemyError

logger = logging.getLogger(__name__)

class SubscriptionTask:
    @staticmethod
    def notify_subscription_expiration(app):
        with app.app_context():
            logger.info("Starting subscription expiration notification task")
            try:
                notification_days = [5, 3, 1]
                now = datetime.now(timezone.utc)

                subscriptions = SubscriptionRepository.get_active_subscriptions_ending_after(now)

                for subscription in subscriptions:
                    try:
                        days_left = (subscription.end_date - now).days
                        if days_left in notification_days:
                            user = subscription.user
                            logger.info(f"Processing notification for user {user.user_id}, days left: {days_left}")

                            if user.email_confirmed:
                                SubscriptionEmailService.send_subscription_expiration_email(user, subscription)
                                logger.info(f"Sent expiration email to user {user.user_id}")

                            SubscriptionNotificationService.send_subscription_expiration_notification(user, subscription, days_left)
                            logger.info(f"Sent expiration notification to user {user.user_id}")
                    except SQLAlchemyError as e:
                        logger.error(f"Database error processing subscription {subscription.subscription_id}: {str(e)}", exc_info=True)
                        db.session.rollback()
                        continue
                    except Exception as e:
                        logger.error(f"Unexpected error processing subscription {subscription.subscription_id}: {str(e)}", exc_info=True)
                        continue
                logger.info("Completed subscription expiration notification task")
            except SQLAlchemyError as e:
                logger.error(f"Database error in notify_subscription_expiration: {str(e)}", exc_info=True)
                db.session.rollback()
            except Exception as e:
                logger.error(f"Unexpected error in notify_subscription_expiration: {str(e)}", exc_info=True)

    @staticmethod
    def check_subscription_expiration(app):
        with app.app_context():
            logger.info("Starting subscription expiration check task")
            try:
                now = datetime.now(timezone.utc)
                subscriptions_ending = SubscriptionRepository.get_active_subscriptions_ending_before(now)

                basic_plan = SubscriptionPlanRepository.get_by_name("basic")
                if not basic_plan:
                    logger.error("Basic plan not found, skipping subscription expiration check")
                    return

                for subscription in subscriptions_ending:
                    try:
                        logger.info(f"Processing subscription {subscription.subscription_id} for user {subscription.user_id}")
                        if subscription.plan_id == basic_plan.plan_id:
                            subscription.end_date = now + timedelta(days=subscription.plan.duration_days)
                            SubscriptionRepository.update(subscription)
                            logger.info(f"Extended basic subscription {subscription.subscription_id}")
                        else:
                            # Cancel expired subscription
                            subscription.is_active = False
                            subscription.end_date = now
                            SubscriptionRepository.update(subscription)
                            logger.info(f"Canceled subscription {subscription.subscription_id}")

                            # Create basic plan subscription
                            SubscriptionService.create_basic_subscription(user_id=subscription.user_id)
                            logger.info(f"Created basic subscription for user {subscription.user_id}")

                            # Archive homes and sensors
                            user_homes = HomeRepository.get_all_by_user(subscription.user_id)
                            for home in user_homes:
                                if not home.is_archived:
                                    HomeService.archive_home(home)
                                    logger.info(f"Archived home {home.home_id} for user {subscription.user_id}")

                            # Send notifications
                            user = subscription.user
                            if user.email_confirmed:
                                SubscriptionEmailService.send_subscription_canceled_email(user, subscription)
                                logger.info(f"Sent cancellation email to user {user.user_id}")

                            SubscriptionNotificationService.send_subscription_canceled_notification(user, subscription)
                            logger.info(f"Sent cancellation notification to user {user.user_id}")
                    except SQLAlchemyError as e:
                        logger.error(f"Database error processing subscription {subscription.subscription_id}: {str(e)}", exc_info=True)
                        db.session.rollback()
                        continue
                    except Exception as e:
                        logger.error(f"Unexpected error processing subscription {subscription.subscription_id}: {str(e)}", exc_info=True)
                        continue
                logger.info("Completed subscription expiration check task")
            except SQLAlchemyError as e:
                logger.error(f"Database error in check_subscription_expiration: {str(e)}", exc_info=True)
                db.session.rollback()
            except Exception as e:
                logger.error(f"Unexpected error in check_subscription_expiration: {str(e)}", exc_info=True)
