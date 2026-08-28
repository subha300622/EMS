package com.example.ems.leave.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.dto.*;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        Leave leave = new Leave(1L, employee, type, request.getStartDate(), request.getEndDate(), request.getReason(),
                "PENDING", null, null, null);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(leaveService.applyLeave(any(Employee.class), any(LeaveRequest.class))).thenReturn(leave);

        mockMvc.perform(post("/api/v1/leave/requests")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Leave request submitted successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
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
        when(leaveService.getLeaves(any(), eq(1L), any(), any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/leave/requests?mine=true")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Leave requests retrieved successfully"));
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
        when(leaveService.toggleLeaveTypeStatus(1L, false)).thenReturn(new LeaveType());

        mockMvc.perform(patch("/api/v1/leave/types/1/status?active=false")
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
        when(leaveService.toggleLeaveTypeStatus(1L, true)).thenReturn(new LeaveType());

        mockMvc.perform(patch("/api/v1/leave/types/1/status?active=true")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testApproveLeaveSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "manager@example.com";
        User user = new User();
        user.setWorkEmail(email);

        Employee approver = new Employee();
        approver.setId(2L);
        approver.setEmail(email);

        ManagerApprovalActionResponseDto actionResp = new ManagerApprovalActionResponseDto();
        actionResp.setLeaveId(1L);
        actionResp.setStatus("APPROVED");

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(approver));
        when(leaveService.approveLeaveWithComment(eq(1L), any(), any(Employee.class))).thenReturn(actionResp);

        mockMvc.perform(post("/api/v1/leave/requests/1/approve")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Leave request approved successfully"))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    public void testRejectLeaveSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "manager@example.com";
        User user = new User();
        user.setWorkEmail(email);

        Employee approver = new Employee();
        approver.setId(2L);
        approver.setEmail(email);

        ManagerApprovalActionResponseDto actionResp = new ManagerApprovalActionResponseDto();
        actionResp.setLeaveId(1L);
        actionResp.setStatus("REJECTED");

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(approver));
        when(leaveService.rejectLeaveWithComment(eq(1L), any(), any(Employee.class))).thenReturn(actionResp);

        mockMvc.perform(post("/api/v1/leave/requests/1/reject")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Leave request rejected successfully"))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    public void testGetMyLeaveRequestsSuccess() throws Exception {
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
        when(leaveService.getLeaves(any(), eq(1L), any(), any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/leave/requests?mine=true")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Leave requests retrieved successfully"));
    }

    @Test
    public void testApproveLeaveWithCommentSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "manager@example.com";

        User user = new User();
        user.setWorkEmail(email);

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setEmail(email);

        ManagerCommentRequest commentReq = new ManagerCommentRequest();
        commentReq.setComment("Approved request");

        ManagerApprovalActionResponseDto actionResp = new ManagerApprovalActionResponseDto();
        actionResp.setLeaveId(101L);
        actionResp.setStatus("APPROVED");
        actionResp.setApprovedBy(2L);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(manager));
        when(leaveService.approveLeaveWithComment(eq(101L), eq("Approved request"), any(Employee.class))).thenReturn(actionResp);

        mockMvc.perform(post("/api/v1/leave/requests/101/approve")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.leaveId").value(101))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    public void testRejectLeaveWithCommentSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "manager@example.com";

        User user = new User();
        user.setWorkEmail(email);

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setEmail(email);

        ManagerCommentRequest commentReq = new ManagerCommentRequest();
        commentReq.setComment("Rejected request");

        ManagerApprovalActionResponseDto actionResp = new ManagerApprovalActionResponseDto();
        actionResp.setLeaveId(101L);
        actionResp.setStatus("REJECTED");
        actionResp.setRejectedBy(2L);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(manager));
        when(leaveService.rejectLeaveWithComment(eq(101L), eq("Rejected request"), any(Employee.class))).thenReturn(actionResp);

        mockMvc.perform(post("/api/v1/leave/requests/101/reject")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.leaveId").value(101))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }
}
