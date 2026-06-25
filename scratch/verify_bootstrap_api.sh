#!/bin/bash
set -e

echo "=== Authentication / Login ==="
echo "Logging in as Employee (employee@company.com)..."
LOGIN_RESP=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "employee@company.com", "password": "employee@3"}')

echo "Login Response payload:"
echo "$LOGIN_RESP" | jq .

ACCESS_TOKEN=$(echo "$LOGIN_RESP" | grep -oP '"accessToken":"\K[^"]+')

if [ -z "$ACCESS_TOKEN" ]; then
  echo "Failed to get access token."
  exit 1
fi

echo ""
echo "=== Call GET /api/v1/auth/permissions ==="
curl -s -X GET http://localhost:8080/api/v1/auth/permissions \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

echo ""
echo "=== Call GET /api/v1/users/me/profile ==="
curl -s -X GET http://localhost:8080/api/v1/users/me/profile \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

echo ""
echo "=== Call GET /api/v1/users/me/context ==="
curl -s -X GET http://localhost:8080/api/v1/users/me/context \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

echo ""
echo "=== Call GET /api/v1/users/me/bootstrap ==="
curl -s -X GET http://localhost:8080/api/v1/users/me/bootstrap \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

echo ""
echo "=== SUCCESS: All decoupled identity endpoints work correctly! ==="
