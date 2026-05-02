from flask_socketio import SocketIO, emit, join_room
from flask import request
from app.repositories import HomeRepository, SensorRepository
from app.utils.auth_socket_decorator import socketio_role_required
from app.utils.error_handler import UnprocessableError, ValidationError, socketio_handle_errors

def setup_home_sockets(socketio):
    @socketio.on('subscribe_home')
    @socketio_handle_errors
    @socketio_role_required(['user'])
    def handle_subscribe_home(data):
        home_id = data.get('home_id')
        if not home_id:
            raise ValidationError("home_id is required")

        user_id = request.current_user.user_id
        home = HomeRepository.get_by_user_and_id(user_id, home_id)
        if not home:
            raise UnprocessableError("Home not found for the user.")

        join_room(home_id)

        emit('home_update', {
            'home_id': home.home_id,
            'default_mode_id': home.default_mode_id,
            'default_mode_name': home.default_mode.mode_name,
            'is_archived': home.is_archived
        })

        sensors = SensorRepository.get_all_by_home(home_id)
        emit('sensors_update', {
            'home_id': home_id,
            'sensors': [
                {
                    'sensor_id': sensor.sensor_id,
                    'is_active': sensor.is_active,
                    'is_closed': sensor.is_closed,
                    'is_security_breached': sensor.is_security_breached,
                    'is_archived': sensor.is_archived
                } for sensor in sensors['sensors']
            ]
        })
