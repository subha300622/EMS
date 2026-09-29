package com.example.ems.auth.service;

import com.example.ems.auth.entity.AuthProvider;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.entity.UserStatus;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class UserProvisioningService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserProvisioningService(UserRepository userRepository, EmployeeRepository employeeRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createAdminUser(Organization organization, Role adminRole, String fullName, String email, String phone, String password, String address) {
        User user = new User();
        user.setFullName(fullName);
        user.setWorkEmail(email.trim().toLowerCase());
        user.setMobileNumber(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(adminRole);
        user.setRoleId(adminRole != null ? adminRole.getId() : null);
        user.setRequestedRole("SUPER_ADMIN");
        user.setOrganization(organization);
        user.setOrganizationId(organization != null ? organization.getId() : null);
        user.setOrganizationName(organization != null ? organization.getName() : null);
        user.setStatus(UserStatus.ACTIVE.name());
        user.setProvider(AuthProvider.LOCAL);
        user.setLocation(address);

        user = userRepository.save(user);

        // Generate userId: USR + String.format("%03d", id)
        String userId = "USR" + String.format("%03d", user.getId());
        user.setUserId(userId);
        user = userRepository.save(user);

        // Provision Employee record
        Employee emp = new Employee();
        emp.setFullName(user.getFullName());
        emp.setEmail(user.getWorkEmail());
        emp.setEmployeeId(userId);
        emp.setPhone(user.getMobileNumber());
        emp.setGender("MALE");
        emp.setDob(LocalDate.of(1990, 1, 1));
        emp.setAddress(address != null ? address : "123 Corporate Way");
        emp.setEmergencyContact("9876543210");
        emp.setDepartment("HR"); // default seeded department
        emp.setDesignation("SUPER_ADMIN");
        emp.setAnnualSalary(BigDecimal.ZERO);
        emp.setJoiningDate(LocalDate.now());
        emp.setLocation(address);
        emp.setEmploymentType("FULL_TIME");
        emp.setStatus("ACTIVE");
        emp.setOrganization(organization);

        employeeRepository.save(emp);

        return user;
    }
}
