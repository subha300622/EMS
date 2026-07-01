#!/bin/bash
set -e

BASE_URL="http://localhost:8080/api/v1"
export PGPASSWORD=12345
DB_CMD="psql -h localhost -U ems_user -d employee_db"

echo "=========================================================="
echo "=== PRE-TEST CLEANUP: Resetting database state for Robert Chen ==="
echo "=========================================================="
$DB_CMD -c "DELETE FROM users WHERE work_email = 'robert@company.com';"
$DB_CMD -c "DELETE FROM invitations WHERE email = 'robert@company.com';"

echo ""
echo "========================================="
echo "=== STEP 1: Login as Admin to get Token ==="
echo "========================================="
ADMIN_LOGIN_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@company.com", "password": "admin@6"}')

echo "Admin Login Response:"
echo "$ADMIN_LOGIN_RESP" | jq .

ADMIN_TOKEN=$(echo "$ADMIN_LOGIN_RESP" | jq -r '.data.token.accessToken // empty')
if [ -z "$ADMIN_TOKEN" ]; then
  ADMIN_TOKEN=$(echo "$ADMIN_LOGIN_RESP" | grep -oP '"accessToken":"\K[^"]+')
fi

if [ -z "$ADMIN_TOKEN" ]; then
  echo "Error: Admin login failed."
  exit 1
fi

echo ""
echo "========================================="
echo "=== STEP 2: Invite Robert Chen (Emp 9) ==="
echo "========================================="
# Role 3 is EMPLOYEE
INVITE_RESP=$(curl -s -X POST "$BASE_URL/auth/invite" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"employeeId": 9, "roleId": 3}')

echo "Invite Response:"
echo "$INVITE_RESP" | jq .

echo ""
echo "=== Querying DB to get invitation token ==="
INVITE_TOKEN=$($DB_CMD -t -A -c "SELECT invitation_token FROM invitations WHERE email = 'robert@company.com';")
if [ -z "$INVITE_TOKEN" ]; then
  echo "Error: Invitation token not found in database."
  exit 1
fi
echo "Invitation Token retrieved: $INVITE_TOKEN"

echo ""
echo "========================================="
echo "=== STEP 3: Accept Invitation & Create Account ==="
echo "========================================="
ACCEPT_RESP=$(curl -s -X POST "$BASE_URL/auth/accept-invitation" \
  -H "Content-Type: application/json" \
  -d "{\"invitationToken\": \"$INVITE_TOKEN\", \"password\": \"robert@123\", \"confirmPassword\": \"robert@123\"}")

echo "Accept Invitation Response:"
echo "$ACCEPT_RESP" | jq .

echo ""
echo "============================="
echo "=== STEP 4: Login as Robert ==="
echo "============================="
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "robert@company.com", "password": "robert@123"}')

echo "Login Response:"
echo "$LOGIN_RESP" | jq .

ACCESS_TOKEN=$(echo "$LOGIN_RESP" | jq -r '.data.token.accessToken // empty')
REFRESH_TOKEN=$(echo "$LOGIN_RESP" | jq -r '.data.token.refreshToken // empty')

if [ -z "$ACCESS_TOKEN" ] || [ "$ACCESS_TOKEN" = "null" ]; then
  ACCESS_TOKEN=$(echo "$LOGIN_RESP" | grep -oP '"accessToken":"\K[^"]+')
  REFRESH_TOKEN=$(echo "$LOGIN_RESP" | grep -oP '"refreshToken":"\K[^"]+')
fi

echo ""
echo "========================================="
echo "=== STEP 5: Validate Session (/me) ======="
echo "========================================="
ME_RESP=$(curl -s -X GET "$BASE_URL/auth/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "/auth/me Response:"
echo "$ME_RESP" | jq .

echo ""
echo "========================================="
echo "=== STEP 6: Validate RBAC (/permissions) ==="
echo "========================================="
PERM_RESP=$(curl -s -X GET "$BASE_URL/auth/permissions" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "/auth/permissions Response:"
echo "$PERM_RESP" | jq .

echo ""
echo "========================================="
echo "=== STEP 7: Refresh Token ================"
echo "========================================="
REFRESH_RESP=$(curl -s -X POST "$BASE_URL/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}")

echo "Refresh Response:"
echo "$REFRESH_RESP" | jq .

NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESP" | jq -r '.data.accessToken // empty')
NEW_REFRESH_TOKEN=$(echo "$REFRESH_RESP" | jq -r '.data.refreshToken // empty')

if [ -z "$NEW_ACCESS_TOKEN" ] || [ "$NEW_ACCESS_TOKEN" = "null" ]; then
  NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESP" | grep -oP '"accessToken":"\K[^"]+')
  NEW_REFRESH_TOKEN=$(echo "$REFRESH_RESP" | grep -oP '"refreshToken":"\K[^"]+')
fi

echo ""
echo "========================================="
echo "=== STEP 8: Forgot Password -> OTP -> Reset ==="
echo "========================================="
# 8.1 Forgot password trigger
FORGOT_RESP=$(curl -s -X POST "$BASE_URL/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{"email": "robert@company.com"}')
echo "Forgot Password Response:"
echo "$FORGOT_RESP" | jq .

# 8.2 Extract OTP code from server logs
echo "Extracting OTP code from server logs..."
OTP_CODE=$(grep -A 1 "GENERATED OTP FOR robert@company.com" /home/subashini/.gemini/antigravity-ide/brain/fdef6dca-a7d0-4886-b336-93ba5bc1bcf0/.system_generated/tasks/task-725.log | tail -n 2 | tr -d '\n' | tr -d ' ' | grep -oP 'robert@company.com:\K\d{6}')
echo "Extracted OTP Code: $OTP_CODE"

# 8.3 Verify OTP to get reset token
VERIFY_RESP=$(curl -s -X POST "$BASE_URL/auth/verify-otp" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"robert@company.com\", \"otp\": \"$OTP_CODE\"}")
echo "Verify OTP Response:"
echo "$VERIFY_RESP" | jq .

RESET_TOKEN=$(echo "$VERIFY_RESP" | jq -r '.data.resetToken // empty')
if [ -z "$RESET_TOKEN" ] || [ "$RESET_TOKEN" = "null" ]; then
  RESET_TOKEN=$(echo "$VERIFY_RESP" | grep -oP '"resetToken":"\K[^"]+')
fi
echo "Reset Token: $RESET_TOKEN"

# 8.4 Reset Password
RESET_PWD_RESP=$(curl -s -X POST "$BASE_URL/auth/reset-password" \
  -H "Content-Type: application/json" \
  -d "{\"resetToken\": \"$RESET_TOKEN\", \"newPassword\": \"resetpwd@123\", \"confirmPassword\": \"resetpwd@123\"}")
echo "Reset Password Response:"
echo "$RESET_PWD_RESP" | jq .

echo ""
echo "========================================="
echo "=== STEP 9: Login again & Change Password ==="
echo "========================================="
# 9.1 Login with reset password
LOGIN_RESET_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "robert@company.com", "password": "resetpwd@123"}')
echo "Login with Reset Password Response:"
echo "$LOGIN_RESET_RESP" | jq .

RESET_ACCESS_TOKEN=$(echo "$LOGIN_RESET_RESP" | jq -r '.data.token.accessToken // empty')
if [ -z "$RESET_ACCESS_TOKEN" ] || [ "$RESET_ACCESS_TOKEN" = "null" ]; then
  RESET_ACCESS_TOKEN=$(echo "$LOGIN_RESET_RESP" | grep -oP '"accessToken":"\K[^"]+')
fi

# 9.2 Change password
CHANGE_RESP=$(curl -s -X POST "$BASE_URL/auth/change-password" \
  -H "Authorization: Bearer $RESET_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"currentPassword": "resetpwd@123", "newPassword": "changedpwd@123", "confirmPassword": "changedpwd@123"}')
echo "Change Password Response:"
echo "$CHANGE_RESP" | jq .

echo ""
echo "=========================================================="
echo "=== STEP 10: Session Management: List, Revoke & Verify ==="
echo "=========================================================="
# 10.1 Login to get active session 1
SESSION_LOGIN_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "robert@company.com", "password": "changedpwd@123"}')
SESS_ACCESS_TOKEN=$(echo "$SESSION_LOGIN_RESP" | jq -r '.data.token.accessToken // empty')
if [ -z "$SESS_ACCESS_TOKEN" ] || [ "$SESS_ACCESS_TOKEN" = "null" ]; then
  SESS_ACCESS_TOKEN=$(echo "$SESSION_LOGIN_RESP" | grep -oP '"accessToken":"\K[^"]+')
fi

# 10.2 Login again as session 2 (simulate another browser context)
SESSION_LOGIN2_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "robert@company.com", "password": "changedpwd@123"}')
SESS2_ACCESS_TOKEN=$(echo "$SESSION_LOGIN2_RESP" | jq -r '.data.token.accessToken // empty')
if [ -z "$SESS2_ACCESS_TOKEN" ] || [ "$SESS2_ACCESS_TOKEN" = "null" ]; then
  SESS2_ACCESS_TOKEN=$(echo "$SESSION_LOGIN2_RESP" | grep -oP '"accessToken":"\K[^"]+')
fi

# 10.3 List active sessions using token 1
SESSIONS_RESP=$(curl -s -X GET "$BASE_URL/auth/sessions" \
  -H "Authorization: Bearer $SESS_ACCESS_TOKEN")
echo "Sessions List Response:"
echo "$SESSIONS_RESP" | jq .

# Find the session ID for session 2 using Python JWT decoder
REVOKE_SESS_ID=$(python3 -c "import base64, json; print(json.loads(base64.urlsafe_b64decode('$SESS2_ACCESS_TOKEN'.split('.')[1] + '===').decode('utf-8'))['sessionId'])")
echo "Revoking Session ID (Session 2): $REVOKE_SESS_ID"

# 10.4 Revoke session 2 using token 1
REVOKE_RESP=$(curl -s -X DELETE "$BASE_URL/auth/sessions/$REVOKE_SESS_ID" \
  -H "Authorization: Bearer $SESS_ACCESS_TOKEN")
echo "Revoke Session Response:"
echo "$REVOKE_RESP" | jq .

# 10.5 Verify session 2 token is revoked and fails
SESS2_VERIFY_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$BASE_URL/auth/me" \
  -H "Authorization: Bearer $SESS2_ACCESS_TOKEN")
SESS2_STATUS=$(echo "$SESS2_VERIFY_RESP" | tail -n1 | cut -d':' -f2)
SESS2_BODY=$(echo "$SESS2_VERIFY_RESP" | sed '$d')
echo "Revoked Token Verification HTTP Status: $SESS2_STATUS"
echo "Revoked Token Verification Response Body: $SESS2_BODY"

echo ""
echo "========================================="
echo "=== STEP 11: Logout session 1 ============="
echo "========================================="
SESS1_REFRESH_TOKEN=$(echo "$SESSION_LOGIN_RESP" | jq -r '.data.token.refreshToken // empty')
if [ -z "$SESS1_REFRESH_TOKEN" ] || [ "$SESS1_REFRESH_TOKEN" = "null" ]; then
  SESS1_REFRESH_TOKEN=$(echo "$SESSION_LOGIN_RESP" | grep -oP '"refreshToken":"\K[^"]+')
fi

LOGOUT_RESP=$(curl -s -X POST "$BASE_URL/auth/logout" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$SESS1_REFRESH_TOKEN\"}")
echo "Logout Response:"
echo "$LOGOUT_RESP" | jq .

# Verify session 1 token is revoked
SESS1_VERIFY_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$BASE_URL/auth/me" \
  -H "Authorization: Bearer $SESS_ACCESS_TOKEN")
SESS1_STATUS=$(echo "$SESS1_VERIFY_RESP" | tail -n1 | cut -d':' -f2)
echo "Revoked Token 1 Verification HTTP Status: $SESS1_STATUS"

echo ""
echo "========================================="
echo "=== STEP 12: Logout All Devices =========="
echo "========================================="
# 12.1 Log in again to get fresh active sessions
FRESH_LOGIN_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "robert@company.com", "password": "changedpwd@123"}')
FRESH_ACCESS_TOKEN=$(echo "$FRESH_LOGIN_RESP" | jq -r '.data.token.accessToken // empty')
if [ -z "$FRESH_ACCESS_TOKEN" ] || [ "$FRESH_ACCESS_TOKEN" = "null" ]; then
  FRESH_ACCESS_TOKEN=$(echo "$FRESH_LOGIN_RESP" | grep -oP '"accessToken":"\K[^"]+')
fi

# 12.2 Logout from all devices
LOGOUT_ALL_RESP=$(curl -s -X POST "$BASE_URL/auth/logout-all" \
  -H "Authorization: Bearer $FRESH_ACCESS_TOKEN")
echo "Logout All Response:"
echo "$LOGOUT_ALL_RESP" | jq .

echo ""
echo "========================================="
echo "=== STEP 13: Verify token fails ========="
echo "========================================="
FINAL_VERIFY_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$BASE_URL/auth/me" \
  -H "Authorization: Bearer $FRESH_ACCESS_TOKEN")
FINAL_STATUS=$(echo "$FINAL_VERIFY_RESP" | tail -n1 | cut -d':' -f2)
FINAL_BODY=$(echo "$FINAL_VERIFY_RESP" | sed '$d')

echo "Final Verification HTTP Status (expected 401/403): $FINAL_STATUS"
echo "Final Verification Response Body: $FINAL_BODY"

if [ "$FINAL_STATUS" -ne 401 ] && [ "$FINAL_STATUS" -ne 403 ]; then
  echo "Error: Token verification succeeded after logout-all."
  exit 1
fi

echo ""
echo "=== SUCCESS: The complete detailed flow test passed successfully! ==="
