# Finance Onboarding Endpoints Verification Report
Generated at: 2026-06-20 12:54:42 (UTC)

### Status Summary: **24 / 24 Tests Passed**

| Method | Endpoint | Authorized Role | Expected Status | Actual Status | Result |
| --- | --- | --- | --- | --- | --- |
| `POST` | `/finance/onboarding` | `FINANCE` | `201` | `201` | ✅ PASSED |
| `GET` | `/finance/onboarding/dashboard` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `GET` | `/finance/onboarding/pending-reviews` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `PATCH` | `/finance/onboarding/1/reject` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `PATCH` | `/finance/onboarding/1/verify` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `GET` | `/finance/onboarding/3/bank-details` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `PUT` | `/finance/onboarding/3/bank-details` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `PATCH` | `/finance/onboarding/3/bank-details/verify` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `GET` | `/finance/onboarding/3/tax-details` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `PUT` | `/finance/onboarding/3/tax-details` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `GET` | `/finance/onboarding/3/statutory-details` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `PUT` | `/finance/onboarding/3/statutory-details` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `POST` | `/finance/onboarding/3/salary-structure` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `GET` | `/finance/onboarding/3/salary-structure` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `POST` | `/finance/onboarding/calculate-ctc` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `GET` | `/finance/onboarding/3/salary-preview` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `POST` | `/finance/onboarding/3/activate-payroll` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `GET` | `/finance/onboarding/3/payroll-status` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `GET` | `/finance/onboarding/1/approval-history` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `GET` | `/finance/onboarding/dashboard/recent-activities` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `GET` | `/finance/onboarding/reports` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `GET` | `/finance/onboarding/reports/export` | `FINANCE` | `200` | `200` | ✅ PASSED |
| `GET` | `/finance/onboarding` | `EMPLOYEE` | `403` | `403` | ✅ PASSED |
| `GET` | `/finance/onboarding/dashboard` | `EMPLOYEE` | `403` | `403` | ✅ PASSED |

---

## Detailed Response Logs

### POST /finance/onboarding (FINANCE)
**Expected Status**: 201 | **Actual Status**: 201 | **Success**: True

```json
{
  "success": true,
  "message": "Employee finance onboarding session initialized successfully",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "allowances": 0,
    "bankAccountNumber": "1122334455",
    "bankIfsc": "ICIC0000123",
    "bankName": "ICICI Bank",
    "bankVerificationNotes": null,
    "bankVerificationStatus": "PENDING",
    "basicSalary": 0,
    "createdAt": "2026-06-20T12:54:42.375034752",
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "availability": "AVAILABLE",
      "currentStatus": "WORKING",
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP003",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 3,
      "joiningDate": "2026-06-10",
      "lastActiveAt": "2026-06-20T12:54:13.757229",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550003",
      "profileImage": null,
      "status": "ACTIVE",
      "team": null,
      "workMode": "OFFICE"
    },
    "hra": 0,
    "id": 1,
    "monthlyCtc": 0,
    "panNumber": "ABCDE5678G",
    "panVerificationNotes": null,
    "panVerificationStatus": "PENDING",
    "payrollActivated": false,
    "payrollActivatedAt": null,
    "salaryStructureAssigned": false,
    "status": "PENDING",
    "uanNumber": "100900800700",
    "uanVerificationNotes": null,
    "uanVerificationStatus": "PENDING",
    "updatedAt": "2026-06-20T12:54:42.375052085"
  }
}
```

---

### GET /finance/onboarding/dashboard (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Finance onboarding summary counts retrieved",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "pendingVerification": 1,
    "salaryAssignmentPending": 1,
    "payrollActivationPending": 0,
    "completed": 0
  }
}
```

---

### GET /finance/onboarding/pending-reviews (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Pending finance onboarding reviews retrieved",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": [
    {
      "allowances": 0.0,
      "bankAccountNumber": "1122334455",
      "bankIfsc": "ICIC0000123",
      "bankName": "ICICI Bank",
      "bankVerificationNotes": null,
      "bankVerificationStatus": "PENDING",
      "basicSalary": 0.0,
      "createdAt": "2026-06-20T12:54:42.375035",
      "employee": {
        "address": "123 Corporate Way",
        "annualSalary": 85000.0,
        "availability": "AVAILABLE",
        "currentStatus": "WORKING",
        "department": "Employee",
        "designation": "EMPLOYEE",
        "dob": "1990-01-01",
        "email": "employee@company.com",
        "emergencyContact": "9876543210",
        "employeeId": "EMP003",
        "employmentType": "FULL_TIME",
        "fullName": "Employee User",
        "gender": "MALE",
        "id": 3,
        "joiningDate": "2026-06-10",
        "lastActiveAt": "2026-06-20T12:54:13.757229",
        "location": "Headquarters",
        "manager": null,
        "phone": "5550003",
        "profileImage": null,
        "status": "ACTIVE",
        "team": null,
        "workMode": "OFFICE"
      },
      "hra": 0.0,
      "id": 1,
      "monthlyCtc": 0.0,
      "panNumber": "ABCDE5678G",
      "panVerificationNotes": null,
      "panVerificationStatus": "PENDING",
      "payrollActivated": false,
      "payrollActivatedAt": null,
      "salaryStructureAssigned": false,
      "status": "PENDING",
      "uanNumber": "100900800700",
      "uanVerificationNotes": null,
      "uanVerificationStatus": "PENDING",
      "updatedAt": "2026-06-20T12:54:42.375052"
    }
  ]
}
```

---

### PATCH /finance/onboarding/1/reject (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Employee finance onboarding rejected",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "allowances": 0.0,
    "bankAccountNumber": "1122334455",
    "bankIfsc": "ICIC0000123",
    "bankName": "ICICI Bank",
    "bankVerificationNotes": null,
    "bankVerificationStatus": "PENDING",
    "basicSalary": 0.0,
    "createdAt": "2026-06-20T12:54:42.375035",
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "availability": "AVAILABLE",
      "currentStatus": "WORKING",
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP003",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 3,
      "joiningDate": "2026-06-10",
      "lastActiveAt": "2026-06-20T12:54:13.757229",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550003",
      "profileImage": null,
      "status": "ACTIVE",
      "team": null,
      "workMode": "OFFICE"
    },
    "hra": 0.0,
    "id": 1,
    "monthlyCtc": 0.0,
    "panNumber": "ABCDE5678G",
    "panVerificationNotes": null,
    "panVerificationStatus": "PENDING",
    "payrollActivated": false,
    "payrollActivatedAt": null,
    "salaryStructureAssigned": false,
    "status": "REJECTED",
    "uanNumber": "100900800700",
    "uanVerificationNotes": null,
    "uanVerificationStatus": "PENDING",
    "updatedAt": "2026-06-20T12:54:42.474125534"
  }
}
```

---

### PATCH /finance/onboarding/1/verify (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Employee finance onboarding verified successfully",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "allowances": 0.0,
    "bankAccountNumber": "1122334455",
    "bankIfsc": "ICIC0000123",
    "bankName": "ICICI Bank",
    "bankVerificationNotes": "Batch verification success",
    "bankVerificationStatus": "VERIFIED",
    "basicSalary": 0.0,
    "createdAt": "2026-06-20T12:54:42.375035",
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "availability": "AVAILABLE",
      "currentStatus": "WORKING",
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP003",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 3,
      "joiningDate": "2026-06-10",
      "lastActiveAt": "2026-06-20T12:54:13.757229",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550003",
      "profileImage": null,
      "status": "ACTIVE",
      "team": null,
      "workMode": "OFFICE"
    },
    "hra": 0.0,
    "id": 1,
    "monthlyCtc": 0.0,
    "panNumber": "ABCDE5678G",
    "panVerificationNotes": "Batch verification success",
    "panVerificationStatus": "VERIFIED",
    "payrollActivated": false,
    "payrollActivatedAt": null,
    "salaryStructureAssigned": false,
    "status": "APPROVED",
    "uanNumber": "100900800700",
    "uanVerificationNotes": "Batch verification success",
    "uanVerificationStatus": "VERIFIED",
    "updatedAt": "2026-06-20T12:54:42.505641139"
  }
}
```

---

### GET /finance/onboarding/3/bank-details (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Bank details retrieved",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "bankName": "ICICI Bank",
    "bankAccountNumber": "1122334455",
    "bankIfsc": "ICIC0000123",
    "bankVerificationStatus": "VERIFIED",
    "bankVerificationNotes": "Batch verification success"
  }
}
```

---

### PUT /finance/onboarding/3/bank-details (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Bank details updated successfully",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "allowances": 0.0,
    "bankAccountNumber": "9999999999",
    "bankIfsc": "HDFC0009999",
    "bankName": "HDFC Bank",
    "bankVerificationNotes": "Details updated by user",
    "bankVerificationStatus": "PENDING",
    "basicSalary": 0.0,
    "createdAt": "2026-06-20T12:54:42.375035",
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "availability": "AVAILABLE",
      "currentStatus": "WORKING",
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP003",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 3,
      "joiningDate": "2026-06-10",
      "lastActiveAt": "2026-06-20T12:54:13.757229",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550003",
      "profileImage": null,
      "status": "ACTIVE",
      "team": null,
      "workMode": "OFFICE"
    },
    "hra": 0.0,
    "id": 1,
    "monthlyCtc": 0.0,
    "panNumber": "ABCDE5678G",
    "panVerificationNotes": "Batch verification success",
    "panVerificationStatus": "VERIFIED",
    "payrollActivated": false,
    "payrollActivatedAt": null,
    "salaryStructureAssigned": false,
    "status": "APPROVED",
    "uanNumber": "100900800700",
    "uanVerificationNotes": "Batch verification success",
    "uanVerificationStatus": "VERIFIED",
    "updatedAt": "2026-06-20T12:54:42.552904338"
  }
}
```

---

### PATCH /finance/onboarding/3/bank-details/verify (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Bank details verified successfully",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "allowances": 0.0,
    "bankAccountNumber": "9999999999",
    "bankIfsc": "HDFC0009999",
    "bankName": "HDFC Bank",
    "bankVerificationNotes": "Bank account successfully verified",
    "bankVerificationStatus": "VERIFIED",
    "basicSalary": 0.0,
    "createdAt": "2026-06-20T12:54:42.375035",
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "availability": "AVAILABLE",
      "currentStatus": "WORKING",
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP003",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 3,
      "joiningDate": "2026-06-10",
      "lastActiveAt": "2026-06-20T12:54:13.757229",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550003",
      "profileImage": null,
      "status": "ACTIVE",
      "team": null,
      "workMode": "OFFICE"
    },
    "hra": 0.0,
    "id": 1,
    "monthlyCtc": 0.0,
    "panNumber": "ABCDE5678G",
    "panVerificationNotes": "Batch verification success",
    "panVerificationStatus": "VERIFIED",
    "payrollActivated": false,
    "payrollActivatedAt": null,
    "salaryStructureAssigned": false,
    "status": "APPROVED",
    "uanNumber": "100900800700",
    "uanVerificationNotes": "Batch verification success",
    "uanVerificationStatus": "VERIFIED",
    "updatedAt": "2026-06-20T12:54:42.569799159"
  }
}
```

---

### GET /finance/onboarding/3/tax-details (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Tax details retrieved",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "panNumber": "ABCDE5678G",
    "panVerificationStatus": "VERIFIED",
    "panVerificationNotes": "Batch verification success"
  }
}
```

---

### PUT /finance/onboarding/3/tax-details (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Tax details updated successfully",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "allowances": 0.0,
    "bankAccountNumber": "9999999999",
    "bankIfsc": "HDFC0009999",
    "bankName": "HDFC Bank",
    "bankVerificationNotes": "Bank account successfully verified",
    "bankVerificationStatus": "VERIFIED",
    "basicSalary": 0.0,
    "createdAt": "2026-06-20T12:54:42.375035",
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "availability": "AVAILABLE",
      "currentStatus": "WORKING",
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP003",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 3,
      "joiningDate": "2026-06-10",
      "lastActiveAt": "2026-06-20T12:54:13.757229",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550003",
      "profileImage": null,
      "status": "ACTIVE",
      "team": null,
      "workMode": "OFFICE"
    },
    "hra": 0.0,
    "id": 1,
    "monthlyCtc": 0.0,
    "panNumber": "XYZZY1234A",
    "panVerificationNotes": "Details updated by user",
    "panVerificationStatus": "PENDING",
    "payrollActivated": false,
    "payrollActivatedAt": null,
    "salaryStructureAssigned": false,
    "status": "APPROVED",
    "uanNumber": "100900800700",
    "uanVerificationNotes": "Batch verification success",
    "uanVerificationStatus": "VERIFIED",
    "updatedAt": "2026-06-20T12:54:42.604506498"
  }
}
```

---

### GET /finance/onboarding/3/statutory-details (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Statutory details retrieved",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "uanNumber": "100900800700",
    "uanVerificationStatus": "VERIFIED",
    "uanVerificationNotes": "Batch verification success"
  }
}
```

---

### PUT /finance/onboarding/3/statutory-details (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Statutory details updated successfully",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "allowances": 0.0,
    "bankAccountNumber": "9999999999",
    "bankIfsc": "HDFC0009999",
    "bankName": "HDFC Bank",
    "bankVerificationNotes": "Bank account successfully verified",
    "bankVerificationStatus": "VERIFIED",
    "basicSalary": 0.0,
    "createdAt": "2026-06-20T12:54:42.375035",
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "availability": "AVAILABLE",
      "currentStatus": "WORKING",
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP003",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 3,
      "joiningDate": "2026-06-10",
      "lastActiveAt": "2026-06-20T12:54:13.757229",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550003",
      "profileImage": null,
      "status": "ACTIVE",
      "team": null,
      "workMode": "OFFICE"
    },
    "hra": 0.0,
    "id": 1,
    "monthlyCtc": 0.0,
    "panNumber": "XYZZY1234A",
    "panVerificationNotes": "Details updated by user",
    "panVerificationStatus": "PENDING",
    "payrollActivated": false,
    "payrollActivatedAt": null,
    "salaryStructureAssigned": false,
    "status": "APPROVED",
    "uanNumber": "999988887777",
    "uanVerificationNotes": "Details updated by user",
    "uanVerificationStatus": "PENDING",
    "updatedAt": "2026-06-20T12:54:42.638939401"
  }
}
```

---

### POST /finance/onboarding/3/salary-structure (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Salary structure assigned successfully",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "allowances": 12000,
    "bankAccountNumber": "9999999999",
    "bankIfsc": "HDFC0009999",
    "bankName": "HDFC Bank",
    "bankVerificationNotes": "Re-verified after update",
    "bankVerificationStatus": "VERIFIED",
    "basicSalary": 45000,
    "createdAt": "2026-06-20T12:54:42.375035",
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "availability": "AVAILABLE",
      "currentStatus": "WORKING",
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP003",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 3,
      "joiningDate": "2026-06-10",
      "lastActiveAt": "2026-06-20T12:54:13.757229",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550003",
      "profileImage": null,
      "status": "ACTIVE",
      "team": null,
      "workMode": "OFFICE"
    },
    "hra": 18000,
    "id": 1,
    "monthlyCtc": 75000,
    "panNumber": "XYZZY1234A",
    "panVerificationNotes": "Re-verified after update",
    "panVerificationStatus": "VERIFIED",
    "payrollActivated": false,
    "payrollActivatedAt": null,
    "salaryStructureAssigned": true,
    "status": "APPROVED",
    "uanNumber": "999988887777",
    "uanVerificationNotes": "Re-verified after update",
    "uanVerificationStatus": "VERIFIED",
    "updatedAt": "2026-06-20T12:54:42.680155584"
  }
}
```

---

### GET /finance/onboarding/3/salary-structure (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Assigned salary structure retrieved",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "basicSalary": 45000.0,
    "hra": 18000.0,
    "allowances": 12000.0,
    "monthlyCtc": 75000.0,
    "assigned": true
  }
}
```

---

### POST /finance/onboarding/calculate-ctc (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "CTC breakup calculated successfully",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "annualCtc": 900000,
    "monthlyCtc": 75000,
    "basicSalary": 37500.0,
    "hra": 15000.0,
    "allowances": 22500.0,
    "providentFund": 4500.0,
    "professionalTax": 200.0,
    "incomeTax": 7500.0,
    "netPay": 62800.0
  }
}
```

---

### GET /finance/onboarding/3/salary-preview (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Salary breakup and preview generated",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "basic": 45000.0,
    "hra": 18000.0,
    "allowances": 12000.0,
    "grossSalary": 75000.0,
    "pf": 1800.0,
    "netSalary": 73200.0
  }
}
```

---

### POST /finance/onboarding/3/activate-payroll (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Employee activated for payroll system",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "allowances": 12000.0,
    "bankAccountNumber": "9999999999",
    "bankIfsc": "HDFC0009999",
    "bankName": "HDFC Bank",
    "bankVerificationNotes": "Re-verified after update",
    "bankVerificationStatus": "VERIFIED",
    "basicSalary": 45000.0,
    "createdAt": "2026-06-20T12:54:42.375035",
    "employee": {
      "address": "123 Corporate Way",
      "annualSalary": 85000.0,
      "availability": "AVAILABLE",
      "currentStatus": "WORKING",
      "department": "Employee",
      "designation": "EMPLOYEE",
      "dob": "1990-01-01",
      "email": "employee@company.com",
      "emergencyContact": "9876543210",
      "employeeId": "EMP003",
      "employmentType": "FULL_TIME",
      "fullName": "Employee User",
      "gender": "MALE",
      "id": 3,
      "joiningDate": "2026-06-10",
      "lastActiveAt": "2026-06-20T12:54:13.757229",
      "location": "Headquarters",
      "manager": null,
      "phone": "5550003",
      "profileImage": null,
      "status": "ACTIVE",
      "team": null,
      "workMode": "OFFICE"
    },
    "hra": 18000.0,
    "id": 1,
    "monthlyCtc": 75000.0,
    "panNumber": "XYZZY1234A",
    "panVerificationNotes": "Re-verified after update",
    "panVerificationStatus": "VERIFIED",
    "payrollActivated": true,
    "payrollActivatedAt": "2026-06-20T12:54:42.749107601",
    "salaryStructureAssigned": true,
    "status": "APPROVED",
    "uanNumber": "999988887777",
    "uanVerificationNotes": "Re-verified after update",
    "uanVerificationStatus": "VERIFIED",
    "updatedAt": "2026-06-20T12:54:42.75006944"
  }
}
```

---

### GET /finance/onboarding/3/payroll-status (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Payroll activation status checked",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": {
    "activated": true,
    "activatedAt": "2026-06-20T12:54:42.749108"
  }
}
```

---

### GET /finance/onboarding/1/approval-history (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Approval and verification action logs retrieved",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": [
    {
      "onboardingId": 1,
      "action": "PAYROLL_ACTIVATED",
      "performedBy": "finance@company.com",
      "notes": "Payroll activated successfully for employee",
      "id": 13,
      "timestamp": "2026-06-20T12:54:42.749321"
    },
    {
      "onboardingId": 1,
      "action": "SALARY_ASSIGNED",
      "performedBy": "finance@company.com",
      "notes": "Salary structure assigned: Basic=45000, HRA=18000, Allowances=12000",
      "id": 12,
      "timestamp": "2026-06-20T12:54:42.679357"
    },
    {
      "onboardingId": 1,
      "action": "VERIFIED_DETAILS",
      "performedBy": "finance@company.com",
      "notes": "Batch verification: Bank=true, PAN=true, UAN=true. Remarks: Re-verified after update",
      "id": 11,
      "timestamp": "2026-06-20T12:54:42.65433"
    },
    {
      "onboardingId": 1,
      "action": "VERIFIED_UAN",
      "performedBy": "finance@company.com",
      "notes": "PF/UAN verification status: PENDING. Notes: Details updated by user",
      "id": 10,
      "timestamp": "2026-06-20T12:54:42.638159"
    },
    {
      "onboardingId": 1,
      "action": "UPDATED",
      "performedBy": "finance@company.com",
      "notes": "Details updated",
      "id": 9,
      "timestamp": "2026-06-20T12:54:42.63574"
    },
    {
      "onboardingId": 1,
      "action": "VERIFIED_PAN",
      "performedBy": "finance@company.com",
      "notes": "PAN verification status: PENDING. Notes: Details updated by user",
      "id": 8,
      "timestamp": "2026-06-20T12:54:42.603844"
    },
    {
      "onboardingId": 1,
      "action": "UPDATED",
      "performedBy": "finance@company.com",
      "notes": "Details updated",
      "id": 7,
      "timestamp": "2026-06-20T12:54:42.601375"
    },
    {
      "onboardingId": 1,
      "action": "VERIFIED_BANK",
      "performedBy": "finance@company.com",
      "notes": "Bank verification status: VERIFIED. Notes: Bank account successfully verified",
      "id": 6,
      "timestamp": "2026-06-20T12:54:42.569135"
    },
    {
      "onboardingId": 1,
      "action": "VERIFIED_BANK",
      "performedBy": "finance@company.com",
      "notes": "Bank verification status: PENDING. Notes: Details updated by user",
      "id": 5,
      "timestamp": "2026-06-20T12:54:42.552325"
    },
    {
      "onboardingId": 1,
      "action": "UPDATED",
      "performedBy": "finance@company.com",
      "notes": "Details updated",
      "id": 4,
      "timestamp": "2026-06-20T12:54:42.549545"
    },
    {
      "onboardingId": 1,
      "action": "VERIFIED_DETAILS",
      "performedBy": "finance@company.com",
      "notes": "Batch verification: Bank=true, PAN=true, UAN=true. Remarks: Batch verification success",
      "id": 3,
      "timestamp": "2026-06-20T12:54:42.504487"
    },
    {
      "onboardingId": 1,
      "action": "REJECTED",
      "performedBy": "finance@company.com",
      "notes": "Rejecting for details test",
      "id": 2,
      "timestamp": "2026-06-20T12:54:42.472801"
    },
    {
      "onboardingId": 1,
      "action": "CREATED",
      "performedBy": "finance@company.com",
      "notes": "Finance onboarding record initialized",
      "id": 1,
      "timestamp": "2026-06-20T12:54:42.377346"
    }
  ]
}
```

---

### GET /finance/onboarding/dashboard/recent-activities (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Recent finance onboarding activities retrieved",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": [
    {
      "id": 13,
      "onboardingId": 1,
      "action": "PAYROLL_ACTIVATED",
      "performedBy": "finance@company.com",
      "notes": "Payroll activated successfully for employee",
      "timestamp": "2026-06-20T12:54:42.749321",
      "employeeName": "Employee User"
    },
    {
      "id": 12,
      "onboardingId": 1,
      "action": "SALARY_ASSIGNED",
      "performedBy": "finance@company.com",
      "notes": "Salary structure assigned: Basic=45000, HRA=18000, Allowances=12000",
      "timestamp": "2026-06-20T12:54:42.679357",
      "employeeName": "Employee User"
    },
    {
      "id": 11,
      "onboardingId": 1,
      "action": "VERIFIED_DETAILS",
      "performedBy": "finance@company.com",
      "notes": "Batch verification: Bank=true, PAN=true, UAN=true. Remarks: Re-verified after update",
      "timestamp": "2026-06-20T12:54:42.65433",
      "employeeName": "Employee User"
    },
    {
      "id": 10,
      "onboardingId": 1,
      "action": "VERIFIED_UAN",
      "performedBy": "finance@company.com",
      "notes": "PF/UAN verification status: PENDING. Notes: Details updated by user",
      "timestamp": "2026-06-20T12:54:42.638159",
      "employeeName": "Employee User"
    },
    {
      "id": 9,
      "onboardingId": 1,
      "action": "UPDATED",
      "performedBy": "finance@company.com",
      "notes": "Details updated",
      "timestamp": "2026-06-20T12:54:42.63574",
      "employeeName": "Employee User"
    },
    {
      "id": 8,
      "onboardingId": 1,
      "action": "VERIFIED_PAN",
      "performedBy": "finance@company.com",
      "notes": "PAN verification status: PENDING. Notes: Details updated by user",
      "timestamp": "2026-06-20T12:54:42.603844",
      "employeeName": "Employee User"
    },
    {
      "id": 7,
      "onboardingId": 1,
      "action": "UPDATED",
      "performedBy": "finance@company.com",
      "notes": "Details updated",
      "timestamp": "2026-06-20T12:54:42.601375",
      "employeeName": "Employee User"
    },
    {
      "id": 6,
      "onboardingId": 1,
      "action": "VERIFIED_BANK",
      "performedBy": "finance@company.com",
      "notes": "Bank verification status: VERIFIED. Notes: Bank account successfully verified",
      "timestamp": "2026-06-20T12:54:42.569135",
      "employeeName": "Employee User"
    },
    {
      "id": 5,
      "onboardingId": 1,
      "action": "VERIFIED_BANK",
      "performedBy": "finance@company.com",
      "notes": "Bank verification status: PENDING. Notes: Details updated by user",
      "timestamp": "2026-06-20T12:54:42.552325",
      "employeeName": "Employee User"
    },
    {
      "id": 4,
      "onboardingId": 1,
      "action": "UPDATED",
      "performedBy": "finance@company.com",
      "notes": "Details updated",
      "timestamp": "2026-06-20T12:54:42.549545",
      "employeeName": "Employee User"
    }
  ]
}
```

---

### GET /finance/onboarding/reports (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Finance onboarding report compiled",
  "timestamp": "2026-06-20T07:24:42Z",
  "data": [
    {
      "onboardingId": 1,
      "employeeId": "EMP003",
      "employeeName": "Employee User",
      "employeeEmail": "employee@company.com",
      "joiningDate": "2026-06-10",
      "status": "APPROVED",
      "bankVerified": true,
      "panVerified": true,
      "uanVerified": true,
      "salaryStructureAssigned": true,
      "payrollActivated": true
    }
  ]
}
```

---

### GET /finance/onboarding/reports/export (FINANCE)
**Expected Status**: 200 | **Actual Status**: 200 | **Success**: True

```json
CSV raw report (231 bytes)
```

---

### GET /finance/onboarding (EMPLOYEE)
**Expected Status**: 403 | **Actual Status**: 403 | **Success**: True

```json
{
  "success": false,
  "message": "Access Denied: Requires finance privileges.",
  "errorCode": "AUTH_002",
  "timestamp": "2026-06-20T07:24:42Z"
}
```

---

### GET /finance/onboarding/dashboard (EMPLOYEE)
**Expected Status**: 403 | **Actual Status**: 403 | **Success**: True

```json
{
  "success": false,
  "message": "Access Denied: Requires finance privileges.",
  "errorCode": "AUTH_002",
  "timestamp": "2026-06-20T07:24:42Z"
}
```

---

