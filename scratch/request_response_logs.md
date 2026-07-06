# Verified API Request & Response Payloads

This log documents the exact request bodies and corresponding response payloads captured during the execution of [test_performance_and_permissions.sh](file:///home/subashini/Documents/ems-backend/scratch/test_performance_and_permissions.sh).

---

## 🔐 1. Authentication & Session Login

### Login as Employee
* **Request URL**: `POST /api/v1/auth/login`
* **Request Payload**:
  ```json
  {"email": "employee@company.com", "password": "employee@3"}
  ```
* **Response Payload (truncated)**:
  ```json
  {
    "success": true,
    "message": "Login successful",
    "data": {
      "user": { "id": 6, "email": "employee@company.com" },
      "tokens": { "accessToken": "eyJhbGciOi..." }
    }
  }
  ```

---

## 🛠️ 2. Platform Permission Administration

### Create System Permission
* **Request URL**: `POST /api/v1/platform/permissions`
* **Request Payload**:
  ```json
  {
    "name": "test.custom.perf.permission.29377",
    "description": "Custom permission for performance testing"
  }
  ```
* **Response Payload**:
  ```json
  {
    "data": {
      "permissionId": 283,
      "name": "test.custom.perf.permission.29377",
      "description": "Custom permission for performance testing"
    },
    "links": {},
    "message": "System permission created successfully",
    "metadata": { "executionTimeMs": 13, "version": "v1" },
    "requestId": "REQ-6F384F96",
    "success": true,
    "timestamp": "2026-07-06T03:56:52Z"
  }
  ```

### Update System Permission
* **Request URL**: `PUT /api/v1/platform/permissions/283`
* **Request Payload**:
  ```json
  {
    "name": "test.custom.perf.permission.29377.updated",
    "description": "Updated custom permission for performance testing"
  }
  ```
* **Response**:
  ```json
  {
    "data": {
      "permissionId": 283,
      "name": "test.custom.perf.permission.29377.updated",
      "description": "Updated custom permission for performance testing"
    },
    "message": "System permission updated successfully",
    "success": true
  }
  ```

---

## 📈 3. Performance Cycles

### Create Performance Review Cycle
* **Request URL**: `POST /api/v1/performance/reviews/cycles`
* **Request Payload**:
  ```json
  {
    "name": "FY 2026-27 Test Cycle 29377",
    "startDate": "2026-04-01",
    "endDate": "2027-03-31",
    "status": "ACTIVE"
  }
  ```
* **Response Payload**:
  ```json
  {
    "data": {
      "id": 3,
      "name": "FY 2026-27 Test Cycle 29377",
      "startDate": "2026-04-01",
      "endDate": "2027-03-31",
      "status": "ACTIVE",
      "durationDays": 364
    },
    "message": "Performance cycle created successfully",
    "success": true
  }
  ```

---

## 🎯 4. Performance Goals

### Create Goal
* **Request URL**: `POST /api/v1/performance/reviews/goals`
* **Request Payload**:
  ```json
  {
    "employeeId": 6,
    "cycleId": 3,
    "title": "Develop robust API tests",
    "description": "Write scripts to test endpoints",
    "dueDate": "2026-09-30"
  }
  ```
* **Response Payload**:
  ```json
  {
    "data": {
      "id": 5,
      "employeeId": 6,
      "employeeName": "Employee User",
      "cycleId": 3,
      "cycleName": "FY 2026-27 Test Cycle 29377 (Updated)",
      "title": "Develop robust API tests",
      "status": "IN_PROGRESS",
      "progressPercent": 0
    },
    "message": "Performance goal created successfully",
    "success": true
  }
  ```

---

## 📝 5. Employee Self-Service Review & Assessments

### Submit Self Review
* **Request URL**: `POST /api/v1/performance/reviews/self-reviews`
* **Request Payload**:
  ```json
  {
    "employeeId": 6,
    "cycleId": 3,
    "achievements": "Implemented test scripts for all performance controllers.",
    "areasForImprovement": "Need to learn database seed structures.",
    "comments": "Proud of this quarter's output.",
    "rating": 4
  }
  ```
* **Response Payload**:
  ```json
  {
    "data": {
      "id": 7,
      "employeeId": 6,
      "employeeName": "Employee User",
      "cycleId": 4,
      "cycleName": "FY 2026-27 Test Cycle 10015 (Updated)",
      "rating": 4,
      "status": "SUBMITTED"
    },
    "message": "Self-review submitted successfully",
    "success": true
  }
  ```

### Submit Self Assessment
* **Request URL**: `POST /api/v1/my-performance/reviews/287/self-assessment`
* **Request Payload**:
  ```json
  {
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
  }
  ```
* **Response Payload**:
  ```json
  {
    "data": {
      "message": "Self-assessment submitted successfully",
      "status": "SUBMITTED",
      "submittedAt": "2026-07-06T09:28:53.616770236",
      "summary": [
        "Rating: 4.5",
        "Achievements: 2"
      ]
    },
    "message": "Self-assessment submitted",
    "success": true
  }
  ```

---

## 💼 6. Employee Self-Service - Performance Dashboard

### Get Dashboard Info
* **Request URL**: `GET /api/v1/my-performance/dashboard`
* **Response Payload**:
  ```json
  {
    "data": {
      "employeeId": "EMP006",
      "employeeName": "Employee User",
      "designation": "EMPLOYEE",
      "department": "Employee",
      "activeGoals": 10,
      "completedGoals": 8,
      "goalCompletionPercentage": 80.0,
      "overallRating": 4.6,
      "reviewStatus": "Open",
      "myBand": "A+"
    },
    "message": "Performance dashboard retrieved",
    "success": true
  }
  ```

---

## 👔 7. Manager Performance Management

### Save Manager Rating
* **Request URL**: `POST /api/v1/performance/17/manager-rating`
* **Request Payload**:
  ```json
  {
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
  }
  ```
* **Response Payload**:
  ```json
  {
    "data": {
      "reviewId": 17,
      "managerRating": 4.5,
      "finalScore": 4.0,
      "status": "IN_PROGRESS",
      "updatedAt": "2026-07-06T09:26:53.104968559"
    },
    "message": "Manager ratings saved successfully",
    "success": true
  }
  ```

---

## 📋 8. Performance Review Cycles & PIP

### Trigger Performance Improvement Plan (PIP)
* **Request URL**: `POST /api/v1/performance/reviews/pips`
* **Request Payload**:
  ```json
  {
    "employeeId": 6,
    "improvementPlan": "Improve test automation coverage by 15%",
    "startDate": "2026-07-06",
    "endDate": "2026-10-06"
  }
  ```
* **Response Payload**:
  ```json
  {
    "data": {
      "id": 4,
      "employeeId": 6,
      "employeeName": "Employee User",
      "improvementPlan": "Improve test automation coverage by 15%",
      "startDate": "2026-07-06",
      "endDate": "2026-10-06",
      "durationDays": 92,
      "daysRemaining": 92,
      "status": "ACTIVE"
    },
    "message": "Performance Improvement Plan (PIP) created successfully",
    "success": true
  }
  ```
