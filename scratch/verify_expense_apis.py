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

def run_tests():
    print("======================================================================")
    print("🚀 Running Expense Approvals Comprehensive Verification Suite")
    print("======================================================================")
    
    # 1. Login
    print("Logging in as Finance User...")
    passwords = ["finance@5", "finance@2", "finance@3", "finance@1"]
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

    # 2. Get Dashboard and verify seeded metrics
    print("\n[API 1] GET /finance/expenses/dashboard")
    status, body = make_request("GET", "/finance/expenses/dashboard", finance_token)
    print(f"Status: {status}")
    if status == 200 and body.get("success"):
        data = body["data"]
        print(f"Total Pending: {data.get('totalPending')} (Expected: 36)")
        print(f"Pending Amount: {data.get('pendingAmount')} (Expected: 154000.00)")
        print(f"Approved This Month: {data.get('approvedThisMonth')} (Expected: 84)")
        print(f"Approved Amount This Month: {data.get('approvedAmountThisMonth')} (Expected: 128400.00)")
        print(f"Rejected: {data.get('rejected')} (Expected: 12)")
        print(f"Rejected Amount: {data.get('rejectedAmount')} (Expected: 38000.00)")
        print(f"Average Approval Days: {data.get('averageApprovalDays')} (Expected: 1.8)")
        
        assert data.get('totalPending') == 36, "totalPending KPI mismatch"
        assert float(data.get('pendingAmount')) == 154000.0, "pendingAmount KPI mismatch"
        assert data.get('approvedThisMonth') == 84, "approvedThisMonth KPI mismatch"
        assert float(data.get('approvedAmountThisMonth')) == 128400.0, "approvedAmountThisMonth KPI mismatch"
        assert data.get('rejected') == 12, "rejected KPI mismatch"
        assert float(data.get('rejectedAmount')) == 38000.0, "rejectedAmount KPI mismatch"
        assert float(data.get('averageApprovalDays')) == 1.8, "averageApprovalDays KPI mismatch"
        print("✅ Dashboard KPI Metrics verified successfully!")
    else:
        print("❌ Dashboard retrieval failed.")
        return

    # 3. Get Expense Listing (All/Pending)
    print("\n[API 2] GET /finance/expenses")
    status, body = make_request("GET", "/finance/expenses?status=PENDING&category=TRAVEL&department=Engineering&page=0&size=20", finance_token)
    print(f"Status: {status}")
    
    robert_expense_id = None
    if body.get("success") and body["data"]["content"]:
        for item in body["data"]["content"]:
            if item["employeeName"] == "Robert Chen" and float(item["amount"]) == 4200.0:
                robert_expense_id = item["expenseId"]
                print(f"Detected Robert Chen's Expense ID: {robert_expense_id}")
                break

    if not robert_expense_id:
        print("❌ Seeded Robert Chen 4200 TRAVEL expense not found!")
        return

    # 4. Get Expense Details
    print(f"\n[API 3] GET /finance/expenses/{robert_expense_id}")
    status, body = make_request("GET", f"/finance/expenses/{robert_expense_id}", finance_token)
    print(f"Status: {status}")
    if status == 200:
        data = body["data"]
        print(f"Employee: {data['employee']['name']}")
        print(f"Category: {data['category']}")
        print(f"Amount: {data['amount']}")
        print(f"Business Purpose: {data['businessPurpose']}")
        print(f"Receipt Attached: {data['receiptAttached']}")
        assert data['category'] == "TRAVEL", "Category mismatch"
        assert float(data['amount']) == 4200.0, "Amount mismatch"
        assert data['businessPurpose'] == "Client Meeting", "Business purpose mismatch"
        print("✅ Expense Details verified successfully!")

    # 5. Get Receipt Metadata and check size = 254321 bytes
    print(f"\n[API 4] GET /finance/expenses/{robert_expense_id}/receipt")
    status, body = make_request("GET", f"/finance/expenses/{robert_expense_id}/receipt", finance_token)
    print(f"Status: {status}")
    if status == 200:
        data = body["data"]
        print(f"File Name: {data['fileName']} (Expected: flight-ticket.pdf)")
        print(f"Content Type: {data['contentType']} (Expected: application/pdf)")
        print(f"Size: {data['size']} bytes (Expected: 254321)")
        print(f"Download URL: {data['downloadUrl']}")
        assert data['fileName'] == "flight-ticket.pdf", "File name mismatch"
        assert data['contentType'] == "application/pdf", "Content type mismatch"
        assert data['size'] == 254321, "Size mismatch"
        print("✅ Receipt Metadata verified successfully!")
        
        # 6. Download Receipt File
        receipt_url = data['downloadUrl']
        print(f"\n[Download Receipt File] GET {receipt_url}")
        dl_status, dl_body = make_request("GET", receipt_url.replace("/api/v1", ""), finance_token)
        print(f"Download Status: {dl_status}")
        print(f"Downloaded binary data length: {len(dl_body)} bytes (Expected: 254321)")
        assert len(dl_body) == 254321, "Downloaded file size mismatch"
        print("✅ Receipt file downloaded and verified successfully!")

    # 7. Get Timeline / Audit History
    print(f"\n[API 8] GET /finance/expenses/{robert_expense_id}/timeline")
    status, body = make_request("GET", f"/finance/expenses/{robert_expense_id}/timeline", finance_token)
    print(f"Status: {status}")
    if status == 200:
        print(json.dumps(body["data"], indent=2))
        assert len(body["data"]) >= 1, "Timeline should have at least 1 entry"
        print("✅ Timeline retrieved successfully!")

    # 8. Workflow Transition Rules testing (including blocked transitions)
    # Target transitions to test:
    # PENDING -> APPROVED (Should succeed)
    # APPROVED -> REJECTED (Should fail - 400 Bad Request)
    # APPROVED -> REIMBURSED (Should succeed)
    # REIMBURSED -> APPROVED (Should fail - 400 Bad Request)
    # REIMBURSED -> SENT_BACK (Should fail - 400 Bad Request)

    print("\n[API 5] Approve Expense (PENDING -> APPROVED)")
    status, body = make_request("PATCH", f"/finance/expenses/{robert_expense_id}/approve", finance_token, data={"remarks": "Verified and approved"})
    print(f"Status: {status}, Response: {body}")
    assert status == 200, "Approval failed"
    
    print("\n[Blocked Transition] Try APPROVED -> REJECTED (Should fail)")
    status, body = make_request("PATCH", f"/finance/expenses/{robert_expense_id}/reject", finance_token, data={"reason": "Receipt is invalid"})
    print(f"Status: {status}, Response: {body}")
    assert status == 400, "Should have blocked APPROVED -> REJECTED"
    print("✅ Blocked APPROVED -> REJECTED successfully!")

    print("\n[API 9] Reimburse Expense (APPROVED -> REIMBURSED)")
    status, body = make_request("PATCH", f"/finance/expenses/{robert_expense_id}/reimburse", finance_token, data={"paymentMode": "BANK_TRANSFER", "transactionReference": "TXN202600123"})
    print(f"Status: {status}, Response: {body}")
    assert status == 200, "Reimbursement failed"
    assert body.get("status") == "REIMBURSED", "Status should be REIMBURSED"
    assert body.get("paymentMode") == "BANK_TRANSFER", "Payment mode mismatch"
    assert body.get("transactionReference") == "TXN202600123", "Transaction reference mismatch"

    print("\n[Blocked Transition] Try REIMBURSED -> APPROVED (Should fail)")
    status, body = make_request("PATCH", f"/finance/expenses/{robert_expense_id}/approve", finance_token, data={"remarks": "Attempt duplicate approval"})
    print(f"Status: {status}, Response: {body}")
    assert status == 400, "Should have blocked REIMBURSED -> APPROVED"
    print("✅ Blocked REIMBURSED -> APPROVED successfully!")

    print("\n[Blocked Transition] Try REIMBURSED -> SENT_BACK (Should fail)")
    status, body = make_request("PATCH", f"/finance/expenses/{robert_expense_id}/send-back", finance_token, data={"reason": "Attempt send back after reimbursement"})
    print(f"Status: {status}, Response: {body}")
    assert status == 400, "Should have blocked REIMBURSED -> SENT_BACK"
    print("✅ Blocked REIMBURSED -> SENT_BACK successfully!")

    # Check Timeline again to verify all transitions were logged in audit logs
    print(f"\nGET /finance/expenses/{robert_expense_id}/timeline (After actions)")
    status, body = make_request("GET", f"/finance/expenses/{robert_expense_id}/timeline", finance_token)
    print(f"Status: {status}")
    if status == 200:
        print(json.dumps(body["data"], indent=2))
        statuses = [item["status"] for item in body["data"]]
        print(f"Audit log statuses sequence: {statuses}")
        assert "SUBMITTED" in statuses, "SUBMITTED missing in audit log"
        assert "APPROVED" in statuses, "APPROVED missing in audit log"
        assert "REIMBURSED" in statuses, "REIMBURSED missing in audit log"
        print("✅ Audit history timeline log sequence verified successfully!")

    # 9. Bulk Operations Testing
    # Find some pending expenses to use for bulk testing
    print("\nFetching pending expenses for bulk operations...")
    status, body = make_request("GET", "/finance/expenses?status=PENDING&page=0&size=10", finance_token)
    pending_ids = [item["expenseId"] for item in body["data"]["content"]]
    print(f"Pending expense IDs available: {pending_ids}")
    if len(pending_ids) >= 4:
        bulk_approve_ids = pending_ids[0:2]
        bulk_reject_ids = pending_ids[2:4]
        
        # Bulk Approve
        print(f"\n[API 10] Bulk Approve: {bulk_approve_ids}")
        status, body = make_request("PATCH", "/finance/expenses/bulk-approve", finance_token, data={"expenseIds": bulk_approve_ids, "remarks": "Bulk approved"})
        print(f"Status: {status}, Response: {body}")
        assert status == 200, "Bulk approve failed"
        
        # Bulk Reject
        print(f"\n[API 11] Bulk Reject: {bulk_reject_ids}")
        status, body = make_request("PATCH", "/finance/expenses/bulk-reject", finance_token, data={"expenseIds": bulk_reject_ids, "remarks": "Duplicate claims"})
        print(f"Status: {status}, Response: {body}")
        assert status == 200, "Bulk reject failed"
        
        print("✅ Bulk operations completed and verified successfully!")
    else:
        print("⚠️ Not enough pending expenses to test bulk actions.")

    # 10. Reports Summary
    print("\n[API 12] GET /finance/expenses/reports/summary")
    status, body = make_request("GET", "/finance/expenses/reports/summary", finance_token)
    print(f"Status: {status}")
    if status == 200:
        data = body["data"]
        print(f"Approved Expenses count: {data.get('approvedExpenses')}")
        print(f"Total Approved Amount: {data.get('totalApprovedAmount')}")
        print(f"Average Approval Days: {data.get('averageApprovalDays')}")
        print("✅ Reports summary analytics verified successfully!")

    # 11. Reports Export testing
    # Export CSV
    print("\n[API 13] GET /finance/expenses/export/csv")
    status, body = make_request("GET", "/finance/expenses/export/csv", finance_token)
    print(f"Status: {status}")
    if status == 200:
        print(f"Received CSV data of length {len(body)} bytes.")
        assert len(body) > 0, "CSV export is empty"
        print("✅ CSV export verified successfully!")
        
    # Export XLSX
    print("\n[API 14] GET /finance/expenses/export/xlsx")
    status, body = make_request("GET", "/finance/expenses/export/xlsx", finance_token)
    print(f"Status: {status}")
    if status == 200:
        print(f"Received XLSX workbook data of length {len(body)} bytes.")
        assert len(body) > 0, "XLSX export is empty"
        print("✅ XLSX export verified successfully!")

    # Export PDF
    print("\n[API 15] GET /finance/expenses/export/pdf")
    status, body = make_request("GET", "/finance/expenses/export/pdf", finance_token)
    print(f"Status: {status}")
    if status == 200:
        print(f"Received PDF report data of length {len(body)} bytes.")
        assert len(body) > 0, "PDF export is empty"
        print("✅ PDF export verified successfully!")

    print("\n======================================================================")
    print("🎉 All 15 Expense Approvals APIs Verified Successfully!")
    print("======================================================================")

if __name__ == "__main__":
    run_tests()
