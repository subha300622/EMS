package com.example.ems.employee.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.dto.DepartmentCreateRequest;
import com.example.ems.employee.dto.DepartmentResponseDto;
import com.example.ems.employee.dto.DepartmentUpdateRequest;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.DepartmentAuditLog;
import com.example.ems.employee.service.DepartmentService;
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
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private com.example.ems.auth.service.RoleService roleService;

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
        when(roleService.hasPermission(any(), any())).thenReturn(true);
    }

    @Test
    public void testCreateDepartment() throws Exception {
        Department d = new Department(1L, "Engineering", "ENG", "Dev");
        when(departmentService.createDepartment(any(DepartmentCreateRequest.class), any(User.class))).thenReturn(d);

        DepartmentCreateRequest req = new DepartmentCreateRequest();
        req.setName("Engineering");
        req.setCode("ENG");
        req.setHead("admin@example.com");
        req.setParentDepartment("None");
        req.setDescription("Dev");

        mockMvc.perform(post("/api/v1/departments")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Engineering"));
    }

    @Test
    public void testGetDepartmentsList() throws Exception {
        DepartmentResponseDto dto = new DepartmentResponseDto();
        dto.setId("1");
        dto.setName("Engineering");
        dto.setCode("ENG");
        dto.setStatus("Active");
        dto.setEmployeeCount(5);

        when(departmentService.getDepartmentsList()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/departments")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Engineering"))
                .andExpect(jsonPath("$.data[0].employeeCount").value(5));
    }

    @Test
    public void testGetDepartmentDetails() throws Exception {
        DepartmentResponseDto dto = new DepartmentResponseDto();
        dto.setId("1");
        dto.setName("Engineering");
        dto.setCode("ENG");
        dto.setStatus("Active");
        dto.setEmployeeCount(5);
        dto.setDescription("Dev");

        when(departmentService.getDepartmentDetails(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/departments/1")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.description").value("Dev"));
    }

    @Test
    public void testDeleteDepartment() throws Exception {
        when(departmentService.deleteDepartment(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/departments/1")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    public void testToggleStatus() throws Exception {
        Department d = new Department(1L, "Engineering", "ENG", "Dev");
        d.setStatus("INACTIVE");
        when(departmentService.toggleDepartmentStatus(1L, "Inactive")).thenReturn(d);

        Map<String, String> statusMap = Map.of("status", "Inactive");

        mockMvc.perform(patch("/api/v1/departments/1")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("Inactive"));
    }

    @Test
    public void testUpdateDepartment() throws Exception {
        DepartmentResponseDto dto = new DepartmentResponseDto();
        dto.setId("1");
        dto.setName("Sales & Support");
        dto.setCode("SLS");
        dto.setDescription("Customer relations");

        when(departmentService.updateDepartment(eq(1L), any(DepartmentUpdateRequest.class), any(User.class))).thenReturn(dto);

        DepartmentUpdateRequest req = new DepartmentUpdateRequest();
        req.setName("Sales & Support");
        req.setCode("SLS");
        req.setDescription("Customer relations");

        mockMvc.perform(put("/api/v1/departments/1")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Sales & Support"));
    }

    @Test
    public void testGetHistory() throws Exception {
        DepartmentAuditLog log = new DepartmentAuditLog(1L, "Department Head", "John Smith", "Sarah Connor",
                1L, "Admin", "Super Admin", "Sarah Connor assigned as new head");

        when(departmentService.getDepartmentHistory(1L)).thenReturn(List.of(log));

        mockMvc.perform(get("/api/v1/departments/1/history")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].field").value("Department Head"))
                .andExpect(jsonPath("$.data[0].newValue").value("Sarah Connor"));
    }
}
