package com.example.ems.auth.service;

import com.example.ems.auth.dto.RegisterRequest;
import com.example.ems.auth.dto.UserCreateRequest;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.repository.EmployeeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;

public class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @InjectMocks
    private RoleService roleService;

    private Role superAdminRole;
    private Role hrRole;
    private Role financeRole;
    private Role employeeRole;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        superAdminRole = new Role();
        superAdminRole.setId(1L);
        superAdminRole.setName("SUPER_ADMIN");

        hrRole = new Role();
        hrRole.setId(3L);
        hrRole.setName("HR");

        financeRole = new Role();
        financeRole.setId(5L);
        financeRole.setName("FINANCE");

        employeeRole = new Role();
        employeeRole.setId(6L);
        employeeRole.setName("EMPLOYEE");

        when(roleRepository.findByName("SUPER_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(roleRepository.findByName("HR")).thenReturn(Optional.of(hrRole));
        when(roleRepository.findByName("FINANCE")).thenReturn(Optional.of(financeRole));
        when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(employeeRole));
    }

    @Test
    public void testRegisterDynamicRoleFromEmailPrefix() {
        String testEmail = "finance@company.com";
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test User");
        req.setWorkEmail(testEmail);
        req.setPassword("password123");
        req.setConfirmPassword("password123");
        req.setRequestedRole("EMPLOYEE"); // Requested role is EMPLOYEE, but email prefix is finance

        when(userRepository.existsByWorkEmail(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed_password");
        
        // Mock save returning user to get ID (usually JPA generates it, we mock setting it on user object)
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        String result = userService.register(req);
        assertTrue(result.contains("Registration Successful!"));
        assertTrue(result.contains("Role ID: 5"));
    }

    @Test
    public void testRegisterFallbackToRequestedRole() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test User");
        req.setWorkEmail("john.doe@company.com"); // Email prefix does not match any role
        req.setPassword("password123");
        req.setConfirmPassword("password123");
        req.setRequestedRole("HR");

        when(userRepository.existsByWorkEmail("john.doe@company.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(11L);
            return u;
        });

        String result = userService.register(req);
        assertTrue(result.contains("Registration Successful!"));
        assertTrue(result.contains("Role ID: 3"));
    }

    @Test
    public void testCreateUserDynamicRoleFromEmailPrefix() {
        String testEmail = "hr@company.com";
        UserCreateRequest req = new UserCreateRequest();
        req.setFullName("Created User");
        req.setWorkEmail(testEmail);
        req.setPassword("password123");
        req.setConfirmPassword("password123");
        req.setRole("EMPLOYEE"); // Overridden by hr prefix

        when(userRepository.existsByWorkEmail(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(12L);
            return u;
        });

        User created = userService.createUser(req);
        assertNotNull(created);
        assertEquals("HR", created.getRole().getName());
    }

    @Test
    public void testHasRoleOrGreaterHierarchy() {
        User superAdmin = new User();
        superAdmin.setRole(superAdminRole);

        User hr = new User();
        hr.setRole(hrRole);

        User finance = new User();
        finance.setRole(financeRole);

        User employee = new User();
        employee.setRole(employeeRole);

        // Super Admin has id 1, HR has id 3, Finance has id 5, Employee has id 6.
        // A smaller or equal ID corresponds to equal or greater privilege.
        assertTrue(roleService.hasRoleOrGreater(superAdmin, "FINANCE")); // 1 <= 5 (true)
        assertTrue(roleService.hasRoleOrGreater(hr, "FINANCE"));         // 3 <= 5 (true)
        assertTrue(roleService.hasRoleOrGreater(finance, "FINANCE"));    // 5 <= 5 (true)
        assertFalse(roleService.hasRoleOrGreater(employee, "FINANCE"));   // 6 <= 5 (false)

        assertTrue(roleService.hasRoleOrGreater(superAdmin, "HR"));      // 1 <= 3 (true)
        assertTrue(roleService.hasRoleOrGreater(hr, "HR"));              // 3 <= 3 (true)
        assertFalse(roleService.hasRoleOrGreater(finance, "HR"));         // 5 <= 3 (false)
        assertFalse(roleService.hasRoleOrGreater(employee, "HR"));        // 6 <= 3 (false)
    }
}
