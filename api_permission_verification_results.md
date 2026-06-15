# Permission-Based API Verification Results
**Passed**: 37 / 37

| Permission Name | Endpoint & Method | Status Code | Result |
| --- | --- | --- | --- |
| profile.read / employee.profile.read | `GET /employees/me/profile` | 200 | ✅ PASSED |
| profile.update / employee.profile.update | `PUT /employees/me/profile` | 200 | ✅ PASSED |
| employee.dashboard.read | `GET /employees/me/dashboard` | 200 | ✅ PASSED |
| onboarding.self.read / employee.onboarding.read.self / employee.onboarding.read | `GET /employees/me/onboarding` | 200 | ✅ PASSED |
| onboarding.self.update / employee.onboarding.update | `PUT /employees/me/onboarding` | 200 | ✅ PASSED |
| onboarding.document.upload / employee.onboarding.document.upload | `POST /employees/me/onboarding/documents` | 201 | ✅ PASSED |
| onboarding.document.read.self / employee.onboarding.document.read | `GET /employees/me/onboarding/documents` | 200 | ✅ PASSED |
| employee.onboarding.submit / onboarding.self.submit | `POST /employees/me/onboarding/submit` | 200 | ✅ PASSED |
| employee.notification.read | `GET /employees/me/notifications` | 200 | ✅ PASSED |
| employee.notification.update | `PUT /employees/me/notifications/999/read` | 404 | ✅ PASSED |
| attendance.self.read / employee.attendance.read | `GET /employees/me/attendance` | 200 | ✅ PASSED |
| employee.attendance.create (punch-in) | `POST /employees/me/attendance/punch-in` | 200 | ✅ PASSED |
| employee.attendance.create (punch-out) | `POST /employees/me/attendance/punch-out` | 200 | ✅ PASSED |
| leave.create / employee.leave.create | `POST /employees/me/leaves` | 201 | ✅ PASSED |
| leave.self.read / employee.leave.read | `GET /employees/me/leaves` | 200 | ✅ PASSED |
| employee.leave.cancel | `PUT /employees/me/leaves/27/cancel` | 200 | ✅ PASSED |
| payslip.self.read / employee.payslip.read / payslip.read | `GET /employees/me/payslips` | 200 | ✅ PASSED |
| employee.payslip.download | `GET /employees/me/payslips/999/download` | 404 | ✅ PASSED |
| employee.document.upload | `POST /employees/me/documents` | 201 | ✅ PASSED |
| document.self.read / employee.document.read | `GET /employees/me/documents` | 200 | ✅ PASSED |
| employee.document.delete | `DELETE /employees/me/documents/7` | 200 | ✅ PASSED |
| employee.asset.read / asset.self.read | `GET /employees/me/assets` | 200 | ✅ PASSED |
| employee.asset.request | `POST /employees/me/assets/999/request` | 404 | ✅ PASSED |
| employee.expense.create | `POST /employees/me/expenses` | 201 | ✅ PASSED |
| expense.self.read / employee.expense.read | `GET /employees/me/expenses` | 200 | ✅ PASSED |
| employee.expense.update | `PUT /employees/me/expenses/13` | 200 | ✅ PASSED |
| employee.performance.read / performance.self.read | `GET /employees/me/performance` | 200 | ✅ PASSED |
| employee.performance.self-review.submit | `POST /employees/me/performance/999/self-review` | 404 | ✅ PASSED |
| employee.training.read | `GET /employees/me/trainings` | 200 | ✅ PASSED |
| employee.training.complete | `POST /employees/me/trainings/999/complete` | 404 | ✅ PASSED |
| employee.support-ticket.create | `POST /employees/me/support-tickets` | 201 | ✅ PASSED |
| employee.support-ticket.read | `GET /employees/me/support-tickets` | 200 | ✅ PASSED |
| employee.support-ticket.update | `PUT /employees/me/support-tickets/11` | 200 | ✅ PASSED |
| employee.goal.read / goal.self.read | `GET /employees/me/goals` | 200 | ✅ PASSED |
| employee.goal.update | `PUT /employees/me/goals/999` | 404 | ✅ PASSED |
| employee.announcement.read | `GET /announcements` | 200 | ✅ PASSED |
| employee.schedule.read | `GET /employees/me/schedule` | 200 | ✅ PASSED |


## Detailed Responses
### profile.read / employee.profile.read (GET /employees/me/profile)
**Status**: 200

**Response Payload**:
```json
{
  "address": "123 Corporate Way",
  "annualSalary": 85000.0,
  "department": "Employee",
  "designation": "EMPLOYEE",
  "dob": "1990-01-01",
  "email": "employee@company.com",
  "emergencyContact": "9876543210",
  "employeeId": "EMP010",
  "employmentType": "FULL_TIME",
  "fullName": "Employee User",
  "gender": "MALE",
  "id": 28,
  "joiningDate": "2026-06-10",
  "location": "Headquarters",
  "manager": null,
  "phone": "5550009",
  "status": "ACTIVE"
}
```

---

### profile.update / employee.profile.update (PUT /employees/me/profile)
**Status**: 200

**Response Payload**:
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "timestamp": "2026-06-15T05:38:26Z",
  "data": {
    "address": "123 Corporate Way",
    "annualSalary": 85000.0,
    "department": "Employee",
    "designation": "EMPLOYEE",
    "dob": "1990-01-01",
    "email": "employee@company.com",
    "emergencyContact": "9876543210",
    "employeeId": "EMP010",
    "employmentType": "FULL_TIME",
    "fullName": "Employee User",
    "gender": "MALE",
    "id": 28,
    "joiningDate": "2026-06-10",
    "location": "Headquarters",
    "manager": null,
    "phone": "5550009",
    "status": "ACTIVE"
  }
}
```

---

### employee.dashboard.read (GET /employees/me/dashboard)
**Status**: 200

**Response Payload**:
```json
{
  "performanceRating": 4.5,
  "leaveBalance": 120,
  "attendancePercentage": 96.4,
  "pendingActions": 15,
  "currentCTC": 85000.0
}
```

---

### onboarding.self.read / employee.onboarding.read.self / employee.onboarding.read (GET /employees/me/onboarding)
**Status**: 200

**Response Payload**:
```json
{
  "completedSteps": 0,
  "onboardingStatus": "UNDER_REVIEW",
  "totalSteps": 4,
  "fullName": "Employee User",
  "employeeId": "EMP010",
  "joiningDate": "2026-06-10",
  "department": "Employee"
}
```

---

### onboarding.self.update / employee.onboarding.update (PUT /employees/me/onboarding)
**Status**: 200

**Response Payload**:
```json
{
  "message": "Onboarding profile updated successfully"
}
```

---

### onboarding.document.upload / employee.onboarding.document.upload (POST /employees/me/onboarding/documents)
**Status**: 201

**Response Payload**:
```json
{
  "verificationStatus": "PENDING",
  "message": "Document uploaded successfully"
}
```

---

### onboarding.document.read.self / employee.onboarding.document.read (GET /employees/me/onboarding/documents)
**Status**: 200

**Response Payload**:
```json
{
  "documents": [
    {
      "documentType": "PASSPORT",
      "status": "PENDING"
    },
    {
      "documentType": "PASSPORT",
      "status": "PENDING"
    },
    {
      "documentType": "PASSPORT",
      "status": "PENDING"
    }
  ]
}
```

---

### employee.onboarding.submit / onboarding.self.submit (POST /employees/me/onboarding/submit)
**Status**: 200

**Response Payload**:
```json
{
  "message": "Onboarding submitted successfully",
  "status": "UNDER_REVIEW"
}
```

---

### employee.notification.read (GET /employees/me/notifications)
**Status**: 200

**Response Payload**:
```json
[]
```

---

### employee.notification.update (PUT /employees/me/notifications/999/read)
**Status**: 404

**Response Payload**:
```json
{
  "success": false,
  "message": "Notification not found",
  "errorCode": "NT_001",
  "timestamp": "2026-06-15T05:38:27Z"
}
```

---

### attendance.self.read / employee.attendance.read (GET /employees/me/attendance)
**Status**: 200

**Response Payload**:
```json
[]
```

---

### employee.attendance.create (punch-in) (POST /employees/me/attendance/punch-in)
**Status**: 200

**Response Payload**:
```json
{
  "success": true,
  "message": "Punched in successfully",
  "timestamp": "2026-06-15T05:38:27Z",
  "data": {
    "date": "2026-06-15",
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "id": 29,
    "notes": "Punch in",
    "punchInTime": "11:08:27.075680141",
    "punchOutTime": null,
    "status": "Late",
    "workingHours": null
  }
}
```

---

### employee.attendance.create (punch-out) (POST /employees/me/attendance/punch-out)
**Status**: 200

**Response Payload**:
```json
{
  "success": true,
  "message": "Punched out successfully",
  "timestamp": "2026-06-15T05:38:27Z",
  "data": {
    "date": "2026-06-15",
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "id": 29,
    "notes": "Punch out",
    "punchInTime": "11:08:27",
    "punchOutTime": "11:08:27.104986286",
    "status": "Late",
    "workingHours": "0h 00m"
  }
}
```

---

### leave.create / employee.leave.create (POST /employees/me/leaves)
**Status**: 201

**Response Payload**:
```json
{
  "success": true,
  "message": "Leave request submitted successfully",
  "timestamp": "2026-06-15T05:38:27Z",
  "data": {
    "id": 27,
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "leaveType": {
      "id": 1,
      "name": "Annual Leave",
      "description": "Paid time off for holidays/vacation",
      "defaultDays": 20,
      "active": true
    },
    "startDate": "2026-07-10",
    "endDate": "2026-07-12",
    "reason": "Verification test 1781501906",
    "status": "PENDING",
    "approvedBy": null,
    "appliedAt": "2026-06-15T11:08:27.145798365",
    "updatedAt": "2026-06-15T11:08:27.145802342"
  }
}
```

---

### leave.self.read / employee.leave.read (GET /employees/me/leaves)
**Status**: 200

**Response Payload**:
```json
{
  "success": true,
  "message": "Leave history retrieved successfully",
  "timestamp": "2026-06-15T05:38:27Z",
  "data": [
    {
      "id": 21,
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "department": "Employee",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "employee@company.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP010",
        "employmentType": "FULL_TIME",
        "fullName": "Employee User",
        "gender": "MALE",
        "id": 28,
        "joiningDate": "2026-06-10",
        "location": "Headquarters",
        "manager": null,
        "phone": "5550009",
        "status": "ACTIVE"
      },
      "leaveType": {
        "id": 1,
        "name": "Annual Leave",
        "description": "Paid time off for holidays/vacation",
        "defaultDays": 20,
        "active": true
      },
      "startDate": "2026-06-20",
      "endDate": "2026-06-22",
      "reason": "Recovering from flu - test 1781496545",
      "status": "PENDING",
      "approvedBy": null,
      "appliedAt": "2026-06-15T09:39:06.263827",
      "updatedAt": "2026-06-15T09:39:06.263829"
    },
    {
      "id": 22,
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "department": "Employee",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "employee@company.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP010",
        "employmentType": "FULL_TIME",
        "fullName": "Employee User",
        "gender": "MALE",
        "id": 28,
        "joiningDate": "2026-06-10",
        "location": "Headquarters",
        "manager": null,
        "phone": "5550009",
        "status": "ACTIVE"
      },
      "leaveType": {
        "id": 1,
        "name": "Annual Leave",
        "description": "Paid time off for holidays/vacation",
        "defaultDays": 20,
        "active": true
      },
      "startDate": "2026-06-20",
      "endDate": "2026-06-22",
      "reason": "Recovering from flu - test 1781496607",
      "status": "PENDING",
      "approvedBy": null,
      "appliedAt": "2026-06-15T09:40:08.157415",
      "updatedAt": "2026-06-15T09:40:08.157416"
    },
    {
      "id": 23,
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "department": "Employee",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "employee@company.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP010",
        "employmentType": "FULL_TIME",
        "fullName": "Employee User",
        "gender": "MALE",
        "id": 28,
        "joiningDate": "2026-06-10",
        "location": "Headquarters",
        "manager": null,
        "phone": "5550009",
        "status": "ACTIVE"
      },
      "leaveType": {
        "id": 1,
        "name": "Annual Leave",
        "description": "Paid time off for holidays/vacation",
        "defaultDays": 20,
        "active": true
      },
      "startDate": "2026-06-20",
      "endDate": "2026-06-22",
      "reason": "Recovering from flu - test 1781497059",
      "status": "PENDING",
      "approvedBy": null,
      "appliedAt": "2026-06-15T09:47:40.277875",
      "updatedAt": "2026-06-15T09:47:40.277876"
    },
    {
      "id": 24,
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "department": "Employee",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "employee@company.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP010",
        "employmentType": "FULL_TIME",
        "fullName": "Employee User",
        "gender": "MALE",
        "id": 28,
        "joiningDate": "2026-06-10",
        "location": "Headquarters",
        "manager": null,
        "phone": "5550009",
        "status": "ACTIVE"
      },
      "leaveType": {
        "id": 1,
        "name": "Annual Leave",
        "description": "Paid time off for holidays/vacation",
        "defaultDays": 20,
        "active": true
      },
      "startDate": "2026-06-20",
      "endDate": "2026-06-22",
      "reason": "Recovering from flu - test 1781497296",
      "status": "PENDING",
      "approvedBy": null,
      "appliedAt": "2026-06-15T09:51:37.408256",
      "updatedAt": "2026-06-15T09:51:37.408257"
    },
    {
      "id": 25,
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "department": "Employee",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "employee@company.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP010",
        "employmentType": "FULL_TIME",
        "fullName": "Employee User",
        "gender": "MALE",
        "id": 28,
        "joiningDate": "2026-06-10",
        "location": "Headquarters",
        "manager": null,
        "phone": "5550009",
        "status": "ACTIVE"
      },
      "leaveType": {
        "id": 1,
        "name": "Annual Leave",
        "description": "Paid time off for holidays/vacation",
        "defaultDays": 20,
        "active": true
      },
      "startDate": "2026-07-10",
      "endDate": "2026-07-12",
      "reason": "Verification test 1781498128",
      "status": "CANCELLED",
      "approvedBy": null,
      "appliedAt": "2026-06-15T10:05:29.181763",
      "updatedAt": "2026-06-15T10:05:29.200316"
    },
    {
      "id": 26,
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "department": "Employee",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "employee@company.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP010",
        "employmentType": "FULL_TIME",
        "fullName": "Employee User",
        "gender": "MALE",
        "id": 28,
        "joiningDate": "2026-06-10",
        "location": "Headquarters",
        "manager": null,
        "phone": "5550009",
        "status": "ACTIVE"
      },
      "leaveType": {
        "id": 1,
        "name": "Annual Leave",
        "description": "Paid time off for holidays/vacation",
        "defaultDays": 20,
        "active": true
      },
      "startDate": "2026-07-10",
      "endDate": "2026-07-12",
      "reason": "Verification test 1781498728",
      "status": "CANCELLED",
      "approvedBy": null,
      "appliedAt": "2026-06-15T10:15:29.415517",
      "updatedAt": "2026-06-15T10:15:29.44314"
    },
    {
      "id": 27,
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "department": "Employee",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "employee@company.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP010",
        "employmentType": "FULL_TIME",
        "fullName": "Employee User",
        "gender": "MALE",
        "id": 28,
        "joiningDate": "2026-06-10",
        "location": "Headquarters",
        "manager": null,
        "phone": "5550009",
        "status": "ACTIVE"
      },
      "leaveType": {
        "id": 1,
        "name": "Annual Leave",
        "description": "Paid time off for holidays/vacation",
        "defaultDays": 20,
        "active": true
      },
      "startDate": "2026-07-10",
      "endDate": "2026-07-12",
      "reason": "Verification test 1781501906",
      "status": "PENDING",
      "approvedBy": null,
      "appliedAt": "2026-06-15T11:08:27.145798",
      "updatedAt": "2026-06-15T11:08:27.145802"
    }
  ]
}
```

---

### employee.leave.cancel (PUT /employees/me/leaves/27/cancel)
**Status**: 200

**Response Payload**:
```json
{
  "success": true,
  "message": "Leave request cancelled successfully",
  "timestamp": "2026-06-15T05:38:27Z",
  "data": {
    "id": 27,
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "leaveType": {
      "id": 1,
      "name": "Annual Leave",
      "description": "Paid time off for holidays/vacation",
      "defaultDays": 20,
      "active": true
    },
    "startDate": "2026-07-10",
    "endDate": "2026-07-12",
    "reason": "Verification test 1781501906",
    "status": "CANCELLED",
    "approvedBy": null,
    "appliedAt": "2026-06-15T11:08:27.145798",
    "updatedAt": "2026-06-15T11:08:27.200024492"
  }
}
```

---

### payslip.self.read / employee.payslip.read / payslip.read (GET /employees/me/payslips)
**Status**: 200

**Response Payload**:
```json
{
  "success": true,
  "message": "My payslips retrieved successfully",
  "timestamp": "2026-06-15T05:38:27Z",
  "data": []
}
```

---

### employee.payslip.download (GET /employees/me/payslips/999/download)
**Status**: 404

**Response Payload**:
```json
{
  "success": false,
  "message": "Payslip not found",
  "errorCode": "PS_001",
  "timestamp": "2026-06-15T05:38:27Z"
}
```

---

### employee.document.upload (POST /employees/me/documents)
**Status**: 201

**Response Payload**:
```json
{
  "success": true,
  "message": "Document uploaded successfully",
  "timestamp": "2026-06-15T05:38:27Z",
  "data": {
    "id": 7,
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "fileName": "my_resume.pdf",
    "fileType": "application/pdf",
    "fileSize": 41,
    "downloadUrl": "/api/v1/documents/download/1781501907256",
    "uploadedAt": "2026-06-15T11:08:27.256920367"
  }
}
```

---

### document.self.read / employee.document.read (GET /employees/me/documents)
**Status**: 200

**Response Payload**:
```json
[
  {
    "id": 7,
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "fileName": "my_resume.pdf",
    "fileType": "application/pdf",
    "fileSize": 41,
    "downloadUrl": "/api/v1/documents/download/1781501907256",
    "uploadedAt": "2026-06-15T11:08:27.25692"
  }
]
```

---

### employee.document.delete (DELETE /employees/me/documents/7)
**Status**: 200

**Response Payload**:
```json
{
  "success": true,
  "message": "Document deleted successfully",
  "timestamp": "2026-06-15T05:38:27Z",
  "data": null
}
```

---

### employee.asset.read / asset.self.read (GET /employees/me/assets)
**Status**: 200

**Response Payload**:
```json
[]
```

---

### employee.asset.request (POST /employees/me/assets/999/request)
**Status**: 404

**Response Payload**:
```json
{
  "success": false,
  "message": "Asset not found",
  "errorCode": "AST_001",
  "timestamp": "2026-06-15T05:38:27Z"
}
```

---

### employee.expense.create (POST /employees/me/expenses)
**Status**: 201

**Response Payload**:
```json
{
  "success": true,
  "message": "Expense submitted successfully",
  "timestamp": "2026-06-15T05:38:27Z",
  "data": {
    "id": 13,
    "title": "Travel Reimbursement 1781501906",
    "amount": 450,
    "expenseDate": "2026-06-15",
    "status": "PENDING",
    "description": "Client visit travel reimbursement",
    "rejectionReason": null,
    "category": {
      "id": 1,
      "name": "Travel",
      "description": "Business trips, flights, hotels, and mileage reimbursement"
    },
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "attachmentName": null,
    "attachmentType": null,
    "attachmentUrl": null,
    "attachmentData": null,
    "createdAt": "2026-06-15T11:08:27.37432194",
    "updatedAt": "2026-06-15T11:08:27.374325006"
  }
}
```

---

### expense.self.read / employee.expense.read (GET /employees/me/expenses)
**Status**: 200

**Response Payload**:
```json
[
  {
    "id": 6,
    "title": "Internet Allowance 1781496485",
    "amount": 1200.0,
    "expenseDate": "2026-06-15",
    "status": "PENDING",
    "description": "Monthly broadband reimbursement",
    "rejectionReason": null,
    "category": {
      "id": 9,
      "name": "General",
      "description": "Auto-created category from self-service"
    },
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "attachmentName": null,
    "attachmentType": null,
    "attachmentUrl": null,
    "attachmentData": null,
    "createdAt": "2026-06-15T09:38:06.374236",
    "updatedAt": "2026-06-15T09:38:06.374237"
  },
  {
    "id": 7,
    "title": "Internet Allowance 1781496545",
    "amount": 1200.0,
    "expenseDate": "2026-06-15",
    "status": "PENDING",
    "description": "Monthly broadband reimbursement",
    "rejectionReason": null,
    "category": {
      "id": 9,
      "name": "General",
      "description": "Auto-created category from self-service"
    },
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "attachmentName": null,
    "attachmentType": null,
    "attachmentUrl": null,
    "attachmentData": null,
    "createdAt": "2026-06-15T09:39:06.279477",
    "updatedAt": "2026-06-15T09:39:06.279477"
  },
  {
    "id": 8,
    "title": "Internet Allowance 1781496607",
    "amount": 1200.0,
    "expenseDate": "2026-06-15",
    "status": "PENDING",
    "description": "Monthly broadband reimbursement",
    "rejectionReason": null,
    "category": {
      "id": 9,
      "name": "General",
      "description": "Auto-created category from self-service"
    },
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "attachmentName": null,
    "attachmentType": null,
    "attachmentUrl": null,
    "attachmentData": null,
    "createdAt": "2026-06-15T09:40:08.172315",
    "updatedAt": "2026-06-15T09:40:08.172316"
  },
  {
    "id": 9,
    "title": "Internet Allowance 1781497059",
    "amount": 1200.0,
    "expenseDate": "2026-06-15",
    "status": "PENDING",
    "description": "Monthly broadband reimbursement",
    "rejectionReason": null,
    "category": {
      "id": 9,
      "name": "General",
      "description": "Auto-created category from self-service"
    },
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "attachmentName": null,
    "attachmentType": null,
    "attachmentUrl": null,
    "attachmentData": null,
    "createdAt": "2026-06-15T09:47:40.291625",
    "updatedAt": "2026-06-15T09:47:40.291626"
  },
  {
    "id": 10,
    "title": "Internet Allowance 1781497296",
    "amount": 1200.0,
    "expenseDate": "2026-06-15",
    "status": "PENDING",
    "description": "Monthly broadband reimbursement",
    "rejectionReason": null,
    "category": {
      "id": 9,
      "name": "General",
      "description": "Auto-created category from self-service"
    },
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "attachmentName": null,
    "attachmentType": null,
    "attachmentUrl": null,
    "attachmentData": null,
    "createdAt": "2026-06-15T09:51:37.438873",
    "updatedAt": "2026-06-15T09:51:37.438875"
  },
  {
    "id": 11,
    "title": "Updated Expense Title",
    "amount": 500.0,
    "expenseDate": "2026-06-15",
    "status": "PENDING",
    "description": "Client visit travel reimbursement",
    "rejectionReason": null,
    "category": {
      "id": 1,
      "name": "Travel",
      "description": "Business trips, flights, hotels, and mileage reimbursement"
    },
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "attachmentName": null,
    "attachmentType": null,
    "attachmentUrl": null,
    "attachmentData": null,
    "createdAt": "2026-06-15T10:05:29.269361",
    "updatedAt": "2026-06-15T10:05:29.289462"
  },
  {
    "id": 12,
    "title": "Updated Expense Title",
    "amount": 500.0,
    "expenseDate": "2026-06-15",
    "status": "PENDING",
    "description": "Client visit travel reimbursement",
    "rejectionReason": null,
    "category": {
      "id": 1,
      "name": "Travel",
      "description": "Business trips, flights, hotels, and mileage reimbursement"
    },
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "attachmentName": null,
    "attachmentType": null,
    "attachmentUrl": null,
    "attachmentData": null,
    "createdAt": "2026-06-15T10:15:29.53372",
    "updatedAt": "2026-06-15T10:15:29.566867"
  },
  {
    "id": 13,
    "title": "Travel Reimbursement 1781501906",
    "amount": 450.0,
    "expenseDate": "2026-06-15",
    "status": "PENDING",
    "description": "Client visit travel reimbursement",
    "rejectionReason": null,
    "category": {
      "id": 1,
      "name": "Travel",
      "description": "Business trips, flights, hotels, and mileage reimbursement"
    },
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "attachmentName": null,
    "attachmentType": null,
    "attachmentUrl": null,
    "attachmentData": null,
    "createdAt": "2026-06-15T11:08:27.374322",
    "updatedAt": "2026-06-15T11:08:27.374325"
  }
]
```

---

### employee.expense.update (PUT /employees/me/expenses/13)
**Status**: 200

**Response Payload**:
```json
{
  "success": true,
  "message": "Expense updated successfully",
  "timestamp": "2026-06-15T05:38:27Z",
  "data": {
    "id": 13,
    "title": "Updated Expense Title",
    "amount": 500,
    "expenseDate": "2026-06-15",
    "status": "PENDING",
    "description": "Client visit travel reimbursement",
    "rejectionReason": null,
    "category": {
      "id": 1,
      "name": "Travel",
      "description": "Business trips, flights, hotels, and mileage reimbursement"
    },
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP010",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 28,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550009",
      "status": "ACTIVE"
    },
    "attachmentName": null,
    "attachmentType": null,
    "attachmentUrl": null,
    "attachmentData": null,
    "createdAt": "2026-06-15T11:08:27.374322",
    "updatedAt": "2026-06-15T11:08:27.439768515"
  }
}
```

---

### employee.performance.read / performance.self.read (GET /employees/me/performance)
**Status**: 200

**Response Payload**:
```json
[]
```

---

### employee.performance.self-review.submit (POST /employees/me/performance/999/self-review)
**Status**: 404

**Response Payload**:
```json
{
  "success": false,
  "message": "Performance review not found",
  "errorCode": "PR_001",
  "timestamp": "2026-06-15T05:38:27Z"
}
```

---

### employee.training.read (GET /employees/me/trainings)
**Status**: 200

**Response Payload**:
```json
[]
```

---

### employee.training.complete (POST /employees/me/trainings/999/complete)
**Status**: 404

**Response Payload**:
```json
{
  "success": false,
  "message": "Training enrollment not found",
  "errorCode": "TR_001",
  "timestamp": "2026-06-15T05:38:27Z"
}
```

---

### employee.support-ticket.create (POST /employees/me/support-tickets)
**Status**: 201

**Response Payload**:
```json
{
  "success": true,
  "message": "Support ticket created successfully",
  "timestamp": "2026-06-15T05:38:27Z",
  "data": {
    "category": "IT Support",
    "createdAt": "2026-06-15T11:08:27.540032651",
    "description": "Unable to connect to production VPN server",
    "employeeId": "EMP010",
    "id": 11,
    "status": "OPEN",
    "title": "VPN Access Issue 1781501906",
    "updatedAt": "2026-06-15T11:08:27.540034455"
  }
}
```

---

### employee.support-ticket.read (GET /employees/me/support-tickets)
**Status**: 200

**Response Payload**:
```json
[
  {
    "category": "IT Support",
    "createdAt": "2026-06-15T09:38:06.394486",
    "description": "Some keys are not responding",
    "employeeId": "EMP010",
    "id": 4,
    "status": "OPEN",
    "title": "Broken keyboard 1781496485",
    "updatedAt": "2026-06-15T09:38:06.394489"
  },
  {
    "category": "IT Support",
    "createdAt": "2026-06-15T09:39:06.292183",
    "description": "Some keys are not responding",
    "employeeId": "EMP010",
    "id": 5,
    "status": "OPEN",
    "title": "Broken keyboard 1781496545",
    "updatedAt": "2026-06-15T09:39:06.292184"
  },
  {
    "category": "IT Support",
    "createdAt": "2026-06-15T09:40:08.186463",
    "description": "Some keys are not responding",
    "employeeId": "EMP010",
    "id": 6,
    "status": "OPEN",
    "title": "Broken keyboard 1781496607",
    "updatedAt": "2026-06-15T09:40:08.186465"
  },
  {
    "category": "IT Support",
    "createdAt": "2026-06-15T09:47:40.3025",
    "description": "Some keys are not responding",
    "employeeId": "EMP010",
    "id": 7,
    "status": "OPEN",
    "title": "Broken keyboard 1781497059",
    "updatedAt": "2026-06-15T09:47:40.302501"
  },
  {
    "category": "IT Support",
    "createdAt": "2026-06-15T09:51:37.455186",
    "description": "Some keys are not responding",
    "employeeId": "EMP010",
    "id": 8,
    "status": "OPEN",
    "title": "Broken keyboard 1781497296",
    "updatedAt": "2026-06-15T09:51:37.455187"
  },
  {
    "category": "IT Support",
    "createdAt": "2026-06-15T10:05:29.330208",
    "description": "Still unable to connect after network reset",
    "employeeId": "EMP010",
    "id": 9,
    "status": "OPEN",
    "title": "VPN Access Issue 1781498128",
    "updatedAt": "2026-06-15T10:05:29.346504"
  },
  {
    "category": "IT Support",
    "createdAt": "2026-06-15T10:15:29.620489",
    "description": "Still unable to connect after network reset",
    "employeeId": "EMP010",
    "id": 10,
    "status": "OPEN",
    "title": "VPN Access Issue 1781498728",
    "updatedAt": "2026-06-15T10:15:29.643987"
  },
  {
    "category": "IT Support",
    "createdAt": "2026-06-15T11:08:27.540033",
    "description": "Unable to connect to production VPN server",
    "employeeId": "EMP010",
    "id": 11,
    "status": "OPEN",
    "title": "VPN Access Issue 1781501906",
    "updatedAt": "2026-06-15T11:08:27.540034"
  }
]
```

---

### employee.support-ticket.update (PUT /employees/me/support-tickets/11)
**Status**: 200

**Response Payload**:
```json
{
  "success": true,
  "message": "Support ticket updated successfully",
  "timestamp": "2026-06-15T05:38:27Z",
  "data": {
    "category": "IT Support",
    "createdAt": "2026-06-15T11:08:27.540033",
    "description": "Still unable to connect after network reset",
    "employeeId": "EMP010",
    "id": 11,
    "status": "OPEN",
    "title": "VPN Access Issue 1781501906",
    "updatedAt": "2026-06-15T11:08:27.589009394"
  }
}
```

---

### employee.goal.read / goal.self.read (GET /employees/me/goals)
**Status**: 200

**Response Payload**:
```json
[]
```

---

### employee.goal.update (PUT /employees/me/goals/999)
**Status**: 404

**Response Payload**:
```json
{
  "success": false,
  "message": "Goal not found",
  "errorCode": "GL_001",
  "timestamp": "2026-06-15T05:38:27Z"
}
```

---

### employee.announcement.read (GET /announcements)
**Status**: 200

**Response Payload**:
```json
[
  {
    "active": true,
    "author": "Operations",
    "content": "System will undergo maintenance at 2 AM.",
    "id": 1,
    "publishedDate": "2026-06-13T15:06:21.155264",
    "title": "EMS Upgrade Schedule"
  }
]
```

---

### employee.schedule.read (GET /employees/me/schedule)
**Status**: 200

**Response Payload**:
```json
{
  "shiftName": "Standard General Shift",
  "timezone": "IST",
  "workHours": "09:00 AM - 06:00 PM",
  "weeklyOffs": [
    "Saturday",
    "Sunday"
  ],
  "effectiveDate": "2026-06-10"
}
```

---

