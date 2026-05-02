from app.utils.error_handler import handle_errors, UnprocessableError
from app.repositories.default_security_mode_repo import DefaultSecurityModeRepository
from flask import jsonify

class DefaultSecurityModeService:
    @staticmethod
    @handle_errors
    def get_all_default_modes():
        modes = DefaultSecurityModeRepository.get_all()
        default_modes = [
            {
                "mode_id": str(mode.mode_id),
                "mode_name": mode.mode_name,
                "description": mode.description,
                "is_selectable": mode.is_selectable
            } for mode in modes
        ]
        return jsonify({"default_modes": default_modes}), 200

    @staticmethod
    def get_security_mode(mode_name):
        mode = DefaultSecurityModeRepository.get_by_name(mode_name)
        if not mode:
            raise UnprocessableError(f"Default security mode '{mode_name}' not found.")
        return mode
