#!/bin/bash

echo "Testing /auth/check-method endpoint..."

# Test the endpoint with curl to simulate frontend request
echo "Making POST request to /auth/check-method with query parameter..."

curl -v -X POST "http://localhost:8080/auth/check-method?email=test1@example.com" \
     -H "Accept: application/json, text/plain, */*" \
     -H "Content-Type: application/json" \
     -H "Origin: http://localhost:4200" \
     -d ''

echo -e "\n\nMaking POST request to /auth/check-method with form data..."

curl -v -X POST "http://localhost:8080/auth/check-method" \
     -H "Accept: application/json, text/plain, */*" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -H "Origin: http://localhost:4200" \
     -d 'email=test1@example.com'

echo -e "\n\nMaking POST request to /auth/check-method with JSON body..."

curl -v -X POST "http://localhost:8080/auth/check-method" \
     -H "Accept: application/json, text/plain, */*" \
     -H "Content-Type: application/json" \
     -H "Origin: http://localhost:4200" \
     -d '{"email": "test1@example.com"}'
