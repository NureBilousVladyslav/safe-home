# We use the official Python image
FROM python:3.11

# Install working director
WORKDIR /app

# Copy dependency files
COPY requirements.txt .

# Establish dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy all project files
COPY . .

# Open port 5000
EXPOSE 5000

# Launch the application via Gunicorn
CMD ["gunicorn", "-w", "4", "-b", "0.0.0.0:5000", "run:flask_app"]
