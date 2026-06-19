#!/usr/bin/env python3
import json
import urllib.request
import urllib.error
import time
import os

BASE_URL = "http://localhost:8080/api/v1"
REPORT_PATH = "/home/subashini/Documents/ems-backend/finance_api_verification_results.md"

def make_request(method, endpoint, token=None, data=None):
    url = f"{BASE_URL}{endpoint}"
    req_headers = {
        "Content-Type": "application/json",
        "Accept": "application/json"
    }
    if token:
        req_headers["Authorization"] = f"Bearer {token}"
        
    req_data = None
    if data is not None:
        req_data = json.dumps(data).encode("utf-8")
        
    req = urllib.request.Request(url, data=req_data, headers=req_headers, method=method)
    
    try:
        with urllib.request.urlopen(req) as response:
            resp_body = response.read().decode("utf-8")
            status_code = response.status
            try:
                parsed_json = json.loads(resp_body)
                return status_code, parsed_json
            except json.JSONDecodeError:
                return status_code, resp_body
    except urllib.error.HTTPError as e:
        resp_body = e.read().decode("utf-8")
        try:
            parsed_json = json.loads(resp_body)
            return e.code, parsed_json
        except json.JSONDecodeError:
            return e.code, resp_body
    except Exception as e:
        return 500, {"error": str(e)}

def run_tests():
    # Login users
    print("Logging in users...")
    
    # 1. Finance User
    status, res = make_request("POST", "/auth/login", data={"email": "finance@company.com", "password": "finance@2"})
    if status == 200 and res.get("success"):
        finance_token = res["data"]["tokens"]["accessToken"]
        print("✅ Logged in as Finance")
    else:
        print("❌ Finance login failed", res)
        return
        
    # 2. Employee User
    status, res = make_request("POST", "/auth/login", data={"email": "employee@company.com", "password": "employee@3"})
    if status == 200 and res.get("success"):
        employee_token = res["data"]["tokens"]["accessToken"]
        print("✅ Logged in as Employee")
    else:
        print("❌ Employee login failed", res)
        return
        
    # 3. Super Admin User
    status, res = make_request("POST", "/auth/login", data={"email": "super_admin@company.com", "password": "super_admin@1"})
    if status == 200 and res.get("success"):
        admin_token = res["data"]["tokens"]["accessToken"]
        print("✅ Logged in as Super Admin")
    else:
        print("❌ Super Admin login failed", res)
        return

    results = []
    
    def log_result(category, method, endpoint, role, status_code, response):
        success = status_code in [200, 201]
        results.append({
            "category": category,
            "method": method,
            "endpoint": endpoint,
            "role": role,
            "status_code": status_code,
            "success": success,
            "response": response
        })
        status_symbol = "✅ PASSED" if success else "❌ FAILED"
        print(f"[{category}] {method} {endpoint} ({role}) -> {status_code} {status_symbol}")

    # =========================================================================
    # 💸 Category 1: Finance Dashboard Overview
    # =========================================================================
    for ep in ["/finance/dashboard", "/finance/analytics/monthly", "/finance/transactions/recent", "/finance/salary/summary", "/finance/report"]:
        st, body = make_request("GET", ep, finance_token)
        log_result("1. Dashboard Overview", "GET", ep, "FINANCE", st, body)

    # =========================================================================
    # 💸 Category 2: Expenses & Cost Control
    # =========================================================================
    for ep in ["/my-expenses/dashboard", "/my-expenses", "/my-expenses/categories"]:
        st, body = make_request("GET", ep, employee_token)
        log_result("2. Expenses & Cost Control", "GET", ep, "EMPLOYEE", st, body)
        
    st, body = make_request("GET", "/finance/expenses/categories", finance_token)
    log_result("2. Expenses & Cost Control", "GET", "/finance/expenses/categories", "FINANCE", st, body)
    
    st, body = make_request("GET", "/expense-categories", finance_token)
    log_result("2. Expenses & Cost Control", "GET", "/expense-categories", "FINANCE", st, body)

    # =========================================================================
    # 💸 Category 3: Payroll & Salary Insights
    # =========================================================================
    st, body = make_request("GET", "/payroll-runs/stats", finance_token)
    log_result("3. Payroll & Salary Insights", "GET", "/payroll-runs/stats", "FINANCE", st, body)
    
    st, body = make_request("GET", "/payroll-runs/reports", finance_token)
    log_result("3. Payroll & Salary Insights", "GET", "/payroll-runs/reports", "FINANCE", st, body)
    
    st, body = make_request("GET", "/payslips/export", finance_token)
    log_result("3. Payroll & Salary Insights", "GET", "/payslips/export", "FINANCE", st, body)
    
    st, body = make_request("GET", "/my-payslips/annual-statement", employee_token)
    log_result("3. Payroll & Salary Insights", "GET", "/my-payslips/annual-statement", "EMPLOYEE", st, body)

    # =========================================================================
    # 💸 Category 4: Full & Final Settlement (F&F)
    # =========================================================================
    # Create test F&F settlement first
    fnf_payload = {
        "employeeId": 3,
        "gratuity": 15000.0,
        "noticePay": 5000.0,
        "unpaidSalary": 8000.0,
        "otherDeductions": 1000.0,
        "netAmount": 27000.0,
        "notes": "Test exit settlement for employee 3"
    }
    st, body = make_request("POST", "/fnf-settlements", finance_token, data=fnf_payload)
    log_result("4. F&F Settlement", "POST", "/fnf-settlements", "FINANCE", st, body)
    
    fnf_id = None
    if st == 201 and body.get("success") and body.get("data"):
        fnf_id = body["data"].get("id")
        
    st, body = make_request("GET", "/fnf-settlements", finance_token)
    log_result("4. F&F Settlement", "GET", "/fnf-settlements", "FINANCE", st, body)
    
    if fnf_id:
        st, body = make_request("GET", f"/fnf-settlements/{fnf_id}", finance_token)
        log_result("4. F&F Settlement", "GET", f"/fnf-settlements/{fnf_id}", "FINANCE", st, body)
        
        st, body = make_request("GET", f"/fnf-settlements/employee/3", finance_token)
        log_result("4. F&F Settlement", "GET", f"/fnf-settlements/employee/3", "FINANCE", st, body)
        
        # Approve
        st, body = make_request("POST", f"/fnf-settlements/{fnf_id}/approve", finance_token)
        log_result("4. F&F Settlement", "POST", f"/fnf-settlements/{fnf_id}/approve", "FINANCE", st, body)
        
        # Process
        st, body = make_request("POST", f"/fnf-settlements/{fnf_id}/process", finance_token)
        log_result("4. F&F Settlement", "POST", f"/fnf-settlements/{fnf_id}/process", "FINANCE", st, body)
        
        # Reject (should fail/or show rejected status code, since it is already processed)
        st, body = make_request("POST", f"/fnf-settlements/{fnf_id}/reject", finance_token)
        log_result("4. F&F Settlement", "POST", f"/fnf-settlements/{fnf_id}/reject", "FINANCE", st, body)
    else:
        log_result("4. F&F Settlement", "GET", "/fnf-settlements/999", "FINANCE", 404, {"error": "Skipped, no test settlement ID"})
        log_result("4. F&F Settlement", "GET", "/fnf-settlements/employee/3", "FINANCE", 404, {"error": "Skipped, no test settlement ID"})
        log_result("4. F&F Settlement", "POST", "/fnf-settlements/999/approve", "FINANCE", 404, {"error": "Skipped, no test settlement ID"})
        log_result("4. F&F Settlement", "POST", "/fnf-settlements/999/process", "FINANCE", 404, {"error": "Skipped, no test settlement ID"})
        log_result("4. F&F Settlement", "POST", "/fnf-settlements/999/reject", "FINANCE", 404, {"error": "Skipped, no test settlement ID"})

    # =========================================================================
    # 💸 Category 5: Finance Onboarding (Employee/Company Setup)
    # =========================================================================
    # Create onboarding record first
    st, body = make_request("POST", "/finance/onboarding", finance_token)
    log_result("5. Finance Onboarding", "POST", "/finance/onboarding", "FINANCE", st, body)
    
    ob_id = None
    if st == 201 and body.get("success") and body.get("data"):
        ob_id = body["data"].get("id")
        
    st, body = make_request("GET", "/finance/onboarding/current", finance_token)
    log_result("5. Finance Onboarding", "GET", "/finance/onboarding/current", "FINANCE", st, body)
    
    if ob_id:
        st, body = make_request("GET", f"/finance/onboarding/{ob_id}", finance_token)
        log_result("5. Finance Onboarding", "GET", f"/finance/onboarding/{ob_id}", "FINANCE", st, body)
        
        st, body = make_request("GET", f"/finance/onboarding/{ob_id}/progress", finance_token)
        log_result("5. Finance Onboarding", "GET", f"/finance/onboarding/{ob_id}/progress", "FINANCE", st, body)
        
        # Patch updates
        st, body = make_request("PATCH", f"/finance/onboarding/{ob_id}/company", finance_token, {"companyName": "Apex Corp", "registrationNumber": "CO-123456"})
        log_result("5. Finance Onboarding", "PATCH", f"/finance/onboarding/{ob_id}/company", "FINANCE", st, body)
        
        st, body = make_request("PATCH", f"/finance/onboarding/{ob_id}/bank-account", finance_token, {"bankName": "First Reserve Bank", "accountNumber": "9876543210", "routingNumber": "123456789"})
        log_result("5. Finance Onboarding", "PATCH", f"/finance/onboarding/{ob_id}/bank-account", "FINANCE", st, body)
        
        st, body = make_request("PATCH", f"/finance/onboarding/{ob_id}/tax", finance_token, {"taxId": "TAX-998877", "taxRate": 12.5})
        log_result("5. Finance Onboarding", "PATCH", f"/finance/onboarding/{ob_id}/tax", "FINANCE", st, body)
        
        st, body = make_request("PATCH", f"/finance/onboarding/{ob_id}/payment-method", finance_token, {"paymentMethod": "DIRECT_DEPOSIT"})
        log_result("5. Finance Onboarding", "PATCH", f"/finance/onboarding/{ob_id}/payment-method", "FINANCE", st, body)
        
        st, body = make_request("PATCH", f"/finance/onboarding/{ob_id}/payroll", finance_token, {"payCycleDays": 30})
        log_result("5. Finance Onboarding", "PATCH", f"/finance/onboarding/{ob_id}/payroll", "FINANCE", st, body)
        
        st, body = make_request("PATCH", f"/finance/onboarding/{ob_id}/budget", finance_token, {"monthlyBudget": 150000.0})
        log_result("5. Finance Onboarding", "PATCH", f"/finance/onboarding/{ob_id}/budget", "FINANCE", st, body)
        
        # Validate
        st, body = make_request("POST", f"/finance/onboarding/{ob_id}/validate", finance_token)
        log_result("5. Finance Onboarding", "POST", f"/finance/onboarding/{ob_id}/validate", "FINANCE", st, body)
        
        # Complete
        st, body = make_request("POST", f"/finance/onboarding/{ob_id}/complete", finance_token)
        log_result("5. Finance Onboarding", "POST", f"/finance/onboarding/{ob_id}/complete", "FINANCE", st, body)
    else:
        for suffix in ["{id}", "{id}/progress", "{id}/company", "{id}/bank-account", "{id}/tax", "{id}/payment-method", "{id}/payroll", "{id}/budget", "{id}/validate", "{id}/complete"]:
            log_result("5. Finance Onboarding", "PATCH/POST", f"/finance/onboarding/{suffix}", "FINANCE", 404, {"error": "Skipped, no test onboarding ID"})

    # =========================================================================
    # 💸 Category 6: Payments & Financial Flow
    # =========================================================================
    st, body = make_request("GET", "/finance/payments/pending", finance_token)
    log_result("6. Payments & Flow", "GET", "/finance/payments/pending", "FINANCE", st, body)
    
    st, body = make_request("GET", "/payroll-runs", finance_token)
    log_result("6. Payments & Flow", "GET", "/payroll-runs", "FINANCE", st, body)
    
    st, body = make_request("POST", "/payroll-runs/process", finance_token, data=[])
    log_result("6. Payments & Flow", "POST", "/payroll-runs/process", "FINANCE", st, body)

    # =========================================================================
    # 💸 Category 7: Financial Analytics & Reporting
    # =========================================================================
    st, body = make_request("GET", "/reports/dashboard/admin", admin_token)
    log_result("7. Reporting & Analytics", "GET", "/reports/dashboard/admin", "SUPER_ADMIN", st, body)
    
    for suffix in ["payroll", "employees", "attendance", "leaves"]:
        st, body = make_request("GET", f"/reports/reports/{suffix}", admin_token)
        # These export endpoints return raw CSV data. We treat any response body as success if status is 200.
        log_result("7. Reporting & Analytics", "GET", f"/reports/reports/{suffix}", "SUPER_ADMIN", st, 
                   f"CSV data ({len(body)} bytes)" if st == 200 else body)

    # =========================================================================
    # 💸 Category 8: Salary Revisions
    # =========================================================================
    # Create a salary revision request first (uses Super Admin to be safe, which has all permissions)
    rev_payload = {
        "employeeId": "EMP003",
        "incrementPercentage": 12.5,
        "effectiveDate": "2026-07-01",
        "reason": "Annual performance-based raise"
    }
    st, body = make_request("POST", "/salary-revisions", admin_token, data=rev_payload)
    log_result("8. Salary Revisions", "POST", "/salary-revisions", "SUPER_ADMIN", st, body)
    
    rev_id = None
    if st == 201 and body.get("success") and body.get("data"):
        rev_id = body["data"].get("revisionId") # Returns REV001 format
        
    st, body = make_request("GET", "/salary-revisions", admin_token)
    log_result("8. Salary Revisions", "GET", "/salary-revisions", "SUPER_ADMIN", st, body)
    
    if rev_id:
        st, body = make_request("GET", f"/salary-revisions/{rev_id}", admin_token)
        log_result("8. Salary Revisions", "GET", f"/salary-revisions/{rev_id}", "SUPER_ADMIN", st, body)
        
        # Reject
        st, body = make_request("PATCH", f"/salary-revisions/{rev_id}/reject", admin_token, data={"reason": "Incorrect increment details"})
        log_result("8. Salary Revisions", "PATCH", f"/salary-revisions/{rev_id}/reject", "SUPER_ADMIN", st, body)
        
        # Re-create and Approve
        st, body = make_request("POST", "/salary-revisions", admin_token, data=rev_payload)
        rev_id_2 = body["data"].get("id") if (st == 201 and body.get("data")) else None
        
        if rev_id_2:
            st, body = make_request("PATCH", f"/salary-revisions/{rev_id_2}/approve", admin_token)
            log_result("8. Salary Revisions", "PATCH", f"/salary-revisions/{rev_id_2}/approve", "SUPER_ADMIN", st, body)
            
            # Apply
            st, body = make_request("POST", f"/salary-revisions/{rev_id_2}/apply", admin_token)
            log_result("8. Salary Revisions", "POST", f"/salary-revisions/{rev_id_2}/apply", "SUPER_ADMIN", st, body)
            
            # Letter
            st, body = make_request("GET", f"/salary-revisions/{rev_id_2}/letter", admin_token)
            log_result("8. Salary Revisions", "GET", f"/salary-revisions/{rev_id_2}/letter", "SUPER_ADMIN", st, body)
    else:
        for suffix in ["{id}", "{id}/approve", "{id}/reject", "{id}/apply", "{id}/letter"]:
            log_result("8. Salary Revisions", "GET/PATCH/POST", f"/salary-revisions/{suffix}", "SUPER_ADMIN", 404, {"error": "Skipped, no test revision ID"})

    # Write Markdown Report
    print(f"Writing report to {REPORT_PATH}...")
    with open(REPORT_PATH, "w") as f:
        f.write("# Finance Endpoints API Verification Report\n")
        f.write(f"Generated at: {time.strftime('%Y-%m-%d %H:%M:%S')} (UTC)\n\n")
        
        passed = sum(1 for r in results if r["success"])
        total = len(results)
        f.write(f"### Status Summary: **{passed} / {total} Passed**\n\n")
        
        f.write("| Category | Method | Endpoint | Authorized Role | Status | Result |\n")
        f.write("| --- | --- | --- | --- | --- | --- |\n")
        for r in results:
            status_txt = "✅ PASSED" if r["success"] else "❌ FAILED"
            f.write(f"| {r['category']} | `{r['method']}` | `{r['endpoint']}` | `{r['role']}` | `{r['status_code']}` | {status_txt} |\n")
            
        f.write("\n---\n\n## Detailed Response Logs\n\n")
        for r in results:
            f.write(f"### {r['method']} {r['endpoint']} ({r['role']})\n")
            f.write(f"**Status**: {r['status_code']} | **Success**: {r['success']}\n\n")
            f.write("```json\n")
            f.write(json.dumps(r["response"], indent=2) if isinstance(r["response"], (dict, list)) else str(r["response"]))
            f.write("\n```\n\n---\n\n")
            
    print("Done!")

if __name__ == "__main__":
    run_tests()
