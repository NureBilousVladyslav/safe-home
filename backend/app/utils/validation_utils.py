from app.utils.error_handler import ValidationError
import re


class Validator:
    EMAIL_REGEX = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    NAME_REGEX = r'^[a-zA-Zа-яА-Я\s-]{2,100}$'

    @staticmethod
    def validate_required_fields(data, required_fields):
        """Validate that all required fields are present and not empty."""
        missing_fields = [field for field in required_fields if field not in data or data[field] is None]
        if missing_fields:
            raise ValidationError(f"Required fields missing: {', '.join(missing_fields)}")

    @staticmethod
    def validate_positive_integer(value, field_name):
        if not isinstance(value, int) or value <= 0:
            raise ValidationError(f"{field_name} must be a positive integer.")

    @staticmethod
    def validate_positive_number(value, field_name):
        if not isinstance(value, (int, float)) or value <= 0:
            raise ValidationError(f"{field_name} must be a positive number.")

    @staticmethod
    def validate_boolean(value, field_name):
        """
        Validates that the value is a boolean (True/False) or a string representation ('true', 'false', '1', '0').
        Returns the converted boolean value if valid, otherwise raises ValidationError.
        """
        if isinstance(value, bool):
            return value
        if isinstance(value, str):
            value = value.lower()
            if value in ('true', '1'):
                return True
            if value in ('false', '0'):
                return False
        raise ValidationError(f"{field_name} must be a boolean value (true/false) or string ('true'/'false'/'1'/'0').")

    @staticmethod
    def validate_email(email: str) -> None:
        """Validate email format using regex."""
        if not re.match(Validator.EMAIL_REGEX, email):
            raise ValueError("Invalid email format")

    @staticmethod
    def validate_name(name: str) -> None:
        """Validate name format using regex."""
        if not re.match(Validator.NAME_REGEX, name):
            raise ValueError("Invalid name format")

    @staticmethod
    def validate_password(password: str) -> None:
        """Validate password strength."""
        if len(password) < 8:
            raise ValueError("Password must be at least 8 characters")
