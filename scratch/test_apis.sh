#!/bin/bash
TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiTUFOQUdFUiIsInNlc3Npb25JZCI6ImNlNjM3MWExLTcxNDctNDgwOC1iYmFkLTBiZWUyYjQ5NTM3MSIsInVzZXJJZCI6IkVNUDAwNCIsInN1YiI6Im1hbmFnZXJAY29tcGFueS5jb20iLCJpYXQiOjE3ODIyOTkxODIsImV4cCI6MTc4MjM4NTU4Mn0.QT4vmyMNEXTbp7xV9b96jb__v-KR3vvWIosFxYCnRJtL_ojRcT__wVCLxcp3Vlf2tZQUaaZY4ukMOCk_Rjkwkg"
BASE_URL="http://localhost:8080/api/v1/manager/team-assets"

echo "=== 1. GET Dashboard Summary ==="
curl -s -X GET "$BASE_URL/dashboard" -H "Authorization: Bearer $TOKEN" | jq .
echo ""

echo "=== 2. GET Team Assets Inventory ==="
curl -s -X GET "$BASE_URL?page=0&size=5" -H "Authorization: Bearer $TOKEN" | jq .
echo ""

echo "=== 3. GET Team Asset Details for Asset ID 1 ==="
curl -s -X GET "$BASE_URL/1" -H "Authorization: Bearer $TOKEN" | jq .
echo ""

echo "=== 4. GET Asset Timeline for Asset ID 1 ==="
curl -s -X GET "$BASE_URL/1/timeline" -H "Authorization: Bearer $TOKEN" | jq .
echo ""

echo "=== 5. GET Pending Asset Requests ==="
curl -s -X GET "$BASE_URL/requests?page=0&size=5" -H "Authorization: Bearer $TOKEN" | jq .
echo ""

echo "=== 6. GET Pending Return Requests ==="
curl -s -X GET "$BASE_URL/returns?page=0&size=5" -H "Authorization: Bearer $TOKEN" | jq .
echo ""

echo "=== 7. GET Analytics ==="
curl -s -X GET "$BASE_URL/analytics" -H "Authorization: Bearer $TOKEN" | jq .
echo ""
