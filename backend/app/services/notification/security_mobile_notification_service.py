from app.utils.error_handler import handle_errors, UnprocessableError
from app.services.security_user_notification_service import SecurityUserNotificationService
from app.utils import send_notification

class SecurityNotificationService:
    @staticmethod
    @handle_errors
    def send_security_mode_change_notification(user_id, home, new_mode):
        title = "Security Mode Change Notice"
        body = f"Your home '{home.name}' security mode has been changed to {new_mode.mode_name}, {new_mode.description}."
        data = {
            'title': title,
            'new_mode_name': new_mode.mode_name,
            'home_id': str(home.home_id),
            'home_name': home.name,
        }

        SecurityUserNotificationService.create_notification(
            home_id=home.home_id,
            title=title,
            body=body,
            importance="medium",
            type="security_mode_change",
            user_id=user_id,
            data={"new_mode_name": new_mode.mode_name}
        )

        send_notification(user_id, title, body, data)
        return {'message': 'Security mode change notification sent successfully.'}

    @staticmethod
    @handle_errors
    def send_sensor_activity_change_notification(user_id, sensor, new_activity, new_mode_name):
        title = "Sensor Activity Change Notice"
        body = (f"Sensor '{sensor.name}' in home '{sensor.home.name}' was turned {'on' if new_activity else 'off'}. "
                f"Default security mode is set to {new_mode_name}.")
        data = {
            'title': title,
            'sensor_id': str(sensor.sensor_id),
            'sensor_name': sensor.name,
            'home_id': str(sensor.home_id),
            'home_name': sensor.home.name,
            'new_activity': str(new_activity),
            'new_mode_name': new_mode_name
        }

        SecurityUserNotificationService.create_notification(
            home_id=sensor.home_id,
            title=title,
            body=body,
            importance="medium",
            type="sensor_activity_change",
            sensor_id=sensor.sensor_id,
            user_id=user_id,
            data={"new_activity": new_activity}
        )

        send_notification(user_id, title, body, data)
        return {'message': 'Sensor activity change notification sent successfully.'}

    @staticmethod
    @handle_errors
    def send_active_sensor_status_changed_notification(user_id, sensor):
        title = "Sensor Status Change Notice"
        body = (f"The {sensor.type} '{sensor.name}' in home '{sensor.home.name}' "
                f"was {'closed' if sensor.is_closed else 'opened'}.")
        data = {
            'title': title,
            'sensor_id': str(sensor.sensor_id),
            'sensor_name': sensor.name,
            'home_id': str(sensor.home_id),
            'home_name': sensor.home.name,
            'new_status': str(sensor.is_active)
        }

        SecurityUserNotificationService.create_notification(
            home_id=sensor.home_id,
            title=title,
            body=body,
            importance="high",
            type="sensor_status_change",
            sensor_id=sensor.sensor_id,
            user_id=user_id
        )

        send_notification(user_id, title, body, data)
        return {'message': 'Sensor status change notification sent successfully.'}

    @staticmethod
    @handle_errors
    def send_sensor_security_breached_notification(user_id, sensor):
        title = "Sensor Security Breached"
        body = (f"The security of sensor '{sensor.name}' in home '{sensor.home.name}' was Breached. "
                f"Check the situation and fix the problem by deactivating and activating sensor again.")
        data = {
            'title': title,
            'sensor_id': str(sensor.sensor_id),
            'sensor_name': sensor.name,
            'home_id': str(sensor.home_id),
            'home_name': sensor.home.name,
        }

        SecurityUserNotificationService.create_notification(
            home_id=sensor.home_id,
            title=title,
            body=body,
            importance="high",
            type="sensor_security_breached",
            sensor_id=sensor.sensor_id,
            user_id=user_id
        )

        send_notification(user_id, title, body, data)
        return {'message': 'Sensor security breached notification sent successfully.'}
