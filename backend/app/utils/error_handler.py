from flask import jsonify
from werkzeug.exceptions import HTTPException

class ErrorHandler:
    @staticmethod
    def handle_error(error, message=None, status_code=None):
        if isinstance(error, HTTPException):
            response = jsonify({
                "error": error.name,
                "message": message + error.description
            })
            response.status_code = status_code + error.code
        else:
            response = jsonify({
                "error": "Internal Server Error",
                "message": message + str(error)
            })
            response.status_code = status_code or 500
        return response

    @staticmethod
    def handle_validation_error(message):
        if isinstance(message, dict):
            error_messages = []
            for field, errors in message.items():
                error_messages.append(f"{field}: {', '.join(errors)}")
            message = "; ".join(error_messages)

        return {
            'success': False,
            'message': message
        }, 400

from flask import jsonify
from functools import wraps
import logging
import traceback
from flask_socketio import disconnect, emit
from werkzeug.exceptions import HTTPException, BadRequest, NotFound, Forbidden, Unauthorized, MethodNotAllowed, InternalServerError, UnprocessableEntity


logger = logging.getLogger(__name__)

class ValidationError(Exception):
    def __init__(self, message):
        super().__init__(message)
        self.message = message
        self.status_code = 400

class ResourceNotFoundError(Exception):
    def __init__(self, message):
        super().__init__(message)
        self.message = message
        self.status_code = 404

class UnprocessableError(Exception):
    def __init__(self, message):
        super().__init__(message)
        self.message = message
        self.status_code = 422

class DatabaseError(Exception):
    def __init__(self, message):
        super().__init__(message)
        self.message = message
        self.status_code = 500

class AuthError(Exception):
    def __init__(self, message):
        super().__init__(message)
        self.message = message
        self.status_code = 403

class UnauthorizedError(Exception):
    def __init__(self, message):
        super().__init__(message)
        self.message = message
        self.status_code = 401

def format_error_response(message, status_code):
    return jsonify({"error": message}), status_code

def handle_errors(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        try:
            return f(*args, **kwargs)
        except ValidationError as ve:
            logger.error(f"Validation error: {ve.message}\n")
            return format_error_response(ve.message, ve.status_code)
        except ResourceNotFoundError as rnf:
            logger.error(f"Resource not found error: {rnf.message}\n")
            return format_error_response(rnf.message, rnf.status_code)
        except UnprocessableError as ue:
            logger.error(f"Unprocessable error: {ue.message}\n")
            return format_error_response(ue.message, ue.status_code)
        except DatabaseError as de:
            logger.error(f"Database error: {de.message}\n")
            return format_error_response(de.message, de.status_code)
        except AuthError as ae:
            logger.error(f"Auth error: {ae.message}\n")
            return format_error_response(ae.message, ae.status_code)
        except UnauthorizedError as ue:
            logger.error(f"Unauthorized error: {ue.message}\n")
            return format_error_response(ue.message, ue.status_code)
        except Exception as e:
            logger.error(f"Unexpected error: {str(e)}\n")
            return format_error_response("An unexpected error occurred", 500)
    return decorated_function

def socketio_handle_errors(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        try:
            return f(*args, **kwargs)
        except ValidationError as ve:
            logger.error(f"Validation error: {ve.message}\n")
            emit('error', {'message': ve.message, 'code': ve.status_code})
            disconnect()
            return
        except ResourceNotFoundError as rnf:
            logger.error(f"Resource not found error: {rnf.message}\n")
            emit('error', {'message': rnf.message, 'code': rnf.status_code})
            disconnect()
            return
        except UnprocessableError as ue:
            logger.error(f"Unprocessable error: {ue.message}\n")
            emit('error', {'message': ue.message, 'code': ue.status_code})
            disconnect()
            return
        except DatabaseError as de:
            logger.error(f"Database error: {de.message}\n")
            emit('error', {'message': de.message, 'code': de.status_code})
            disconnect()
            return
        except AuthError as ae:
            logger.error(f"Auth error: {ae.message}\n")
            emit('error', {'message': ae.message, 'code': ae.status_code})
            disconnect()
            return
        except UnauthorizedError as ue:
            logger.error(f"Unauthorized error: {ue.message}\n")
            emit('error', {'message': ue.message, 'code': ue.status_code})
            disconnect()
            return
        except Exception as e:
            logger.error(f"Unexpected error: {str(e)}\n")
            emit('error', {'message': 'An unexpected error occurred', 'code': 500})
            disconnect()
            return
    return decorated_function

def register_error_handlers(app):
    @app.errorhandler(BadRequest)
    def handle_bad_request(e):
        logger.error(f"Bad request error: {str(e)}\n")
        return format_error_response("Bad request", 400)

    @app.errorhandler(Unauthorized)
    def handle_unauthorized(e):
        logger.error(f"Unauthorized error: {str(e)}\n")
        return format_error_response("Unauthorized", 401)

    @app.errorhandler(Forbidden)
    def handle_forbidden(e):
        logger.error(f"Forbidden error: {str(e)}\n")
        return format_error_response("Forbidden", 403)

    @app.errorhandler(NotFound)
    def handle_not_found(e):
        logger.error(f"Not found error: {str(e)}\n")
        return format_error_response("Not found", 404)

    @app.errorhandler(MethodNotAllowed)
    def handle_method_not_allowed(e):
        logger.error(f"Method not allowed error: {str(e)}\n")
        return format_error_response("Method not allowed", 405)

    @app.errorhandler(UnprocessableEntity)
    def handle_unprocessable_entity(e):
        logger.error(f"Unprocessable entity error: {str(e)}\n")
        return format_error_response("Unprocessable entity", 422)

    @app.errorhandler(InternalServerError)
    def handle_internal_server_error(e):
        logger.error(f"Internal server error: {str(e)}\n")
        return format_error_response("Internal server error", 500)

    @app.errorhandler(Exception)
    def handle_general_exception(e):
        if isinstance(e, HTTPException):
            return e
        logger.error(f"Unexpected global error: {str(e)}\n")
        return format_error_response("An unexpected error occurred", 500)

    @app.errorhandler(ValidationError)
    def handle_validation_error(e):
        logger.error(f"Validation error: {e.message}\n")
        return format_error_response(e.message, e.status_code)

    @app.errorhandler(ResourceNotFoundError)
    def handle_resource_not_found_error(e):
        logger.error(f"Resource not found error: {e.message}\n")
        return format_error_response(e.message, e.status_code)

    @app.errorhandler(UnprocessableError)
    def handle_unprocessable_error(e):
        logger.error(f"Unprocessable error: {e.message}\n")
        return format_error_response(e.message, e.status_code)

    @app.errorhandler(DatabaseError)
    def handle_database_error(e):
        logger.error(f"Database error: {e.message}\n")
        return format_error_response(e.message, e.status_code)

    @app.errorhandler(AuthError)
    def handle_auth_error(e):
        logger.error(f"Auth error: {e.message}\n")
        return format_error_response(e.message, e.status_code)

    @app.errorhandler(UnauthorizedError)
    def handle_unauthorized_error(e):
        logger.error(f"Unauthorized error: {e.message}\n")
        return format_error_response(e.message, e.status_code)
