package com.example.ems.employee.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.dto.DepartmentRequest;
import com.example.ems.employee.dto.DepartmentTransferRequest;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.DepartmentTransfer;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.service.DepartmentService;
import com.example.ems.employee.service.EmployeeService;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DepartmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private DepartmentController departmentController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "admin@example.com";
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(departmentController).build();

        // Configure standard mock behavior
        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        User user = new User();
        user.setWorkEmail(EMAIL);
        user.setFullName("System Admin");
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));

        when(roleService.hasPermission(EMAIL, "employee.create")).thenReturn(true);
        when(roleService.hasPermission(EMAIL, "employee.update")).thenReturn(true);
        when(roleService.hasPermission(EMAIL, "department.manage")).thenReturn(true);
    }

    @Test
    public void testGetAllDepartments() throws Exception {
        Department d = new Department(1L, "Engineering", "ENG", "Dev");
        when(departmentService.getAllDepartments()).thenReturn(List.of(d));

        mockMvc.perform(get("/api/v1/departments")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Engineering"));
    }

    @Test
    public void testGetDepartmentById() throws Exception {
        Department d = new Department(1L, "Engineering", "ENG", "Dev");
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(d));

        mockMvc.perform(get("/api/v1/departments/1")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Engineering"));
    }

    @Test
    public void testCreateDepartment() throws Exception {
        DepartmentRequest req = new DepartmentRequest("Engineering", "ENG", "Dev", null, null, BigDecimal.valueOf(50000), "ACTIVE");
        Department d = new Department(1L, "Engineering", "ENG", "Dev", null, null, BigDecimal.valueOf(50000), "ACTIVE");
        when(departmentService.createDepartment(any(DepartmentRequest.class))).thenReturn(d);

        mockMvc.perform(post("/api/v1/departments")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Engineering"));
    }

    @Test
    public void testUpdateDepartment() throws Exception {
        DepartmentRequest req = new DepartmentRequest("Engineering", "ENG", "Dev", null, null, BigDecimal.valueOf(50000), "ACTIVE");
        Department d = new Department(1L, "Engineering", "ENG", "Dev", null, null, BigDecimal.valueOf(50000), "ACTIVE");
        when(departmentService.updateDepartment(eq(1L), any(DepartmentRequest.class))).thenReturn(d);

        mockMvc.perform(put("/api/v1/departments/1")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Engineering"));
    }

    @Test
    public void testDeleteDepartment() throws Exception {
        when(departmentService.deleteDepartment(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/departments/1")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetDepartmentsDropdown() throws Exception {
        Department d = new Department(1L, "Engineering", "ENG", "Dev");
        when(departmentService.getAllDepartments()).thenReturn(List.of(d));

        mockMvc.perform(get("/api/v1/departments/dropdown")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Engineering"));
    }

    @Test
    public void testGetHierarchy() throws Exception {
        Map<String, Object> node = Map.of("id", 1L, "name", "Engineering", "children", List.of());
        when(departmentService.getHierarchy()).thenReturn(List.of(node));

        mockMvc.perform(get("/api/v1/departments/hierarchy")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Engineering"));
    }

    @Test
    public void testGetDashboard() throws Exception {
        Map<String, Object> dashboard = Map.of("totalDepartments", 12L, "activeDepartments", 10L);
        when(departmentService.getDashboard()).thenReturn(dashboard);

        mockMvc.perform(get("/api/v1/departments/dashboard")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDepartments").value(12));
    }

    @Test
    public void testGetEmployeesByDepartment() throws Exception {
        Department d = new Department(1L, "Engineering", "ENG", "Dev");
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(d));
        Employee emp = new Employee();
        emp.setFullName("John Doe");
        emp.setStatus("ACTIVE");
        when(employeeService.getEmployeesByDepartment("Engineering")).thenReturn(List.of(emp));

        mockMvc.perform(get("/api/v1/departments/1/employees")
                        .header("Authorization", AUTH_HEADER)
                        .param("page", "0")
                        .param("size", "20")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].fullName").value("John Doe"));
    }

    @Test
    public void testGetManager() throws Exception {
        Employee manager = new Employee();
        manager.setId(15L);
        manager.setFullName("Rajan Kumar");
        manager.setEmail("rajan@example.com");
        manager.setDesignation("Manager");
        when(departmentService.getManager(1L)).thenReturn(Optional.of(manager));

        mockMvc.perform(get("/api/v1/departments/1/manager")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Rajan Kumar"));
    }

    @Test
    public void testUpdateManager() throws Exception {
        Department d = new Department(1L, "Engineering", "ENG", "Dev");
        when(departmentService.updateManager(1L, 15L)).thenReturn(d);

        mockMvc.perform(put("/api/v1/departments/1/manager")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("managerId", 15L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testTransferEmployee() throws Exception {
        DepartmentTransfer transfer = new DepartmentTransfer(25L, 1L, 3L, LocalDate.now(), "Project Allocation");
        when(departmentService.transferEmployee(eq(25L), eq(1L), eq(3L), any(LocalDate.class), any(String.class))).thenReturn(transfer);

        DepartmentTransferRequest req = new DepartmentTransferRequest(25L, 1L, 3L, LocalDate.now(), "Project Allocation");

        mockMvc.perform(post("/api/v1/departments/transfers")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(25));
    }

    @Test
    public void testGetAllTransfers() throws Exception {
        DepartmentTransfer transfer = new DepartmentTransfer(25L, 1L, 3L, LocalDate.now(), "Project Allocation");
        when(departmentService.getAllTransfers()).thenReturn(List.of(transfer));

        mockMvc.perform(get("/api/v1/departments/transfers")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].employeeId").value(25));
    }

    @Test
    public void testGetEmployeeDistribution() throws Exception {
        when(departmentService.getEmployeeDistribution()).thenReturn(List.of(Map.of("departmentName", "Engineering", "employeeCount", 5)));

        mockMvc.perform(get("/api/v1/departments/analytics/employee-distribution")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].employeeCount").value(5));
    }

    @Test
    public void testGetBudgetDistribution() throws Exception {
        when(departmentService.getBudgetDistribution()).thenReturn(List.of(Map.of("departmentName", "Engineering", "budget", 50000)));

        mockMvc.perform(get("/api/v1/departments/analytics/budget-distribution")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetGrowth() throws Exception {
        when(departmentService.getGrowth()).thenReturn(Map.of("labels", List.of("Q1"), "growthRates", List.of(5.2)));

        mockMvc.perform(get("/api/v1/departments/analytics/growth")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetHeadcountTrend() throws Exception {
        when(departmentService.getHeadcountTrend()).thenReturn(Map.of("labels", List.of("Jan"), "values", List.of(200)));

        mockMvc.perform(get("/api/v1/departments/analytics/headcount-trend")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetCostCenter() throws Exception {
        when(departmentService.getCostCenter(1L)).thenReturn(Map.of("departmentId", 1L, "costCenter", "CC-ENG"));

        mockMvc.perform(get("/api/v1/departments/1/cost-center")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.costCenter").value("CC-ENG"));
    }

    @Test
    public void testUpdateCostCenter() throws Exception {
        Department d = new Department(1L, "Engineering", "ENG", "Dev");
        d.setCostCenter("CC-ENG-NEW");
        when(departmentService.updateCostCenter(1L, "CC-ENG-NEW")).thenReturn(d);

        mockMvc.perform(put("/api/v1/departments/1/cost-center")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("costCenter", "CC-ENG-NEW"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetBudget() throws Exception {
        when(departmentService.getBudget(1L)).thenReturn(Map.of("allocated", 1000, "utilized", 500, "remaining", 500));

        mockMvc.perform(get("/api/v1/departments/1/budget")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.remaining").value(500));
    }

    @Test
    public void testUpdateBudget() throws Exception {
        Department d = new Department(1L, "Engineering", "ENG", "Dev");
        when(departmentService.updateBudgetFields(eq(1L), any(BigDecimal.class), any(BigDecimal.class))).thenReturn(d);

        mockMvc.perform(put("/api/v1/departments/1/budget")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("allocated", 1000, "utilized", 500))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetReports() throws Exception {
        when(departmentService.getHeadcountReport()).thenReturn(List.of(Map.of("department", "Engineering", "headcount", 5)));
        when(departmentService.getBudgetUtilizationReport()).thenReturn(List.of(Map.of("department", "Engineering", "allocated", 1000)));
        when(departmentService.getEmployeeAllocationReport()).thenReturn(List.of(Map.of("employeeName", "John Doe", "departmentName", "Engineering")));
        when(departmentService.getPerformanceSummaryReport()).thenReturn(List.of(Map.of("department", "Engineering", "averageRating", 4.2)));

        mockMvc.perform(get("/api/v1/departments/reports/headcount")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/departments/reports/budget-utilization")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/departments/reports/employee-allocation")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/departments/reports/performance-summary")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
