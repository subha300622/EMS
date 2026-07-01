package com.example.ems.auth.controller;

import com.example.ems.auth.dto.ProfileUpdateRequest;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.dto.EmployeeProfileResponse;
import com.example.ems.employee.dto.HrmsProfileResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.entity.Offboarding;
import com.example.ems.offboarding.repository.OffboardingRepository;
import com.example.ems.offboarding.repository.ExitKtTaskRepository;
import com.example.ems.security.service.JwtService;
import com.example.ems.auth.dto.MyDashboardResponse;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.asset.repository.MyAssetRepository;
import com.example.ems.performance.repository.PerformanceReviewRepository;
import com.example.ems.support.repository.MySupportTicketRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.common.repository.NotificationRepository;
import com.example.ems.employee.repository.AnnouncementRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/me")
@CrossOrigin("*")
@Tag(name = "Employee Self Service - Profile")
public class MeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private MyAssetRepository assetRepository;

    @Autowired
    private PerformanceReviewRepository reviewRepository;

    @Autowired
    private MySupportTicketRepository supportTicketRepository;

    @Autowired
    private OffboardingRepository offboardingRepository;

    @Autowired
    private ExitKtTaskRepository exitKtTaskRepository;

    @Operation(summary = "Get My Profile", description = "Retrieves the full HRMS profile of the currently authenticated user.")
    @GetMapping("/profile")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.profile.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.profile.read' permission.", "AUTH_002"));
        }

        Employee employee = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        HrmsProfileResponse profileResponse = buildHrmsProfileResponse(employee, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profileResponse));
    }

    @Operation(summary = "Update My Profile",
            description = "Updates editable self-service fields: phone, address, emergencyContact, and profileImage. " +
                    "All fields are optional — only supplied (non-null) fields are updated. " +
                    "Admin-only fields (fullName, department, designation, salary) are not accepted.")
    @PutMapping("/profile")
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ProfileUpdateRequest body){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.profile.update")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.profile.update' permission.", "AUTH_002"));
        }

        Employee employee = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        if (!body.hasAnyUpdate()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("No editable fields provided. Accepted fields: phone, address, emergencyContact, profileImage", "VAL_004"));
        }

        // Apply only supplied (non-null) fields
        if (body.getPhone() != null) {
            employee.setPhone(body.getPhone());
        }
        if (body.getAddress() != null) {
            employee.setAddress(body.getAddress());
        }
        if (body.getEmergencyContact() != null) {
            employee.setEmergencyContact(body.getEmergencyContact());
        }
        if (body.getProfileImage() != null) {
            employee.setProfileImage(body.getProfileImage());
        }

        Employee saved = employeeRepository.save(employee);
        roleService.evictUserPermissionsCache(currentUser.getUserId());

        EmployeeProfileResponse profileResponse = buildProfileResponse(saved);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profileResponse));
    }

    @Operation(summary = "Get My Dashboard Stats", description = "Retrieves active counts of pending leaves, pending expenses, assigned assets, pending reviews, and open tickets.")
    @GetMapping("/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<MyDashboardResponse>> getMyDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.dashboard.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.dashboard.read' permission.", "AUTH_002"));
        }

        Employee employee = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        // 1. Attendance Data
        java.util.Optional<com.example.ems.attendance.entity.Attendance> optAttendance =
                attendanceRepository.findByEmployeeIdAndDate(employee.getId(), java.time.LocalDate.now());

        String todayStatus = "Absent";
        String checkIn = null;
        String checkOut = null;
        String workingHours = null;

        if (optAttendance.isPresent()) {
            com.example.ems.attendance.entity.Attendance att = optAttendance.get();
            String rawStatus = att.getStatus();
            if (rawStatus != null && !rawStatus.isBlank()) {
                todayStatus = rawStatus.substring(0, 1).toUpperCase() + rawStatus.substring(1).toLowerCase();
            } else {
                todayStatus = "Present";
            }

            java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            if (att.getPunchInTime() != null) {
                checkIn = att.getPunchInTime().format(timeFormatter);
                java.time.LocalTime end = att.getPunchOutTime();
                if (end != null) {
                    checkOut = end.format(timeFormatter);
                }
                
                java.time.LocalTime start = att.getPunchInTime();
                java.time.LocalTime effectiveEnd = (end != null) ? end : java.time.LocalTime.now();
                if (!effectiveEnd.isBefore(start)) {
                    java.time.Duration duration = java.time.Duration.between(start, effectiveEnd);
                    long hrs = duration.toHours();
                    long mins = duration.toMinutesPart();
                    workingHours = String.format("%02d:%02d", hrs, mins);
                } else {
                    workingHours = "00:00";
                }
            }
        }
        MyDashboardResponse.AttendanceData attendanceData = new MyDashboardResponse.AttendanceData(
                todayStatus, checkIn, checkOut, workingHours
        );

        // 2. Leave Data
        long pendingLeavesCount = leaveRepository.findByEmployeeIdAndStatus(employee.getId(), "PENDING").size();
        long remainingLeaves = 0;
        try {
            Map<String, Object> balances = leaveService.getLeaveBalance(employee.getId());
            for (Object detailsObj : balances.values()) {
                if (detailsObj instanceof Map) {
                    Map<?, ?> details = (Map<?, ?>) detailsObj;
                    Object rem = details.get("remaining");
                    if (rem instanceof Number) {
                        remainingLeaves += ((Number) rem).longValue();
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to a default
        }
        MyDashboardResponse.LeaveData leaveData = new MyDashboardResponse.LeaveData(
                pendingLeavesCount, remainingLeaves
        );

        // 3. Expenses Data
        long pendingExpensesCount = expenseRepository.findByEmployeeId(employee.getId()).stream()
                .filter(e -> {
                    String status = e.getStatus();
                    return "PENDING".equals(status) || "SUBMITTED".equals(status)
                            || "PENDING_MANAGER_APPROVAL".equals(status) || "PENDING_FINANCE_APPROVAL".equals(status);
                })
                .count();
        MyDashboardResponse.ExpenseData expenseData = new MyDashboardResponse.ExpenseData(pendingExpensesCount);

        // 4. Assets Data
        long assignedAssetsCount = assetRepository.findByAssignedToId(employee.getId()).size();
        MyDashboardResponse.AssetData assetData = new MyDashboardResponse.AssetData(assignedAssetsCount);

        // 5. Notifications Data
        long unreadNotifications = 0;
        try {
            unreadNotifications = notificationRepository.countByUserIdAndIsReadFalse(currentUser.getId());
        } catch (Exception e) {
            // Fallback
        }
        MyDashboardResponse.NotificationData notificationData = new MyDashboardResponse.NotificationData(unreadNotifications);

        // 6. Announcements Data
        long unreadAnnouncements = 0;
        try {
            unreadAnnouncements = announcementRepository.findByActiveTrueOrderByPublishedDateDesc().size();
        } catch (Exception e) {
            // Fallback
        }
        MyDashboardResponse.AnnouncementData announcementData = new MyDashboardResponse.AnnouncementData(unreadAnnouncements);

        // 7. Performance Data
        long pendingReviewsCount = reviewRepository.findByEmployeeId(employee.getId()).stream()
                .filter(r -> !"FINALIZED".equalsIgnoreCase(r.getStatus()))
                .count();
        MyDashboardResponse.PerformanceData performanceData = new MyDashboardResponse.PerformanceData(pendingReviewsCount);

        // 8. Support Data
        long openTicketsCount = supportTicketRepository.findByEmployeeEmail(employee.getEmail()).stream()
                .filter(t -> "OPEN".equalsIgnoreCase(t.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(t.getStatus()))
                .count();
        MyDashboardResponse.SupportData supportData = new MyDashboardResponse.SupportData(openTicketsCount);

        // 9. Profile Data
        int completionPercentage = calculateProfileCompletion(employee);
        MyDashboardResponse.ProfileData profileData = new MyDashboardResponse.ProfileData(completionPercentage);

        MyDashboardResponse dashboardResponse = new MyDashboardResponse(
                attendanceData,
                leaveData,
                expenseData,
                assetData,
                notificationData,
                announcementData,
                performanceData,
                supportData,
                profileData
        );

        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", dashboardResponse));
    }

    /**
     * Builds a production-grade HRMS profile response.
     * Covers: employee info, org hierarchy, contact, personalInfo,
     * skills, systemAccess, exitSummary, and audit.
     */
    private HrmsProfileResponse buildHrmsProfileResponse(Employee employee, User currentUser) {
        HrmsProfileResponse response = new HrmsProfileResponse();

        // ── employee section ──────────────────────────────────────────
        HrmsProfileResponse.EmployeeSection emp = new HrmsProfileResponse.EmployeeSection();
        emp.setId(employee.getId());
        emp.setCode(employee.getEmployeeId());
        emp.setFullName(employee.getFullName());
        emp.setDesignation(employee.getDesignation());
        emp.setDepartment(employee.getDepartment());
        emp.setEmploymentStatus(employee.getStatus());
        emp.setEmploymentType(employee.getEmploymentType());
        emp.setWorkMode(employee.getWorkMode() != null ? employee.getWorkMode() : "OFFICE");
        emp.setLocation(employee.getLocation());
        emp.setJoiningDate(employee.getJoiningDate() != null ? employee.getJoiningDate().toString() : null);
        emp.setProbationStatus("CONFIRMED");
        String profileImage = employee.getProfileImage();
        if (profileImage == null || profileImage.isBlank()) {
            try {
                profileImage = "https://api.dicebear.com/7.x/initials/svg?seed="
                        + java.net.URLEncoder.encode(employee.getFullName(), "UTF-8");
            } catch (Exception e) {
                profileImage = "https://api.dicebear.com/7.x/initials/svg?seed=" + employee.getFullName();
            }
        }
        emp.setProfileImage(profileImage);
        response.setEmployee(emp);

        // ── organization section ──────────────────────────────────────
        HrmsProfileResponse.OrganizationSection org = new HrmsProfileResponse.OrganizationSection();
        if (employee.getManager() != null) {
            Employee mgr = employee.getManager();
            org.setManager(new HrmsProfileResponse.ManagerDto(
                    mgr.getId(),
                    mgr.getFullName(),
                    mgr.getDesignation(),
                    mgr.getEmail()));
        } else {
            org.setManager(new HrmsProfileResponse.ManagerDto(null, null, null, null));
        }
        org.setReportingChain(Collections.emptyList());
        try {
            int teamSize = (int) employeeRepository.countByManagerId(employee.getId());
            org.setTeamSize(teamSize);
        } catch (Exception e) {
            org.setTeamSize(0);
        }
        response.setOrganization(org);

        // ── contact section ───────────────────────────────────────────
        HrmsProfileResponse.ContactSection contact = new HrmsProfileResponse.ContactSection();
        contact.setEmail(employee.getEmail());
        contact.setPhone(employee.getPhone());
        String rawEmergency = employee.getEmergencyContact();
        HrmsProfileResponse.EmergencyContactDto ecDto = parseEmergencyContact(rawEmergency);
        contact.setEmergencyContact(ecDto);
        response.setContact(contact);

        // ── personalInfo section ──────────────────────────────────────
        HrmsProfileResponse.PersonalInfoSection personal = new HrmsProfileResponse.PersonalInfoSection();
        personal.setGender(employee.getGender());
        personal.setDateOfBirth(employee.getDob() != null ? employee.getDob().toString() : null);
        personal.setAddress(parseAddress(employee.getAddress()));
        response.setPersonalInfo(personal);

        // ── skills section ────────────────────────────────────────────
        HrmsProfileResponse.SkillsSection skillsSection = new HrmsProfileResponse.SkillsSection();
        skillsSection.setPrimary(Collections.emptyList());
        skillsSection.setSecondary(Collections.emptyList());
        skillsSection.setCertifications(Collections.emptyList());
        response.setSkills(skillsSection);

        // ── systemAccess section ──────────────────────────────────────
        HrmsProfileResponse.SystemAccessSection access = new HrmsProfileResponse.SystemAccessSection();
        String roleName = (currentUser.getRole() != null) ? currentUser.getRole().getName() : "EMPLOYEE";
        access.setRoles(Collections.singletonList(roleName));
        try {
            List<String> perms = roleService.getPermissionsForUserId(currentUser.getUserId());
            access.setPermissions(perms != null ? perms : Collections.emptyList());
        } catch (Exception e) {
            access.setPermissions(Collections.emptyList());
        }
        access.setLastLogin(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        response.setSystemAccess(access);

        // ── exitSummary section ───────────────────────────────────────
        HrmsProfileResponse.ExitSummarySection exitSection = new HrmsProfileResponse.ExitSummarySection();
        try {
            Optional<Offboarding> offboarding = offboardingRepository.findByEmployeeId(employee.getId());
            if (offboarding.isPresent()) {
                Offboarding ob = offboarding.get();
                exitSection.setExitInitiated(true);
                exitSection.setExitStatus(ob.getStatus());
                exitSection.setLastWorkingDay(
                        ob.getRequestedLastWorkingDay() != null ? ob.getRequestedLastWorkingDay().toString() : null);
                try {
                    long pendingKt = exitKtTaskRepository.countPendingByOffboardingId(employee.getId());
                    exitSection.setPendingKTTasks((int) pendingKt);
                } catch (Exception e) {
                    exitSection.setPendingKTTasks(0);
                }
                exitSection.setClearanceStatus(ob.getCurrentStage() != null ? ob.getCurrentStage() : "IN_PROGRESS");
            } else {
                exitSection.setExitInitiated(false);
                exitSection.setExitStatus(null);
                exitSection.setLastWorkingDay(null);
                exitSection.setPendingKTTasks(0);
                exitSection.setClearanceStatus("NOT_STARTED");
            }
        } catch (Exception e) {
            exitSection.setExitInitiated(false);
            exitSection.setClearanceStatus("NOT_STARTED");
        }
        response.setExitSummary(exitSection);

        // ── audit section ─────────────────────────────────────────────
        String createdAt = currentUser.getCreatedAt() != null ? currentUser.getCreatedAt().toString() : null;
        String updatedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        response.setAudit(new HrmsProfileResponse.AuditSection(createdAt, updatedAt));

        return response;
    }

    /**
     * Parses raw emergency contact string (e.g. "Jane Doe - 9876543211")
     * into a structured EmergencyContactDto.
     */
    private HrmsProfileResponse.EmergencyContactDto parseEmergencyContact(String raw) {
        if (raw == null || raw.isBlank()) {
            return new HrmsProfileResponse.EmergencyContactDto(null, null, "UNKNOWN");
        }
        // Try pattern: "Name - Phone" or "Name - Phone - Relation"
        String[] parts = raw.split("-");
        String name = parts[0].trim();
        String phone = parts.length > 1 ? parts[1].trim() : null;
        String relation = parts.length > 2 ? parts[2].trim() : "UNKNOWN";
        return new HrmsProfileResponse.EmergencyContactDto(name, phone, relation);
    }

    /**
     * Parses a raw address string into a structured AddressDto.
     * Falls back gracefully if the address doesn't match expected patterns.
     */
    private HrmsProfileResponse.AddressDto parseAddress(String raw) {
        if (raw == null || raw.isBlank()) {
            return new HrmsProfileResponse.AddressDto(null, null, null, null, "India");
        }
        // Try to split on comma: "Line1, City StatePostal"
        String[] parts = raw.split(",");
        String line1 = parts[0].trim();
        String city = null;
        String state = null;
        String postalCode = null;
        if (parts.length > 1) {
            // e.g. "Bangalore 560001" or "Bangalore, Karnataka 560001"
            String rest = parts[parts.length - 1].trim();
            String[] tokens = rest.split(" ");
            // Last token could be postal code if numeric
            if (tokens.length > 1 && tokens[tokens.length - 1].matches("\\d{4,6}")) {
                postalCode = tokens[tokens.length - 1];
                city = tokens[0];
            } else {
                city = rest;
            }
        }
        return new HrmsProfileResponse.AddressDto(line1, city, state, postalCode, "India");
    }

    /**
     * Builds the legacy flat EmployeeProfileResponse (used by PUT /profile update response).
     */
    private EmployeeProfileResponse buildProfileResponse(Employee employee) {
        EmployeeProfileResponse.ManagerProfileDto managerDto = null;
        if (employee.getManager() != null) {
            managerDto = new EmployeeProfileResponse.ManagerProfileDto(
                    employee.getManager().getId(),
                    employee.getManager().getFullName());
        }
        EmployeeProfileResponse.ContactProfileDto contactDto = new EmployeeProfileResponse.ContactProfileDto(
                employee.getEmail(), employee.getPhone(), employee.getEmergencyContact());
        EmployeeProfileResponse.PersonalInfoDto personalInfoDto = new EmployeeProfileResponse.PersonalInfoDto(
                employee.getGender(),
                employee.getDob() != null ? employee.getDob().toString() : null,
                employee.getAddress());
        EmployeeProfileResponse.WorkInformationDto workDto = new EmployeeProfileResponse.WorkInformationDto(
                employee.getLocation(), employee.getWorkMode(),
                employee.getJoiningDate() != null ? employee.getJoiningDate().toString() : null,
                employee.getEmploymentType(), employee.getStatus());
        EmployeeProfileResponse resp = new EmployeeProfileResponse();
        resp.setEmployeeId(employee.getId());
        resp.setEmployeeCode(employee.getEmployeeId());
        resp.setFullName(employee.getFullName());
        resp.setProfileImage(employee.getProfileImage());
        resp.setDesignation(employee.getDesignation());
        resp.setDepartment(employee.getDepartment());
        resp.setManager(managerDto);
        resp.setContact(contactDto);
        resp.setPersonalInfo(personalInfoDto);
        resp.setWorkInformation(workDto);
        return resp;
    }

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

    private int calculateProfileCompletion(Employee employee) {
        if (employee == null) return 0;
        int filledFields = 0;
        int totalFields = 10;
        
        if (employee.getFullName() != null && !employee.getFullName().isBlank()) filledFields++;
        if (employee.getPhone() != null && !employee.getPhone().isBlank()) filledFields++;
        if (employee.getGender() != null && !employee.getGender().isBlank()) filledFields++;
        if (employee.getDob() != null) filledFields++;
        if (employee.getAddress() != null && !employee.getAddress().isBlank()) filledFields++;
        if (employee.getEmergencyContact() != null && !employee.getEmergencyContact().isBlank()) filledFields++;
        if (employee.getDepartment() != null && !employee.getDepartment().isBlank()) filledFields++;
        if (employee.getDesignation() != null && !employee.getDesignation().isBlank()) filledFields++;
        if (employee.getJoiningDate() != null) filledFields++;
        if (employee.getProfileImage() != null && !employee.getProfileImage().isBlank()) filledFields++;
        
        return (filledFields * 100) / totalFields;
    }
}
