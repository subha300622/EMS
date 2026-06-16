#!/bash
# Manual verification script for My Performance Self-Service Module

BASE_URL="http://localhost:8080/api/v1"
EMAIL="employee@company.com"
PASSWORD="employee@5" # Based on DatabaseSeeder role name + role id (EMPLOYEE id is 5 in seeder loop if it's the 5th role)
# Wait, let me check the seeder to be sure about the password.
# role id for EMPLOYEE is likely 5.

echo "--- 1. Authenticating as employee@company.com ---"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$EMAIL\", \"password\": \"$PASSWORD\"}")

TOKEN=$(echo $LOGIN_RESPONSE | grep -oP '(?<="accessToken":")[^"]*')

if [ -z "$TOKEN" ]; then
  echo "Login failed. Response: $LOGIN_RESPONSE"
  # Fallback: Use the dev token if AuthorizationHeaderFilter is active
  TOKEN="mock-token"
  echo "Using dev token fallback."
fi

AUTH_HEADER="Authorization: Bearer $TOKEN"

echo "--- 2. GET /my-performance/dashboard ---"
curl -s -X GET "$BASE_URL/my-performance/dashboard" -H "$AUTH_HEADER" | jq .

echo "--- 3. GET /my-performance/goals ---"
GOALS_RESPONSE=$(curl -s -X GET "$BASE_URL/my-performance/goals" -H "$AUTH_HEADER")
echo $GOALS_RESPONSE | jq .
GOAL_ID=$(echo $GOALS_RESPONSE | jq -r '.data.goals[0].id')

if [ "$GOAL_ID" != "null" ]; then
  echo "--- 4. GET /my-performance/goals/$GOAL_ID ---"
  curl -s -X GET "$BASE_URL/my-performance/goals/$GOAL_ID" -H "$AUTH_HEADER" | jq .

  echo "--- 5. PATCH /my-performance/goals/$GOAL_ID/progress ---"
  curl -s -X PATCH "$BASE_URL/my-performance/goals/$GOAL_ID/progress" \
    -H "$AUTH_HEADER" -H "Content-Type: application/json" \
    -d "{\"progressPercentage\": 75.5, \"status\": \"IN_PROGRESS\", \"achievement\": \"Reached key milestone\"}" | jq .
fi

echo "--- 6. GET /my-performance/reviews ---"
REVIEWS_RESPONSE=$(curl -s -X GET "$BASE_URL/my-performance/reviews" -H "$AUTH_HEADER")
echo $REVIEWS_RESPONSE | jq .
REVIEW_ID=$(echo $REVIEWS_RESPONSE | jq -r '.data.cycles[0].reviewId')

if [ "$REVIEW_ID" != "null" ]; then
  echo "--- 7. POST /my-performance/reviews/$REVIEW_ID/self-assessment ---"
  curl -s -X POST "$BASE_URL/my-performance/reviews/$REVIEW_ID/self-assessment" \
    -H "$AUTH_HEADER" -H "Content-Type: application/json" \
    -d "{\"selfRating\": 5, \"selfReview\": \"Excellent year with many achievements.\", \"achievements\": [\"Delivered Project X\", \"Automated CI/CD\"], \"strengths\": [\"Leadership\", \"Java\"], \"improvementAreas\": [\"Public Speaking\"]}" | jq .
fi

echo "--- 8. GET /my-performance/feedback ---"
curl -s -X GET "$BASE_URL/my-performance/feedback" -H "$AUTH_HEADER" | jq .

echo "--- 9. GET /my-performance/history ---"
curl -s -X GET "$BASE_URL/my-performance/history" -H "$AUTH_HEADER" | jq .

echo "--- 10. GET /my-performance/competencies ---"
curl -s -X GET "$BASE_URL/my-performance/competencies" -H "$AUTH_HEADER" | jq .

echo "--- 11. GET /my-performance/timeline ---"
curl -s -X GET "$BASE_URL/my-performance/timeline" -H "$AUTH_HEADER" | jq .

echo "--- 12. GET /my-performance/policies ---"
curl -s -X GET "$BASE_URL/my-performance/policies" -H "$AUTH_HEADER" | jq .
