# Local API Verification Run Results
**Passed**: 42 / 42

| Endpoint Name | User Role | Method | URL | Status | Result |
| --- | --- | --- | --- | --- | --- |
| Health | Anonymous | GET | `http://localhost:8080/api/v1/health` | 200 | ✅ PASSED |
| Version | Anonymous | GET | `http://localhost:8080/api/v1/version` | 200 | ✅ PASSED |
| Metrics | Anonymous | GET | `http://localhost:8080/api/v1/metrics` | 200 | ✅ PASSED |
| Super Admin Login | Super Admin | POST | `http://localhost:8080/api/v1/auth/login` | 200 | ✅ PASSED |
| Employee Login | Employee | POST | `http://localhost:8080/api/v1/auth/login` | 200 | ✅ PASSED |
| List Users | Super Admin | GET | `http://localhost:8080/api/v1/users` | 200 | ✅ PASSED |
| List Roles | Super Admin | GET | `http://localhost:8080/api/v1/roles` | 200 | ✅ PASSED |
| List Permissions | Super Admin | GET | `http://localhost:8080/api/v1/permissions` | 200 | ✅ PASSED |
| List Departments | Super Admin | GET | `http://localhost:8080/api/v1/departments` | 200 | ✅ PASSED |
| List Employees | Super Admin | GET | `http://localhost:8080/api/v1/employees` | 200 | ✅ PASSED |
| List Attendance | Super Admin | GET | `http://localhost:8080/api/v1/attendance` | 200 | ✅ PASSED |
| List Leave Types | Super Admin | GET | `http://localhost:8080/api/v1/leave-types` | 200 | ✅ PASSED |
| List Recruitment Jobs | Super Admin | GET | `http://localhost:8080/api/v1/recruitments/jobs` | 200 | ✅ PASSED |
| List Training Courses | Super Admin | GET | `http://localhost:8080/api/v1/trainings/courses` | 200 | ✅ PASSED |
| List Onboarding Records | Super Admin | GET | `http://localhost:8080/api/v1/onboarding-records` | 200 | ✅ PASSED |
| Onboarding Dashboard | Super Admin | GET | `http://localhost:8080/api/v1/onboarding-records/dashboard` | 200 | ✅ PASSED |
| List Appraisals | Super Admin | GET | `http://localhost:8080/api/v1/appraisals` | 200 | ✅ PASSED |
| Appraisals Dashboard | Super Admin | GET | `http://localhost:8080/api/v1/appraisals/dashboard` | 200 | ✅ PASSED |
| List Appraisal Cycles | Super Admin | GET | `http://localhost:8080/api/v1/appraisal-cycles` | 200 | ✅ PASSED |
| List Increment Policies | Super Admin | GET | `http://localhost:8080/api/v1/increment-policies` | 200 | ✅ PASSED |
| Create Employee | Super Admin | POST | `http://localhost:8080/api/v1/employees` | 201 | ✅ PASSED |
| ESS Dashboard (employee.dashboard.read) | Employee | GET | `http://localhost:8080/api/v1/employees/me/dashboard` | 200 | ✅ PASSED |
| ESS Profile (employee.profile.read) | Employee | GET | `http://localhost:8080/api/v1/employees/me/profile` | 200 | ✅ PASSED |
| ESS Attendance Records (attendance.self.read) | Employee | GET | `http://localhost:8080/api/v1/employees/me/attendance` | 200 | ✅ PASSED |
| ESS Leave History (leave.self.read) | Employee | GET | `http://localhost:8080/api/v1/employees/me/leaves` | 200 | ✅ PASSED |
| ESS Payslips (payslip.self.read) | Employee | GET | `http://localhost:8080/api/v1/employees/me/payslips` | 200 | ✅ PASSED |
| ESS Documents (document.self.read) | Employee | GET | `http://localhost:8080/api/v1/employees/me/documents` | 200 | ✅ PASSED |
| ESS Expenses (expense.self.read) | Employee | GET | `http://localhost:8080/api/v1/employees/me/expenses` | 200 | ✅ PASSED |
| ESS Performance (performance.self.read) | Employee | GET | `http://localhost:8080/api/v1/employees/me/performance` | 200 | ✅ PASSED |
| ESS Goals (goal.self.read) | Employee | GET | `http://localhost:8080/api/v1/employees/me/goals` | 200 | ✅ PASSED |
| ESS Assets (asset.self.read) | Employee | GET | `http://localhost:8080/api/v1/employees/me/assets` | 200 | ✅ PASSED |
| ESS Onboarding Details (onboarding.self.read) | Employee | GET | `http://localhost:8080/api/v1/employees/me/onboarding` | 200 | ✅ PASSED |
| ESS Apply Leave | Employee | POST | `http://localhost:8080/api/v1/employees/me/leaves` | 201 | ✅ PASSED |
| ESS Submit Expense | Employee | POST | `http://localhost:8080/api/v1/employees/me/expenses` | 201 | ✅ PASSED |
| ESS Create Support Ticket | Employee | POST | `http://localhost:8080/api/v1/employees/me/support-tickets` | 201 | ✅ PASSED |
| ESS Punch In | Employee | POST | `http://localhost:8080/api/v1/employees/me/attendance/punch-in` | 200 | ✅ PASSED |
| ESS Punch Out | Employee | POST | `http://localhost:8080/api/v1/employees/me/attendance/punch-out` | 200 | ✅ PASSED |
| Removed Employees attendance by ID | Employee | GET | `http://localhost:8080/api/v1/employees/2/attendance` | 404 | ✅ PASSED |
| Removed Employees documents by ID | Employee | GET | `http://localhost:8080/api/v1/employees/2/documents` | 404 | ✅ PASSED |
| Removed Leaves employee by ID | Employee | GET | `http://localhost:8080/api/v1/leaves/employee/2` | 404 | ✅ PASSED |
| Removed Payslips my | Employee | GET | `http://localhost:8080/api/v1/payslips/my` | 400 | ✅ PASSED |
| Removed Attendance my | Employee | GET | `http://localhost:8080/api/v1/attendance/my` | 400 | ✅ PASSED |


## Detailed Responses
### Health as Anonymous (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/health`

**Response Payload**:
```json
{
  "success": true,
  "message": "System is healthy",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": {
    "database": "UP",
    "redis": "UP",
    "status": "UP"
  }
}
```

---

### Version as Anonymous (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/version`

**Response Payload**:
```json
{
  "success": true,
  "message": "App version details retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": {
    "appName": "Employee Management System",
    "version": "1.0.0",
    "apiVersion": "v1",
    "environment": "production"
  }
}
```

---

### Metrics as Anonymous (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/metrics`

**Response Payload**:
```json
{
  "success": true,
  "message": "System metrics retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": {
    "memory": {
      "totalMemoryBytes": 276824064,
      "freeMemoryBytes": 156242768,
      "maxMemoryBytes": 4030726144,
      "usedMemoryBytes": 120581296,
      "usedMemoryPercentage": 43.55882008870442
    },
    "activeThreads": 23,
    "availableProcessors": 16,
    "timestamp": 1781501913263
  }
}
```

---

### Super Admin Login as Super Admin (POST - 200)
**Request URL**: `http://localhost:8080/api/v1/auth/login`

**Request Payload**:
```json
{
  "email": "emssuperadmin@gmail.com",
  "password": "Admin@123"
}
```

**Response Payload**:
```json
{
  "success": true,
  "message": "Login successful",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": {
    "tokens": {
      "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiU1VQRVJfQURNSU4iLCJzZXNzaW9uSWQiOiI5YzkzYzBiNy1lNGJlLTRkNTItODNmNC0xZWEzMWMxODlkYWMiLCJ1c2VySWQiOiJFTVAwMDUiLCJzdWIiOiJlbXNzdXBlcmFkbWluQGdtYWlsLmNvbSIsImlhdCI6MTc4MTUwMTkxMywiZXhwIjoxNzgxNTg4MzEzfQ.p2trvE9t-hwdQ-qS8ZRifCCv0BNq29u8Va5GQ8i7MSjLyWGAyh9Ho4kp7VVgEuiY8Y5iKwAqh7BM3JoqPh---Q",
      "refreshToken": "90d568b3-aa0b-4a3f-93d9-e1d27d90eca3",
      "tokenType": "Bearer",
      "accessTokenExpiresIn": 900,
      "refreshTokenExpiresIn": 604800
    },
    "user": {
      "id": 1,
      "employeeId": "EMP005",
      "name": "Super Admin",
      "email": "emssuperadmin@gmail.com",
      "role": "SUPER_ADMIN",
      "permissions": [
        "employee.announcement.read",
        "leave.self.read",
        "employee.onboarding.document.upload",
        "employee.document.delete",
        "employee.create",
        "employee.attendance.create",
        "user.delete",
        "salary.manage",
        "employee.team.read",
        "employee.document.read",
        "reports.manager",
        "profile.update",
        "employee.payslip.download",
        "attendance.manage",
        "attendance.self.read",
        "onboarding.document.read.self",
        "employee.onboarding.read.self",
        "employee.attendance.read",
        "reports.hr",
        "employee.expense.update",
        "employee.notification.update",
        "recruitment.manage",
        "employee.leave.read",
        "employee.onboarding.document.read",
        "user.read",
        "payslip.self.read",
        "onboarding.self.submit",
        "performance.review",
        "permission.manage",
        "employee.onboarding.update",
        "leave.create",
        "employee.expense.read",
        "leave.manage",
        "reports.view",
        "employee.support-ticket.create",
        "payroll.manage",
        "employee.support-ticket.read",
        "employee.onboarding.read",
        "employee.payslip.read",
        "expense.self.read",
        "attendance.read",
        "payroll.read",
        "employee.update",
        "employee.training.complete",
        "employee.performance.self-review.submit",
        "user.create",
        "user.update",
        "employee.read",
        "expense.manage",
        "profile.read",
        "onboarding.self.read",
        "employee.profile.update",
        "employee.asset.request",
        "employee.support-ticket.update",
        "document.self.read",
        "performance.self.read",
        "payslip.read",
        "task.assign",
        "employee.schedule.read",
        "employee.notification.read",
        "employee.goal.update",
        "role.manage",
        "employee.onboarding.submit",
        "employee.leave.create",
        "employee.goal.read",
        "employee.dashboard.read",
        "employee.training.read",
        "employee.leave.cancel",
        "leave.approve",
        "employee.profile.read",
        "system.manage",
        "reports.finance",
        "employee.expense.create",
        "goal.self.read",
        "leave.team.approve",
        "employee.performance.read",
        "leave.read",
        "asset.self.read",
        "attendance.team.read",
        "onboarding.document.upload",
        "user.manage",
        "employee.document.upload",
        "onboarding.self.update",
        "employee.delete",
        "employee.asset.read"
      ],
      "status": "ACTIVE",
      "lastLogin": "2026-06-15T05:38:33Z"
    }
  }
}
```

---

### Employee Login as Employee (POST - 200)
**Request URL**: `http://localhost:8080/api/v1/auth/login`

**Request Payload**:
```json
{
  "email": "employee@company.com",
  "password": "employee@2"
}
```

**Response Payload**:
```json
{
  "success": true,
  "message": "Login successful",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": {
    "tokens": {
      "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiRU1QTE9ZRUUiLCJzZXNzaW9uSWQiOiI4MmFhYzkzOS05YTY1LTQ4NGUtOWExNC02NzM1N2YxZDhiOTIiLCJ1c2VySWQiOiJFTVAwMTAiLCJzdWIiOiJlbXBsb3llZUBjb21wYW55LmNvbSIsImlhdCI6MTc4MTUwMTkxMywiZXhwIjoxNzgxNTg4MzEzfQ.cFRsVVVKft4hAm5vGwUpqUINrvV_cyXY-oqtpPgFMdzgqfwxXlQyUTM9WcX9Y4D3Knej6o-jEKx7CncR_K5dUw",
      "refreshToken": "05984c58-bbd6-4464-8a1b-16553263e68f",
      "tokenType": "Bearer",
      "accessTokenExpiresIn": 900,
      "refreshTokenExpiresIn": 604800
    },
    "user": {
      "id": 28,
      "employeeId": "EMP010",
      "name": "Employee User",
      "email": "employee@company.com",
      "role": "EMPLOYEE",
      "permissions": [
        "profile.update",
        "employee.notification.update",
        "employee.leave.read",
        "onboarding.self.update",
        "expense.self.read",
        "employee.profile.update",
        "employee.leave.cancel",
        "employee.onboarding.document.upload",
        "employee.document.upload",
        "employee.payslip.read",
        "employee.expense.read",
        "leave.create",
        "employee.onboarding.update",
        "employee.asset.read",
        "employee.training.read",
        "employee.document.delete",
        "employee.dashboard.read",
        "document.self.read",
        "employee.training.complete",
        "employee.support-ticket.update",
        "onboarding.self.submit",
        "employee.payslip.download",
        "employee.support-ticket.create",
        "employee.schedule.read",
        "goal.self.read",
        "payslip.read",
        "employee.expense.create",
        "employee.asset.request",
        "employee.onboarding.read",
        "employee.onboarding.read.self",
        "onboarding.self.read",
        "employee.leave.create",
        "employee.announcement.read",
        "employee.attendance.read",
        "employee.performance.self-review.submit",
        "attendance.self.read",
        "leave.self.read",
        "onboarding.document.read.self",
        "employee.document.read",
        "payslip.self.read",
        "employee.onboarding.document.read",
        "employee.goal.update",
        "asset.self.read",
        "employee.profile.read",
        "employee.notification.read",
        "performance.self.read",
        "employee.performance.read",
        "employee.attendance.create",
        "employee.onboarding.submit",
        "profile.read",
        "employee.expense.update",
        "onboarding.document.upload",
        "employee.goal.read",
        "employee.support-ticket.read"
      ],
      "status": "ACTIVE",
      "lastLogin": "2026-06-15T05:38:33Z"
    }
  }
}
```

---

### List Users as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/users`

**Response Payload**:
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": [
    {
      "id": 11,
      "userId": "EMP011",
      "fullName": "Manager User",
      "workEmail": "manager@company.com",
      "mobileNumber": "5550010",
      "employeeId": null,
      "department": "Manager",
      "requestedRole": "MANAGER",
      "role": {
        "roleId": 1,
        "name": "MANAGER",
        "description": "Role for MANAGER",
        "permissions": [
          {
            "permissionId": 34,
            "name": "performance.review",
            "description": "Permission for performance.review"
          },
          {
            "permissionId": 19,
            "name": "leave.team.approve",
            "description": "Permission for leave.team.approve"
          },
          {
            "permissionId": 38,
            "name": "employee.team.read",
            "description": "Permission for employee.team.read"
          },
          {
            "permissionId": 13,
            "name": "attendance.team.read",
            "description": "Permission for attendance.team.read"
          },
          {
            "permissionId": 33,
            "name": "task.assign",
            "description": "Permission for task.assign"
          }
        ]
      },
      "location": "Headquarters",
      "status": "ACTIVE"
    },
    {
      "id": 18,
      "userId": "EMP018",
      "fullName": "Rahul Verma",
      "workEmail": "rahul.verma@emscompany.com",
      "mobileNumber": "9988776655",
      "employeeId": "EMP2026003",
      "department": "Engineering",
      "requestedRole": "MANAGER",
      "role": {
        "roleId": 1,
        "name": "MANAGER",
        "description": "Role for MANAGER",
        "permissions": [
          {
            "permissionId": 34,
            "name": "performance.review",
            "description": "Permission for performance.review"
          },
          {
            "permissionId": 19,
            "name": "leave.team.approve",
            "description": "Permission for leave.team.approve"
          },
          {
            "permissionId": 38,
            "name": "employee.team.read",
            "description": "Permission for employee.team.read"
          },
          {
            "permissionId": 13,
            "name": "attendance.team.read",
            "description": "Permission for attendance.team.read"
          },
          {
            "permissionId": 33,
            "name": "task.assign",
            "description": "Permission for task.assign"
          }
        ]
      },
      "location": "Hyderabad",
      "status": "ACTIVE"
    },
    {
      "id": 19,
      "userId": "EMP019",
      "fullName": "Test Register User",
      "workEmail": "newemployee7@company.com",
      "mobileNumber": "1234567890",
      "employeeId": null,
      "department": "Engineering",
      "requestedRole": "EMPLOYEE",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Bangalore",
      "status": "ACTIVE"
    },
    {
      "id": 28,
      "userId": "EMP028",
      "fullName": "John Doe 1781340790",
      "workEmail": "onboard_emp_1781340790@company.com",
      "mobileNumber": "9876543210",
      "employeeId": null,
      "department": "Engineering",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 7,
      "userId": "EMP007",
      "fullName": "Register Test User",
      "workEmail": "regtest@company.com",
      "mobileNumber": "0987654321",
      "employeeId": null,
      "department": "QA",
      "requestedRole": "EMPLOYEE",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": null,
      "status": null
    },
    {
      "id": 6,
      "userId": "EMP006",
      "fullName": "David",
      "workEmail": "david@company.com",
      "mobileNumber": null,
      "employeeId": "2",
      "department": null,
      "requestedRole": "EMPLOYEE",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": null,
      "status": null
    },
    {
      "id": 22,
      "userId": "EMP022",
      "fullName": "Flow Register Test",
      "workEmail": "flow_emp_1781330458@company.com",
      "mobileNumber": null,
      "employeeId": null,
      "department": null,
      "requestedRole": "EMPLOYEE",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": null,
      "status": "ACTIVE"
    },
    {
      "id": 20,
      "userId": "EMP020",
      "fullName": "Test Custom JSON ID",
      "workEmail": "newemployee8@company.com",
      "mobileNumber": "1234567890",
      "employeeId": null,
      "department": "Engineering",
      "requestedRole": "EMPLOYEE",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Bangalore",
      "status": "ACTIVE"
    },
    {
      "id": 23,
      "userId": "EMP023",
      "fullName": "Flow Register Test",
      "workEmail": "flow_emp_1781331583@company.com",
      "mobileNumber": null,
      "employeeId": null,
      "department": null,
      "requestedRole": "EMPLOYEE",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": null,
      "status": "ACTIVE"
    },
    {
      "id": 9,
      "userId": "EMP009",
      "fullName": "Super admin User",
      "workEmail": "superadmin@company.com",
      "mobileNumber": "5550007",
      "employeeId": null,
      "department": "Super admin",
      "requestedRole": "SUPER_ADMIN",
      "role": {
        "roleId": 6,
        "name": "SUPER_ADMIN",
        "description": "Role for SUPER_ADMIN",
        "permissions": [
          {
            "permissionId": 32,
            "name": "recruitment.manage",
            "description": "Permission for recruitment.manage"
          },
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 7,
            "name": "user.update",
            "description": "Permission for user.update"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 19,
            "name": "leave.team.approve",
            "description": "Permission for leave.team.approve"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 11,
            "name": "attendance.read",
            "description": "Permission for attendance.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 33,
            "name": "task.assign",
            "description": "Permission for task.assign"
          },
          {
            "permissionId": 23,
            "name": "salary.manage",
            "description": "Permission for salary.manage"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 30,
            "name": "role.manage",
            "description": "Permission for role.manage"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 38,
            "name": "employee.team.read",
            "description": "Permission for employee.team.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 6,
            "name": "user.read",
            "description": "Permission for user.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 34,
            "name": "performance.review",
            "description": "Permission for performance.review"
          },
          {
            "permissionId": 25,
            "name": "reports.view",
            "description": "Permission for reports.view"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 40,
            "name": "user.manage",
            "description": "Permission for user.manage"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 4,
            "name": "employee.update",
            "description": "Update employees"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 29,
            "name": "system.manage",
            "description": "Permission for system.manage"
          },
          {
            "permissionId": 31,
            "name": "permission.manage",
            "description": "Permission for permission.manage"
          },
          {
            "permissionId": 9,
            "name": "employee.read",
            "description": "Permission for employee.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 5,
            "name": "user.create",
            "description": "Permission for user.create"
          },
          {
            "permissionId": 13,
            "name": "attendance.team.read",
            "description": "Permission for attendance.team.read"
          },
          {
            "permissionId": 28,
            "name": "reports.manager",
            "description": "Permission for reports.manager"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 8,
            "name": "user.delete",
            "description": "Permission for user.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 16,
            "name": "leave.read",
            "description": "Permission for leave.read"
          },
          {
            "permissionId": 18,
            "name": "leave.manage",
            "description": "Permission for leave.manage"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 17,
            "name": "leave.approve",
            "description": "Permission for leave.approve"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 27,
            "name": "reports.finance",
            "description": "Permission for reports.finance"
          },
          {
            "permissionId": 3,
            "name": "employee.create",
            "description": "Create employees"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 10,
            "name": "employee.delete",
            "description": "Permission for employee.delete"
          },
          {
            "permissionId": 22,
            "name": "payroll.manage",
            "description": "Permission for payroll.manage"
          },
          {
            "permissionId": 21,
            "name": "payroll.read",
            "description": "Permission for payroll.read"
          },
          {
            "permissionId": 26,
            "name": "reports.hr",
            "description": "Permission for reports.hr"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          },
          {
            "permissionId": 12,
            "name": "attendance.manage",
            "description": "Permission for attendance.manage"
          },
          {
            "permissionId": 35,
            "name": "expense.manage",
            "description": "Permission for expense.manage"
          }
        ]
      },
      "location": "Headquarters",
      "status": "ACTIVE"
    },
    {
      "id": 25,
      "userId": "EMP025",
      "fullName": "John Doe 1781334248",
      "workEmail": "onboard_emp_1781334248@company.com",
      "mobileNumber": "9876543210",
      "employeeId": null,
      "department": "Engineering",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 17,
      "userId": "EMP017",
      "fullName": "Arun Kumar",
      "workEmail": "arun.kumar@emscompany.com",
      "mobileNumber": "9876543210",
      "employeeId": "EMP2026001",
      "department": "Software Development",
      "requestedRole": "EMPLOYEE",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 14,
      "userId": "EMP014",
      "fullName": "Finance Register User",
      "workEmail": "finance.register1781241236@company.com",
      "mobileNumber": "1234567890",
      "employeeId": "EMP236",
      "department": "Finance Department",
      "requestedRole": "EMPLOYEE",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": null,
      "status": "ACTIVE"
    },
    {
      "id": 26,
      "userId": "EMP026",
      "fullName": "John Doe 1781334271",
      "workEmail": "onboard_emp_1781334271@company.com",
      "mobileNumber": "9876543210",
      "employeeId": null,
      "department": "Engineering",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 32,
      "userId": "EMP032",
      "fullName": "Manoj Test",
      "workEmail": "manoj_test_1781498927@gmail.com",
      "mobileNumber": "9128909848",
      "employeeId": "EMP_TEST_1781498927",
      "department": "IT",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 33,
      "userId": "EMP033",
      "fullName": "Manoj Test",
      "workEmail": "manoj_test@gmail.com",
      "mobileNumber": "9128909848",
      "employeeId": "EM927",
      "department": "IT",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 4,
      "userId": "EMP004",
      "fullName": "Subashini",
      "workEmail": "subhamano3006@gmail.com",
      "mobileNumber": "9360182757",
      "employeeId": "123",
      "department": "IT",
      "requestedRole": "HR",
      "role": {
        "roleId": 3,
        "name": "HR",
        "description": "Role for HR",
        "permissions": [
          {
            "permissionId": 32,
            "name": "recruitment.manage",
            "description": "Permission for recruitment.manage"
          },
          {
            "permissionId": 3,
            "name": "employee.create",
            "description": "Create employees"
          },
          {
            "permissionId": 9,
            "name": "employee.read",
            "description": "Permission for employee.read"
          },
          {
            "permissionId": 26,
            "name": "reports.hr",
            "description": "Permission for reports.hr"
          },
          {
            "permissionId": 4,
            "name": "employee.update",
            "description": "Update employees"
          },
          {
            "permissionId": 16,
            "name": "leave.read",
            "description": "Permission for leave.read"
          },
          {
            "permissionId": 11,
            "name": "attendance.read",
            "description": "Permission for attendance.read"
          },
          {
            "permissionId": 17,
            "name": "leave.approve",
            "description": "Permission for leave.approve"
          }
        ]
      },
      "location": "TRL",
      "status": null
    },
    {
      "id": 27,
      "userId": "EMP027",
      "fullName": "John Doe 1781334442",
      "workEmail": "onboard_emp_1781334442@company.com",
      "mobileNumber": "9876543210",
      "employeeId": null,
      "department": "Engineering",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 37,
      "userId": "EMP037",
      "fullName": "Manoj Separate",
      "workEmail": "manoj_sep_1781501344@gmail.com",
      "mobileNumber": "9128909848",
      "employeeId": "EMP_SEP_1781501344",
      "department": "IT",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 3,
      "userId": "EMP003",
      "fullName": "karthick G",
      "workEmail": "abcd12@gmail.com",
      "mobileNumber": "09938362013",
      "employeeId": "ems123",
      "department": "IT",
      "requestedRole": "ADMIN",
      "role": {
        "roleId": 5,
        "name": "ADMIN",
        "description": "Role for ADMIN",
        "permissions": [
          {
            "permissionId": 3,
            "name": "employee.create",
            "description": "Create employees"
          },
          {
            "permissionId": 7,
            "name": "user.update",
            "description": "Permission for user.update"
          },
          {
            "permissionId": 9,
            "name": "employee.read",
            "description": "Permission for employee.read"
          },
          {
            "permissionId": 4,
            "name": "employee.update",
            "description": "Update employees"
          },
          {
            "permissionId": 18,
            "name": "leave.manage",
            "description": "Permission for leave.manage"
          },
          {
            "permissionId": 12,
            "name": "attendance.manage",
            "description": "Permission for attendance.manage"
          },
          {
            "permissionId": 5,
            "name": "user.create",
            "description": "Permission for user.create"
          },
          {
            "permissionId": 25,
            "name": "reports.view",
            "description": "Permission for reports.view"
          },
          {
            "permissionId": 40,
            "name": "user.manage",
            "description": "Permission for user.manage"
          },
          {
            "permissionId": 6,
            "name": "user.read",
            "description": "Permission for user.read"
          }
        ]
      },
      "location": "TRL",
      "status": null
    },
    {
      "id": 21,
      "userId": "EMP021",
      "fullName": "Rahul Kumar",
      "workEmail": "rahul.kumar@company.com",
      "mobileNumber": "9876543210",
      "employeeId": "EMP102",
      "department": "Engineering",
      "requestedRole": "Admin",
      "role": {
        "roleId": 5,
        "name": "ADMIN",
        "description": "Role for ADMIN",
        "permissions": [
          {
            "permissionId": 3,
            "name": "employee.create",
            "description": "Create employees"
          },
          {
            "permissionId": 7,
            "name": "user.update",
            "description": "Permission for user.update"
          },
          {
            "permissionId": 9,
            "name": "employee.read",
            "description": "Permission for employee.read"
          },
          {
            "permissionId": 4,
            "name": "employee.update",
            "description": "Update employees"
          },
          {
            "permissionId": 18,
            "name": "leave.manage",
            "description": "Permission for leave.manage"
          },
          {
            "permissionId": 12,
            "name": "attendance.manage",
            "description": "Permission for attendance.manage"
          },
          {
            "permissionId": 5,
            "name": "user.create",
            "description": "Permission for user.create"
          },
          {
            "permissionId": 25,
            "name": "reports.view",
            "description": "Permission for reports.view"
          },
          {
            "permissionId": 40,
            "name": "user.manage",
            "description": "Permission for user.manage"
          },
          {
            "permissionId": 6,
            "name": "user.read",
            "description": "Permission for user.read"
          }
        ]
      },
      "location": "Bangalore",
      "status": "ACTIVE"
    },
    {
      "id": 5,
      "userId": "EMP005",
      "fullName": "Super Admin",
      "workEmail": "emssuperadmin@gmail.com",
      "mobileNumber": "1234567890",
      "employeeId": null,
      "department": "IT",
      "requestedRole": "Super Admin",
      "role": {
        "roleId": 6,
        "name": "SUPER_ADMIN",
        "description": "Role for SUPER_ADMIN",
        "permissions": [
          {
            "permissionId": 32,
            "name": "recruitment.manage",
            "description": "Permission for recruitment.manage"
          },
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 7,
            "name": "user.update",
            "description": "Permission for user.update"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 19,
            "name": "leave.team.approve",
            "description": "Permission for leave.team.approve"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 11,
            "name": "attendance.read",
            "description": "Permission for attendance.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 33,
            "name": "task.assign",
            "description": "Permission for task.assign"
          },
          {
            "permissionId": 23,
            "name": "salary.manage",
            "description": "Permission for salary.manage"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 30,
            "name": "role.manage",
            "description": "Permission for role.manage"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 38,
            "name": "employee.team.read",
            "description": "Permission for employee.team.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 6,
            "name": "user.read",
            "description": "Permission for user.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 34,
            "name": "performance.review",
            "description": "Permission for performance.review"
          },
          {
            "permissionId": 25,
            "name": "reports.view",
            "description": "Permission for reports.view"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 40,
            "name": "user.manage",
            "description": "Permission for user.manage"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 4,
            "name": "employee.update",
            "description": "Update employees"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 29,
            "name": "system.manage",
            "description": "Permission for system.manage"
          },
          {
            "permissionId": 31,
            "name": "permission.manage",
            "description": "Permission for permission.manage"
          },
          {
            "permissionId": 9,
            "name": "employee.read",
            "description": "Permission for employee.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 5,
            "name": "user.create",
            "description": "Permission for user.create"
          },
          {
            "permissionId": 13,
            "name": "attendance.team.read",
            "description": "Permission for attendance.team.read"
          },
          {
            "permissionId": 28,
            "name": "reports.manager",
            "description": "Permission for reports.manager"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 8,
            "name": "user.delete",
            "description": "Permission for user.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 16,
            "name": "leave.read",
            "description": "Permission for leave.read"
          },
          {
            "permissionId": 18,
            "name": "leave.manage",
            "description": "Permission for leave.manage"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 17,
            "name": "leave.approve",
            "description": "Permission for leave.approve"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 27,
            "name": "reports.finance",
            "description": "Permission for reports.finance"
          },
          {
            "permissionId": 3,
            "name": "employee.create",
            "description": "Create employees"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 10,
            "name": "employee.delete",
            "description": "Permission for employee.delete"
          },
          {
            "permissionId": 22,
            "name": "payroll.manage",
            "description": "Permission for payroll.manage"
          },
          {
            "permissionId": 21,
            "name": "payroll.read",
            "description": "Permission for payroll.read"
          },
          {
            "permissionId": 26,
            "name": "reports.hr",
            "description": "Permission for reports.hr"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          },
          {
            "permissionId": 12,
            "name": "attendance.manage",
            "description": "Permission for attendance.manage"
          },
          {
            "permissionId": 35,
            "name": "expense.manage",
            "description": "Permission for expense.manage"
          }
        ]
      },
      "location": "Headquarters",
      "status": null
    },
    {
      "id": 36,
      "userId": "EMP036",
      "fullName": "Manoj Test",
      "workEmail": "manoj_test_1781500676@gmail.com",
      "mobileNumber": "9128909848",
      "employeeId": "EMP_TEST_1781500676",
      "department": "IT",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 16,
      "userId": "EMP016",
      "fullName": "HR Register User",
      "workEmail": "hr@company.com",
      "mobileNumber": "1234567890",
      "employeeId": "EMP992",
      "department": "HR Department",
      "requestedRole": "EMPLOYEE",
      "role": {
        "roleId": 3,
        "name": "HR",
        "description": "Role for HR",
        "permissions": [
          {
            "permissionId": 32,
            "name": "recruitment.manage",
            "description": "Permission for recruitment.manage"
          },
          {
            "permissionId": 3,
            "name": "employee.create",
            "description": "Create employees"
          },
          {
            "permissionId": 9,
            "name": "employee.read",
            "description": "Permission for employee.read"
          },
          {
            "permissionId": 26,
            "name": "reports.hr",
            "description": "Permission for reports.hr"
          },
          {
            "permissionId": 4,
            "name": "employee.update",
            "description": "Update employees"
          },
          {
            "permissionId": 16,
            "name": "leave.read",
            "description": "Permission for leave.read"
          },
          {
            "permissionId": 11,
            "name": "attendance.read",
            "description": "Permission for attendance.read"
          },
          {
            "permissionId": 17,
            "name": "leave.approve",
            "description": "Permission for leave.approve"
          }
        ]
      },
      "location": null,
      "status": "ACTIVE"
    },
    {
      "id": 29,
      "userId": "EMP029",
      "fullName": "Flow Register Test",
      "workEmail": "flow_emp_1781346341@company.com",
      "mobileNumber": null,
      "employeeId": null,
      "department": null,
      "requestedRole": "EMPLOYEE",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": null,
      "status": "ACTIVE"
    },
    {
      "id": 38,
      "userId": "EMP038",
      "fullName": "Manoj Separate",
      "workEmail": "manoj_sep_1781501352@gmail.com",
      "mobileNumber": "9128909848",
      "employeeId": "EMP_SEP_1781501352",
      "department": "IT",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 31,
      "userId": "EMP031",
      "fullName": "Manoj Test",
      "workEmail": "manoj_test_1781498766@gmail.com",
      "mobileNumber": "9128909848",
      "employeeId": "EMP_TEST_178149",
      "department": "IT",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 34,
      "userId": "EMP034",
      "fullName": "Manoj Separate",
      "workEmail": "manoj_sep_1781499541@gmail.com",
      "mobileNumber": "9128909848",
      "employeeId": "EMP_SEP_1781499541",
      "department": "IT",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 15,
      "userId": "EMP015",
      "fullName": "Finance Register User",
      "workEmail": "finance@company.com",
      "mobileNumber": "1234567890",
      "employeeId": "EMP991",
      "department": "Finance Department",
      "requestedRole": "EMPLOYEE",
      "role": {
        "roleId": 4,
        "name": "FINANCE",
        "description": "Role for FINANCE",
        "permissions": [
          {
            "permissionId": 23,
            "name": "salary.manage",
            "description": "Permission for salary.manage"
          },
          {
            "permissionId": 27,
            "name": "reports.finance",
            "description": "Permission for reports.finance"
          },
          {
            "permissionId": 22,
            "name": "payroll.manage",
            "description": "Permission for payroll.manage"
          },
          {
            "permissionId": 21,
            "name": "payroll.read",
            "description": "Permission for payroll.read"
          },
          {
            "permissionId": 35,
            "name": "expense.manage",
            "description": "Permission for expense.manage"
          }
        ]
      },
      "location": null,
      "status": "ACTIVE"
    },
    {
      "id": 30,
      "userId": "EMP030",
      "fullName": "Manoj",
      "workEmail": "manoj@gmail.com",
      "mobileNumber": "9128909848",
      "employeeId": "EBM120",
      "department": "IT",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 10,
      "userId": "EMP010",
      "fullName": "Employee User",
      "workEmail": "employee@company.com",
      "mobileNumber": "5550009",
      "employeeId": null,
      "department": "Employee",
      "requestedRole": "EMPLOYEE",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Headquarters",
      "status": "ACTIVE"
    },
    {
      "id": 35,
      "userId": "EMP035",
      "fullName": "Manoj Separate",
      "workEmail": "manoj_sep_1781499896@gmail.com",
      "mobileNumber": "9128909848",
      "employeeId": "EMP_SEP_1781499896",
      "department": "IT",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    },
    {
      "id": 13,
      "userId": "EMP013",
      "fullName": "Admin User",
      "workEmail": "admin@company.com",
      "mobileNumber": "5550012",
      "employeeId": null,
      "department": "Admin",
      "requestedRole": "ADMIN",
      "role": {
        "roleId": 5,
        "name": "ADMIN",
        "description": "Role for ADMIN",
        "permissions": [
          {
            "permissionId": 3,
            "name": "employee.create",
            "description": "Create employees"
          },
          {
            "permissionId": 7,
            "name": "user.update",
            "description": "Permission for user.update"
          },
          {
            "permissionId": 9,
            "name": "employee.read",
            "description": "Permission for employee.read"
          },
          {
            "permissionId": 4,
            "name": "employee.update",
            "description": "Update employees"
          },
          {
            "permissionId": 18,
            "name": "leave.manage",
            "description": "Permission for leave.manage"
          },
          {
            "permissionId": 12,
            "name": "attendance.manage",
            "description": "Permission for attendance.manage"
          },
          {
            "permissionId": 5,
            "name": "user.create",
            "description": "Permission for user.create"
          },
          {
            "permissionId": 25,
            "name": "reports.view",
            "description": "Permission for reports.view"
          },
          {
            "permissionId": 40,
            "name": "user.manage",
            "description": "Permission for user.manage"
          },
          {
            "permissionId": 6,
            "name": "user.read",
            "description": "Permission for user.read"
          }
        ]
      },
      "location": "Headquarters",
      "status": "ACTIVE"
    },
    {
      "id": 39,
      "userId": "EMP039",
      "fullName": "Manoj Test",
      "workEmail": "manoj_test_1781501358@gmail.com",
      "mobileNumber": "9128909848",
      "employeeId": "EMP_TEST_1781501358",
      "department": "IT",
      "requestedRole": "Employee",
      "role": {
        "roleId": 2,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "permissionId": 46,
            "name": "employee.onboarding.read.self",
            "description": "Permission for employee.onboarding.read.self"
          },
          {
            "permissionId": 65,
            "name": "employee.asset.read",
            "description": "Permission for employee.asset.read"
          },
          {
            "permissionId": 50,
            "name": "employee.onboarding.read",
            "description": "Permission for employee.onboarding.read"
          },
          {
            "permissionId": 68,
            "name": "employee.expense.read",
            "description": "Permission for employee.expense.read"
          },
          {
            "permissionId": 47,
            "name": "employee.dashboard.read",
            "description": "Permission for employee.dashboard.read"
          },
          {
            "permissionId": 78,
            "name": "employee.support-ticket.update",
            "description": "Permission for employee.support-ticket.update"
          },
          {
            "permissionId": 81,
            "name": "employee.schedule.read",
            "description": "Permission for employee.schedule.read"
          },
          {
            "permissionId": 86,
            "name": "performance.self.read",
            "description": "Permission for performance.self.read"
          },
          {
            "permissionId": 57,
            "name": "employee.leave.create",
            "description": "Permission for employee.leave.create"
          },
          {
            "permissionId": 54,
            "name": "employee.onboarding.submit",
            "description": "Permission for employee.onboarding.submit"
          },
          {
            "permissionId": 73,
            "name": "employee.training.complete",
            "description": "Permission for employee.training.complete"
          },
          {
            "permissionId": 77,
            "name": "employee.support-ticket.read",
            "description": "Permission for employee.support-ticket.read"
          },
          {
            "permissionId": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "permissionId": 79,
            "name": "employee.goal.read",
            "description": "Permission for employee.goal.read"
          },
          {
            "permissionId": 88,
            "name": "asset.self.read",
            "description": "Permission for asset.self.read"
          },
          {
            "permissionId": 42,
            "name": "onboarding.self.update",
            "description": "Permission for onboarding.self.update"
          },
          {
            "permissionId": 41,
            "name": "onboarding.self.read",
            "description": "Permission for onboarding.self.read"
          },
          {
            "permissionId": 58,
            "name": "employee.leave.read",
            "description": "Permission for employee.leave.read"
          },
          {
            "permissionId": 83,
            "name": "payslip.self.read",
            "description": "Permission for payslip.self.read"
          },
          {
            "permissionId": 66,
            "name": "employee.asset.request",
            "description": "Permission for employee.asset.request"
          },
          {
            "permissionId": 67,
            "name": "employee.expense.create",
            "description": "Permission for employee.expense.create"
          },
          {
            "permissionId": 51,
            "name": "employee.onboarding.update",
            "description": "Permission for employee.onboarding.update"
          },
          {
            "permissionId": 43,
            "name": "onboarding.document.upload",
            "description": "Permission for onboarding.document.upload"
          },
          {
            "permissionId": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "permissionId": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "permissionId": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "permissionId": 63,
            "name": "employee.document.upload",
            "description": "Permission for employee.document.upload"
          },
          {
            "permissionId": 55,
            "name": "employee.attendance.read",
            "description": "Permission for employee.attendance.read"
          },
          {
            "permissionId": 69,
            "name": "employee.expense.update",
            "description": "Permission for employee.expense.update"
          },
          {
            "permissionId": 75,
            "name": "employee.notification.update",
            "description": "Permission for employee.notification.update"
          },
          {
            "permissionId": 45,
            "name": "onboarding.self.submit",
            "description": "Permission for onboarding.self.submit"
          },
          {
            "permissionId": 44,
            "name": "onboarding.document.read.self",
            "description": "Permission for onboarding.document.read.self"
          },
          {
            "permissionId": 74,
            "name": "employee.notification.read",
            "description": "Permission for employee.notification.read"
          },
          {
            "permissionId": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "permissionId": 71,
            "name": "employee.performance.self-review.submit",
            "description": "Permission for employee.performance.self-review.submit"
          },
          {
            "permissionId": 82,
            "name": "employee.announcement.read",
            "description": "Permission for employee.announcement.read"
          },
          {
            "permissionId": 56,
            "name": "employee.attendance.create",
            "description": "Permission for employee.attendance.create"
          },
          {
            "permissionId": 61,
            "name": "employee.payslip.download",
            "description": "Permission for employee.payslip.download"
          },
          {
            "permissionId": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "permissionId": 60,
            "name": "employee.payslip.read",
            "description": "Permission for employee.payslip.read"
          },
          {
            "permissionId": 64,
            "name": "employee.document.delete",
            "description": "Permission for employee.document.delete"
          },
          {
            "permissionId": 52,
            "name": "employee.onboarding.document.upload",
            "description": "Permission for employee.onboarding.document.upload"
          },
          {
            "permissionId": 70,
            "name": "employee.performance.read",
            "description": "Permission for employee.performance.read"
          },
          {
            "permissionId": 85,
            "name": "expense.self.read",
            "description": "Permission for expense.self.read"
          },
          {
            "permissionId": 72,
            "name": "employee.training.read",
            "description": "Permission for employee.training.read"
          },
          {
            "permissionId": 80,
            "name": "employee.goal.update",
            "description": "Permission for employee.goal.update"
          },
          {
            "permissionId": 84,
            "name": "document.self.read",
            "description": "Permission for document.self.read"
          },
          {
            "permissionId": 62,
            "name": "employee.document.read",
            "description": "Permission for employee.document.read"
          },
          {
            "permissionId": 59,
            "name": "employee.leave.cancel",
            "description": "Permission for employee.leave.cancel"
          },
          {
            "permissionId": 76,
            "name": "employee.support-ticket.create",
            "description": "Permission for employee.support-ticket.create"
          },
          {
            "permissionId": 49,
            "name": "employee.profile.update",
            "description": "Permission for employee.profile.update"
          },
          {
            "permissionId": 53,
            "name": "employee.onboarding.document.read",
            "description": "Permission for employee.onboarding.document.read"
          },
          {
            "permissionId": 87,
            "name": "goal.self.read",
            "description": "Permission for goal.self.read"
          },
          {
            "permissionId": 48,
            "name": "employee.profile.read",
            "description": "Permission for employee.profile.read"
          }
        ]
      },
      "location": "Chennai",
      "status": "ACTIVE"
    }
  ]
}
```

---

### List Roles as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/roles`

**Response Payload**:
```json
{
  "success": true,
  "message": "Roles list retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": [
    {
      "roleId": 13,
      "name": "SUPPORT_STAFF",
      "description": "Role for customer and system support staff",
      "permissions": []
    },
    {
      "roleId": 15,
      "name": "SETTINGS_AUDITOR_UPDATED",
      "description": "Auditing settings updated",
      "permissions": []
    },
    {
      "roleId": 16,
      "name": "SETTINGS_AUDITOR",
      "description": "Role for settings auditing",
      "permissions": []
    },
    {
      "roleId": 1,
      "name": "MANAGER",
      "description": "Role for MANAGER",
      "permissions": [
        {
          "permissionId": 38,
          "name": "employee.team.read",
          "description": "Permission for employee.team.read"
        },
        {
          "permissionId": 19,
          "name": "leave.team.approve",
          "description": "Permission for leave.team.approve"
        },
        {
          "permissionId": 34,
          "name": "performance.review",
          "description": "Permission for performance.review"
        },
        {
          "permissionId": 33,
          "name": "task.assign",
          "description": "Permission for task.assign"
        },
        {
          "permissionId": 13,
          "name": "attendance.team.read",
          "description": "Permission for attendance.team.read"
        }
      ]
    },
    {
      "roleId": 2,
      "name": "EMPLOYEE",
      "description": "Role for EMPLOYEE",
      "permissions": [
        {
          "permissionId": 70,
          "name": "employee.performance.read",
          "description": "Permission for employee.performance.read"
        },
        {
          "permissionId": 14,
          "name": "attendance.self.read",
          "description": "Permission for attendance.self.read"
        },
        {
          "permissionId": 86,
          "name": "performance.self.read",
          "description": "Permission for performance.self.read"
        },
        {
          "permissionId": 66,
          "name": "employee.asset.request",
          "description": "Permission for employee.asset.request"
        },
        {
          "permissionId": 85,
          "name": "expense.self.read",
          "description": "Permission for expense.self.read"
        },
        {
          "permissionId": 24,
          "name": "payslip.read",
          "description": "Permission for payslip.read"
        },
        {
          "permissionId": 42,
          "name": "onboarding.self.update",
          "description": "Permission for onboarding.self.update"
        },
        {
          "permissionId": 47,
          "name": "employee.dashboard.read",
          "description": "Permission for employee.dashboard.read"
        },
        {
          "permissionId": 72,
          "name": "employee.training.read",
          "description": "Permission for employee.training.read"
        },
        {
          "permissionId": 60,
          "name": "employee.payslip.read",
          "description": "Permission for employee.payslip.read"
        },
        {
          "permissionId": 52,
          "name": "employee.onboarding.document.upload",
          "description": "Permission for employee.onboarding.document.upload"
        },
        {
          "permissionId": 46,
          "name": "employee.onboarding.read.self",
          "description": "Permission for employee.onboarding.read.self"
        },
        {
          "permissionId": 71,
          "name": "employee.performance.self-review.submit",
          "description": "Permission for employee.performance.self-review.submit"
        },
        {
          "permissionId": 51,
          "name": "employee.onboarding.update",
          "description": "Permission for employee.onboarding.update"
        },
        {
          "permissionId": 65,
          "name": "employee.asset.read",
          "description": "Permission for employee.asset.read"
        },
        {
          "permissionId": 67,
          "name": "employee.expense.create",
          "description": "Permission for employee.expense.create"
        },
        {
          "permissionId": 54,
          "name": "employee.onboarding.submit",
          "description": "Permission for employee.onboarding.submit"
        },
        {
          "permissionId": 36,
          "name": "profile.read",
          "description": "Permission for profile.read"
        },
        {
          "permissionId": 62,
          "name": "employee.document.read",
          "description": "Permission for employee.document.read"
        },
        {
          "permissionId": 78,
          "name": "employee.support-ticket.update",
          "description": "Permission for employee.support-ticket.update"
        },
        {
          "permissionId": 84,
          "name": "document.self.read",
          "description": "Permission for document.self.read"
        },
        {
          "permissionId": 53,
          "name": "employee.onboarding.document.read",
          "description": "Permission for employee.onboarding.document.read"
        },
        {
          "permissionId": 55,
          "name": "employee.attendance.read",
          "description": "Permission for employee.attendance.read"
        },
        {
          "permissionId": 58,
          "name": "employee.leave.read",
          "description": "Permission for employee.leave.read"
        },
        {
          "permissionId": 76,
          "name": "employee.support-ticket.create",
          "description": "Permission for employee.support-ticket.create"
        },
        {
          "permissionId": 77,
          "name": "employee.support-ticket.read",
          "description": "Permission for employee.support-ticket.read"
        },
        {
          "permissionId": 48,
          "name": "employee.profile.read",
          "description": "Permission for employee.profile.read"
        },
        {
          "permissionId": 57,
          "name": "employee.leave.create",
          "description": "Permission for employee.leave.create"
        },
        {
          "permissionId": 75,
          "name": "employee.notification.update",
          "description": "Permission for employee.notification.update"
        },
        {
          "permissionId": 49,
          "name": "employee.profile.update",
          "description": "Permission for employee.profile.update"
        },
        {
          "permissionId": 20,
          "name": "leave.self.read",
          "description": "Permission for leave.self.read"
        },
        {
          "permissionId": 80,
          "name": "employee.goal.update",
          "description": "Permission for employee.goal.update"
        },
        {
          "permissionId": 83,
          "name": "payslip.self.read",
          "description": "Permission for payslip.self.read"
        },
        {
          "permissionId": 37,
          "name": "profile.update",
          "description": "Permission for profile.update"
        },
        {
          "permissionId": 45,
          "name": "onboarding.self.submit",
          "description": "Permission for onboarding.self.submit"
        },
        {
          "permissionId": 74,
          "name": "employee.notification.read",
          "description": "Permission for employee.notification.read"
        },
        {
          "permissionId": 68,
          "name": "employee.expense.read",
          "description": "Permission for employee.expense.read"
        },
        {
          "permissionId": 15,
          "name": "leave.create",
          "description": "Permission for leave.create"
        },
        {
          "permissionId": 41,
          "name": "onboarding.self.read",
          "description": "Permission for onboarding.self.read"
        },
        {
          "permissionId": 64,
          "name": "employee.document.delete",
          "description": "Permission for employee.document.delete"
        },
        {
          "permissionId": 79,
          "name": "employee.goal.read",
          "description": "Permission for employee.goal.read"
        },
        {
          "permissionId": 50,
          "name": "employee.onboarding.read",
          "description": "Permission for employee.onboarding.read"
        },
        {
          "permissionId": 43,
          "name": "onboarding.document.upload",
          "description": "Permission for onboarding.document.upload"
        },
        {
          "permissionId": 88,
          "name": "asset.self.read",
          "description": "Permission for asset.self.read"
        },
        {
          "permissionId": 44,
          "name": "onboarding.document.read.self",
          "description": "Permission for onboarding.document.read.self"
        },
        {
          "permissionId": 61,
          "name": "employee.payslip.download",
          "description": "Permission for employee.payslip.download"
        },
        {
          "permissionId": 87,
          "name": "goal.self.read",
          "description": "Permission for goal.self.read"
        },
        {
          "permissionId": 59,
          "name": "employee.leave.cancel",
          "description": "Permission for employee.leave.cancel"
        },
        {
          "permissionId": 81,
          "name": "employee.schedule.read",
          "description": "Permission for employee.schedule.read"
        },
        {
          "permissionId": 63,
          "name": "employee.document.upload",
          "description": "Permission for employee.document.upload"
        },
        {
          "permissionId": 73,
          "name": "employee.training.complete",
          "description": "Permission for employee.training.complete"
        },
        {
          "permissionId": 82,
          "name": "employee.announcement.read",
          "description": "Permission for employee.announcement.read"
        },
        {
          "permissionId": 69,
          "name": "employee.expense.update",
          "description": "Permission for employee.expense.update"
        },
        {
          "permissionId": 56,
          "name": "employee.attendance.create",
          "description": "Permission for employee.attendance.create"
        }
      ]
    },
    {
      "roleId": 3,
      "name": "HR",
      "description": "Role for HR",
      "permissions": [
        {
          "permissionId": 3,
          "name": "employee.create",
          "description": "Create employees"
        },
        {
          "permissionId": 17,
          "name": "leave.approve",
          "description": "Permission for leave.approve"
        },
        {
          "permissionId": 4,
          "name": "employee.update",
          "description": "Update employees"
        },
        {
          "permissionId": 32,
          "name": "recruitment.manage",
          "description": "Permission for recruitment.manage"
        },
        {
          "permissionId": 11,
          "name": "attendance.read",
          "description": "Permission for attendance.read"
        },
        {
          "permissionId": 9,
          "name": "employee.read",
          "description": "Permission for employee.read"
        },
        {
          "permissionId": 16,
          "name": "leave.read",
          "description": "Permission for leave.read"
        },
        {
          "permissionId": 26,
          "name": "reports.hr",
          "description": "Permission for reports.hr"
        }
      ]
    },
    {
      "roleId": 4,
      "name": "FINANCE",
      "description": "Role for FINANCE",
      "permissions": [
        {
          "permissionId": 21,
          "name": "payroll.read",
          "description": "Permission for payroll.read"
        },
        {
          "permissionId": 23,
          "name": "salary.manage",
          "description": "Permission for salary.manage"
        },
        {
          "permissionId": 35,
          "name": "expense.manage",
          "description": "Permission for expense.manage"
        },
        {
          "permissionId": 27,
          "name": "reports.finance",
          "description": "Permission for reports.finance"
        },
        {
          "permissionId": 22,
          "name": "payroll.manage",
          "description": "Permission for payroll.manage"
        }
      ]
    },
    {
      "roleId": 5,
      "name": "ADMIN",
      "description": "Role for ADMIN",
      "permissions": [
        {
          "permissionId": 6,
          "name": "user.read",
          "description": "Permission for user.read"
        },
        {
          "permissionId": 12,
          "name": "attendance.manage",
          "description": "Permission for attendance.manage"
        },
        {
          "permissionId": 25,
          "name": "reports.view",
          "description": "Permission for reports.view"
        },
        {
          "permissionId": 3,
          "name": "employee.create",
          "description": "Create employees"
        },
        {
          "permissionId": 4,
          "name": "employee.update",
          "description": "Update employees"
        },
        {
          "permissionId": 7,
          "name": "user.update",
          "description": "Permission for user.update"
        },
        {
          "permissionId": 5,
          "name": "user.create",
          "description": "Permission for user.create"
        },
        {
          "permissionId": 9,
          "name": "employee.read",
          "description": "Permission for employee.read"
        },
        {
          "permissionId": 40,
          "name": "user.manage",
          "description": "Permission for user.manage"
        },
        {
          "permissionId": 18,
          "name": "leave.manage",
          "description": "Permission for leave.manage"
        }
      ]
    },
    {
      "roleId": 6,
      "name": "SUPER_ADMIN",
      "description": "Role for SUPER_ADMIN",
      "permissions": [
        {
          "permissionId": 10,
          "name": "employee.delete",
          "description": "Permission for employee.delete"
        },
        {
          "permissionId": 35,
          "name": "expense.manage",
          "description": "Permission for expense.manage"
        },
        {
          "permissionId": 5,
          "name": "user.create",
          "description": "Permission for user.create"
        },
        {
          "permissionId": 70,
          "name": "employee.performance.read",
          "description": "Permission for employee.performance.read"
        },
        {
          "permissionId": 9,
          "name": "employee.read",
          "description": "Permission for employee.read"
        },
        {
          "permissionId": 40,
          "name": "user.manage",
          "description": "Permission for user.manage"
        },
        {
          "permissionId": 14,
          "name": "attendance.self.read",
          "description": "Permission for attendance.self.read"
        },
        {
          "permissionId": 86,
          "name": "performance.self.read",
          "description": "Permission for performance.self.read"
        },
        {
          "permissionId": 66,
          "name": "employee.asset.request",
          "description": "Permission for employee.asset.request"
        },
        {
          "permissionId": 18,
          "name": "leave.manage",
          "description": "Permission for leave.manage"
        },
        {
          "permissionId": 85,
          "name": "expense.self.read",
          "description": "Permission for expense.self.read"
        },
        {
          "permissionId": 24,
          "name": "payslip.read",
          "description": "Permission for payslip.read"
        },
        {
          "permissionId": 42,
          "name": "onboarding.self.update",
          "description": "Permission for onboarding.self.update"
        },
        {
          "permissionId": 47,
          "name": "employee.dashboard.read",
          "description": "Permission for employee.dashboard.read"
        },
        {
          "permissionId": 19,
          "name": "leave.team.approve",
          "description": "Permission for leave.team.approve"
        },
        {
          "permissionId": 72,
          "name": "employee.training.read",
          "description": "Permission for employee.training.read"
        },
        {
          "permissionId": 60,
          "name": "employee.payslip.read",
          "description": "Permission for employee.payslip.read"
        },
        {
          "permissionId": 26,
          "name": "reports.hr",
          "description": "Permission for reports.hr"
        },
        {
          "permissionId": 52,
          "name": "employee.onboarding.document.upload",
          "description": "Permission for employee.onboarding.document.upload"
        },
        {
          "permissionId": 46,
          "name": "employee.onboarding.read.self",
          "description": "Permission for employee.onboarding.read.self"
        },
        {
          "permissionId": 71,
          "name": "employee.performance.self-review.submit",
          "description": "Permission for employee.performance.self-review.submit"
        },
        {
          "permissionId": 51,
          "name": "employee.onboarding.update",
          "description": "Permission for employee.onboarding.update"
        },
        {
          "permissionId": 65,
          "name": "employee.asset.read",
          "description": "Permission for employee.asset.read"
        },
        {
          "permissionId": 67,
          "name": "employee.expense.create",
          "description": "Permission for employee.expense.create"
        },
        {
          "permissionId": 54,
          "name": "employee.onboarding.submit",
          "description": "Permission for employee.onboarding.submit"
        },
        {
          "permissionId": 36,
          "name": "profile.read",
          "description": "Permission for profile.read"
        },
        {
          "permissionId": 22,
          "name": "payroll.manage",
          "description": "Permission for payroll.manage"
        },
        {
          "permissionId": 62,
          "name": "employee.document.read",
          "description": "Permission for employee.document.read"
        },
        {
          "permissionId": 78,
          "name": "employee.support-ticket.update",
          "description": "Permission for employee.support-ticket.update"
        },
        {
          "permissionId": 30,
          "name": "role.manage",
          "description": "Permission for role.manage"
        },
        {
          "permissionId": 12,
          "name": "attendance.manage",
          "description": "Permission for attendance.manage"
        },
        {
          "permissionId": 21,
          "name": "payroll.read",
          "description": "Permission for payroll.read"
        },
        {
          "permissionId": 8,
          "name": "user.delete",
          "description": "Permission for user.delete"
        },
        {
          "permissionId": 25,
          "name": "reports.view",
          "description": "Permission for reports.view"
        },
        {
          "permissionId": 3,
          "name": "employee.create",
          "description": "Create employees"
        },
        {
          "permissionId": 84,
          "name": "document.self.read",
          "description": "Permission for document.self.read"
        },
        {
          "permissionId": 31,
          "name": "permission.manage",
          "description": "Permission for permission.manage"
        },
        {
          "permissionId": 53,
          "name": "employee.onboarding.document.read",
          "description": "Permission for employee.onboarding.document.read"
        },
        {
          "permissionId": 23,
          "name": "salary.manage",
          "description": "Permission for salary.manage"
        },
        {
          "permissionId": 55,
          "name": "employee.attendance.read",
          "description": "Permission for employee.attendance.read"
        },
        {
          "permissionId": 58,
          "name": "employee.leave.read",
          "description": "Permission for employee.leave.read"
        },
        {
          "permissionId": 76,
          "name": "employee.support-ticket.create",
          "description": "Permission for employee.support-ticket.create"
        },
        {
          "permissionId": 77,
          "name": "employee.support-ticket.read",
          "description": "Permission for employee.support-ticket.read"
        },
        {
          "permissionId": 6,
          "name": "user.read",
          "description": "Permission for user.read"
        },
        {
          "permissionId": 48,
          "name": "employee.profile.read",
          "description": "Permission for employee.profile.read"
        },
        {
          "permissionId": 57,
          "name": "employee.leave.create",
          "description": "Permission for employee.leave.create"
        },
        {
          "permissionId": 75,
          "name": "employee.notification.update",
          "description": "Permission for employee.notification.update"
        },
        {
          "permissionId": 49,
          "name": "employee.profile.update",
          "description": "Permission for employee.profile.update"
        },
        {
          "permissionId": 20,
          "name": "leave.self.read",
          "description": "Permission for leave.self.read"
        },
        {
          "permissionId": 80,
          "name": "employee.goal.update",
          "description": "Permission for employee.goal.update"
        },
        {
          "permissionId": 83,
          "name": "payslip.self.read",
          "description": "Permission for payslip.self.read"
        },
        {
          "permissionId": 37,
          "name": "profile.update",
          "description": "Permission for profile.update"
        },
        {
          "permissionId": 45,
          "name": "onboarding.self.submit",
          "description": "Permission for onboarding.self.submit"
        },
        {
          "permissionId": 74,
          "name": "employee.notification.read",
          "description": "Permission for employee.notification.read"
        },
        {
          "permissionId": 68,
          "name": "employee.expense.read",
          "description": "Permission for employee.expense.read"
        },
        {
          "permissionId": 7,
          "name": "user.update",
          "description": "Permission for user.update"
        },
        {
          "permissionId": 15,
          "name": "leave.create",
          "description": "Permission for leave.create"
        },
        {
          "permissionId": 28,
          "name": "reports.manager",
          "description": "Permission for reports.manager"
        },
        {
          "permissionId": 32,
          "name": "recruitment.manage",
          "description": "Permission for recruitment.manage"
        },
        {
          "permissionId": 41,
          "name": "onboarding.self.read",
          "description": "Permission for onboarding.self.read"
        },
        {
          "permissionId": 64,
          "name": "employee.document.delete",
          "description": "Permission for employee.document.delete"
        },
        {
          "permissionId": 79,
          "name": "employee.goal.read",
          "description": "Permission for employee.goal.read"
        },
        {
          "permissionId": 50,
          "name": "employee.onboarding.read",
          "description": "Permission for employee.onboarding.read"
        },
        {
          "permissionId": 43,
          "name": "onboarding.document.upload",
          "description": "Permission for onboarding.document.upload"
        },
        {
          "permissionId": 88,
          "name": "asset.self.read",
          "description": "Permission for asset.self.read"
        },
        {
          "permissionId": 13,
          "name": "attendance.team.read",
          "description": "Permission for attendance.team.read"
        },
        {
          "permissionId": 29,
          "name": "system.manage",
          "description": "Permission for system.manage"
        },
        {
          "permissionId": 44,
          "name": "onboarding.document.read.self",
          "description": "Permission for onboarding.document.read.self"
        },
        {
          "permissionId": 61,
          "name": "employee.payslip.download",
          "description": "Permission for employee.payslip.download"
        },
        {
          "permissionId": 17,
          "name": "leave.approve",
          "description": "Permission for leave.approve"
        },
        {
          "permissionId": 87,
          "name": "goal.self.read",
          "description": "Permission for goal.self.read"
        },
        {
          "permissionId": 11,
          "name": "attendance.read",
          "description": "Permission for attendance.read"
        },
        {
          "permissionId": 34,
          "name": "performance.review",
          "description": "Permission for performance.review"
        },
        {
          "permissionId": 33,
          "name": "task.assign",
          "description": "Permission for task.assign"
        },
        {
          "permissionId": 16,
          "name": "leave.read",
          "description": "Permission for leave.read"
        },
        {
          "permissionId": 38,
          "name": "employee.team.read",
          "description": "Permission for employee.team.read"
        },
        {
          "permissionId": 4,
          "name": "employee.update",
          "description": "Update employees"
        },
        {
          "permissionId": 59,
          "name": "employee.leave.cancel",
          "description": "Permission for employee.leave.cancel"
        },
        {
          "permissionId": 81,
          "name": "employee.schedule.read",
          "description": "Permission for employee.schedule.read"
        },
        {
          "permissionId": 63,
          "name": "employee.document.upload",
          "description": "Permission for employee.document.upload"
        },
        {
          "permissionId": 27,
          "name": "reports.finance",
          "description": "Permission for reports.finance"
        },
        {
          "permissionId": 73,
          "name": "employee.training.complete",
          "description": "Permission for employee.training.complete"
        },
        {
          "permissionId": 82,
          "name": "employee.announcement.read",
          "description": "Permission for employee.announcement.read"
        },
        {
          "permissionId": 69,
          "name": "employee.expense.update",
          "description": "Permission for employee.expense.update"
        },
        {
          "permissionId": 56,
          "name": "employee.attendance.create",
          "description": "Permission for employee.attendance.create"
        }
      ]
    }
  ]
}
```

---

### List Permissions as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/permissions`

**Response Payload**:
```json
[
  {
    "permissionId": 3,
    "name": "employee.create",
    "description": "Create employees"
  },
  {
    "permissionId": 4,
    "name": "employee.update",
    "description": "Update employees"
  },
  {
    "permissionId": 5,
    "name": "user.create",
    "description": "Permission for user.create"
  },
  {
    "permissionId": 6,
    "name": "user.read",
    "description": "Permission for user.read"
  },
  {
    "permissionId": 7,
    "name": "user.update",
    "description": "Permission for user.update"
  },
  {
    "permissionId": 8,
    "name": "user.delete",
    "description": "Permission for user.delete"
  },
  {
    "permissionId": 9,
    "name": "employee.read",
    "description": "Permission for employee.read"
  },
  {
    "permissionId": 10,
    "name": "employee.delete",
    "description": "Permission for employee.delete"
  },
  {
    "permissionId": 11,
    "name": "attendance.read",
    "description": "Permission for attendance.read"
  },
  {
    "permissionId": 12,
    "name": "attendance.manage",
    "description": "Permission for attendance.manage"
  },
  {
    "permissionId": 13,
    "name": "attendance.team.read",
    "description": "Permission for attendance.team.read"
  },
  {
    "permissionId": 14,
    "name": "attendance.self.read",
    "description": "Permission for attendance.self.read"
  },
  {
    "permissionId": 15,
    "name": "leave.create",
    "description": "Permission for leave.create"
  },
  {
    "permissionId": 16,
    "name": "leave.read",
    "description": "Permission for leave.read"
  },
  {
    "permissionId": 17,
    "name": "leave.approve",
    "description": "Permission for leave.approve"
  },
  {
    "permissionId": 18,
    "name": "leave.manage",
    "description": "Permission for leave.manage"
  },
  {
    "permissionId": 19,
    "name": "leave.team.approve",
    "description": "Permission for leave.team.approve"
  },
  {
    "permissionId": 20,
    "name": "leave.self.read",
    "description": "Permission for leave.self.read"
  },
  {
    "permissionId": 21,
    "name": "payroll.read",
    "description": "Permission for payroll.read"
  },
  {
    "permissionId": 22,
    "name": "payroll.manage",
    "description": "Permission for payroll.manage"
  },
  {
    "permissionId": 23,
    "name": "salary.manage",
    "description": "Permission for salary.manage"
  },
  {
    "permissionId": 24,
    "name": "payslip.read",
    "description": "Permission for payslip.read"
  },
  {
    "permissionId": 25,
    "name": "reports.view",
    "description": "Permission for reports.view"
  },
  {
    "permissionId": 26,
    "name": "reports.hr",
    "description": "Permission for reports.hr"
  },
  {
    "permissionId": 27,
    "name": "reports.finance",
    "description": "Permission for reports.finance"
  },
  {
    "permissionId": 28,
    "name": "reports.manager",
    "description": "Permission for reports.manager"
  },
  {
    "permissionId": 29,
    "name": "system.manage",
    "description": "Permission for system.manage"
  },
  {
    "permissionId": 30,
    "name": "role.manage",
    "description": "Permission for role.manage"
  },
  {
    "permissionId": 31,
    "name": "permission.manage",
    "description": "Permission for permission.manage"
  },
  {
    "permissionId": 32,
    "name": "recruitment.manage",
    "description": "Permission for recruitment.manage"
  },
  {
    "permissionId": 33,
    "name": "task.assign",
    "description": "Permission for task.assign"
  },
  {
    "permissionId": 34,
    "name": "performance.review",
    "description": "Permission for performance.review"
  },
  {
    "permissionId": 35,
    "name": "expense.manage",
    "description": "Permission for expense.manage"
  },
  {
    "permissionId": 36,
    "name": "profile.read",
    "description": "Permission for profile.read"
  },
  {
    "permissionId": 37,
    "name": "profile.update",
    "description": "Permission for profile.update"
  },
  {
    "permissionId": 38,
    "name": "employee.team.read",
    "description": "Permission for employee.team.read"
  },
  {
    "permissionId": 39,
    "name": "temp.test.permission",
    "description": "Temp test permission"
  },
  {
    "permissionId": 40,
    "name": "user.manage",
    "description": "Permission for user.manage"
  },
  {
    "permissionId": 41,
    "name": "onboarding.self.read",
    "description": "Permission for onboarding.self.read"
  },
  {
    "permissionId": 42,
    "name": "onboarding.self.update",
    "description": "Permission for onboarding.self.update"
  },
  {
    "permissionId": 43,
    "name": "onboarding.document.upload",
    "description": "Permission for onboarding.document.upload"
  },
  {
    "permissionId": 44,
    "name": "onboarding.document.read.self",
    "description": "Permission for onboarding.document.read.self"
  },
  {
    "permissionId": 45,
    "name": "onboarding.self.submit",
    "description": "Permission for onboarding.self.submit"
  },
  {
    "permissionId": 46,
    "name": "employee.onboarding.read.self",
    "description": "Permission for employee.onboarding.read.self"
  },
  {
    "permissionId": 47,
    "name": "employee.dashboard.read",
    "description": "Permission for employee.dashboard.read"
  },
  {
    "permissionId": 48,
    "name": "employee.profile.read",
    "description": "Permission for employee.profile.read"
  },
  {
    "permissionId": 49,
    "name": "employee.profile.update",
    "description": "Permission for employee.profile.update"
  },
  {
    "permissionId": 50,
    "name": "employee.onboarding.read",
    "description": "Permission for employee.onboarding.read"
  },
  {
    "permissionId": 51,
    "name": "employee.onboarding.update",
    "description": "Permission for employee.onboarding.update"
  },
  {
    "permissionId": 52,
    "name": "employee.onboarding.document.upload",
    "description": "Permission for employee.onboarding.document.upload"
  },
  {
    "permissionId": 53,
    "name": "employee.onboarding.document.read",
    "description": "Permission for employee.onboarding.document.read"
  },
  {
    "permissionId": 54,
    "name": "employee.onboarding.submit",
    "description": "Permission for employee.onboarding.submit"
  },
  {
    "permissionId": 55,
    "name": "employee.attendance.read",
    "description": "Permission for employee.attendance.read"
  },
  {
    "permissionId": 56,
    "name": "employee.attendance.create",
    "description": "Permission for employee.attendance.create"
  },
  {
    "permissionId": 57,
    "name": "employee.leave.create",
    "description": "Permission for employee.leave.create"
  },
  {
    "permissionId": 58,
    "name": "employee.leave.read",
    "description": "Permission for employee.leave.read"
  },
  {
    "permissionId": 59,
    "name": "employee.leave.cancel",
    "description": "Permission for employee.leave.cancel"
  },
  {
    "permissionId": 60,
    "name": "employee.payslip.read",
    "description": "Permission for employee.payslip.read"
  },
  {
    "permissionId": 61,
    "name": "employee.payslip.download",
    "description": "Permission for employee.payslip.download"
  },
  {
    "permissionId": 62,
    "name": "employee.document.read",
    "description": "Permission for employee.document.read"
  },
  {
    "permissionId": 63,
    "name": "employee.document.upload",
    "description": "Permission for employee.document.upload"
  },
  {
    "permissionId": 64,
    "name": "employee.document.delete",
    "description": "Permission for employee.document.delete"
  },
  {
    "permissionId": 65,
    "name": "employee.asset.read",
    "description": "Permission for employee.asset.read"
  },
  {
    "permissionId": 66,
    "name": "employee.asset.request",
    "description": "Permission for employee.asset.request"
  },
  {
    "permissionId": 67,
    "name": "employee.expense.create",
    "description": "Permission for employee.expense.create"
  },
  {
    "permissionId": 68,
    "name": "employee.expense.read",
    "description": "Permission for employee.expense.read"
  },
  {
    "permissionId": 69,
    "name": "employee.expense.update",
    "description": "Permission for employee.expense.update"
  },
  {
    "permissionId": 70,
    "name": "employee.performance.read",
    "description": "Permission for employee.performance.read"
  },
  {
    "permissionId": 71,
    "name": "employee.performance.self-review.submit",
    "description": "Permission for employee.performance.self-review.submit"
  },
  {
    "permissionId": 72,
    "name": "employee.training.read",
    "description": "Permission for employee.training.read"
  },
  {
    "permissionId": 73,
    "name": "employee.training.complete",
    "description": "Permission for employee.training.complete"
  },
  {
    "permissionId": 74,
    "name": "employee.notification.read",
    "description": "Permission for employee.notification.read"
  },
  {
    "permissionId": 75,
    "name": "employee.notification.update",
    "description": "Permission for employee.notification.update"
  },
  {
    "permissionId": 76,
    "name": "employee.support-ticket.create",
    "description": "Permission for employee.support-ticket.create"
  },
  {
    "permissionId": 77,
    "name": "employee.support-ticket.read",
    "description": "Permission for employee.support-ticket.read"
  },
  {
    "permissionId": 78,
    "name": "employee.support-ticket.update",
    "description": "Permission for employee.support-ticket.update"
  },
  {
    "permissionId": 79,
    "name": "employee.goal.read",
    "description": "Permission for employee.goal.read"
  },
  {
    "permissionId": 80,
    "name": "employee.goal.update",
    "description": "Permission for employee.goal.update"
  },
  {
    "permissionId": 81,
    "name": "employee.schedule.read",
    "description": "Permission for employee.schedule.read"
  },
  {
    "permissionId": 82,
    "name": "employee.announcement.read",
    "description": "Permission for employee.announcement.read"
  },
  {
    "permissionId": 83,
    "name": "payslip.self.read",
    "description": "Permission for payslip.self.read"
  },
  {
    "permissionId": 84,
    "name": "document.self.read",
    "description": "Permission for document.self.read"
  },
  {
    "permissionId": 85,
    "name": "expense.self.read",
    "description": "Permission for expense.self.read"
  },
  {
    "permissionId": 86,
    "name": "performance.self.read",
    "description": "Permission for performance.self.read"
  },
  {
    "permissionId": 87,
    "name": "goal.self.read",
    "description": "Permission for goal.self.read"
  },
  {
    "permissionId": 88,
    "name": "asset.self.read",
    "description": "Permission for asset.self.read"
  }
]
```

---

### List Departments as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/departments`

**Response Payload**:
```json
{
  "success": true,
  "message": "Departments retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": [
    {
      "id": 1,
      "name": "Engineering",
      "code": "ENG",
      "description": "Software development and engineering department"
    },
    {
      "id": 2,
      "name": "RND Flow 1781329922",
      "code": "RND9922",
      "description": "Research and Development integration flow dept"
    },
    {
      "id": 3,
      "name": "RND Flow 1781329940",
      "code": "RND9940",
      "description": "Research and Development integration flow dept"
    },
    {
      "id": 4,
      "name": "RND Flow 1781329956",
      "code": "RND9956",
      "description": "Research and Development integration flow dept"
    },
    {
      "id": 5,
      "name": "RND Flow 1781330147",
      "code": "RND0147",
      "description": "Research and Development integration flow dept"
    },
    {
      "id": 6,
      "name": "RND Flow 1781331577",
      "code": "RND1577",
      "description": "Research and Development integration flow dept"
    },
    {
      "id": 7,
      "name": "RND Flow 1781340805",
      "code": "RND0805",
      "description": "Research and Development integration flow dept"
    }
  ]
}
```

---

### List Employees as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/employees`

**Response Payload**:
```json
{
  "success": true,
  "message": "Employees list retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": [
    {
      "address": null,
      "annualSalary": 75000.0,
      "department": "HR",
      "designation": "HR Specialist",
      "dob": null,
      "email": "alicesmith@example.com",
      "emergencyContact": null,
      "employeeId": null,
      "employmentType": null,
      "fullName": "Alice Smith",
      "gender": null,
      "id": 3,
      "joiningDate": "2026-06-10",
      "location": "New York",
      "manager": null,
      "phone": null,
      "status": null
    },
    {
      "address": null,
      "annualSalary": 80000.0,
      "department": "Engineering",
      "designation": "QA Engineer",
      "dob": null,
      "email": "flow_emp_1781330422@company.com",
      "emergencyContact": null,
      "employeeId": null,
      "employmentType": null,
      "fullName": "Flow Register Test",
      "gender": "MALE",
      "id": 8,
      "joiningDate": "2026-06-13",
      "location": "Bangalore",
      "manager": null,
      "phone": "9998887776",
      "status": "ACTIVE"
    },
    {
      "address": "Chennai, Tamil Nadu",
      "annualSalary": 85000.0,
      "department": "Engineering",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "onboard_emp_1781340790@company.com",
      "emergencyContact": "9876543211",
      "employeeId": "EMP028",
      "employmentType": "FULL_TIME",
      "fullName": "John Doe 1781340790",
      "gender": "MALE",
      "id": 30,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9876543210",
      "status": "ACTIVE"
    },
    {
      "address": "Bangalore, Karnataka",
      "annualSalary": 85000.0,
      "department": "Engineering",
      "designation": "Software Engineer",
      "dob": "1990-01-01",
      "email": "emssuperadmin@gmail.com",
      "emergencyContact": "9876543213",
      "employeeId": "EMP005",
      "employmentType": "FULL_TIME",
      "fullName": "Super Admin",
      "gender": "MALE",
      "id": 1,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "9876543212",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "HR Department",
      "designation": "HR",
      "dob": "1990-01-01",
      "email": "hr@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP016",
      "employmentType": "FULL_TIME",
      "fullName": "HR Register User",
      "gender": "MALE",
      "id": 14,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "1234567890",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Engineering",
      "designation": "MANAGER",
      "dob": "1990-01-01",
      "email": "rahul.verma@emscompany.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP018",
      "employmentType": "FULL_TIME",
      "fullName": "Rahul Verma",
      "gender": "MALE",
      "id": 15,
      "joiningDate": "2026-06-10",
      "location": "Hyderabad",
      "manager": null,
      "phone": "9988776655",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Engineering",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "newemployee7@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP019",
      "employmentType": "FULL_TIME",
      "fullName": "Test Register User",
      "gender": "MALE",
      "id": 16,
      "joiningDate": "2026-06-10",
      "location": "Bangalore",
      "manager": null,
      "phone": "1234567890",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Admin",
      "designation": "ADMIN",
      "dob": "1990-01-01",
      "email": "admin@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP013",
      "employmentType": "FULL_TIME",
      "fullName": "Admin User",
      "gender": "MALE",
      "id": 17,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550012",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "QA",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "regtest@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP007",
      "employmentType": "FULL_TIME",
      "fullName": "Register Test User",
      "gender": "MALE",
      "id": 18,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "0987654321",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 30000.0,
      "department": "IT",
      "designation": "admin",
      "dob": "1990-01-01",
      "email": "david@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP006",
      "employmentType": "FULL_TIME",
      "fullName": "David",
      "gender": "MALE",
      "id": 2,
      "joiningDate": "2025-06-10",
      "location": "Headquarters",
      "manager": {
        "address": "Bangalore, Karnataka",
        "annualSalary": 85000.0,
        "department": "Engineering",
        "designation": "Software Engineer",
        "dob": "1990-01-01",
        "email": "emssuperadmin@gmail.com",
        "emergencyContact": "9876543213",
        "employeeId": "EMP005",
        "employmentType": "FULL_TIME",
        "fullName": "Super Admin",
        "gender": "MALE",
        "id": 1,
        "joiningDate": "2026-06-10",
        "location": "Headquarters",
        "manager": null,
        "phone": "9876543212",
        "status": "ACTIVE"
      },
      "phone": "1234567890",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 80000.0,
      "department": "Engineering",
      "designation": "QA Engineer",
      "dob": "1990-01-01",
      "email": "flow_emp_1781330458@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP022",
      "employmentType": "FULL_TIME",
      "fullName": "Flow Register Test",
      "gender": "MALE",
      "id": 9,
      "joiningDate": "2026-06-13",
      "location": "Bangalore",
      "manager": null,
      "phone": "9998887776",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Engineering",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "newemployee8@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP020",
      "employmentType": "FULL_TIME",
      "fullName": "Test Custom JSON ID",
      "gender": "MALE",
      "id": 19,
      "joiningDate": "2026-06-10",
      "location": "Bangalore",
      "manager": null,
      "phone": "1234567890",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 80000.0,
      "department": "Engineering",
      "designation": "QA Engineer",
      "dob": "1990-01-01",
      "email": "flow_emp_1781331583@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP023",
      "employmentType": "FULL_TIME",
      "fullName": "Flow Register Test",
      "gender": "MALE",
      "id": 11,
      "joiningDate": "2026-06-13",
      "location": "Bangalore",
      "manager": null,
      "phone": "9998887776",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Super admin",
      "designation": "SUPER_ADMIN",
      "dob": "1990-01-01",
      "email": "superadmin@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP009",
      "employmentType": "FULL_TIME",
      "fullName": "Super admin User",
      "gender": "MALE",
      "id": 20,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550007",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Engineering",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "onboard_emp_1781334248@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP025",
      "employmentType": "FULL_TIME",
      "fullName": "John Doe 1781334248",
      "gender": "MALE",
      "id": 21,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9876543210",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Software Development",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "arun.kumar@emscompany.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP017",
      "employmentType": "FULL_TIME",
      "fullName": "Arun Kumar",
      "gender": "MALE",
      "id": 22,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9876543210",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Finance Department",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "finance.register1781241236@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP014",
      "employmentType": "FULL_TIME",
      "fullName": "Finance Register User",
      "gender": "MALE",
      "id": 23,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "1234567890",
      "status": "ACTIVE"
    },
    {
      "address": "Chennai, Tamil Nadu",
      "annualSalary": 85000.0,
      "department": "Engineering",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "onboard_emp_1781334271@company.com",
      "emergencyContact": "9876543211",
      "employeeId": "EMP026",
      "employmentType": "FULL_TIME",
      "fullName": "John Doe 1781334271",
      "gender": "MALE",
      "id": 12,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9876543210",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "IT",
      "designation": "HR",
      "dob": "1990-01-01",
      "email": "subhamano3006@gmail.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP004",
      "employmentType": "FULL_TIME",
      "fullName": "Subashini",
      "gender": "MALE",
      "id": 24,
      "joiningDate": "2026-06-10",
      "location": "TRL",
      "manager": null,
      "phone": "9360182757",
      "status": "ACTIVE"
    },
    {
      "address": "Chennai, Tamil Nadu",
      "annualSalary": 85000.0,
      "department": "Engineering",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "onboard_emp_1781334442@company.com",
      "emergencyContact": "9876543211",
      "employeeId": "EMP027",
      "employmentType": "FULL_TIME",
      "fullName": "John Doe 1781334442",
      "gender": "MALE",
      "id": 13,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9876543210",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "IT",
      "designation": "ADMIN",
      "dob": "1990-01-01",
      "email": "abcd12@gmail.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP003",
      "employmentType": "FULL_TIME",
      "fullName": "karthick G",
      "gender": "MALE",
      "id": 25,
      "joiningDate": "2026-06-10",
      "location": "TRL",
      "manager": null,
      "phone": "09938362013",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Engineering",
      "designation": "ADMIN",
      "dob": "1990-01-01",
      "email": "rahul.kumar@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP021",
      "employmentType": "FULL_TIME",
      "fullName": "Rahul Kumar",
      "gender": "MALE",
      "id": 26,
      "joiningDate": "2026-06-10",
      "location": "Bangalore",
      "manager": null,
      "phone": "9876543210",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Finance Department",
      "designation": "FINANCE",
      "dob": "1990-01-01",
      "email": "finance@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP015",
      "employmentType": "FULL_TIME",
      "fullName": "Finance Register User",
      "gender": "MALE",
      "id": 27,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "1234567890",
      "status": "ACTIVE"
    },
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
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "Manager",
      "designation": "MANAGER",
      "dob": "1990-01-01",
      "email": "manager@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP011",
      "employmentType": "FULL_TIME",
      "fullName": "Manager User",
      "gender": "MALE",
      "id": 29,
      "joiningDate": "2026-06-10",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550010",
      "status": "ACTIVE"
    },
    {
      "address": null,
      "annualSalary": 80000.0,
      "department": "Engineering",
      "designation": "QA Engineer",
      "dob": null,
      "email": "flow_emp_1781346327@company.com",
      "emergencyContact": null,
      "employeeId": null,
      "employmentType": null,
      "fullName": "Flow Register Test",
      "gender": "MALE",
      "id": 32,
      "joiningDate": "2026-06-13",
      "location": "Bangalore",
      "manager": null,
      "phone": "9998887776",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 80000.0,
      "department": "Engineering",
      "designation": "QA Engineer",
      "dob": "1990-01-01",
      "email": "flow_emp_1781346341@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP029",
      "employmentType": "FULL_TIME",
      "fullName": "Flow Register Test",
      "gender": "MALE",
      "id": 33,
      "joiningDate": "2026-06-13",
      "location": "Bangalore",
      "manager": null,
      "phone": "9998887776",
      "status": "ACTIVE"
    },
    {
      "address": null,
      "annualSalary": 90000.0,
      "department": "Engineering",
      "designation": "Software Engineer",
      "dob": null,
      "email": "post_test_1781496485@company.com",
      "emergencyContact": null,
      "employeeId": null,
      "employmentType": null,
      "fullName": "Test POST Employee 1781496485",
      "gender": null,
      "id": 34,
      "joiningDate": "2026-06-15",
      "location": null,
      "manager": null,
      "phone": null,
      "status": "ACTIVE"
    },
    {
      "address": null,
      "annualSalary": 90000.0,
      "department": "Engineering",
      "designation": "Software Engineer",
      "dob": null,
      "email": "post_test_1781496545@company.com",
      "emergencyContact": null,
      "employeeId": null,
      "employmentType": null,
      "fullName": "Test POST Employee 1781496545",
      "gender": null,
      "id": 35,
      "joiningDate": "2026-06-15",
      "location": null,
      "manager": null,
      "phone": null,
      "status": "ACTIVE"
    },
    {
      "address": null,
      "annualSalary": 90000.0,
      "department": "Engineering",
      "designation": "Software Engineer",
      "dob": null,
      "email": "post_test_1781496607@company.com",
      "emergencyContact": null,
      "employeeId": null,
      "employmentType": null,
      "fullName": "Test POST Employee 1781496607",
      "gender": null,
      "id": 36,
      "joiningDate": "2026-06-15",
      "location": null,
      "manager": null,
      "phone": null,
      "status": "ACTIVE"
    },
    {
      "address": null,
      "annualSalary": 90000.0,
      "department": "Engineering",
      "designation": "Software Engineer",
      "dob": null,
      "email": "post_test_1781497059@company.com",
      "emergencyContact": null,
      "employeeId": null,
      "employmentType": null,
      "fullName": "Test POST Employee 1781497059",
      "gender": null,
      "id": 37,
      "joiningDate": "2026-06-15",
      "location": null,
      "manager": null,
      "phone": null,
      "status": "ACTIVE"
    },
    {
      "address": null,
      "annualSalary": 90000.0,
      "department": "Engineering",
      "designation": "Software Engineer",
      "dob": null,
      "email": "post_test_1781497296@company.com",
      "emergencyContact": null,
      "employeeId": null,
      "employmentType": null,
      "fullName": "Test POST Employee 1781497296",
      "gender": null,
      "id": 38,
      "joiningDate": "2026-06-15",
      "location": null,
      "manager": null,
      "phone": null,
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "IT",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "manoj_sep_1781499896@gmail.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP035",
      "employmentType": "FULL_TIME",
      "fullName": "Manoj Separate",
      "gender": "MALE",
      "id": 44,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9128909848",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "IT",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "manoj@gmail.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP030",
      "employmentType": "FULL_TIME",
      "fullName": "Manoj",
      "gender": "MALE",
      "id": 39,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9128909848",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "IT",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "manoj_test_1781498766@gmail.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP031",
      "employmentType": "FULL_TIME",
      "fullName": "Manoj Test",
      "gender": "MALE",
      "id": 40,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9128909848",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "IT",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "manoj_test_1781498927@gmail.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP032",
      "employmentType": "FULL_TIME",
      "fullName": "Manoj Test",
      "gender": "MALE",
      "id": 41,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9128909848",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "IT",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "manoj_test@gmail.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP033",
      "employmentType": "FULL_TIME",
      "fullName": "Manoj Test",
      "gender": "MALE",
      "id": 42,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9128909848",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "IT",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "manoj_sep_1781499541@gmail.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP034",
      "employmentType": "FULL_TIME",
      "fullName": "Manoj Separate",
      "gender": "MALE",
      "id": 43,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9128909848",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "IT",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "manoj_test_1781500676@gmail.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP036",
      "employmentType": "FULL_TIME",
      "fullName": "Manoj Test",
      "gender": "MALE",
      "id": 45,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9128909848",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "IT",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "manoj_sep_1781501344@gmail.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP037",
      "employmentType": "FULL_TIME",
      "fullName": "Manoj Separate",
      "gender": "MALE",
      "id": 46,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9128909848",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "IT",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "manoj_sep_1781501352@gmail.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP038",
      "employmentType": "FULL_TIME",
      "fullName": "Manoj Separate",
      "gender": "MALE",
      "id": 47,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9128909848",
      "status": "ACTIVE"
    },
    {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "department": "IT",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "manoj_test_1781501358@gmail.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP039",
      "employmentType": "FULL_TIME",
      "fullName": "Manoj Test",
      "gender": "MALE",
      "id": 48,
      "joiningDate": "2026-06-10",
      "location": "Chennai",
      "manager": null,
      "phone": "9128909848",
      "status": "ACTIVE"
    }
  ]
}
```

---

### List Attendance as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/attendance`

**Response Payload**:
```json
{
  "success": true,
  "message": "Attendance records retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": [
    {
      "date": "2026-06-10",
      "employee": {
        "address": "Bangalore, Karnataka",
        "annualSalary": 85000.0,
        "department": "Engineering",
        "designation": "Software Engineer",
        "dob": "1990-01-01",
        "email": "emssuperadmin@gmail.com",
        "emergencyContact": "9876543213",
        "employeeId": "EMP005",
        "employmentType": "FULL_TIME",
        "fullName": "Super Admin",
        "gender": "MALE",
        "id": 1,
        "joiningDate": "2026-06-10",
        "location": "Headquarters",
        "manager": null,
        "phone": "9876543212",
        "status": "ACTIVE"
      },
      "id": 1,
      "notes": "On-time arrival",
      "punchInTime": "09:00:00",
      "punchOutTime": "17:00:00",
      "status": "Present",
      "workingHours": "8h 00m"
    },
    {
      "date": "2026-06-13",
      "employee": {
        "address": "Bangalore, Karnataka",
        "annualSalary": 85000.0,
        "department": "Engineering",
        "designation": "Software Engineer",
        "dob": "1990-01-01",
        "email": "emssuperadmin@gmail.com",
        "emergencyContact": "9876543213",
        "employeeId": "EMP005",
        "employmentType": "FULL_TIME",
        "fullName": "Super Admin",
        "gender": "MALE",
        "id": 1,
        "joiningDate": "2026-06-10",
        "location": "Headquarters",
        "manager": null,
        "phone": "9876543212",
        "status": "ACTIVE"
      },
      "id": 10,
      "notes": "Completed shift task",
      "punchInTime": "15:49:35",
      "punchOutTime": "15:49:35",
      "status": "Late",
      "workingHours": "0h 00m"
    },
    {
      "date": "2026-06-15",
      "employee": {
        "address": "Bangalore, Karnataka",
        "annualSalary": 85000.0,
        "department": "Engineering",
        "designation": "Software Engineer",
        "dob": "1990-01-01",
        "email": "emssuperadmin@gmail.com",
        "emergencyContact": "9876543213",
        "employeeId": "EMP005",
        "employmentType": "FULL_TIME",
        "fullName": "Super Admin",
        "gender": "MALE",
        "id": 1,
        "joiningDate": "2026-06-10",
        "location": "Headquarters",
        "manager": null,
        "phone": "9876543212",
        "status": "ACTIVE"
      },
      "id": 15,
      "notes": "check in",
      "punchInTime": "09:59:35",
      "punchOutTime": null,
      "status": "Late",
      "workingHours": null
    },
    {
      "date": "2026-06-15",
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "department": "IT",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "manoj_test_1781498766@gmail.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP031",
        "employmentType": "FULL_TIME",
        "fullName": "Manoj Test",
        "gender": "MALE",
        "id": 40,
        "joiningDate": "2026-06-10",
        "location": "Chennai",
        "manager": null,
        "phone": "9128909848",
        "status": "ACTIVE"
      },
      "id": 18,
      "notes": "Testing my first check-in",
      "punchInTime": "10:16:07",
      "punchOutTime": null,
      "status": "Late",
      "workingHours": null
    },
    {
      "date": "2026-06-15",
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "department": "IT",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "manoj_test_1781498927@gmail.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP032",
        "employmentType": "FULL_TIME",
        "fullName": "Manoj Test",
        "gender": "MALE",
        "id": 41,
        "joiningDate": "2026-06-10",
        "location": "Chennai",
        "manager": null,
        "phone": "9128909848",
        "status": "ACTIVE"
      },
      "id": 19,
      "notes": "Testing my first check-in",
      "punchInTime": "10:18:48",
      "punchOutTime": null,
      "status": "Late",
      "workingHours": null
    },
    {
      "date": "2026-06-15",
      "employee": {
        "address": null,
        "annualSalary": 90000.0,
        "department": "Engineering",
        "designation": "Software Engineer",
        "dob": null,
        "email": "post_test_1781496485@company.com",
        "emergencyContact": null,
        "employeeId": null,
        "employmentType": null,
        "fullName": "Test POST Employee 1781496485",
        "gender": null,
        "id": 34,
        "joiningDate": "2026-06-15",
        "location": null,
        "manager": null,
        "phone": null,
        "status": "ACTIVE"
      },
      "id": 21,
      "notes": "Checking in using numeric employeeId",
      "punchInTime": "10:29:02",
      "punchOutTime": null,
      "status": "Late",
      "workingHours": null
    },
    {
      "date": "2026-06-15",
      "employee": {
        "address": null,
        "annualSalary": 90000.0,
        "department": "Engineering",
        "designation": "Software Engineer",
        "dob": null,
        "email": "post_test_1781496545@company.com",
        "emergencyContact": null,
        "employeeId": null,
        "employmentType": null,
        "fullName": "Test POST Employee 1781496545",
        "gender": null,
        "id": 35,
        "joiningDate": "2026-06-15",
        "location": null,
        "manager": null,
        "phone": null,
        "status": "ACTIVE"
      },
      "id": 22,
      "notes": "Checking in using numeric employeeId",
      "punchInTime": "10:34:57",
      "punchOutTime": null,
      "status": "Late",
      "workingHours": null
    },
    {
      "date": "2026-06-15",
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "department": "IT",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "manoj_test_1781500676@gmail.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP036",
        "employmentType": "FULL_TIME",
        "fullName": "Manoj Test",
        "gender": "MALE",
        "id": 45,
        "joiningDate": "2026-06-10",
        "location": "Chennai",
        "manager": null,
        "phone": "9128909848",
        "status": "ACTIVE"
      },
      "id": 23,
      "notes": "Testing my first check-in",
      "punchInTime": "10:47:57",
      "punchOutTime": null,
      "status": "Late",
      "workingHours": null
    },
    {
      "date": "2026-06-15",
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "department": "IT",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "manoj@gmail.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP030",
        "employmentType": "FULL_TIME",
        "fullName": "Manoj",
        "gender": "MALE",
        "id": 39,
        "joiningDate": "2026-06-10",
        "location": "Chennai",
        "manager": null,
        "phone": "9128909848",
        "status": "ACTIVE"
      },
      "id": 24,
      "notes": "test",
      "punchInTime": "10:52:47",
      "punchOutTime": null,
      "status": "Late",
      "workingHours": null
    },
    {
      "date": "2026-06-15",
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "department": "IT",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "manoj_sep_1781499541@gmail.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP034",
        "employmentType": "FULL_TIME",
        "fullName": "Manoj Separate",
        "gender": "MALE",
        "id": 43,
        "joiningDate": "2026-06-10",
        "location": "Chennai",
        "manager": null,
        "phone": "9128909848",
        "status": "ACTIVE"
      },
      "id": 25,
      "notes": "test",
      "punchInTime": "10:54:50",
      "punchOutTime": null,
      "status": "Late",
      "workingHours": null
    },
    {
      "date": "2026-06-15",
      "employee": {
        "address": null,
        "annualSalary": 90000.0,
        "department": "Engineering",
        "designation": "Software Engineer",
        "dob": null,
        "email": "post_test_1781497059@company.com",
        "emergencyContact": null,
        "employeeId": null,
        "employmentType": null,
        "fullName": "Test POST Employee 1781497059",
        "gender": null,
        "id": 37,
        "joiningDate": "2026-06-15",
        "location": null,
        "manager": null,
        "phone": null,
        "status": "ACTIVE"
      },
      "id": 26,
      "notes": "Checking in using numeric employeeId parameter",
      "punchInTime": "10:59:05",
      "punchOutTime": null,
      "status": "Late",
      "workingHours": null
    },
    {
      "date": "2026-06-15",
      "employee": {
        "address": null,
        "annualSalary": 90000.0,
        "department": "Engineering",
        "designation": "Software Engineer",
        "dob": null,
        "email": "post_test_1781497296@company.com",
        "emergencyContact": null,
        "employeeId": null,
        "employmentType": null,
        "fullName": "Test POST Employee 1781497296",
        "gender": null,
        "id": 38,
        "joiningDate": "2026-06-15",
        "location": null,
        "manager": null,
        "phone": null,
        "status": "ACTIVE"
      },
      "id": 27,
      "notes": "Checking out using numeric employeeId parameter",
      "punchInTime": "10:59:13",
      "punchOutTime": "10:59:13",
      "status": "Late",
      "workingHours": "0h 00m"
    },
    {
      "date": "2026-06-15",
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "department": "IT",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "manoj_test_1781501358@gmail.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP039",
        "employmentType": "FULL_TIME",
        "fullName": "Manoj Test",
        "gender": "MALE",
        "id": 48,
        "joiningDate": "2026-06-10",
        "location": "Chennai",
        "manager": null,
        "phone": "9128909848",
        "status": "ACTIVE"
      },
      "id": 28,
      "notes": "Testing my first check-in",
      "punchInTime": "10:59:18",
      "punchOutTime": null,
      "status": "Late",
      "workingHours": null
    },
    {
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
      "punchOutTime": "11:08:27",
      "status": "Late",
      "workingHours": "0h 00m"
    }
  ]
}
```

---

### List Leave Types as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/leave-types`

**Response Payload**:
```json
{
  "success": true,
  "message": "Leave types retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": [
    {
      "id": 1,
      "name": "Annual Leave",
      "description": "Paid time off for holidays/vacation",
      "defaultDays": 20,
      "active": true
    },
    {
      "id": 2,
      "name": "Maternity Leave Updated",
      "description": "Paid parental leave for new mothers",
      "defaultDays": 100,
      "active": true
    }
  ]
}
```

---

### List Recruitment Jobs as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/recruitments/jobs`

**Response Payload**:
```json
{
  "success": true,
  "message": "Jobs retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": [
    {
      "createdAt": "2026-06-11T14:08:11.862384",
      "department": "Engineering",
      "description": "Design and build enterprise applications",
      "id": 1,
      "location": "Headquarters",
      "requirements": "Java, Spring Boot, PostgreSQL, Redis",
      "salaryRange": "80,000 - 120,000 USD",
      "status": "PUBLISHED",
      "title": "Software Engineer",
      "updatedAt": "2026-06-11T14:08:11.862403"
    },
    {
      "createdAt": "2026-06-11T14:08:50.971955",
      "department": "Engineering",
      "description": "Design and build enterprise applications",
      "id": 2,
      "location": "Headquarters",
      "requirements": "Java, Spring Boot, PostgreSQL, Redis",
      "salaryRange": "80,000 - 120,000 USD",
      "status": "PUBLISHED",
      "title": "Software Engineer",
      "updatedAt": "2026-06-11T14:08:50.971979"
    }
  ]
}
```

---

### List Training Courses as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/trainings/courses`

**Response Payload**:
```json
{
  "success": true,
  "message": "Training courses retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": [
    {
      "category": "Engineering",
      "createdAt": "2026-06-11T14:08:11.936524",
      "description": "Introduction to Spring Boot framework",
      "durationHours": 16,
      "id": 1,
      "status": "ACTIVE",
      "title": "Spring Boot Basics",
      "updatedAt": "2026-06-11T14:08:11.936548"
    },
    {
      "category": "Engineering",
      "createdAt": "2026-06-11T14:08:50.994979",
      "description": "Introduction to Spring Boot framework",
      "durationHours": 16,
      "id": 2,
      "status": "ACTIVE",
      "title": "Spring Boot Basics",
      "updatedAt": "2026-06-11T14:08:50.995004"
    },
    {
      "category": "Engineering",
      "createdAt": "2026-06-13T15:06:20.832571",
      "description": "REST specs",
      "durationHours": 10,
      "id": 3,
      "status": "ACTIVE",
      "title": "Rest API design",
      "updatedAt": "2026-06-13T15:06:20.832574"
    }
  ]
}
```

---

### List Onboarding Records as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/onboarding-records`

**Response Payload**:
```json
{
  "success": true,
  "message": "Onboarding records retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": [
    {
      "tasks": [
        {
          "completedAt": null,
          "description": "Please upload your Government ID and Bank Account details.",
          "dueDate": "2026-06-20",
          "id": 1,
          "status": "PENDING",
          "title": "Submit Personal Documents"
        },
        {
          "completedAt": null,
          "description": "Select hardware requirements (Laptop size, Keyboard, Monitor).",
          "dueDate": "2026-06-20",
          "id": 2,
          "status": "PENDING",
          "title": "Request Hardware/Assets"
        },
        {
          "completedAt": null,
          "description": "Watch compliance and information security webinars.",
          "dueDate": "2026-06-20",
          "id": 3,
          "status": "PENDING",
          "title": "Complete Compliance Trainings"
        },
        {
          "completedAt": null,
          "description": "Setup a 30-minute sync meeting with your direct reporting manager.",
          "dueDate": "2026-06-20",
          "id": 4,
          "status": "PENDING",
          "title": "Schedule Manager Sync"
        }
      ],
      "assets": [],
      "trainings": [
        {
          "completedAt": null,
          "courseName": "Code of Business Conduct",
          "id": 1,
          "status": "ASSIGNED"
        },
        {
          "completedAt": null,
          "courseName": "Information Security & Privacy Awareness",
          "id": 2,
          "status": "ASSIGNED"
        },
        {
          "completedAt": null,
          "courseName": "Workplace Health and Safety",
          "id": 3,
          "status": "ASSIGNED"
        }
      ],
      "completionDate": "2026-06-13",
      "createdAt": "2026-06-13T12:34:32.762234",
      "documents": [
        {
          "documentType": "Aadhar Card",
          "downloadUrl": "http://localhost:8080/api/documents/download/1781334272958",
          "fileName": "aadhar_card.pdf",
          "fileType": "application/pdf",
          "id": 1,
          "uploadedAt": "2026-06-13T12:34:32.960069",
          "verificationNotes": null,
          "verificationStatus": "PENDING"
        }
      ],
      "employeeEmail": "onboard_emp_1781334271@company.com",
      "employeeId": 12,
      "employeeName": "John Doe 1781334271",
      "id": 1,
      "startDate": "2026-06-13",
      "status": "UNDER_REVIEW",
      "updatedAt": "2026-06-13T12:34:33.019921"
    },
    {
      "tasks": [
        {
          "completedAt": null,
          "description": "Please upload your Government ID and Bank Account details.",
          "dueDate": "2026-06-20",
          "id": 5,
          "status": "PENDING",
          "title": "Submit Personal Documents"
        },
        {
          "completedAt": null,
          "description": "Select hardware requirements (Laptop size, Keyboard, Monitor).",
          "dueDate": "2026-06-20",
          "id": 6,
          "status": "PENDING",
          "title": "Request Hardware/Assets"
        },
        {
          "completedAt": null,
          "description": "Watch compliance and information security webinars.",
          "dueDate": "2026-06-20",
          "id": 7,
          "status": "PENDING",
          "title": "Complete Compliance Trainings"
        },
        {
          "completedAt": null,
          "description": "Setup a 30-minute sync meeting with your direct reporting manager.",
          "dueDate": "2026-06-20",
          "id": 8,
          "status": "PENDING",
          "title": "Schedule Manager Sync"
        }
      ],
      "assets": [],
      "trainings": [
        {
          "completedAt": null,
          "courseName": "Code of Business Conduct",
          "id": 4,
          "status": "ASSIGNED"
        },
        {
          "completedAt": null,
          "courseName": "Information Security & Privacy Awareness",
          "id": 5,
          "status": "ASSIGNED"
        },
        {
          "completedAt": null,
          "courseName": "Workplace Health and Safety",
          "id": 6,
          "status": "ASSIGNED"
        }
      ],
      "completionDate": "2026-06-13",
      "createdAt": "2026-06-13T12:37:24.38558",
      "documents": [
        {
          "documentType": "Aadhar Card",
          "downloadUrl": "http://localhost:8080/api/documents/download/1781334444583",
          "fileName": "aadhar_card.pdf",
          "fileType": "application/pdf",
          "id": 2,
          "uploadedAt": "2026-06-13T12:37:24.585837",
          "verificationNotes": null,
          "verificationStatus": "PENDING"
        }
      ],
      "employeeEmail": "onboard_emp_1781334442@company.com",
      "employeeId": 13,
      "employeeName": "John Doe 1781334442",
      "id": 2,
      "startDate": "2026-06-13",
      "status": "UNDER_REVIEW",
      "updatedAt": "2026-06-13T12:37:24.640492"
    },
    {
      "tasks": [
        {
          "completedAt": null,
          "description": "Please upload your Government ID and Bank Account details.",
          "dueDate": "2026-06-20",
          "id": 13,
          "status": "PENDING",
          "title": "Submit Personal Documents"
        },
        {
          "completedAt": null,
          "description": "Select hardware requirements (Laptop size, Keyboard, Monitor).",
          "dueDate": "2026-06-20",
          "id": 14,
          "status": "PENDING",
          "title": "Request Hardware/Assets"
        },
        {
          "completedAt": null,
          "description": "Watch compliance and information security webinars.",
          "dueDate": "2026-06-20",
          "id": 15,
          "status": "PENDING",
          "title": "Complete Compliance Trainings"
        },
        {
          "completedAt": null,
          "description": "Setup a 30-minute sync meeting with your direct reporting manager.",
          "dueDate": "2026-06-20",
          "id": 16,
          "status": "PENDING",
          "title": "Schedule Manager Sync"
        }
      ],
      "assets": [],
      "trainings": [
        {
          "completedAt": null,
          "courseName": "Code of Business Conduct",
          "id": 10,
          "status": "ASSIGNED"
        },
        {
          "completedAt": null,
          "courseName": "Information Security & Privacy Awareness",
          "id": 11,
          "status": "ASSIGNED"
        },
        {
          "completedAt": null,
          "courseName": "Workplace Health and Safety",
          "id": 12,
          "status": "ASSIGNED"
        }
      ],
      "completionDate": "2026-06-13",
      "createdAt": "2026-06-13T14:23:11.930225",
      "documents": [
        {
          "documentType": "Aadhar Card",
          "downloadUrl": "http://localhost:8080/api/documents/download/1781340792129",
          "fileName": "aadhar_card.pdf",
          "fileType": "application/pdf",
          "id": 3,
          "uploadedAt": "2026-06-13T14:23:12.131722",
          "verificationNotes": null,
          "verificationStatus": "PENDING"
        }
      ],
      "employeeEmail": "onboard_emp_1781340790@company.com",
      "employeeId": 30,
      "employeeName": "John Doe 1781340790",
      "id": 4,
      "startDate": "2026-06-13",
      "status": "UNDER_REVIEW",
      "updatedAt": "2026-06-13T14:23:12.190731"
    },
    {
      "tasks": [
        {
          "completedAt": null,
          "description": "Please upload your Government ID and Bank Account details.",
          "dueDate": "2026-06-20",
          "id": 9,
          "status": "PENDING",
          "title": "Submit Personal Documents"
        },
        {
          "completedAt": null,
          "description": "Select hardware requirements (Laptop size, Keyboard, Monitor).",
          "dueDate": "2026-06-20",
          "id": 10,
          "status": "PENDING",
          "title": "Request Hardware/Assets"
        },
        {
          "completedAt": null,
          "description": "Watch compliance and information security webinars.",
          "dueDate": "2026-06-20",
          "id": 11,
          "status": "PENDING",
          "title": "Complete Compliance Trainings"
        },
        {
          "completedAt": null,
          "description": "Setup a 30-minute sync meeting with your direct reporting manager.",
          "dueDate": "2026-06-20",
          "id": 12,
          "status": "PENDING",
          "title": "Schedule Manager Sync"
        }
      ],
      "assets": [
        {
          "assetName": "Self-Service Workstation Laptop",
          "assignedAt": null,
          "id": 1,
          "serialNumber": "SN-DEV-8877",
          "status": "SERVICE_REQUESTED"
        }
      ],
      "trainings": [
        {
          "completedAt": null,
          "courseName": "Code of Business Conduct",
          "id": 7,
          "status": "ASSIGNED"
        },
        {
          "completedAt": null,
          "courseName": "Information Security & Privacy Awareness",
          "id": 8,
          "status": "ASSIGNED"
        },
        {
          "completedAt": null,
          "courseName": "Workplace Health and Safety",
          "id": 9,
          "status": "ASSIGNED"
        }
      ],
      "completionDate": "2026-06-13",
      "createdAt": "2026-06-13T12:39:13.160386",
      "documents": [
        {
          "documentType": "PASSPORT",
          "downloadUrl": "http://localhost:8080/api/documents/download/1781342819483",
          "fileName": "test_onboard.pdf",
          "fileType": "application/pdf",
          "id": 4,
          "uploadedAt": "2026-06-13T14:56:59.486222",
          "verificationNotes": null,
          "verificationStatus": "PENDING"
        },
        {
          "documentType": "PASSPORT",
          "downloadUrl": "http://localhost:8080/api/documents/download/1781343323230",
          "fileName": "test_onboard.pdf",
          "fileType": "application/pdf",
          "id": 5,
          "uploadedAt": "2026-06-13T15:05:23.230753",
          "verificationNotes": null,
          "verificationStatus": "PENDING"
        },
        {
          "documentType": "PASSPORT",
          "downloadUrl": "http://localhost:8080/api/documents/download/1781343380376",
          "fileName": "test_onboard.pdf",
          "fileType": "application/pdf",
          "id": 6,
          "uploadedAt": "2026-06-13T15:06:20.376803",
          "verificationNotes": null,
          "verificationStatus": "PENDING"
        },
        {
          "documentType": "PASSPORT",
          "downloadUrl": "http://localhost:8080/api/documents/download/1781344931374",
          "fileName": "test_onboard.pdf",
          "fileType": "application/pdf",
          "id": 7,
          "uploadedAt": "2026-06-13T15:32:11.376663",
          "verificationNotes": null,
          "verificationStatus": "PENDING"
        },
        {
          "documentType": "PASSPORT",
          "downloadUrl": "http://localhost:8080/api/documents/download/1781345974523",
          "fileName": "test_onboard.pdf",
          "fileType": "application/pdf",
          "id": 8,
          "uploadedAt": "2026-06-13T15:49:34.525799",
          "verificationNotes": null,
          "verificationStatus": "PENDING"
        }
      ],
      "employeeEmail": "emssuperadmin@gmail.com",
      "employeeId": 1,
      "employeeName": "Super Admin",
      "id": 3,
      "startDate": "2026-06-13",
      "status": "UNDER_REVIEW",
      "updatedAt": "2026-06-13T15:49:34.602686"
    },
    {
      "tasks": [
        {
          "completedAt": null,
          "description": "Please upload your Government ID and Bank Account details.",
          "dueDate": "2026-06-22",
          "id": 17,
          "status": "PENDING",
          "title": "Submit Personal Documents"
        },
        {
          "completedAt": null,
          "description": "Select hardware requirements (Laptop size, Keyboard, Monitor).",
          "dueDate": "2026-06-22",
          "id": 18,
          "status": "PENDING",
          "title": "Request Hardware/Assets"
        },
        {
          "completedAt": null,
          "description": "Watch compliance and information security webinars.",
          "dueDate": "2026-06-22",
          "id": 19,
          "status": "PENDING",
          "title": "Complete Compliance Trainings"
        },
        {
          "completedAt": null,
          "description": "Setup a 30-minute sync meeting with your direct reporting manager.",
          "dueDate": "2026-06-22",
          "id": 20,
          "status": "PENDING",
          "title": "Schedule Manager Sync"
        }
      ],
      "assets": [],
      "trainings": [
        {
          "completedAt": null,
          "courseName": "Code of Business Conduct",
          "id": 13,
          "status": "ASSIGNED"
        },
        {
          "completedAt": null,
          "courseName": "Information Security & Privacy Awareness",
          "id": 14,
          "status": "ASSIGNED"
        },
        {
          "completedAt": null,
          "courseName": "Workplace Health and Safety",
          "id": 15,
          "status": "ASSIGNED"
        }
      ],
      "completionDate": "2026-06-15",
      "createdAt": "2026-06-15T09:26:10.558525",
      "documents": [
        {
          "documentType": "PASSPORT",
          "downloadUrl": "http://localhost:8080/api/documents/download/1781498129075",
          "fileName": "test_doc.txt",
          "fileType": "text/plain",
          "id": 9,
          "uploadedAt": "2026-06-15T10:05:29.076685",
          "verificationNotes": null,
          "verificationStatus": "PENDING"
        },
        {
          "documentType": "PASSPORT",
          "downloadUrl": "http://localhost:8080/api/documents/download/1781498729281",
          "fileName": "test_doc.txt",
          "fileType": "text/plain",
          "id": 10,
          "uploadedAt": "2026-06-15T10:15:29.286554",
          "verificationNotes": null,
          "verificationStatus": "PENDING"
        },
        {
          "documentType": "PASSPORT",
          "downloadUrl": "http://localhost:8080/api/documents/download/1781501906885",
          "fileName": "test_doc.txt",
          "fileType": "text/plain",
          "id": 11,
          "uploadedAt": "2026-06-15T11:08:26.88857",
          "verificationNotes": null,
          "verificationStatus": "PENDING"
        }
      ],
      "employeeEmail": "employee@company.com",
      "employeeId": 28,
      "employeeName": "Employee User",
      "id": 5,
      "startDate": "2026-06-15",
      "status": "UNDER_REVIEW",
      "updatedAt": "2026-06-15T11:08:26.962473"
    }
  ]
}
```

---

### Onboarding Dashboard as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/onboarding-records/dashboard`

**Response Payload**:
```json
{
  "success": true,
  "message": "Onboarding dashboard statistics retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": {
    "approvedOnboardings": 0,
    "completedOnboardings": 0,
    "completedTasksCount": 0,
    "inProgressOnboardings": 0,
    "pendingOnboardings": 0,
    "pendingVerifications": 11,
    "taskCompletionRate": 0.0,
    "totalOnboardings": 5,
    "totalTasksAssigned": 20
  }
}
```

---

### List Appraisals as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/appraisals`

**Response Payload**:
```json
{
  "success": true,
  "message": "Appraisals retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": []
}
```

---

### Appraisals Dashboard as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/appraisals/dashboard`

**Response Payload**:
```json
{
  "success": true,
  "message": "Appraisal dashboard statistics retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": {
    "appliedIncrements": 0,
    "approvedIncrements": 0,
    "averageIncrementPercentage": 0.0,
    "averageRating": 0.0,
    "finalizedAppraisals": 0,
    "pendingIncrements": 0,
    "pendingManagerReviews": 0,
    "pendingSelfReviews": 0,
    "totalAppraisals": 0,
    "totalIncrements": 0
  }
}
```

---

### List Appraisal Cycles as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/appraisal-cycles`

**Response Payload**:
```json
{
  "success": true,
  "message": "Appraisal cycles retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": [
    {
      "createdAt": "2026-06-10T15:26:15.618954",
      "endDate": "2026-12-31",
      "id": 1,
      "name": "Annual Appraisal Cycle 2026",
      "startDate": "2026-01-01",
      "status": "ACTIVE",
      "updatedAt": "2026-06-10T15:26:15.618964"
    }
  ]
}
```

---

### List Increment Policies as Super Admin (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/increment-policies`

**Response Payload**:
```json
{
  "success": true,
  "message": "Increment policies retrieved successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": [
    {
      "description": "Needs improvement - No increment",
      "id": 1,
      "rating": 1,
      "recommendedPercentage": 0.0
    },
    {
      "description": "Below expectations - 2% baseline increment",
      "id": 2,
      "rating": 2,
      "recommendedPercentage": 2.0
    },
    {
      "description": "Meets expectations - 5% standard increment",
      "id": 3,
      "rating": 3,
      "recommendedPercentage": 5.0
    },
    {
      "description": "Exceeds expectations - 10% high performer increment",
      "id": 4,
      "rating": 4,
      "recommendedPercentage": 10.0
    },
    {
      "description": "Outstanding - 15% top performer increment",
      "id": 5,
      "rating": 5,
      "recommendedPercentage": 15.0
    }
  ]
}
```

---

### Create Employee as Super Admin (POST - 201)
**Request URL**: `http://localhost:8080/api/v1/employees`

**Request Payload**:
```json
{
  "fullName": "Test POST Employee 1781501913",
  "email": "post_test_1781501913@company.com",
  "department": "Engineering",
  "designation": "Software Engineer",
  "annualSalary": 90000,
  "joiningDate": "2026-06-15"
}
```

**Response Payload**:
```json
{
  "success": true,
  "message": "Employee created successfully",
  "timestamp": "2026-06-15T05:38:33Z",
  "data": {
    "address": null,
    "annualSalary": 90000,
    "department": "Engineering",
    "designation": "Software Engineer",
    "dob": null,
    "email": "post_test_1781501913@company.com",
    "emergencyContact": null,
    "employeeId": null,
    "employmentType": null,
    "fullName": "Test POST Employee 1781501913",
    "gender": null,
    "id": 49,
    "joiningDate": "2026-06-15",
    "location": null,
    "manager": null,
    "phone": null,
    "status": "ACTIVE"
  }
}
```

---

### ESS Dashboard (employee.dashboard.read) as Employee (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/employees/me/dashboard`

**Response Payload**:
```json
{
  "performanceRating": 4.5,
  "leaveBalance": 120,
  "attendancePercentage": 96.4,
  "pendingActions": 16,
  "currentCTC": 85000.0
}
```

---

### ESS Profile (employee.profile.read) as Employee (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/employees/me/profile`

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

### ESS Attendance Records (attendance.self.read) as Employee (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/employees/me/attendance`

**Response Payload**:
```json
[]
```

---

### ESS Leave History (leave.self.read) as Employee (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/employees/me/leaves`

**Response Payload**:
```json
{
  "success": true,
  "message": "Leave history retrieved successfully",
  "timestamp": "2026-06-15T05:38:34Z",
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
      "status": "CANCELLED",
      "approvedBy": null,
      "appliedAt": "2026-06-15T11:08:27.145798",
      "updatedAt": "2026-06-15T11:08:27.200024"
    }
  ]
}
```

---

### ESS Payslips (payslip.self.read) as Employee (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/employees/me/payslips`

**Response Payload**:
```json
{
  "success": true,
  "message": "My payslips retrieved successfully",
  "timestamp": "2026-06-15T05:38:34Z",
  "data": []
}
```

---

### ESS Documents (document.self.read) as Employee (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/employees/me/documents`

**Response Payload**:
```json
[]
```

---

### ESS Expenses (expense.self.read) as Employee (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/employees/me/expenses`

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
    "createdAt": "2026-06-15T11:08:27.374322",
    "updatedAt": "2026-06-15T11:08:27.439769"
  }
]
```

---

### ESS Performance (performance.self.read) as Employee (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/employees/me/performance`

**Response Payload**:
```json
[]
```

---

### ESS Goals (goal.self.read) as Employee (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/employees/me/goals`

**Response Payload**:
```json
[]
```

---

### ESS Assets (asset.self.read) as Employee (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/employees/me/assets`

**Response Payload**:
```json
[]
```

---

### ESS Onboarding Details (onboarding.self.read) as Employee (GET - 200)
**Request URL**: `http://localhost:8080/api/v1/employees/me/onboarding`

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

### ESS Apply Leave as Employee (POST - 201)
**Request URL**: `http://localhost:8080/api/v1/employees/me/leaves`

**Request Payload**:
```json
{
  "leaveType": "Annual Leave",
  "fromDate": "2026-06-20",
  "toDate": "2026-06-22",
  "reason": "Recovering from flu - test 1781501913"
}
```

**Response Payload**:
```json
{
  "success": true,
  "message": "Leave request submitted successfully",
  "timestamp": "2026-06-15T05:38:34Z",
  "data": {
    "id": 28,
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
    "reason": "Recovering from flu - test 1781501913",
    "status": "PENDING",
    "approvedBy": null,
    "appliedAt": "2026-06-15T11:08:34.168285317",
    "updatedAt": "2026-06-15T11:08:34.168286279"
  }
}
```

---

### ESS Submit Expense as Employee (POST - 201)
**Request URL**: `http://localhost:8080/api/v1/employees/me/expenses`

**Request Payload**:
```json
{
  "title": "Internet Allowance 1781501913",
  "amount": 1200,
  "categoryName": "General",
  "description": "Monthly broadband reimbursement"
}
```

**Response Payload**:
```json
{
  "success": true,
  "message": "Expense submitted successfully",
  "timestamp": "2026-06-15T05:38:34Z",
  "data": {
    "id": 14,
    "title": "Internet Allowance 1781501913",
    "amount": 1200,
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
    "createdAt": "2026-06-15T11:08:34.193934569",
    "updatedAt": "2026-06-15T11:08:34.193935431"
  }
}
```

---

### ESS Create Support Ticket as Employee (POST - 201)
**Request URL**: `http://localhost:8080/api/v1/employees/me/support-tickets`

**Request Payload**:
```json
{
  "title": "Broken keyboard 1781501913",
  "description": "Some keys are not responding",
  "category": "IT Support"
}
```

**Response Payload**:
```json
{
  "success": true,
  "message": "Support ticket created successfully",
  "timestamp": "2026-06-15T05:38:34Z",
  "data": {
    "category": "IT Support",
    "createdAt": "2026-06-15T11:08:34.218009534",
    "description": "Some keys are not responding",
    "employeeId": "EMP010",
    "id": 12,
    "status": "OPEN",
    "title": "Broken keyboard 1781501913",
    "updatedAt": "2026-06-15T11:08:34.218010055"
  }
}
```

---

### ESS Punch In as Employee (POST - 200)
**Request URL**: `http://localhost:8080/api/v1/employees/me/attendance/punch-in`

**Request Payload**:
```json
{
  "notes": "Testing punch in from script"
}
```

**Response Payload**:
```json
{
  "success": true,
  "message": "Punched in successfully",
  "timestamp": "2026-06-15T05:38:34Z",
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
    "id": 30,
    "notes": "Testing punch in from script",
    "punchInTime": "11:08:34.242173455",
    "punchOutTime": null,
    "status": "Late",
    "workingHours": null
  }
}
```

---

### ESS Punch Out as Employee (POST - 200)
**Request URL**: `http://localhost:8080/api/v1/employees/me/attendance/punch-out`

**Request Payload**:
```json
{
  "notes": "Testing punch out from script"
}
```

**Response Payload**:
```json
{
  "success": true,
  "message": "Punched out successfully",
  "timestamp": "2026-06-15T05:38:34Z",
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
    "id": 30,
    "notes": "Testing punch out from script",
    "punchInTime": "11:08:34",
    "punchOutTime": "11:08:34.268349989",
    "status": "Late",
    "workingHours": "0h 00m"
  }
}
```

---

### Removed Employees attendance by ID as Employee (GET - 404)
**Request URL**: `http://localhost:8080/api/v1/employees/2/attendance`

**Response Payload**:
```json
{
  "timestamp": "2026-06-15T05:38:34.301Z",
  "status": 404,
  "error": "Not Found",
  "trace": "org.springframework.web.servlet.resource.NoResourceFoundException: No static resource api/v1/employees/2/attendance for request '/api/v1/employees/2/attendance'.\n\tat org.springframework.web.servlet.resource.ResourceHttpRequestHandler.handleRequest(ResourceHttpRequestHandler.java:526)\n\tat org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter.handle(HttpRequestHandlerAdapter.java:50)\n\tat org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963)\n\tat org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:866)\n\tat org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1000)\n\tat org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:892)\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:622)\n\tat org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:874)\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:710)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:128)\n\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat com.example.ems.security.logging.PostApiPayloadLoggingFilter.doFilterInternal(PostApiPayloadLoggingFilter.java:39)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat com.example.ems.security.RateLimitingFilter.doFilterInternal(RateLimitingFilter.java:92)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat com.example.ems.security.RequestCorrelationFilter.doFilterInternal(RequestCorrelationFilter.java:34)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat com.example.ems.security.AuthorizationHeaderFilter.doFilterInternal(AuthorizationHeaderFilter.java:69)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:199)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:165)\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:77)\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:492)\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:113)\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:83)\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:72)\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341)\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:397)\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903)\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1801)\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:946)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:480)\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:57)\n\tat java.base/java.lang.Thread.run(Thread.java:1474)\n",
  "message": "No static resource api/v1/employees/2/attendance.",
  "path": "/api/v1/employees/2/attendance"
}
```

---

### Removed Employees documents by ID as Employee (GET - 404)
**Request URL**: `http://localhost:8080/api/v1/employees/2/documents`

**Response Payload**:
```json
{
  "timestamp": "2026-06-15T05:38:34.312Z",
  "status": 404,
  "error": "Not Found",
  "trace": "org.springframework.web.servlet.resource.NoResourceFoundException: No static resource api/v1/employees/2/documents for request '/api/v1/employees/2/documents'.\n\tat org.springframework.web.servlet.resource.ResourceHttpRequestHandler.handleRequest(ResourceHttpRequestHandler.java:526)\n\tat org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter.handle(HttpRequestHandlerAdapter.java:50)\n\tat org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963)\n\tat org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:866)\n\tat org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1000)\n\tat org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:892)\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:622)\n\tat org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:874)\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:710)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:128)\n\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat com.example.ems.security.logging.PostApiPayloadLoggingFilter.doFilterInternal(PostApiPayloadLoggingFilter.java:39)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat com.example.ems.security.RateLimitingFilter.doFilterInternal(RateLimitingFilter.java:92)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat com.example.ems.security.RequestCorrelationFilter.doFilterInternal(RequestCorrelationFilter.java:34)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat com.example.ems.security.AuthorizationHeaderFilter.doFilterInternal(AuthorizationHeaderFilter.java:69)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:199)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:165)\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:77)\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:492)\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:113)\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:83)\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:72)\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341)\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:397)\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903)\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1801)\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:946)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:480)\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:57)\n\tat java.base/java.lang.Thread.run(Thread.java:1474)\n",
  "message": "No static resource api/v1/employees/2/documents.",
  "path": "/api/v1/employees/2/documents"
}
```

---

### Removed Leaves employee by ID as Employee (GET - 404)
**Request URL**: `http://localhost:8080/api/v1/leaves/employee/2`

**Response Payload**:
```json
{
  "timestamp": "2026-06-15T05:38:34.321Z",
  "status": 404,
  "error": "Not Found",
  "trace": "org.springframework.web.servlet.resource.NoResourceFoundException: No static resource api/v1/leaves/employee/2 for request '/api/v1/leaves/employee/2'.\n\tat org.springframework.web.servlet.resource.ResourceHttpRequestHandler.handleRequest(ResourceHttpRequestHandler.java:526)\n\tat org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter.handle(HttpRequestHandlerAdapter.java:50)\n\tat org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963)\n\tat org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:866)\n\tat org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1000)\n\tat org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:892)\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:622)\n\tat org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:874)\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:710)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:128)\n\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat com.example.ems.security.logging.PostApiPayloadLoggingFilter.doFilterInternal(PostApiPayloadLoggingFilter.java:39)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat com.example.ems.security.RateLimitingFilter.doFilterInternal(RateLimitingFilter.java:92)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat com.example.ems.security.RequestCorrelationFilter.doFilterInternal(RequestCorrelationFilter.java:34)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat com.example.ems.security.AuthorizationHeaderFilter.doFilterInternal(AuthorizationHeaderFilter.java:69)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:199)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:165)\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:77)\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:492)\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:113)\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:83)\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:72)\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341)\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:397)\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903)\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1801)\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:946)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:480)\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:57)\n\tat java.base/java.lang.Thread.run(Thread.java:1474)\n",
  "message": "No static resource api/v1/leaves/employee/2.",
  "path": "/api/v1/leaves/employee/2"
}
```

---

### Removed Payslips my as Employee (GET - 400)
**Request URL**: `http://localhost:8080/api/v1/payslips/my`

**Response Payload**:
```json
{
  "success": false,
  "message": "Parameter 'id' value 'my' could not be converted to type 'Long'",
  "errorCode": "VAL_002",
  "timestamp": "2026-06-15T05:38:34Z"
}
```

---

### Removed Attendance my as Employee (GET - 400)
**Request URL**: `http://localhost:8080/api/v1/attendance/my`

**Response Payload**:
```json
{
  "success": false,
  "message": "Parameter 'id' value 'my' could not be converted to type 'Long'",
  "errorCode": "VAL_002",
  "timestamp": "2026-06-15T05:38:34Z"
}
```

---

