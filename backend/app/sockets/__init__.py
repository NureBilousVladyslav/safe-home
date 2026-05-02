from .home_sockets import setup_home_sockets
from .admin_audit_logs_sockets import admin_audit_logs_sockets

def init_sockets(socketio):
    setup_home_sockets(socketio)
    admin_audit_logs_sockets(socketio)
