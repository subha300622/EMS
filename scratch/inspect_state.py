#!/usr/bin/env python3
import json
import urllib.request
import urllib.error

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
    with urllib.request.urlopen(req) as response:
        return json.loads(response.read().decode("utf-8"))

def main():
    # Login Super Admin
    res = make_request("POST", "/auth/login", data={"email": "super_admin@company.com", "password": "super_admin@1"})
    admin_token = res["data"]["tokens"]["accessToken"]
    
    # Get employees
    emp_res = make_request("GET", "/employees", admin_token)
    print("Employees list:")
    for emp in emp_res.get("data", []):
        print(f"ID: {emp['id']}, EmployeeId: {emp.get('employeeId')}, Name: {emp['fullName']}, Email: {emp['email']}")
        
    # Get onboarding records
    onb_res = make_request("GET", "/onboarding-records", admin_token)
    print("\nOnboarding records:")
    for ob in onb_res.get("data", []):
        print(f"ID: {ob['id']}, Employee: {ob.get('employeeName')}, Email: {ob.get('employeeEmail')}, Status: {ob.get('status')}")

if __name__ == "__main__":
    main()
