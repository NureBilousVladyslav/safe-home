from app import db
from sqlalchemy.sql import func
import uuid
from flask_login import UserMixin
from werkzeug.security import generate_password_hash, check_password_hash
from cryptography.fernet import Fernet
import os

cipher = Fernet(os.getenv('SECRET_KEY_Fernet'))


class User(db.Model, UserMixin):
    __tablename__ = 'user'
    __table_args__ = (
        db.Index('idx_user_role_id', 'role_id'),
    )

    user_id = db.Column(db.UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    role_id = db.Column(db.UUID(as_uuid=True), db.ForeignKey('role.role_id', ondelete='CASCADE'), nullable=False)
    name = db.Column(db.String(100), nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password = db.Column(db.String(256))
    google_id = db.Column(db.String(128))
    google_refresh_token = db.Column(db.Text)
    created_at = db.Column(db.DateTime(timezone=True), server_default=func.now())
    email_confirmed = db.Column(db.Boolean, default=False)
    subscription_plan_name = db.Column(db.String(100))

    role = db.relationship('Role', back_populates='users')

    subscriptions = db.relationship(
        'Subscription',
        back_populates='user',
        cascade="all, delete-orphan"
    )

    general_notifications = db.relationship(
        'GeneralUserNotification',
        back_populates='user',
        cascade="all, delete-orphan"
    )

    admin_audit_logs = db.relationship(
        'AdminAuditLog',
        back_populates='admin',
        cascade="all, delete-orphan"
    )

    devices = db.relationship(
        'MobileDevice',
        back_populates='user',
        cascade="all, delete-orphan"
    )

    homes = db.relationship(
        'Home',
        back_populates='user',
        cascade="all, delete-orphan"
    )

    def get_id(self):
        return self.user_id

    def set_password(self, password):
        self.password = generate_password_hash(password)

    def check_password(self, password):
        if self.password is None:
            return False
        return check_password_hash(self.password, password)

    def set_refresh_token(self, refresh_token):
        self.google_refresh_token = cipher.encrypt(refresh_token.encode()).decode()

    def get_refresh_token(self):
        if self.google_refresh_token:
            return cipher.decrypt(self.google_refresh_token.encode()).decode()
        return None