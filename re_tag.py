import os
import re

mapping = {
    # Authentication
    "src/main/java/com/example/ems/auth/controller/AuthController.java": "Authentication",

    # Dashboard
    "src/main/java/com/example/ems/employee/controller/EmployeeSelfServiceController.java": "Dashboard",

    # My Workspace
    "src/main/java/com/example/ems/schedule/controller/MyScheduleController.java": "My Workspace",
    "src/main/java/com/example/ems/support/controller/MySupportController.java": "My Workspace",

    # Employee Management
    "src/main/java/com/example/ems/employee/controller/EmployeeController.java": "Employee Management",
    "src/main/java/com/example/ems/employee/controller/EmployeeDirectoryController.java": "Employee Management",
    "src/main/java/com/example/ems/employee/controller/DepartmentController.java": "Employee Management",

    # Attendance & Leave
    "src/main/java/com/example/ems/attendance/controller/AttendanceController.java": "Attendance & Leave",
    "src/main/java/com/example/ems/leave/controller/LeaveController.java": "Attendance & Leave",

    # Performance Management
    "src/main/java/com/example/ems/performance/controller/GoalController.java": "Performance Management",
    "src/main/java/com/example/ems/performance/controller/MyPerformanceController.java": "Performance Management",
    "src/main/java/com/example/ems/performance/controller/PerformanceController.java": "Performance Management",
    "src/main/java/com/example/ems/appraisal/controller/AppraisalController.java": "Performance Management",
    "src/main/java/com/example/ems/appraisal/controller/SalaryRevisionController.java": "Performance Management",

    # Payroll & Finance
    "src/main/java/com/example/ems/payroll/controller/PayrollController.java": "Payroll & Finance",
    "src/main/java/com/example/ems/payroll/controller/PayslipController.java": "Payroll & Finance",
    "src/main/java/com/example/ems/payroll/controller/MyPayslipController.java": "Payroll & Finance",
    "src/main/java/com/example/ems/finance/controller/FinanceController.java": "Payroll & Finance",
    "src/main/java/com/example/ems/finance/controller/FinanceOnboardingController.java": "Payroll & Finance",

    # Recruitment & Lifecycle
    "src/main/java/com/example/ems/recruitment/controller/RecruitmentController.java": "Recruitment & Lifecycle",
    "src/main/java/com/example/ems/onboarding/controller/OnboardingController.java": "Recruitment & Lifecycle",
    "src/main/java/com/example/ems/offboarding/controller/OffboardingController.java": "Recruitment & Lifecycle",
    "src/main/java/com/example/ems/offboarding/controller/MyExitController.java": "Recruitment & Lifecycle",

    # Learning & Training
    "src/main/java/com/example/ems/training/controller/TrainingController.java": "Learning & Training",

    # Document Management
    "src/main/java/com/example/ems/common/controller/DmsController.java": "Document Management",
    "src/main/java/com/example/ems/employee/controller/MyDocumentController.java": "Document Management",

    # Expense Management
    "src/main/java/com/example/ems/expense/controller/MyExpenseController.java": "Expense Management",
    "src/main/java/com/example/ems/expense/controller/ExpenseCategoryController.java": "Expense Management",

    # Asset Management
    "src/main/java/com/example/ems/asset/controller/MyAssetController.java": "Asset Management",

    # Reports & Analytics
    "src/main/java/com/example/ems/reports/controller/DashboardReportController.java": "Reports & Analytics",

    # Administration
    "src/main/java/com/example/ems/auth/controller/UserController.java": "Administration",
    "src/main/java/com/example/ems/auth/controller/RoleController.java": "Administration",
    "src/main/java/com/example/ems/auth/controller/PermissionController.java": "Administration",
    "src/main/java/com/example/ems/common/controller/NotificationController.java": "Administration",

    # Audit & Settings
    "src/main/java/com/example/ems/settings/controller/MySettingsController.java": "Audit & Settings",

    # System Monitoring
    "src/main/java/com/example/ems/common/controller/MonitoringController.java": "System Monitoring"
}

for file_path, new_tag in mapping.items():
    if os.path.exists(file_path):
        with open(file_path, "r") as f:
            content = f.read()
        
        # Replace existing @Tag
        updated_content = re.sub(r'@Tag\(name\s*=\s*"[^"]+"\)', f'@Tag(name = "{new_tag}")', content)
        
        with open(file_path, "w") as f:
            f.write(updated_content)
        print(f"Updated {file_path} -> {new_tag}")
    else:
        print(f"File not found: {file_path}")

