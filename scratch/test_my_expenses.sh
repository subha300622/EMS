#!/bin/bash

# Setup colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color
BLUE='\033[0;34m'

echo -e "${BLUE}=== Starting My Expenses Module Manual Verification ===${NC}"

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
echo -e "\n${BLUE}1. Getting Allowed Expense Categories...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-expenses/categories" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 3. Get Policies
echo -e "\n${BLUE}2. Getting Expense Policies...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-expenses/policies" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 4. Get Dashboard
echo -e "\n${BLUE}3. Getting Dashboard Stats...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-expenses/dashboard" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 5. Get My Expense List
echo -e "\n${BLUE}4. Getting Expense Claims List...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-expenses?status=PENDING_MANAGER_APPROVAL" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 6. Upload a Receipt
echo -e "\n${BLUE}5. Uploading a Receipt...${NC}"
echo "dummy invoice content" > dummy_invoice.pdf
UPLOAD_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/my-expenses/receipts" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@dummy_invoice.pdf" \
  -F "receiptType=INVOICE")
echo $UPLOAD_RESPONSE | jq .

RECEIPT_ID=$(echo $UPLOAD_RESPONSE | grep -oP '"receiptId":\K[0-9]+')
rm dummy_invoice.pdf

if [ -z "$RECEIPT_ID" ]; then
  echo -e "${RED}Receipt upload failed!${NC}"
  exit 1
fi

# 7. Create Expense Claim
echo -e "\n${BLUE}6. Creating a new Expense Claim...${NC}"
CREATE_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/my-expenses" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"category\": \"TRAVEL\",
    \"title\": \"Chennai to Bangalore Client Visit\",
    \"description\": \"Travel for customer meeting\",
    \"expenseDate\": \"2026-06-16\",
    \"amount\": 4500.00,
    \"currency\": \"INR\",
    \"projectCode\": \"PRJ-101\",
    \"receiptIds\": [$RECEIPT_ID]
  }")
echo $CREATE_RESPONSE | jq .

NEW_CLAIM_ID=$(echo $CREATE_RESPONSE | grep -oP '"expenseId":\K[0-9]+')

# 8. Get Expense Details
echo -e "\n${BLUE}7. Getting Details for Claim ID $NEW_CLAIM_ID...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-expenses/$NEW_CLAIM_ID" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 9. Get Expense Timeline
echo -e "\n${BLUE}8. Getting Timeline for Claim ID $NEW_CLAIM_ID...${NC}"
curl -s -X GET "http://localhost:8080/api/v1/my-expenses/$NEW_CLAIM_ID/timeline" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 10. Update Expense Claim (Allowed on DRAFT claim, seeded draft ID is 24)
DRAFT_CLAIM_ID=24
echo -e "\n${BLUE}9. Updating Draft Claim ID $DRAFT_CLAIM_ID...${NC}"
curl -s -X PUT "http://localhost:8080/api/v1/my-expenses/$DRAFT_CLAIM_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Broadband Bill",
    "description": "Corrected billing amount details",
    "amount": 1200.00
  }' | jq .

# 11. Withdraw Expense Claim (Cancel the newly created claim)
echo -e "\n${BLUE}10. Withdrawing Claim ID $NEW_CLAIM_ID...${NC}"
curl -s -X PATCH "http://localhost:8080/api/v1/my-expenses/$NEW_CLAIM_ID/withdraw" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Incorrect billing codes selected"}' | jq .

# 12. Download Receipt Binary Stream
echo -e "\n${BLUE}11. Downloading Receipt ID $RECEIPT_ID...${NC}"
curl -i -s -X GET "http://localhost:8080/api/v1/my-expenses/receipts/$RECEIPT_ID/download" \
  -H "Authorization: Bearer $TOKEN" > receipt_download.txt
head -n 10 receipt_download.txt
rm receipt_download.txt

echo -e "\n${GREEN}=== Verification Complete ===${NC}"
