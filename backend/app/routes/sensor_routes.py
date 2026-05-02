from flask import Blueprint, request
from app.services.sensor_service import SensorService
from app.utils.auth_decorator import auth_required, role_required
from app.utils.error_handler import handle_errors, ValidationError
from flasgger import swag_from

sensor_bp = Blueprint('sensor', __name__)


@sensor_bp.route('/sensors/<home_id>', methods=['GET'])
@swag_from({
    'tags': ['Sensor'],
    'summary': 'Get all sensors for the authenticated user in a specific home',
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
            'description': 'List of sensors',
            'schema': {
                'type': 'object',
                'properties': {
                    'sensors': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'sensor_id': {'type': 'string'},
                                'name': {'type': 'string'},
                                'type': {'type': 'string'},
                                'is_closed': {'type': 'boolean'},
                                'is_active': {'type': 'boolean'},
                                'is_security_breached': {'type': 'boolean'},
                                'created_at': {'type': 'string'},
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
def get_all_sensors(home_id):
    user_id = request.current_user.user_id
    return SensorService.get_all_sensors(user_id, home_id)


@sensor_bp.route('/sensors', methods=['POST'])
@swag_from({
    'tags': ['Sensor'],
    'summary': 'Add a new sensor for the authenticated user',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'home_id': {'type': 'string'},
                    'name': {'type': 'string'},
                    'type': {'type': 'string'}
                },
                'required': ['home_id', 'name', 'type']
            }
        }
    ],
    'responses': {
        201: {'description': 'Sensor added successfully'},
        400: {'description': 'Validation error'},
        422: {'description': 'Unprocessable entity'},
        401: {'description': 'Unauthorized'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def add_sensor():
    user_id = request.current_user.user_id
    return SensorService.add_sensor(user_id, request.json)


@sensor_bp.route('/sensors/<sensor_id>', methods=['DELETE'])
@swag_from({
    'tags': ['Sensor'],
    'summary': 'Delete a sensor for the authenticated user',
    'parameters': [
        {
            'name': 'sensor_id',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'Sensor ID to delete'
        }
    ],
    'responses': {
        200: {'description': 'Sensor deleted successfully'},
        401: {'description': 'Unauthorized'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def delete_sensor(sensor_id):
    user_id = request.current_user.user_id
    return SensorService.delete_sensor(user_id, sensor_id)


@sensor_bp.route('/sensors/<sensor_id>/activity', methods=['PATCH'])
@swag_from({
    'tags': ['Sensor'],
    'summary': 'Set sensor activity (turn on/off) for the authenticated user',
    'parameters': [
        {
            'name': 'sensor_id',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'Sensor ID to update activity for'
        },
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'is_active': {'type': 'boolean'}
                },
                'required': ['is_active']
            }
        }
    ],
    'responses': {
        200: {'description': 'Sensor activity updated successfully'},
        400: {'description': 'Validation error'},
        401: {'description': 'Unauthorized'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def set_sensor_activity(sensor_id):
    user_id = request.current_user.user_id
    return SensorService.set_sensor_activity(user_id, sensor_id, request.json)


@sensor_bp.route('/sensors/<sensor_id>/archive', methods=['POST'])
@swag_from({
    'tags': ['Sensor'],
    'summary': 'Archive a sensor for the authenticated user',
    'parameters': [
        {
            'name': 'sensor_id',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'Sensor ID to archive'
        }
    ],
    'responses': {
        200: {'description': 'Sensor archived successfully'},
        401: {'description': 'Unauthorized'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def archive_sensor(sensor_id):
    user_id = request.current_user.user_id
    return SensorService.archive_sensor(user_id, sensor_id)


@sensor_bp.route('/sensors/<sensor_id>/unarchive', methods=['POST'])
@swag_from({
    'tags': ['Sensor'],
    'summary': 'Unarchive a sensor for the authenticated user',
    'parameters': [
        {
            'name': 'sensor_id',
            'in': 'path',
            'required': True,
            'type': 'string',
            'description': 'Sensor ID to unarchive'
        }
    ],
    'responses': {
        200: {'description': 'Sensor unarchived successfully'},
        401: {'description': 'Unauthorized'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@role_required(['user'])
@handle_errors
def unarchive_sensor(sensor_id):
    user_id = request.current_user.user_id
    return SensorService.unarchive_sensor(user_id, sensor_id)
