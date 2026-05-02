import os
import re
import tempfile
from dotenv import load_dotenv
from app.utils.error_handler import ValidationError

def create_filled_service_account(template_path: str) -> str:
    try:
        load_dotenv()

        # Reading template
        with open(template_path, 'r') as f:
            template = f.read()

        # Environment change
        def replace_env(match):
            var_name = match.group(1)
            value = os.getenv(var_name)
            if value is None:
                raise ValidationError(f"Environment variable '{var_name}' not found")
            return value

        filled_content = re.sub(r'\$\{(\w+)\}', replace_env, template)

        # Write to temporary file
        temp = tempfile.NamedTemporaryFile(delete=False, mode='w', suffix='.json')
        temp.write(filled_content)
        temp.close()

        return temp.name

    except FileNotFoundError as e:
        raise ValidationError(f"Template file not found: {template_path}")
    except Exception as e:
        raise
