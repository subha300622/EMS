package com.example.ems.settings.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.MyEmployeeDocument;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.MyEmployeeDocumentRepository;
import com.example.ems.security.service.JwtService;
import com.example.ems.settings.service.MySettingsService;
import com.example.ems.payroll.entity.Payslip;
import com.example.ems.payroll.repository.PayslipRepository;
import com.example.ems.payroll.service.MyPayslipService;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.performance.entity.Goal;
import com.example.ems.performance.entity.PerformanceReview;
import com.example.ems.performance.repository.GoalRepository;
import com.example.ems.performance.repository.PerformanceReviewRepository;
import com.example.ems.training.entity.TrainingEnrollment;
import com.example.ems.training.repository.TrainingEnrollmentRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "My Data Export")
public class MyDataExportController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MySettingsService mySettingsService;

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private MyPayslipService myPayslipService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private PerformanceReviewRepository reviewRepository;

    @Autowired
    private MyEmployeeDocumentRepository employeeDocumentRepository;

    @Autowired
    private TrainingEnrollmentRepository trainingEnrollmentRepository;

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email).orElse(null);
            }
        }
        return null;
    }

    private Employee resolveEmployee(User user) {
        if (user == null) return null;
        return employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
    }

    private boolean checkPermission(User user, String permission) {
        if (user == null) return false;
        return roleService.hasPermission(user.getWorkEmail(), permission)
                || roleService.isSuperAdmin(user.getWorkEmail());
    }

    private ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<?> forbiddenResponse(String permission) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires '" + permission + "' permission.", "AUTH_002"));
    }

    private byte[] generatePdf(String title, List<String> lines) {
        StringBuilder contentStream = new StringBuilder();
        contentStream.append("BT\n/F1 12 Tf\n70 720 Td\n(").append(title).append(") Tj\n");
        for (String line : lines) {
            // Escape parentheses for PDF text format
            String escapedLine = line.replace("(", "\\(").replace(")", "\\)");
            contentStream.append("0 -20 Td\n(").append(escapedLine).append(") Tj\n");
        }
        contentStream.append("ET\n");

        String pdf = "%PDF-1.4\n" +
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << >> >>\nendobj\n" +
                "4 0 obj\n<< /Length " + contentStream.length() + " >>\nstream\n" +
                contentStream.toString() +
                "endstream\nendobj\nxref\n0 5\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\n0000000212 00000 n\ntrailer\n<< /Size 5 >>\nstartxref\n313\n%%EOF";

        return pdf.getBytes(StandardCharsets.US_ASCII);
    }

    // ── 1. EXPORT PAYSLIPS (ZIP) ─────────────────────────────────────────────
    @GetMapping("/my-payslips/export")
    public ResponseEntity<?> exportPayslips(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "payslip.self.download")) return forbiddenResponse("payslip.self.download");

        Employee emp = resolveEmployee(currentUser);
        if (emp == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Employee profile not found", "EMP_002"));
        }

        try {
            List<Payslip> payslips = payslipRepository.findByPayrollEmployeeId(emp.getId());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            if (payslips.isEmpty()) {
                zos.putNextEntry(new ZipEntry("README.txt"));
                zos.write("No payslips found for export.".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            } else {
                for (Payslip p : payslips) {
                    byte[] pdfBytes = myPayslipService.getPayslipPdf(currentUser.getWorkEmail(), p.getId());
                    String name = "payslip-" + p.getPayslipNumber() + ".pdf";
                    zos.putNextEntry(new ZipEntry(name));
                    zos.write(pdfBytes);
                    zos.closeEntry();
                }
            }
            zos.close();

            byte[] zipBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/zip"));
            headers.setContentDispositionFormData("attachment", "payslips-export.zip");
            headers.setContentLength(zipBytes.length);

            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to generate payslips ZIP: " + e.getMessage(), "EXP_500"));
        }
    }

    // ── 2. EXPORT ATTENDANCE (CSV) ───────────────────────────────────────────
    @GetMapping("/my-attendance/export")
    public ResponseEntity<?> exportAttendance(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        Employee emp = resolveEmployee(currentUser);
        if (emp == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Employee profile not found", "EMP_002"));
        }

        try {
            List<Attendance> list = attendanceService.getAttendanceByEmployeeId(emp.getId());
            StringBuilder csv = new StringBuilder("ID,Date,Status,Punch In,Punch Out,Notes\n");
            for (Attendance a : list) {
                csv.append(a.getId()).append(",")
                   .append(a.getDate()).append(",")
                   .append(a.getStatus()).append(",")
                   .append(a.getPunchInTime() != null ? a.getPunchInTime() : "").append(",")
                   .append(a.getPunchOutTime() != null ? a.getPunchOutTime() : "").append(",")
                   .append(a.getNotes() != null ? a.getNotes().replace(",", " ") : "").append("\n");
            }

            byte[] data = csv.toString().getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "attendance-export.csv");
            headers.setContentLength(data.length);

            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to export attendance CSV: " + e.getMessage(), "EXP_500"));
        }
    }

    // ── 3. EXPORT LEAVES (CSV) ───────────────────────────────────────────────
    @GetMapping("/my-leaves/export")
    public ResponseEntity<?> exportLeaves(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        Employee emp = resolveEmployee(currentUser);
        if (emp == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Employee profile not found", "EMP_002"));
        }

        try {
            List<Leave> leaves = leaveRepository.findByEmployeeId(emp.getId());
            StringBuilder csv = new StringBuilder("ID,Leave Type,Start Date,End Date,Reason,Status,Approved By,Applied At\n");
            for (Leave l : leaves) {
                csv.append(l.getId()).append(",")
                   .append(l.getLeaveType() != null ? l.getLeaveType().getName() : "").append(",")
                   .append(l.getStartDate()).append(",")
                   .append(l.getEndDate()).append(",")
                   .append(l.getReason() != null ? l.getReason().replace(",", " ") : "").append(",")
                   .append(l.getStatus()).append(",")
                   .append(l.getApprovedBy() != null ? l.getApprovedBy().getFullName() : "").append(",")
                   .append(l.getAppliedAt()).append("\n");
            }

            byte[] data = csv.toString().getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "leaves-export.csv");
            headers.setContentLength(data.length);

            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to export leaves CSV: " + e.getMessage(), "EXP_500"));
        }
    }

    // ── 4. EXPORT EXPENSES (CSV) ──────────────────────────────────────────────
    @GetMapping("/my-expenses/export")
    public ResponseEntity<?> exportExpenses(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "expense.self.read")) return forbiddenResponse("expense.self.read");

        Employee emp = resolveEmployee(currentUser);
        if (emp == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Employee profile not found", "EMP_002"));
        }

        try {
            List<Expense> expenses = expenseRepository.findByEmployeeId(emp.getId());
            StringBuilder csv = new StringBuilder("Expense Number,Title,Category,Amount,Currency,Expense Date,Status,Reimbursement Status\n");
            for (Expense e : expenses) {
                csv.append(e.getExpenseNumber() != null ? e.getExpenseNumber() : e.getId()).append(",")
                   .append(e.getTitle() != null ? e.getTitle().replace(",", " ") : "").append(",")
                   .append(e.getCategory() != null ? e.getCategory().getName() : "").append(",")
                   .append(e.getAmount()).append(",")
                   .append(e.getCurrency()).append(",")
                   .append(e.getExpenseDate()).append(",")
                   .append(e.getStatus()).append(",")
                   .append(e.getReimbursementStatus()).append("\n");
            }

            byte[] data = csv.toString().getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "expenses-export.csv");
            headers.setContentLength(data.length);

            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to export expenses CSV: " + e.getMessage(), "EXP_500"));
        }
    }

    // ── 5. EXPORT PERFORMANCE (PDF) ──────────────────────────────────────────
    @GetMapping("/my-performance/export")
    public ResponseEntity<?> exportPerformance(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "performance.self.read")) return forbiddenResponse("performance.self.read");

        Employee emp = resolveEmployee(currentUser);
        if (emp == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Employee profile not found", "EMP_002"));
        }

        try {
            List<Goal> goals = goalRepository.findByEmployeeId(emp.getId());
            List<PerformanceReview> reviews = reviewRepository.findByEmployeeId(emp.getId());

            List<String> lines = new ArrayList<>();
            lines.add("Employee: " + emp.getFullName() + " (" + emp.getEmployeeId() + ")");
            lines.add("Department: " + emp.getDepartment() + " | Designation: " + emp.getDesignation());
            lines.add("");
            lines.add("--- GOALS ---");
            if (goals.isEmpty()) {
                lines.add("No goals found.");
            } else {
                for (Goal g : goals) {
                    lines.add(String.format("[%s] %s (Type: %s, Progress: %d%%, Status: %s)",
                            g.getGoalCode(), g.getTitle(), g.getGoalType(), g.getProgress(), g.getStatus()));
                }
            }
            lines.add("");
            lines.add("--- PERFORMANCE REVIEWS ---");
            if (reviews.isEmpty()) {
                lines.add("No reviews found.");
            } else {
                for (PerformanceReview r : reviews) {
                    lines.add(String.format("Cycle: %s | Type: %s | Rating: %s | Status: %s",
                            r.getCycle() != null ? r.getCycle().getName() : "N/A",
                            r.getReviewType(),
                            r.getRating() != null ? r.getRating().toString() : "N/A",
                            r.getStatus()));
                }
            }

            byte[] pdfBytes = generatePdf("Performance Goals & Reviews Report", lines);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "performance-export.pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to generate performance PDF: " + e.getMessage(), "EXP_500"));
        }
    }

    // ── 6. EXPORT DOCUMENTS (ZIP) ────────────────────────────────────────────
    @GetMapping("/my-documents/export")
    public ResponseEntity<?> exportDocuments(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "document.self.download")) return forbiddenResponse("document.self.download");

        Employee emp = resolveEmployee(currentUser);
        if (emp == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Employee profile not found", "EMP_002"));
        }

        try {
            List<MyEmployeeDocument> docs = employeeDocumentRepository.findByEmployeeId(emp.getId());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            if (docs.isEmpty()) {
                zos.putNextEntry(new ZipEntry("README.txt"));
                zos.write("No documents uploaded.".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            } else {
                for (MyEmployeeDocument d : docs) {
                    if (d.getFileData() != null) {
                        zos.putNextEntry(new ZipEntry(d.getFileName()));
                        zos.write(d.getFileData());
                        zos.closeEntry();
                    }
                }
            }
            zos.close();

            byte[] zipBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/zip"));
            headers.setContentDispositionFormData("attachment", "documents-export.zip");
            headers.setContentLength(zipBytes.length);

            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to package documents ZIP: " + e.getMessage(), "EXP_500"));
        }
    }

    // ── 7. EXPORT TRAININGS (PDF) ────────────────────────────────────────────
    @GetMapping("/my-trainings/export")
    public ResponseEntity<?> exportTrainings(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        Employee emp = resolveEmployee(currentUser);
        if (emp == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Employee profile not found", "EMP_002"));
        }

        try {
            List<TrainingEnrollment> enrollments = trainingEnrollmentRepository.findByEmployeeId(emp.getId());

            List<String> lines = new ArrayList<>();
            lines.add("Employee: " + emp.getFullName() + " (" + emp.getEmployeeId() + ")");
            lines.add("Department: " + emp.getDepartment());
            lines.add("");
            lines.add("--- TRAINING COURSE HISTORY ---");
            if (enrollments.isEmpty()) {
                lines.add("No training modules found.");
            } else {
                for (TrainingEnrollment en : enrollments) {
                    String title = en.getSession() != null && en.getSession().getCourse() != null
                            ? en.getSession().getCourse().getTitle() : "Unknown Course";
                    String trainer = en.getSession() != null ? en.getSession().getTrainerName() : "N/A";
                    String dateStr = en.getSession() != null && en.getSession().getScheduleDate() != null
                            ? en.getSession().getScheduleDate().toString() : "N/A";
                    lines.add(String.format("Course: %s", title));
                    lines.add(String.format("  Trainer: %s | Date: %s", trainer, dateStr));
                    lines.add(String.format("  Status: %s | Progress: %d%% | Grade: %s",
                            en.getStatus(), en.getProgressPercent(), en.getGrade() != null ? en.getGrade() : "N/A"));
                    lines.add("");
                }
            }

            byte[] pdfBytes = generatePdf("Training Records Summary Report", lines);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "trainings-export.pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to generate trainings PDF: " + e.getMessage(), "EXP_500"));
        }
    }

    // ── 8. REQUEST SETTINGS DATA EXPORT ──────────────────────────────────────
    @PostMapping("/my-settings/data/export")
    public ResponseEntity<?> exportData(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.data.export")) return forbiddenResponse("settings.data.export");

        try {
            Map<String, Object> response = mySettingsService.exportData(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Data export request initiated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // ── 9. GET SETTINGS EXPORT STATUS ────────────────────────────────────────
    @GetMapping("/my-settings/data/export/{requestId}")
    public ResponseEntity<?> getExportStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("requestId") String requestId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.data.export")) return forbiddenResponse("settings.data.export");

        try {
            Map<String, Object> response = mySettingsService.getExportStatus(currentUser.getWorkEmail(), requestId);
            return ResponseEntity.ok(ApiResponse.success("Export status retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // ── 10. DOWNLOAD SETTINGS EXPORTED DATA (CSV) ────────────────────────────
    @GetMapping("/my-settings/data/export/{requestId}/download")
    public ResponseEntity<?> downloadExportedCsv(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("requestId") String requestId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.data.export")) return forbiddenResponse("settings.data.export");

        try {
            byte[] csvBytes = mySettingsService.getExportedDataCsv(currentUser.getWorkEmail(), requestId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "personal-data-" + requestId + ".csv");
            headers.setContentLength(csvBytes.length);
            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }
}
