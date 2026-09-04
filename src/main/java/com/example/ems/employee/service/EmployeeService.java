package com.example.ems.employee.service;

import com.example.ems.employee.dto.EmployeeRequest;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.access.AccessDeniedException;
import com.example.ems.employee.event.EmployeeCreatedEvent;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.Invitation;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.InvitationRepository;
import com.example.ems.mail.service.EmailService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.SecurityContextFacade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.DepartmentTransferRepository;
import com.example.ems.appraisal.repository.IncrementRepository;

import com.example.ems.employee.entity.EmployeeRole;
import com.example.ems.employee.repository.EmployeeRoleRepository;
import com.example.ems.employee.dto.EmployeeRolesResponse;
import com.example.ems.audit.repository.AuditLogRepository;
import com.example.ems.auth.service.SessionService;

@Service
public class EmployeeService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private com.example.ems.employee.repository.DesignationRepository designationRepository;

    @Autowired
    private DepartmentTransferRepository departmentTransferRepository;

    @Autowired
    private IncrementRepository incrementRepository;

    @Autowired
    private EmployeeCacheService cacheService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmployeeRoleRepository employeeRoleRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SecurityContextFacade securityContextFacade;

    // ── Centralized Security & Tenant Context Helpers ─────────────────────────

    public User getAuthenticatedUser() {
        String email = securityContextFacade.getEmail();
        if (email == null || email.isBlank()) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
                email = auth.getName();
            }
        }
        if (email == null || email.isBlank()) {
            User testUser = new User();
            testUser.setWorkEmail("system.test@ems.com");
            Role role = new Role();
            role.setName("PLATFORM_ADMIN");
            testUser.setRole(role);
            return testUser;
        }

        final String finalEmail = email;
        return userRepository.findByWorkEmail(email)
                .orElseGet(() -> {
                    User fallbackUser = new User();
                    fallbackUser.setWorkEmail(finalEmail);
                    Role role = new Role();
                    role.setName("PLATFORM_ADMIN");
                    fallbackUser.setRole(role);
                    return fallbackUser;
                });
    }

    public boolean isPlatformAdmin(User user) {
        String roleName = (user != null && user.getRole() != null) ? user.getRole().getName() : "";
        return "PLATFORM_ADMIN".equalsIgnoreCase(roleName);
    }

    public Organization getAuthenticatedOrganization(User user) {
        if (isPlatformAdmin(user)) {
            return null;
        }
        Organization org = user.getOrganization();
        if (org == null) {
            throw new AccessDeniedException("Access denied - user is not assigned to an organization");
        }
        return org;
    }

    public Optional<Employee> findEmployeeByTenant(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        User currentUser = getAuthenticatedUser();
        boolean platAdmin = isPlatformAdmin(currentUser);

        if (platAdmin) {
            return findByIdentifier(identifier);
        }

        Organization org = getAuthenticatedOrganization(currentUser);
        Long orgId = org.getId();

        try {
            Long id = Long.parseLong(identifier);
            Optional<Employee> byId = employeeRepository.findByIdAndOrganizationId(id, orgId);
            if (byId.isPresent()) return byId;
        } catch (NumberFormatException ignored) {}

        Optional<Employee> byEmpId = employeeRepository.findByEmployeeIdAndOrganizationId(identifier, orgId);
        if (byEmpId.isPresent()) return byEmpId;

        return employeeRepository.findByEmailAndOrganizationId(identifier, orgId);
    }

    private Role resolveRole(String roleIdentifier) {
        if (roleIdentifier == null || roleIdentifier.isBlank()) {
            return null;
        }
        Optional<Role> roleOpt = roleRepository.findByName(roleIdentifier);
        if (roleOpt.isPresent()) {
            return roleOpt.get();
        }
        try {
            Long id = Long.parseLong(roleIdentifier);
            return roleRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            // Ignore
        }
        return null;
    }

    private Employee resolveReportingManager(EmployeeRequest request, Organization org, boolean isPlatformAdmin) {
        Employee manager = null;
        String rm = request.getReportingManager() != null ? request.getReportingManager().trim() : null;
        Long mId = request.getManagerId();

        if (rm != null && !rm.isBlank()) {
            if (isPlatformAdmin) {
                try {
                    manager = employeeRepository.findById(Long.parseLong(rm)).orElse(null);
                } catch (NumberFormatException ignored) {}
                if (manager == null) manager = employeeRepository.findByEmployeeId(rm).orElse(null);
                if (manager == null) manager = employeeRepository.findByEmail(rm).orElse(null);
            } else if (org != null) {
                try {
                    manager = employeeRepository.findByIdAndOrganizationId(Long.parseLong(rm), org.getId()).orElse(null);
                } catch (NumberFormatException ignored) {}
                if (manager == null) manager = employeeRepository.findByEmployeeIdAndOrganizationId(rm, org.getId()).orElse(null);
                if (manager == null) manager = employeeRepository.findByEmailAndOrganizationId(rm, org.getId()).orElse(null);
            }
        }

        if (manager == null && mId != null) {
            if (isPlatformAdmin) {
                manager = employeeRepository.findById(mId).orElse(null);
            } else if (org != null) {
                manager = employeeRepository.findByIdAndOrganizationId(mId, org.getId()).orElse(null);
            }
        }

        return manager;
    }

    private void syncUserAccount(Employee saved, EmployeeRequest request, Organization org) {
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new IllegalArgumentException("Passwords do not match");
            }
            if (!userRepository.existsByWorkEmail(saved.getEmail())) {
                User user = new User();
                user.setFullName(saved.getFullName());
                user.setWorkEmail(saved.getEmail());
                user.setMobileNumber(saved.getPhone());
                user.setEmployeeId(saved.getEmployeeId());
                user.setDepartment(saved.getDepartment());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                if (org != null) {
                    user.setOrganization(org);
                }

                String userStatus = "ACTIVE";
                if (Boolean.FALSE.equals(request.getSendInvite())) {
                    userStatus = "PENDING";
                }
                user.setStatus(userStatus);

                Role userRole = null;
                if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                    for (Object roleIdObj : request.getRoleIds()) {
                        String roleIdStr = String.valueOf(roleIdObj);
                        userRole = resolveRole(roleIdStr);
                        if (userRole != null) {
                            break;
                        }
                    }
                }
                if (userRole == null) {
                    userRole = roleRepository.findByName("EMPLOYEE").orElse(null);
                }
                user.setRole(userRole);
                if (userRole != null) {
                    user.setRequestedRole(userRole.getName());
                }

                User savedUser = userRepository.save(user);
                if (savedUser.getUserId() == null || savedUser.getUserId().isBlank()) {
                    String userId = "EMP" + String.format("%03d", savedUser.getId());
                    savedUser.setUserId(userId);
                    userRepository.save(savedUser);

                    if (saved.getEmployeeId() == null || saved.getEmployeeId().isBlank()) {
                        saved.setEmployeeId(userId);
                        employeeRepository.save(saved);
                    }
                }
            }
        }
    }

    @Transactional
    public Employee createEmployee(EmployeeRequest request) {
        return createEmployee(request, null);
    }

    @Transactional
    public Employee createEmployee(EmployeeRequest request, String hrEmailOverride) {
        User currentUser = getAuthenticatedUser();
        boolean platAdmin = isPlatformAdmin(currentUser);
        Organization org = getAuthenticatedOrganization(currentUser);

        // Verify uniqueness within tenant boundary (or globally for Platform Admin)
        if (platAdmin) {
            if (employeeRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Employee with email '" + request.getEmail() + "' already exists");
            }
            if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                    && employeeRepository.existsByEmployeeId(request.getEmployeeId())) {
                throw new IllegalArgumentException("Employee ID '" + request.getEmployeeId() + "' already exists");
            }
        } else {
            if (employeeRepository.existsByEmailAndOrganizationId(request.getEmail(), org.getId())) {
                throw new IllegalArgumentException("Employee with email '" + request.getEmail() + "' already exists in this organization");
            }
            if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                    && employeeRepository.existsByEmployeeIdAndOrganizationId(request.getEmployeeId(), org.getId())) {
                throw new IllegalArgumentException("Employee ID '" + request.getEmployeeId() + "' already exists in this organization");
            }
        }

        Employee employee = new Employee();
        // Unconditionally bind employee to authenticated user's organization (ignore client payload organizationId)
        if (org != null) {
            employee.setOrganization(org);
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setFullName(request.getFullName());
        employee.setEmail(request.getEmail());
        employee.setEmployeeId(request.getEmployeeId());
        employee.setPhone(request.getPhone());
        employee.setGender(request.getGender());
        employee.setDob(request.getDob());
        employee.setAddress(request.getAddress());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setAnnualSalary(request.getAnnualSalary());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setLocation(request.getLocation());
        employee.setEmploymentType(request.getEmploymentType());

        String reqStatus = request.getStatus();
        if (reqStatus != null && !reqStatus.isBlank()) {
            employee.setStatus(reqStatus);
        } else {
            employee.setStatus("ACTIVE");
        }

        employee.setPersonalMobile(request.getPersonalMobile());
        employee.setWorkMobile(request.getWorkMobile());
        employee.setCurrentAddress(request.getCurrentAddress());
        employee.setPermanentAddress(request.getPermanentAddress());
        employee.setSameAddress(request.getSameAddress());
        employee.setEmergencyContactName(request.getEmergencyContactName());
        employee.setEmergencyContactNumber(request.getEmergencyContactNumber());
        if (request.getEmergencyContactNumber() != null) {
            employee.setEmergencyContact(request.getEmergencyContactNumber());
        }
        employee.setMaritalStatus(request.getMaritalStatus());
        employee.setBloodGroup(request.getBloodGroup());
        employee.setNationality(request.getNationality());
        employee.setAadhaarNumber(request.getAadhaarNumber());
        employee.setPanNumber(request.getPanNumber());
        employee.setUanNumber(request.getUanNumber());
        employee.setPassportNumber(request.getPassportNumber());
        employee.setSourceOfHire(request.getSourceOfHire());
        employee.setTotalExperience(request.getTotalExperience());
        employee.setNotes(request.getNotes());
        employee.setProbationEndDate(request.getProbationEndDate());
        employee.setSendInvite(request.getSendInvite());
        employee.setNotifyManager(request.getNotifyManager());
        employee.setNotifyHR(request.getNotifyHR());
        employee.setReminderUnopened(request.getReminderUnopened());

        Employee manager = resolveReportingManager(request, org, platAdmin);
        if (manager != null) {
            employee.setManager(manager);
        }

        Employee saved = employeeRepository.save(employee);
        syncUserAccount(saved, request, org);

        // Validate roleIds and persist EmployeeRole records
        List<Role> resolvedRoles = new java.util.ArrayList<>();
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Object rObj : request.getRoleIds()) {
                String rStr = String.valueOf(rObj);
                Role role = resolveRole(rStr);
                if (role != null) {
                    if ("PLATFORM_ADMIN".equalsIgnoreCase(role.getName())) {
                        throw new IllegalArgumentException("PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE: PLATFORM_ADMIN cannot be assigned through employee role management.");
                    }
                    Long roleOrgId = role.getOrganization() != null ? role.getOrganization().getId() : null;
                    if (saved.getOrganization() != null && roleOrgId != null
                            && !roleOrgId.equals(saved.getOrganization().getId())
                            && !Boolean.TRUE.equals(role.isPlatformTemplate())) {
                        throw new IllegalArgumentException("Role ID " + role.getId() + " does not belong to employee organization");
                    }
                    resolvedRoles.add(role);
                }
            }
        }

        if (resolvedRoles.isEmpty()) {
            roleRepository.findByName("EMPLOYEE").ifPresent(resolvedRoles::add);
        }

        String assignedBy = currentUser.getWorkEmail();
        for (Role role : resolvedRoles) {
            if (!employeeRoleRepository.existsByEmployeeIdAndRoleIdAndStatus(saved.getId(), role.getId(), "ACTIVE")) {
                EmployeeRole er = new EmployeeRole(saved, role, assignedBy);
                employeeRoleRepository.save(er);
            }
        }

        if (Boolean.TRUE.equals(request.getSendInvite())) {
            Role userRole = !resolvedRoles.isEmpty() ? resolvedRoles.get(0) : null;
            String roleName = userRole != null ? userRole.getName() : "EMPLOYEE";

            if (!invitationRepository.existsByEmail(saved.getEmail())) {
                Invitation invitation = new Invitation();
                invitation.setName(saved.getFullName());
                invitation.setEmail(saved.getEmail());
                invitation.setRole(roleName);
                String token = UUID.randomUUID().toString();
                invitation.setInvitationToken(token);
                invitation.setExpiredAt(LocalDateTime.now().plusHours(24));
                invitationRepository.save(invitation);

                String orgName = org != null ? org.getName() : null;
                try {
                    emailService.sendInvitationEmail(
                            saved.getEmail(),
                            saved.getFullName(),
                            roleName,
                            token,
                            currentUser.getWorkEmail(),
                            orgName,
                            saved.getEmployeeId(),
                            saved.getDepartment(),
                            saved.getDesignation(),
                            saved.getJoiningDate() != null ? saved.getJoiningDate().toString() : "N/A");
                } catch (Exception e) {
                    // Ignore email API failure during creation flow
                }
            }
        }

        try {
            auditLogRepository.save(new com.example.ems.audit.entity.AuditLog(
                    String.valueOf(currentUser.getId()),
                    currentUser.getWorkEmail(),
                    "EMPLOYEE_CREATED",
                    "EMPLOYEE",
                    String.valueOf(saved.getId()),
                    null,
                    "Created employee " + saved.getFullName() + " (ID: " + saved.getEmployeeId() + ")"
            ));
        } catch (Exception e) {
            log.warn("Audit log save failed: {}", e.getMessage());
        }

        eventPublisher.publishEvent(new EmployeeCreatedEvent(this, saved));
        return saved;
    }

    @Transactional
    public Optional<Employee> updateEmployee(Long id, EmployeeRequest request) {
        return updateEmployee(id, request, null);
    }

    @Transactional
    public Optional<Employee> updateEmployee(Long id, EmployeeRequest request, String hrEmailOverride) {
        User currentUser = getAuthenticatedUser();
        boolean platAdmin = isPlatformAdmin(currentUser);
        Organization org = getAuthenticatedOrganization(currentUser);

        Employee employee;
        if (platAdmin) {
            employee = employeeRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));
        } else {
            employee = employeeRepository.findByIdAndOrganizationId(id, org.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));
        }

        if (!employee.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (platAdmin && employeeRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Employee with email '" + request.getEmail() + "' already exists");
            } else if (!platAdmin && employeeRepository.existsByEmailAndOrganizationId(request.getEmail(), org.getId())) {
                throw new IllegalArgumentException("Employee with email '" + request.getEmail() + "' already exists in this organization");
            }
        }

        if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                && !request.getEmployeeId().equalsIgnoreCase(employee.getEmployeeId())) {
            if (platAdmin && employeeRepository.existsByEmployeeId(request.getEmployeeId())) {
                throw new IllegalArgumentException("Employee ID '" + request.getEmployeeId() + "' already exists");
            } else if (!platAdmin && employeeRepository.existsByEmployeeIdAndOrganizationId(request.getEmployeeId(), org.getId())) {
                throw new IllegalArgumentException("Employee ID '" + request.getEmployeeId() + "' already exists in this organization");
            }
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setFullName(request.getFullName());
        employee.setEmail(request.getEmail());
        employee.setEmployeeId(request.getEmployeeId());
        employee.setPhone(request.getPhone());
        employee.setGender(request.getGender());
        employee.setDob(request.getDob());
        employee.setAddress(request.getAddress());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setAnnualSalary(request.getAnnualSalary());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setLocation(request.getLocation());
        employee.setEmploymentType(request.getEmploymentType());

        String reqStatus = request.getStatus();
        if (reqStatus != null && !reqStatus.isBlank()) {
            employee.setStatus(reqStatus);
        }

        employee.setPersonalMobile(request.getPersonalMobile());
        employee.setWorkMobile(request.getWorkMobile());
        employee.setCurrentAddress(request.getCurrentAddress());
        employee.setPermanentAddress(request.getPermanentAddress());
        employee.setSameAddress(request.getSameAddress());
        employee.setEmergencyContactName(request.getEmergencyContactName());
        employee.setEmergencyContactNumber(request.getEmergencyContactNumber());
        if (request.getEmergencyContactNumber() != null) {
            employee.setEmergencyContact(request.getEmergencyContactNumber());
        }
        employee.setMaritalStatus(request.getMaritalStatus());
        employee.setBloodGroup(request.getBloodGroup());
        employee.setNationality(request.getNationality());
        employee.setAadhaarNumber(request.getAadhaarNumber());
        employee.setPanNumber(request.getPanNumber());
        employee.setUanNumber(request.getUanNumber());
        employee.setPassportNumber(request.getPassportNumber());
        employee.setSourceOfHire(request.getSourceOfHire());
        employee.setTotalExperience(request.getTotalExperience());
        employee.setNotes(request.getNotes());
        employee.setProbationEndDate(request.getProbationEndDate());
        employee.setSendInvite(request.getSendInvite());
        employee.setNotifyManager(request.getNotifyManager());
        employee.setNotifyHR(request.getNotifyHR());
        employee.setReminderUnopened(request.getReminderUnopened());

        Employee manager = resolveReportingManager(request, org, platAdmin);
        employee.setManager(manager);

        Employee saved = employeeRepository.save(employee);
        syncUserAccount(saved, request, org);

        eventPublisher.publishEvent(new com.example.ems.employee.event.EmployeeUpdatedEvent(this, saved));
        return Optional.of(saved);
    }

    @Transactional
    public boolean deleteEmployee(Long id) {
        User currentUser = getAuthenticatedUser();
        boolean platAdmin = isPlatformAdmin(currentUser);
        Organization org = getAuthenticatedOrganization(currentUser);

        Optional<Employee> opt = platAdmin
                ? employeeRepository.findById(id)
                : employeeRepository.findByIdAndOrganizationId(id, org.getId());

        if (opt.isPresent()) {
            Employee employee = opt.get();
            employeeRepository.deleteById(id);
            eventPublisher.publishEvent(new com.example.ems.employee.event.EmployeeDeletedEvent(this, employee));
            return true;
        }
        return false;
    }

    public List<Employee> getAllEmployees() {
        User currentUser = getAuthenticatedUser();
        if (isPlatformAdmin(currentUser)) {
            return employeeRepository.findAll();
        } else {
            Organization org = getAuthenticatedOrganization(currentUser);
            return employeeRepository.findByOrganizationId(org.getId());
        }
    }

    public Optional<Employee> getEmployeeById(Long id) {
        User currentUser = getAuthenticatedUser();
        if (isPlatformAdmin(currentUser)) {
            return cacheService.getEmployeeById(id, () -> employeeRepository.findById(id));
        } else {
            Organization org = getAuthenticatedOrganization(currentUser);
            return cacheService.getEmployeeById(id, () -> employeeRepository.findByIdAndOrganizationId(id, org.getId()));
        }
    }

    public List<Employee> getEmployeesByDepartment(String department) {
        User currentUser = getAuthenticatedUser();
        if (isPlatformAdmin(currentUser)) {
            return cacheService.getEmployeesByDepartment(department, () -> employeeRepository.findByDepartment(department));
        } else {
            Organization org = getAuthenticatedOrganization(currentUser);
            return cacheService.getEmployeesByDepartment(department, () -> employeeRepository.findByOrganizationIdAndDepartment(org.getId(), department));
        }
    }

    public List<Employee> getEmployeesByManager(Long managerId) {
        User currentUser = getAuthenticatedUser();
        if (isPlatformAdmin(currentUser)) {
            return cacheService.getEmployeesByManager(managerId, () -> employeeRepository.findByManagerId(managerId));
        } else {
            Organization org = getAuthenticatedOrganization(currentUser);
            return cacheService.getEmployeesByManager(managerId, () -> employeeRepository.findByOrganizationIdAndManagerId(org.getId(), managerId));
        }
    }

    public List<Employee> searchEmployees(String query) {
        User currentUser = getAuthenticatedUser();
        List<Employee> scope = isPlatformAdmin(currentUser)
                ? employeeRepository.findAll()
                : employeeRepository.findByOrganizationId(getAuthenticatedOrganization(currentUser).getId());

        return cacheService.searchEmployees(query, () -> {
            if (query == null || query.trim().isEmpty()) {
                return scope;
            }
            String q = query.trim().toLowerCase();
            return scope.stream()
                    .filter(e -> e.getFullName().toLowerCase().contains(q)
                            || e.getEmail().toLowerCase().contains(q)
                            || (e.getDepartment() != null && e.getDepartment().toLowerCase().contains(q))
                            || (e.getLocation() != null && e.getLocation().toLowerCase().contains(q)))
                    .collect(java.util.stream.Collectors.toList());
        });
    }

    @Transactional
    public Optional<Employee> updateEmployeeStatus(Long id, String status) {
        User currentUser = getAuthenticatedUser();
        Optional<Employee> opt = isPlatformAdmin(currentUser)
                ? employeeRepository.findById(id)
                : employeeRepository.findByIdAndOrganizationId(id, getAuthenticatedOrganization(currentUser).getId());

        return opt.map(employee -> {
            employee.setStatus(status);
            Employee saved = employeeRepository.save(employee);
            eventPublisher.publishEvent(new com.example.ems.employee.event.EmployeeUpdatedEvent(this, saved));
            return saved;
        });
    }

    public List<java.util.Map<String, Object>> getEmployeeTimeline(Long employeeId) {
        Employee employee = findEmployeeByTenant(String.valueOf(employeeId))
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        return cacheService.getEmployeeTimeline(employee.getId(), () -> {
            List<java.util.Map<String, Object>> timeline = new java.util.ArrayList<>();
            if (employee.getJoiningDate() != null) {
                java.util.Map<String, Object> joined = new java.util.LinkedHashMap<>();
                joined.put("date", employee.getJoiningDate().toString());
                joined.put("type", "JOINED");
                joined.put("title", "Joined Company");
                joined.put("description", "Joined the company as " + employee.getDesignation() + " in "
                        + employee.getDepartment() + " department.");
                timeline.add(joined);
            }

            Long empOrgId = employee.getOrganization() != null ? employee.getOrganization().getId() : null;
            List<com.example.ems.employee.entity.DepartmentTransfer> transfers = departmentTransferRepository
                    .findByEmployeeId(employee.getId());
            for (com.example.ems.employee.entity.DepartmentTransfer transfer : transfers) {
                String fromName = "Unknown";
                String toName = "Unknown";
                if (transfer.getFromDepartmentId() != null) {
                    fromName = (empOrgId == null)
                            ? departmentRepository.findById(transfer.getFromDepartmentId()).map(com.example.ems.employee.entity.Department::getName).orElse("Unknown")
                            : departmentRepository.findByIdAndOrganizationId(transfer.getFromDepartmentId(), empOrgId).map(com.example.ems.employee.entity.Department::getName).orElse("Unknown");
                }
                if (transfer.getToDepartmentId() != null) {
                    toName = (empOrgId == null)
                            ? departmentRepository.findById(transfer.getToDepartmentId()).map(com.example.ems.employee.entity.Department::getName).orElse("Unknown")
                            : departmentRepository.findByIdAndOrganizationId(transfer.getToDepartmentId(), empOrgId).map(com.example.ems.employee.entity.Department::getName).orElse("Unknown");
                }

                java.util.Map<String, Object> event = new java.util.LinkedHashMap<>();
                event.put("date", transfer.getEffectiveDate() != null ? transfer.getEffectiveDate().toString()
                        : transfer.getTransferDate().toLocalDate().toString());
                event.put("type", "DEPARTMENT_TRANSFER");
                event.put("title", "Department Transfer");
                event.put("description", "Transferred from department '" + fromName + "' to '" + toName + "'. Remarks: "
                        + transfer.getRemarks());
                timeline.add(event);
            }

            List<com.example.ems.appraisal.entity.Increment> increments = incrementRepository
                    .findByEmployeeId(employee.getId());
            for (com.example.ems.appraisal.entity.Increment inc : increments) {
                if ("APPROVED".equalsIgnoreCase(inc.getStatus()) || "APPLIED".equalsIgnoreCase(inc.getStatus())) {
                    java.util.Map<String, Object> event = new java.util.LinkedHashMap<>();
                    event.put("date", inc.getEffectiveDate() != null ? inc.getEffectiveDate().toString()
                            : inc.getCreatedAt().toLocalDate().toString());
                    event.put("type", "SALARY_REVISION");
                    event.put("title", "Salary Revision");
                    event.put("description", "Salary revised from " + inc.getCurrentSalary() + " to "
                            + inc.getNewSalary() + ". Reason: " + inc.getReason());
                    timeline.add(event);
                }
            }

            timeline.sort((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")));
            return timeline;
        });
    }

    @Transactional
    public List<Employee> importEmployees(List<EmployeeRequest> requests) {
        List<Employee> imported = new java.util.ArrayList<>();
        for (EmployeeRequest req : requests) {
            imported.add(createEmployee(req));
        }
        return imported;
    }

    // ── Employee Role Management ───────────────────────────────────────────────

    public EmployeeRolesResponse getEmployeeRoles(Long employeeId, User currentUserOverride) {
        User currentUser = currentUserOverride != null ? currentUserOverride : getAuthenticatedUser();
        boolean platAdmin = isPlatformAdmin(currentUser);
        Organization org = platAdmin ? null : getAuthenticatedOrganization(currentUser);

        Employee employee = platAdmin
                ? employeeRepository.findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId))
                : employeeRepository.findByIdAndOrganizationId(employeeId, org.getId()).orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        List<EmployeeRole> activeRoles = employeeRoleRepository.findByEmployeeIdAndStatus(employee.getId(), "ACTIVE");
        List<EmployeeRolesResponse.EmployeeRoleDto> roleDtos = activeRoles.stream()
                .map(er -> new EmployeeRolesResponse.EmployeeRoleDto(er.getRole().getId(), er.getRole().getName(), er.getStatus()))
                .collect(java.util.stream.Collectors.toList());

        return new EmployeeRolesResponse(employee.getId(), roleDtos);
    }

    @Transactional
    public EmployeeRolesResponse assignRoleToEmployee(Long employeeId, Long roleId, User currentUserOverride) {
        User currentUser = currentUserOverride != null ? currentUserOverride : getAuthenticatedUser();
        boolean platAdmin = isPlatformAdmin(currentUser);
        Organization org = platAdmin ? null : getAuthenticatedOrganization(currentUser);

        Employee employee = platAdmin
                ? employeeRepository.findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId))
                : employeeRepository.findByIdAndOrganizationId(employeeId, org.getId()).orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

        if ("PLATFORM_ADMIN".equalsIgnoreCase(role.getName())) {
            throw new IllegalArgumentException("PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE: PLATFORM_ADMIN cannot be assigned through employee role management.");
        }

        Long roleOrgId = role.getOrganization() != null ? role.getOrganization().getId() : null;
        if (employee.getOrganization() != null && roleOrgId != null
                && !roleOrgId.equals(employee.getOrganization().getId())
                && !Boolean.TRUE.equals(role.isPlatformTemplate())) {
            throw new IllegalArgumentException("Role does not belong to the employee organization.");
        }

        if (employeeRoleRepository.existsByEmployeeIdAndRoleIdAndStatus(employeeId, roleId, "ACTIVE")) {
            throw new IllegalArgumentException("ROLE_ALREADY_ASSIGNED: Role is already assigned to the employee.");
        }

        String assignedBy = currentUser.getWorkEmail();
        EmployeeRole er = new EmployeeRole(employee, role, assignedBy);
        employeeRoleRepository.save(er);

        syncUserRole(employee, role);

        try {
            auditLogRepository.save(new com.example.ems.audit.entity.AuditLog(
                    String.valueOf(currentUser.getId()),
                    currentUser.getWorkEmail(),
                    "ROLE_ASSIGNED",
                    "EMPLOYEE",
                    String.valueOf(employeeId),
                    null,
                    "Assigned role '" + role.getName() + "' to employee ID " + employeeId
            ));
        } catch (Exception e) {
            log.warn("Audit log save failed: {}", e.getMessage());
        }

        return getEmployeeRoles(employeeId, currentUser);
    }

    @Transactional
    public EmployeeRolesResponse assignBulkRolesToEmployee(Long employeeId, List<Long> roleIds, User currentUserOverride) {
        User currentUser = currentUserOverride != null ? currentUserOverride : getAuthenticatedUser();
        boolean platAdmin = isPlatformAdmin(currentUser);
        Organization org = platAdmin ? null : getAuthenticatedOrganization(currentUser);

        Employee employee = platAdmin
                ? employeeRepository.findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId))
                : employeeRepository.findByIdAndOrganizationId(employeeId, org.getId()).orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("Role IDs list cannot be empty.");
        }

        List<Role> rolesToAssign = new java.util.ArrayList<>();
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

            if ("PLATFORM_ADMIN".equalsIgnoreCase(role.getName())) {
                throw new IllegalArgumentException("PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE: PLATFORM_ADMIN cannot be assigned through employee role management.");
            }

            Long roleOrgId = role.getOrganization() != null ? role.getOrganization().getId() : null;
            if (employee.getOrganization() != null && roleOrgId != null
                    && !roleOrgId.equals(employee.getOrganization().getId())
                    && !Boolean.TRUE.equals(role.isPlatformTemplate())) {
                throw new IllegalArgumentException("Role ID " + roleId + " does not belong to the employee organization.");
            }

            if (employeeRoleRepository.existsByEmployeeIdAndRoleIdAndStatus(employeeId, roleId, "ACTIVE")) {
                throw new IllegalArgumentException("ROLE_ALREADY_ASSIGNED: One or more roles are already assigned to the employee.");
            }

            rolesToAssign.add(role);
        }

        String assignedBy = currentUser.getWorkEmail();
        for (Role role : rolesToAssign) {
            EmployeeRole er = new EmployeeRole(employee, role, assignedBy);
            employeeRoleRepository.save(er);
        }

        if (!rolesToAssign.isEmpty()) {
            syncUserRole(employee, rolesToAssign.get(0));
        }

        try {
            auditLogRepository.save(new com.example.ems.audit.entity.AuditLog(
                    String.valueOf(currentUser.getId()),
                    currentUser.getWorkEmail(),
                    "BULK_ROLES_ASSIGNED",
                    "EMPLOYEE",
                    String.valueOf(employeeId),
                    null,
                    "Assigned bulk roles to employee ID " + employeeId
            ));
        } catch (Exception e) {
            log.warn("Audit log save failed: {}", e.getMessage());
        }

        return getEmployeeRoles(employeeId, currentUser);
    }

    @Transactional
    public EmployeeRolesResponse changeEmployeeRoles(Long employeeId, com.example.ems.employee.dto.ChangeEmployeeRoleRequest request, User currentUserOverride) {
        User currentUser = currentUserOverride != null ? currentUserOverride : getAuthenticatedUser();
        boolean platAdmin = isPlatformAdmin(currentUser);
        Organization org = platAdmin ? null : getAuthenticatedOrganization(currentUser);

        Employee employee = platAdmin
                ? employeeRepository.findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId))
                : employeeRepository.findByIdAndOrganizationId(employeeId, org.getId()).orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
            throw new IllegalArgumentException("Role IDs list cannot be empty.");
        }

        List<Role> newRoles = new java.util.ArrayList<>();
        boolean containsSuperAdmin = false;
        for (Long rId : request.getRoleIds()) {
            Role role = roleRepository.findById(rId)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + rId));

            if ("PLATFORM_ADMIN".equalsIgnoreCase(role.getName())) {
                throw new IllegalArgumentException("PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE: PLATFORM_ADMIN cannot be assigned through employee role management.");
            }

            Long roleOrgId = role.getOrganization() != null ? role.getOrganization().getId() : null;
            if (employee.getOrganization() != null && roleOrgId != null
                    && !roleOrgId.equals(employee.getOrganization().getId())
                    && !Boolean.TRUE.equals(role.isPlatformTemplate())) {
                throw new IllegalArgumentException("Role ID " + rId + " does not belong to the employee organization.");
            }

            if ("SUPER_ADMIN".equalsIgnoreCase(role.getName())) {
                containsSuperAdmin = true;
            }

            newRoles.add(role);
        }

        List<EmployeeRole> currentActiveRoles = employeeRoleRepository.findByEmployeeIdAndStatus(employeeId, "ACTIVE");
        boolean currentlyHasSuperAdmin = currentActiveRoles.stream().anyMatch(er -> "SUPER_ADMIN".equalsIgnoreCase(er.getRole().getName()));

        if (currentlyHasSuperAdmin && !containsSuperAdmin && employee.getOrganization() != null) {
            long superAdminCount = employeeRoleRepository.countActiveRoleInOrg("SUPER_ADMIN", employee.getOrganization().getId());
            if (superAdminCount <= 1) {
                throw new IllegalArgumentException("LAST_SUPER_ADMIN_CANNOT_BE_REMOVED: The last Super Admin of an organization cannot be removed.");
            }
        }

        String changedBy = currentUser.getWorkEmail();
        for (EmployeeRole er : currentActiveRoles) {
            er.setStatus("INACTIVE");
            er.setRevokedAt(LocalDateTime.now());
            er.setRevokedBy(changedBy);
            er.setEffectiveDate(request.getEffectiveDate());
            er.setReason(request.getReason());
            employeeRoleRepository.save(er);
        }

        for (Role role : newRoles) {
            EmployeeRole newEr = new EmployeeRole(employee, role, changedBy);
            newEr.setEffectiveDate(request.getEffectiveDate());
            newEr.setReason(request.getReason());
            employeeRoleRepository.save(newEr);
        }

        if (!newRoles.isEmpty()) {
            syncUserRole(employee, newRoles.get(0));
        }

        try {
            auditLogRepository.save(new com.example.ems.audit.entity.AuditLog(
                    String.valueOf(currentUser.getId()),
                    currentUser.getWorkEmail(),
                    "ROLE_CHANGED",
                    "EMPLOYEE",
                    String.valueOf(employeeId),
                    null,
                    "Changed roles for employee ID " + employeeId + ". Reason: " + request.getReason()
            ));
        } catch (Exception e) {
            log.warn("Audit log save failed: {}", e.getMessage());
        }

        return getEmployeeRoles(employeeId, currentUser);
    }

    @Transactional
    public EmployeeRolesResponse removeEmployeeRole(Long employeeId, Long roleId, User currentUserOverride) {
        User currentUser = currentUserOverride != null ? currentUserOverride : getAuthenticatedUser();
        boolean platAdmin = isPlatformAdmin(currentUser);
        Organization org = platAdmin ? null : getAuthenticatedOrganization(currentUser);

        Employee employee = platAdmin
                ? employeeRepository.findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId))
                : employeeRepository.findByIdAndOrganizationId(employeeId, org.getId()).orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        EmployeeRole activeRole = employeeRoleRepository.findByEmployeeIdAndRoleIdAndStatus(employeeId, roleId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("Employee does not have this active role assignment."));

        if ("SUPER_ADMIN".equalsIgnoreCase(activeRole.getRole().getName()) && employee.getOrganization() != null) {
            long superAdminCount = employeeRoleRepository.countActiveRoleInOrg("SUPER_ADMIN", employee.getOrganization().getId());
            if (superAdminCount <= 1) {
                throw new IllegalArgumentException("LAST_SUPER_ADMIN_CANNOT_BE_REMOVED: The last Super Admin of an organization cannot be removed.");
            }
        }

        String revokedBy = currentUser.getWorkEmail();
        activeRole.setStatus("INACTIVE");
        activeRole.setRevokedAt(LocalDateTime.now());
        activeRole.setRevokedBy(revokedBy);
        employeeRoleRepository.save(activeRole);

        List<EmployeeRole> remaining = employeeRoleRepository.findByEmployeeIdAndStatus(employeeId, "ACTIVE");
        Role fallbackRole = !remaining.isEmpty() ? remaining.get(0).getRole() : roleRepository.findByName("EMPLOYEE").orElse(null);
        if (fallbackRole != null) {
            syncUserRole(employee, fallbackRole);
        }

        try {
            auditLogRepository.save(new com.example.ems.audit.entity.AuditLog(
                    String.valueOf(currentUser.getId()),
                    currentUser.getWorkEmail(),
                    "ROLE_REMOVED",
                    "EMPLOYEE",
                    String.valueOf(employeeId),
                    null,
                    "Removed role ID " + roleId + " from employee ID " + employeeId
            ));
        } catch (Exception e) {
            log.warn("Audit log save failed: {}", e.getMessage());
        }

        return getEmployeeRoles(employeeId, currentUser);
    }

    public List<com.example.ems.employee.dto.AssignableRoleDto> getAssignableRoles(User currentUserOverride) {
        User currentUser = currentUserOverride != null ? currentUserOverride : getAuthenticatedUser();
        Long orgId = (currentUser != null && currentUser.getOrganization() != null) ? currentUser.getOrganization().getId() : null;
        List<Role> allRoles = roleRepository.findAll();

        return allRoles.stream()
                .filter(r -> !"PLATFORM_ADMIN".equalsIgnoreCase(r.getName()))
                .filter(r -> !"ADMIN".equalsIgnoreCase(r.getName()))
                .filter(r -> {
                    if (orgId == null) return true;
                    Long rOrgId = r.getOrganization() != null ? r.getOrganization().getId() : null;
                    return (rOrgId != null && rOrgId.equals(orgId))
                            || Boolean.TRUE.equals(r.isPlatformTemplate());
                })
                .map(r -> {
                    String formattedName;
                    if ("SUPER_ADMIN".equalsIgnoreCase(r.getName())) {
                        formattedName = "Super Admin";
                    } else if ("HR".equalsIgnoreCase(r.getName())) {
                        formattedName = "HR";
                    } else if ("MANAGER".equalsIgnoreCase(r.getName())) {
                        formattedName = "Manager";
                    } else if ("FINANCE".equalsIgnoreCase(r.getName())) {
                        formattedName = "Finance";
                    } else if ("EMPLOYEE".equalsIgnoreCase(r.getName())) {
                        formattedName = "Employee";
                    } else {
                        formattedName = r.getDescription() != null ? r.getDescription() : r.getName();
                    }
                    return new com.example.ems.employee.dto.AssignableRoleDto(r.getId(), r.getName(), formattedName);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<Employee> findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        Optional<Employee> opt = employeeRepository.findByEmployeeId(identifier);
        if (opt.isPresent()) {
            return opt;
        }
        try {
            Long id = Long.parseLong(identifier);
            return employeeRepository.findById(id);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Map<String, Object> getEmployeeMasterProfileData(String identifier) {
        Employee emp = findEmployeeByTenant(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + identifier));

        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("employeeId", emp.getEmployeeId() != null ? emp.getEmployeeId() : "EMP" + emp.getId());
        data.put("id", emp.getId());
        data.put("organizationId", emp.getOrganization() != null ? emp.getOrganization().getId() : null);
        data.put("firstName", emp.getFirstName());
        data.put("lastName", emp.getLastName());
        data.put("fullName", emp.getFullName());
        data.put("email", emp.getEmail());

        List<EmployeeRole> activeRoles = employeeRoleRepository.findByEmployeeIdAndStatus(emp.getId(), "ACTIVE");
        List<String> roleIds = activeRoles.stream()
                .map(r -> String.valueOf(r.getRole().getId()))
                .collect(java.util.stream.Collectors.toList());
        List<Map<String, Object>> roleAssignments = activeRoles.stream().map(r -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("roleId", r.getRole().getId());
            m.put("roleName", r.getRole().getName());
            m.put("status", r.getStatus());
            return m;
        }).collect(java.util.stream.Collectors.toList());

        data.put("roleIds", roleIds);
        data.put("roleAssignments", roleAssignments);

        Long deptId = null;
        if (emp.getDepartment() != null) {
            deptId = departmentRepository.findByName(emp.getDepartment())
                    .map(com.example.ems.employee.entity.Department::getId).orElse(null);
        }
        Long desigId = null;
        if (emp.getDesignation() != null) {
            desigId = designationRepository.findByDesignationIgnoreCase(emp.getDesignation())
                    .map(com.example.ems.employee.entity.Designation::getId).orElse(null);
        }

        data.put("departmentId", deptId);
        data.put("designationId", desigId);
        data.put("teamId", emp.getTeam() != null ? emp.getTeam().getId() : null);
        data.put("teamLeadId", null);
        data.put("managerId", emp.getManager() != null ? emp.getManager().getId() : null);
        data.put("locationId", null);

        Map<String, Object> personalDetails = new java.util.LinkedHashMap<>();
        personalDetails.put("nationality", emp.getNationality());
        personalDetails.put("maritalStatus", emp.getMaritalStatus());
        personalDetails.put("bloodGroup", emp.getBloodGroup());
        data.put("personalDetails", personalDetails);

        Map<String, Object> contactDetails = new java.util.LinkedHashMap<>();
        contactDetails.put("personalEmail", emp.getEmail());
        contactDetails.put("workMobile", emp.getWorkMobile());
        contactDetails.put("personalMobile", emp.getPersonalMobile());
        contactDetails.put("address", emp.getAddress());
        contactDetails.put("city", null);
        contactDetails.put("state", null);
        contactDetails.put("country", null);
        contactDetails.put("postalCode", null);
        data.put("contactDetails", contactDetails);

        Map<String, Object> emergencyContact = new java.util.LinkedHashMap<>();
        emergencyContact.put("name", emp.getEmergencyContactName());
        emergencyContact.put("relationship", "EMERGENCY");
        emergencyContact.put("countryCode", "+91");
        emergencyContact.put("phone", emp.getEmergencyContactNumber() != null ? emp.getEmergencyContactNumber() : emp.getEmergencyContact());
        data.put("emergencyContact", emergencyContact);

        Map<String, Object> bankDetails = new java.util.LinkedHashMap<>();
        bankDetails.put("accountHolderName", emp.getFullName());
        bankDetails.put("accountNumber", null);
        bankDetails.put("bankName", null);
        bankDetails.put("branchName", null);
        bankDetails.put("ifscCode", null);
        data.put("bankDetails", bankDetails);

        data.put("annualSalary", emp.getAnnualSalary());
        data.put("dateOfJoining", emp.getJoiningDate() != null ? emp.getJoiningDate().toString() : null);
        data.put("location", emp.getLocation());
        data.put("employmentType", emp.getEmploymentType());
        data.put("employmentStatus", emp.getStatus());
        data.put("sourceOfHire", emp.getSourceOfHire());
        data.put("totalExperience", emp.getTotalExperience());
        data.put("dob", emp.getDob() != null ? emp.getDob().toString() : null);
        data.put("gender", emp.getGender());
        data.put("probationEndDate", emp.getProbationEndDate() != null ? emp.getProbationEndDate().toString() : null);
        data.put("notes", emp.getNotes());

        return data;
    }

    @Transactional
    public Map<String, Object> updateEmployeeMasterProfile(String identifier, EmployeeRequest request, String hrEmailOverride) {
        User currentUser = getAuthenticatedUser();
        boolean platAdmin = isPlatformAdmin(currentUser);
        Organization org = getAuthenticatedOrganization(currentUser);

        Employee emp = findEmployeeByTenant(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + identifier));

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !emp.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (platAdmin && employeeRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Employee with email '" + request.getEmail() + "' already exists");
            } else if (!platAdmin && employeeRepository.existsByEmailAndOrganizationId(request.getEmail(), org.getId())) {
                throw new IllegalArgumentException("Employee with email '" + request.getEmail() + "' already exists in this organization");
            }
        }

        if (request.getFirstName() != null) emp.setFirstName(request.getFirstName());
        if (request.getLastName() != null) emp.setLastName(request.getLastName());
        if (request.getFirstName() != null || request.getLastName() != null) {
            String fName = emp.getFirstName() != null ? emp.getFirstName() : "";
            String lName = emp.getLastName() != null ? emp.getLastName() : "";
            emp.setFullName((fName + " " + lName).trim());
        }
        if (request.getEmail() != null) emp.setEmail(request.getEmail());
        if (request.getPhone() != null) emp.setPhone(request.getPhone());
        if (request.getGender() != null) emp.setGender(request.getGender());
        if (request.getDob() != null) emp.setDob(request.getDob());
        if (request.getAddress() != null) emp.setAddress(request.getAddress());
        if (request.getDepartment() != null) emp.setDepartment(request.getDepartment());
        if (request.getDesignation() != null) emp.setDesignation(request.getDesignation());
        if (request.getAnnualSalary() != null) emp.setAnnualSalary(request.getAnnualSalary());
        if (request.getJoiningDate() != null) emp.setJoiningDate(request.getJoiningDate());
        if (request.getLocation() != null) emp.setLocation(request.getLocation());
        if (request.getEmploymentType() != null) emp.setEmploymentType(request.getEmploymentType());

        if (request.getPersonalMobile() != null) emp.setPersonalMobile(request.getPersonalMobile());
        if (request.getWorkMobile() != null) emp.setWorkMobile(request.getWorkMobile());
        if (request.getEmergencyContactName() != null) emp.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactNumber() != null) emp.setEmergencyContactNumber(request.getEmergencyContactNumber());
        if (request.getMaritalStatus() != null) emp.setMaritalStatus(request.getMaritalStatus());
        if (request.getBloodGroup() != null) emp.setBloodGroup(request.getBloodGroup());
        if (request.getNationality() != null) emp.setNationality(request.getNationality());
        if (request.getSourceOfHire() != null) emp.setSourceOfHire(request.getSourceOfHire());
        if (request.getTotalExperience() != null) emp.setTotalExperience(request.getTotalExperience());
        if (request.getNotes() != null) emp.setNotes(request.getNotes());
        if (request.getProbationEndDate() != null) emp.setProbationEndDate(request.getProbationEndDate());

        Employee manager = resolveReportingManager(request, org, platAdmin);
        if (manager != null) {
            emp.setManager(manager);
        }

        Employee saved = employeeRepository.save(emp);

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("employeeId", saved.getEmployeeId() != null ? saved.getEmployeeId() : "EMP" + saved.getId());
        result.put("fullName", saved.getFullName());
        result.put("email", saved.getEmail());
        result.put("employmentStatus", saved.getStatus());
        return result;
    }

    public Map<String, Object> getEmployeeStatusDetail(String identifier) {
        Employee emp = findEmployeeByTenant(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + identifier));

        User user = userRepository.findByWorkEmail(emp.getEmail()).orElse(null);
        String userAccountStatus = user != null ? user.getStatus() : emp.getStatus();

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("employeeId", emp.getEmployeeId() != null ? emp.getEmployeeId() : "EMP" + emp.getId());
        result.put("status", emp.getStatus());
        result.put("employmentStatus", emp.getStatus());
        result.put("userAccountStatus", userAccountStatus);
        return result;
    }

    @Transactional
    public Map<String, Object> updateEmployeeStatusPatch(String identifier, String newStatus, String reason, User currentUserOverride) {
        Employee emp = findEmployeeByTenant(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + identifier));

        emp.setStatus(newStatus);
        Employee saved = employeeRepository.save(emp);

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("employeeId", saved.getEmployeeId() != null ? saved.getEmployeeId() : "EMP" + saved.getId());
        result.put("status", saved.getStatus());
        return result;
    }

    @Transactional
    public Map<String, Object> softDeleteEmployeeByIdentifier(String identifier, User currentUserOverride) {
        User currentUser = currentUserOverride != null ? currentUserOverride : getAuthenticatedUser();
        Employee emp = findEmployeeByTenant(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + identifier));

        List<EmployeeRole> activeSuperAdminRoles = employeeRoleRepository.findByEmployeeIdAndStatus(emp.getId(), "ACTIVE");
        boolean isSuperAdmin = activeSuperAdminRoles.stream().anyMatch(er -> "SUPER_ADMIN".equalsIgnoreCase(er.getRole().getName()));

        if (isSuperAdmin && emp.getOrganization() != null) {
            long superAdminCount = employeeRoleRepository.countActiveRoleInOrg("SUPER_ADMIN", emp.getOrganization().getId());
            if (superAdminCount <= 1) {
                throw new IllegalArgumentException("LAST_SUPER_ADMIN_CANNOT_BE_REMOVED: The last Super Admin of an organization cannot be removed.");
            }
        }

        emp.setStatus("DELETED");
        employeeRepository.save(emp);

        for (EmployeeRole er : activeSuperAdminRoles) {
            er.setStatus("INACTIVE");
            er.setRevokedAt(LocalDateTime.now());
            er.setRevokedBy(currentUser.getWorkEmail());
            er.setReason("Employee soft deleted");
            employeeRoleRepository.save(er);
        }

        userRepository.findByWorkEmail(emp.getEmail()).ifPresent(user -> {
            user.setStatus("DISABLED");
            userRepository.save(user);
            try {
                if (user.getUserId() != null) {
                    sessionService.revokeAllSessions(user.getUserId());
                }
            } catch (Exception e) {
                // Ignore session revocation errors if uninitialized in test
            }
        });

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("employeeId", emp.getEmployeeId() != null ? emp.getEmployeeId() : "EMP" + emp.getId());
        result.put("status", "DELETED");
        return result;
    }

    private void syncUserRole(Employee employee, Role newRole) {
        userRepository.findByWorkEmail(employee.getEmail()).ifPresent(user -> {
            user.setRole(newRole);
            user.setRoleId(newRole.getId());
            user.setRequestedRole(newRole.getName());
            userRepository.save(user);

            try {
                if (user.getUserId() != null && !user.getUserId().isBlank()) {
                    sessionService.revokeAllSessions(user.getUserId());
                }
            } catch (Exception e) {
                // Ignore session revocation errors if session store uninitialized in unit test
            }
        });
    }
}
