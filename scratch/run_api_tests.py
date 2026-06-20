import urllib.request
import json
import os

base_url = "http://localhost:8080/api/v1"

def login():
    url = f"{base_url}/auth/login"
    payload = {
        "email": "super_admin@company.com",
        "password": "super_admin@1"
    }
    req = urllib.request.Request(
        url,
        data=json.dumps(payload).encode('utf-8'),
        headers={'Content-Type': 'application/json'},
        method='POST'
    )
    try:
        with urllib.request.urlopen(req) as res:
            data = json.loads(res.read().decode('utf-8'))
            return data["data"]["tokens"]["accessToken"]
    except Exception as e:
        print("Login failed:", e)
        return None

def send_request(token, path, method="GET", body=None):
    url = f"{base_url}{path}"
    headers = {
        'Authorization': f'Bearer {token}',
        'Content-Type': 'application/json'
    }
    
    data_bytes = None
    if body is not None:
        data_bytes = json.dumps(body).encode('utf-8')
        
    req = urllib.request.Request(
        url,
        data=data_bytes,
        headers=headers,
        method=method
    )
    try:
        with urllib.request.urlopen(req) as res:
            status = res.status
            content_type = res.headers.get('Content-Type', '')
            if 'octet-stream' in content_type:
                resp_body = f"[Binary download: {res.headers.get('Content-Disposition', 'filename')}]"
            else:
                resp_body = json.loads(res.read().decode('utf-8'))
            return status, resp_body
    except urllib.error.HTTPError as e:
        try:
            err_body = json.loads(e.read().decode('utf-8'))
        except Exception:
            err_body = e.reason
        return e.code, err_body
    except Exception as e:
        return 500, str(e)

def run_tests():
    token = login()
    if not token:
        return
    
    results = []
    
    # Define test endpoints
    tests = [
        # Dropdowns
        ("Departments Dropdown", "/departments/dropdown", "GET", None),
        ("Branches Dropdown", "/branches/dropdown", "GET", None),
        ("Report Categories", "/reports/categories", "GET", None),
        ("Period Options", "/reports/periods", "GET", None),
        
        # Dashboards
        ("Dashboard Summary", "/reports/dashboard-summary?period=MONTH&departmentId=1&branchId=1&category=ALL", "GET", None),
        ("Payroll Cost Trend Chart", "/reports/payroll-cost-trend", "GET", None),
        ("Department Cost Distribution", "/reports/department-cost-distribution", "GET", None),
        
        # Tab Reports
        ("Payroll Reports Tab", "/reports/payroll?fromDate=2026-01-01&toDate=2026-01-31", "GET", None),
        ("Expense Reports Tab", "/reports/expenses", "GET", None),
        ("Tax Reports Tab", "/reports/tax", "GET", None),
        ("Asset Reports Tab", "/reports/assets", "GET", None),
        
        # Custom Report Builder
        ("Custom Report Builder", "/reports/custom", "POST", {
            "name": "Department Payroll Report",
            "module": "PAYROLL",
            "columns": ["employeeName", "department", "grossSalary", "netSalary"],
            "filters": {"departmentId": 1}
        }),
        ("Report History", "/reports/history", "GET", None),
        
        # Export Report
        ("Export Report", "/reports/export", "POST", {
            "reportType": "PAYROLL",
            "format": "EXCEL",
            "period": "MONTH"
        }),
    ]
    
    md = ["# Reports & Analytics APIs - Functional Verification Report\n"]
    
    for title, path, method, body in tests:
        status, response = send_request(token, path, method, body)
        md.append(f"## {title}")
        md.append(f"**Endpoint**: `{method} {path}`")
        if body:
            md.append("**Request Body**:")
            md.append("```json")
            md.append(json.dumps(body, indent=2))
            md.append("```")
        md.append(f"**HTTP Response Status**: `{status}`")
        md.append("**Response Body**:")
        md.append("```json")
        md.append(json.dumps(response, indent=2))
        md.append("```")
        md.append("\n---\n")
        
        # Capture export ID for download test
        if title == "Export Report" and isinstance(response, dict) and "data" in response:
            export_id = response["data"].get("exportId")
            if export_id:
                dl_status, dl_resp = send_request(token, f"/reports/export/{export_id}", "GET")
                md.append("## Download Export")
                md.append(f"**Endpoint**: `GET /reports/export/{export_id}`")
                md.append(f"**HTTP Response Status**: `{dl_status}`")
                md.append("**Response Body**:")
                md.append(f"`{dl_resp}`")
                md.append("\n---\n")

    # Schedule CRUD Test
    md.append("## Scheduled Reports CRUD Flow")
    
    # 1. Create Schedule
    s_body = {
        "reportType": "PAYROLL",
        "frequency": "MONTHLY",
        "emailRecipients": ["finance@company.com"]
    }
    status, response = send_request(token, "/reports/schedules", "POST", s_body)
    md.append("### 1. Create Schedule")
    md.append("**Endpoint**: `POST /reports/schedules`")
    md.append("**Request Body**:")
    md.append("```json")
    md.append(json.dumps(s_body, indent=2))
    md.append("```")
    md.append(f"**HTTP Response Status**: `{status}`")
    md.append("**Response Body**:")
    md.append("```json")
    md.append(json.dumps(response, indent=2))
    md.append("```")
    
    if isinstance(response, dict) and "data" in response:
        schedule_id = response["data"].get("id")
        
        # 2. Get Schedules
        status, g_response = send_request(token, "/reports/schedules", "GET")
        md.append("### 2. Get Schedules")
        md.append("**Endpoint**: `GET /reports/schedules`")
        md.append(f"**HTTP Response Status**: `{status}`")
        md.append("**Response Body**:")
        md.append("```json")
        md.append(json.dumps(g_response, indent=2))
        md.append("```")
        
        # 3. Update Schedule
        u_body = {
            "frequency": "WEEKLY"
        }
        status, u_response = send_request(token, f"/reports/schedules/{schedule_id}", "PUT", u_body)
        md.append("### 3. Update Schedule")
        md.append(f"**Endpoint**: `PUT /reports/schedules/{schedule_id}`")
        md.append("**Request Body**:")
        md.append("```json")
        md.append(json.dumps(u_body, indent=2))
        md.append("```")
        md.append(f"**HTTP Response Status**: `{status}`")
        md.append("**Response Body**:")
        md.append("```json")
        md.append(json.dumps(u_response, indent=2))
        md.append("```")
        
        # 4. Delete Schedule
        status, d_response = send_request(token, f"/reports/schedules/{schedule_id}", "DELETE")
        md.append("### 4. Delete Schedule")
        md.append(f"**Endpoint**: `DELETE /reports/schedules/{schedule_id}`")
        md.append(f"**HTTP Response Status**: `{status}`")
        md.append("**Response Body**:")
        md.append("```json")
        md.append(json.dumps(d_response, indent=2))
        md.append("```")
    
    # Write report
    art_path = "/home/subashini/.gemini/antigravity-ide/brain/dca44ea3-46fe-49f7-b86e-a93a0853cfc9/api_test_results.md"
    with open(art_path, 'w', encoding='utf-8') as f:
        f.write("\n".join(md))
        
    print(f"Report written successfully to: {art_path}")

if __name__ == "__main__":
    run_tests()
