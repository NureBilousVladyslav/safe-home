from flask import Blueprint, request
from app.services import AdminAuditLogService
from app.services.user_service import UserService
from app.utils import success_trigger
from app.utils.auth_decorator import auth_required, role_required
from app.utils.error_handler import handle_errors, ValidationError
from flasgger import swag_from

user_bp = Blueprint('user', __name__)


@user_bp.route('/users', methods=['GET'])
@swag_from({
    'tags': ['User'],
    'summary': 'Get all users with role "user" (admin only)',
    'description': 'Retrieves a list of all users with the "user" role. Restricted to admin users.',
    'responses': {
        200: {
            'description': 'List of users',
            'schema': {
                'type': 'object',
                'properties': {
                    'users': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'user_id': {'type': 'string'},
                                'name': {'type': 'string'},
                                'email': {'type': 'string'},
                                'role': {'type': 'string'},
                                'created_at': {'type': 'string', 'format': 'date-time'},
                                'email_confirmed': {'type': 'boolean'},
                                'subscription_plan_name': {'type': 'string'}
                            }
                        }
                    }
                }
            }
        },
        401: {'description': 'Unauthorized - Admin role required'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['admin', 'super_admin'])
@handle_errors
def get_all_users():
    return UserService.get_all_users()


@user_bp.route('/admins', methods=['GET'])
@swag_from({
    'tags': ['User'],
    'summary': 'Get all users with role "admin" (admin only)',
    'description': 'Retrieves a list of all users with the "admin" role. Restricted to admin users.',
    'responses': {
        200: {
            'description': 'List of admins',
            'schema': {
                'type': 'object',
                'properties': {
                    'admins': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'user_id': {'type': 'string'},
                                'name': {'type': 'string'},
                                'email': {'type': 'string'},
                                'role': {'type': 'string'},
                                'created_at': {'type': 'string', 'format': 'date-time'},
                                'email_confirmed': {'type': 'boolean'}
                            }
                        }
                    }
                }
            }
        },
        401: {'description': 'Unauthorized - Admin role required'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['admin', 'super_admin'])
@handle_errors
def get_all_admins():
    return UserService.get_all_admins()


@user_bp.route('/admins/<user_id>', methods=['DELETE'])
@swag_from({
    'tags': ['User'],
    'summary': 'Delete an admin by ID (super admin only)',
    'description': 'Deletes an admin by their ID.',
    'parameters': [
        {
            'name': 'user_id',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'User ID to delete admin'
        }
    ],
    'responses': {
        200: {'description': 'Admin deleted successfully'},
        401: {'description': 'Unauthorized - Super Admin role required'},
        422: {'description': 'Unprocessable entity - User not found'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['super_admin'])
@handle_errors
@success_trigger(message="deleted an admin.", handler=AdminAuditLogService.build_delete_audit_log)
def delete_admin(user_id):
    if not user_id:
        raise ValidationError("User ID is required.")
    return UserService.delete_user(user_id, 'admin')


@user_bp.route('/users/<user_id>', methods=['DELETE'])
@swag_from({
    'tags': ['User'],
    'summary': 'Delete a user by ID (admin only)',
    'description': 'Deletes a user by their ID. Restricted to admin users.',
    'parameters': [
        {
            'name': 'user_id',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'User ID to delete user'
        }
    ],
    'responses': {
        200: {'description': 'User deleted successfully'},
        401: {'description': 'Unauthorized - Admin role required'},
        422: {'description': 'Unprocessable entity - User not found'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['admin', 'super_admin'])
@handle_errors
@success_trigger(message="deleted a user.", handler=AdminAuditLogService.build_delete_audit_log)
def delete_user(user_id):
    if not user_id:
        raise ValidationError("User ID is required.")
    return UserService.delete_user(user_id, 'user')


@user_bp.route('/users/<user_id>', methods=['Get'])
@swag_from({
    'tags': ['User'],
    'summary': 'Get a user by ID (admin only)',
    'description': 'Get a user by their ID.',
    'parameters': [
        {
            'name': 'user_id',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'User ID to receive user information'
        }
    ],
    'responses': {
        200: {
            'description': 'User data',
            'schema': {
                'type': 'object',
                'properties': {
                    'user': {
                        'type': 'object',
                        'properties': {
                            'user_id': {'type': 'string'},
                            'name': {'type': 'string'},
                            'email': {'type': 'string'},
                            'role': {'type': 'string'},
                            'created_at': {'type': 'string', 'format': 'date-time'},
                            'email_confirmed': {'type': 'boolean'},
                            'subscription_plan_name': {'type': 'string'}
                        }
                    }
                }
            }
        },
        401: {'description': 'Unauthorized - Authentication required'},
        422: {'description': 'Unprocessable entity - User not found'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['admin', 'super_admin'])
@handle_errors
def get_user_by_id(user_id):
    if not user_id:
        raise ValidationError("User ID is required.")
    return UserService.get_user(user_id)


@user_bp.route('/user', methods=['GET'])
@swag_from({
    'tags': ['User'],
    'summary': 'Get current user profile',
    'description': 'Retrieves the profile of the currently authenticated user.',
    'responses': {
        200: {
            'description': 'User data',
            'schema': {
                'type': 'object',
                'properties': {
                    'user': {
                        'type': 'object',
                        'properties': {
                            'user_id': {'type': 'string'},
                            'name': {'type': 'string'},
                            'email': {'type': 'string'},
                            'role': {'type': 'string'},
                            'created_at': {'type': 'string', 'format': 'date-time'},
                            'email_confirmed': {'type': 'boolean'},
                            'subscription_plan_name': {'type': 'string'}
                        }
                    }
                }
            }
        },
        401: {'description': 'Unauthorized - Authentication required'},
        422: {'description': 'Unprocessable entity - User not found'},
        500: {'description': 'Internal server error'}
    }
})
@auth_required
@handle_errors
def get_user():
    user_id = request.current_user.user_id
    return UserService.get_user(user_id)


@user_bp.route('/user', methods=['PUT'])
@swag_from({
    'tags': ['User'],
    'summary': 'Update current user profile',
    'description': 'Updates the profile of the currently authenticated user (only name field).',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'name': {'type': 'string'}
                },
                'required': ['name']
            }
        }
    ],
    'responses': {
        200: {'description': 'User data updated successfully'},
        400: {'description': 'Validation error - Missing required field'},
        401: {'description': 'Unauthorized - Authentication required'},
        422: {'description': 'Unprocessable entity - User not found'},
        500: {'description': 'Internal server error'}
    }
})
@auth_required
@handle_errors
def update_user():
    user_id = request.current_user.user_id
    return UserService.update_user(user_id, request.json)


@user_bp.route('/user', methods=['DELETE'])
@swag_from({
    'tags': ['User'],
    'summary': 'Delete current user profile',
    'description': 'Delete the profile of the currently authenticated user.',
    'responses': {
        200: {'description': 'User profile deleted successfully'},
        401: {'description': 'Unauthorized - Authentication required'},
        422: {'description': 'Unprocessable entity - User not found'},
        500: {'description': 'Internal server error'}
    }
})
@auth_required
@handle_errors
def delete_user_profile():
    user_id = request.current_user.user_id
    return UserService.delete_user(user_id, 'user')


@user_bp.route('/user/password', methods=['PUT'])
@swag_from({
    'tags': ['User'],
    'summary': 'Update current user password',
    'description': 'Updates the password of the currently authenticated user.',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'old_password': {'type': 'string'},
                    'new_password': {'type': 'string'}
                },
                'required': ['old_password', 'new_password']
            }
        }
    ],
    'responses': {
        200: {'description': 'Password updated successfully'},
        400: {'description': 'Validation error - Invalid old password'},
        401: {'description': 'Unauthorized - Authentication required'},
        422: {'description': 'Unprocessable entity - User not found'},
        500: {'description': 'Internal server error'}
    }
})
@auth_required
@handle_errors
def update_user_password():
    user_id = request.current_user.user_id
    return UserService.update_user_password(user_id, request.json)
