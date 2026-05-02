import os
import logging
from logging.handlers import RotatingFileHandler
from datetime import timedelta
from dotenv import load_dotenv

load_dotenv()


class Config:
    SECRET_KEY = os.environ.get('SECRET_KEY')
    SQLALCHEMY_DATABASE_URI = os.environ.get('DATABASE_URL')
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    DEBUG = os.getenv('FLASK_DEBUG', 'True').lower() == 'true'

    AUTO_DB_SETUP = os.getenv('AUTO_DB_SETUP', 'True').lower() == 'true'
    SEED_FORCE = os.getenv('SEED_FORCE', 'False').lower() == 'true'
    DEV_MODE = os.getenv('DEV_MODE', 'False').lower() == 'true'

    # Email configuration
    MAIL_SERVER = 'smtp.gmail.com'
    MAIL_PORT = 587
    MAIL_USE_TLS = True
    MAIL_USERNAME = os.getenv('MAIL_USERNAME')
    MAIL_PASSWORD = os.getenv('MAIL_PASSWORD')
    MAIL_DEFAULT_SENDER = os.getenv('MAIL_SENDER_NAME'), os.getenv('MAIL_DEFAULT_SENDER')

    SCHEDULER_API_ENABLED = True

    STRIPE_SECRET_KEY = os.getenv('STRIPE_SECRET_KEY')
    STRIPE_PUBLISHABLE_KEY = os.getenv('STRIPE_PUBLISHABLE_KEY')
    STRIPE_WEBHOOK_SECRET = os.getenv('STRIPE_WEBHOOK_SECRET')

    PERMANENT_SESSION_LIFETIME = timedelta(hours=3)

    # CORS configuration
    CORS_ALLOW_ORIGINS = os.environ.get('CORS_ALLOW_ORIGINS', '*').split(',')
    CORS_ALLOW_METHODS = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH']
    CORS_ALLOW_HEADERS = ['Content-Type', 'Authorization']
    CORS_SUPPORTS_CREDENTIALS = True
    CORS_MAX_AGE = 3600

def setup_logging(app):
    """Configure logging for the application."""
    log_level = logging.DEBUG if app.debug else logging.INFO
    log_format = '%(asctime)s [%(levelname)s] %(name)s: %(message)s'

    # Clear any existing handlers to avoid duplicates
    root_logger = logging.getLogger()
    for handler in root_logger.handlers[:]:
        root_logger.removeHandler(handler)

    # Console handler for local development
    console_handler = logging.StreamHandler()
    console_handler.setLevel(log_level)
    console_handler.setFormatter(logging.Formatter(log_format))
    root_logger.addHandler(console_handler)

    # File handler for persistent logs (useful in Azure)
    log_dir = os.path.join(app.instance_path, 'logs')
    os.makedirs(log_dir, exist_ok=True)
    file_handler = RotatingFileHandler(
        os.path.join(log_dir, 'app.log'),
        maxBytes=10*1024*1024,  # 10MB
        backupCount=5
    )
    file_handler.setLevel(log_level)
    file_handler.setFormatter(logging.Formatter(log_format))
    root_logger.addHandler(file_handler)

    root_logger.setLevel(log_level)

    # Configure loggers for specific libraries
    for lib in ['apscheduler', 'stripe']:
        logging.getLogger(lib).setLevel(log_level)
        logging.getLogger(lib).propagate = True
    # SQLAlchemy: only warnings, except engine for SQL queries
    logging.getLogger('sqlalchemy').setLevel(logging.WARNING)
    logging.getLogger('alembic').setLevel(logging.WARNING)
    logging.getLogger('sqlalchemy.engine').setLevel(logging.WARNING)
