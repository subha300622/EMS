package com.example.ems.auth.service;

import com.example.ems.auth.dto.RegisterRequest;
import com.example.ems.auth.dto.UserCreateRequest;
import com.example.ems.auth.dto.UserUpdateRequest;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private com.example.ems.employee.repository.DepartmentRepository departmentRepository;
    @Autowired private com.example.ems.settings.repository.EmployeeSettingRepository employeeSettingRepository;
    @Autowired private com.example.ems.settings.repository.CompanySettingRepository companySettingRepository;
    @Autowired private RoleService roleService;

    // ──────────────────────────────────────────
    //  LOGIN — compares BCrypt-hashed password
    // ──────────────────────────────────────────

    public User login(String workEmail, String password) {
        Optional<User> optUser = userRepository.findByWorkEmail(workEmail);
        if (optUser.isEmpty()) return null;

        User user = optUser.get();
        // BCrypt comparison
        if (!passwordEncoder.matches(password, user.getPassword())) return null;

        return user;
    }

    // ──────────────────────────────────────────
    //  REGISTER — BCrypt-hashes password
    // ──────────────────────────────────────────

    public String register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return "Passwords do not match";
        }

        if (userRepository.existsByWorkEmail(request.getWorkEmail())) {
            return "This work email is already registered. Each email can have only one account.";
        }

        if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                && userRepository.existsByEmployeeId(request.getEmployeeId())) {
            return "Employee ID is already in use";
        }

        // ── Auto-assign the EMPLOYEE role so users can login immediately ────────
        // Admin can upgrade to HR / MANAGER / FINANCE / ADMIN anytime via assign-role.
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElse(null); // safe — seeder always creates EMPLOYEE role on startup

        User user = new User();
        user.setFullName(request.getFullName());
        user.setWorkEmail(request.getWorkEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setEmployeeId(request.getEmployeeId());
        user.setDepartment(request.getDepartment());
        user.setLocation(request.getLocation());
        user.setStatus("ACTIVE");          // ← Can login immediately
        user.setRole(employeeRole);        // ← Default EMPLOYEE role
        user.setRequestedRole(employeeRole != null ? "EMPLOYEE" : null);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String userId = "EMP" + String.format("%03d", user.getId());
        user.setUserId(userId);
        userRepository.save(user);

        // Create Employee profile record
        Employee emp = employeeRepository.findByEmail(user.getWorkEmail())
                .orElseGet(Employee::new);
        emp.setFullName(user.getFullName());
        emp.setEmail(user.getWorkEmail());
        emp.setEmployeeId(userId);
        if (emp.getPhone() == null) emp.setPhone(user.getMobileNumber() != null && !user.getMobileNumber().isBlank() ? user.getMobileNumber() : "1234567890");
        if (emp.getGender() == null) emp.setGender("MALE");
        if (emp.getDob() == null) emp.setDob(LocalDate.of(1990, 1, 1));
        if (emp.getAddress() == null) emp.setAddress("123 Corporate Way");
        if (emp.getEmergencyContact() == null) emp.setEmergencyContact("9876543210");
        if (emp.getDepartment() == null) emp.setDepartment(user.getDepartment() != null && !user.getDepartment().isBlank() ? user.getDepartment() : "Engineering");
        if (emp.getDesignation() == null) emp.setDesignation("Employee");
        if (emp.getAnnualSalary() == null) emp.setAnnualSalary(BigDecimal.valueOf(0));
        if (emp.getJoiningDate() == null) emp.setJoiningDate(LocalDate.now());
        if (emp.getLocation() == null) emp.setLocation(user.getLocation() != null && !user.getLocation().isBlank() ? user.getLocation() : "Headquarters");
        if (emp.getEmploymentType() == null) emp.setEmploymentType("FULL_TIME");
        if (emp.getStatus() == null || emp.getStatus().isBlank()) emp.setStatus("ACTIVE");
        employeeRepository.save(emp);

        log.info("New user registered with EMPLOYEE role: {} ({})", user.getWorkEmail(), userId);
        return "Registration Successful! Your User ID: " + userId + " | Role: EMPLOYEE (admin can upgrade your role)";
    }

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(UserCreateRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByWorkEmail(request.getWorkEmail())) {
            throw new IllegalArgumentException("This work email is already registered");
        }

        if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                && userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID is already in use");
        }

        Optional<Role> optRole = Optional.empty();
        String workEmail = request.getWorkEmail();
        if (workEmail != null && workEmail.endsWith("@company.com")) {
            String prefix = workEmail.substring(0, workEmail.indexOf("@")).trim().toUpperCase().replace(" ", "_");
            if (prefix.equals("SUPERADMIN")) {
                prefix = "SUPER_ADMIN";
            }
            optRole = roleRepository.findByName(prefix);
        }

        if (optRole.isEmpty()) {
            String reqRole = request.getRole();
            if (reqRole != null) {
                String normalizedRole = reqRole.trim().toUpperCase().replace(" ", "_");
                if (normalizedRole.equals("SUPERADMIN")) {
                    normalizedRole = "SUPER_ADMIN";
                }

                optRole = roleRepository.findByName(normalizedRole);
                if (optRole.isEmpty()) {
                    optRole = roleRepository.findByName(reqRole);
                }
            }
        }

        if (optRole.isEmpty()) {
            throw new IllegalArgumentException("Role does not exist");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setWorkEmail(request.getWorkEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setEmployeeId(request.getEmployeeId());
        user.setDepartment(request.getDepartment());
        user.setRequestedRole(request.getRole());
        user.setRole(optRole.get());
        user.setLocation(request.getLocation());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");

        userRepository.save(user);

        String userId = "EMP" + String.format("%03d", user.getId());
        user.setUserId(userId);
        userRepository.save(user);

        Employee emp = employeeRepository.findByEmail(user.getWorkEmail())
                .orElseGet(Employee::new);
        emp.setFullName(user.getFullName());
        emp.setEmail(user.getWorkEmail());
        emp.setEmployeeId(userId);
        if (emp.getPhone() == null) emp.setPhone(user.getMobileNumber() != null && !user.getMobileNumber().isBlank() ? user.getMobileNumber() : "1234567890");
        if (emp.getGender() == null) emp.setGender("MALE");
        if (emp.getDob() == null) emp.setDob(LocalDate.of(1990, 1, 1));
        if (emp.getAddress() == null) emp.setAddress("123 Corporate Way");
        if (emp.getEmergencyContact() == null) emp.setEmergencyContact("9876543210");
        if (emp.getDepartment() == null) emp.setDepartment(user.getDepartment() != null && !user.getDepartment().isBlank() ? user.getDepartment() : "Engineering");
        if (emp.getDesignation() == null) emp.setDesignation(user.getRole() != null ? user.getRole().getName() : "Software Engineer");
        if (emp.getAnnualSalary() == null) emp.setAnnualSalary(BigDecimal.valueOf(85000));
        if (emp.getJoiningDate() == null) emp.setJoiningDate(LocalDate.of(2026, 6, 10));
        if (emp.getLocation() == null) emp.setLocation(user.getLocation() != null && !user.getLocation().isBlank() ? user.getLocation() : "Headquarters");
        if (emp.getEmploymentType() == null) emp.setEmploymentType("FULL_TIME");
        if (emp.getStatus() == null || emp.getStatus().isBlank()) emp.setStatus("ACTIVE");
        employeeRepository.save(emp);

        return user;
    }

    public Optional<User> getUserByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    public Optional<User> updateUserByUserId(String userId, UserUpdateRequest request) {
        return userRepository.findByUserId(userId).map(user -> {
            if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                    && !request.getEmployeeId().equalsIgnoreCase(user.getEmployeeId())
                    && userRepository.existsByEmployeeId(request.getEmployeeId())) {
                throw new IllegalArgumentException("Employee ID is already in use");
            }
            user.setFullName(request.getFullName());
            user.setMobileNumber(request.getMobileNumber());
            if (request.getEmployeeId() != null) {
                user.setEmployeeId(request.getEmployeeId());
            }
            user.setDepartment(request.getDepartment());
            user.setLocation(request.getLocation());
            User saved = userRepository.save(user);
            roleService.evictUserPermissionsCache(userId);
            return saved;
        });
    }

    public boolean deleteUserByUserId(String userId) {
        Optional<User> optUser = userRepository.findByUserId(userId);
        if (optUser.isPresent()) {
            userRepository.delete(optUser.get());
            return true;
        }
        return false;
    }

    public boolean updateUserRoleByUserId(String userId, String roleName) {
        Optional<User> optUser = userRepository.findByUserId(userId);
        if (optUser.isEmpty()) {
            return false;
        }

        String normalizedRole = roleName.trim().toUpperCase().replace(" ", "_");
        if (normalizedRole.equals("SUPERADMIN")) {
            normalizedRole = "SUPER_ADMIN";
        }

        Optional<Role> optRole = roleRepository.findByName(normalizedRole);
        if (optRole.isEmpty()) {
            optRole = roleRepository.findByName(roleName);
        }

        if (optRole.isEmpty()) {
            throw new IllegalArgumentException("Role '" + roleName + "' does not exist");
        }

        User user = optUser.get();
        user.setRole(optRole.get());
        user.setRequestedRole(roleName);
        userRepository.save(user);
        roleService.evictUserPermissionsCache(userId);
        return true;
    }

    public boolean removeUserRoleByUserId(String userId) {
        Optional<User> optUser = userRepository.findByUserId(userId);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();
        user.setRole(null);
        user.setRequestedRole(null);
        userRepository.save(user);
        roleService.evictUserPermissionsCache(userId);
        return true;
    }

    public boolean updateUserStatusByUserId(String userId, String status) {
        Optional<User> optUser = userRepository.findByUserId(userId);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();
        user.setStatus(status);
        userRepository.save(user);
        return true;
    }

    public java.util.List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.searchUsers(query.trim());
    }

    public User updateUserProfile(Long id, com.example.ems.auth.dto.ProfileUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (request.getPhone() != null) {
            user.setMobileNumber(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setLocation(request.getAddress());
        }
        User saved = userRepository.save(user);
        roleService.evictUserPermissionsCache(saved.getUserId());
        return saved;
    }

    public void resetUserPasswordByUserId(String userId, String newPassword) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        roleService.evictUserPermissionsCache(userId);
    }

    public com.example.ems.auth.dto.BootstrapResponse.UserProfileResponse getUserProfile(User user) {
        Optional<Employee> optEmp = employeeRepository.findByEmail(user.getWorkEmail());
        String profileImage = optEmp.map(Employee::getProfileImage).orElse(null);
        if (profileImage == null || profileImage.isBlank()) {
            try {
                profileImage = "https://api.dicebear.com/7.x/initials/svg?seed=" + java.net.URLEncoder.encode(user.getFullName(), java.nio.charset.StandardCharsets.UTF_8.toString());
            } catch (Exception e) {
                profileImage = "https://api.dicebear.com/7.x/initials/svg?seed=" + user.getFullName();
            }
        }

        boolean mfaRequired = false;
        try {
            Optional<com.example.ems.settings.entity.EmployeeSetting> optSettings = employeeSettingRepository.findByUserEmail(user.getWorkEmail());
            if (optSettings.isPresent()) {
                mfaRequired = Boolean.TRUE.equals(optSettings.get().getMfaEnabled());
            }
        } catch (Exception e) {
            // Fallback to false
        }

        return new com.example.ems.auth.dto.BootstrapResponse.UserProfileResponse(
            user.getId(),
            user.getUserId(),
            user.getFullName(),
            user.getWorkEmail(),
            user.getRole() != null ? user.getRole().getName() : "EMPLOYEE",
            profileImage,
            false, // mustChangePassword
            mfaRequired,
            user.getStatus(),
            java.time.Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()
        );
    }

    public com.example.ems.auth.dto.BootstrapResponse.OrgContextResponse getUserContext(User user) {
        Optional<Employee> optEmp = employeeRepository.findByEmail(user.getWorkEmail());
        
        Long companyId = null;
        try {
            Optional<com.example.ems.settings.entity.CompanySetting> optCompany = companySettingRepository.findAll().stream().findFirst();
            if (optCompany.isPresent()) {
                companyId = optCompany.get().getId();
            }
        } catch (Exception e) {
            // Fallback to null
        }

        Long departmentId = null;
        if (optEmp.isPresent() && optEmp.get().getDepartment() != null) {
            Optional<com.example.ems.employee.entity.Department> optDept = departmentRepository.findByNameIgnoreCase(optEmp.get().getDepartment());
            if (optDept.isPresent()) {
                departmentId = optDept.get().getId();
            }
        }

        return new com.example.ems.auth.dto.BootstrapResponse.OrgContextResponse(
            companyId,
            departmentId,
            new com.example.ems.auth.dto.LoginResponse.BranchContext(null, false)
        );
    }
}
