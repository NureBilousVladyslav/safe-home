# Safe Home Backend

This is the backend for the Safe Home application, built with Flask, Flask-Migrate, and SQLAlchemy.

## Setup

### Prerequisites
- Python 3.8+
- Docker (optional, for containerized deployment)
- PostgreSQL database (local or Azure-hosted)

### Required Files
- `.env`: Contains environment variables (see below).

### Environment Variables
Create a `.env` file in the project root with the following variables:

| Variable                     | Value/Format                         |
|------------------------------|--------------------------------------|
| `SECRET_KEY`                 | Secure random string (e.g., 32+ chars) |
| `SECRET_KEY_Fernet`          | Fernet key for encryption           |
| `GOOGLE_CLIENT_ID`           | Google OAuth client ID              |
| `GOOGLE_CLIENT_SECRET`       | Google OAuth client secret          |
| `DATABASE_URL`               | PostgreSQL URI |
| `PROJECT_ID`                 | Firebase project ID                 |
| `PRIVATE_KEY_ID`             | Firebase private key ID             |
| `PRIVATE_KEY`                | Firebase private key (multi-line)   |
| `CLIENT_EMAIL`               | Firebase client email               |
| `CLIENT_ID`                  | Firebase client ID                  |
| `CLIENT_CERT_URL`            | Firebase client cert URL            |
| `ADMIN_EMAIL`                | Admin email address                 |
| `ADMIN_NAME`                 | Admin username                      |
| `ADMIN_PASSWORD`             | Admin password                      |
| `FLASK_DEBUG`                | `True` (development) or `False` (production) |
| `AUTO_DB_SETUP`              | `True` (auto setup) or `False` (manual setup) |
| `MAIL_USERNAME`              | Email account username              |
| `MAIL_PASSWORD`              | Email account password or app key   |
| `MAIL_DEFAULT_SENDER`        | Sender email address                |
| `MAIL_SENDER_NAME`           | Sender name                         |
| `STRIPE_PUBLISHABLE_KEY`     | Stripe publishable key              |
| `STRIPE_SECRET_KEY`          | Stripe secret key                   |
| `CORS_ALLOW_ORIGINS`         | Allowed CORS origins (e.g., `*` or URL list) |
| `FRONTEND_LINK`             | Frontend URL |
| `DOMAIN_URL`                 | Backend URL|
| `FLASK_APP`                  | Entry point file (e.g., `run.py`)   |

### Database Setup
If `AUTO_DB_SETUP=False` in `.env`, run these commands in the project root:

```bash
flask db init          # Initialize migrations (if migrations folder is missing)
flask db migrate       # Create migration files (if models have changed)
flask db upgrade       # Apply migrations to the database
flask seed init        # Populate database with initial data (roles, admin, etc.)
flask seed init --force # Reset existing data and repopulate
```

If AUTO_DB_SETUP=True, the application automatically sets up the database and seeds data on startup. Disable this in production by setting AUTO_DB_SETUP=False.

### Running the Application

#### With Python
```bash
python -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate
pip install -r requirements.txt
flask run
```

#### With Docker
```bash
docker build -t safe_home_backend .
docker run -p 5000:5000 --env-file .env safe_home_backend
```
