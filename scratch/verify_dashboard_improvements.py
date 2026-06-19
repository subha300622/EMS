#!/usr/bin/env python3
import json
import urllib.request
import urllib.error
import sys

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

def run_verification():
    print("Logging in users...")
    
    # 1. Login Finance
    status, res = make_request("POST", "/auth/login", data={"email": "finance@company.com", "password": "finance@2"})
    if status == 200 and res.get("success"):
        finance_token = res["data"]["tokens"]["accessToken"]
        print("✅ Logged in as Finance")
    else:
        print("❌ Finance login failed", res)
        sys.exit(1)
        
    # 2. Login Employee
    status, res = make_request("POST", "/auth/login", data={"email": "employee@company.com", "password": "employee@3"})
    if status == 200 and res.get("success"):
        employee_token = res["data"]["tokens"]["accessToken"]
        print("✅ Logged in as Employee")
    else:
        print("❌ Employee login failed", res)
        sys.exit(1)

    print("\n--- Testing HRA split ---")
    st, body = make_request("GET", "/finance/salary/summary", finance_token)
    if st == 200 and "totalHra" in body["data"]:
        print("✅ Success: summary returns totalHra =", body["data"]["totalHra"])
    else:
        print("❌ Failure: summary does not contain totalHra", body)
        sys.exit(1)

    print("\n--- Testing Salary Distribution ---")
    st, body = make_request("GET", "/finance/salary/distribution", finance_token)
    if st == 200 and body.get("success"):
        print("✅ Success: salary distribution returned:", body["data"])
    else:
        print("❌ Failure: salary distribution failed", body)
        sys.exit(1)

    print("\n--- Creating a pending expense claim as Employee ---")
    expense_payload = {
        "title": "Client dinner test",
        "description": "Dinner with premium clients",
        "expenseDate": "2026-06-19",
        "amount": 2500.0,
        "currency": "INR",
        "category": "MEALS",
        "projectCode": "PRJ-101",
        "receiptIds": []
    }
    st, body = make_request("POST", "/my-expenses", employee_token, expense_payload)
    if st == 200 and body.get("success"):
        expense_id = body["data"]["expenseId"]
        print(f"✅ Success: expense created with ID {expense_id}")
    else:
        print("❌ Failure: expense creation failed", body)
        sys.exit(1)

    print(f"\n--- Approving expense EXP-{expense_id} as Finance ---")
    st, body = make_request("PATCH", f"/approvals/EXP-{expense_id}/approve", finance_token)
    if st == 200 and body.get("success"):
        print("✅ Success: expense approved successfully!")
    else:
        print("❌ Failure: unified approval failed", body)
        sys.exit(1)

    print("\n--- Checking expense status after approval ---")
    st, body = make_request("GET", f"/my-expenses/{expense_id}", employee_token)
    if st == 200 and body["data"]["payment"]["status"] == "PAID":
        print("✅ Success: expense is marked as PAID / REIMBURSED!")
    else:
        print("❌ Failure: expense status mismatch", body)
        sys.exit(1)

    print("\n🎉 ALL TESTS PASSED SUCCESSFULLY!")

if __name__ == "__main__":
    run_verification()
