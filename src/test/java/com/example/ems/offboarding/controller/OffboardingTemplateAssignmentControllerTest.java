package com.example.ems.offboarding.controller;

import com.example.ems.offboarding.dto.EmployeeTemplateAssignmentRequest;
import com.example.ems.offboarding.dto.EmployeeTemplateAssignmentResponse;
import com.example.ems.offboarding.service.OffboardingTemplateAssignmentService;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OffboardingTemplateAssignmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OffboardingTemplateAssignmentService assignmentService;

    @InjectMocks
    private OffboardingTemplateAssignmentController controller;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Long ORG_ID = 100L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        TenantContext.setCurrentTenant(ORG_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("PUT /api/v1/offboarding/template-assignments/employees/25 - Success")
    void testAssignTemplate_Success() throws Exception {
        EmployeeTemplateAssignmentRequest req = new EmployeeTemplateAssignmentRequest(4L, "RESIGNATION");
        EmployeeTemplateAssignmentResponse resp = new EmployeeTemplateAssignmentResponse(
                1L, 25L, "John Doe", "EMP025", 4L, "Standard Resignation Template", "RESIGNATION",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(assignmentService.assignTemplateToEmployee(eq(25L), any(EmployeeTemplateAssignmentRequest.class)))
                .thenReturn(resp);

        mockMvc.perform(put("/api/v1/offboarding/template-assignments/employees/25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(25))
                .andExpect(jsonPath("$.data.templateId").value(4))
                .andExpect(jsonPath("$.data.exitType").value("RESIGNATION"));
    }

    @Test
    @DisplayName("GET /api/v1/offboarding/template-assignments/employees/25 - Success")
    void testGetAssignments_Success() throws Exception {
        EmployeeTemplateAssignmentResponse resp = new EmployeeTemplateAssignmentResponse(
                1L, 25L, "John Doe", "EMP025", 4L, "Standard Resignation Template", "RESIGNATION",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(assignmentService.getAssignments(25L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/offboarding/template-assignments/employees/25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].employeeId").value(25))
                .andExpect(jsonPath("$.data[0].templateName").value("Standard Resignation Template"));
    }

    @Test
    @DisplayName("DELETE /api/v1/offboarding/template-assignments/employees/25?exitType=RESIGNATION - Success")
    void testRemoveAssignment_Success() throws Exception {
        doNothing().when(assignmentService).removeAssignment(25L, "RESIGNATION");

        mockMvc.perform(delete("/api/v1/offboarding/template-assignments/employees/25")
                        .param("exitType", "RESIGNATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(assignmentService, times(1)).removeAssignment(25L, "RESIGNATION");
    }
}
