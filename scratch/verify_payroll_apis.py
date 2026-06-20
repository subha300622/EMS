import urllib.request
import urllib.parse
import json
import time

BASE_URL = "http://localhost:8080/api/v1"
TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiU1VQRVJfQURNSU4iLCJzZXNzaW9uSWQiOiI3YzNlNzBlYy04MmFmLTQ2NmMtODQzOS1mMzFiN2YwZDZiMzEiLCJ1c2VySWQiOiJFTVAwMDEiLCJzdWIiOiJzdXBlcl9hZG1pbkBjb21wYW55LmNvbSIsImlhdCI6MTc4MTg2ODUxNiwiZXhwIjoxNzgxOTU0OTE2fQ.MMydvUAZb8fkG6sUHYznFxjInABHlRXKhDigm55LvM87c78PPyIbvxSM5HCHNS28RHzzE9HjCkIfPrHPMZOUtQ"

headers = {
    "Authorization": f"Bearer {TOKEN}",
    "Content-Type": "application/json"
}

results = []

def run_test(method, path, body=None, custom_headers=None):
    url = f"{BASE_URL}{path}"
    req_body_str = ""
    data = None
    if body is not None:
        req_body_str = json.dumps(body, indent=2)
        data = json.dumps(body).encode('utf-8')
        
    current_headers = headers.copy()
    if custom_headers:
        current_headers.update(custom_headers)
        
    req = urllib.request.Request(url, data=data, headers=current_headers, method=method)
    
    print(f"Testing {method} {path}...")
    start_time = time.time()
    try:
        with urllib.request.urlopen(req) as response:
            status_code = response.getcode()
            content_type = response.info().get_content_type()
            if "application/pdf" in content_type:
                resp_body_str = f"[PDF Byte Stream, Length: {len(response.read())}]"
            else:
                resp_body_str = response.read().decode('utf-8')
                try:
                    resp_json = json.loads(resp_body_str)
                    resp_body_str = json.dumps(resp_json, indent=2)
                except Exception:
                    pass
    except urllib.error.HTTPError as e:
        status_code = e.code
        resp_body_str = e.read().decode('utf-8')
        try:
            resp_json = json.loads(resp_body_str)
            resp_body_str = json.dumps(resp_json, indent=2)
        except Exception:
            pass
    except Exception as e:
        status_code = 500
        resp_body_str = str(e)
        
    duration = (time.time() - start_time) * 1000
    
    results.append({
        "method": method,
        "path": path,
        "request_body": req_body_str,
        "status_code": status_code,
        "response_body": resp_body_str,
        "duration_ms": duration
    })
    time.sleep(0.1)

# 1. Get initial dashboard
run_test("GET", "/payroll/dashboard")

# 2. Save Salary Structure
run_test("POST", "/payroll/salary-structures", {
    "employeeId": 1,
    "basicSalary": 60000.0,
    "hra": 25000.0,
    "allowances": 15000.0
})

# 3. Attendance integration
run_test("GET", "/attendance/payroll-summary?employeeId=1&month=2026-06")

# 4. Leave integration
run_test("GET", "/leaves/payroll-impact?employeeId=1&month=2026-06")

# 5. Calculation preview
run_test("GET", "/payroll/1/calculation")

# 6. Process Payroll
run_test("POST", "/payroll/process", {
    "month": "2026-06",
    "departmentId": 1
})

# 7. Approve Payroll run (using generated run id from history or default 1)
run_test("POST", "/payroll/1/approve")

# 8. Payslip JSON
run_test("GET", "/payroll/1/payslip")

# 9. Payslip PDF simulated
run_test("GET", "/payroll/1/payslip", custom_headers={"Accept": "application/pdf"})

# 10. Disburse Payroll
run_test("POST", "/payroll/disburse", {
    "payrollRunId": 1
})

# 11. History
run_test("GET", "/payroll/history")

# 12. Employee specific history
run_test("GET", "/payroll/employees/1/history")

# 13. Tax Settings GET
run_test("GET", "/payroll/taxes")

# 14. Tax Settings PUT
run_test("PUT", "/payroll/taxes", {
    "pfRate": 12.5,
    "esiRate": 0.8
})

# 15. Analytics cost-trend
run_test("GET", "/payroll/analytics/cost-trend")

# 16. Analytics department-cost
run_test("GET", "/payroll/analytics/department-cost")

# 17. Reports
run_test("GET", "/payroll/reports/monthly")
run_test("GET", "/payroll/reports/salary-register")
run_test("GET", "/payroll/reports/tax")
run_test("GET", "/payroll/reports/disbursement")

# --- Write Report in Markdown ---
report_content = """# Payroll Management API Live Verification Report

This document records the actual HTTP requests and response payloads captured from hitting the live running instance of the EMS backend on localhost:8080.

## Summary Table

| Method | Endpoint | Status Code | Duration | Result |
|--------|----------|-------------|----------|--------|
"""

for r in results:
    icon = "✅ PASSED" if r["status_code"] in [200, 201] else "❌ FAILED"
    report_content += f"| `{r['method']}` | `{r['path']}` | `{r['status_code']}` | {r['duration_ms']:.1f}ms | {icon} |\n"

report_content += "\n## Detailed Request & Response Logs\n\n"

for idx, r in enumerate(results, 1):
    report_content += f"### Test Case {idx}: {r['method']} {r['path']}\n\n"
    report_content += f"**Request URL**: `http://localhost:8080/api/v1{r['path']}`\n\n"
    if r["request_body"]:
        report_content += f"**Request Body**:\n```json\n{r['request_body']}\n```\n\n"
    else:
        report_content += "**Request Body**: (none)\n\n"
    report_content += f"**Response Status Code**: `{r['status_code']}`\n\n"
    report_content += f"**Response Body**:\n```json\n{r['response_body']}\n```\n\n"
    report_content += "---\n\n"

with open("/home/subashini/.gemini/antigravity-ide/brain/dca44ea3-46fe-49f7-b86e-a93a0853cfc9/payroll_verification_results.md", "w") as f:
    f.write(report_content)

print("Verification run completed. Results written to payroll_verification_results.md.")
