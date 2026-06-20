#!/usr/bin/env python3
import json
import urllib.request
import urllib.error
import time
import os

BASE_URL = "http://localhost:8080/api/v1"
REPORT_PATH = "/home/subashini/Documents/ems-backend/finance_onboarding_verification_results.md"

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
    print("Logging in users...")
    
    # 1. Finance User
    status, res = make_request("POST", "/auth/login", data={"email": "finance@company.com", "password": "finance@2"})
    if status == 200 and res.get("success"):
        finance_token = res["data"]["tokens"]["accessToken"]
        print("✅ Logged in as Finance User")
    else:
        print("❌ Finance login failed", res)
        return
        
    # 2. Employee User
    status, res = make_request("POST", "/auth/login", data={"email": "employee@company.com", "password": "employee@3"})
    if status == 200 and res.get("success"):
        employee_token = res["data"]["tokens"]["accessToken"]
        print("✅ Logged in as Employee User")
    else:
        print("❌ Employee login failed", res)
        return

    results = []

    def log_result(method, endpoint, role, status_code, response, expected):
        success = (status_code == expected)
        results.append({
            "method": method,
            "endpoint": endpoint,
            "role": role,
            "status_code": status_code,
            "expected": expected,
            "success": success,
            "response": response
        })
        status_symbol = "✅ PASSED" if success else "❌ FAILED"
        print(f"{method} {endpoint} ({role}) -> {status_code} (Expected {expected}) {status_symbol}")

    # =========================================================================
    # 1. SETUP / SEED ONBOARDING RECORD FOR EMPLOYEE ID 3
    # =========================================================================
    emp_id = 3
    # Delete existing onboarding for clean state
    # First get details to obtain onboarding ID
    st, body = make_request("GET", "/finance/onboarding", finance_token)
    if st == 200 and body.get("data"):
        for ob in body["data"]:
            if ob.get("employee") and ob["employee"].get("id") == emp_id:
                make_request("DELETE", f"/finance/onboarding/{ob['id']}", finance_token)

    payload = {
        "employeeId": emp_id,
        "bankName": "ICICI Bank",
        "bankAccountNumber": "1122334455",
        "bankIfsc": "ICIC0000123",
        "panNumber": "ABCDE5678G",
        "uanNumber": "100900800700"
    }
    
    st, body = make_request("POST", "/finance/onboarding", finance_token, data=payload)
    log_result("POST", "/finance/onboarding", "FINANCE", st, body, 201)
    
    onb_id = None
    if st == 201 and body.get("success") and body.get("data"):
        onb_id = body["data"].get("id")
        
    if not onb_id:
        st, body = make_request("GET", "/finance/onboarding", finance_token)
        if st == 200 and body.get("data"):
            for ob in body["data"]:
                if ob.get("employee") and ob["employee"].get("id") == emp_id:
                    onb_id = ob["id"]

    if not onb_id:
        print("❌ Failed to set up / find test onboarding session.")
        return

    # =========================================================================
    # 2. GENERAL ENDPOINTS (Reviews, Dashboard, Reject)
    # =========================================================================
    # GET dashboard counts
    st, body = make_request("GET", "/finance/onboarding/dashboard", finance_token)
    log_result("GET", "/finance/onboarding/dashboard", "FINANCE", st, body, 200)

    # GET pending reviews
    st, body = make_request("GET", "/finance/onboarding/pending-reviews", finance_token)
    log_result("GET", "/finance/onboarding/pending-reviews", "FINANCE", st, body, 200)

    # PATCH reject onboarding verification
    st, body = make_request("PATCH", f"/finance/onboarding/{onb_id}/reject", finance_token, data={"notes": "Rejecting for details test"})
    log_result("PATCH", f"/finance/onboarding/{onb_id}/reject", "FINANCE", st, body, 200)

    # PATCH verify batch financial details
    st, body = make_request("PATCH", f"/finance/onboarding/{onb_id}/verify", finance_token, data={
        "bankVerified": True,
        "panVerified": True,
        "uanVerified": True,
        "remarks": "Batch verification success"
    })
    log_result("PATCH", f"/finance/onboarding/{onb_id}/verify", "FINANCE", st, body, 200)

    # =========================================================================
    # 3. BANKING ENDPOINTS
    # =========================================================================
    # GET bank details
    st, body = make_request("GET", f"/finance/onboarding/{emp_id}/bank-details", finance_token)
    log_result("GET", f"/finance/onboarding/{emp_id}/bank-details", "FINANCE", st, body, 200)

    # PUT bank details
    st, body = make_request("PUT", f"/finance/onboarding/{emp_id}/bank-details", finance_token, data={
        "bankName": "HDFC Bank",
        "bankAccountNumber": "9999999999",
        "bankIfsc": "HDFC0009999"
    })
    log_result("PUT", f"/finance/onboarding/{emp_id}/bank-details", "FINANCE", st, body, 200)

    # PATCH verify bank details
    st, body = make_request("PATCH", f"/finance/onboarding/{emp_id}/bank-details/verify", finance_token, data={
        "remarks": "Bank account successfully verified"
    })
    log_result("PATCH", f"/finance/onboarding/{emp_id}/bank-details/verify", "FINANCE", st, body, 200)

    # =========================================================================
    # 4. TAX & COMPLIANCE ENDPOINTS
    # =========================================================================
    # GET tax details
    st, body = make_request("GET", f"/finance/onboarding/{emp_id}/tax-details", finance_token)
    log_result("GET", f"/finance/onboarding/{emp_id}/tax-details", "FINANCE", st, body, 200)

    # PUT tax details
    st, body = make_request("PUT", f"/finance/onboarding/{emp_id}/tax-details", finance_token, data={
        "panNumber": "XYZZY1234A"
    })
    log_result("PUT", f"/finance/onboarding/{emp_id}/tax-details", "FINANCE", st, body, 200)

    # GET statutory details
    st, body = make_request("GET", f"/finance/onboarding/{emp_id}/statutory-details", finance_token)
    log_result("GET", f"/finance/onboarding/{emp_id}/statutory-details", "FINANCE", st, body, 200)

    # PUT statutory details
    st, body = make_request("PUT", f"/finance/onboarding/{emp_id}/statutory-details", finance_token, data={
        "uanNumber": "999988887777"
    })
    log_result("PUT", f"/finance/onboarding/{emp_id}/statutory-details", "FINANCE", st, body, 200)

    # Re-verify PAN & UAN for full approval state
    st, body = make_request("PATCH", f"/finance/onboarding/{onb_id}/verify", finance_token, data={
        "bankVerified": True,
        "panVerified": True,
        "uanVerified": True,
        "remarks": "Re-verified after update"
    })

    # =========================================================================
    # 5. SALARY SETUP ENDPOINTS
    # =========================================================================
    # POST assign salary structure
    st, body = make_request("POST", f"/finance/onboarding/{emp_id}/salary-structure", finance_token, data={
        "basicSalary": 45000,
        "hra": 18000,
        "allowances": 12000
    })
    log_result("POST", f"/finance/onboarding/{emp_id}/salary-structure", "FINANCE", st, body, 200)

    # GET salary structure
    st, body = make_request("GET", f"/finance/onboarding/{emp_id}/salary-structure", finance_token)
    log_result("GET", f"/finance/onboarding/{emp_id}/salary-structure", "FINANCE", st, body, 200)

    # POST calculate CTC breakup
    st, body = make_request("POST", "/finance/onboarding/calculate-ctc", finance_token, data={"monthlyCtc": 75000})
    log_result("POST", "/finance/onboarding/calculate-ctc", "FINANCE", st, body, 200)

    # GET salary preview
    st, body = make_request("GET", f"/finance/onboarding/{emp_id}/salary-preview", finance_token)
    log_result("GET", f"/finance/onboarding/{emp_id}/salary-preview", "FINANCE", st, body, 200)

    # =========================================================================
    # 6. PAYROLL ACTIVATION ENDPOINTS
    # =========================================================================
    # POST activate payroll
    st, body = make_request("POST", f"/finance/onboarding/{emp_id}/activate-payroll", finance_token)
    log_result("POST", f"/finance/onboarding/{emp_id}/activate-payroll", "FINANCE", st, body, 200)

    # GET payroll status
    st, body = make_request("GET", f"/finance/onboarding/{emp_id}/payroll-status", finance_token)
    log_result("GET", f"/finance/onboarding/{emp_id}/payroll-status", "FINANCE", st, body, 200)

    # =========================================================================
    # 7. AUDITING & REPORTING ENDPOINTS
    # =========================================================================
    # GET summary activity logs
    st, body = make_request("GET", f"/finance/onboarding/{onb_id}/approval-history", finance_token)
    log_result("GET", f"/finance/onboarding/{onb_id}/approval-history", "FINANCE", st, body, 200)

    # GET dashboard recent activities
    st, body = make_request("GET", "/finance/onboarding/dashboard/recent-activities", finance_token)
    log_result("GET", "/finance/onboarding/dashboard/recent-activities", "FINANCE", st, body, 200)

    # GET reports
    st, body = make_request("GET", "/finance/onboarding/reports", finance_token)
    log_result("GET", "/finance/onboarding/reports", "FINANCE", st, body, 200)

    # GET export reports
    st, body = make_request("GET", "/finance/onboarding/reports/export", finance_token)
    log_result("GET", "/finance/onboarding/reports/export", "FINANCE", st, f"CSV raw report ({len(body)} bytes)" if st == 200 else body, 200)

    # =========================================================================
    # 8. SECURITY & ROLE VALIDATION
    # =========================================================================
    # GET list (Employee) -> Should be 403 Forbidden
    st, body = make_request("GET", "/finance/onboarding", employee_token)
    log_result("GET", "/finance/onboarding", "EMPLOYEE", st, body, 403)
    
    # GET dashboard (Employee) -> Should be 403 Forbidden
    st, body = make_request("GET", "/finance/onboarding/dashboard", employee_token)
    log_result("GET", "/finance/onboarding/dashboard", "EMPLOYEE", st, body, 403)

    # Write Markdown Report
    print(f"Writing report to {REPORT_PATH}...")
    with open(REPORT_PATH, "w") as f:
        f.write("# Finance Onboarding Endpoints Verification Report\n")
        f.write(f"Generated at: {time.strftime('%Y-%m-%d %H:%M:%S')} (UTC)\n\n")
        
        passed = sum(1 for r in results if r["success"])
        total = len(results)
        f.write(f"### Status Summary: **{passed} / {total} Tests Passed**\n\n")
        
        f.write("| Method | Endpoint | Authorized Role | Expected Status | Actual Status | Result |\n")
        f.write("| --- | --- | --- | --- | --- | --- |\n")
        for r in results:
            status_txt = "✅ PASSED" if r["success"] else "❌ FAILED"
            f.write(f"| `{r['method']}` | `{r['endpoint']}` | `{r['role']}` | `{r['expected']}` | `{r['status_code']}` | {status_txt} |\n")
            
        f.write("\n---\n\n## Detailed Response Logs\n\n")
        for r in results:
            f.write(f"### {r['method']} {r['endpoint']} ({r['role']})\n")
            f.write(f"**Expected Status**: {r['expected']} | **Actual Status**: {r['status_code']} | **Success**: {r['success']}\n\n")
            f.write("```json\n")
            f.write(json.dumps(r["response"], indent=2) if isinstance(r["response"], (dict, list)) else str(r["response"]))
            f.write("\n```\n\n---\n\n")
            
    print("Done!")

if __name__ == "__main__":
    run_tests()
