package com.example.ems.leave.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.dto.LeaveRequest;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.service.LeaveService;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LeaveControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LeaveService leaveService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LeaveController leaveController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(leaveController).build();
    }

    @Test
    public void testApplyLeaveSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "john.doe@example.com";
        
        User user = new User();
        user.setWorkEmail(email);

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmail(email);

        LeaveRequest request = new LeaveRequest(1L, LocalDate.now(), LocalDate.now().plusDays(2), "Vacation");
        LeaveType type = new LeaveType(1L, "Sick Leave", "Sick description", 10, true);
        Leave leave = new Leave(1L, employee, type, request.getStartDate(), request.getEndDate(), request.getReason(), "PENDING", null, null, null);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(leaveService.applyLeave(any(Employee.class), any(LeaveRequest.class))).thenReturn(leave);

        mockMvc.perform(post("/api/v1/leaves")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Leave request submitted successfully"))
                .andExpect(jsonPath("$.data.reason").value("Vacation"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    public void testGetMyLeavesSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "john.doe@example.com";
        
        User user = new User();
        user.setWorkEmail(email);

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmail(email);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(leaveService.getLeavesByEmployeeId(1L)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/leaves?my=true")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Leave history retrieved successfully"));
    }

    @Test
    public void testDeactivateLeaveTypeSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "admin@example.com";
        User user = new User();
        user.setWorkEmail(email);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(email, "leave.manage")).thenReturn(true);
        when(leaveService.deactivateLeaveType(1L)).thenReturn(new LeaveType());

        mockMvc.perform(patch("/api/v1/leave-types/1/deactivate")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testActivateLeaveTypeSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "admin@example.com";
        User user = new User();
        user.setWorkEmail(email);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(email, "leave.manage")).thenReturn(true);
        when(leaveService.activateLeaveType(1L)).thenReturn(new LeaveType());

        mockMvc.perform(patch("/api/v1/leave-types/1/activate")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
