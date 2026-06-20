#!/usr/bin/env python3
import json
import urllib.request
import urllib.error
import time
import os

BASE_URL = "http://localhost:8080/api/v1"
REPORT_PATH = "/home/subashini/Documents/ems-backend/asset_verification_results.md"

def make_request(method, endpoint, token=None, data=None):
    if endpoint.startswith("/api/v1"):
        url = f"http://localhost:8080{endpoint}"
    else:
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
            resp_body = response.read()
            status_code = response.status
            
            # Check if it's a file download (binary or text content that might not be JSON)
            content_type = response.headers.get("Content-Type", "")
            if "application/json" not in content_type:
                return status_code, f"File Content ({len(resp_body)} bytes)"
                
            resp_text = resp_body.decode("utf-8")
            try:
                parsed_json = json.loads(resp_text)
                return status_code, parsed_json
            except json.JSONDecodeError:
                return status_code, resp_text
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
    print("Logging in as Finance User...")
    status, res = make_request("POST", "/auth/login", data={"email": "finance@company.com", "password": "finance@2"})
    if status == 200 and res.get("success"):
        finance_token = res["data"]["tokens"]["accessToken"]
        print("✅ Logged in as Finance")
    else:
        print("❌ Finance login failed", res)
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

    # 1. Dashboard
    st, body = make_request("GET", "/finance/asset-cost-report/dashboard", finance_token)
    log_result("1. Dashboard", "GET", "/finance/asset-cost-report/dashboard", "FINANCE", st, body)

    # 2. Asset Cost Breakdown
    st, body = make_request("GET", "/finance/asset-cost-report?page=0&size=10", finance_token)
    log_result("2. Asset Cost Breakdown", "GET", "/finance/asset-cost-report", "FINANCE", st, body)

    # Get first categoryId if present, else default to 1
    category_id = 1
    category_name = "Laptop"
    if st == 200 and body.get("success") and body.get("data") and body["data"].get("content"):
        first_cat = body["data"]["content"][0]
        category_id = first_cat.get("categoryId", 1)
        category_name = first_cat.get("categoryName", "Laptop")

    # 3. Category Details
    st, body = make_request("GET", f"/finance/asset-cost-report/categories/{category_id}", finance_token)
    log_result("3. Category Details", "GET", f"/finance/asset-cost-report/categories/{category_id}", "FINANCE", st, body)

    # 4. Category Assets (Popup)
    st, body = make_request("GET", f"/finance/asset-cost-report/categories/{category_id}/assets", finance_token)
    log_result("4. Category Assets", "GET", f"/finance/asset-cost-report/categories/{category_id}/assets", "FINANCE", st, body)

    # Get first assetId if present, else default to 1
    asset_id = 1
    if st == 200 and body.get("success") and body.get("data") and body["data"].get("assets"):
        first_asset = body["data"]["assets"][0]
        asset_id = first_asset.get("assetId", 1)

    # 5. Asset Financial Details
    st, body = make_request("GET", f"/finance/asset-cost-report/assets/{asset_id}", finance_token)
    log_result("5. Asset Financial Details", "GET", f"/finance/asset-cost-report/assets/{asset_id}", "FINANCE", st, body)

    # 6. Depreciation Report
    st, body = make_request("GET", "/finance/asset-cost-report/depreciation", finance_token)
    log_result("6. Depreciation Report", "GET", "/finance/asset-cost-report/depreciation", "FINANCE", st, body)

    # 7. Maintenance Cost Report
    st, body = make_request("GET", "/finance/asset-cost-report/maintenance-cost", finance_token)
    log_result("7. Maintenance Cost Report", "GET", "/finance/asset-cost-report/maintenance-cost", "FINANCE", st, body)

    # 8. Replacement Due Assets
    st, body = make_request("GET", "/finance/asset-cost-report/replacement-due", finance_token)
    log_result("8. Replacement Due Assets", "GET", "/finance/asset-cost-report/replacement-due", "FINANCE", st, body)

    # 9. Export PDF
    pdf_url = ""
    st, body = make_request("GET", "/finance/asset-cost-report/export/pdf", finance_token)
    log_result("9. Export PDF", "GET", "/finance/asset-cost-report/export/pdf", "FINANCE", st, body)
    if st == 200 and body.get("success") and body.get("data"):
        pdf_url = body["data"].get("downloadUrl", "")

    # 10. Export CSV
    csv_url = ""
    st, body = make_request("GET", "/finance/asset-cost-report/export/csv", finance_token)
    log_result("10. Export CSV", "GET", "/finance/asset-cost-report/export/csv", "FINANCE", st, body)
    if st == 200 and body.get("success") and body.get("data"):
        csv_url = body["data"].get("downloadUrl", "")

    # 11. Download PDF File
    if pdf_url:
        st, body = make_request("GET", pdf_url, finance_token)
        log_result("11. Download PDF File", "GET", pdf_url, "FINANCE", st, body)

    # 12. Download CSV File
    if csv_url:
        st, body = make_request("GET", csv_url, finance_token)
        log_result("12. Download CSV File", "GET", csv_url, "FINANCE", st, body)

    # Write Markdown Report
    print(f"Writing report to {REPORT_PATH}...")
    with open(REPORT_PATH, "w") as f:
        f.write("# Asset Cost Report APIs Verification Report\n")
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
            
    print("Done! View verification report at asset_verification_results.md")

if __name__ == "__main__":
    run_tests()
