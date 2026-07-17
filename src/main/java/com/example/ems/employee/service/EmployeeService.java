package com.example.ems.employee.service;

import com.example.ems.employee.dto.EmployeeRequest;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.ems.employee.event.EmployeeCreatedEvent;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.Invitation;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.InvitationRepository;
import com.example.ems.mail.service.EmailService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.DepartmentTransferRepository;
import com.example.ems.appraisal.repository.IncrementRepository;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private DepartmentRepository departmentRepository;

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

    private Employee resolveReportingManager(EmployeeRequest request) {
        Employee manager = null;
        if (request.getReportingManager() != null && !request.getReportingManager().isBlank()) {
            String rm = request.getReportingManager().trim();
            try {
                Long mId = Long.parseLong(rm);
                manager = employeeRepository.findById(mId).orElse(null);
            } catch (NumberFormatException e) {
                // Ignore
            }
            if (manager == null) {
                manager = employeeRepository.findByEmployeeId(rm).orElse(null);
            }
            if (manager == null) {
                manager = employeeRepository.findByEmail(rm).orElse(null);
            }
            if (manager == null) {
                List<Employee> byName = employeeRepository.findAll().stream()
                        .filter(e -> e.getFullName().equalsIgnoreCase(rm))
                        .toList();
                if (!byName.isEmpty()) {
                    manager = byName.get(0);
                }
            }
        }
        if (manager == null && request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId()).orElse(null);
        }
        return manager;
    }

    private void syncUserAccount(Employee saved, EmployeeRequest request) {
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

                String userStatus = "ACTIVE";
                if (Boolean.FALSE.equals(request.getSendInvite())) {
                    userStatus = "PENDING";
                }
                user.setStatus(userStatus);

                Role userRole = null;
                if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                    for (String roleIdStr : request.getRoleIds()) {
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
    public Employee createEmployee(EmployeeRequest request, String hrEmail) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Employee with email '" + request.getEmail() + "' already exists");
        }

        if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                && employeeRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID '" + request.getEmployeeId() + "' already exists");
        }

        Employee employee = new Employee();
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

        // Map new fields
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

        Employee manager = resolveReportingManager(request);
        if (manager != null) {
            employee.setManager(manager);
        }

        User hrUser = null;
        if (hrEmail != null && !hrEmail.isBlank()) {
            hrUser = userRepository.findByWorkEmail(hrEmail).orElse(null);
            if (hrUser != null) {
                employee.setOrganization(hrUser.getOrganization());
            }
        }

        Employee saved = employeeRepository.save(employee);
        syncUserAccount(saved, request);

        if (Boolean.TRUE.equals(request.getSendInvite())) {
            Role userRole = null;
            if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                for (String roleIdStr : request.getRoleIds()) {
                    userRole = resolveRole(roleIdStr);
                    if (userRole != null) {
                        break;
                    }
                }
            }
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

                String orgName = (hrUser != null && hrUser.getOrganization() != null)
                        ? hrUser.getOrganization().getName()
                        : null;
                try {
                    emailService.sendInvitationEmail(
                            saved.getEmail(),
                            saved.getFullName(),
                            roleName,
                            token,
                            hrEmail,
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

        eventPublisher.publishEvent(new EmployeeCreatedEvent(this, saved));
        return saved;
    }

    @Transactional
    public Optional<Employee> updateEmployee(Long id, EmployeeRequest request) {
        return updateEmployee(id, request, null);
    }

    @Transactional
    public Optional<Employee> updateEmployee(Long id, EmployeeRequest request, String hrEmail) {
        return employeeRepository.findById(id).map(employee -> {
            if (!employee.getEmail().equalsIgnoreCase(request.getEmail())
                    && employeeRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Employee with email '" + request.getEmail() + "' already exists");
            }

            if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                    && !request.getEmployeeId().equalsIgnoreCase(employee.getEmployeeId())
                    && employeeRepository.existsByEmployeeId(request.getEmployeeId())) {
                throw new IllegalArgumentException("Employee ID '" + request.getEmployeeId() + "' already exists");
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

            // Map new fields
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

            Employee manager = resolveReportingManager(request);
            if (manager != null) {
                employee.setManager(manager);
            } else {
                employee.setManager(null);
            }

            User hrUser = null;
            if (hrEmail != null && !hrEmail.isBlank()) {
                hrUser = userRepository.findByWorkEmail(hrEmail).orElse(null);
                if (hrUser != null) {
                    employee.setOrganization(hrUser.getOrganization());
                }
            }

            Employee saved = employeeRepository.save(employee);
            syncUserAccount(saved, request);

            if (Boolean.TRUE.equals(request.getSendInvite())) {
                Role userRole = null;
                if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                    for (String roleIdStr : request.getRoleIds()) {
                        userRole = resolveRole(roleIdStr);
                        if (userRole != null) {
                            break;
                        }
                    }
                }
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

                    String orgName = (hrUser != null && hrUser.getOrganization() != null)
                            ? hrUser.getOrganization().getName()
                            : null;
                    try {
                        emailService.sendInvitationEmail(
                                saved.getEmail(),
                                saved.getFullName(),
                                roleName,
                                token,
                                hrEmail,
                                orgName,
                                saved.getEmployeeId(),
                                saved.getDepartment(),
                                saved.getDesignation(),
                                saved.getJoiningDate() != null ? saved.getJoiningDate().toString() : "N/A");
                    } catch (Exception e) {
                        // Ignore email API failure
                    }
                }
            }

            eventPublisher.publishEvent(new com.example.ems.employee.event.EmployeeUpdatedEvent(this, saved));
            return saved;
        });
    }

    @Transactional
    public boolean deleteEmployee(Long id) {
        Optional<Employee> opt = employeeRepository.findById(id);
        if (opt.isPresent()) {
            Employee employee = opt.get();
            employeeRepository.deleteById(id);
            eventPublisher.publishEvent(new com.example.ems.employee.event.EmployeeDeletedEvent(this, employee));
            return true;
        }
        return false;
    }

    public List<Employee> getAllEmployees() {
        return cacheService.getAllEmployees(() -> employeeRepository.findAll());
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return cacheService.getEmployeeById(id, () -> employeeRepository.findById(id));
    }

    public List<Employee> getEmployeesByDepartment(String department) {
        return cacheService.getEmployeesByDepartment(department, () -> employeeRepository.findByDepartment(department));
    }

    public List<Employee> getEmployeesByManager(Long managerId) {
        return cacheService.getEmployeesByManager(managerId, () -> employeeRepository.findByManagerId(managerId));
    }

    public List<Employee> searchEmployees(String query) {
        return cacheService.searchEmployees(query, () -> {
            if (query == null || query.trim().isEmpty()) {
                return employeeRepository.findAll();
            }
            String q = query.trim().toLowerCase();
            return employeeRepository.findAll().stream()
                    .filter(e -> e.getFullName().toLowerCase().contains(q)
                            || e.getEmail().toLowerCase().contains(q)
                            || (e.getDepartment() != null && e.getDepartment().toLowerCase().contains(q))
                            || (e.getLocation() != null && e.getLocation().toLowerCase().contains(q)))
                    .collect(java.util.stream.Collectors.toList());
        });
    }

    @Transactional
    public Optional<Employee> updateEmployeeStatus(Long id, String status) {
        return employeeRepository.findById(id).map(employee -> {
            employee.setStatus(status);
            Employee saved = employeeRepository.save(employee);
            eventPublisher.publishEvent(new com.example.ems.employee.event.EmployeeUpdatedEvent(this, saved));
            return saved;
        });
    }

    public List<java.util.Map<String, Object>> getEmployeeTimeline(Long employeeId) {
        return cacheService.getEmployeeTimeline(employeeId, () -> {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

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

            // 2. Department transfer events
            List<com.example.ems.employee.entity.DepartmentTransfer> transfers = departmentTransferRepository
                    .findByEmployeeId(employeeId);
            for (com.example.ems.employee.entity.DepartmentTransfer transfer : transfers) {
                String fromName = "Unknown";
                String toName = "Unknown";
                if (transfer.getFromDepartmentId() != null) {
                    fromName = departmentRepository.findById(transfer.getFromDepartmentId())
                            .map(com.example.ems.employee.entity.Department::getName)
                            .orElse("Unknown");
                }
                if (transfer.getToDepartmentId() != null) {
                    toName = departmentRepository.findById(transfer.getToDepartmentId())
                            .map(com.example.ems.employee.entity.Department::getName)
                            .orElse("Unknown");
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

            // 3. Salary Revision (Increment) events
            List<com.example.ems.appraisal.entity.Increment> increments = incrementRepository
                    .findByEmployeeId(employeeId);
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

            // Sort chronological
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
}
