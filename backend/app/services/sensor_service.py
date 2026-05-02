from app.utils.error_handler import handle_errors, UnprocessableError, ValidationError
from app.repositories.sensor_repo import SensorRepository
from app.repositories.home_repo import HomeRepository
from app.services.subscription_service import SubscriptionService
from app.services.default_security_mode_service import DefaultSecurityModeService
from app.services.notification import SecurityNotificationService
from app.utils import Validator
from app.models.sensor import Sensor
from flask import jsonify
import base36

class SensorService:
    @staticmethod
    @handle_errors
    def get_all_sensors(user_id, home_id):
        sensors = SensorRepository.get_all_by_user_and_home(user_id, home_id)
        sensors_list = [
            {
                "sensor_id": str(sensor.sensor_id),
                "short_code": sensor.short_code,
                "name": sensor.name,
                "type": sensor.type,
                "is_closed": sensor.is_closed,
                "is_active": sensor.is_active,
                "is_security_breached": sensor.is_security_breached,
                "created_at": sensor.created_at.isoformat(),
                "is_archived": sensor.is_archived
            } for sensor in sensors
        ]
        return jsonify({"sensors": sensors_list}), 200

    @staticmethod
    @handle_errors
    def add_sensor(user_id, body):
        Validator.validate_required_fields(body, ['home_id', 'name', 'type'])

        home_id = body['home_id']
        home = HomeRepository.get_by_user_and_id(user_id, home_id)
        if not home or home.is_archived:
            raise UnprocessableError("Active home not found for the user.")

        current_subscription = SubscriptionService.get_current_subscription(user_id)
        if not current_subscription:
            raise UnprocessableError("User does not have an active subscription.")

        current_sensors_count = SensorRepository.count_active_by_user(user_id)
        if current_sensors_count >= current_subscription.plan.max_sensors:
            raise UnprocessableError("You have reached the maximum number of sensors allowed by your subscription.")

        new_sensor = Sensor(
            home_id=home_id,
            user_id=user_id,
            name=body['name'],
            type=body['type']
        )
        SensorRepository.add(new_sensor)
        return jsonify({"message": "Sensor added successfully."}), 201

    @staticmethod
    @handle_errors
    def delete_sensor(user_id, sensor_id):
        sensor = SensorRepository.get_by_user_and_id(user_id, sensor_id)
        if not sensor:
            raise UnprocessableError("Sensor not found for the specified user.")

        SensorRepository.delete(sensor)
        return jsonify({"message": "Sensor was deleted successfully."}), 200

    @staticmethod
    @handle_errors
    def set_sensor_activity(user_id, sensor_id, body):
        Validator.validate_required_fields(body, ['is_active'])

        is_active = Validator.validate_boolean(body['is_active'], 'is_active')

        sensor = SensorRepository.get_by_user_and_id_archived(user_id, sensor_id, is_archived=False)
        if not sensor:
            raise UnprocessableError("Sensor not found for the specified user.")

        if sensor.is_active == is_active:
            return jsonify({"message": "No changes needed, sensor activity remains the same."}), 200

        if is_active and not sensor.is_closed:
            raise UnprocessableError("Sensor can't be set as active. Close device connected to sensor.")

        sensor.is_active = is_active
        sensor.is_security_breached = False
        SensorRepository.update(sensor)

        home = sensor.home
        all_sensors_status = all(s.is_active == is_active for s in home.sensors if not s.is_archived)
        all_sensors_not_breached = all(s.is_security_breached == False for s in home.sensors if not s.is_archived)

        # Determine the new security mode for the home based on sensor statuses:
        # 1. If all sensors are active and none are breached → set mode to "armed".
        # 2. If all sensors are inactive → set mode to "disarmed".
        # 3. If some sensors are active but none are breached → set mode to "custom".
        # 4. Otherwise, keep the current security mode.
        mode_name = home.default_mode.mode_name
        if all_sensors_status and is_active and all_sensors_not_breached:
            mode_name = "armed"
        elif all_sensors_status and not is_active:
            mode_name = "disarmed"
        elif all_sensors_not_breached:
            mode_name = "custom"

        default_mode = DefaultSecurityModeService.get_security_mode(mode_name)
        home.default_mode_id = default_mode.mode_id
        SensorRepository.update(sensor)

        SecurityNotificationService.send_sensor_activity_change_notification(user_id, sensor, is_active, default_mode.mode_name)
        return jsonify({"message": f"Sensor was turned {'on' if is_active else 'off'} successfully."}), 200

    @staticmethod
    @handle_errors
    def set_sensor_status(user_id, body):
        Validator.validate_required_fields(body, ['sensor_id', 'is_closed'])

        short_code = body['sensor_id']
        short_id = base36.loads(short_code)

        is_closed = Validator.validate_boolean(body['is_closed'], 'is_closed')

        sensor = SensorRepository.get_by_user_short_id_archived(user_id, short_id, is_archived=False)
        if not sensor:
            raise UnprocessableError("Sensor not found for the specified user.")

        if sensor.is_closed == is_closed:
            return jsonify({"message": "No changes needed, sensor status remains the same."}), 200

        sensor.is_closed = is_closed
        SensorRepository.update(sensor)

        if sensor.is_active:
            if not is_closed and not sensor.is_security_breached:
                sensor.is_security_breached = True
                home = sensor.home
                default_mode = DefaultSecurityModeService.get_security_mode("alert")
                home.default_mode_id = default_mode.mode_id
                SensorRepository.update(sensor)
                SecurityNotificationService.send_sensor_security_breached_notification(user_id, sensor)
            else:
                SecurityNotificationService.send_active_sensor_status_changed_notification(user_id, sensor)

        return jsonify({"message": f"Sensor status 'is_closed' was set as {is_closed}."}), 200

    @staticmethod
    @handle_errors
    def unarchive_sensor(user_id, sensor_id):
        sensor = SensorRepository.get_by_user_and_id_archived(user_id, sensor_id, is_archived=True)
        if not sensor:
            raise UnprocessableError("Archived sensor not found for the user.")

        if sensor.home.is_archived:
            raise UnprocessableError("Home associated with this sensor is archived.")

        current_subscription = SubscriptionService.get_current_subscription(user_id)
        if not current_subscription:
            raise UnprocessableError("User does not have an active subscription.")

        current_sensors_count = SensorRepository.count_active_by_user(user_id)
        if current_sensors_count >= current_subscription.plan.max_sensors:
            raise UnprocessableError("You have reached the maximum number of sensors allowed by your subscription.")

        sensor.is_archived = False
        SensorRepository.update(sensor)
        return jsonify({"message": "Sensor unarchived successfully."}), 200

    @staticmethod
    @handle_errors
    def archive_sensor(user_id, sensor_id):
        sensor = SensorRepository.get_by_user_and_id_archived(user_id, sensor_id, is_archived=False)
        if not sensor:
            raise UnprocessableError("Active sensor not found for the user.")

        sensor.is_archived = True
        sensor.is_closed = False
        sensor.is_active = False
        sensor.is_security_breached = False
        SensorRepository.update(sensor)
        return jsonify({"message": "Sensor archived successfully."}), 200
