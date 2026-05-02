from .auth_routes import auth_bp
from .payments_routes import payments_bp
from .iot_routes import iot_bp
from .user_routes import user_bp
from .home_routes import home_bp
from .sensor_routes import sensor_bp
from .default_security_mode_routes import default_security_mode_bp
from .general_user_notification_routes import general_notification_bp
from .security_user_notification_routes import security_notification_bp
from .subscription_plan_routes import subscription_plan_bp
from .mobile_devices_routes import mobile_device_bp
from .subscription_routes import subscription_bp
from .admin_audit_log_routes import admin_audit_log_bp
from .stats_routes import stats_bp
from .page_routes import page_bp

def init_routes(app):
    app.register_blueprint(auth_bp, url_prefix='/api')
    app.register_blueprint(default_security_mode_bp, url_prefix='/api')
    app.register_blueprint(general_notification_bp, url_prefix='/api')
    app.register_blueprint(security_notification_bp, url_prefix='/api')
    app.register_blueprint(sensor_bp, url_prefix='/api')
    app.register_blueprint(home_bp, url_prefix='/api')
    app.register_blueprint(mobile_device_bp, url_prefix='/api')
    app.register_blueprint(subscription_bp, url_prefix='/api')
    app.register_blueprint(subscription_plan_bp, url_prefix='/api')
    app.register_blueprint(user_bp, url_prefix='/api')
    app.register_blueprint(admin_audit_log_bp, url_prefix='/api')
    app.register_blueprint(stats_bp, url_prefix='/api')
    app.register_blueprint(iot_bp, url_prefix='/iot')
    app.register_blueprint(payments_bp,  url_prefix='/api/payments')
    app.register_blueprint(page_bp)
