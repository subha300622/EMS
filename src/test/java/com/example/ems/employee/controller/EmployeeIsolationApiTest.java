package com.example.ems.employee.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.dto.EmployeeRequest;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.service.EmployeeService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.SecurityContextFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EmployeeIsolationApiTest {

    @Mock
    private SecurityContextFacade securityContextFacade;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Mock
    private com.example.ems.employee.repository.EmployeeRoleRepository employeeRoleRepository;

    @Mock
    private com.example.ems.employee.service.EmployeeCacheService cacheService;

    @InjectMocks
    private EmployeeService employeeService;

    private Organization orgA;
    private Organization orgB;
    private User superAdminA;
    private User superAdminB;
    private User platformAdmin;
    private Employee empOrgA;
    private Employee empOrgB;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        orgA = new Organization();
        orgA.setId(1001L);
        orgA.setName("Organization A");

        orgB = new Organization();
        orgB.setId(1002L);
        orgB.setName("Organization B");

        Role superAdminRole = new Role();
        superAdminRole.setId(1L);
        superAdminRole.setName("SUPER_ADMIN");

        Role platformAdminRole = new Role();
        platformAdminRole.setId(2L);
        platformAdminRole.setName("PLATFORM_ADMIN");

        superAdminA = new User();
        superAdminA.setId(10L);
        superAdminA.setWorkEmail("adminA@orga.com");
        superAdminA.setOrganization(orgA);
        superAdminA.setRole(superAdminRole);

        superAdminB = new User();
        superAdminB.setId(20L);
        superAdminB.setWorkEmail("adminB@orgb.com");
        superAdminB.setOrganization(orgB);
        superAdminB.setRole(superAdminRole);

        platformAdmin = new User();
        platformAdmin.setId(99L);
        platformAdmin.setWorkEmail("platform@ems.com");
        platformAdmin.setOrganization(null);
        platformAdmin.setRole(platformAdminRole);

        empOrgA = new Employee();
        empOrgA.setId(100L);
        empOrgA.setEmployeeId("EMP-A-001");
        empOrgA.setFirstName("Alice");
        empOrgA.setLastName("Smith");
        empOrgA.setFullName("Alice Smith");
        empOrgA.setEmail("alice@orga.com");
        empOrgA.setOrganization(orgA);
        empOrgA.setStatus("ACTIVE");

        empOrgB = new Employee();
        empOrgB.setId(200L);
        empOrgB.setEmployeeId("EMP-B-001");
        empOrgB.setFirstName("Bob");
        empOrgB.setLastName("Jones");
        empOrgB.setFullName("Bob Jones");
        empOrgB.setEmail("bob@orgb.com");
        empOrgB.setOrganization(orgB);
        empOrgB.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("Super Admin A creates employee -> Employee unconditionally receives Org A")
    public void superAdminACreatesEmployee_getsOrgA() {
        when(securityContextFacade.getEmail()).thenReturn("adminA@orga.com");
        when(userRepository.findByWorkEmail("adminA@orga.com")).thenReturn(Optional.of(superAdminA));
        when(employeeRepository.existsByEmailAndOrganizationId("alice.new@orga.com", 1001L)).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> {
            Employee e = i.getArgument(0);
            e.setId(101L);
            return e;
        });

        EmployeeRequest req = new EmployeeRequest();
        req.setFirstName("Alice");
        req.setLastName("New");
        req.setFullName("Alice New");
        req.setEmail("alice.new@orga.com");
        req.setEmployeeId("EMP-A-002");

        Employee created = employeeService.createEmployee(req);
        assertNotNull(created);
        assertNotNull(created.getOrganization());
        assertEquals(1001L, created.getOrganization().getId());
    }

    @Test
    @DisplayName("Super Admin A lists employees -> Returns only Org A employees")
    public void superAdminAListsEmployees_onlyOrgA() {
        when(securityContextFacade.getEmail()).thenReturn("adminA@orga.com");
        when(userRepository.findByWorkEmail("adminA@orga.com")).thenReturn(Optional.of(superAdminA));
        when(employeeRepository.findByOrganizationId(1001L)).thenReturn(List.of(empOrgA));

        List<Employee> list = employeeService.getAllEmployees();
        assertEquals(1, list.size());
        assertEquals("EMP-A-001", list.get(0).getEmployeeId());
        assertEquals(1001L, list.get(0).getOrganization().getId());
    }

    @Test
    @DisplayName("Super Admin B lists employees -> Returns only Org B employees")
    public void superAdminBListsEmployees_onlyOrgB() {
        when(securityContextFacade.getEmail()).thenReturn("adminB@orgb.com");
        when(userRepository.findByWorkEmail("adminB@orgb.com")).thenReturn(Optional.of(superAdminB));
        when(employeeRepository.findByOrganizationId(1002L)).thenReturn(List.of(empOrgB));

        List<Employee> list = employeeService.getAllEmployees();
        assertEquals(1, list.size());
        assertEquals("EMP-B-001", list.get(0).getEmployeeId());
        assertEquals(1002L, list.get(0).getOrganization().getId());
    }

    @Test
    @DisplayName("Super Admin A reads Org B employee -> Returns 404 / Throws IllegalArgumentException")
    public void superAdminAReadsOrgBEmployee_returns404() {
        when(securityContextFacade.getEmail()).thenReturn("adminA@orga.com");
        when(userRepository.findByWorkEmail("adminA@orga.com")).thenReturn(Optional.of(superAdminA));
        when(employeeRepository.findByEmployeeIdAndOrganizationId("EMP-B-001", 1001L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeMasterProfileData("EMP-B-001");
        });
    }

    @Test
    @DisplayName("Super Admin A updates Org B employee -> Returns 404")
    public void superAdminAUpdatesOrgBEmployee_returns404() {
        when(securityContextFacade.getEmail()).thenReturn("adminA@orga.com");
        when(userRepository.findByWorkEmail("adminA@orga.com")).thenReturn(Optional.of(superAdminA));
        when(employeeRepository.findByEmployeeIdAndOrganizationId("EMP-B-001", 1001L)).thenReturn(Optional.empty());

        EmployeeRequest req = new EmployeeRequest();
        req.setFirstName("Hacked");

        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployeeMasterProfile("EMP-B-001", req, null);
        });
    }

    @Test
    @DisplayName("Super Admin A deletes Org B employee -> Returns 404")
    public void superAdminADeletesOrgBEmployee_returns404() {
        when(securityContextFacade.getEmail()).thenReturn("adminA@orga.com");
        when(userRepository.findByWorkEmail("adminA@orga.com")).thenReturn(Optional.of(superAdminA));
        when(employeeRepository.findByEmployeeIdAndOrganizationId("EMP-B-001", 1001L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.softDeleteEmployeeByIdentifier("EMP-B-001", null);
        });
    }

    @Test
    @DisplayName("Super Admin A assigns Org B manager to Org A employee -> Rejected")
    public void superAdminAAssignsOrgBManager_rejected() {
        when(securityContextFacade.getEmail()).thenReturn("adminA@orga.com");
        when(userRepository.findByWorkEmail("adminA@orga.com")).thenReturn(Optional.of(superAdminA));
        when(employeeRepository.existsByEmailAndOrganizationId("new.emp@orga.com", 1001L)).thenReturn(false);
        // Attempt to look up manager 200L in Org A (1001L) -> returns empty because
        // manager belongs to 1002L
        when(employeeRepository.findByIdAndOrganizationId(200L, 1001L)).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        EmployeeRequest req = new EmployeeRequest();
        req.setFirstName("New");
        req.setLastName("Emp");
        req.setEmail("new.emp@orga.com");
        req.setManagerId(200L); // Manager from Org B

        Employee created = employeeService.createEmployee(req);
        assertNull(created.getManager()); // Manager assignment rejected because manager belongs to Org B
    }

    @Test
    @DisplayName("Platform Admin reads Org A and Org B employee -> Allowed")
    public void platformAdminReadsOrgAAndOrgBEmployee_allowed() {
        when(securityContextFacade.getEmail()).thenReturn("platform@ems.com");
        when(userRepository.findByWorkEmail("platform@ems.com")).thenReturn(Optional.of(platformAdmin));
        when(employeeRepository.findByEmployeeId("EMP-A-001")).thenReturn(Optional.of(empOrgA));
        when(employeeRepository.findByEmployeeId("EMP-B-001")).thenReturn(Optional.of(empOrgB));

        Map<String, Object> dataA = employeeService.getEmployeeMasterProfileData("EMP-A-001");
        assertNotNull(dataA);
        assertEquals("EMP-A-001", dataA.get("employeeId"));

        Map<String, Object> dataB = employeeService.getEmployeeMasterProfileData("EMP-B-001");
        assertNotNull(dataB);
        assertEquals("EMP-B-001", dataB.get("employeeId"));
    }

    @Test
    @DisplayName("Duplicate employee ID within same organization -> Rejected")
    public void duplicateEmployeeIdWithinSameOrg_rejected() {
        when(securityContextFacade.getEmail()).thenReturn("adminA@orga.com");
        when(userRepository.findByWorkEmail("adminA@orga.com")).thenReturn(Optional.of(superAdminA));
        when(employeeRepository.existsByEmailAndOrganizationId("dup@orga.com", 1001L)).thenReturn(false);
        when(employeeRepository.existsByEmployeeIdAndOrganizationId("EMP-A-001", 1001L)).thenReturn(true);

        EmployeeRequest req = new EmployeeRequest();
        req.setEmail("dup@orga.com");
        req.setEmployeeId("EMP-A-001");

        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(req);
        });
    }

    @Test
    @DisplayName("Super Admin A reads Org B employee timeline -> Returns 404")
    public void superAdminAReadsOrgBTimeline_returns404() {
        when(securityContextFacade.getEmail()).thenReturn("adminA@orga.com");
        when(userRepository.findByWorkEmail("adminA@orga.com")).thenReturn(Optional.of(superAdminA));
        when(employeeRepository.findByEmployeeIdAndOrganizationId("EMP-B-001", 1001L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeTimeline(200L);
        });
    }

    @Test
    @DisplayName("Super Admin A updates Org B employee status -> Returns 404")
    public void superAdminAUpdatesOrgBStatus_returns404() {
        when(securityContextFacade.getEmail()).thenReturn("adminA@orga.com");
        when(userRepository.findByWorkEmail("adminA@orga.com")).thenReturn(Optional.of(superAdminA));
        when(employeeRepository.findByEmployeeIdAndOrganizationId("EMP-B-001", 1001L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployeeStatusPatch("EMP-B-001", "INACTIVE", "Testing", superAdminA);
        });
    }

    @Test
    @DisplayName("Super Admin A manages Org B employee roles -> Returns 404")
    public void superAdminAChangesOrgBRoles_returns404() {
        when(securityContextFacade.getEmail()).thenReturn("adminA@orga.com");
        when(userRepository.findByWorkEmail("adminA@orga.com")).thenReturn(Optional.of(superAdminA));
        when(employeeRepository.findByIdAndOrganizationId(200L, 1001L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeRoles(200L, superAdminA);
        });
    }

    @Test
    @DisplayName("Super Admin A searches employees -> Returns only Org A employees")
    public void superAdminASearchesEmployees_onlyOrgA() {
        when(securityContextFacade.getEmail()).thenReturn("adminA@orga.com");
        when(userRepository.findByWorkEmail("adminA@orga.com")).thenReturn(Optional.of(superAdminA));
        when(employeeRepository.findByOrganizationId(1001L)).thenReturn(List.of(empOrgA));
        when(cacheService.searchEmployees(anyString(), any())).thenAnswer(i -> {
            java.util.function.Supplier<List<Employee>> supplier = i.getArgument(1);
            return supplier.get();
        });

        List<Employee> results = employeeService.searchEmployees("Alice");
        assertEquals(1, results.size());
        assertEquals("EMP-A-001", results.get(0).getEmployeeId());
    }

    @Test
    @DisplayName("Super Admin A reads Org B employee status detail -> Returns 404")
    public void superAdminAReadsOrgBStatusDetail_returns404() {
        when(securityContextFacade.getEmail()).thenReturn("adminA@orga.com");
        when(userRepository.findByWorkEmail("adminA@orga.com")).thenReturn(Optional.of(superAdminA));
        when(employeeRepository.findByEmployeeIdAndOrganizationId("EMP-B-001", 1001L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeStatusDetail("EMP-B-001");
        });
    }
}
