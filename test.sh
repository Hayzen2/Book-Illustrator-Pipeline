#!/bin/bash

# Ensure we are in the directory containing the script
cd "$(dirname "$0")"

echo "========================================="
echo " Running Book Illustrator Pipeline Tests "
echo "========================================="

# Run Spring Boot Tests
echo ""
echo "--- [1/2] Running Spring Boot Backend Tests ---"
cd backend
./mvnw test
BACKEND_EXIT_CODE=$?
cd ..

# Run React Tests
echo ""
echo "--- [2/2] Running React Frontend Tests ---"
cd frontend
npm run test
FRONTEND_EXIT_CODE=$?
cd ..

echo ""
echo "========================================="
echo " Test Execution Summary "
echo "========================================="

# Evaluate results
if [ $BACKEND_EXIT_CODE -eq 0 ] && [ $FRONTEND_EXIT_CODE -eq 0 ]; then
    echo "✅ SUCCESS: All backend and frontend tests passed!"
    exit 0
else
    echo "❌ FAILURE: One or more test suites failed."
    
    if [ $BACKEND_EXIT_CODE -ne 0 ]; then
        echo "   -> Backend tests failed (Exit code: $BACKEND_EXIT_CODE)"
    fi
    
    if [ $FRONTEND_EXIT_CODE -ne 0 ]; then
        echo "   -> Frontend tests failed (Exit code: $FRONTEND_EXIT_CODE)"
    fi
    
    exit 1
fi