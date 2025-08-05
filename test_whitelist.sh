#!/bin/bash

# Test script to verify that authentication endpoints are whitelisted

echo "Testing AuthCheckController endpoints..."

# Test check-method endpoint without authentication
echo "Testing /auth/check-method without authentication..."
curl -X POST "http://localhost:8080/auth/check-method" \
     -d "email=test@gmail.com" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -v

echo -e "\n\nTesting /auth/entra/authorization-url without authentication..."
curl -X GET "http://localhost:8080/auth/entra/authorization-url?redirect_uri=http://localhost:4200/callback" \
     -H "Content-Type: application/json" \
     -v

echo -e "\n\nTesting protected endpoint /refresh without authentication (should fail)..."
curl -X POST "http://localhost:8080/refresh" \
     -v

echo -e "\n\nTest completed!"
