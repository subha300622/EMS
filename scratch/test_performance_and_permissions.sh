#!/bin/bash
set -e

PORT=8080
BASE_URL="http://localhost:$PORT/api/v1"

# Generate random suffix for unique names to prevent conflict on repeated runs
SUFFIX=$RANDOM
PERM_NAME="test.custom.perf.permission.$SUFFIX"
CYCLE_NAME="FY 2026-27 Test Cycle $SUFFIX"
CYCLE_NAME_UPDATED="FY 2026-27 Test Cycle $SUFFIX (Updated)"

echo "========================================="
echo "1. AUTHENTICATION & LOGIN"
echo "========================================="

# 1. Login as Employee
echo "Logging in as Employee (employee@company.com)..."
LOGIN_EMP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "employee@company.com", "password": "employee@3"}')
EMPLOYEE_TOKEN=$(echo "$LOGIN_EMP" | jq -r '.data.tokens.accessToken')
if [ -z "$EMPLOYEE_TOKEN" ] || [ "$EMPLOYEE_TOKEN" == "null" ]; then
  echo "Employee login failed. Response: $LOGIN_EMP"
  exit 1
fi
echo "Employee logged in successfully."

# 2. Login as Admin (who acts as the Manager with employee.update permission)
echo "Logging in as Admin (admin@company.com)..."
LOGIN_ADM=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@company.com", "password": "admin@6"}')
ADMIN_TOKEN=$(echo "$LOGIN_ADM" | jq -r '.data.tokens.accessToken')
if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" == "null" ]; then
  echo "Admin login failed. Response: $LOGIN_ADM"
  exit 1
fi
echo "Admin logged in successfully."

# 3. Login as Super Admin
echo "Logging in as Super Admin (super_admin@company.com)..."
LOGIN_SADM=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "super_admin@company.com", "password": "super_admin@1"}')
SUPER_ADMIN_TOKEN=$(echo "$LOGIN_SADM" | jq -r '.data.tokens.accessToken')
if [ -z "$SUPER_ADMIN_TOKEN" ] || [ "$SUPER_ADMIN_TOKEN" == "null" ]; then
  echo "Super Admin login failed. Response: $LOGIN_SADM"
  exit 1
fi
echo "Super Admin logged in successfully."

echo ""
echo "========================================="
echo "2. PLATFORM PERMISSION ADMINISTRATION"
echo "========================================="

# 2.1 List All System Permissions
echo "Listing system permissions..."
PERMS_LIST=$(curl -s -X GET "$BASE_URL/platform/permissions" \
  -H "Authorization: Bearer $SUPER_ADMIN_TOKEN")
echo "$PERMS_LIST" | jq . | head -n 20
echo "...(truncated list)..."

# 2.2 Create System Permission
echo "Creating a system permission: $PERM_NAME..."
CREATE_PERM_RES=$(curl -s -X POST "$BASE_URL/platform/permissions" \
  -H "Authorization: Bearer $SUPER_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"$PERM_NAME\", \"description\": \"Custom permission for performance testing\"}")
echo "$CREATE_PERM_RES" | jq .
PERM_ID=$(echo "$CREATE_PERM_RES" | jq -r '.data.permissionId')
if [ -z "$PERM_ID" ] || [ "$PERM_ID" == "null" ]; then
  echo "Permission creation failed."
  exit 1
fi
echo "Created permission ID: $PERM_ID"

# 2.3 Update System Permission
echo "Updating the custom permission..."
UPDATE_PERM_RES=$(curl -s -X PUT "$BASE_URL/platform/permissions/$PERM_ID" \
  -H "Authorization: Bearer $SUPER_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"${PERM_NAME}.updated\", \"description\": \"Updated custom permission for performance testing\"}")
echo "$UPDATE_PERM_RES" | jq .

# 2.4 Delete System Permission
echo "Deleting the custom permission..."
DELETE_PERM_RES=$(curl -s -X DELETE "$BASE_URL/platform/permissions/$PERM_ID" \
  -H "Authorization: Bearer $SUPER_ADMIN_TOKEN")
echo "$DELETE_PERM_RES" | jq .

echo ""
echo "========================================="
echo "3. PERFORMANCE CYCLES"
echo "========================================="

# 3.1 Get Appraisal Cycles (from AppraisalController)
echo "Getting Appraisal Cycles..."
APP_CYCLES_RES=$(curl -s -X GET "$BASE_URL/performance/appraisal-cycles" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN")
echo "$APP_CYCLES_RES" | jq .

# 3.2 Create Performance Cycle
echo "Creating a Performance Review Cycle: $CYCLE_NAME..."
CREATE_CYCLE_RES=$(curl -s -X POST "$BASE_URL/performance/reviews/cycles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"$CYCLE_NAME\", \"startDate\": \"2026-04-01\", \"endDate\": \"2027-03-31\", \"status\": \"ACTIVE\"}")
echo "$CREATE_CYCLE_RES" | jq .
CYCLE_ID=$(echo "$CREATE_CYCLE_RES" | jq -r '.data.id')
if [ -z "$CYCLE_ID" ] || [ "$CYCLE_ID" == "null" ]; then
  echo "Performance cycle creation failed."
  exit 1
fi
echo "Created Performance Cycle ID: $CYCLE_ID"

# 3.3 Get Performance Review Cycles
echo "Getting Performance Review Cycles..."
GET_CYCLES_RES=$(curl -s -X GET "$BASE_URL/performance/reviews/cycles" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN")
echo "$GET_CYCLES_RES" | jq .

# 3.4 Update Performance Cycle
echo "Updating Performance Review Cycle..."
UPDATE_CYCLE_RES=$(curl -s -X PUT "$BASE_URL/performance/reviews/cycles/$CYCLE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"$CYCLE_NAME_UPDATED\", \"startDate\": \"2026-04-01\", \"endDate\": \"2027-03-31\", \"status\": \"ACTIVE\"}")
echo "$UPDATE_CYCLE_RES" | jq .

echo ""
echo "========================================="
echo "4. PERFORMANCE GOALS"
echo "========================================="

# 4.1 Create Goal (via Performance Reviews endpoint)
echo "Creating Goal..."
CREATE_GOAL_RES=$(curl -s -X POST "$BASE_URL/performance/reviews/goals" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"employeeId\": 6, \"cycleId\": $CYCLE_ID, \"title\": \"Develop robust API tests\", \"description\": \"Write scripts to test endpoints\", \"dueDate\": \"2026-09-30\"}")
echo "$CREATE_GOAL_RES" | jq .
GOAL_ID=$(echo "$CREATE_GOAL_RES" | jq -r '.data.id')
if [ -z "$GOAL_ID" ] || [ "$GOAL_ID" == "null" ]; then
  echo "Goal creation failed."
  exit 1
fi
echo "Created Goal ID: $GOAL_ID"

# 4.2 Get Goals
echo "Getting Goals..."
GET_GOALS_RES=$(curl -s -X GET "$BASE_URL/performance/reviews/goals" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN")
echo "$GET_GOALS_RES" | jq .

# 4.3 Update Goal Progress
echo "Updating Goal Progress..."
UPDATE_GOAL_PROGRESS_RES=$(curl -s -X PATCH "$BASE_URL/performance/reviews/goals/$GOAL_ID/progress" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"progressPercent": 65}')
echo "$UPDATE_GOAL_PROGRESS_RES" | jq .

# 4.4 Create a Temp Goal and Delete it
echo "Creating temp goal for deletion..."
TEMP_GOAL_RES=$(curl -s -X POST "$BASE_URL/performance/reviews/goals" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"employeeId\": 6, \"cycleId\": $CYCLE_ID, \"title\": \"Temp Goal to Delete\", \"description\": \"Will be deleted\", \"dueDate\": \"2026-07-30\"}")
TEMP_GOAL_ID=$(echo "$TEMP_GOAL_RES" | jq -r '.data.id')
echo "Deleting temp goal ID: $TEMP_GOAL_ID..."
DELETE_GOAL_RES=$(curl -s -X DELETE "$BASE_URL/performance/reviews/goals/$TEMP_GOAL_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo "$DELETE_GOAL_RES" | jq .

echo ""
echo "========================================="
echo "5. SELF REVIEW & ASSESSMENT SUBMISSION"
echo "========================================="

# 5.1 Submit Self Review (Manager/Employee endpoint to initialize review/cycle relationship)
echo "Submitting Self Review..."
SELF_REV_BODY="{\"employeeId\": 6, \"cycleId\": $CYCLE_ID, \"achievements\": \"Implemented test scripts for all performance controllers.\", \"areasForImprovement\": \"Need to learn database seed structures.\", \"comments\": \"Proud of this quarter's output.\", \"rating\": 4}"
SUBMIT_SELF_REV_RES=$(curl -s -X POST "$BASE_URL/performance/reviews/self-reviews" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$SELF_REV_BODY")
echo "$SUBMIT_SELF_REV_RES" | jq .
REVIEW_ID=$(echo "$SUBMIT_SELF_REV_RES" | jq -r '.data.id')
if [ -z "$REVIEW_ID" ] || [ "$REVIEW_ID" == "null" ]; then
  echo "Self review submission failed."
  exit 1
fi
echo "Created/Initialized Review ID: $REVIEW_ID"

# Fetch review cycles for the employee to get the correct Appraisal ID
echo "Fetching active employee appraisal cycles..."
CYCLES_RES=$(curl -s -X GET "$BASE_URL/my-performance/reviews" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN")
echo "$CYCLES_RES" | jq .
APPRAISAL_REVIEW_ID=$(echo "$CYCLES_RES" | jq -r '.data.cycles[0].reviewId')
echo "Appraisal Review ID: $APPRAISAL_REVIEW_ID"

# 5.2 Submit Self Assessment (Employee Self-Service)
echo "Submitting Self Assessment for Appraisal Review ID: $APPRAISAL_REVIEW_ID..."
SELF_ASSESSMENT_BODY='{
  "selfRating": 4.5,
  "selfReview": "Solid quarters of development and test automation.",
  "achievements": ["Created scripts", "Validated database roles"],
  "strengths": ["Java", "API Testing", "Postgresql"],
  "improvementAreas": ["Kubernetes"],
  "leadershipOwnershipRating": 4.0,
  "technicalExcellenceRating": 4.5,
  "deliveryManagementRating": 4.0,
  "communicationInfluenceRating": 4.0,
  "teamMentorshipRating": 4.0,
  "innovationInitiativeRating": 4.5
}'
SELF_ASSESS_RES=$(curl -s -X POST "$BASE_URL/my-performance/reviews/$APPRAISAL_REVIEW_ID/self-assessment" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$SELF_ASSESSMENT_BODY")
echo "$SELF_ASSESS_RES" | jq .

echo ""
echo "========================================="
echo "6. EMPLOYEE SELF SERVICE - PERFORMANCE ENDPOINTS"
echo "========================================="

# 6.1 Get Competencies
echo "GET Get Competencies..."
curl -s -X GET "$BASE_URL/my-performance/competencies" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" | jq .

# 6.2 Get My Performance Dashboard
echo "GET Get My Performance Dashboard..."
curl -s -X GET "$BASE_URL/my-performance/dashboard" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" | jq .

# 6.3 Get Feedback
echo "GET Get Feedback..."
curl -s -X GET "$BASE_URL/my-performance/feedback" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" | jq .

# 6.4 Get Appraisal History
echo "GET Get Appraisal History..."
curl -s -X GET "$BASE_URL/my-performance/history" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" | jq .

# 6.5 Get Performance Policies
echo "GET Get Performance Policies..."
curl -s -X GET "$BASE_URL/my-performance/policies" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" | jq .

# 6.6 Get Review Cycles
echo "GET Get Review Cycles..."
curl -s -X GET "$BASE_URL/my-performance/reviews" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" | jq .

# 6.7 Get Performance Timeline
echo "GET Get Performance Timeline..."
curl -s -X GET "$BASE_URL/my-performance/timeline" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" | jq .

echo ""
echo "========================================="
echo "7. MANAGER PERFORMANCE MANAGEMENT"
echo "========================================="

# 7.1 Get Employee Performance Detail (via Manager)
# NOTE: The default query param cycle matches our created cycle name
echo "GET Get Employee Performance Detail..."
DETAIL_RES=$(curl -s -X GET "$BASE_URL/performance/6/review?cycle=$(echo "$CYCLE_NAME_UPDATED" | sed 's/ /%20/g')" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo "$DETAIL_RES" | jq .

# Query the database to retrieve the dynamically generated manager performance review ID
echo "Retrieving dynamic manager performance review ID from Postgres..."
MGR_PERF_REVIEW_ID=$(PGPASSWORD=12345 psql -h 127.0.0.1 -U ems_user -d employee_db -t -A -c "SELECT id FROM manager_performance_reviews WHERE employee_id = 6 AND review_cycle = '$CYCLE_NAME_UPDATED' LIMIT 1;")
echo "Manager Performance Review ID: $MGR_PERF_REVIEW_ID"
if [ -z "$MGR_PERF_REVIEW_ID" ] || [ "$MGR_PERF_REVIEW_ID" == "null" ]; then
  echo "Manager performance review not found in DB."
  exit 1
fi

# 7.2 Save Manager Rating
echo "POST Save Manager Rating..."
MGR_RATING_BODY='{
  "managerComment": "Demonstrates excellent technical ability and tests APIs thoroughly.",
  "recommendation": "PROMOTION",
  "competencyRatings": [
    {
      "competency": "Technical Skills",
      "score": 5,
      "comment": "Outstanding scripting and automation skills."
    },
    {
      "competency": "Communication",
      "score": 4,
      "comment": "Good logs and reporting format."
    }
  ]
}'
curl -s -X POST "$BASE_URL/performance/$MGR_PERF_REVIEW_ID/manager-rating" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$MGR_RATING_BODY" | jq .

# 7.3 Submit Final Review
echo "POST Submit Final Review..."
curl -s -X POST "$BASE_URL/performance/$MGR_PERF_REVIEW_ID/submit" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# 7.4 Get Team Performance Dashboard
echo "GET Get Team Performance Dashboard..."
curl -s -X GET "$BASE_URL/performance/team?cycle=$(echo "$CYCLE_NAME_UPDATED" | sed 's/ /%20/g')" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# 7.5 Team Summary (Top Cards Data)
echo "GET Team Summary (Top Cards Data)..."
curl -s -X GET "$BASE_URL/performance/team/summary?cycle=$(echo "$CYCLE_NAME_UPDATED" | sed 's/ /%20/g')" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

echo ""
echo "========================================="
echo "8. PERFORMANCE REVIEWS ENDPOINTS"
echo "========================================="

# 8.1 Submit Manager Review (via reviews/{id}/manager-review)
echo "Creating performance cycle for Manager Review..."
MGR_REV_BODY="{\"employeeId\": 9, \"reviewerId\": 2, \"cycleId\": $CYCLE_ID, \"achievements\": \"Assisted with backend testing.\", \"areasForImprovement\": \"None\", \"comments\": \"Highly reliable.\", \"rating\": 4}"
MGR_REV_RES=$(curl -s -X POST "$BASE_URL/performance/reviews/9/manager-review" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$MGR_REV_BODY")
echo "$MGR_REV_RES" | jq .
MGR_REVIEW_ID=$(echo "$MGR_REV_RES" | jq -r '.data.id')
echo "Initialized Manager Review ID: $MGR_REVIEW_ID"

# 8.2 Get Dashboard Stats
echo "GET Get Dashboard Stats..."
curl -s -X GET "$BASE_URL/performance/reviews/dashboard" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# 8.3 Get Feedbacks
echo "GET Get Feedbacks..."
curl -s -X GET "$BASE_URL/performance/reviews/feedbacks" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# 8.4 Send Performance Notification
echo "POST Send Performance Notification..."
curl -s -X POST "$BASE_URL/performance/reviews/notifications" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message": "Urgent reminder to complete self-appraisal cycles."}' | jq .

# 8.5 Create PIP
echo "POST Create PIP..."
PIP_BODY="{\"employeeId\": 6, \"improvementPlan\": \"Improve test automation coverage by 15%\", \"startDate\": \"2026-07-06\", \"endDate\": \"2026-10-06\"}"
curl -s -X POST "$BASE_URL/performance/reviews/pips" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$PIP_BODY" | jq .

# 8.6 Get Performance Report
echo "GET Get Performance Report..."
curl -s -X GET "$BASE_URL/performance/reviews/reports/annual" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# 8.7 Finalize Review
echo "POST Finalize Review..."
curl -s -X POST "$BASE_URL/performance/reviews/reviews/$MGR_REVIEW_ID/finalize" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

echo ""
echo "=== ALL API TESTS EXECUTED SUCCESSFULLY ==="
