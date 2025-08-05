#!/bin/bash

# Test Script for SSO Flow
echo "=== Testing SSO Flow Implementation ==="

# Wait for backend to be available
echo "Waiting for backend to be available..."
for i in {1..30}; do
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/auth/check-method > /dev/null 2>&1; then
        echo "Backend is available"
        break
    fi
    sleep 2
done

echo ""
echo "1. Testing auth method check for SSO user (gmail.com):"
curl -X POST "http://localhost:8080/auth/check-method?email=test@gmail.com" \
     -H "Content-Type: application/json" \
     -w "\nHTTP Status: %{http_code}\n" \
     2>/dev/null

echo ""
echo "2. Testing auth method check for database user (example.com):"
curl -X POST "http://localhost:8080/auth/check-method?email=test@example.com" \
     -H "Content-Type: application/json" \
     -w "\nHTTP Status: %{http_code}\n" \
     2>/dev/null

echo ""
echo "3. Testing SSO user login (should return SSO redirect required):"
curl -X POST "http://localhost:8080/login" \
     -H "X-Email: test@gmail.com" \
     -H "X-Password: testpass" \
     -H "Content-Type: application/json" \
     -w "\nHTTP Status: %{http_code}\n" \
     2>/dev/null

echo ""
echo "4. Testing authorization URL generation:"
curl -X GET "http://localhost:8080/auth/entra/authorization-url?redirect_uri=http://localhost:4200/auth/callback&state=test123" \
     -H "Content-Type: application/json" \
     -w "\nHTTP Status: %{http_code}\n" \
     2>/dev/null

echo ""
echo "=== Testing Complete ==="
