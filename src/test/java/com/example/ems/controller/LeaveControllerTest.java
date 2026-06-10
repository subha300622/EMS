package com.example.ems.controller;

import com.example.ems.dto.LeaveRequest;
import com.example.ems.entity.Employee;
import com.example.ems.entity.Leave;
import com.example.ems.entity.LeaveType;
import com.example.ems.entity.User;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.JwtService;
import com.example.ems.service.LeaveService;
import com.example.ems.service.RoleService;
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
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        mockMvc.perform(post("/api/leaves")
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
    public void testGetMyLeaveBalance() throws Exception {
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
        when(leaveService.getLeaveBalance(1L)).thenReturn(Map.of("Sick Leave", Map.of("total", 10, "used", 2, "remaining", 8)));

        mockMvc.perform(get("/api/leaves/my/balance")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data['Sick Leave'].total").value(10))
                .andExpect(jsonPath("$.data['Sick Leave'].used").value(2))
                .andExpect(jsonPath("$.data['Sick Leave'].remaining").value(8));
    }
}
