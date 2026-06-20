import urllib.request
import urllib.parse
import json
import time

BASE_URL = "http://localhost:8080/api/v1"
TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiU1VQRVJfQURNSU4iLCJzZXNzaW9uSWQiOiIzZTVhMmMxMi03YzQ2LTQ3NmEtYjQ3My1mMTQzM2U2MWI1ZDkiLCJ1c2VySWQiOiJFTVAwMDEiLCJzdWIiOiJzdXBlcl9hZG1pbkBjb21wYW55LmNvbSIsImlhdCI6MTc4MTg2NzA4MiwiZXhwIjoxNzgxOTUzNDgyfQ.6Zp0Qf4ykE6zA6J-XtrU-YHprHNTMwlUzN3sr-VK5R2RBsnGctV0sHzP0hhqVURK2_BgJuHSYHu9mU6Dg653rg"

headers = {
    "Authorization": f"Bearer {TOKEN}",
    "Content-Type": "application/json"
}

results = []

def run_test(method, path, body=None):
    url = f"{BASE_URL}{path}"
    req_body_str = ""
    data = None
    if body is not None:
        req_body_str = json.dumps(body, indent=2)
        data = json.dumps(body).encode('utf-8')
        
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    
    print(f"Testing {method} {path}...")
    start_time = time.time()
    try:
        with urllib.request.urlopen(req) as response:
            status_code = response.getcode()
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
    
    # Small pause to be nice to the DB/server
    time.sleep(0.1)

# 1. Get initial list
run_test("GET", "/departments")

# 2. Create Technology parent department
run_test("POST", "/departments", {
    "name": "Technology",
    "code": "TECH",
    "description": "Tech parent org",
    "budget": 50000000.0,
    "status": "ACTIVE"
})

# Let's get the list to find ID of Technology
import re
# We'll run a quick GET to find the generated IDs
run_test("GET", "/departments")
last_res = results[-1]["response_body"]
try:
    deps = json.loads(last_res)["data"]
    tech_id = next(d["id"] for d in deps if d["name"] == "Technology")
except Exception:
    tech_id = 1

# 3. Create Engineering with Technology as parent
run_test("POST", "/departments", {
    "name": "Engineering",
    "code": "ENG",
    "description": "Software Development",
    "parentDepartmentId": tech_id,
    "budget": 20000000.0,
    "status": "ACTIVE",
    "costCenter": "CC-ENG"
})

# Get list again to find Engineering ID
run_test("GET", "/departments")
try:
    deps = json.loads(results[-1]["response_body"])["data"]
    eng_id = next(d["id"] for d in deps if d["name"] == "Engineering")
except Exception:
    eng_id = 2

# 4. Get specific department
run_test("GET", f"/departments/{eng_id}")

# 5. Get Dropdown
run_test("GET", "/departments/dropdown")

# 6. Get Hierarchy tree
run_test("GET", "/departments/hierarchy")

# 7. Get Dashboard details
run_test("GET", "/departments/dashboard")

# 8. Assign manager to Engineering
run_test("PUT", f"/departments/{eng_id}/manager", {
    "managerId": 1
})

# 9. Get manager details
run_test("GET", f"/departments/{eng_id}/manager")

# 10. Get employees list for Engineering
run_test("GET", f"/departments/{eng_id}/employees?page=0&size=20&status=ACTIVE")

# 11. Log employee transfer (Arjun Mehta is employee id 2 or 3, we'll try 2. If it fails, no worries)
run_test("POST", "/departments/transfers", {
    "employeeId": 1,
    "fromDepartmentId": tech_id,
    "toDepartmentId": eng_id,
    "effectiveDate": "2026-06-19",
    "remarks": "Project Allocation"
})

# 12. Get transfer logs
run_test("GET", "/departments/transfers")

# 13. Get Analytics distribution
run_test("GET", "/departments/analytics/employee-distribution")
run_test("GET", "/departments/analytics/budget-distribution")
run_test("GET", "/departments/analytics/growth")
run_test("GET", "/departments/analytics/headcount-trend")

# 14. Get/Put Cost-center
run_test("GET", f"/departments/{eng_id}/cost-center")
run_test("PUT", f"/departments/{eng_id}/cost-center", {
    "costCenter": "CC-ENG-MAIN"
})
run_test("GET", f"/departments/{eng_id}/cost-center")

# 15. Get/Put Budget
run_test("GET", f"/departments/{eng_id}/budget")
run_test("PUT", f"/departments/{eng_id}/budget", {
    "allocated": 25000000.0,
    "utilized": 8500000.0
})
run_test("GET", f"/departments/{eng_id}/budget")

# 16. Get Reports
run_test("GET", "/departments/reports/headcount")
run_test("GET", "/departments/reports/budget-utilization")
run_test("GET", "/departments/reports/employee-allocation")
run_test("GET", "/departments/reports/performance-summary")

# --- Write Report in Markdown ---
report_content = """# Department Management API Live Verification Report

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

with open("/home/subashini/.gemini/antigravity-ide/brain/dca44ea3-46fe-49f7-b86e-a93a0853cfc9/api_test_results.md", "w") as f:
    f.write(report_content)

print("Verification run completed. Results written to api_test_results.md.")
