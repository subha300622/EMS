#!/usr/bin/env python3
import json
import urllib.request
import urllib.error
import time

BASE_URL = "http://localhost:8080/api/v1"

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
            resp_body = response.read()
            status_code = response.status
            content_type = response.headers.get("Content-Type", "")
            
            if "application/json" in content_type:
                try:
                    parsed_json = json.loads(resp_body.decode("utf-8"))
                    return status_code, parsed_json
                except json.JSONDecodeError:
                    return status_code, resp_body.decode("utf-8")
            else:
                return status_code, f"<Binary Data of length {len(resp_body)}>"
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
    print("======================================================================")
    print("🚀 Running F&F Settlements Verification Suite")
    print("======================================================================")
    
    # 1. Login
    print("Logging in as Finance User...")
    # Trying common seeded password combinations
    passwords = ["finance@2", "finance@5"]
    finance_token = None
    
    for pwd in passwords:
        status, res = make_request("POST", "/auth/login", data={"email": "finance@company.com", "password": pwd})
        if status == 200 and res.get("success"):
            finance_token = res["data"]["tokens"]["accessToken"]
            print(f"✅ Logged in successfully using password: {pwd}")
            break
            
    if not finance_token:
        print("❌ Finance login failed.")
        return

    # 2. Get Dashboard
    print("\n[API 1] GET /finance/settlements/dashboard")
    status, body = make_request("GET", "/finance/settlements/dashboard", finance_token)
    print(f"Status: {status}")
    print(json.dumps(body, indent=2))

    # 3. Get Settlements List
    print("\n[API 2] GET /finance/settlements (All)")
    status, body = make_request("GET", "/finance/settlements", finance_token)
    print(f"Status: {status}")
    print(f"Total elements: {body['data']['totalElements']}")
    
    ravi_id = None
    priya_id = None
    
    for item in body["data"]["content"]:
        if "Ravi Kumar" in item["employeeName"]:
            ravi_id = item["settlementId"]
        if "Priya Sharma" in item["employeeName"]:
            priya_id = item["settlementId"]

    print(f"Detected Ravi Kumar Settlement ID: {ravi_id}")
    print(f"Detected Priya Sharma Settlement ID: {priya_id}")

    print("\n[API 2 (Filtered)] GET /finance/settlements?status=PENDING_REVIEW&search=Priya&department=HR")
    status, body = make_request("GET", "/finance/settlements?status=PENDING_REVIEW&search=Priya&department=HR", finance_token)
    print(f"Status: {status}")
    print(json.dumps(body, indent=2))

    if not ravi_id:
        print("❌ Seeded Ravi Kumar settlement not found!")
        return

    # 4. Get Settlement By ID
    print(f"\n[API 3] GET /finance/settlements/{ravi_id} (Review Popup)")
    status, body = make_request("GET", f"/finance/settlements/{ravi_id}", finance_token)
    print(f"Status: {status}")
    print(json.dumps(body, indent=2))

    # 5. Get Asset Clearance
    print(f"\n[API 4] GET /finance/settlements/{ravi_id}/asset-clearance")
    status, body = make_request("GET", f"/finance/settlements/{ravi_id}/asset-clearance", finance_token)
    print(f"Status: {status}")
    print(json.dumps(body, indent=2))

    # 6. Get Timeline
    print(f"\n[API 5] GET /finance/settlements/{ravi_id}/timeline")
    status, body = make_request("GET", f"/finance/settlements/{ravi_id}/timeline", finance_token)
    print(f"Status: {status}")
    print(json.dumps(body, indent=2))

    # 7. Send Back to HR
    print(f"\n[API 6] PATCH /finance/settlements/{priya_id}/send-back")
    status, body = make_request("PATCH", f"/finance/settlements/{priya_id}/send-back", finance_token, data={"reason": "Asset recovery mismatch"})
    print(f"Status: {status}")
    print(json.dumps(body, indent=2))

    # Check Priya's Timeline to verify send-back event logged
    print(f"\nGET /finance/settlements/{priya_id}/timeline")
    status, body = make_request("GET", f"/finance/settlements/{priya_id}/timeline", finance_token)
    print(json.dumps(body, indent=2))

    # 8. Reject Settlement
    print(f"\n[API 7] PATCH /finance/settlements/{priya_id}/reject")
    status, body = make_request("PATCH", f"/finance/settlements/{priya_id}/reject", finance_token, data={"reason": "Audit failed"})
    print(f"Status: {status}")
    print(json.dumps(body, indent=2))

    # Check Priya's Timeline to verify rejection logged
    print(f"\nGET /finance/settlements/{priya_id}/timeline")
    status, body = make_request("GET", f"/finance/settlements/{priya_id}/timeline", finance_token)
    print(json.dumps(body, indent=2))

    # 9. Approve Settlement (Ravi Kumar)
    print(f"\n[API 8] PATCH /finance/settlements/{ravi_id}/approve")
    status, body = make_request("PATCH", f"/finance/settlements/{ravi_id}/approve", finance_token, data={"remarks": "Verified and approved"})
    print(f"Status: {status}")
    print(json.dumps(body, indent=2))

    # 10. Process Settlement (Ravi Kumar)
    print(f"\n[API 9] POST /finance/settlements/{ravi_id}/process")
    status, body = make_request("POST", f"/finance/settlements/{ravi_id}/process", finance_token, data={
        "paymentMode": "BANK_TRANSFER",
        "transactionReference": "FNF20260001"
    })
    print(f"Status: {status}")
    print(json.dumps(body, indent=2))

    # 11. Get Status Details
    print(f"\n[API 10] GET /finance/settlements/{ravi_id}/status")
    status, body = make_request("GET", f"/finance/settlements/{ravi_id}/status", finance_token)
    print(f"Status: {status}")
    print(json.dumps(body, indent=2))

    # 12. Get PDF Metadata
    print(f"\n[API 11] GET /finance/settlements/{ravi_id}/pdf")
    status, body = make_request("GET", f"/finance/settlements/{ravi_id}/pdf", finance_token)
    print(f"Status: {status}")
    print(json.dumps(body, indent=2))
    pdf_download_url = body["data"]["downloadUrl"]

    # Download PDF
    print(f"\n[Download FNF PDF] GET {pdf_download_url}")
    status, body = make_request("GET", pdf_download_url.replace("/api/v1", ""), finance_token)
    print(f"Status: {status}")
    print(body)

    # 13. Reports Summary
    print("\n[API 12] GET /finance/settlements/reports/summary")
    status, body = make_request("GET", "/finance/settlements/reports/summary", finance_token)
    print(f"Status: {status}")
    print(json.dumps(body, indent=2))

    # 14. Export Metadata
    print("\n[API 13] GET /finance/settlements/export")
    status, body = make_request("GET", "/finance/settlements/export", finance_token)
    print(f"Status: {status}")
    print(json.dumps(body, indent=2))
    csv_download_url = body["data"]["downloadUrl"]

    # Download Export CSV
    print(f"\n[Download Settlements CSV] GET {csv_download_url}")
    status, body = make_request("GET", csv_download_url.replace("/api/v1", ""), finance_token)
    print(f"Status: {status}")
    print(str(body)[:200] + "...")

    print("\n======================================================================")
    print("🎉 Verification Suite Completed!")
    print("======================================================================")

if __name__ == "__main__":
    run_tests()
