#!/bin/bash

# Production startup script for Groupy Application
# Usage: ./run-prod.sh

# Load environment variables from .env file if it exists
if [ -f .env ]; then
    export $(cat .env | grep -v '#' | xargs)
fi

# Verify required environment variables
if [ -z "$DB_URL" ] || [ -z "$DB_USERNAME" ] || [ -z "$DB_PASSWORD" ]; then
    echo "Error: Missing required environment variables (DB_URL, DB_USERNAME, DB_PASSWORD)"
    echo "Please set these variables in your .env file or export them before running this script"
    exit 1
fi

# Start the application
java -jar target/groupy-application.jar \
    --spring.profiles.active=prod \
    --server.port=${PORT:-8080}
