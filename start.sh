#!/bin/bash

# Ensure we are in the directory containing the script
cd "$(dirname "$0")"

echo "========================================="
echo " Starting Book Illustrator Pipeline... "
echo "========================================="

# Define the cleanup function
cleanup() {
    echo ""
    echo "========================================="
    echo " Caught Ctrl+C! Initiating teardown... "
    echo "========================================="
    
    echo "[1/3] Stopping background processes (Spring Boot & React)..."
    # Kill all child processes of this script
    kill $(jobs -p) 2>/dev/null
    
    echo "[2/3] Tearing down MySQL container..."
    docker-compose down
    
    echo "[3/3] Teardown complete. Goodbye!"
    exit 0
}

# Trap SIGINT (Ctrl+C) and SIGTERM and route them to the cleanup function
trap cleanup SIGINT SIGTERM

echo "-> Starting MySQL 8.0 container in the background..."
docker-compose up -d

echo "-> Waiting for database to initialize (10 seconds)..."
sleep 10

echo "-> Starting Spring Boot Backend..."
cd backend
./mvnw spring-boot:run &
BACKEND_PID=$!
cd ..

echo "-> Starting React Frontend..."
cd frontend
npm run dev &
FRONTEND_PID=$!
cd ..

echo "========================================="
echo " All services are starting up! "
echo " Press Ctrl+C at any time to gracefully stop everything."
echo "========================================="

# Wait indefinitely until a signal (like Ctrl+C) is received
wait