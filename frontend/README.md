# Safe Home Frontend

This is the frontend for the Safe Home web application, built with React.

## Setup

### Prerequisites
- Node 22.0+
- Docker (optional, for containerized deployment)

### Required Files
- `.env`: Contains environment variables (see below).

### Environment Variables
Create a `.env` file in the project root with the following variables:

| Variable                | Value/Format |
|-------------------------|--------------|
| `REACT_APP_BACKEND_URL` | Backend URL  |

### Running the Application

#### With Node
```bash
npm install --legacy-peer-deps # Install dependencies with legacy peer deps
npm run build # Build the production version
npm start # Start the development server
```

#### With Docker
```bash
docker build -t safe-home-frontend . # Build the Docker image
docker run -d -p 3000:80 safe-home-frontend # Run the container
```

### Troubleshooting

#### CORS Errors:

- Ensure CORS_ALLOW_ORIGINS in backend .env includes the frontend URL (only localhost:3000 for local development)
- Ensure REACT_APP_BACKEND_URL in frontend .env includes the backend URL (only localhost:5000 for local development)
