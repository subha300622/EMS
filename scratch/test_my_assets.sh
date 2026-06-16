#!/bin/bash

# Setup colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color
BLUE='\033[0;34m'

echo -e "${BLUE}=== Starting My Assets Module Manual Verification ===${NC}"

# 1. Login as Super Admin
echo -e "\n${BLUE}Logging in as Super Admin...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"emssuperadmin@gmail.com","password":"Admin@123"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -oP '"accessToken":"\K[^"]+')

if [ -z "$TOKEN" ]; then
  echo -e "${RED}Login failed!${NC}"
  echo $LOGIN_RESPONSE
  exit 1
else
  echo -e "${GREEN}Login successful! Token acquired.${NC}"
fi

# 2. Get Categories
echo -e "\n${BLUE}1. Getting Allowed Categories...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-assets/categories" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 3. Get Policies
echo -e "\n${BLUE}2. Getting Asset Policies...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-assets/policies" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 4. Get Dashboard
echo -e "\n${BLUE}3. Getting Dashboard Stats...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-assets/dashboard" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 5. Get Assigned Assets
echo -e "\n${BLUE}4. Getting Assigned Assets...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-assets" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 6. Submit Asset Request
echo -e "\n${BLUE}5. Submitting a new Asset Request...${NC}"
REQ_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/my-assets/requests" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "assetCategory": "LAPTOP",
    "requestedModel": "MacBook Pro 16",
    "businessReason": "Need macOS for iOS builds",
    "priority": "HIGH",
    "requiredByDate": "2026-07-01"
  }')
echo $REQ_RESPONSE | jq .

# 7. Get Asset Requests List
echo -e "\n${BLUE}6. Getting Asset Requests List...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-assets/requests" \
  -H "Authorization: Bearer $TOKEN" | jq .

# Let's interact with one of the seeded assets for employee@company.com (since we are Super Admin we bypass ownership checks)
ASSET_ID=1

# 8. Get Asset Details
echo -e "\n${BLUE}7. Getting Details for Asset ID $ASSET_ID...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-assets/$ASSET_ID" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 9. Get Asset Timeline
echo -e "\n${BLUE}8. Getting Timeline for Asset ID $ASSET_ID...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-assets/$ASSET_ID/timeline" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 10. Report Issue on Asset
echo -e "\n${BLUE}9. Reporting Hardware Issue on Asset ID $ASSET_ID...${NC}"
ISSUE_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/my-assets/$ASSET_ID/issues" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "issueType": "HARDWARE",
    "severity": "HIGH",
    "title": "Battery Swelling",
    "description": "The laptop chassis is expanding, suspecting battery swelling."
  }')
echo $ISSUE_RESPONSE | jq .

# 11. Get Asset Issues List
echo -e "\n${BLUE}10. Getting Asset Issues List...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-assets/issues" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 12. Submit Asset Return Request
echo -e "\n${BLUE}11. Submitting Return Request for Asset ID $ASSET_ID...${NC}"
RETURN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/my-assets/$ASSET_ID/return-request" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "returnReason": "Project completion",
    "assetCondition": "GOOD",
    "accessoriesReturned": ["Charger", "Laptop Bag"],
    "comments": "Minor scratches on bottom casing."
  }')
echo $RETURN_RESPONSE | jq .

# 13. Re-Check Timeline
echo -e "\n${BLUE}12. Re-Checking Timeline to verify audits logged...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-assets/$ASSET_ID/timeline" \
  -H "Authorization: Bearer $TOKEN" | jq .

echo -e "\n${GREEN}=== Verification Complete ===${NC}"
