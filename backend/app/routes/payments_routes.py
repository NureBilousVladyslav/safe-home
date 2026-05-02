from flask import Blueprint, request, jsonify, render_template, current_app
from app.utils.auth_decorator import role_required
from app.services.subscription_service import SubscriptionService
from app.utils.error_handler import handle_errors, ValidationError
import stripe
import os
from flasgger import swag_from

payments_bp = Blueprint('payments', __name__)


@payments_bp.route('/create-checkout-session/<plan_id>', methods=['POST'])
@role_required(['user'])
@handle_errors
@swag_from({
    'tags': ['Payments'],
    'summary': 'Create Stripe checkout session',
    'description': 'Initiates a Stripe checkout session for a subscription plan.',
    'parameters': [
        {
            'name': 'plan_id',
            'in': 'path',
            'required': True,
            'type': 'string'
        },
    ],
    'responses': {
        200: {
            'description': 'Checkout session created',
            'schema': {
                'type': 'object',
                'properties': {
                    'url': {'type': 'string', 'example': 'https://checkout.stripe.com/pay/cs_test_...'}
                }
            }
        },
        401: {'description': 'Unauthorized'},
        422: {
            'description': 'Validation or unprocessable entity error',
            'schema': {
                'type': 'object',
                'properties': {
                    'error': {'type': 'string', 'example': 'Plan ID is required.'}
                }
            }
        },
        500: {'description': 'Internal server error'}
    }
})
def create_checkout_session(plan_id):
    user = request.current_user
    session_url = SubscriptionService.create_stripe_checkout_session(user.user_id, plan_id)
    return jsonify({"url": session_url}), 200


@payments_bp.route('/success', methods=['GET'])
@role_required(['user'])
@swag_from({
    'tags': ['Payments'],
    'summary': 'Payment success page',
    'description': 'Renders a success page after Stripe checkout redirection.',
    'security': [{'BearerAuth': []}],
    'responses': {
        200: {'description': 'Success page rendered'},
        401: {'description': 'Unauthorized'}
    }
})
def payment_success():
    frontend_url = os.getenv('FRONTEND_LINK') + '/user/subscriptions'
    return render_template('payment_success.html', frontend_url=frontend_url)


@payments_bp.route('/cancel', methods=['GET'])
@role_required(['user'])
@swag_from({
    'tags': ['Payments'],
    'summary': 'Payment cancel page',
    'description': 'Renders a cancel page after Stripe checkout cancellation.',
    'security': [{'BearerAuth': []}],
    'responses': {
        200: {'description': 'Cancel page rendered'},
        401: {'description': 'Unauthorized'}
    }
})
def payment_cancel():
    frontend_url = os.getenv('FRONTEND_LINK') + '/user/subscriptions'
    return render_template('payment_cancelled.html', frontend_url=frontend_url)


@payments_bp.route('/webhook', methods=['POST'])
@handle_errors
@swag_from({
    'tags': ['Payments'],
    'summary': 'Handle Stripe webhook',
    'description': 'Processes Stripe webhook events for payment confirmation.',
    'requestBody': {
        'required': True,
        'content': {
            'application/json': {
                'schema': {
                    'type': 'object',
                    'properties': {
                        'type': {'type': 'string', 'example': 'checkout.session.completed'},
                        'data': {
                            'type': 'object',
                            'properties': {
                                'object': {
                                    'type': 'object',
                                    'properties': {
                                        'metadata': {
                                            'type': 'object',
                                            'properties': {
                                                'user_id': {'type': 'string', 'example': 'user_123'},
                                                'plan_id': {'type': 'string', 'example': 'plan_123'}
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    },
    'responses': {
        200: {
            'description': 'Webhook processed',
            'schema': {
                'type': 'object',
                'properties': {
                    'status': {'type': 'string', 'example': 'success'}
                }
            }
        },
        422: {
            'description': 'Invalid payload or signature',
            'schema': {
                'type': 'object',
                'properties': {
                    'error': {'type': 'string', 'example': 'Invalid signature'}
                }
            }
        }
    }
})
def stripe_webhook():
    payload = request.data
    sig_header = request.headers.get('Stripe-Signature')
    endpoint_secret = current_app.config['STRIPE_WEBHOOK_SECRET']

    try:
        event = stripe.Webhook.construct_event(payload, sig_header, endpoint_secret)
    except ValueError:
        raise ValidationError("Invalid payload")
    except stripe.error.SignatureVerificationError:
        raise ValidationError("Invalid signature")

    SubscriptionService.handle_stripe_webhook(event)
    return jsonify({"status": "success"}), 200
