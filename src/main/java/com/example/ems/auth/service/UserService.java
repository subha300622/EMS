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
            String reqRole = request.getRequestedRole();
            if (reqRole == null || reqRole.trim().isEmpty()) {
                return "Requested role is required";
            }

            String normalizedRole = reqRole.trim().toUpperCase().replace(" ", "_");
            if (normalizedRole.equals("SUPERADMIN")) {
                normalizedRole = "SUPER_ADMIN";
            }

            optRole = roleRepository.findByName(normalizedRole);
            if (optRole.isEmpty()) {
                optRole = roleRepository.findByName(reqRole);
            }
        }

        if (optRole.isEmpty()) {
            return "Role does not exist in the system. Registration aborted.";
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setWorkEmail(request.getWorkEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setEmployeeId(request.getEmployeeId());
        user.setDepartment(request.getDepartment());
        user.setRequestedRole(request.getRequestedRole());
        user.setRole(optRole.get());
        user.setLocation(request.getLocation());
        // BCrypt hash the password before storing
        user.setPassword(passwordEncoder.encode(request.getPassword()));

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

        log.info("New user registered: {} ({}) and employee profile created", user.getWorkEmail(), userId);
        return "Registration Successful! Your User ID: " + userId + " | Role ID: " + user.getRole().getId();
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

    public Optional<User> updateUser(Long id, UserUpdateRequest request) {
        return userRepository.findById(id).map(user -> {
            if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                    && !request.getEmployeeId().equalsIgnoreCase(user.getEmployeeId())
                    && userRepository.existsByEmployeeId(request.getEmployeeId())) {
                throw new IllegalArgumentException("Employee ID is already in use");
            }
            user.setFullName(request.getFullName());
            user.setMobileNumber(request.getMobileNumber());
            user.setEmployeeId(request.getEmployeeId());
            user.setDepartment(request.getDepartment());
            user.setLocation(request.getLocation());
            return userRepository.save(user);
        });
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean updateUserRole(Long id, String roleName) {
        Optional<User> optUser = userRepository.findById(id);
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
        return true;
    }

    public boolean removeUserRole(Long id) {
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();
        user.setRole(null);
        user.setRequestedRole(null);
        userRepository.save(user);
        return true;
    }


    public boolean updateUserStatus(Long id, String status) {
        Optional<User> optUser = userRepository.findById(id);
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

    public User updateUserProfile(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setFullName(request.getFullName());
        user.setMobileNumber(request.getMobileNumber());
        user.setDepartment(request.getDepartment());
        user.setLocation(request.getLocation());
        return userRepository.save(user);
    }

    public void resetUserPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
