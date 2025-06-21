#!/bin/bash

# Test script for signup endpoint
echo "Testing signup endpoint..."

# Test 1: Valid signup request
echo "Test 1: Valid signup request"
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "confirmPassword": "password123"
  }' \
  -v

echo ""
echo "---"

# Test 2: Invalid signup (missing confirmPassword)
echo "Test 2: Invalid signup (missing confirmPassword)"
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser2",
    "email": "test2@example.com",
    "password": "password123"
  }' \
  -v

echo ""
echo "---"

# Test 3: Invalid signup (password mismatch)
echo "Test 3: Invalid signup (password mismatch)"
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser3",
    "email": "test3@example.com",
    "password": "password123",
    "confirmPassword": "different123"
  }' \
  -v

echo ""
echo "---"

# Test 4: Invalid email format
echo "Test 4: Invalid email format"
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser4",
    "email": "invalid-email",
    "password": "password123",
    "confirmPassword": "password123"
  }' \
  -v

echo ""
echo "Testing complete!"
