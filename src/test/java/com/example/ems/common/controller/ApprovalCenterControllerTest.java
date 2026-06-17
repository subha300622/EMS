package com.example.ems.common.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApprovalItemDto;
import com.example.ems.common.service.ApprovalCenterService;
import com.example.ems.security.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApprovalCenterControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ApprovalCenterService approvalCenterService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ApprovalCenterController approvalCenterController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "manager@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(approvalCenterController).build();

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        User user = new User();
        user.setWorkEmail(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    private void mockPermission(String permission, boolean allowed) {
        when(roleService.hasPermission(EMAIL, permission)).thenReturn(allowed);
    }

    @Test
    public void testGetPendingApprovalsSuccess() throws Exception {
        mockPermission("team.read", true);
        ApprovalItemDto item = new ApprovalItemDto("LEAVE-1", "LEAVE", "John Doe", "Sick Leave", "2026-06-17 10:00:00", "PENDING");
        when(approvalCenterService.getPendingApprovals()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/approvals")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("LEAVE-1"))
                .andExpect(jsonPath("$.data[0].requesterName").value("John Doe"));
    }

    @Test
    public void testApproveItemSuccess() throws Exception {
        mockPermission("team.read", true);
        doNothing().when(approvalCenterService).approveItem("LEAVE-1", EMAIL);

        mockMvc.perform(patch("/api/v1/approvals/LEAVE-1/approve")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item approved successfully"));
    }

    @Test
    public void testRejectItemSuccess() throws Exception {
        mockPermission("team.read", true);
        doNothing().when(approvalCenterService).rejectItem("LEAVE-1", EMAIL);

        mockMvc.perform(patch("/api/v1/approvals/LEAVE-1/reject")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item rejected successfully"));
    }
}
