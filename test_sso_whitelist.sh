#!/bin/bash

echo "Testing AuthCheckController endpoint without authentication..."

# Test that check-method endpoint is accessible without authentication
echo "Sending POST request to /auth/check-method..."
response=$(curl -s -w "%{http_code}" -X POST "http://localhost:8080/auth/check-method" \
     -d "email=test@gmail.com" \
     -H "Content-Type: application/x-www-form-urlencoded")

status_code="${response: -3}"
response_body="${response%???}"

echo "Status Code: $status_code"
echo "Response Body: $response_body"
echo ""

if [ "$status_code" = "200" ]; then
    echo "✅ SUCCESS: /auth/check-method is accessible without authentication"
else
    echo "❌ FAILED: /auth/check-method returned status $status_code"
fi

echo ""
echo "Testing SSO user authentication flow..."

# Test SSO redirect for SSO domains
echo "Testing login with SSO domain (gmail.com)..."
sso_response=$(curl -s -w "%{http_code}" -X POST "http://localhost:8080/login" \
     -H "X-Email: test@gmail.com" \
     -H "X-Password: password")

sso_status_code="${sso_response: -3}"
sso_response_body="${sso_response%???}"

echo "Status Code: $sso_status_code"
echo "Response Body: $sso_response_body"
echo ""

if [ "$sso_status_code" = "401" ] && [[ "$sso_response_body" == *"SSO_REDIRECT_REQUIRED"* ]]; then
    echo "✅ SUCCESS: SSO redirect is working correctly"
else
    echo "❌ FAILED: SSO redirect not working as expected"
fi
