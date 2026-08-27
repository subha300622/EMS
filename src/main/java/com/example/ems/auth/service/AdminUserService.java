package com.example.ems.auth.service;

import com.example.ems.auth.dto.AdminUserDtos;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.common.util.RoleIdResolver;
import com.example.ems.common.util.DepartmentIdResolver;
import com.example.ems.common.util.DesignationIdResolver;
import com.example.ems.common.util.JobLevelIdResolver;
import com.example.ems.common.util.EmploymentTypeIdResolver;
import com.example.ems.common.util.UserIdResolver;
import com.example.ems.employee.domain.EmploymentAssignmentValidator;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Designation;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.EmploymentType;
import com.example.ems.employee.entity.JobLevel;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.DesignationRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.EmploymentTypeRepository;
import com.example.ems.employee.repository.JobLevelRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private JobLevelRepository jobLevelRepository;

    @Autowired
    private EmploymentTypeRepository employmentTypeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public AdminUserDtos.AdminUserResponse createUser(AdminUserDtos.CreateAdminUserRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByWorkEmailAndOrganizationId(request.getEmail().trim().toLowerCase(), orgId)) {
            throw new BadRequestException("Work email is already registered");
        }

        if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank() &&
                userRepository.existsByEmployeeIdAndOrganizationId(request.getEmployeeId().trim(), orgId)) {
            throw new BadRequestException("Employee ID is already in use");
        }

        // Resolving reference aggregate roots
        Long rId = RoleIdResolver.parseId(request.getRoleId());
        Role role = roleRepository.findByIdAndOrganizationId(rId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + request.getRoleId()));

        Long dId = DepartmentIdResolver.parseId(request.getDepartmentId());
        Department department = departmentRepository.findByIdAndOrganizationId(dId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + request.getDepartmentId()));

        Long desId = DesignationIdResolver.parseId(request.getDesignationId());
        Designation designation = designationRepository.findByIdAndOrganizationId(desId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation not found with ID: " + request.getDesignationId()));

        Long jlId = JobLevelIdResolver.parseId(request.getJobLevelId());
        JobLevel jobLevel = jobLevelRepository.findByIdAndDesignationIdAndOrganizationId(jlId, desId, orgId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Job Level not found with ID: " + request.getJobLevelId()));

        Long etId = EmploymentTypeIdResolver.parseId(request.getEmploymentTypeId());
        EmploymentType employmentType = employmentTypeRepository
                .findByIdAndJobLevelIdAndOrganizationId(etId, jlId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employment Type not found with ID: " + request.getEmploymentTypeId()));

        User reportingManager = null;
        List<Long> managerChainIds = new ArrayList<>();
        if (request.getReportingManagerId() != null && !request.getReportingManagerId().isBlank()) {
            Long mId = UserIdResolver.parseId(request.getReportingManagerId());
            reportingManager = userRepository.findByIdAndOrganizationId(mId, orgId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Reporting Manager not found with ID: " + request.getReportingManagerId()));

            // Traverse manager chain to detect loop
            User current = reportingManager;
            java.util.Set<Long> visited = new java.util.HashSet<>();
            while (current != null) {
                if (!visited.add(current.getId())) {
                    throw new BadRequestException("Circular reporting manager loop detected");
                }
                managerChainIds.add(current.getId());
                if (current.getReportingManagerId() != null) {
                    current = userRepository.findByIdAndOrganizationId(current.getReportingManagerId(), orgId)
                            .orElse(null);
                } else {
                    current = null;
                }
            }
        }

        // Domain Validation Layer
        new EmploymentAssignmentValidator().validate(
                organization, role, department, designation, jobLevel, employmentType, reportingManager, null,
                managerChainIds);

        // Map request payload to User aggregate root
        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setFullName(user.getFirstName() + " " + user.getLastName());
        user.setWorkEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setOrganizationId(orgId);
        user.setRoleId(role.getId());
        user.setDepartmentId(department.getId());
        user.setDesignationId(designation.getId());
        user.setJobLevelId(jobLevel.getId());
        user.setEmploymentTypeId(employmentType.getId());
        if (reportingManager != null) {
            user.setReportingManagerId(reportingManager.getId());
        }
        user.setStatus("ACTIVE");
        user.setEmployeeId(request.getEmployeeId() != null ? request.getEmployeeId().trim() : null);

        // Save User aggregate
        userRepository.save(user);

        // Generate userId
        String userId = "EMP" + String.format("%03d", user.getId());
        user.setUserId(userId);
        userRepository.save(user);

        // Keep Employee read-projection in sync
        Employee emp = employeeRepository.findByEmail(user.getWorkEmail()).orElseGet(Employee::new);
        emp.setFullName(user.getFullName());
        emp.setEmail(user.getWorkEmail());
        emp.setEmployeeId(userId);
        emp.setOrganization(organization);
        emp.setDepartment(department.getName());
        emp.setDesignation(designation.getDesignation());
        emp.setEmploymentType(employmentType.getEmploymentType());
        if (reportingManager != null) {
            employeeRepository.findByEmail(reportingManager.getWorkEmail()).ifPresent(emp::setManager);
        }
        emp.setStatus("ACTIVE");
        if (request.getWorkInformation() != null && request.getWorkInformation().getDateOfJoining() != null) {
            try {
                emp.setJoiningDate(LocalDate.parse(request.getWorkInformation().getDateOfJoining()));
            } catch (Exception e) {
                emp.setJoiningDate(LocalDate.now());
            }
        }
        employeeRepository.save(emp);

        return mapToResponse(user, request);
    }

    @Transactional(readOnly = true)
    public AdminUserDtos.AdminUserResponse getUser(String userIdStr) {
        Long orgId = TenantContext.requireOrganizationId();
        Long id = UserIdResolver.parseId(userIdStr);
        User user = userRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userIdStr));
        return mapToResponse(user, null);
    }

    @Transactional(readOnly = true)
    public List<AdminUserDtos.AdminUserResponse> listUsers() {
        Long orgId = TenantContext.requireOrganizationId();
        List<User> users = userRepository.findByOrganizationId(orgId);
        return users.stream()
                .map(user -> mapToResponse(user, null))
                .collect(Collectors.toList());
    }

    private AdminUserDtos.AdminUserResponse mapToResponse(User user, AdminUserDtos.CreateAdminUserRequest req) {
        String reportingManagerIdStr = null;
        if (user.getReportingManagerId() != null) {
            reportingManagerIdStr = UserIdResolver.formatId(user.getReportingManagerId());
        }

        AdminUserDtos.WorkInformation workInfo = req != null ? req.getWorkInformation()
                : new AdminUserDtos.WorkInformation();
        AdminUserDtos.PersonalInformation personalInfo = req != null ? req.getPersonalInformation()
                : new AdminUserDtos.PersonalInformation();
        AdminUserDtos.IdentityInformation identityInfo = req != null ? req.getIdentityInformation()
                : new AdminUserDtos.IdentityInformation();
        AdminUserDtos.NotificationPreferences notifPref = req != null ? req.getNotificationPreferences()
                : new AdminUserDtos.NotificationPreferences();

        return AdminUserDtos.AdminUserResponse.builder()
                .id(UserIdResolver.formatId(user.getId()))
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getWorkEmail())
                .employeeId(user.getEmployeeId())
                .roleId(RoleIdResolver.formatId(user.getRoleId()))
                .departmentId(DepartmentIdResolver.formatId(user.getDepartmentId()))
                .designationId(DesignationIdResolver.formatId(user.getDesignationId()))
                .jobLevelId(JobLevelIdResolver.formatId(user.getJobLevelId()))
                .employmentTypeId(EmploymentTypeIdResolver.formatId(user.getEmploymentTypeId()))
                .reportingManagerId(reportingManagerIdStr)
                .status(user.getStatus())
                .createdAt(
                        user.getCreatedAt() != null ? DateTimeFormatter.ISO_INSTANT.format(user.getCreatedAt()) : null)
                .workInformation(workInfo)
                .personalInformation(personalInfo)
                .identityInformation(identityInfo)
                .notificationPreferences(notifPref)
                .build();
    }
}
