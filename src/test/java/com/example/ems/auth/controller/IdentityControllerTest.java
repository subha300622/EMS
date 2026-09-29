package com.example.ems.auth.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IdentityControllerTest {

        private MockMvc mockMvcAuth;

        @Mock
        private RoleService roleService;
        @Mock
        private UserRepository userRepository;
        @Mock
        private JwtService jwtService;

        @InjectMocks
        private AuthController authController;

        @BeforeEach
        public void setUp() {
                MockitoAnnotations.openMocks(this);
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
