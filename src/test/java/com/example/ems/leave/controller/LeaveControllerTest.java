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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

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
        Leave leave = new Leave(1L, employee, type, request.getStartDate(), request.getEndDate(), request.getReason(),
                "PENDING", null, null, null);

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

    @Test
    public void testApproveLeaveSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "manager@example.com";
        User user = new User();
        user.setWorkEmail(email);

        Employee approver = new Employee();
        approver.setId(2L);
        approver.setEmail(email);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(email, "leave.approve")).thenReturn(true);
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(approver));

        Leave leave = new Leave();
        leave.setId(1L);
        leave.setStatus("APPROVED");

        when(leaveService.approveLeave(any(Long.class), any(Employee.class))).thenReturn(leave);

        mockMvc.perform(patch("/api/v1/leaves/1/approve")
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

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(email, "leave.approve")).thenReturn(false);
        when(roleService.hasPermission(email, "leave.team.approve")).thenReturn(true);
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(approver));

        Leave leave = new Leave();
        leave.setId(1L);
        leave.setStatus("REJECTED");

        when(leaveService.rejectLeave(any(Long.class), any(Employee.class))).thenReturn(leave);

        mockMvc.perform(patch("/api/v1/leaves/1/reject")
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
        when(leaveService.getLeavesByEmployeeId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/leaves/my-requests")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("My leave requests retrieved successfully"));
    }

    @Test
    public void testGetManagerLeaveApprovalsSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "manager@example.com";

        User user = new User();
        user.setWorkEmail(email);

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setEmail(email);

        Page<LeaveApprovalResponseDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(manager));
        when(leaveService.getManagerLeaveApprovals(any(Employee.class), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/manager/leave-approvals")
                .header("Authorization", token)
                .param("page", "0")
                .param("size", "10")
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Manager leave approvals retrieved successfully"));
    }

    @Test
    public void testGetManagerLeaveApprovalDetailsSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "manager@example.com";

        User user = new User();
        user.setWorkEmail(email);

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setEmail(email);

        LeaveApprovalResponseDto details = new LeaveApprovalResponseDto(101L, 25L, "EMP025", "Priya Sharma", "Engineering", "Annual Leave", LocalDate.now(), LocalDate.now().plusDays(2), 3L, "Vacation", java.time.LocalDateTime.now(), "PENDING");

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(manager));
        when(leaveService.getManagerLeaveApprovalDetails(101L, manager)).thenReturn(details);

        mockMvc.perform(get("/api/v1/manager/leave-approvals/101")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.leaveId").value(101))
                .andExpect(jsonPath("$.data.employeeName").value("Priya Sharma"));
    }

    @Test
    public void testGetLeaveApprovalSummarySuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "manager@example.com";

        User user = new User();
        user.setWorkEmail(email);

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setEmail(email);

        LeaveApprovalSummaryDto summary = new LeaveApprovalSummaryDto(5L, 2L, 1L);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(manager));
        when(leaveService.getLeaveApprovalSummary(manager)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/manager/leave-approvals/summary")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pending").value(5))
                .andExpect(jsonPath("$.data.approvedToday").value(2));
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
        actionResp.setApprovedAt(java.time.LocalDateTime.now());

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(manager));
        when(leaveService.approveLeaveWithComment(101L, "Approved request", manager)).thenReturn(actionResp);

        mockMvc.perform(post("/api/v1/manager/leave-approvals/101/approve")
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
        actionResp.setRejectedAt(java.time.LocalDateTime.now());

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(manager));
        when(leaveService.rejectLeaveWithComment(101L, "Rejected request", manager)).thenReturn(actionResp);

        mockMvc.perform(post("/api/v1/manager/leave-approvals/101/reject")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.leaveId").value(101))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    public void testBulkApproveSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "manager@example.com";

        User user = new User();
        user.setWorkEmail(email);

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setEmail(email);

        BulkApprovalRequest bulkReq = new BulkApprovalRequest();
        bulkReq.setLeaveIds(List.of(101L, 102L));
        bulkReq.setComment("Bulk approved");

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(manager));

        mockMvc.perform(post("/api/v1/manager/leave-approvals/bulk-approve")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bulk approval successful"));
    }

    @Test
    public void testBulkRejectSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "manager@example.com";

        User user = new User();
        user.setWorkEmail(email);

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setEmail(email);

        BulkApprovalRequest bulkReq = new BulkApprovalRequest();
        bulkReq.setLeaveIds(List.of(101L, 102L));
        bulkReq.setComment("Bulk rejected");

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(manager));

        mockMvc.perform(post("/api/v1/manager/leave-approvals/bulk-reject")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bulk rejection successful"));
    }
}
