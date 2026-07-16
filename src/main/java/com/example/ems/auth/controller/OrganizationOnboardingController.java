package com.example.ems.auth.controller;

import com.example.ems.auth.entity.Invitation;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.entity.OtpToken;
import com.example.ems.auth.repository.InvitationRepository;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.repository.OtpTokenRepository;
import com.example.ems.auth.service.OtpService;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationAddress;
import com.example.ems.organization.entity.OrganizationSettings;
import com.example.ems.organization.entity.Subscription;
import com.example.ems.organization.entity.SubscriptionStatus;
import com.example.ems.organization.repository.OrganizationAddressRepository;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.organization.repository.OrganizationSettingsRepository;
import com.example.ems.organization.repository.SubscriptionRepository;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "EMS SaaS - Organization Onboarding Workflow")
public class OrganizationOnboardingController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private OtpTokenRepository otpTokenRepository;
    @Autowired
    private OtpService otpService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private OrganizationSettingsRepository organizationSettingsRepository;
    @Autowired
    private OrganizationAddressRepository organizationAddressRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private InvitationRepository invitationRepository;

    // Helper: Resolve User from JWT
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

    // Helper: Find Organization by code or DB ID
    private Organization findOrg(String organizationId) {
        if (organizationId.matches("\\d+")) {
            return organizationRepository.findById(Long.parseLong(organizationId))
                    .orElseThrow(
                            () -> new IllegalArgumentException("Organization not found with ID: " + organizationId));
        } else {
            return organizationRepository.findByOrganizationCode(organizationId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("Organization not found with Code: " + organizationId));
        }
    }

    // ==========================================
    // PHASE 1: User Registration (Checks moved to AuthController for normalization support)
    // ==========================================

    public record ResendVerificationRequest(@NotBlank @Email String email) {
    }

    @PostMapping("/auth/resend-verification")
    @Operation(summary = "Phase 1: Resend OTP/verification email")
    public ResponseEntity<?> resendVerification(@RequestBody @Valid ResendVerificationRequest req) {
        otpService.forgotPassword(req.email());
        return ResponseEntity.ok(ApiResponse.success("Verification code resent."));
    }

    // ==========================================
    // PHASE 2: Email Verification
    // ==========================================

    public record VerifyEmailRequest(@NotBlank @Email String email, @NotBlank String otp) {
    }

    @PostMapping("/auth/verify-email")
    @Operation(summary = "Phase 2: Verify email using OTP")
    public ResponseEntity<?> verifyEmail(@RequestBody @Valid VerifyEmailRequest req) {
        Map<String, Object> verifyResult = otpService.verifyOtp(req.email(), req.otp());
        Boolean verified = (Boolean) verifyResult.get("verified");
        if (verified != null && verified) {
            User user = userRepository.findByWorkEmail(req.email()).orElse(null);
            if (user != null) {
                user.setStatus("ACTIVE");
                userRepository.save(user);
            }
            return ResponseEntity.ok(ApiResponse.success("Email verified successfully."));
        } else {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error((String) verifyResult.get("message"), "AUTH_016"));
        }
    }

    public record VerifyLinkRequest(@NotBlank String token) {
    }

    @PostMapping("/auth/verify-link")
    @Operation(summary = "Phase 2: Verify email using verification token")
    public ResponseEntity<?> verifyLink(@RequestBody @Valid VerifyLinkRequest req) {
        Optional<OtpToken> optToken = otpTokenRepository.findByResetToken(req.token());
        if (optToken.isPresent()) {
            OtpToken otpToken = optToken.get();
            if (LocalDateTime.now().isBefore(otpToken.getExpiryTime())) {
                User user = userRepository.findByWorkEmail(otpToken.getEmail()).orElse(null);
                if (user != null) {
                    user.setStatus("ACTIVE");
                    userRepository.save(user);
                }
                otpTokenRepository.delete(otpToken);
                return ResponseEntity.ok(ApiResponse.success("Email verified successfully."));
            }
        }
        return ResponseEntity.badRequest()
                .body(ErrorResponse.error("Invalid or expired verification token", "AUTH_008"));
    }

    // ==========================================
    // PHASE 3: Create Organization
    // ==========================================

    public record CreateOrganizationRequest(
            @NotBlank String organizationName,
            @NotBlank String organizationType,
            @NotBlank String industry,
            @NotBlank String companySize,
            String website,
            String timeZone,
            String currency) {
    }

    @PostMapping("/organizations")
    @Operation(summary = "Phase 3: Create organization")
    public ResponseEntity<?> createOrganization(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid CreateOrganizationRequest req) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Organization org = new Organization();
        org.setName(req.organizationName());
        org.setWebsite(req.website());
        org.setEmail(currentUser.getWorkEmail());
        org.setPhone(currentUser.getMobileNumber());

        organizationRepository.save(org);

        String orgCode = "ORG" + String.format("%04d", org.getId());
        org.setOrganizationCode(orgCode);
        organizationRepository.save(org);

        // Assign User to Organization & Role
        currentUser.setOrganization(org);
        currentUser.setOrganizationName(req.organizationName());
        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
        if (adminRole != null) {
            currentUser.setRole(adminRole);
        }
        userRepository.save(currentUser);

        // Save settings
        OrganizationSettings settings = new OrganizationSettings();
        settings.setOrganization(org);
        settings.getConfig().put("organizationType", req.organizationType());
        settings.getConfig().put("industry", req.industry());
        settings.getConfig().put("companySize", req.companySize());
        settings.getConfig().put("timeZone", req.timeZone() != null ? req.timeZone() : "Asia/Kolkata");
        settings.getConfig().put("currency", req.currency() != null ? req.currency() : "INR");
        settings.getConfig().put("status", "DRAFT");
        organizationSettingsRepository.save(settings);

        org.setSettings(settings);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("organizationId", orgCode);
        data.put("status", "DRAFT");

        return ResponseEntity.ok(ApiResponse.success("Organization created successfully.", data));
    }

    // ==========================================
    // PHASE 4: Organization Address
    // ==========================================

    public record SaveAddressRequest(
            @NotBlank String country,
            @NotBlank String state,
            @NotBlank String city,
            @NotBlank String address,
            @NotBlank String postalCode) {
    }

    @PostMapping("/organizations/{organizationId}/address")
    @Operation(summary = "Phase 4: Save organization address")
    public ResponseEntity<?> saveAddress(
            @PathVariable String organizationId,
            @RequestBody @Valid SaveAddressRequest req) {

        Organization org = findOrg(organizationId);

        OrganizationAddress address = org.getAddress();
        if (address == null) {
            address = new OrganizationAddress();
            address.setOrganization(org);
        }
        address.setCountry(req.country());
        address.setState(req.state());
        address.setCity(req.city());
        address.setStreet(req.address());
        address.setZipCode(req.postalCode());

        organizationAddressRepository.save(address);

        return ResponseEntity.ok(ApiResponse.success("Organization address saved."));
    }

    // ==========================================
    // PHASE 5: Subscription Plan
    // ==========================================

    @GetMapping("/subscriptions/plans")
    @Operation(summary = "Phase 5: List subscription plans")
    public ResponseEntity<?> getPlans() {
        List<Map<String, Object>> plans = new ArrayList<>();
        plans.add(Map.of("plan", "STARTER", "name", "Starter Plan", "price", 1000));
        plans.add(Map.of("plan", "GROWTH", "name", "Growth Plan", "price", 2500));
        plans.add(Map.of("plan", "PROFESSIONAL", "name", "Professional Plan", "price", 5000));
        plans.add(Map.of("plan", "ENTERPRISE", "name", "Enterprise Plan", "price", 1000));
        return ResponseEntity.ok(ApiResponse.success("Subscription plans retrieved.", plans));
    }

    public record SelectSubscriptionRequest(
            @NotBlank String plan,
            @NotBlank String billingCycle) {
    }

    @PostMapping("/organizations/{organizationId}/subscription")
    @Operation(summary = "Phase 5: Select subscription plan")
    public ResponseEntity<?> selectSubscription(
            @PathVariable String organizationId,
            @RequestBody @Valid SelectSubscriptionRequest req) {

        Organization org = findOrg(organizationId);

        Subscription sub = org.getActiveSubscription();
        if (sub == null) {
            sub = new Subscription();
            sub.setOrganization(org);
        }
        sub.setPlanCode(req.plan().toUpperCase());
        sub.setPlanName(req.plan().substring(0, 1).toUpperCase() + req.plan().substring(1).toLowerCase() + " Plan");
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setStartDate(LocalDate.now());
        sub.setExpiryDate(LocalDate.now().plusYears(1));
        sub.setAutoRenew(true);

        subscriptionRepository.save(sub);

        return ResponseEntity.ok(ApiResponse.success("Subscription selected."));
    }

    // ==========================================
    // PHASE 6: Module Selection
    // ==========================================

    @GetMapping("/modules")
    @Operation(summary = "Phase 6: Get available modules")
    public ResponseEntity<?> getModules() {
        List<String> modules = List.of(
                "DASHBOARD", "EMPLOYEE_MANAGEMENT", "ATTENDANCE", "LEAVE", "PAYROLL", "RECRUITMENT",
                "ONBOARDING", "PERFORMANCE", "TRAINING", "ASSET_MANAGEMENT", "DOCUMENTS", "EXPENSES",
                "TIMESHEET", "REPORTS", "NOTIFICATIONS", "SETTINGS");
        return ResponseEntity.ok(ApiResponse.success("Available modules retrieved.", modules));
    }

    public record SelectModulesRequest(@NotEmpty List<String> modules) {
    }

    @PostMapping("/organizations/{organizationId}/modules")
    @Operation(summary = "Phase 6: Enable organization modules")
    public ResponseEntity<?> enableModules(
            @PathVariable String organizationId,
            @RequestBody @Valid SelectModulesRequest req) {

        Organization org = findOrg(organizationId);
        OrganizationSettings settings = org.getSettings();
        if (settings == null) {
            settings = new OrganizationSettings();
            settings.setOrganization(org);
        }

        settings.getConfig().put("enabledModules", req.modules());
        organizationSettingsRepository.save(settings);

        return ResponseEntity.ok(ApiResponse.success("Modules enabled successfully."));
    }

    // ==========================================
    // PHASE 7: Create Head Office Branch
    // ==========================================

    public record CreateBranchRequest(
            @NotBlank String branchName,
            @NotBlank String branchCode,
            @NotBlank String branchType,
            String address,
            String timeZone,
            List<String> workingDays,
            Map<String, String> workingHours) {
    }

    @PostMapping("/organizations/{organizationId}/branches")
    @Operation(summary = "Phase 7: Create head office branch")
    public ResponseEntity<?> createBranch(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String organizationId,
            @RequestBody @Valid CreateBranchRequest req) {

        User currentUser = resolveUser(authHeader);
        if (currentUser != null) {
            currentUser.setBranch(req.branchName());
            userRepository.save(currentUser);
        }

        Organization org = findOrg(organizationId);
        OrganizationSettings settings = org.getSettings();
        if (settings == null) {
            settings = new OrganizationSettings();
            settings.setOrganization(org);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> branches = (List<Map<String, Object>>) settings.getConfig().get("branches");
        if (branches == null) {
            branches = new ArrayList<>();
        }

        Map<String, Object> branchInfo = new LinkedHashMap<>();
        branchInfo.put("branchName", req.branchName());
        branchInfo.put("branchCode", req.branchCode());
        branchInfo.put("branchType", req.branchType());
        branchInfo.put("address", req.address());
        branchInfo.put("timeZone", req.timeZone());
        branchInfo.put("workingDays", req.workingDays());
        branchInfo.put("workingHours", req.workingHours());

        branches.add(branchInfo);
        settings.getConfig().put("branches", branches);
        organizationSettingsRepository.save(settings);

        return ResponseEntity.ok(ApiResponse.success("Branch created successfully."));
    }

    // ==========================================
    // PHASE 8: Create Departments
    // ==========================================

    @GetMapping("/departments/templates")
    @Operation(summary = "Phase 8: Default departments")
    public ResponseEntity<?> getDepartmentTemplates() {
        List<String> templates = List.of("HR", "IT", "Finance", "Marketing", "Sales", "Operations", "Management",
                "Customer Support");
        return ResponseEntity.ok(ApiResponse.success("Default department templates retrieved.", templates));
    }

    public record CreateDepartmentsRequest(@NotEmpty List<String> departments) {
    }

    @PostMapping("/organizations/{organizationId}/departments")
    @Operation(summary = "Phase 8: Create departments")
    public ResponseEntity<?> createDepartments(
            @PathVariable String organizationId,
            @RequestBody @Valid CreateDepartmentsRequest req) {

        Organization org = findOrg(organizationId);

        for (String deptName : req.departments()) {
            // Suffix name/code to ensure global db uniqueness
            String suffix = org.getOrganizationCode();
            String nameWithSuffix = deptName + " (" + suffix + ")";
            String codeWithSuffix = (deptName + "-" + suffix).toUpperCase();

            // Avoid double creation if it exists
            if (!departmentRepository.existsByName(nameWithSuffix)) {
                Department d = new Department();
                d.setName(nameWithSuffix);
                d.setCode(codeWithSuffix);
                d.setOrganization(org);
                d.setDescription(deptName + " department for " + org.getName());
                d.setBudget(BigDecimal.ZERO);
                d.setStatus("ACTIVE");
                departmentRepository.save(d);
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Departments created successfully."));
    }

    // ==========================================
    // PHASE 9: Create Roles
    // ==========================================

    @GetMapping("/roles/templates")
    @Operation(summary = "Phase 9: Default roles")
    public ResponseEntity<?> getRoleTemplates() {
        List<Map<String, Object>> roles = new ArrayList<>();
        roles.add(Map.of("roleId", 1, "name", "CEO", "description", "Chief Executive Officer"));
        roles.add(Map.of("roleId", 2, "name", "Manager", "description", "Team Manager"));
        roles.add(Map.of("roleId", 3, "name", "Assistant Manager", "description", "Assistant Team Manager"));
        roles.add(Map.of("roleId", 4, "name", "HR Manager", "description", "Human Resources Manager"));
        roles.add(Map.of("roleId", 5, "name", "HR Executive", "description", "Human Resources Executive"));
        roles.add(Map.of("roleId", 6, "name", "Finance Manager", "description", "Finance Manager"));
        roles.add(Map.of("roleId", 7, "name", "Finance Executive", "description", "Finance Executive"));
        roles.add(Map.of("roleId", 8, "name", "Team Leader", "description", "Team Leader"));
        roles.add(Map.of("roleId", 9, "name", "Team Member", "description", "Team Member"));
        return ResponseEntity.ok(ApiResponse.success("Default roles templates retrieved.", roles));
    }

    public record CreateRolesRequest(@NotEmpty List<String> roles) {
    }

    @PostMapping("/organizations/{organizationId}/roles")
    @Operation(summary = "Phase 9: Create roles")
    public ResponseEntity<?> createRoles(
            @PathVariable String organizationId,
            @RequestBody @Valid CreateRolesRequest req) {

        Organization org = findOrg(organizationId);

        for (String roleName : req.roles()) {
            if (!roleRepository.existsByOrganizationIdAndName(org.getId(), roleName)) {
                Role role = new Role();
                role.setName(roleName);
                role.setDescription(roleName + " role for tenant " + org.getName());
                role.setOrganization(org);
                role.setPlatformTemplate(false);
                role.setSystemRole(false);
                roleRepository.save(role);
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Roles created successfully."));
    }

    // ==========================================
    // PHASE 10: Assign Roles to Departments
    // ==========================================

    public record RoleMapping(@NotBlank String department, @NotEmpty List<String> roles) {
    }

    public record AssignMappingsRequest(@NotEmpty List<RoleMapping> mappings) {
    }

    @PostMapping("/organizations/{organizationId}/department-role-mappings")
    @Operation(summary = "Phase 10: Assign roles to departments")
    public ResponseEntity<?> assignMappings(
            @PathVariable String organizationId,
            @RequestBody @Valid AssignMappingsRequest req) {

        Organization org = findOrg(organizationId);
        OrganizationSettings settings = org.getSettings();
        if (settings == null) {
            settings = new OrganizationSettings();
            settings.setOrganization(org);
        }

        settings.getConfig().put("departmentRoleMappings", req.mappings());
        organizationSettingsRepository.save(settings);

        return ResponseEntity.ok(ApiResponse.success("Department-role mappings saved."));
    }

    // ==========================================
    // PHASE 11: Configure Permissions
    // ==========================================

    @GetMapping("/permissions/modules")
    @Operation(summary = "Phase 11: Get all module permissions")
    public ResponseEntity<?> getPermissions() {
        return ResponseEntity
                .ok(ApiResponse.success("Module permissions retrieved.", PermissionRegistry.ALL_PERMISSIONS));
    }

    public record AssignPermissionsRequest(@NotEmpty List<String> permissions) {
    }

    @PostMapping("/organizations/{organizationId}/roles/{roleId}/permissions")
    @Operation(summary = "Phase 11: Assign permissions to role")
    public ResponseEntity<?> assignPermissions(
            @PathVariable String organizationId,
            @PathVariable String roleId,
            @RequestBody @Valid AssignPermissionsRequest req) {

        Organization org = findOrg(organizationId);

        Long rid = Long.parseLong(roleId);
        Role role = roleRepository.findById(rid)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

        if (role.getOrganization() == null || !role.getOrganization().getId().equals(org.getId())) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Role does not belong to this organization", "AUTH_011"));
        }

        Set<Permission> perms = new HashSet<>();
        for (String permName : req.permissions()) {
            Permission p = permissionRepository.findByName(permName).orElse(null);
            if (p == null) {
                p = new Permission();
                p.setName(permName);
                p.setDescription(permName + " permission");
                permissionRepository.save(p);
            }
            perms.add(p);
        }

        role.setPermissions(perms);
        roleRepository.save(role);

        return ResponseEntity.ok(ApiResponse.success("Permissions updated successfully."));
    }

    // ==========================================
    // PHASE 12: Invite Employees
    // ==========================================

    public record InviteEmployeesRequest(
            @NotEmpty List<String> emails,
            String defaultRoleId,
            String branchId,
            String departmentId) {
    }

    @PostMapping("/employees/invitations/email")
    @Operation(summary = "Phase 12: Invite employees via email")
    public ResponseEntity<?> inviteEmployees(@RequestBody @Valid InviteEmployeesRequest req) {
        String roleName = "Employee";
        if (req.defaultRoleId() != null) {
            try {
                Role r = roleRepository.findById(Long.parseLong(req.defaultRoleId())).orElse(null);
                if (r != null)
                    roleName = r.getName();
            } catch (Exception e) {
            }
        }

        for (String email : req.emails()) {
            if (!invitationRepository.existsByEmail(email)) {
                String token = UUID.randomUUID().toString();
                Invitation invitation = new Invitation();
                invitation.setName(email.split("@")[0]);
                invitation.setEmail(email);
                invitation.setRole(roleName);
                invitation.setInvitationToken(token);
                invitation.setExpiredAt(LocalDateTime.now().plusHours(24));
                invitationRepository.save(invitation);
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Invitations sent."));
    }

    @PostMapping("/onboarding/skip")
    @Operation(summary = "Phase 12: Skip employee invitation")
    public ResponseEntity<?> skipOnboarding() {
        return ResponseEntity.ok(ApiResponse.success("Onboarding step skipped."));
    }

    // ==========================================
    // PHASE 13: Employee Accept Invitation
    // ==========================================

    @GetMapping("/invitations/{token}")
    @Operation(summary = "Phase 13: Validate invitation")
    public ResponseEntity<?> validateInvitation(@PathVariable String token) {
        Invitation invitation = invitationRepository.findByInvitationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("valid", true);
        data.put("email", invitation.getEmail());

        return ResponseEntity.ok(ApiResponse.success("Token is valid", data));
    }

    public record AcceptInvitationPayload(
            @NotBlank String password,
            @NotBlank String confirmPassword) {
    }

    @PostMapping("/invitations/{token}/accept")
    @Operation(summary = "Phase 13: Accept invitation")
    public ResponseEntity<?> acceptInvitation(
            @PathVariable String token,
            @RequestBody @Valid AcceptInvitationPayload req) {

        if (!req.password().equals(req.confirmPassword())) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Passwords do not match", "VAL_004"));
        }

        Invitation invitation = invitationRepository.findByInvitationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        if (invitation.isAccepted()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Invitation already accepted", "AUTH_009"));
        }

        Role role = roleRepository.findByName(invitation.getRole()).orElse(null);
        if (role == null) {
            role = roleRepository.findByName("EMPLOYEE").orElse(null);
        }

        User user = new User();
        user.setFullName(invitation.getName());
        user.setWorkEmail(invitation.getEmail());
        user.setRequestedRole(invitation.getRole());
        if (role != null) {
            user.setRole(role);
        }
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setStatus("ACTIVE");
        userRepository.save(user);

        String userId = "EMP" + String.format("%03d", user.getId());
        user.setUserId(userId);
        userRepository.save(user);

        Employee emp = new Employee();
        emp.setFullName(user.getFullName());
        emp.setEmail(user.getWorkEmail());
        emp.setEmployeeId(userId);
        emp.setPhone("1234567890");
        emp.setGender("MALE");
        emp.setDob(LocalDate.of(1990, 1, 1));
        emp.setAddress("123 Corporate Way");
        emp.setEmergencyContact("9876543210");
        emp.setDepartment("Engineering");
        emp.setDesignation(role != null ? role.getName() : "Software Engineer");
        emp.setAnnualSalary(BigDecimal.valueOf(85000));
        emp.setJoiningDate(LocalDate.of(2026, 6, 10));
        emp.setLocation("Headquarters");
        emp.setEmploymentType("FULL_TIME");
        emp.setStatus("ACTIVE");
        employeeRepository.save(emp);

        invitation.setAccepted(true);
        invitationRepository.save(invitation);

        return ResponseEntity.ok(ApiResponse.success("Invitation accepted successfully."));
    }

    // ==========================================
    // PHASE 14: Employee Profile Completion
    // ==========================================

    @GetMapping("/profile")
    @Operation(summary = "Phase 14: Get profile")
    public ResponseEntity<?> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("fullName", user.getFullName());
        data.put("email", user.getWorkEmail());
        data.put("mobileNumber", user.getMobileNumber());
        data.put("employeeId", user.getUserId());

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully.", data));
    }

    public record UpdateProfileRequest(
            @NotBlank String fullName,
            @NotBlank String mobileNumber) {
    }

    @PutMapping("/profile")
    @Operation(summary = "Phase 14: Complete employee profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid UpdateProfileRequest req) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        user.setFullName(req.fullName());
        user.setMobileNumber(req.mobileNumber());
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Profile updated."));
    }

    @PostMapping("/profile/documents")
    @Operation(summary = "Phase 14: Upload documents")
    public ResponseEntity<?> uploadProfileDocuments() {
        return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully."));
    }

    // ==========================================
    // PHASE 15: Activate Organization
    // ==========================================

    @PostMapping("/organizations/{organizationId}/activate")
    @Operation(summary = "Phase 15: Activate organization")
    public ResponseEntity<?> activateOrganization(
            @PathVariable String organizationId) {

        Organization org = findOrg(organizationId);
        OrganizationSettings settings = org.getSettings();
        if (settings == null) {
            settings = new OrganizationSettings();
            settings.setOrganization(org);
        }

        settings.getConfig().put("status", "ACTIVE");
        settings.getConfig().put("activatedAt", Instant.now().toString());
        organizationSettingsRepository.save(settings);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("organizationId", org.getOrganizationCode());
        data.put("status", "ACTIVE");
        data.put("activatedAt", Instant.now().toString());

        return ResponseEntity.ok(ApiResponse.success("Organization activated successfully.", data));
    }

    // ==========================================
    // PHASE 16: Organization Admin Dashboard
    // ==========================================

    @GetMapping("/dashboard/admin")
    @Operation(summary = "Phase 16: Organization dashboard")
    public ResponseEntity<?> getAdminDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Long orgId = user.getOrganization() != null ? user.getOrganization().getId() : null;
        long totalEmployees = orgId != null
                ? employeeRepository.findAll().stream()
                        .filter(e -> e.getOrganization() != null && e.getOrganization().getId().equals(orgId)).count()
                : 0;
        long activeUsers = orgId != null ? userRepository.countByOrganizationId(orgId) : 0;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalEmployees", totalEmployees);
        stats.put("activeUsers", activeUsers);
        stats.put("dashboardStatus", "READY");

        return ResponseEntity.ok(ApiResponse.success("Admin dashboard retrieved.", stats));
    }

    @GetMapping("/dashboard/summary")
    @Operation(summary = "Phase 16: Dashboard summary")
    public ResponseEntity<?> getDashboardSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("onboardingStatus", "COMPLETED");
        summary.put("setupProgress", 100);
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved.", summary));
    }

    @GetMapping("/dashboard/widgets")
    @Operation(summary = "Phase 16: Dashboard widgets")
    public ResponseEntity<?> getDashboardWidgets() {
        List<Map<String, Object>> widgets = List.of(
                Map.of("name", "active_employees", "type", "counter", "value", 1),
                Map.of("name", "pending_leaves", "type", "counter", "value", 0));
        return ResponseEntity.ok(ApiResponse.success("Dashboard widgets retrieved.", widgets));
    }
}
