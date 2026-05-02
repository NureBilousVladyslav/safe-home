from flask import Blueprint, jsonify
from app.services.stats_service import StatsService
from app.utils.auth_decorator import auth_required, role_required
from app.utils.error_handler import handle_errors, UnprocessableError
from flasgger import swag_from

stats_bp = Blueprint('stats', __name__)


@stats_bp.route('/stats/users/<int:days>', methods=['GET'])
@swag_from({
    'tags': ['Stats'],
    'summary': 'Get user statistics for a number of days (admin only)',
    'description': 'Returns user count statistics for the specified number of days, ordered from oldest to newest.',
    'parameters': [
        {
            'name': 'days',
            'in': 'path',
            'type': 'integer',
            'required': True,
            'description': 'Number of days to retrieve statistics for'
        }
    ],
    'responses': {
        200: {
            'description': 'List of user statistics',
            'schema': {
                'type': 'object',
                'properties': {
                    'user_stats': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'stats_id': {'type': 'string'},
                                'date': {'type': 'string', 'format': 'date-time'},
                                'user_count': {'type': 'integer'}
                            }
                        }
                    }
                }
            }
        },
        401: {'description': 'Unauthorized - Admin role required'},
        422: {'description': 'Unprocessable Entity - Invalid days parameter'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['admin', 'super_admin'])
@handle_errors
def get_user_stats(days):
    return StatsService.get_user_stats(days)


@stats_bp.route('/stats/subscription-plan/<plan_id>/<int:days>', methods=['GET'])
@swag_from({
    'tags': ['Stats'],
    'summary': 'Get subscription plan statistics for a number of days (admin only)',
    'description': 'Returns subscription plan statistics (user count, average homes, and sensors) for the specified number of days. If plan_id is a UUID, returns an array with one object for that plan. If plan_id is "all", returns an array of objects for all plans. Stats are ordered from oldest to newest.',
    'parameters': [
        {
            'name': 'plan_id',
            'in': 'path',
            'type': 'string',
            'required': True,
            'description': 'UUID of the subscription plan or "all" for all plans'
        },
        {
            'name': 'days',
            'in': 'path',
            'type': 'integer',
            'required': True,
            'description': 'Number of days to retrieve statistics for (must be positive)'
        }
    ],
    'responses': {
        200: {
            'description': 'Subscription plan statistics',
            'schema': {
                'type': 'object',
                'properties': {
                    'subscription_plans_stats': {
                        'type': 'array',
                        'description': 'Array of statistics objects, one per plan (single object for specific plan_id, multiple for "all")',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'plan_id': {
                                    'type': 'string',
                                    'description': 'UUID of the subscription plan'
                                },
                                'plan_name': {
                                    'type': 'string',
                                    'description': 'Name of the subscription plan'
                                },
                                'stats': {
                                    'type': 'array',
                                    'items': {
                                        'type': 'object',
                                        'properties': {
                                            'stats_id': {'type': 'string', 'description': 'UUID of the stats record'},
                                            'date': {'type': 'string', 'format': 'date-time', 'description': 'Date of the stats record'},
                                            'user_count': {'type': 'integer', 'description': 'Number of users with the plan'},
                                            'avg_homes': {'type': 'number', 'description': 'Average number of homes per user'},
                                            'avg_sensors': {'type': 'number', 'description': 'Average number of sensors per user'}
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        401: {'description': 'Unauthorized - Admin or Super Admin role required'},
        422: {'description': 'Unprocessable Entity - Invalid days or plan_id'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['admin', 'super_admin'])
@handle_errors
def get_subscription_plan_stats(plan_id, days):
    plan_id = None if plan_id.lower() == 'all' else plan_id
    return StatsService.get_subscription_plan_stats(days, plan_id)


@stats_bp.route('/stats/subscription-plans', methods=['GET'])
@swag_from({
    'tags': ['Stats'],
    'summary': 'Get latest subscription plan statistics for all plans (admin only)',
    'description': 'Returns the most recent statistics (user count, average homes, and sensors) for each subscription plan.',
    'responses': {
        200: {
            'description': 'List of latest subscription plan statistics',
            'schema': {
                'type': 'object',
                'properties': {
                    'subscription_plans_stats': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'stats_id': {'type': 'string', 'description': 'UUID of the stats record'},
                                'date': {'type': 'string', 'format': 'date-time', 'description': 'Date of the stats record'},
                                'plan_id': {'type': 'string', 'description': 'UUID of the subscription plan'},
                                'plan_name': {'type': 'string', 'description': 'Name of the subscription plan'},
                                'user_count': {'type': 'integer', 'description': 'Number of users with the plan'},
                                'avg_homes': {'type': 'number', 'description': 'Average number of homes per user'},
                                'avg_sensors': {'type': 'number', 'description': 'Average number of sensors per user'},
                                'plan_max_homes': {'type': 'number', 'description': 'Max number of homes per user'},
                                'plan_max_sensors': {'type': 'number', 'description': 'Max number of sensors per user'}
                            }
                        }
                    }
                }
            }
        },
        401: {'description': 'Unauthorized - Admin or Super Admin role required'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['admin', 'super_admin'])
@handle_errors
def get_latest_subscription_plan_stats():
    return StatsService.get_latest_subscription_plan_stats()
