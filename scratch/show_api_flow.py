#!/usr/bin/env python3
import json
import urllib.request
import urllib.error

BASE_URL = "http://localhost:8080/api/v1"

def print_separator(title):
    print("\n" + "=" * 80)
    print(f" {title} ".center(80, "="))
    print("=" * 80)

def make_request(method, endpoint, headers=None, data=None):
    url = f"{BASE_URL}{endpoint}"
    req_headers = {
        "Content-Type": "application/json",
        "Accept": "application/json"
    }
    if headers:
        req_headers.update(headers)
        
    req_data = None
    if data is not None:
        req_data = json.dumps(data).encode("utf-8")
        
    print(f"\n--- REQUEST ---")
    print(f"HTTP Method : {method}")
    print(f"URL         : {url}")
    print(f"Headers     : {json.dumps(req_headers, indent=2)}")
    if data is not None:
        print(f"Body        : {json.dumps(data, indent=2)}")
        
    req = urllib.request.Request(url, data=req_data, headers=req_headers, method=method)
    
    try:
        with urllib.request.urlopen(req) as response:
            resp_body = response.read().decode("utf-8")
            status_code = response.status
            print(f"\n--- RESPONSE ---")
            print(f"Status Code : {status_code}")
            try:
                parsed_json = json.loads(resp_body)
                print(f"Body        :\n{json.dumps(parsed_json, indent=2)}")
                return status_code, parsed_json
            except json.JSONDecodeError:
                print(f"Body        : {resp_body}")
                return status_code, resp_body
    except urllib.error.HTTPError as e:
        resp_body = e.read().decode("utf-8")
        print(f"\n--- RESPONSE (ERROR) ---")
        print(f"Status Code : {e.code}")
        try:
            parsed_json = json.loads(resp_body)
            print(f"Body        :\n{json.dumps(parsed_json, indent=2)}")
            return e.code, parsed_json
        except json.JSONDecodeError:
            print(f"Body        : {resp_body}")
            return e.code, resp_body
    except Exception as e:
        print(f"\n--- CONNECTION ERROR ---")
        print(f"Error: {e}")
        return None, None

def run_tests():
    # 1. Login
    print_separator("1. LOGIN AS EMPLOYEE")
    login_payload = {
        "email": "employee@company.com",
        "password": "employee@3"
    }
    status, response = make_request("POST", "/auth/login", data=login_payload)
    if status != 200 or not response or not response.get("success"):
        print("Login failed, aborting rest of flow.")
        return
        
    token = response["data"]["tokens"]["accessToken"]
    auth_headers = {"Authorization": f"Bearer {token}"}
    
    # 2. Get Dashboard
    print_separator("2. GET MY SUPPORT DASHBOARD")
    make_request("GET", "/my-support/dashboard", headers=auth_headers)
    
    # 3. Get Support Categories
    print_separator("3. GET SUPPORT CATEGORIES")
    status, response = make_request("GET", "/my-support/categories", headers=auth_headers)
    
    category_id = None
    sub_category_id = None
    if status == 200 and response and response.get("success") and response.get("data"):
        first_cat = response["data"][0]
        category_id = first_cat.get("id")
        if first_cat.get("subCategories"):
            sub_category_id = first_cat["subCategories"][0].get("id")
            
    if not category_id:
        category_id = 1
    if not sub_category_id:
        sub_category_id = 1
        
    # 4. Create Support Ticket
    print_separator("4. CREATE SUPPORT TICKET")
    ticket_payload = {
        "categoryId": category_id,
        "subCategoryId": sub_category_id,
        "subject": "Need help with VPN access settings",
        "description": "I cannot connect to the corporate internal network using the provided credentials. Please assist.",
        "priority": "HIGH",
        "preferredContactMethod": "EMAIL"
    }
    status, response = make_request("POST", "/my-support/tickets", headers=auth_headers, data=ticket_payload)
    
    ticket_id = None
    if status == 201 and response and response.get("success") and response.get("data"):
        ticket_id = response["data"].get("ticketId")
        
    # 5. List Tickets
    print_separator("5. LIST MY TICKETS")
    make_request("GET", "/my-support/tickets?status=OPEN&page=0&size=5", headers=auth_headers)
    
    if not ticket_id:
        print("Ticket creation failed or ID not returned, skipping detailed ticket operations.")
        return
        
    # 6. Ticket Details
    print_separator(f"6. GET TICKET DETAILS FOR TICKET {ticket_id}")
    make_request("GET", f"/my-support/tickets/{ticket_id}", headers=auth_headers)
    
    # 7. Add Comment
    print_separator(f"7. ADD COMMENT TO TICKET {ticket_id}")
    comment_payload = {
        "commentText": "Updating: I tried rebooting my computer but still getting error code VPN-101."
    }
    make_request("POST", f"/my-support/tickets/{ticket_id}/comments", headers=auth_headers, data=comment_payload)
    
    # 8. Activity Timeline
    print_separator(f"8. GET ACTIVITY TIMELINE FOR TICKET {ticket_id}")
    make_request("GET", f"/my-support/tickets/{ticket_id}/timeline", headers=auth_headers)
    
    # 9. Close Ticket
    print_separator(f"9. CLOSE TICKET {ticket_id}")
    close_payload = {
        "rating": 5,
        "feedback": "Prompt support, problem resolved after IT guide."
    }
    make_request("PATCH", f"/my-support/tickets/{ticket_id}/close", headers=auth_headers, data=close_payload)

if __name__ == "__main__":
    run_tests()
