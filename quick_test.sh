#!/bin/bash

echo "Testing whitelisted authentication endpoints..."

# Test 1: Check if /auth/check-method is accessible without authentication
echo "Test 1: Testing /auth/check-method without authentication"
response1=$(curl -s -o /dev/null -w "%{http_code}" -X POST "http://localhost:8080/auth/check-method" \
    -d "email=test@gmail.com" \
    -H "Content-Type: application/x-www-form-urlencoded")

echo "Response code: $response1"

if [ "$response1" -eq 200 ]; then
    echo "✅ PASS: /auth/check-method is accessible without authentication"
else
    echo "❌ FAIL: /auth/check-method returned $response1 instead of 200"
fi

echo ""

# Test 2: Check if a protected endpoint still requires authentication  
echo "Test 2: Testing a protected endpoint (should require authentication)"
response2=$(curl -s -o /dev/null -w "%{http_code}" -X GET "http://localhost:8080/protected")

echo "Response code: $response2"

if [ "$response2" -eq 401 ]; then
    echo "✅ PASS: Protected endpoints still require authentication"
else
    echo "❌ FAIL: Protected endpoint returned $response2 instead of 401"
fi

echo ""
echo "Done testing!"
