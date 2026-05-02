# Safe Home  

**Safe Home** is a comprehensive home security management system that leverages door open/close sensors to provide real-time monitoring and control. It combines a robust backend, an intuitive web admin interface, and a user-friendly mobile app to ensure secure and scalable home protection.

## Table of Contents
- [Project Overview](#project-overview)
- [Features](#features)
- [Architecture](#architecture)
- [Setup Instructions](#setup-instructions)
- [DevOps](#devops)
- [Authors](#authors)

## Project Overview

Safe Home is designed to enhance home security through a seamless integration of hardware and software. The system monitors door sensors in real-time, providing users with instant notifications and control via a mobile app, while administrators manage the system through a web interface.

### Integrated Services
- **Backend**: A REST API built with Flask, using PostgreSQL for reliable data storage and management.
- **Frontend**: A Single Page Application (SPA) developed with React, served via Nginx, tailored for admin users.
- **Mobile**: An Android application written in Kotlin, designed for end-users to monitor and control their home security.

## Features
- **Real-time Sensor Monitoring**: Instant updates on door open/close events.
- **Scalable Architecture**: Supports multiple homes and users with a modular design.
- **Secure Authentication**: Protected API endpoints with OAuth and session management.
- **Admin Dashboard**: Web interface for managing sensors, users, and subscriptions.
- **Mobile Access**: User-friendly Android app for remote monitoring and notifications.
- **Subscription Management**: Integration with Stripe for handling payment plans.

## Architecture
The project is structured as a distributed system:
- **Backend**: Flask-based REST API with SQLAlchemy and Flask-Migrate, hosted on Azure App Service.
- **Frontend**: React SPA, bundled with Webpack, served by Nginx, hosted on Azure App Service.
- **Mobile**: Kotlin-based Android app, distributed via Google Play Store.
- **Database**: PostgreSQL on Azure Database for PostgreSQL.
- **CI/CD**: Azure Pipelines for automated builds, tests, and deployments.

## Setup Instructions

### Prerequisites
- **Tools**: Python 3.8+, Node.js 16+, Docker, Android Studio, Azure CLI.
- **Azure Services**: App Service, Static Web Apps, Database for PostgreSQL.
- **Dependencies**: Listed in `backend/requirements.txt`, `frontend/package.json`, and mobile app `build.gradle`.

#### Checkout the README files in the backend, fronted and mobile directories for detailed setup instructions.

## DevOps

The project uses **Microsoft Azure** for CI/CD:
- **Azure App Service**: Hosts the Flask backend.
- **GitHub Actions**: CI/CD for backend.
- **Azure App Service**: Hosts the React frontend.
- **Azure Pipelines**: Automates build, test, and deployment for backend, frontend, and mobile.
- **Azure Database for PostgreSQL**: Stores application data.
- **Monitoring**: Azure Log Stream and Application Insights for logs and performance.

## Authors
- **Nikita Lisovyi**: Backend Developer, DevOps Engineer, Mobile App Designer
- **Oleksandr Kozhanov**: Frontend Developer, DevOps Engineer, Web App Designer
- **Bilous Vladyslav**: Mobile Developer
