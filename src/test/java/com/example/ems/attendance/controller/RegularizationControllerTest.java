package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.AttendanceRegularizationRequest;
import com.example.ems.attendance.dto.RegularizationProcessRequest;
import com.example.ems.attendance.entity.AttendanceRegularization;
import com.example.ems.attendance.service.AttendanceRegularizationService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RegularizationControllerTest {

        private MockMvc mockMvc;

        @Mock
        private AttendanceRegularizationService regularizationService;

        @Mock
        private UserRepository userRepository;

        @Mock
        private EmployeeRepository employeeRepository;

        @Mock
        private RoleService roleService;

        @Mock
        private JwtService jwtService;

        @InjectMocks
        private RegularizationController regularizationController;

        private ObjectMapper objectMapper;

        private static final String TOKEN = "mock-token";
        private static final String AUTH_HEADER = "Bearer " + TOKEN;
        private static final String EMAIL = "manager@example.com";

        private User currentUser;
        private Employee employee;

        @BeforeEach
        public void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(regularizationController).build();
                objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                currentUser = new User();
                currentUser.setWorkEmail(EMAIL);

                employee = new Employee();
                employee.setId(1L);
                employee.setEmail(EMAIL);
                employee.setFullName("Jane Doe");

                when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
                when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
                when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(currentUser));
                when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        }

        @Test
        public void testSubmitRegularizationSuccess() throws Exception {
                AttendanceRegularizationRequest request = new AttendanceRegularizationRequest();
                request.setEmployeeId(1L);
                request.setDate(LocalDate.now());
                request.setProposedPunchInTime(LocalTime.of(9, 0));
                request.setProposedPunchOutTime(LocalTime.of(17, 0));
                request.setReason("Forgot to punch");

                AttendanceRegularization regularization = new AttendanceRegularization();
                regularization.setId(100L);
                regularization.setEmployee(employee);
                regularization.setDate(request.getDate());
                regularization.setProposedPunchInTime(request.getProposedPunchInTime());
                regularization.setProposedPunchOutTime(request.getProposedPunchOutTime());
                regularization.setReason(request.getReason());
                regularization.setStatus("PENDING");

                when(regularizationService.submitRegularization(eq(1L), any(LocalDate.class), any(LocalTime.class),
                                any(LocalTime.class), eq("Forgot to punch")))
                                .thenReturn(regularization);

                mockMvc.perform(post("/api/v1/attendance/regularization")
                                .header("Authorization", AUTH_HEADER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Regularization request submitted successfully"))
                                .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        public void testGetRegularizationsSuccess() throws Exception {
                AttendanceRegularization regularization = new AttendanceRegularization();
                regularization.setId(100L);
                regularization.setEmployee(employee);
                regularization.setStatus("PENDING");

                when(roleService.hasPermission(EMAIL, "attendance.read")).thenReturn(true);
                when(regularizationService.getRegularizations("PENDING")).thenReturn(List.of(regularization));

                mockMvc.perform(get("/api/v1/attendance/regularization")
                                .header("Authorization", AUTH_HEADER)
                                .param("status", "PENDING"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
        }

        @Test
        public void testApproveRegularizationSuccess() throws Exception {
                RegularizationProcessRequest processRequest = new RegularizationProcessRequest();
                processRequest.setCorrectedPunchInTime(LocalTime.of(8, 45));
                processRequest.setCorrectedPunchOutTime(LocalTime.of(17, 15));
                processRequest.setManagerNotes("Approved with minor correction");

                AttendanceRegularization regularization = new AttendanceRegularization();
                regularization.setId(100L);
                regularization.setStatus("APPROVED");
                regularization.setManagerNotes("Approved with minor correction");

                when(roleService.hasPermission(EMAIL, "attendance.manage")).thenReturn(true);
                when(regularizationService.approveRegularization(eq(100L), any(RegularizationProcessRequest.class)))
                                .thenReturn(regularization);

                mockMvc.perform(patch("/api/v1/attendance/regularization/100/approve")
                                .header("Authorization", AUTH_HEADER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(processRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Regularization request approved successfully"))
                                .andExpect(jsonPath("$.data.managerNotes").value("Approved with minor correction"));
        }

        @Test
        public void testRejectRegularizationSuccess() throws Exception {
                RegularizationProcessRequest processRequest = new RegularizationProcessRequest();
                processRequest.setManagerNotes("Rejecting: Incorrect details");

                AttendanceRegularization regularization = new AttendanceRegularization();
                regularization.setId(100L);
                regularization.setStatus("REJECTED");
                regularization.setManagerNotes("Rejecting: Incorrect details");

                when(roleService.hasPermission(EMAIL, "attendance.manage")).thenReturn(true);
                when(regularizationService.rejectRegularization(eq(100L), any(RegularizationProcessRequest.class)))
                                .thenReturn(regularization);

                mockMvc.perform(patch("/api/v1/attendance/regularization/100/reject")
                                .header("Authorization", AUTH_HEADER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(processRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.status").value("REJECTED"));
        }
}
