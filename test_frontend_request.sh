#!/bin/bash

echo "Testing the exact frontend request pattern..."

# Test exactly like the frontend does
echo "Testing POST /auth/check-method with query parameter and no body:"
response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST \
  "http://localhost:8080/auth/check-method?email=test1@example.com" \
  -H "Accept: application/json, text/plain, */*" \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:4200" \
  -d '')

echo "$response"
echo "----"

# Test with a known working domain
echo "Testing POST /auth/check-method with SSO domain:"
response2=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST \
  "http://localhost:8080/auth/check-method?email=test@gmail.com" \
  -H "Accept: application/json, text/plain, */*" \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:4200" \
  -d '')

echo "$response2"
