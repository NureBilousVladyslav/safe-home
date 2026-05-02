import logging
import os
from dotenv import load_dotenv
from app import db
from app.models.role import Role
from app.models.subscription_plan import SubscriptionPlan
from app.models.default_security_mode import DefaultSecurityMode
from app.models.user import User
from app.repositories.role_repo import RoleRepository

logger = logging.getLogger(__name__)
load_dotenv()


def seed_data(app, force=False):
    """
    Populates the database with initial data if it doesn't exist.

    Args:
        app: Flask application instance
        force (bool): If True, drops existing data before seeding

    Returns:
        bool: True if seeding succeeds, False if errors occur
    """
    with app.app_context():
        try:
            logger.info("Starting data seeding")
            # Check required environment variables
            required_env_vars = ['ADMIN_NAME', 'ADMIN_EMAIL', 'ADMIN_PASSWORD']
            missing_vars = [var for var in required_env_vars if not os.getenv(var)]
            if missing_vars:
                logger.error(f"Missing environment variables: {', '.join(missing_vars)}")
                return False

            # Drop and recreate tables if force is True
            if force:
                logger.warning("Forcing data reset")
                db.drop_all()
                db.create_all()
                logger.info("All tables recreated")

            # Seed roles
            if not _seed_roles():
                logger.error("Failed to seed roles")
                return False

            # Seed subscription plans
            if not _seed_subscription_plans():
                logger.error("Failed to seed subscription plans")
                return False

            # Seed security modes
            if not _seed_security_modes():
                logger.error("Failed to seed security modes")
                return False

            # Seed admin user
            if not _seed_admin_user():
                logger.error("Failed to seed admin user")
                return False

            logger.info("Data seeding completed successfully")
            return True

        except Exception as e:
            logger.error(f"Unexpected error during seeding: {str(e)}")
            return False

def _seed_roles():
    """Seeds roles if they don't exist."""
    existing_roles = {role.role_name for role in Role.query.all()}
    logger.info(f"Existing roles: {existing_roles}")
    roles_to_add = [
        {'role_name': 'user', 'description': None},
        {'role_name': 'admin', 'description': None},
        {'role_name': 'super_admin', 'description': None}
    ]

    for role_data in roles_to_add:
        if role_data['role_name'] not in existing_roles:
            role = Role(**role_data)
            db.session.add(role)
            logger.info(f"Added role: {role_data['role_name']}")
        else:
            logger.info(f"Role already exists: {role_data['role_name']}")

    try:
        db.session.commit()
        return True
    except Exception as e:
        logger.error(f"Error adding roles: {str(e)}")
        db.session.rollback()
        return False

def _seed_subscription_plans():
    """Seeds subscription plans if they don't exist."""
    existing_plans = {plan.name for plan in SubscriptionPlan.query.all()}
    logger.info(f"Existing subscription plans: {existing_plans}")
    plans_to_add = [
        {'name': 'premium', 'max_homes': 5, 'max_sensors': 20, 'price': 10.0, 'duration_days': 30},
        {'name': 'basic', 'max_homes': 1, 'max_sensors': 4, 'price': 0.0, 'duration_days': 365}
    ]

    for plan_data in plans_to_add:
        if plan_data['name'] not in existing_plans:
            plan = SubscriptionPlan(**plan_data)
            db.session.add(plan)
            logger.info(f"Added subscription plan: {plan_data['name']}")
        else:
            logger.info(f"Subscription plan already exists: {plan_data['name']}")

    try:
        db.session.commit()
        return True
    except Exception as e:
        logger.error(f"Error adding subscription plans: {str(e)}")
        db.session.rollback()
        return False

def _seed_security_modes():
    """Seeds security modes if they don't exist."""
    existing_modes = {mode.mode_name for mode in DefaultSecurityMode.query.all()}
    logger.info(f"Existing security modes: {existing_modes}")
    modes_to_add = [
        {'mode_name': 'armed', 'description': 'all sensors are active', 'is_selectable': True},
        {'mode_name': 'disarmed', 'description': 'all sensors are disabled', 'is_selectable': True},
        {'mode_name': 'custom', 'description': 'user changed default security mode', 'is_selectable': False},
        {'mode_name': 'alert', 'description': 'security breach detected', 'is_selectable': False}
    ]

    for mode_data in modes_to_add:
        if mode_data['mode_name'] not in existing_modes:
            mode = DefaultSecurityMode(**mode_data)
            db.session.add(mode)
            logger.info(f"Added security mode: {mode_data['mode_name']}")
        else:
            logger.info(f"Security mode already exists: {mode_data['mode_name']}")

    try:
        db.session.commit()
        return True
    except Exception as e:
        logger.error(f"Error adding security modes: {str(e)}")
        db.session.rollback()
        return False

def _seed_admin_user():
    """Seeds admin user if it doesn't exist."""
    admin_email = os.getenv('ADMIN_EMAIL')
    logger.info(f"Checking admin user with email: {admin_email}")
    if User.query.filter_by(email=admin_email).first():
        logger.info(f"Admin user with email {admin_email} already exists")
        return True

    admin_role = RoleRepository.get_by_name('super_admin')
    if not admin_role:
        logger.error("Admin role not found")
        return False

    try:
        admin_user = User(
            name=os.getenv('ADMIN_NAME'),
            email=admin_email,
            email_confirmed=True,
            role_id=admin_role.role_id
        )
        admin_user.set_password(os.getenv('ADMIN_PASSWORD'))
        db.session.add(admin_user)
        db.session.commit()
        logger.info(f"Added admin user: {admin_email}")
        return True
    except Exception as e:
        logger.error(f"Error adding admin user: {str(e)}")
        db.session.rollback()
        return False
