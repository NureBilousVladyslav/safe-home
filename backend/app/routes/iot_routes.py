from flask import Blueprint, request
from app.services.auth import AuthService
from app.services.sensor_service import SensorService
from app.utils.error_handler import handle_errors
from app.utils.error_handler import UnauthorizedError
from flasgger import swag_from

iot_bp = Blueprint('iot', __name__)


@iot_bp.route('/send_sensor_status', methods=['PUT'])
@swag_from({
    'tags': ['IoT'],
    'summary': 'Update sensor status (for IoT devices)',
    'parameters': [
        {
            'name': 'body',
            'in': 'body',
            'required': True,
            'schema': {
                'type': 'object',
                'properties': {
                    'email': {'type': 'string'},
                    'password': {'type': 'string'},
                    'sensor_id': {'type': 'string'},
                    'is_closed': {'type': 'string', 'enum': ['true', 'false', '1', '0']}
                },
                'required': ['sensor_id', 'is_closed']
            }
        }
    ],
    'responses': {
        200: {'description': 'Sensor status updated successfully'},
        400: {'description': 'Validation error'},
        401: {'description': 'Unauthorized'},
        422: {'description': 'Unprocessable entity'},
        500: {'description': 'Internal server error'}
    }
})
@handle_errors
def send_sensor_status():
    data = request.get_json()
    user = AuthService.login_user(data)
    if user:
        return SensorService.set_sensor_status(user.user_id, request.json)
    else:
        raise UnauthorizedError('Sensor status update failed. Invalid credentials.')
