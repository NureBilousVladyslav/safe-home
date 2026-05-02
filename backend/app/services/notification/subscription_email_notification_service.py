from app.utils.error_handler import handle_errors, UnprocessableError
from flask_mail import Message
from flask import render_template_string
from app import mail
import os

class SubscriptionEmailService:
    @staticmethod
    @handle_errors
    def send_subscription_expiration_email(user, subscription):
        formatted_end_date = subscription.end_date.strftime('%A, %d %B %Y')

        with open("app/templates/email_subscription_expiration.html", "r") as html_file:
            html_template = html_file.read()

        html_body = render_template_string(
            html_template,
            name=user.name,
            subscription_name=subscription.plan.name,
            end_date=formatted_end_date,
            website_url=os.getenv('FRONTEND_LINK')
        )

        msg = Message(
            subject="Your Subscription is About to Expire",
            recipients=[user.email],
            body=f"Hello {user.name},\n\n"
                 f"We wanted to inform you that your subscription to {subscription.plan.name} will expire on {subscription.end_date}.\n"
                 f"If you do not renew your subscription, it will be canceled automatically.\n\n"
                 f"After cancellation:\n"
                 f"- Your subscription will be replaced with a basic one.\n"
                 f"- Homes and sensors will be archived until the subscription is extended.\n\n"
                 f"To continue enjoying the benefits of your subscription, please renew it before the expiration date.\n\n"
                 f"Visit our website for more details: {os.getenv('FRONTEND_LINK')}\n\n"
                 f"Thank you for using our service!\n\n"
                 f"Best regards,\n"
                 f"The Subscription Management Team",
            html=html_body
        )

        mail.send(msg)

    @staticmethod
    @handle_errors
    def send_subscription_canceled_email(user, subscription):
        formatted_end_date = subscription.end_date.strftime('%A, %d %B %Y')

        with open("app/templates/email_subscription_cancelled.html", "r") as html_file:
            html_template = html_file.read()

        html_body = render_template_string(
            html_template,
            name=user.name,
            subscription_name=subscription.plan.name,
            end_date=formatted_end_date,
            website_url=os.getenv('FRONTEND_LINK')
        )

        msg = Message(
            subject="Your Subscription has been Canceled",
            recipients=[user.email],
            body=f"Hello {user.name},\n\n"
                 f"We wanted to inform you that your subscription to {subscription.plan.name} has been canceled.\n"
                 f"The subscription was originally set to expire on {formatted_end_date}.\n\n"
                 f"Your account is now on a basic plan, and any associated data, including homes and sensors, has been archived.\n\n"
                 f"Visit our website to renew your subscription: {os.getenv('FRONTEND_LINK')}\n\n"
                 f"Thank you for being a part of our service!",
            html=html_body
        )

        mail.send(msg)

    @staticmethod
    @handle_errors
    def send_subscription_payment_failed_email(user, plan_name):

        with open("app/templates/email_subscription_payment_failed.html", "r") as html_file:
            html_template = html_file.read()

        html_body = render_template_string(
            html_template,
            name=user.name,
            plan_name=plan_name,
            website_url=os.getenv('FRONTEND_LINK') + '/user/subscriptions'
        )

        msg = Message(
            subject="Subscription Payment Failed",
            recipients=[user.email],
            body=f"Hello {user.name},\n\n"
                 f"We wanted to inform you that your payment of subscription plan { plan_name } has failed.\n"
                 f"Your account stayed on previous plan.\n\n"
                 f"Visit our website to try to pay again: {os.getenv('FRONTEND_LINK')}\n\n"
                 f"Thank you for being a part of our service!",
            html=html_body
        )

        mail.send(msg)
