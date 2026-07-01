#!/bin/bash
set -e

BASE_URL="http://localhost:8080/api/files"

echo "============================================="
echo "  Secure Firebase Storage API Test Script"
echo "============================================="

# 1. Login Helper
login_user() {
  local email=$1
  local password=$2
  local resp=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"email\": \"$email\", \"password\": \"$password\"}")
  
  local token=$(echo "$resp" | grep -oP '"accessToken":"\K[^"]+')
  if [ -z "$token" ]; then
    echo "ERROR: Failed to login as $email. Response:"
    echo "$resp"
    exit 1
  fi
  echo "$token"
}

echo "Logging in users..."
EMP_TOKEN=$(login_user "employee@company.com" "employee@3")
MGR_TOKEN=$(login_user "manager@company.com" "manager@4")
ADMIN_TOKEN=$(login_user "super_admin@company.com" "super_admin@1")
echo "Login successful for all users."
echo ""

# Create a temporary file to upload
echo "This is secure user document content." > /tmp/test_user_doc.txt

# 2. Upload Document
echo "=== 1. Uploading Document (as employee) ==="
UPLOAD_RESP=$(curl -s -X POST "$BASE_URL/upload-document" \
  -H "Authorization: Bearer $EMP_TOKEN" \
  -F "file=@/tmp/test_user_doc.txt" \
  -F "fileType=DOCUMENT")

echo "Upload Response:"
echo "$UPLOAD_RESP" | jq .

FILE_ID=$(echo "$UPLOAD_RESP" | jq -r '.data.id')
if [ "$FILE_ID" == "null" ] || [ -z "$FILE_ID" ]; then
  echo "ERROR: Upload failed or file ID not returned."
  exit 1
fi
echo "Uploaded File ID: $FILE_ID"
echo ""

# 3. Access Checks
echo "=== 2. Downloading Document as Owner (employee) - Expecting HTTP 200 ==="
curl -i -s -X GET "$BASE_URL/$FILE_ID/download" \
  -H "Authorization: Bearer $EMP_TOKEN"
echo ""
echo ""

echo "=== 3. Downloading Document as Other User (manager) - Expecting HTTP 403 ==="
curl -i -s -X GET "$BASE_URL/$FILE_ID/download" \
  -H "Authorization: Bearer $MGR_TOKEN"
echo ""
echo ""

echo "=== 4. Downloading Document as Super Admin - Expecting HTTP 200 ==="
curl -i -s -X GET "$BASE_URL/$FILE_ID/download" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
echo ""
echo ""

echo "=== 5. Downloading Document with No Token - Expecting HTTP 401/403 ==="
curl -i -s -X GET "$BASE_URL/$FILE_ID/download"
echo ""
echo ""

# 4. Upload Profile Image
echo "Create a temporary image file..."
echo "fake image bytes" > /tmp/profile.png

echo "=== 6. Uploading Profile Image (as employee) ==="
PROFILE_RESP=$(curl -s -X POST "$BASE_URL/profile-image" \
  -H "Authorization: Bearer $EMP_TOKEN" \
  -F "file=@/tmp/profile.png")

echo "Profile Image Upload Response:"
echo "$PROFILE_RESP" | jq .

PROFILE_FILE_ID=$(echo "$PROFILE_RESP" | jq -r '.data.id')
echo "Uploaded Profile Image File ID: $PROFILE_FILE_ID"
echo ""

echo "=== 7. Downloading Profile Image as Owner (employee) - Expecting HTTP 200 ==="
curl -i -s -X GET "$BASE_URL/$PROFILE_FILE_ID/download" \
  -H "Authorization: Bearer $EMP_TOKEN"
echo ""
echo ""

echo "=== 8. Downloading Profile Image with No Token (Public) - Expecting HTTP 200 ==="
curl -i -s -X GET "$BASE_URL/$PROFILE_FILE_ID/download"
echo ""
echo ""

echo "=== 9. Downloading Private Document via token query parameter - Expecting HTTP 200 ==="
curl -i -s -X GET "$BASE_URL/$FILE_ID/download?token=$EMP_TOKEN"
echo ""
echo ""

# Cleanup temp files
rm -f /tmp/test_user_doc.txt /tmp/profile.png

echo "============================================="
echo "             API Tests Complete!"
echo "============================================="
