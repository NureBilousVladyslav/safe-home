from flask import Blueprint, request
from app.services.home_service import HomeService
from app.utils.auth_decorator import auth_required, role_required
from app.utils.error_handler import handle_errors
from flasgger import swag_from

home_bp = Blueprint('home', __name__)


@home_bp.route('/homes', methods=['GET'])
@swag_from({
    'tags': ['Home'],
    'summary': 'Get all homes for the authenticated user',
    'responses': {
        200: {
            'description': 'List of homes',
            'schema': {
                'type': 'object',
                'properties': {
                    'homes': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'home_id': {'type': 'string'},
                                'name': {'type': 'string'},
                                'address': {'type': 'string'},
                                'created_at': {'type': 'string'},
                                'default_mode_id': {'type': 'string'},
                                'default_mode_name': {'type': 'string'},
                                'is_archived': {'type': 'boolean'}
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
@role_required(['user'])
@handle_errors
def get_all_homes():
    user_id = request.current_user.user_id
    return HomeService.get_all_homes(user_id)


@home_bp.route('/homes/<home_id>', methods=['GET'])
@swag_from({
    'tags': ['Home'],
    'summary': 'Get home by id for the authenticated user',
    'parameters': [
            {
                'name': 'home_id',
                'in': 'path',
                'required': True,
                'type': 'string',
                'description': 'Home ID to retrieve sensors for'
            }
    ],
    'responses': {
        200: {
            'description': 'Home details',
            'schema': {
                'type': 'object',
                'properties': {
                    'home': {
                        'type': 'object',
                        'properties': {
                            'home_id': {'type': 'string'},
                            'name': {'type': 'string'},
                            'address': {'type': 'string'},
                            'created_at': {'type': 'string'},
                            'default_mode_id': {'type': 'string'},
                            'default_mode_name': {'type': 'string'},
                            'is_archived': {'type': 'boolean'}
                        }
                    }
                }
            }
        },
        401: {'description': 'Unauthorized'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def get_home(home_id):
    user_id = request.current_user.user_id
    return HomeService.get_home(user_id, home_id)


@home_bp.route('/homes', methods=['POST'])
@swag_from({
    'tags': ['Home'],
    'summary': 'Add a new home for the authenticated user',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'name': {'type': 'string'},
                    'address': {'type': 'string'}
                },
                'required': ['name', 'address']
            }
        }
    ],
    'responses': {
        201: {'description': 'Home added successfully'},
        400: {'description': 'Validation error'},
        401: {'description': 'Unauthorized'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def add_home():
    user_id = request.current_user.user_id
    return HomeService.add_home(user_id, request.json)


@home_bp.route('/homes/<home_id>', methods=['DELETE'])
@swag_from({
    'tags': ['Home'],
    'summary': 'Delete a home for the authenticated user',
    'parameters': [
        {
            'name': 'home_id',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'Home ID to delete'
        }
    ],
    'responses': {
        200: {'description': 'Home deleted successfully'},
        401: {'description': 'Unauthorized'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def delete_home(home_id):
    user_id = request.current_user.user_id
    return HomeService.delete_home(user_id, home_id)


@home_bp.route('/homes/<home_id>/unarchive', methods=['POST'])
@swag_from({
    'tags': ['Home'],
    'summary': 'Unarchive a home for the authenticated user',
    'parameters': [
        {
            'name': 'home_id',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'Home ID to unarchive'
        }
    ],
    'responses': {
        200: {'description': 'Home unarchived successfully'},
        401: {'description': 'Unauthorized'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def unarchive_home(home_id):
    user_id = request.current_user.user_id
    return HomeService.unarchive_home(user_id, home_id)


@home_bp.route('/homes/<home_id>/archive', methods=['POST'])
@swag_from({
    'tags': ['Home'],
    'summary': 'Archive a home and its sensors for the authenticated user',
    'parameters': [
        {
            'name': 'home_id',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'Home ID to archive'
        }
    ],
    'responses': {
        200: {'description': 'Home and its sensors archived successfully'},
        401: {'description': 'Unauthorized'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def archive_home_sensors(home_id):
    user_id = request.current_user.user_id
    return HomeService.archive_home_sensors(user_id, home_id)


@home_bp.route('/homes/<home_id>/security/armed', methods=['POST'])
@swag_from({
    'tags': ['Home'],
    'summary': 'Set the security mode to armed for a specific home',
    'parameters': [
        {
            'name': 'home_id',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'Home ID to set armed mode for'
        }
    ],
    'responses': {
        200: {'description': 'Home armed successfully'},
        401: {'description': 'Unauthorized'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def set_armed_security_mode(home_id):
    user_id = request.current_user.user_id
    return HomeService.set_armed_security_mode(user_id, home_id)


@home_bp.route('/homes/<home_id>/security/disarmed', methods=['POST'])
@swag_from({
    'tags': ['Home'],
    'summary': 'Set the security mode to disarmed for a specific home',
    'parameters': [
        {
            'name': 'home_id',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'Home ID to set disarmed mode for'
        }
    ],
    'responses': {
        200: {'description': 'Home disarmed successfully'},
        401: {'description': 'Unauthorized'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def set_disarmed_security_mode(home_id):
    user_id = request.current_user.user_id
    return HomeService.set_disarmed_security_mode(user_id, home_id)
