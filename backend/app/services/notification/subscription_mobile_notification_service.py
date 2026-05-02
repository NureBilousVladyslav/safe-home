from app.utils.error_handler import handle_errors, UnprocessableError
from app.services import GeneralUserNotificationService
from app.utils import send_notification

class SubscriptionNotificationService:
    @staticmethod
    @handle_errors
    def send_subscription_expiration_notification(user, subscription, days_left):
        formatted_end_date = subscription.end_date.strftime('%A, %d %B %Y')
        title = "Subscription Expiration Notice"
        body = (f"Your {subscription.plan.name} subscription expires on {formatted_end_date}. "
                f"Renew within {days_left} days to avoid cancellation.")
        data = {
            'title': title,
            'subscription_id': str(subscription.subscription_id),
            'subscription_name': subscription.plan.name,
            'end_date': str(subscription.end_date),
            'cancel_in_days': str(days_left),
            'user_name': user.name,
        }

        GeneralUserNotificationService.create_notification(
            user_id=user.user_id,
            title=title,
            body=body,
            importance="medium",
            type="subscription_expiration",
        )


        send_notification(user.user_id, title, body, data)
        return {'message': 'Subscription expiration notification sent successfully.'}

    @staticmethod
    @handle_errors
    def send_subscription_canceled_notification(user, subscription):
        formatted_end_date = subscription.end_date.strftime('%A, %d %B %Y')
        title = "Subscription Cancellation Notice"
        body = (f"Your {subscription.plan.name} subscription has been canceled. "
                f"Your subscription was set to expire on {formatted_end_date}. "
                f"Your account is now on a basic plan.")
        data = {
            'title': title,
            'subscription_id': str(subscription.subscription_id),
            'subscription_name': subscription.plan.name,
            'end_date': str(subscription.end_date),
            'user_name': user.name,
        }

        GeneralUserNotificationService.create_notification(
            user_id=user.user_id,
            title=title,
            body=body,
            importance="medium",
            type="subscription_canceled"
        )

        send_notification(user.user_id, title, body, data)
        return {'message': 'Subscription cancellation notification sent successfully.'}
