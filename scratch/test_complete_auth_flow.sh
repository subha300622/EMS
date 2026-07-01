#!/bin/bash
set -e

BASE_URL="http://localhost:8080/api/v1"

echo "============================================="
echo "=== STEP 1: Login to get Access & Refresh ==="
echo "============================================="
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "employee@company.com", "password": "employee@3"}')

echo "Login Response:"
echo "$LOGIN_RESP" | jq .

ACCESS_TOKEN=$(echo "$LOGIN_RESP" | jq -r '.data.token.accessToken // empty')
REFRESH_TOKEN=$(echo "$LOGIN_RESP" | jq -r '.data.token.refreshToken // empty')

if [ -z "$ACCESS_TOKEN" ] || [ -z "$REFRESH_TOKEN" ]; then
  # Fallback manual parsing if jq failed or response format is slightly different
  ACCESS_TOKEN=$(echo "$LOGIN_RESP" | grep -oP '"accessToken":"\K[^"]+')
  REFRESH_TOKEN=$(echo "$LOGIN_RESP" | grep -oP '"refreshToken":"\K[^"]+')
fi

if [ -z "$ACCESS_TOKEN" ] || [ -z "$REFRESH_TOKEN" ]; then
  echo "Error: Failed to obtain access or refresh token during login."
  exit 1
fi

echo "Access Token: ${ACCESS_TOKEN:0:20}..."
echo "Refresh Token: ${REFRESH_TOKEN:0:20}..."

echo ""
echo "================================================="
echo "=== STEP 2: Call /auth/me to verify access token ==="
echo "================================================="
ME_RESP=$(curl -s -X GET "$BASE_URL/auth/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "Get /auth/me Response:"
echo "$ME_RESP" | jq .

SUCCESS_ME=$(echo "$ME_RESP" | jq -r '.success')
if [ "$SUCCESS_ME" != "true" ]; then
  echo "Error: /auth/me call failed with the login access token."
  exit 1
fi

echo ""
echo "============================================="
echo "=== STEP 3: Call refresh to rotate tokens  ==="
echo "============================================="
REFRESH_RESP=$(curl -s -X POST "$BASE_URL/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}")

echo "Refresh Response:"
echo "$REFRESH_RESP" | jq .

NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESP" | jq -r '.data.accessToken // empty')
NEW_REFRESH_TOKEN=$(echo "$REFRESH_RESP" | jq -r '.data.refreshToken // empty')

if [ -z "$NEW_ACCESS_TOKEN" ] || [ -z "$NEW_REFRESH_TOKEN" ]; then
  NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESP" | grep -oP '"accessToken":"\K[^"]+')
  NEW_REFRESH_TOKEN=$(echo "$REFRESH_RESP" | grep -oP '"refreshToken":"\K[^"]+')
fi

if [ -z "$NEW_ACCESS_TOKEN" ] || [ -z "$NEW_REFRESH_TOKEN" ]; then
  echo "Error: Failed to refresh token."
  exit 1
fi

echo "New Access Token: ${NEW_ACCESS_TOKEN:0:20}..."
echo "New Refresh Token: ${NEW_REFRESH_TOKEN:0:20}..."

echo ""
echo "====================================================="
echo "=== STEP 4: Call protected API with new access token ==="
echo "====================================================="
DASHBOARD_RESP=$(curl -s -X GET "$BASE_URL/me/dashboard" \
  -H "Authorization: Bearer $NEW_ACCESS_TOKEN")

echo "Dashboard Response:"
echo "$DASHBOARD_RESP" | jq .

SUCCESS_DASHBOARD=$(echo "$DASHBOARD_RESP" | jq -r '.success')
if [ "$SUCCESS_DASHBOARD" != "true" ]; then
  echo "Error: Protected dashboard API call failed with the new access token."
  exit 1
fi

echo ""
echo "=============================="
echo "=== STEP 5: Logout user    ==="
echo "=============================="
LOGOUT_RESP=$(curl -s -X POST "$BASE_URL/auth/logout" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$NEW_REFRESH_TOKEN\"}")

echo "Logout Response:"
echo "$LOGOUT_RESP" | jq .

SUCCESS_LOGOUT=$(echo "$LOGOUT_RESP" | jq -r '.success')
if [ "$SUCCESS_LOGOUT" != "true" ]; then
  echo "Error: Logout request failed."
  exit 1
fi

echo ""
echo "=============================================="
echo "=== STEP 6: Verify old & new tokens fail   ==="
echo "============================================="
echo "Testing with old access token..."
OLD_FAIL_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$BASE_URL/auth/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN")
OLD_STATUS=$(echo "$OLD_FAIL_RESP" | tail -n1 | cut -d':' -f2)
OLD_BODY=$(echo "$OLD_FAIL_RESP" | sed '$d')

echo "Old Access Token Request Status (expected 401/403): $OLD_STATUS"
echo "Old Access Token Request Body: $OLD_BODY"

echo "Testing with new access token..."
NEW_FAIL_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$BASE_URL/auth/me" \
  -H "Authorization: Bearer $NEW_ACCESS_TOKEN")
NEW_STATUS=$(echo "$NEW_FAIL_RESP" | tail -n1 | cut -d':' -f2)
NEW_BODY=$(echo "$NEW_FAIL_RESP" | sed '$d')

echo "New Access Token Request Status (expected 401/403): $NEW_STATUS"
echo "New Access Token Request Body: $NEW_BODY"

if [ "$NEW_STATUS" -ne 401 ] && [ "$NEW_STATUS" -ne 403 ]; then
  echo "Error: Token verification did not fail after logout. Status code was $NEW_STATUS."
  exit 1
fi

echo ""
echo "=== SUCCESS: The complete authentication flow test passed successfully! ==="
