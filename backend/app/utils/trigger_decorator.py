from functools import wraps
from flask import request


def success_trigger(message: str = None, handler=None):
    """
    Universal decorator for processing successful answers route.

    Args:
        message (str): Message for processing (can be a template with {kwargs}).
        handler (callable): A handler function called upon successful response.
                           Takes response_data, status_code, request, kwargs.
    """

    def decorator(f):
        @wraps(f)
        def wrapped(*args, **kwargs):
            response = f(*args, **kwargs)
            status_code = response[1] if isinstance(response, tuple) else response.status_code
            response_data = response[0] if isinstance(response, tuple) else response

            if status_code in (200, 201):

                formatted_message = message.format(**kwargs) if message else None

                if handler:
                    handler(response_data, status_code, request, kwargs, formatted_message)

            return response_data, status_code

        return wrapped

    return decorator