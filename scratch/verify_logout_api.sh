#!/bin/bash
set -e

echo "=== Authentication / Login ==="

# 1. Login as Employee
echo "Logging in as Employee (employee@company.com)..."
EMP_LOGIN_RESP=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "employee@company.com", "password": "employee@3"}')

# Extract access token and refresh token
EMP_ACCESS_TOKEN=$(echo "$EMP_LOGIN_RESP" | grep -oP '"accessToken":"\K[^"]+')
EMP_REFRESH_TOKEN=$(echo "$EMP_LOGIN_RESP" | grep -oP '"refreshToken":"\K[^"]+')

if [ -z "$EMP_REFRESH_TOKEN" ]; then
  echo "Failed to get refresh token. Full response:"
  echo "$EMP_LOGIN_RESP"
  exit 1
fi
echo "Login successful. Refresh token: $EMP_REFRESH_TOKEN"

echo ""
echo "=== Test Logout API ==="
LOGOUT_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$EMP_REFRESH_TOKEN\"}")

LOGOUT_STATUS=$(echo "$LOGOUT_RESP" | tail -n1 | cut -d':' -f2)
LOGOUT_BODY=$(echo "$LOGOUT_RESP" | sed '$d')

echo "Logout Response Status: $LOGOUT_STATUS"
echo "Logout Response Body: $LOGOUT_BODY"

if [ "$LOGOUT_STATUS" -ne 200 ]; then
  echo "FAIL: Expected 200 OK for logout"
  exit 1
fi

echo ""
echo "=== SUCCESS: Logout API works correctly! ==="
