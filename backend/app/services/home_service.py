from app.utils.error_handler import handle_errors, UnprocessableError, ValidationError
from app.repositories.home_repo import HomeRepository
from app.services.default_security_mode_service import DefaultSecurityModeService
from app.services.subscription_service import SubscriptionService
from app.services.notification import SecurityNotificationService
from app.utils import Validator
from app.models.home import Home
from flask import jsonify

class HomeService:
    @staticmethod
    @handle_errors
    def get_all_homes(user_id):
        homes = HomeRepository.get_all_by_user(user_id)
        homes_list = [
            {
                "home_id": str(home.home_id),
                "name": home.name,
                "address": home.address,
                "created_at": home.created_at.isoformat(),
                "default_mode_id": str(home.default_mode_id),
                "default_mode_name": home.default_mode.mode_name,
                "is_archived": home.is_archived
            } for home in homes
        ]
        return jsonify({"homes": homes_list}), 200

    @staticmethod
    @handle_errors
    def get_home(user_id, home_id):
        home = HomeRepository.get_by_user_and_id(user_id, home_id)
        home_data = {
                "home_id": str(home.home_id),
                "name": home.name,
                "address": home.address,
                "created_at": home.created_at.isoformat(),
                "default_mode_id": str(home.default_mode_id),
                "default_mode_name": home.default_mode.mode_name,
                "is_archived": home.is_archived
            }

        return jsonify({"home": home_data}), 200

    @staticmethod
    @handle_errors
    def add_home(user_id, body):
        Validator.validate_required_fields(body, ['name', 'address'])

        current_subscription = SubscriptionService.get_current_subscription(user_id)
        if not current_subscription:
            raise UnprocessableError("User does not have an active subscription.")

        current_homes_count = HomeRepository.count_active_by_user(user_id)
        if current_homes_count >= current_subscription.plan.max_homes:
            raise UnprocessableError("You have reached the maximum number of homes allowed by your subscription.")

        default_mode = DefaultSecurityModeService.get_security_mode("disarmed")

        new_home = Home(
            user_id=user_id,
            name=body['name'],
            address=body['address'],
            default_mode_id=default_mode.mode_id
        )
        HomeRepository.add(new_home)
        return jsonify({"message": "Home added successfully."}), 201

    @staticmethod
    @handle_errors
    def delete_home(user_id, home_id):
        home = HomeRepository.get_by_user_and_id(user_id, home_id)
        if not home:
            raise UnprocessableError("Home not found for the user.")

        HomeRepository.delete(home)
        return jsonify({"message": "Home was deleted successfully."}), 200

    @staticmethod
    @handle_errors
    def unarchive_home(user_id, home_id):
        home = HomeRepository.get_by_user_and_id_archived(user_id, home_id, is_archived=True)
        if not home:
            raise UnprocessableError("Archived home not found for the user.")

        current_subscription = SubscriptionService.get_current_subscription(user_id)
        if not current_subscription:
            raise UnprocessableError("User does not have an active subscription.")

        current_homes_count = HomeRepository.count_active_by_user(user_id)
        if current_homes_count >= current_subscription.plan.max_homes:
            raise UnprocessableError("You have reached the maximum number of homes allowed by your subscription.")

        home.is_archived = False
        HomeRepository.update(home)
        return jsonify({"message": "Home unarchived successfully."}), 200

    @staticmethod
    @handle_errors
    def archive_home_sensors(user_id, home_id):
        home = HomeRepository.get_by_user_and_id_archived(user_id, home_id, is_archived=False)
        if not home:
            raise UnprocessableError("Home not found for the user.")

        HomeService.archive_home(home)
        HomeRepository.update(home)
        return jsonify({"message": "Home and its sensors archived successfully."}), 200

    @staticmethod
    @handle_errors
    def set_armed_security_mode(user_id, home_id):
        home = HomeRepository.get_by_user_and_id_archived(user_id, home_id, is_archived=False)
        if not home:
            raise UnprocessableError("Home not found for the user.")

        default_mode = DefaultSecurityModeService.get_security_mode("armed")

        if not any(not sensor.is_archived for sensor in home.sensors):
            raise UnprocessableError("The list of active sensors is empty.")

        if any(not sensor.is_closed and not sensor.is_archived for sensor in home.sensors):
            return jsonify({"message": "Armed mode cannot be set! Close all your devices connected to sensors."}), 200

        HomeService._arm_sensors(home)
        home.default_mode_id = default_mode.mode_id
        HomeRepository.update(home)

        SecurityNotificationService.send_security_mode_change_notification(user_id, home, default_mode)
        return jsonify({"message": "Home armed successfully."}), 200

    @staticmethod
    @handle_errors
    def set_disarmed_security_mode(user_id, home_id):
        home = HomeRepository.get_by_user_and_id_archived(user_id, home_id, is_archived=False)
        if not home:
            raise UnprocessableError("Home not found for the user.")

        default_mode = DefaultSecurityModeService.get_security_mode("disarmed")

        if not any(not sensor.is_archived for sensor in home.sensors):
            raise UnprocessableError("The list of active sensors is empty.")

        HomeService._disarm_sensors(home)
        home.default_mode_id = default_mode.mode_id
        HomeRepository.update(home)

        SecurityNotificationService.send_security_mode_change_notification(user_id, home, default_mode)
        return jsonify({"message": "Home disarmed successfully."}), 200

    @staticmethod
    def archive_home(home):
        default_mode = DefaultSecurityModeService.get_security_mode("disarmed")

        home.default_mode_id = default_mode.mode_id
        home.is_archived = True

        for sensor in home.sensors:
            sensor.is_archived = True
            sensor.is_closed = False
            sensor.is_active = False
            sensor.is_security_breached = False

    @staticmethod
    def _disarm_sensors(home):
        for sensor in home.sensors:
            if not sensor.is_archived:
                sensor.is_active = False
                sensor.is_security_breached = False

    @staticmethod
    def _arm_sensors(home):
        for sensor in home.sensors:
            if not sensor.is_archived:
                sensor.is_active = True
                sensor.is_security_breached = False
