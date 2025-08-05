#!/bin/bash

# Simple test - just check if the endpoints exist
echo "=== Simple SSO Flow Test ==="

echo "Testing auth method check endpoint:"
timeout 5 curl -s -X POST "http://localhost:8080/auth/check-method?email=test@gmail.com" \
     -H "Content-Type: application/json" || echo "Backend not available on port 8080"

echo ""
echo "Testing authorization URL endpoint:"
timeout 5 curl -s -X GET "http://localhost:8080/auth/entra/authorization-url?redirect_uri=http://localhost:4200/auth/callback" \
     -H "Content-Type: application/json" || echo "Backend not available on port 8080"

echo ""
echo "=== Test Complete ==="
