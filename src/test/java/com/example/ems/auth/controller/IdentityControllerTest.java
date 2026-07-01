package com.example.ems.auth.controller;

import com.example.ems.auth.dto.BootstrapResponse;
import com.example.ems.auth.dto.LoginResponse;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.BootstrapService;
import com.example.ems.auth.service.RoleService;
import com.example.ems.auth.service.UserService;
import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IdentityControllerTest {

        private MockMvc mockMvcUser;
        private MockMvc mockMvcAuth;

        @Mock
        private UserService userService;
        @Mock
        private RoleService roleService;
        @Mock
        private BootstrapService bootstrapService;
        @Mock
        private UserRepository userRepository;
        @Mock
        private JwtService jwtService;

        @InjectMocks
        private UserController userController;
        @InjectMocks
        private AuthController authController;

        @BeforeEach
        public void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvcUser = MockMvcBuilders.standaloneSetup(userController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
                mockMvcAuth = MockMvcBuilders.standaloneSetup(authController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();

                // Standard auth mock setup
                when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
                when(jwtService.getEmailFromToken("mock-token")).thenReturn("test@company.com");

                User mockUser = new User();
                mockUser.setId(1L);
                mockUser.setUserId("EMP001");
                mockUser.setFullName("Test User");
                mockUser.setWorkEmail("test@company.com");
                when(userRepository.findByWorkEmail("test@company.com")).thenReturn(Optional.of(mockUser));
        }

        @Test
        public void testGetUserProfile() throws Exception {
                BootstrapResponse.UserProfileResponse mockProfile = new BootstrapResponse.UserProfileResponse(
                                1L, "EMP001", "Test User", "test@company.com", "EMPLOYEE", "https://avatar.url", false,
                                false, "ACTIVE", "now");
                when(userService.getUserProfile(any())).thenReturn(mockProfile);

                mockMvcUser.perform(get("/api/v1/users/me/profile")
                                .header("Authorization", "Bearer mock-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.employeeId").value("EMP001"))
                                .andExpect(jsonPath("$.data.profileImage").value("https://avatar.url"));
        }

        @Test
        public void testGetUserContext() throws Exception {
                BootstrapResponse.OrgContextResponse mockContext = new BootstrapResponse.OrgContextResponse(
                                10L, 5L, new LoginResponse.BranchContext(null, false));
                when(userService.getUserContext(any())).thenReturn(mockContext);

                mockMvcUser.perform(get("/api/v1/users/me/context")
                                .header("Authorization", "Bearer mock-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.companyId").value(10))
                                .andExpect(jsonPath("$.data.departmentId").value(5))
                                .andExpect(jsonPath("$.data.branch.enabled").value(false));
        }

        @Test
        public void testGetBootstrapData() throws Exception {
                BootstrapResponse.UserProfileResponse mockProfile = new BootstrapResponse.UserProfileResponse(
                                1L, "EMP001", "Test User", "test@company.com", "EMPLOYEE", "https://avatar.url", false,
                                false, "ACTIVE", "now");
                BootstrapResponse.OrgContextResponse mockContext = new BootstrapResponse.OrgContextResponse(
                                10L, 5L, new LoginResponse.BranchContext(null, false));
                BootstrapResponse.AuthDataResponse mockAuth = new BootstrapResponse.AuthDataResponse(
                                Collections.singletonList("attendance.read"));

                BootstrapResponse mockBootstrap = new BootstrapResponse(mockProfile, mockAuth, mockContext);
                when(bootstrapService.getBootstrapData(any())).thenReturn(mockBootstrap);

                mockMvcUser.perform(get("/api/v1/users/me/bootstrap")
                                .header("Authorization", "Bearer mock-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.user.employeeId").value("EMP001"))
                                .andExpect(jsonPath("$.data.auth.permissions[0]").value("attendance.read"))
                                .andExpect(jsonPath("$.data.org.companyId").value(10));
        }

        @Test
        public void testGetPermissions() throws Exception {
                when(roleService.getPermissionsForUserId("EMP001"))
                                .thenReturn(Collections.singletonList("attendance.read"));

                mockMvcAuth.perform(get("/api/v1/auth/permissions")
                                .header("Authorization", "Bearer mock-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0]").value("attendance.read"));
        }
}
