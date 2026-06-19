# Finance Endpoints API Verification Report
Generated at: 2026-06-19 10:16:25 (UTC)

### Status Summary: **43 / 45 Passed**

| Category | Method | Endpoint | Authorized Role | Status | Result |
| --- | --- | --- | --- | --- | --- |
| 1. Dashboard Overview | `GET` | `/finance/dashboard` | `FINANCE` | `200` | ✅ PASSED |
| 1. Dashboard Overview | `GET` | `/finance/analytics/monthly` | `FINANCE` | `200` | ✅ PASSED |
| 1. Dashboard Overview | `GET` | `/finance/transactions/recent` | `FINANCE` | `200` | ✅ PASSED |
| 1. Dashboard Overview | `GET` | `/finance/salary/summary` | `FINANCE` | `200` | ✅ PASSED |
| 1. Dashboard Overview | `GET` | `/finance/report` | `FINANCE` | `200` | ✅ PASSED |
| 2. Expenses & Cost Control | `GET` | `/my-expenses/dashboard` | `EMPLOYEE` | `200` | ✅ PASSED |
| 2. Expenses & Cost Control | `GET` | `/my-expenses` | `EMPLOYEE` | `200` | ✅ PASSED |
| 2. Expenses & Cost Control | `GET` | `/my-expenses/categories` | `EMPLOYEE` | `200` | ✅ PASSED |
| 2. Expenses & Cost Control | `GET` | `/finance/expenses/categories` | `FINANCE` | `200` | ✅ PASSED |
| 2. Expenses & Cost Control | `GET` | `/expense-categories` | `FINANCE` | `200` | ✅ PASSED |
| 3. Payroll & Salary Insights | `GET` | `/payroll-runs/stats` | `FINANCE` | `200` | ✅ PASSED |
| 3. Payroll & Salary Insights | `GET` | `/payroll-runs/reports` | `FINANCE` | `200` | ✅ PASSED |
| 3. Payroll & Salary Insights | `GET` | `/payslips/export` | `FINANCE` | `200` | ✅ PASSED |
| 3. Payroll & Salary Insights | `GET` | `/my-payslips/annual-statement` | `EMPLOYEE` | `200` | ✅ PASSED |
| 4. F&F Settlement | `POST` | `/fnf-settlements` | `FINANCE` | `201` | ✅ PASSED |
| 4. F&F Settlement | `GET` | `/fnf-settlements` | `FINANCE` | `200` | ✅ PASSED |
| 4. F&F Settlement | `GET` | `/fnf-settlements/2` | `FINANCE` | `200` | ✅ PASSED |
| 4. F&F Settlement | `GET` | `/fnf-settlements/employee/3` | `FINANCE` | `200` | ✅ PASSED |
| 4. F&F Settlement | `POST` | `/fnf-settlements/2/approve` | `FINANCE` | `200` | ✅ PASSED |
| 4. F&F Settlement | `POST` | `/fnf-settlements/2/process` | `FINANCE` | `200` | ✅ PASSED |
| 4. F&F Settlement | `POST` | `/fnf-settlements/2/reject` | `FINANCE` | `200` | ✅ PASSED |
| 5. Finance Onboarding | `POST` | `/finance/onboarding` | `FINANCE` | `201` | ✅ PASSED |
| 5. Finance Onboarding | `GET` | `/finance/onboarding/current` | `FINANCE` | `200` | ✅ PASSED |
| 5. Finance Onboarding | `GET` | `/finance/onboarding/2` | `FINANCE` | `200` | ✅ PASSED |
| 5. Finance Onboarding | `GET` | `/finance/onboarding/2/progress` | `FINANCE` | `200` | ✅ PASSED |
| 5. Finance Onboarding | `PATCH` | `/finance/onboarding/2/company` | `FINANCE` | `200` | ✅ PASSED |
| 5. Finance Onboarding | `PATCH` | `/finance/onboarding/2/bank-account` | `FINANCE` | `200` | ✅ PASSED |
| 5. Finance Onboarding | `PATCH` | `/finance/onboarding/2/tax` | `FINANCE` | `200` | ✅ PASSED |
| 5. Finance Onboarding | `PATCH` | `/finance/onboarding/2/payment-method` | `FINANCE` | `200` | ✅ PASSED |
| 5. Finance Onboarding | `PATCH` | `/finance/onboarding/2/payroll` | `FINANCE` | `200` | ✅ PASSED |
| 5. Finance Onboarding | `PATCH` | `/finance/onboarding/2/budget` | `FINANCE` | `200` | ✅ PASSED |
| 5. Finance Onboarding | `POST` | `/finance/onboarding/2/validate` | `FINANCE` | `400` | ❌ FAILED |
| 5. Finance Onboarding | `POST` | `/finance/onboarding/2/complete` | `FINANCE` | `400` | ❌ FAILED |
| 6. Payments & Flow | `GET` | `/finance/payments/pending` | `FINANCE` | `200` | ✅ PASSED |
| 6. Payments & Flow | `GET` | `/payroll-runs` | `FINANCE` | `200` | ✅ PASSED |
| 6. Payments & Flow | `POST` | `/payroll-runs/process` | `FINANCE` | `200` | ✅ PASSED |
| 7. Reporting & Analytics | `GET` | `/reports/dashboard/admin` | `SUPER_ADMIN` | `200` | ✅ PASSED |
| 7. Reporting & Analytics | `GET` | `/reports/reports/payroll` | `SUPER_ADMIN` | `200` | ✅ PASSED |
| 7. Reporting & Analytics | `GET` | `/reports/reports/employees` | `SUPER_ADMIN` | `200` | ✅ PASSED |
| 7. Reporting & Analytics | `GET` | `/reports/reports/attendance` | `SUPER_ADMIN` | `200` | ✅ PASSED |
| 7. Reporting & Analytics | `GET` | `/reports/reports/leaves` | `SUPER_ADMIN` | `200` | ✅ PASSED |
| 8. Salary Revisions | `POST` | `/salary-revisions` | `SUPER_ADMIN` | `201` | ✅ PASSED |
| 8. Salary Revisions | `GET` | `/salary-revisions` | `SUPER_ADMIN` | `200` | ✅ PASSED |
| 8. Salary Revisions | `GET` | `/salary-revisions/REV003` | `SUPER_ADMIN` | `200` | ✅ PASSED |
| 8. Salary Revisions | `PATCH` | `/salary-revisions/REV003/reject` | `SUPER_ADMIN` | `200` | ✅ PASSED |

---

## Detailed Response Logs

### GET /finance/dashboard (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Finance dashboard statistics retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "totalExpenses": 0,
    "totalPayroll": 0,
    "pendingExpensesCount": 0,
    "pendingExpensesAmount": 0,
    "pendingPayrollCount": 0,
    "pendingPayrollAmount": 0,
    "totalOutbound": 0
  }
}
```

---

### GET /finance/analytics/monthly (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Monthly finance analytics retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": [
    {
      "month": "Jan",
      "year": 2026,
      "expense": 0,
      "revenue": 90000.0
    },
    {
      "month": "Feb",
      "year": 2026,
      "expense": 0,
      "revenue": 95000.0
    },
    {
      "month": "Mar",
      "year": 2026,
      "expense": 0,
      "revenue": 100000.0
    },
    {
      "month": "Apr",
      "year": 2026,
      "expense": 0,
      "revenue": 105000.0
    },
    {
      "month": "May",
      "year": 2026,
      "expense": 0,
      "revenue": 110000.0
    },
    {
      "month": "Jun",
      "year": 2026,
      "expense": 0,
      "revenue": 115000.0
    }
  ]
}
```

---

### GET /finance/transactions/recent (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Recent finance transactions retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": [
    {
      "id": "EXP-2",
      "type": "EXPENSE",
      "title": "Premium Client Travel",
      "amount": 4200.0,
      "status": "REIMBURSED",
      "date": "2026-06-19T10:12:12.706618",
      "reference": "Airport cab and client dining charges",
      "employeeName": "Employee User"
    },
    {
      "id": "EXP-1",
      "type": "EXPENSE",
      "title": "Premium Client Travel",
      "amount": 4200.0,
      "status": "REIMBURSED",
      "date": "2026-06-19T10:11:52.659643",
      "reference": "Airport cab and client dining charges",
      "employeeName": "Employee User"
    }
  ]
}
```

---

### GET /finance/salary/summary (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Payroll/Salary overview summary retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "totalBasicSalary": 0,
    "totalAllowances": 0,
    "totalHra": 0,
    "totalDeductions": 0,
    "totalNetPay": 0,
    "employeeCount": 0
  }
}
```

---

### GET /finance/report (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Custom finance report generated successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "startDate": "2026-05-20",
    "endDate": "2026-06-19",
    "totalExpenses": 0,
    "totalPayroll": 0,
    "totalOutbound": 0,
    "expenseCount": 0,
    "payrollCount": 0,
    "details": []
  }
}
```

---

### GET /my-expenses/dashboard (EMPLOYEE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Dashboard statistics retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "employee": {
      "employeeId": 3,
      "employeeCode": "EMP003",
      "fullName": "Employee User",
      "department": "Employee"
    },
    "summary": {
      "totalClaims": 2,
      "pendingApproval": 0,
      "approvedClaims": 2,
      "rejectedClaims": 0,
      "reimbursedClaims": 2,
      "totalClaimAmount": 8400.0,
      "pendingAmount": 0,
      "approvedAmount": 8400.0,
      "reimbursedAmount": 8400.0,
      "currency": "INR"
    },
    "financialYear": "FY-2025-26",
    "lastUpdatedAt": "2026-06-19T10:16:25.552263425"
  }
}
```

---

### GET /my-expenses (EMPLOYEE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Expense claims retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "content": [
      {
        "expenseId": 1,
        "expenseNumber": "EXP-2026-8DEE28E4",
        "category": "TRAVEL",
        "title": "Premium Client Travel",
        "expenseDate": "2026-06-19",
        "amount": 4200.0,
        "currency": "INR",
        "status": "REIMBURSED",
        "submittedAt": "2026-06-19T10:11:52.659775",
        "reimbursementStatus": "PAID",
        "actions": {
          "canEdit": false,
          "canWithdraw": false,
          "canView": true
        }
      },
      {
        "expenseId": 2,
        "expenseNumber": "EXP-2026-34F7E892",
        "category": "TRAVEL",
        "title": "Premium Client Travel",
        "expenseDate": "2026-06-19",
        "amount": 4200.0,
        "currency": "INR",
        "status": "REIMBURSED",
        "submittedAt": "2026-06-19T10:12:12.706663",
        "reimbursementStatus": "PAID",
        "actions": {
          "canEdit": false,
          "canWithdraw": false,
          "canView": true
        }
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 2,
      "totalPages": 1,
      "hasNext": false,
      "hasPrevious": false
    }
  }
}
```

---

### GET /my-expenses/categories (EMPLOYEE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "categories": [
      {
        "code": "TRAVEL",
        "name": "Travel",
        "maxLimit": 10000.0,
        "requiresReceipt": true
      },
      {
        "code": "MEALS",
        "name": "Meals",
        "maxLimit": 2000.0,
        "requiresReceipt": true
      },
      {
        "code": "INTERNET",
        "name": "Internet",
        "maxLimit": 1500.0,
        "requiresReceipt": false
      }
    ]
  }
}
```

---

### GET /finance/expenses/categories (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Expense breakdown by category retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": []
}
```

---

### GET /expense-categories (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Expense categories retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": [
    {
      "code": "TRAVEL",
      "description": "Travel expenses",
      "id": 1,
      "maxLimit": 10000.0,
      "name": "Travel",
      "requiresReceipt": true
    },
    {
      "code": "MEALS",
      "description": "Meals and dining expenses",
      "id": 2,
      "maxLimit": 2000.0,
      "name": "Meals",
      "requiresReceipt": true
    },
    {
      "code": "INTERNET",
      "description": "Internet and phone allowance",
      "id": 3,
      "maxLimit": 1500.0,
      "name": "Internet",
      "requiresReceipt": false
    }
  ]
}
```

---

### GET /payroll-runs/stats (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Payroll statistics retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "totalRecords": 0,
    "totalBasicSalary": 0,
    "totalAllowances": 0,
    "totalDeductions": 0,
    "totalNetPay": 0,
    "statusDistribution": {
      "GENERATED": 0,
      "REVIEWED": 0,
      "APPROVED": 0,
      "PROCESSED": 0,
      "PAID": 0
    }
  }
}
```

---

### GET /payroll-runs/reports (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Payroll statistics retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "totalRecords": 0,
    "totalBasicSalary": 0,
    "totalAllowances": 0,
    "totalDeductions": 0,
    "totalNetPay": 0,
    "statusDistribution": {
      "GENERATED": 0,
      "REVIEWED": 0,
      "APPROVED": 0,
      "PROCESSED": 0,
      "PAID": 0
    }
  }
}
```

---

### GET /payslips/export (FINANCE)
**Status**: 200 | **Success**: True

```json
ID,Payslip Number,Employee ID,Employee Name,Month,Year,Net Pay,Generated At

```

---

### GET /my-payslips/annual-statement (EMPLOYEE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Annual salary statement retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "financialYear": "FY 2025-26",
    "salarySummary": {
      "totalGrossSalary": 0,
      "totalDeductions": 0,
      "totalNetSalary": 0,
      "monthsProcessed": 0
    },
    "generatedAt": "2026-06-19T10:16:25.597455299"
  }
}
```

---

### POST /fnf-settlements (FINANCE)
**Status**: 201 | **Success**: True

```json
{
  "success": true,
  "message": "Settlement created successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "createdAt": "2026-06-19T10:16:25.602324313",
    "employeeId": 3,
    "gratuity": 15000.0,
    "id": 2,
    "netAmount": 27000.0,
    "notes": "Test exit settlement for employee 3",
    "noticePay": 5000.0,
    "otherDeductions": 1000.0,
    "status": "PENDING",
    "unpaidSalary": 8000.0,
    "updatedAt": "2026-06-19T10:16:25.602326126"
  }
}
```

---

### GET /fnf-settlements (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Settlements list retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": [
    {
      "createdAt": "2026-06-19T10:12:16.697876",
      "employeeId": 3,
      "gratuity": 15000.0,
      "id": 1,
      "netAmount": 27000.0,
      "notes": "Test exit settlement for employee 3",
      "noticePay": 5000.0,
      "otherDeductions": 1000.0,
      "status": "REJECTED",
      "unpaidSalary": 8000.0,
      "updatedAt": "2026-06-19T10:12:16.767312"
    },
    {
      "createdAt": "2026-06-19T10:16:25.602324",
      "employeeId": 3,
      "gratuity": 15000.0,
      "id": 2,
      "netAmount": 27000.0,
      "notes": "Test exit settlement for employee 3",
      "noticePay": 5000.0,
      "otherDeductions": 1000.0,
      "status": "PENDING",
      "unpaidSalary": 8000.0,
      "updatedAt": "2026-06-19T10:16:25.602326"
    }
  ]
}
```

---

### GET /fnf-settlements/2 (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Settlement details retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "createdAt": "2026-06-19T10:16:25.602324",
    "employeeId": 3,
    "gratuity": 15000.0,
    "id": 2,
    "netAmount": 27000.0,
    "notes": "Test exit settlement for employee 3",
    "noticePay": 5000.0,
    "otherDeductions": 1000.0,
    "status": "PENDING",
    "unpaidSalary": 8000.0,
    "updatedAt": "2026-06-19T10:16:25.602326"
  }
}
```

---

### GET /fnf-settlements/employee/3 (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Settlement details retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "createdAt": "2026-06-19T10:12:16.697876",
    "employeeId": 3,
    "gratuity": 15000.0,
    "id": 1,
    "netAmount": 27000.0,
    "notes": "Test exit settlement for employee 3",
    "noticePay": 5000.0,
    "otherDeductions": 1000.0,
    "status": "REJECTED",
    "unpaidSalary": 8000.0,
    "updatedAt": "2026-06-19T10:12:16.767312"
  }
}
```

---

### POST /fnf-settlements/2/approve (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Settlement approved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "createdAt": "2026-06-19T10:16:25.602324",
    "employeeId": 3,
    "gratuity": 15000.0,
    "id": 2,
    "netAmount": 27000.0,
    "notes": "Test exit settlement for employee 3",
    "noticePay": 5000.0,
    "otherDeductions": 1000.0,
    "status": "APPROVED",
    "unpaidSalary": 8000.0,
    "updatedAt": "2026-06-19T10:16:25.625059552"
  }
}
```

---

### POST /fnf-settlements/2/process (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Settlement processed successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "createdAt": "2026-06-19T10:16:25.602324",
    "employeeId": 3,
    "gratuity": 15000.0,
    "id": 2,
    "netAmount": 27000.0,
    "notes": "Test exit settlement for employee 3",
    "noticePay": 5000.0,
    "otherDeductions": 1000.0,
    "status": "PROCESSED",
    "unpaidSalary": 8000.0,
    "updatedAt": "2026-06-19T10:16:25.632600875"
  }
}
```

---

### POST /fnf-settlements/2/reject (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Settlement rejected successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "createdAt": "2026-06-19T10:16:25.602324",
    "employeeId": 3,
    "gratuity": 15000.0,
    "id": 2,
    "netAmount": 27000.0,
    "notes": "Test exit settlement for employee 3",
    "noticePay": 5000.0,
    "otherDeductions": 1000.0,
    "status": "REJECTED",
    "unpaidSalary": 8000.0,
    "updatedAt": "2026-06-19T10:16:25.639347834"
  }
}
```

---

### POST /finance/onboarding (FINANCE)
**Status**: 201 | **Success**: True

```json
{
  "success": true,
  "message": "Finance onboarding process started successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "bankAccountNumber": null,
    "bankName": null,
    "bankRoutingNumber": null,
    "bankSwiftCode": null,
    "budgetCurrency": null,
    "budgetDepartmentBreakdown": null,
    "budgetTotal": null,
    "companyAddress": null,
    "companyName": null,
    "companyPhone": null,
    "companyRegistrationNumber": null,
    "companyTaxId": null,
    "companyWebsite": null,
    "completed": false,
    "createdAt": "2026-06-19T10:16:25.647296534",
    "id": 2,
    "paymentCurrency": null,
    "paymentMethod": null,
    "payrollCycleEndDay": null,
    "payrollCycleStartDay": null,
    "status": "DRAFT",
    "stepProgress": 0,
    "taxFinancialYear": null,
    "taxRate": null,
    "taxRegime": null,
    "updatedAt": "2026-06-19T10:16:25.647296744",
    "validated": false
  }
}
```

---

### GET /finance/onboarding/current (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Current finance onboarding retrieved",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "bankAccountNumber": null,
    "bankName": null,
    "bankRoutingNumber": null,
    "bankSwiftCode": null,
    "budgetCurrency": null,
    "budgetDepartmentBreakdown": null,
    "budgetTotal": null,
    "companyAddress": null,
    "companyName": null,
    "companyPhone": null,
    "companyRegistrationNumber": null,
    "companyTaxId": null,
    "companyWebsite": null,
    "completed": false,
    "createdAt": "2026-06-19T10:16:25.647297",
    "id": 2,
    "paymentCurrency": null,
    "paymentMethod": null,
    "payrollCycleEndDay": null,
    "payrollCycleStartDay": null,
    "status": "DRAFT",
    "stepProgress": 0,
    "taxFinancialYear": null,
    "taxRate": null,
    "taxRegime": null,
    "updatedAt": "2026-06-19T10:16:25.647297",
    "validated": false
  }
}
```

---

### GET /finance/onboarding/2 (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Finance onboarding details retrieved",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "bankAccountNumber": null,
    "bankName": null,
    "bankRoutingNumber": null,
    "bankSwiftCode": null,
    "budgetCurrency": null,
    "budgetDepartmentBreakdown": null,
    "budgetTotal": null,
    "companyAddress": null,
    "companyName": null,
    "companyPhone": null,
    "companyRegistrationNumber": null,
    "companyTaxId": null,
    "companyWebsite": null,
    "completed": false,
    "createdAt": "2026-06-19T10:16:25.647297",
    "id": 2,
    "paymentCurrency": null,
    "paymentMethod": null,
    "payrollCycleEndDay": null,
    "payrollCycleStartDay": null,
    "status": "DRAFT",
    "stepProgress": 0,
    "taxFinancialYear": null,
    "taxRate": null,
    "taxRegime": null,
    "updatedAt": "2026-06-19T10:16:25.647297",
    "validated": false
  }
}
```

---

### GET /finance/onboarding/2/progress (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Finance onboarding progress calculated",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "progressPercentage": 0,
    "status": "DRAFT",
    "id": 2
  }
}
```

---

### PATCH /finance/onboarding/2/company (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Company settings step updated",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "bankAccountNumber": null,
    "bankName": null,
    "bankRoutingNumber": null,
    "bankSwiftCode": null,
    "budgetCurrency": null,
    "budgetDepartmentBreakdown": null,
    "budgetTotal": null,
    "companyAddress": null,
    "companyName": "Apex Corp",
    "companyPhone": null,
    "companyRegistrationNumber": null,
    "companyTaxId": null,
    "companyWebsite": null,
    "completed": false,
    "createdAt": "2026-06-19T10:16:25.647297",
    "id": 2,
    "paymentCurrency": null,
    "paymentMethod": null,
    "payrollCycleEndDay": null,
    "payrollCycleStartDay": null,
    "status": "DRAFT",
    "stepProgress": 17,
    "taxFinancialYear": null,
    "taxRate": null,
    "taxRegime": null,
    "updatedAt": "2026-06-19T10:16:25.669442744",
    "validated": false
  }
}
```

---

### PATCH /finance/onboarding/2/bank-account (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Bank account settings step updated",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "bankAccountNumber": null,
    "bankName": "First Reserve Bank",
    "bankRoutingNumber": null,
    "bankSwiftCode": null,
    "budgetCurrency": null,
    "budgetDepartmentBreakdown": null,
    "budgetTotal": null,
    "companyAddress": null,
    "companyName": "Apex Corp",
    "companyPhone": null,
    "companyRegistrationNumber": null,
    "companyTaxId": null,
    "companyWebsite": null,
    "completed": false,
    "createdAt": "2026-06-19T10:16:25.647297",
    "id": 2,
    "paymentCurrency": null,
    "paymentMethod": null,
    "payrollCycleEndDay": null,
    "payrollCycleStartDay": null,
    "status": "DRAFT",
    "stepProgress": 34,
    "taxFinancialYear": null,
    "taxRate": null,
    "taxRegime": null,
    "updatedAt": "2026-06-19T10:16:25.675012938",
    "validated": false
  }
}
```

---

### PATCH /finance/onboarding/2/tax (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Tax settings step updated",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "bankAccountNumber": null,
    "bankName": "First Reserve Bank",
    "bankRoutingNumber": null,
    "bankSwiftCode": null,
    "budgetCurrency": null,
    "budgetDepartmentBreakdown": null,
    "budgetTotal": null,
    "companyAddress": null,
    "companyName": "Apex Corp",
    "companyPhone": null,
    "companyRegistrationNumber": null,
    "companyTaxId": null,
    "companyWebsite": null,
    "completed": false,
    "createdAt": "2026-06-19T10:16:25.647297",
    "id": 2,
    "paymentCurrency": null,
    "paymentMethod": null,
    "payrollCycleEndDay": null,
    "payrollCycleStartDay": null,
    "status": "DRAFT",
    "stepProgress": 34,
    "taxFinancialYear": null,
    "taxRate": 12.5,
    "taxRegime": null,
    "updatedAt": "2026-06-19T10:16:25.680652652",
    "validated": false
  }
}
```

---

### PATCH /finance/onboarding/2/payment-method (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Payment method settings step updated",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "bankAccountNumber": null,
    "bankName": "First Reserve Bank",
    "bankRoutingNumber": null,
    "bankSwiftCode": null,
    "budgetCurrency": null,
    "budgetDepartmentBreakdown": null,
    "budgetTotal": null,
    "companyAddress": null,
    "companyName": "Apex Corp",
    "companyPhone": null,
    "companyRegistrationNumber": null,
    "companyTaxId": null,
    "companyWebsite": null,
    "completed": false,
    "createdAt": "2026-06-19T10:16:25.647297",
    "id": 2,
    "paymentCurrency": null,
    "paymentMethod": "DIRECT_DEPOSIT",
    "payrollCycleEndDay": null,
    "payrollCycleStartDay": null,
    "status": "DRAFT",
    "stepProgress": 50,
    "taxFinancialYear": null,
    "taxRate": 12.5,
    "taxRegime": null,
    "updatedAt": "2026-06-19T10:16:25.68622504",
    "validated": false
  }
}
```

---

### PATCH /finance/onboarding/2/payroll (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Payroll settings step updated",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "bankAccountNumber": null,
    "bankName": "First Reserve Bank",
    "bankRoutingNumber": null,
    "bankSwiftCode": null,
    "budgetCurrency": null,
    "budgetDepartmentBreakdown": null,
    "budgetTotal": null,
    "companyAddress": null,
    "companyName": "Apex Corp",
    "companyPhone": null,
    "companyRegistrationNumber": null,
    "companyTaxId": null,
    "companyWebsite": null,
    "completed": false,
    "createdAt": "2026-06-19T10:16:25.647297",
    "id": 2,
    "paymentCurrency": null,
    "paymentMethod": "DIRECT_DEPOSIT",
    "payrollCycleEndDay": null,
    "payrollCycleStartDay": null,
    "status": "DRAFT",
    "stepProgress": 50,
    "taxFinancialYear": null,
    "taxRate": 12.5,
    "taxRegime": null,
    "updatedAt": "2026-06-19T10:16:25.691163664",
    "validated": false
  }
}
```

---

### PATCH /finance/onboarding/2/budget (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Budget settings step updated",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "bankAccountNumber": null,
    "bankName": "First Reserve Bank",
    "bankRoutingNumber": null,
    "bankSwiftCode": null,
    "budgetCurrency": null,
    "budgetDepartmentBreakdown": null,
    "budgetTotal": null,
    "companyAddress": null,
    "companyName": "Apex Corp",
    "companyPhone": null,
    "companyRegistrationNumber": null,
    "companyTaxId": null,
    "companyWebsite": null,
    "completed": false,
    "createdAt": "2026-06-19T10:16:25.647297",
    "id": 2,
    "paymentCurrency": null,
    "paymentMethod": "DIRECT_DEPOSIT",
    "payrollCycleEndDay": null,
    "payrollCycleStartDay": null,
    "status": "DRAFT",
    "stepProgress": 50,
    "taxFinancialYear": null,
    "taxRate": 12.5,
    "taxRegime": null,
    "updatedAt": "2026-06-19T10:16:25.696912183",
    "validated": false
  }
}
```

---

### POST /finance/onboarding/2/validate (FINANCE)
**Status**: 400 | **Success**: False

```json
{
  "success": false,
  "message": "Validation failed: Company settings: Company tax ID is required, Bank account settings: Account number is required, Tax settings: Tax regime is required, Payroll settings: Pay cycle start day must be between 1 and 31, Payroll settings: Pay cycle end day must be between 1 and 31, Budget settings: Total budget must be greater than zero",
  "errorCode": "OB_004",
  "timestamp": "2026-06-19T04:46:25Z"
}
```

---

### POST /finance/onboarding/2/complete (FINANCE)
**Status**: 400 | **Success**: False

```json
{
  "success": false,
  "message": "Cannot complete onboarding. Validation failed: Company settings: Company tax ID is required, Bank account settings: Account number is required, Tax settings: Tax regime is required, Payroll settings: Pay cycle start day must be between 1 and 31, Payroll settings: Pay cycle end day must be between 1 and 31, Budget settings: Total budget must be greater than zero",
  "errorCode": "OB_004",
  "timestamp": "2026-06-19T04:46:25Z"
}
```

---

### GET /finance/payments/pending (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Pending due payments retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": []
}
```

---

### GET /payroll-runs (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Payroll records retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": []
}
```

---

### POST /payroll-runs/process (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Batch payroll processing completed",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "processedCount": 0,
    "processedRecords": [],
    "errors": []
  }
}
```

---

### GET /reports/dashboard/admin (SUPER_ADMIN)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Admin dashboard statistics retrieved",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "employeeCount": 6,
    "activeSessions": 24,
    "departmentCount": 0,
    "pendingLeaves": 0,
    "processedPayroll": 0
  }
}
```

---

### GET /reports/reports/payroll (SUPER_ADMIN)
**Status**: 200 | **Success**: True

```json
CSV data (90 bytes)
```

---

### GET /reports/reports/employees (SUPER_ADMIN)
**Status**: 200 | **Success**: True

```json
CSV data (565 bytes)
```

---

### GET /reports/reports/attendance (SUPER_ADMIN)
**Status**: 200 | **Success**: True

```json
CSV data (54 bytes)
```

---

### GET /reports/reports/leaves (SUPER_ADMIN)
**Status**: 200 | **Success**: True

```json
CSV data (73 bytes)
```

---

### POST /salary-revisions (SUPER_ADMIN)
**Status**: 201 | **Success**: True

```json
{
  "success": true,
  "message": "Salary increment request created successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "revisionId": "REV003",
    "employeeId": "EMP003",
    "employeeName": "Employee User",
    "currentSalary": 85000.0,
    "incrementPercentage": 12.5,
    "incrementAmount": 10625.0,
    "newSalary": 95625.0,
    "effectiveDate": "2026-07-01",
    "reason": "Annual performance-based raise",
    "status": "PENDING",
    "createdAt": "2026-06-19T10:16:25Z"
  }
}
```

---

### GET /salary-revisions (SUPER_ADMIN)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Salary revisions retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "content": [
      {
        "revisionId": "REV001",
        "employeeId": "EMP003",
        "employeeName": "Employee User",
        "incrementPercentage": 12.5,
        "newSalary": 95625.0,
        "effectiveDate": "2026-07-01",
        "status": "REJECTED"
      },
      {
        "revisionId": "REV002",
        "employeeId": "EMP003",
        "employeeName": "Employee User",
        "incrementPercentage": 12.5,
        "newSalary": 95625.0,
        "effectiveDate": "2026-07-01",
        "status": "PENDING"
      },
      {
        "revisionId": "REV003",
        "employeeId": "EMP003",
        "employeeName": "Employee User",
        "incrementPercentage": 12.5,
        "newSalary": 95625.0,
        "effectiveDate": "2026-07-01",
        "status": "PENDING"
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 3,
      "totalPages": 1,
      "last": true
    }
  }
}
```

---

### GET /salary-revisions/REV003 (SUPER_ADMIN)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Salary revision details retrieved successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "revisionId": "REV003",
    "employeeId": "EMP003",
    "employeeName": "Employee User",
    "currentSalary": 85000.0,
    "incrementPercentage": 12.5,
    "incrementAmount": 10625.0,
    "newSalary": 95625.0,
    "effectiveDate": "2026-07-01",
    "reason": "Annual performance-based raise",
    "status": "PENDING",
    "createdAt": "2026-06-19T10:16:25Z"
  }
}
```

---

### PATCH /salary-revisions/REV003/reject (SUPER_ADMIN)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Salary revision rejected successfully",
  "timestamp": "2026-06-19T04:46:25Z",
  "data": {
    "revisionId": "REV003",
    "status": "REJECTED",
    "rejectedAt": "2026-06-19T10:16:25Z",
    "reason": "Incorrect increment details"
  }
}
```

---

