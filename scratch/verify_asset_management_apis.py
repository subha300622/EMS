#!/usr/bin/env python3
import json
import urllib.request
import urllib.error
import sys

BASE_URL = "http://localhost:8080/api/v1"
REPORT_PATH = "/home/subashini/Documents/ems-backend/asset_verification_results.md"

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

def make_multipart_request(endpoint, file_name, file_content, document_type, token):
    boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW"
    url = f"{BASE_URL}{endpoint}"
    
    body = []
    # File part
    body.append(f"--{boundary}".encode("utf-8"))
    body.append(f'Content-Disposition: form-data; name="file"; filename="{file_name}"'.encode("utf-8"))
    body.append(b'Content-Type: application/pdf')
    body.append(b'')
    body.append(file_content)
    
    # documentType part
    body.append(f"--{boundary}".encode("utf-8"))
    body.append(b'Content-Disposition: form-data; name="documentType"')
    body.append(b'')
    body.append(document_type.encode("utf-8"))
    
    body.append(f"--{boundary}--".encode("utf-8"))
    body.append(b'')
    
    req_data = b'\r\n'.join(body)
    
    req = urllib.request.Request(
        url,
        data=req_data,
        headers={
            "Content-Type": f"multipart/form-data; boundary={boundary}",
            "Authorization": f"Bearer {token}"
        },
        method="POST"
    )
    
    try:
        with urllib.request.urlopen(req) as response:
            return response.status, json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        resp_body = e.read().decode("utf-8")
        try:
            return e.code, json.loads(resp_body)
        except json.JSONDecodeError:
            return e.code, resp_body

def run_tests():
    print("Authenticating Admin User...")
    
    # Try logging in as admin
    admin_token = None
    passwords_to_try = ["admin@2", "admin@1", "super_admin@1", "super_admin@2"]
    emails_to_try = ["admin@company.com", "super_admin@company.com"]
    
    for email in emails_to_try:
        for pwd in passwords_to_try:
            status, res = make_request("POST", "/auth/login", data={"email": email, "password": pwd})
            if status == 200 and res.get("success"):
                admin_token = res["data"]["tokens"]["accessToken"]
                print(f"✅ Authenticated successfully as: {email}")
                break
        if admin_token:
            break
            
    if not admin_token:
        print("❌ Admin authentication failed. Make sure dev server is running on port 8080.")
        sys.exit(1)

    results = []

    def log_result(method, endpoint, test_name, status_code, response, expected):
        success = (status_code == expected)
        results.append({
            "method": method,
            "endpoint": endpoint,
            "test_name": test_name,
            "status_code": status_code,
            "expected": expected,
            "success": success,
            "response": response
        })
        status_symbol = "✅ PASSED" if success else "❌ FAILED"
        print(f"{method} {endpoint} ({test_name}) -> {status_code} (Expected {expected}) {status_symbol}")

    # Test 1: Get Dashboard Metrics
    status, res = make_request("GET", "/assets/dashboard", admin_token)
    log_result("GET", "/assets/dashboard", "Dashboard Metrics", status, res, 200)

    # Test 2: Create a new Asset
    asset_payload = {
        "assetCode": "AST-TEST-999",
        "assetName": "MacBook Pro M3 Max",
        "category": "LAPTOP",
        "brand": "Apple",
        "model": "M3 Max 16inch",
        "serialNumber": "SN-MACPRO-999",
        "purchaseDate": "2026-06-20",
        "purchasePrice": 250000.00,
        "currentValue": 240000.00,
        "location": "Mumbai HQ",
        "condition": "EXCELLENT",
        "warrantyStatus": "ACTIVE",
        "warrantyExpiryDate": "2029-06-20",
        "vendor": "Apple India Pvt Ltd",
        "depreciationPercentage": 10.00
    }
    status, res = make_request("POST", "/assets", admin_token, asset_payload)
    log_result("POST", "/assets", "Create Asset", status, res, 201)
    
    asset_id = None
    if status == 201 and res.get("success"):
        asset_id = res["data"]["id"]
        print(f"Created Asset ID: {asset_id}")

    if not asset_id:
        print("❌ Cannot proceed with asset sub-routes test without created asset.")
        sys.exit(1)

    # Test 3: Get Asset List with filters
    status, res = make_request("GET", f"/assets?status=AVAILABLE&category=LAPTOP&search=MacBook", admin_token)
    log_result("GET", "/assets", "Asset Listing with filters", status, res, 200)

    # Test 4: Get Asset Details
    status, res = make_request("GET", f"/assets/{asset_id}", admin_token)
    log_result("GET", f"/assets/{{id}}", "Asset Details Drawer", status, res, 200)

    # Test 5: Assign Asset
    assign_payload = {
        "employeeId": 1,
        "expectedReturnDate": "2027-06-20"
    }
    status, res = make_request("POST", f"/assets/{asset_id}/assign", admin_token, assign_payload)
    log_result("POST", f"/assets/{{id}}/assign", "Assign Asset", status, res, 200)

    # Test 6: Assignment History
    status, res = make_request("GET", f"/assets/{asset_id}/assignments", admin_token)
    log_result("GET", f"/assets/{{id}}/assignments", "Assignment History list", status, res, 200)

    # Test 7: Transfer Asset
    transfer_payload = {
        "fromEmployeeId": 1,
        "toEmployeeId": 2,
        "remarks": "Project change transfer"
    }
    status, res = make_request("POST", f"/assets/{asset_id}/transfer", admin_token, transfer_payload)
    log_result("POST", f"/assets/{{id}}/transfer", "Transfer Asset", status, res, 200)

    # Test 8: Recover Asset
    recover_payload = {
        "remarks": "Regular recovery"
    }
    status, res = make_request("POST", f"/assets/{asset_id}/recover", admin_token, recover_payload)
    log_result("POST", f"/assets/{{id}}/recover", "Recover Asset", status, res, 200)

    # Test 9: Create Maintenance Request
    maint_payload = {
        "issue": "Keyboard keys stuck",
        "vendor": "Apple Store Mumbai",
        "estimatedCost": 12000.00
    }
    status, res = make_request("POST", f"/assets/{asset_id}/maintenance", admin_token, maint_payload)
    log_result("POST", f"/assets/{{id}}/maintenance", "Create Maintenance Request", status, res, 200)
    
    maint_id = None
    if status == 200 and res.get("success"):
        maint_id = res["data"]["id"]

    # Test 10: Get Maintenance Records
    status, res = make_request("GET", f"/assets/{asset_id}/maintenance", admin_token)
    log_result("GET", f"/assets/{{id}}/maintenance", "Get Maintenance Records", status, res, 200)

    # Test 11: Complete Maintenance
    if maint_id:
        status, res = make_request("PATCH", f"/assets/maintenance/{maint_id}/complete", admin_token, {"actualCost": 11500.00})
        log_result("PATCH", "/assets/maintenance/{maintId}/complete", "Complete Maintenance", status, res, 200)

    # Test 12: Upload Asset Document
    status, res = make_multipart_request(f"/assets/{asset_id}/documents", "invoice_rec.pdf", b"dummy pdf invoice bytes", "Invoice", admin_token)
    log_result("POST", f"/assets/{{id}}/documents", "Upload Document", status, res, 200)

    # Test 13: Get Asset Documents
    status, res = make_request("GET", f"/assets/{asset_id}/documents", admin_token)
    log_result("GET", f"/assets/{{id}}/documents", "Get Documents", status, res, 200)

    # Test 14: Retire Asset
    status, res = make_request("PATCH", f"/assets/{asset_id}/retire", admin_token)
    log_result("PATCH", f"/assets/{{id}}/retire", "Retire Asset", status, res, 200)

    # Test 15: Dispose Asset
    status, res = make_request("PATCH", f"/assets/{asset_id}/dispose", admin_token)
    log_result("PATCH", f"/assets/{{id}}/dispose", "Dispose Asset", status, res, 200)

    # Test 16: Reports - Utilization
    status, res = make_request("GET", "/assets/reports/utilization", admin_token)
    log_result("GET", "/assets/reports/utilization", "Utilization Report", status, res, 200)

    # Test 17: Reports - Depreciation
    status, res = make_request("GET", "/assets/reports/depreciation", admin_token)
    log_result("GET", "/assets/reports/depreciation", "Depreciation Report", status, res, 200)

    # Test 18: Reports - Maintenance
    status, res = make_request("GET", "/assets/reports/maintenance", admin_token)
    log_result("GET", "/assets/reports/maintenance", "Maintenance Report", status, res, 200)

    # Test 19: Reports - Inventory
    status, res = make_request("GET", "/assets/reports/inventory", admin_token)
    log_result("GET", "/assets/reports/inventory", "Inventory Report", status, res, 200)

    # Generate Markdown Report
    with open(REPORT_PATH, "w") as f:
        f.write("# Asset Management APIs Verification Results\n\n")
        f.write("| Test Name | Method | Endpoint | Status Code | Expected | Result |\n")
        f.write("|---|---|---|---|---|---|\n")
        for r in results:
            res_symbol = "✅ Passed" if r["success"] else "❌ Failed"
            f.write(f"| {r['test_name']} | {r['method']} | {r['endpoint']} | {r['status_code']} | {r['expected']} | {res_symbol} |\n")
        
        f.write("\n\n## Sample Response (Dashboard Metrics)\n")
        f.write("```json\n")
        # Extract dashboard response
        db_res = next((x["response"] for x in results if x["test_name"] == "Dashboard Metrics"), {})
        f.write(json.dumps(db_res, indent=2))
        f.write("\n```\n")

    print(f"\nVerification complete. Results written to: {REPORT_PATH}")

if __name__ == "__main__":
    run_tests()
