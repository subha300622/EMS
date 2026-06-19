#!/usr/bin/env python3
import json
import urllib.request
import urllib.error
import sys

BASE_URL = "http://localhost:8080/api/v1"

def perform_api_call(method, endpoint, token=None, data=None):
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
        
    # Print Request Info
    print(f"\n==================================================")
    print(f"▶ REQUEST: {method} {url}")
    print(f"Headers: {json.dumps(req_headers, indent=2)}")
    if data is not None:
        print(f"Payload: {json.dumps(data, indent=2)}")
    print(f"--------------------------------------------------")

    req = urllib.request.Request(url, data=req_data, headers=req_headers, method=method)
    
    try:
        with urllib.request.urlopen(req) as response:
            resp_body = response.read().decode("utf-8")
            status_code = response.status
            print(f"◀ RESPONSE Status: {status_code}")
            try:
                parsed_json = json.loads(resp_body)
                print(f"Body:\n{json.dumps(parsed_json, indent=2)}")
                return status_code, parsed_json
            except json.JSONDecodeError:
                print(f"Body:\n{resp_body}")
                return status_code, resp_body
    except urllib.error.HTTPError as e:
        resp_body = e.read().decode("utf-8")
        print(f"◀ RESPONSE Status: {e.code}")
        try:
            parsed_json = json.loads(resp_body)
            print(f"Body:\n{json.dumps(parsed_json, indent=2)}")
            return e.code, parsed_json
        except json.JSONDecodeError:
            print(f"Body:\n{resp_body}")
            return e.code, resp_body
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        return 500, {"error": str(e)}

def run():
    print("--- 1. Login as Finance user ---")
    login_data_fin = {"email": "finance@company.com", "password": "finance@2"}
    st, res = perform_api_call("POST", "/auth/login", data=login_data_fin)
    if st != 200:
        print("Login failed")
        return
    finance_token = res["data"]["tokens"]["accessToken"]

    print("\n--- 2. Fetch Finance Salary Summary (shows totalHra split) ---")
    perform_api_call("GET", "/finance/salary/summary", finance_token)

    print("\n--- 3. Fetch Salary Bracket Distribution ---")
    perform_api_call("GET", "/finance/salary/distribution", finance_token)

    print("\n--- 4. Login as Employee user ---")
    login_data_emp = {"email": "employee@company.com", "password": "employee@3"}
    st, res = perform_api_call("POST", "/auth/login", data=login_data_emp)
    if st != 200:
        print("Login failed")
        return
    employee_token = res["data"]["tokens"]["accessToken"]

    print("\n--- 5. Create a new Expense Claim as Employee ---")
    expense_payload = {
        "title": "Premium Client Travel",
        "description": "Airport cab and client dining charges",
        "expenseDate": "2026-06-19",
        "amount": 4200.00,
        "currency": "INR",
        "category": "TRAVEL",
        "projectCode": "PRJ-101",
        "receiptIds": []
    }
    st, res = perform_api_call("POST", "/my-expenses", employee_token, data=expense_payload)
    if st != 200:
        print("Expense creation failed")
        return
    expense_id = res["data"]["expenseId"]

    print(f"\n--- 6. Unified Approval of Expense Claim EXP-{expense_id} as Finance ---")
    perform_api_call("PATCH", f"/approvals/EXP-{expense_id}/approve", finance_token)

    print(f"\n--- 7. Get Expense Claim Details to verify PAID status ---")
    perform_api_call("GET", f"/my-expenses/{expense_id}", employee_token)

if __name__ == "__main__":
    run()
