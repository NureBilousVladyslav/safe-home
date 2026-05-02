import stripe
from app.services.notification import SubscriptionEmailService, SubscriptionNotificationService
from app.utils.error_handler import handle_errors, UnprocessableError, ValidationError
from app.repositories.subscription_repo import SubscriptionRepository
from app.repositories.user_repo import UserRepository
from app.repositories.subscription_plan_repo import SubscriptionPlanRepository
from app.models.subscription import Subscription
from flask import jsonify, g, url_for
from datetime import datetime, timezone, timedelta

class SubscriptionService:
    @staticmethod
    def get_current_subscription(user_id):
        current_subscription = SubscriptionRepository.get_active_by_user(user_id)
        if not current_subscription:
            raise UnprocessableError("No active subscription found for the user.")
        return current_subscription

    @staticmethod
    @handle_errors
    def get_current_subscription_info(user_id):
        current_subscription = SubscriptionRepository.get_active_by_user(user_id)
        if not current_subscription:
            raise UnprocessableError("No active subscription found for the user.")

        subscription_info = {
            "subscription_id": str(current_subscription.subscription_id),
            "user_id": str(current_subscription.user_id),
            "plan": {
                "plan_id": str(current_subscription.plan.plan_id),
                "name": current_subscription.plan.name,
                "price": current_subscription.plan.price,
                "duration_days": current_subscription.plan.duration_days,
            },
            "start_date": current_subscription.start_date.isoformat(),
            "end_date": current_subscription.end_date.isoformat() if current_subscription.end_date else None,
            "is_active": current_subscription.is_active,
        }
        return jsonify(subscription_info), 200

    @staticmethod
    @handle_errors
    def get_user_subscriptions(user_id):
        subscriptions = SubscriptionRepository.get_all_by_user(user_id)
        if not subscriptions:
            raise UnprocessableError("No subscriptions found for the user.")

        subscriptions_list = [
            {
                "subscription_id": str(subscription.subscription_id),
                "user_id": str(subscription.user_id),
                "plan": {
                    "plan_id": str(subscription.plan.plan_id),
                    "name": subscription.plan.name,
                    "price": subscription.plan.price,
                    "duration_days": subscription.plan.duration_days,
                },
                "start_date": subscription.start_date.isoformat(),
                "end_date": subscription.end_date.isoformat() if subscription.end_date else None,
                "is_active": subscription.is_active,
            } for subscription in subscriptions
        ]
        return jsonify({"subscriptions": subscriptions_list}), 200

    @staticmethod
    @handle_errors
    def cancel_current_subscription(user_id):
        current_subscription = SubscriptionRepository.get_active_by_user(user_id)
        if not current_subscription:
            raise UnprocessableError("No active subscription found for the user.")

        if current_subscription.plan.name == 'basic':
            raise UnprocessableError("Cannot cancel a basic subscription.")

        current_subscription.is_active = False
        current_subscription.end_date = datetime.now(timezone.utc)
        SubscriptionRepository.update(current_subscription)

        user = UserRepository.get_by_id(user_id)

        g.old_data = {
            'user_id': str(user.user_id),
            'name': user.name,
            'email': user.email,
            'plan': current_subscription.plan.name,
        }

        g.new_data = {
            'user_id': str(user.user_id),
            'name': user.name,
            'email': user.email,
            'plan': 'basic',
        }

        SubscriptionService.create_basic_subscription(user_id)

        from app.repositories import HomeRepository
        from app.services import HomeService
        # Archive homes and sensors
        user_homes = HomeRepository.get_all_by_user(current_subscription.user_id)
        for home in user_homes:
            if not home.is_archived:
                HomeService.archive_home(home)

        # Send notifications
        user = current_subscription.user
        if user.email_confirmed:
            SubscriptionEmailService.send_subscription_canceled_email(user, current_subscription)

        SubscriptionNotificationService.send_subscription_canceled_notification(user, current_subscription)

        return jsonify({"message": "Subscription cancelled successfully."}), 200

    @staticmethod
    @handle_errors
    def create_basic_subscription(user_id):
        existing_subscription = SubscriptionRepository.get_active_by_user(user_id)
        if existing_subscription:
            raise UnprocessableError("User already has an active subscription.")

        plan = SubscriptionPlanRepository.get_by_name('basic')
        if not plan:
            raise UnprocessableError("Subscription plan 'basic' not found.")

        new_subscription = Subscription(
            user_id=user_id,
            plan_id=plan.plan_id,
            end_date=datetime.now(timezone.utc) + timedelta(days=plan.duration_days),
            is_active=True
        )
        user = UserRepository.get_by_id(user_id)
        user.subscription_plan_name = plan.name
        UserRepository.update(user)

        SubscriptionRepository.add(new_subscription)

    @staticmethod
    def create_stripe_checkout_session(user_id, plan_id):
        plan = SubscriptionPlanRepository.get_by_id(plan_id)
        if not plan:
            raise UnprocessableError("Subscription plan not found.")
        if plan.name == 'basic':
            raise UnprocessableError("Cannot purchase a basic plan as a paid subscription.")

        current_subscription = SubscriptionRepository.get_active_by_user(user_id)
        if not current_subscription:
            raise UnprocessableError("User does not have an active subscription.")
        if current_subscription.plan.name not in ['basic', plan.name]:
            raise UnprocessableError(
                f"You already have '{current_subscription.plan.name}' subscription. "
                f"Cancel it first to purchase another paid subscription."
            )

        session = stripe.checkout.Session.create(
            payment_method_types=['card'],
            line_items=[{
                'price_data': {
                    'currency': 'usd',
                    'product_data': {
                        'name': plan.name,
                        'description': f"Homes allowed: {plan.max_homes}, Sensors allowed: {plan.max_sensors}, duration: {plan.duration_days} days",
                    },
                    'unit_amount': int(plan.price * 100),
                },
                'quantity': 1,
            }],
            mode='payment',
            success_url=url_for('payments.payment_success', _external=True),
            cancel_url=url_for('payments.payment_cancel', _external=True),
            metadata={'user_id': str(user_id), 'plan_id': str(plan_id), 'plan_name': plan.name},
        )
        return session.url

    @staticmethod
    def handle_stripe_webhook(event):
        if event['type'] == 'checkout.session.completed':
            session = event['data']['object']
            user_id = session['metadata']['user_id']
            plan_id = session['metadata']['plan_id']

            current_subscription = SubscriptionRepository.get_active_by_user(user_id)
            if not current_subscription:
                raise UnprocessableError("No active subscription found for the user.")

            if current_subscription.plan.name == 'basic':
                SubscriptionService.purchase_paid_subscription(user_id, plan_id)
            elif current_subscription.plan.name == session['metadata']['plan_name']:
                SubscriptionService.extend_subscription(user_id)

        elif event['type'] == 'payment_intent.payment_failed':
            session = event['data']['object']
            user_id = session['metadata']['user_id']
            plan_name = session['metadata']['plan_name']
            user = UserRepository.get_by_id(user_id)

            SubscriptionEmailService.send_subscription_payment_failed_email(user, plan_name)

    @staticmethod
    @handle_errors
    def purchase_paid_subscription(user_id, plan_id):
        plan = SubscriptionPlanRepository.get_by_id(plan_id)
        if not plan:
            raise UnprocessableError("Subscription plan not found.")

        if plan.name == 'basic':
            raise UnprocessableError("Cannot purchase a basic plan as a paid subscription.")

        existing_subscription = SubscriptionRepository.get_active_by_user(user_id)
        if existing_subscription:
            existing_subscription.is_active = False
            existing_subscription.end_date = datetime.now(timezone.utc)
            SubscriptionRepository.update(existing_subscription)

        new_subscription = Subscription(
            user_id=user_id,
            plan_id=plan_id,
            end_date=datetime.now(timezone.utc) + timedelta(days=plan.duration_days),
            is_active=True
        )
        user = UserRepository.get_by_id(user_id)
        user.subscription_plan_name = plan.name
        UserRepository.update(user)

        SubscriptionRepository.add(new_subscription)

    @staticmethod
    @handle_errors
    def extend_subscription(user_id):
        active_subscription = SubscriptionRepository.get_active_by_user(user_id)
        if not active_subscription:
            raise UnprocessableError("User does not have an active subscription.")

        active_subscription.end_date += timedelta(days=active_subscription.plan.duration_days)
        SubscriptionRepository.update(active_subscription)
