# EMS API Verification Results
This document contains the complete and raw API responses from the local verification run.
## 1. Authentication (Login)
**Request**: `POST http://localhost:8080/api/v1/auth/login`
**Status Code**: `200`
```json
{
  "success": true,
  "message": "Login successful",
  "timestamp": "2026-06-11T08:38:50.914036380Z",
  "data": {
    "tokens": {
      "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiU1VQRVJfQURNSU4iLCJ1c2VySWQiOiJFTVAwMDUiLCJzdWIiOiJlbXNzdXBlcmFkbWluQGdtYWlsLmNvbSIsImlhdCI6MTc4MTE2NzEzMCwiZXhwIjoxNzgxMTY4MDMwfQ.46Ow8bba6BEr2ezGvYnrCMS3SiSjtp_lVmJrbnTUhV5JiE42bhQ1nKp6vI3wvz6IFkJfR8RcYVtzjVemZqkLhg",
      "refreshToken": "2faab4bb-ddee-4348-9b46-626c57c24ff6",
      "tokenType": "Bearer",
      "accessTokenExpiresIn": 900,
      "refreshTokenExpiresIn": 604800
    },
    "user": {
      "id": 5,
      "employeeId": "EMP005",
      "name": "Super Admin",
      "email": "emssuperadmin@gmail.com",
      "role": {
        "id": 7,
        "name": "SUPER_ADMIN"
      },
      "permissions": [
        "performance.review",
        "leave.approve",
        "user.read",
        "user.update",
        "employee.read",
        "leave.self.read",
        "leave.read",
        "employee.create",
        "reports.hr",
        "employee.update",
        "profile.update",
        "leave.team.approve",
        "profile.read",
        "employee.team.read",
        "payroll.read",
        "role.manage",
        "task.assign",
        "user.manage",
        "leave.create",
        "expense.manage",
        "attendance.read",
        "employee.delete",
        "attendance.self.read",
        "reports.finance",
        "payroll.manage",
        "payslip.read",
        "reports.manager",
        "user.delete",
        "user.create",
        "attendance.manage",
        "salary.manage",
        "reports.view",
        "recruitment.manage",
        "leave.manage",
        "system.manage",
        "permission.manage",
        "attendance.team.read"
      ],
      "status": "ACTIVE",
      "lastLogin": "2026-06-11T08:38:50.913976418Z"
    }
  }
}
```
# Seeding Write Verification
## Create Department (Write)
**Request**: `POST http://localhost:8080/api/v1/departments`
**Payload**:
```json
{
  "name": "Engineering",
  "code": "ENG",
  "description": "Software development and engineering department"
}
```
**Status Code**: `400`
**Response**:
```json
{
  "success": false,
  "message": "Department name already exists",
  "errorCode": "DEP_002",
  "timestamp": "2026-06-11T08:38:50.934148048Z"
}
```
## Create Leave Type (Write)
**Request**: `POST http://localhost:8080/api/v1/leave-types`
**Payload**:
```json
{
  "name": "Annual Leave",
  "description": "Paid time off for holidays/vacation",
  "defaultDays": 20
}
```
**Status Code**: `400`
**Response**:
```json
{
  "success": false,
  "message": "Leave type name already exists",
  "errorCode": "LVT_001",
  "timestamp": "2026-06-11T08:38:50.955334650Z"
}
```
## Create Recruitment Job (Write)
**Request**: `POST http://localhost:8080/api/v1/recruitments/jobs`
**Payload**:
```json
{
  "title": "Software Engineer",
  "department": "Engineering",
  "location": "Headquarters",
  "description": "Design and build enterprise applications",
  "requirements": "Java, Spring Boot, PostgreSQL, Redis",
  "salaryRange": "80,000 - 120,000 USD",
  "status": "PUBLISHED"
}
```
**Status Code**: `201`
**Response**:
```json
{
  "success": true,
  "message": "Job posting created successfully",
  "timestamp": "2026-06-11T08:38:50.979097326Z",
  "data": {
    "createdAt": "2026-06-11T14:08:50.971955482",
    "department": "Engineering",
    "description": "Design and build enterprise applications",
    "id": 2,
    "location": "Headquarters",
    "requirements": "Java, Spring Boot, PostgreSQL, Redis",
    "salaryRange": "80,000 - 120,000 USD",
    "status": "PUBLISHED",
    "title": "Software Engineer",
    "updatedAt": "2026-06-11T14:08:50.971979056"
  }
}
```
## Create Training Course (Write)
**Request**: `POST http://localhost:8080/api/v1/trainings/courses`
**Payload**:
```json
{
  "title": "Spring Boot Basics",
  "description": "Introduction to Spring Boot framework",
  "category": "Engineering",
  "durationHours": 16
}
```
**Status Code**: `201`
**Response**:
```json
{
  "success": true,
  "message": "Training course created successfully",
  "timestamp": "2026-06-11T08:38:51.001669959Z",
  "data": {
    "category": "Engineering",
    "createdAt": "2026-06-11T14:08:50.994979215",
    "description": "Introduction to Spring Boot framework",
    "durationHours": 16,
    "id": 2,
    "status": "ACTIVE",
    "title": "Spring Boot Basics",
    "updatedAt": "2026-06-11T14:08:50.995004212"
  }
}
```
## Generate Payroll Run (Write)
**Request**: `POST http://localhost:8080/api/v1/payroll-runs`
**Payload**:
```json
{
  "month": 6,
  "year": 2026
}
```
**Status Code**: `201`
**Response**:
```json
{
  "success": true,
  "message": "Payroll generated successfully. Records created: 0",
  "timestamp": "2026-06-11T08:38:51.025648241Z",
  "data": []
}
```
# Read (GET) Verification
## List Users (Read)
**Request**: `GET http://localhost:8080/api/v1/users`
**Status Code**: `200`
**Response**:
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "timestamp": "2026-06-11T08:38:51.047951958Z",
  "data": [
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
        "id": 7,
        "name": "SUPER_ADMIN",
        "description": "Role for SUPER_ADMIN",
        "permissions": [
          {
            "id": 38,
            "name": "employee.team.read",
            "description": "Permission for employee.team.read"
          },
          {
            "id": 25,
            "name": "reports.view",
            "description": "Permission for reports.view"
          },
          {
            "id": 30,
            "name": "role.manage",
            "description": "Permission for role.manage"
          },
          {
            "id": 18,
            "name": "leave.manage",
            "description": "Permission for leave.manage"
          },
          {
            "id": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "id": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "id": 33,
            "name": "task.assign",
            "description": "Permission for task.assign"
          },
          {
            "id": 11,
            "name": "attendance.read",
            "description": "Permission for attendance.read"
          },
          {
            "id": 13,
            "name": "attendance.team.read",
            "description": "Permission for attendance.team.read"
          },
          {
            "id": 22,
            "name": "payroll.manage",
            "description": "Permission for payroll.manage"
          },
          {
            "id": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "id": 4,
            "name": "employee.update",
            "description": "Update employees"
          },
          {
            "id": 40,
            "name": "user.manage",
            "description": "Permission for user.manage"
          },
          {
            "id": 35,
            "name": "expense.manage",
            "description": "Permission for expense.manage"
          },
          {
            "id": 10,
            "name": "employee.delete",
            "description": "Permission for employee.delete"
          },
          {
            "id": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "id": 7,
            "name": "user.update",
            "description": "Permission for user.update"
          },
          {
            "id": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "id": 32,
            "name": "recruitment.manage",
            "description": "Permission for recruitment.manage"
          },
          {
            "id": 21,
            "name": "payroll.read",
            "description": "Permission for payroll.read"
          },
          {
            "id": 6,
            "name": "user.read",
            "description": "Permission for user.read"
          },
          {
            "id": 26,
            "name": "reports.hr",
            "description": "Permission for reports.hr"
          },
          {
            "id": 5,
            "name": "user.create",
            "description": "Permission for user.create"
          },
          {
            "id": 9,
            "name": "employee.read",
            "description": "Permission for employee.read"
          },
          {
            "id": 16,
            "name": "leave.read",
            "description": "Permission for leave.read"
          },
          {
            "id": 28,
            "name": "reports.manager",
            "description": "Permission for reports.manager"
          },
          {
            "id": 34,
            "name": "performance.review",
            "description": "Permission for performance.review"
          },
          {
            "id": 3,
            "name": "employee.create",
            "description": "Create employees"
          },
          {
            "id": 17,
            "name": "leave.approve",
            "description": "Permission for leave.approve"
          },
          {
            "id": 29,
            "name": "system.manage",
            "description": "Permission for system.manage"
          },
          {
            "id": 19,
            "name": "leave.team.approve",
            "description": "Permission for leave.team.approve"
          },
          {
            "id": 23,
            "name": "salary.manage",
            "description": "Permission for salary.manage"
          },
          {
            "id": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          },
          {
            "id": 31,
            "name": "permission.manage",
            "description": "Permission for permission.manage"
          },
          {
            "id": 12,
            "name": "attendance.manage",
            "description": "Permission for attendance.manage"
          },
          {
            "id": 8,
            "name": "user.delete",
            "description": "Permission for user.delete"
          },
          {
            "id": 27,
            "name": "reports.finance",
            "description": "Permission for reports.finance"
          }
        ]
      },
      "location": "Headquarters",
      "status": null
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
        "id": 11,
        "name": "HR",
        "description": "Role for HR",
        "permissions": [
          {
            "id": 9,
            "name": "employee.read",
            "description": "Permission for employee.read"
          },
          {
            "id": 16,
            "name": "leave.read",
            "description": "Permission for leave.read"
          },
          {
            "id": 32,
            "name": "recruitment.manage",
            "description": "Permission for recruitment.manage"
          },
          {
            "id": 3,
            "name": "employee.create",
            "description": "Create employees"
          },
          {
            "id": 17,
            "name": "leave.approve",
            "description": "Permission for leave.approve"
          },
          {
            "id": 11,
            "name": "attendance.read",
            "description": "Permission for attendance.read"
          },
          {
            "id": 4,
            "name": "employee.update",
            "description": "Update employees"
          },
          {
            "id": 26,
            "name": "reports.hr",
            "description": "Permission for reports.hr"
          }
        ]
      },
      "location": "TRL",
      "status": null
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
        "id": 12,
        "name": "ADMIN",
        "description": "Role for ADMIN",
        "permissions": [
          {
            "id": 5,
            "name": "user.create",
            "description": "Permission for user.create"
          },
          {
            "id": 9,
            "name": "employee.read",
            "description": "Permission for employee.read"
          },
          {
            "id": 18,
            "name": "leave.manage",
            "description": "Permission for leave.manage"
          },
          {
            "id": 7,
            "name": "user.update",
            "description": "Permission for user.update"
          },
          {
            "id": 3,
            "name": "employee.create",
            "description": "Create employees"
          },
          {
            "id": 25,
            "name": "reports.view",
            "description": "Permission for reports.view"
          },
          {
            "id": 4,
            "name": "employee.update",
            "description": "Update employees"
          },
          {
            "id": 40,
            "name": "user.manage",
            "description": "Permission for user.manage"
          },
          {
            "id": 6,
            "name": "user.read",
            "description": "Permission for user.read"
          },
          {
            "id": 12,
            "name": "attendance.manage",
            "description": "Permission for attendance.manage"
          }
        ]
      },
      "location": "TRL",
      "status": null
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
        "id": 9,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "id": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "id": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "id": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "id": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "id": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "id": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
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
        "id": 9,
        "name": "EMPLOYEE",
        "description": "Role for EMPLOYEE",
        "permissions": [
          {
            "id": 14,
            "name": "attendance.self.read",
            "description": "Permission for attendance.self.read"
          },
          {
            "id": 37,
            "name": "profile.update",
            "description": "Permission for profile.update"
          },
          {
            "id": 20,
            "name": "leave.self.read",
            "description": "Permission for leave.self.read"
          },
          {
            "id": 15,
            "name": "leave.create",
            "description": "Permission for leave.create"
          },
          {
            "id": 36,
            "name": "profile.read",
            "description": "Permission for profile.read"
          },
          {
            "id": 24,
            "name": "payslip.read",
            "description": "Permission for payslip.read"
          }
        ]
      },
      "location": null,
      "status": null
    }
  ]
}
```
## List Roles (Read)
**Request**: `GET http://localhost:8080/api/v1/roles`
**Status Code**: `200`
**Response**:
```json
{
  "success": true,
  "message": "Roles list retrieved successfully",
  "timestamp": "2026-06-11T08:38:51.071776700Z",
  "data": [
    {
      "id": 7,
      "name": "SUPER_ADMIN",
      "description": "Role for SUPER_ADMIN",
      "permissions": [
        {
          "id": 4,
          "name": "employee.update",
          "description": "Update employees"
        },
        {
          "id": 21,
          "name": "payroll.read",
          "description": "Permission for payroll.read"
        },
        {
          "id": 33,
          "name": "task.assign",
          "description": "Permission for task.assign"
        },
        {
          "id": 12,
          "name": "attendance.manage",
          "description": "Permission for attendance.manage"
        },
        {
          "id": 22,
          "name": "payroll.manage",
          "description": "Permission for payroll.manage"
        },
        {
          "id": 25,
          "name": "reports.view",
          "description": "Permission for reports.view"
        },
        {
          "id": 40,
          "name": "user.manage",
          "description": "Permission for user.manage"
        },
        {
          "id": 27,
          "name": "reports.finance",
          "description": "Permission for reports.finance"
        },
        {
          "id": 28,
          "name": "reports.manager",
          "description": "Permission for reports.manager"
        },
        {
          "id": 23,
          "name": "salary.manage",
          "description": "Permission for salary.manage"
        },
        {
          "id": 24,
          "name": "payslip.read",
          "description": "Permission for payslip.read"
        },
        {
          "id": 38,
          "name": "employee.team.read",
          "description": "Permission for employee.team.read"
        },
        {
          "id": 15,
          "name": "leave.create",
          "description": "Permission for leave.create"
        },
        {
          "id": 20,
          "name": "leave.self.read",
          "description": "Permission for leave.self.read"
        },
        {
          "id": 31,
          "name": "permission.manage",
          "description": "Permission for permission.manage"
        },
        {
          "id": 19,
          "name": "leave.team.approve",
          "description": "Permission for leave.team.approve"
        },
        {
          "id": 11,
          "name": "attendance.read",
          "description": "Permission for attendance.read"
        },
        {
          "id": 7,
          "name": "user.update",
          "description": "Permission for user.update"
        },
        {
          "id": 35,
          "name": "expense.manage",
          "description": "Permission for expense.manage"
        },
        {
          "id": 8,
          "name": "user.delete",
          "description": "Permission for user.delete"
        },
        {
          "id": 9,
          "name": "employee.read",
          "description": "Permission for employee.read"
        },
        {
          "id": 14,
          "name": "attendance.self.read",
          "description": "Permission for attendance.self.read"
        },
        {
          "id": 5,
          "name": "user.create",
          "description": "Permission for user.create"
        },
        {
          "id": 29,
          "name": "system.manage",
          "description": "Permission for system.manage"
        },
        {
          "id": 13,
          "name": "attendance.team.read",
          "description": "Permission for attendance.team.read"
        },
        {
          "id": 10,
          "name": "employee.delete",
          "description": "Permission for employee.delete"
        },
        {
          "id": 34,
          "name": "performance.review",
          "description": "Permission for performance.review"
        },
        {
          "id": 6,
          "name": "user.read",
          "description": "Permission for user.read"
        },
        {
          "id": 3,
          "name": "employee.create",
          "description": "Create employees"
        },
        {
          "id": 17,
          "name": "leave.approve",
          "description": "Permission for leave.approve"
        },
        {
          "id": 18,
          "name": "leave.manage",
          "description": "Permission for leave.manage"
        },
        {
          "id": 30,
          "name": "role.manage",
          "description": "Permission for role.manage"
        },
        {
          "id": 37,
          "name": "profile.update",
          "description": "Permission for profile.update"
        },
        {
          "id": 26,
          "name": "reports.hr",
          "description": "Permission for reports.hr"
        },
        {
          "id": 16,
          "name": "leave.read",
          "description": "Permission for leave.read"
        },
        {
          "id": 32,
          "name": "recruitment.manage",
          "description": "Permission for recruitment.manage"
        },
        {
          "id": 36,
          "name": "profile.read",
          "description": "Permission for profile.read"
        }
      ]
    },
    {
      "id": 8,
      "name": "FINANCE",
      "description": "Role for FINANCE",
      "permissions": [
        {
          "id": 23,
          "name": "salary.manage",
          "description": "Permission for salary.manage"
        },
        {
          "id": 21,
          "name": "payroll.read",
          "description": "Permission for payroll.read"
        },
        {
          "id": 22,
          "name": "payroll.manage",
          "description": "Permission for payroll.manage"
        },
        {
          "id": 27,
          "name": "reports.finance",
          "description": "Permission for reports.finance"
        },
        {
          "id": 35,
          "name": "expense.manage",
          "description": "Permission for expense.manage"
        }
      ]
    },
    {
      "id": 9,
      "name": "EMPLOYEE",
      "description": "Role for EMPLOYEE",
      "permissions": [
        {
          "id": 24,
          "name": "payslip.read",
          "description": "Permission for payslip.read"
        },
        {
          "id": 15,
          "name": "leave.create",
          "description": "Permission for leave.create"
        },
        {
          "id": 20,
          "name": "leave.self.read",
          "description": "Permission for leave.self.read"
        },
        {
          "id": 14,
          "name": "attendance.self.read",
          "description": "Permission for attendance.self.read"
        },
        {
          "id": 37,
          "name": "profile.update",
          "description": "Permission for profile.update"
        },
        {
          "id": 36,
          "name": "profile.read",
          "description": "Permission for profile.read"
        }
      ]
    },
    {
      "id": 10,
      "name": "MANAGER",
      "description": "Role for MANAGER",
      "permissions": [
        {
          "id": 13,
          "name": "attendance.team.read",
          "description": "Permission for attendance.team.read"
        },
        {
          "id": 34,
          "name": "performance.review",
          "description": "Permission for performance.review"
        },
        {
          "id": 38,
          "name": "employee.team.read",
          "description": "Permission for employee.team.read"
        },
        {
          "id": 33,
          "name": "task.assign",
          "description": "Permission for task.assign"
        },
        {
          "id": 19,
          "name": "leave.team.approve",
          "description": "Permission for leave.team.approve"
        }
      ]
    },
    {
      "id": 11,
      "name": "HR",
      "description": "Role for HR",
      "permissions": [
        {
          "id": 4,
          "name": "employee.update",
          "description": "Update employees"
        },
        {
          "id": 9,
          "name": "employee.read",
          "description": "Permission for employee.read"
        },
        {
          "id": 3,
          "name": "employee.create",
          "description": "Create employees"
        },
        {
          "id": 17,
          "name": "leave.approve",
          "description": "Permission for leave.approve"
        },
        {
          "id": 11,
          "name": "attendance.read",
          "description": "Permission for attendance.read"
        },
        {
          "id": 26,
          "name": "reports.hr",
          "description": "Permission for reports.hr"
        },
        {
          "id": 16,
          "name": "leave.read",
          "description": "Permission for leave.read"
        },
        {
          "id": 32,
          "name": "recruitment.manage",
          "description": "Permission for recruitment.manage"
        }
      ]
    },
    {
      "id": 12,
      "name": "ADMIN",
      "description": "Role for ADMIN",
      "permissions": [
        {
          "id": 4,
          "name": "employee.update",
          "description": "Update employees"
        },
        {
          "id": 6,
          "name": "user.read",
          "description": "Permission for user.read"
        },
        {
          "id": 9,
          "name": "employee.read",
          "description": "Permission for employee.read"
        },
        {
          "id": 12,
          "name": "attendance.manage",
          "description": "Permission for attendance.manage"
        },
        {
          "id": 25,
          "name": "reports.view",
          "description": "Permission for reports.view"
        },
        {
          "id": 40,
          "name": "user.manage",
          "description": "Permission for user.manage"
        },
        {
          "id": 3,
          "name": "employee.create",
          "description": "Create employees"
        },
        {
          "id": 18,
          "name": "leave.manage",
          "description": "Permission for leave.manage"
        },
        {
          "id": 7,
          "name": "user.update",
          "description": "Permission for user.update"
        },
        {
          "id": 5,
          "name": "user.create",
          "description": "Permission for user.create"
        }
      ]
    },
    {
      "id": 13,
      "name": "SUPPORT_STAFF",
      "description": "Role for customer and system support staff",
      "permissions": []
    },
    {
      "id": 15,
      "name": "SETTINGS_AUDITOR_UPDATED",
      "description": "Auditing settings updated",
      "permissions": []
    },
    {
      "id": 16,
      "name": "SETTINGS_AUDITOR",
      "description": "Role for settings auditing",
      "permissions": []
    }
  ]
}
```
## List Permissions (Read)
**Request**: `GET http://localhost:8080/api/v1/permissions`
**Status Code**: `200`
**Response**:
```json
[
  {
    "id": 3,
    "name": "employee.create",
    "description": "Create employees"
  },
  {
    "id": 4,
    "name": "employee.update",
    "description": "Update employees"
  },
  {
    "id": 5,
    "name": "user.create",
    "description": "Permission for user.create"
  },
  {
    "id": 6,
    "name": "user.read",
    "description": "Permission for user.read"
  },
  {
    "id": 7,
    "name": "user.update",
    "description": "Permission for user.update"
  },
  {
    "id": 8,
    "name": "user.delete",
    "description": "Permission for user.delete"
  },
  {
    "id": 9,
    "name": "employee.read",
    "description": "Permission for employee.read"
  },
  {
    "id": 10,
    "name": "employee.delete",
    "description": "Permission for employee.delete"
  },
  {
    "id": 11,
    "name": "attendance.read",
    "description": "Permission for attendance.read"
  },
  {
    "id": 12,
    "name": "attendance.manage",
    "description": "Permission for attendance.manage"
  },
  {
    "id": 13,
    "name": "attendance.team.read",
    "description": "Permission for attendance.team.read"
  },
  {
    "id": 14,
    "name": "attendance.self.read",
    "description": "Permission for attendance.self.read"
  },
  {
    "id": 15,
    "name": "leave.create",
    "description": "Permission for leave.create"
  },
  {
    "id": 16,
    "name": "leave.read",
    "description": "Permission for leave.read"
  },
  {
    "id": 17,
    "name": "leave.approve",
    "description": "Permission for leave.approve"
  },
  {
    "id": 18,
    "name": "leave.manage",
    "description": "Permission for leave.manage"
  },
  {
    "id": 19,
    "name": "leave.team.approve",
    "description": "Permission for leave.team.approve"
  },
  {
    "id": 20,
    "name": "leave.self.read",
    "description": "Permission for leave.self.read"
  },
  {
    "id": 21,
    "name": "payroll.read",
    "description": "Permission for payroll.read"
  },
  {
    "id": 22,
    "name": "payroll.manage",
    "description": "Permission for payroll.manage"
  },
  {
    "id": 23,
    "name": "salary.manage",
    "description": "Permission for salary.manage"
  },
  {
    "id": 24,
    "name": "payslip.read",
    "description": "Permission for payslip.read"
  },
  {
    "id": 25,
    "name": "reports.view",
    "description": "Permission for reports.view"
  },
  {
    "id": 26,
    "name": "reports.hr",
    "description": "Permission for reports.hr"
  },
  {
    "id": 27,
    "name": "reports.finance",
    "description": "Permission for reports.finance"
  },
  {
    "id": 28,
    "name": "reports.manager",
    "description": "Permission for reports.manager"
  },
  {
    "id": 29,
    "name": "system.manage",
    "description": "Permission for system.manage"
  },
  {
    "id": 30,
    "name": "role.manage",
    "description": "Permission for role.manage"
  },
  {
    "id": 31,
    "name": "permission.manage",
    "description": "Permission for permission.manage"
  },
  {
    "id": 32,
    "name": "recruitment.manage",
    "description": "Permission for recruitment.manage"
  },
  {
    "id": 33,
    "name": "task.assign",
    "description": "Permission for task.assign"
  },
  {
    "id": 34,
    "name": "performance.review",
    "description": "Permission for performance.review"
  },
  {
    "id": 35,
    "name": "expense.manage",
    "description": "Permission for expense.manage"
  },
  {
    "id": 36,
    "name": "profile.read",
    "description": "Permission for profile.read"
  },
  {
    "id": 37,
    "name": "profile.update",
    "description": "Permission for profile.update"
  },
  {
    "id": 38,
    "name": "employee.team.read",
    "description": "Permission for employee.team.read"
  },
  {
    "id": 39,
    "name": "temp.test.permission",
    "description": "Temp test permission"
  },
  {
    "id": 40,
    "name": "user.manage",
    "description": "Permission for user.manage"
  }
]
```
## List Departments (Read)
**Request**: `GET http://localhost:8080/api/v1/departments`
**Status Code**: `200`
**Response**:
```json
{
  "success": true,
  "message": "Departments retrieved successfully",
  "timestamp": "2026-06-11T08:38:51.109658474Z",
  "data": [
    {
      "id": 1,
      "name": "Engineering",
      "code": "ENG",
      "description": "Software development and engineering department"
    }
  ]
}
```
## List Employees (Read)
**Request**: `GET http://localhost:8080/api/v1/employees`
**Status Code**: `200`
**Response**:
```json
{
  "success": true,
  "message": "Employees list retrieved successfully",
  "timestamp": "2026-06-11T08:38:51.127483634Z",
  "data": [
    {
      "address": null,
      "annualSalary": 75000.0,
      "department": "HR",
      "designation": "HR Specialist",
      "dob": null,
      "email": "alicesmith@example.com",
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
      "annualSalary": 85000.0,
      "department": "Engineering",
      "designation": "Software Engineer",
      "dob": null,
      "email": "emssuperadmin@gmail.com",
      "employeeId": null,
      "employmentType": null,
      "fullName": "John Doe",
      "gender": null,
      "id": 1,
      "joiningDate": "2026-06-10",
      "location": null,
      "manager": null,
      "phone": null,
      "status": null
    },
    {
      "address": null,
      "annualSalary": 30000.0,
      "department": "IT",
      "designation": "admin",
      "dob": null,
      "email": "david@company.com",
      "employeeId": null,
      "employmentType": null,
      "fullName": "Pavi",
      "gender": null,
      "id": 2,
      "joiningDate": "2025-06-10",
      "location": null,
      "manager": {
        "address": null,
        "annualSalary": 85000.0,
        "department": "Engineering",
        "designation": "Software Engineer",
        "dob": null,
        "email": "emssuperadmin@gmail.com",
        "employeeId": null,
        "employmentType": null,
        "fullName": "John Doe",
        "gender": null,
        "id": 1,
        "joiningDate": "2026-06-10",
        "location": null,
        "manager": null,
        "phone": null,
        "status": null
      },
      "phone": null,
      "status": null
    }
  ]
}
```
## List Attendance (Read)
**Request**: `GET http://localhost:8080/api/v1/attendance`
**Status Code**: `200`
**Response**:
```json
{
  "success": true,
  "message": "Attendance records retrieved successfully",
  "timestamp": "2026-06-11T08:38:51.147181001Z",
  "data": [
    {
      "date": "2026-06-10",
      "employee": {
        "address": null,
        "annualSalary": 85000.0,
        "department": "Engineering",
        "designation": "Software Engineer",
        "dob": null,
        "email": "emssuperadmin@gmail.com",
        "employeeId": null,
        "employmentType": null,
        "fullName": "John Doe",
        "gender": null,
        "id": 1,
        "joiningDate": "2026-06-10",
        "location": null,
        "manager": null,
        "phone": null,
        "status": null
      },
      "id": 1,
      "notes": "On-time arrival",
      "punchInTime": "09:00:00",
      "punchOutTime": "17:00:00",
      "status": "Present",
      "workingHours": "8h 00m"
    }
  ]
}
```
## List Leave Types (Read)
**Request**: `GET http://localhost:8080/api/v1/leave-types`
**Status Code**: `200`
**Response**:
```json
{
  "success": true,
  "message": "Leave types retrieved successfully",
  "timestamp": "2026-06-11T08:38:51.161668603Z",
  "data": [
    {
      "id": 1,
      "name": "Annual Leave",
      "description": "Paid time off for holidays/vacation",
      "defaultDays": 20,
      "active": true
    }
  ]
}
```
## Get My Payroll History (Read)
**Request**: `GET http://localhost:8080/api/v1/payroll-runs/my`
**Status Code**: `200`
**Response**:
```json
{
  "success": true,
  "message": "My payroll history retrieved successfully",
  "timestamp": "2026-06-11T08:38:51.177125412Z",
  "data": []
}
```
## List Recruitment Jobs (Read)
**Request**: `GET http://localhost:8080/api/v1/recruitments/jobs`
**Status Code**: `200`
**Response**:
```json
{
  "success": true,
  "message": "Jobs retrieved successfully",
  "timestamp": "2026-06-11T08:38:51.194308632Z",
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
## List Training Courses (Read)
**Request**: `GET http://localhost:8080/api/v1/trainings/courses`
**Status Code**: `200`
**Response**:
```json
{
  "success": true,
  "message": "Training courses retrieved successfully",
  "timestamp": "2026-06-11T08:38:51.209715507Z",
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
    }
  ]
}
```
