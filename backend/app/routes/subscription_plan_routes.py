from flask import Blueprint, request
from app.services import AdminAuditLogService
from app.services.subscription_plan_service import SubscriptionPlanService
from app.utils.auth_decorator import auth_required, role_required
from app.utils.error_handler import handle_errors
from app.utils import success_trigger
from flasgger import swag_from

subscription_plan_bp = Blueprint('subscription_plan', __name__)


@subscription_plan_bp.route('/subscription-plans', methods=['GET'])
@swag_from({
    'tags': ['Subscription Plan'],
    'summary': 'Get all subscription plans',
    'responses': {
        200: {
            'description': 'List of subscription plans',
            'schema': {
                'type': 'object',
                'properties': {
                    'subscription_plans': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'plan_id': {'type': 'string'},
                                'name': {'type': 'string'},
                                'max_homes': {'type': 'integer'},
                                'max_sensors': {'type': 'integer'},
                                'price': {'type': 'number'},
                                'duration': {'type': 'integer'}
                            }
                        }
                    }
                }
            }
        },
        401: {'description': 'Unauthorized'},
        500: {'description': 'Internal server error'}
    }
})
@handle_errors
def get_all_subscription_plans():
    return SubscriptionPlanService.get_all_subscription_plans()


@subscription_plan_bp.route('/subscription-plans', methods=['POST'])
@swag_from({
    'tags': ['Subscription Plan'],
    'summary': 'Create a new subscription plan (admin only)',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'name': {'type': 'string'},
                    'max_homes': {'type': 'integer'},
                    'max_sensors': {'type': 'integer'},
                    'price': {'type': 'number'},
                    'duration_days': {'type': 'integer'}
                },
                'required': ['name', 'max_homes', 'max_sensors', 'price', 'duration_days']
            }
        }
    ],
    'responses': {
        201: {'description': 'Subscription plan created successfully'},
        400: {'description': 'Validation error'},
        401: {'description': 'Unauthorized'},
        403: {'description': 'Forbidden'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['admin', 'super_admin'])
@handle_errors
@success_trigger(message="created a new subscription plan.", handler=AdminAuditLogService.build_create_audit_log)
def create_subscription_plan():
    return SubscriptionPlanService.create_subscription_plan(request.json)


@subscription_plan_bp.route('/subscription-plans/<plan_id>', methods=['PATCH'])
@swag_from({
    'tags': ['Subscription Plan'],
    'summary': 'Update a subscription plan (admin only)',
    'parameters': [
        {
            'name': 'plan_id',
            'in': 'path',
            'required': True,
            'type': 'string'
        },
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'name': {'type': 'string'},
                    'max_homes': {'type': 'integer'},
                    'max_sensors': {'type': 'integer'},
                    'price': {'type': 'number'},
                    'duration_days': {'type': 'integer'}
                }
            }
        }
    ],
    'responses': {
        200: {'description': 'Subscription plan updated successfully'},
        400: {'description': 'Validation error'},
        422: {'description': 'Unprocessable entity'},
        401: {'description': 'Unauthorized'},
        403: {'description': 'Forbidden'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['admin', 'super_admin'])
@handle_errors
@success_trigger(message="updated a subscription plan.", handler=AdminAuditLogService.build_update_audit_log)
def update_subscription_plan(plan_id):
    return SubscriptionPlanService.update_subscription_plan(plan_id, request.json)
