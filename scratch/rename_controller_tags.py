import os, re

def replace_in_file(path, pattern, replacement):
    if not os.path.exists(path):
        print(f"ERROR: File not found {path}")
        return False
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    new_content = re.sub(pattern, replacement, content)
    if content != new_content:
        with open(path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {os.path.basename(path)}")
        return True
    return False

# Simple class-level tag renames
renames = [
    # User & RBAC
    ("auth/controller/UserController.java", r'@Tag\(name\s*=\s*"Users & RBAC"\)', '@Tag(name = "User & RBAC")'),
    ("auth/controller/RoleController.java", r'@Tag\(name\s*=\s*"Users & RBAC"\)', '@Tag(name = "User & RBAC")'),
    ("auth/controller/PermissionController.java", r'@Tag\(name\s*=\s*"Users & RBAC"\)', '@Tag(name = "User & RBAC")'),

    # Audit Logs
    ("audit/controller/AuditLogController.java", r'@Tag\(name\s*=\s*"Settings"\)', '@Tag(name = "Audit Logs")'),

    # System Administration
    ("settings/controller/SystemSettingController.java", r'@Tag\(name\s*=\s*"Settings"\)', '@Tag(name = "System Administration")'),
    ("common/controller/MonitoringController.java", r'@Tag\(name\s*=\s*"Settings"\)', '@Tag(name = "System Administration")'),
    ("employee/controller/AnnouncementController.java", r'@Tag\(name\s*=\s*"Employees"\)', '@Tag(name = "System Administration")'),

    # Approval Center
    ("common/controller/ApprovalCenterController.java", r'@Tag\(name\s*=\s*"Employees"\)', '@Tag(name = "Approval Center")'),

    # Notifications
    ("common/controller/NotificationController.java", r'@Tag\(name\s*=\s*"Administration"\)', '@Tag(name = "Notifications")'),

    # Organization Management
    ("employee/controller/DepartmentController.java", r'@Tag\(name\s*=\s*"Employees"\)', '@Tag(name = "Organization Management")'),
    ("employee/controller/EmployeeDirectoryController.java", r'@Tag\(name\s*=\s*"Employees"\)', '@Tag(name = "Organization Management")'),
    ("employee/controller/OrgChartController.java", r'@Tag\(name\s*=\s*"Employees"\)', '@Tag(name = "Organization Management")'),
    ("employee/controller/TeamManagementController.java", r'@Tag\(name\s*=\s*"Employees"\)', '@Tag(name = "Organization Management")'),

    # Employee Management
    ("employee/controller/EmployeeController.java", r'@Tag\(name\s*=\s*"Employees"\)', '@Tag(name = "Employee Management")'),

    # Attendance Management
    ("attendance/controller/AttendanceController.java", r'@Tag\(name\s*=\s*"Attendance"\)', '@Tag(name = "Attendance Management")'),

    # Payroll Management
    ("payroll/controller/PayrollController.java", r'@Tag\(name\s*=\s*"Payroll"\)', '@Tag(name = "Payroll Management")'),
    ("payroll/controller/PayrollSettingsController.java", r'@Tag\(name\s*=\s*"Payroll"\)', '@Tag(name = "Payroll Management")'),
    ("payroll/controller/PayslipController.java", r'@Tag\(name\s*=\s*"Payroll"\)', '@Tag(name = "Payroll Management")'),
    ("appraisal/controller/SalaryRevisionController.java", r'@Tag\(name\s*=\s*"Payroll"\)', '@Tag(name = "Payroll Management")'),

    # Finance Management
    ("finance/controller/FinanceController.java", r'@Tag\(name\s*=\s*"Finance"\)', '@Tag(name = "Finance Management")'),
    ("finance/controller/FinanceSetupController.java", r'@Tag\(name\s*=\s*"Finance"\)', '@Tag(name = "Finance Management")'),
    ("expense/controller/ExpenseCategoryController.java", r'@Tag\(name\s*=\s*"Finance"\)', '@Tag(name = "Finance Management")'),
    ("payroll/controller/FnfSettlementController.java", r'@Tag\(name\s*=\s*"Finance"\)', '@Tag(name = "Finance Management")'),

    # Performance Management
    ("performance/controller/PerformanceController.java", r'@Tag\(name\s*=\s*"Performance"\)', '@Tag(name = "Performance Management")'),
    ("performance/controller/GoalController.java", r'@Tag\(name\s*=\s*"Performance"\)', '@Tag(name = "Performance Management")'),
    ("appraisal/controller/AppraisalController.java", r'@Tag\(name\s*=\s*"Performance"\)', '@Tag(name = "Performance Management")'),

    # Asset Management
    ("asset/controller/AssetAdminController.java", r'@Tag\(name\s*=\s*"Assets"\)', '@Tag(name = "Asset Management")'),

    # Document Management
    ("common/controller/DmsController.java", r'@Tag\(name\s*=\s*"Documents"\)', '@Tag(name = "Document Management")'),

    # Training Management
    ("training/controller/TrainingController.java", r'@Tag\(name\s*=\s*"Training"\)', '@Tag(name = "Training Management")'),

    # Employee Portal Class Renames
    ("asset/controller/MyAssetController.java", r'@Tag\(name\s*=\s*"Employee Self Service"\)', '@Tag(name = "My Assets")'),
    ("employee/controller/MyDocumentController.java", r'@Tag\(name\s*=\s*"Employee Self Service"\)', '@Tag(name = "My Documents")'),
    ("expense/controller/MyExpenseController.java", r'@Tag\(name\s*=\s*"Employee Self Service"\)', '@Tag(name = "My Expenses")'),
    ("payroll/controller/MyPayslipController.java", r'@Tag\(name\s*=\s*"Employee Self Service"\)', '@Tag(name = "My Payroll")'),
    ("performance/controller/MyPerformanceController.java", r'@Tag\(name\s*=\s*"Employee Self Service"\)', '@Tag(name = "My Performance")'),
    ("schedule/controller/MyScheduleController.java", r'@Tag\(name\s*=\s*"Employee Self Service"\)', '@Tag(name = "My Schedule")'),
    ("offboarding/controller/MyExitController.java", r'@Tag\(name\s*=\s*"Employee Self Service"\)', '@Tag(name = "My Exit")'),
    ("settings/controller/MySettingsController.java", r'@Tag\(name\s*=\s*"Employee Self Service"\)', '@Tag(name = "My Settings")'),
]

base_dir = "/home/subashini/Documents/ems-backend/src/main/java/com/example/ems"

for path, pattern, replacement in renames:
    full_path = os.path.join(base_dir, path)
    replace_in_file(full_path, pattern, replacement)

# ── Method level overrides in AttendanceController.java ──────────────────────
# Check-in, check-out, and get my attendance endpoints
attendance_path = os.path.join(base_dir, "attendance/controller/AttendanceController.java")
if os.path.exists(attendance_path):
    with open(attendance_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Let's add @Tag(name = "My Attendance") before checkIn, checkOut, and getMyAttendance
    # We find @PostMapping("/attendance/me/check-in")
    content = content.replace(
        '@PostMapping("/attendance/me/check-in")',
        '@Tag(name = "My Attendance")\n    @PostMapping("/attendance/me/check-in")'
    )
    content = content.replace(
        '@PostMapping("/attendance/me/check-out")',
        '@Tag(name = "My Attendance")\n    @PostMapping("/attendance/me/check-out")'
    )
    content = content.replace(
        '@GetMapping("/attendance/me")',
        '@Tag(name = "My Attendance")\n    @GetMapping("/attendance/me")'
    )
    with open(attendance_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Updated AttendanceController.java method overrides")

# ── Method level overrides in LeaveController.java ──────────────────────────
# getMyLeaveRecords
leave_path = os.path.join(base_dir, "leave/controller/LeaveController.java")
if os.path.exists(leave_path):
    with open(leave_path, 'r', encoding='utf-8') as f:
        content = f.read()
    content = content.replace(
        '@GetMapping("/leaves/my")',
        '@Tag(name = "My Leave")\n    @GetMapping("/leaves/my")'
    )
    with open(leave_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Updated LeaveController.java method overrides")

# ── Method level overrides in UserController.java ───────────────────────────
# getProfile and updateProfile
user_path = os.path.join(base_dir, "auth/controller/UserController.java")
if os.path.exists(user_path):
    with open(user_path, 'r', encoding='utf-8') as f:
        content = f.read()
    content = content.replace(
        '@GetMapping("/users/profile")',
        '@Tag(name = "My Profile")\n    @GetMapping("/users/profile")'
    )
    content = content.replace(
        '@PutMapping("/users/profile")',
        '@Tag(name = "My Profile")\n    @PutMapping("/users/profile")'
    )
    with open(user_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Updated UserController.java method overrides")

# ── Method level overrides in MySupportController.java ──────────────────────
# Replace all @Tag(name = "Employee Self Service") with @Tag(name = "My Support")
support_path = os.path.join(base_dir, "support/controller/MySupportController.java")
replace_in_file(support_path, r'@Tag\(name\s*=\s*"Employee Self Service"\)', '@Tag(name = "My Support")')

# ── Remove class-level and add method-level in EmployeeSelfServiceController ─
ess_path = os.path.join(base_dir, "employee/controller/EmployeeSelfServiceController.java")
if os.path.exists(ess_path):
    with open(ess_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Remove class-level @Tag
    content = re.sub(r'@Tag\(name\s*=\s*"Employee Self Service"\)\s*\n', '', content)
    
    # Add method level
    content = content.replace(
        '@GetMapping("/employees/me/dashboard")',
        '@Tag(name = "My Profile")\n    @GetMapping("/employees/me/dashboard")'
    )
    content = content.replace(
        '@GetMapping("/employees/me/profile")',
        '@Tag(name = "My Profile")\n    @GetMapping("/employees/me/profile")'
    )
    content = content.replace(
        '@PutMapping("/employees/me/profile")',
        '@Tag(name = "My Profile")\n    @PutMapping("/employees/me/profile")'
    )
    content = content.replace(
        '@GetMapping("/employees/me/assets")',
        '@Tag(name = "My Assets")\n    @GetMapping("/employees/me/assets")'
    )
    content = content.replace(
        '@PostMapping("/employees/me/assets/{id}/request")',
        '@Tag(name = "My Assets")\n    @PostMapping("/employees/me/assets/{id}/request")'
    )
    content = content.replace(
        '@GetMapping("/employees/me/performance")',
        '@Tag(name = "My Performance")\n    @GetMapping("/employees/me/performance")'
    )
    content = content.replace(
        '@PostMapping("/employees/me/performance/{id}/self-review")',
        '@Tag(name = "My Performance")\n    @PostMapping("/employees/me/performance/{id}/self-review")'
    )
    content = content.replace(
        '@GetMapping("/employees/me/schedule")',
        '@Tag(name = "My Schedule")\n    @GetMapping("/employees/me/schedule")'
    )
    with open(ess_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Updated EmployeeSelfServiceController.java")

# ── Remove class-level and add method-level in MyDataExportController ────────
mde_path = os.path.join(base_dir, "settings/controller/MyDataExportController.java")
if os.path.exists(mde_path):
    with open(mde_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Remove class-level @Tag
    content = re.sub(r'@Tag\(name\s*=\s*"Employee Self Service"\)\s*\n', '', content)
    
    content = content.replace(
        '@GetMapping("/my-payslips/export")',
        '@Tag(name = "My Payroll")\n    @GetMapping("/my-payslips/export")'
    )
    content = content.replace(
        '@GetMapping("/my-attendance/export")',
        '@Tag(name = "My Attendance")\n    @GetMapping("/my-attendance/export")'
    )
    content = content.replace(
        '@GetMapping("/my-leaves/export")',
        '@Tag(name = "My Leave")\n    @GetMapping("/my-leaves/export")'
    )
    content = content.replace(
        '@GetMapping("/my-expenses/export")',
        '@Tag(name = "My Expenses")\n    @GetMapping("/my-expenses/export")'
    )
    content = content.replace(
        '@GetMapping("/my-performance/export")',
        '@Tag(name = "My Performance")\n    @GetMapping("/my-performance/export")'
    )
    content = content.replace(
        '@GetMapping("/my-documents/export")',
        '@Tag(name = "My Documents")\n    @GetMapping("/my-documents/export")'
    )
    content = content.replace(
        '@GetMapping("/my-trainings/export")',
        '@Tag(name = "My Profile")\n    @GetMapping("/my-trainings/export")'
    )
    content = content.replace(
        '@PostMapping("/my-settings/data/export")',
        '@Tag(name = "My Settings")\n    @PostMapping("/my-settings/data/export")'
    )
    content = content.replace(
        '@GetMapping("/my-settings/data/export/{requestId}")',
        '@Tag(name = "My Settings")\n    @GetMapping("/my-settings/data/export/{requestId}")'
    )
    content = content.replace(
        '@GetMapping("/my-settings/data/export/{requestId}/download")',
        '@Tag(name = "My Settings")\n    @GetMapping("/my-settings/data/export/{requestId}/download")'
    )
    with open(mde_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Updated MyDataExportController.java")
