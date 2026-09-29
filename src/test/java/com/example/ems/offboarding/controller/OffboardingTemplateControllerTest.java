package com.example.ems.offboarding.controller;

import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.enums.*;
import com.example.ems.offboarding.service.OffboardingTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class OffboardingTemplateControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OffboardingTemplateService templateService;

    @InjectMocks
    private OffboardingTemplateController templateController;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(templateController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testCreateTemplate() throws Exception {
        OffboardingTemplateRequest req = new OffboardingTemplateRequest();
        req.setName("Standard Exit Template");
        req.setDescription("Standard exit process");
        req.setNoticePeriodDefaultDays(30);

        OffboardingTemplateResponse resp = new OffboardingTemplateResponse();
        resp.setId(1L);
        resp.setOrganizationId(100L);
        resp.setName("Standard Exit Template");
        resp.setStatus(OffboardingTemplateStatus.ACTIVE);

        when(templateService.createTemplate(any(OffboardingTemplateRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/offboarding/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Standard Exit Template"));
    }

    @Test
    void testGetTemplateDetails() throws Exception {
        OffboardingTemplateDetailResponse resp = new OffboardingTemplateDetailResponse();
        resp.setId(1L);
        resp.setName("Standard Exit Template");
        resp.setClearanceTasks(Collections.emptyList());
        resp.setAssetRequirements(Collections.emptyList());
        resp.setDocumentRequirements(Collections.emptyList());

        when(templateService.getTemplateDetails(1L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/offboarding/templates/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Standard Exit Template"));
    }

    @Test
    void testListTemplates() throws Exception {
        OffboardingTemplateResponse resp = new OffboardingTemplateResponse();
        resp.setId(1L);
        resp.setName("Standard Exit Template");

        when(templateService.listTemplates(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(resp), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/offboarding/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void testAddClearanceTask() throws Exception {
        ClearanceTaskTemplateRequest req = new ClearanceTaskTemplateRequest();
        req.setTaskName("Collect Keycard");
        req.setAssignToType(ClearanceAssignToType.ADMIN);

        ClearanceTaskTemplateResponse resp = new ClearanceTaskTemplateResponse();
        resp.setId(10L);
        resp.setTemplateId(1L);
        resp.setTaskName("Collect Keycard");
        resp.setAssignToType(ClearanceAssignToType.ADMIN);

        when(templateService.addClearanceTask(eq(1L), any(ClearanceTaskTemplateRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/offboarding/templates/1/clearance-tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.taskName").value("Collect Keycard"));
    }

    @Test
    void testAddAssetRequirement() throws Exception {
        AssetRequirementTemplateRequest req = new AssetRequirementTemplateRequest();
        req.setName("Company Laptop");
        req.setAssetType(OffboardingAssetType.LAPTOP);
        req.setAssignedToType(ClearanceAssignToType.IT_MANAGER);

        AssetRequirementTemplateResponse resp = new AssetRequirementTemplateResponse();
        resp.setId(20L);
        resp.setTemplateId(1L);
        resp.setName("Company Laptop");
        resp.setAssetType(OffboardingAssetType.LAPTOP);

        when(templateService.addAssetRequirement(eq(1L), any(AssetRequirementTemplateRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/offboarding/templates/1/asset-requirements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(20))
                .andExpect(jsonPath("$.data.name").value("Company Laptop"));
    }

    @Test
    void testConfigureKtTemplate() throws Exception {
        KtTemplateRequest req = new KtTemplateRequest();
        req.setTitle("Project Handover");
        req.setAssignToType(ClearanceAssignToType.REPORTING_MANAGER);

        KtTemplateResponse resp = new KtTemplateResponse();
        resp.setId(30L);
        resp.setTemplateId(1L);
        resp.setTitle("Project Handover");

        when(templateService.configureKtTemplate(eq(1L), any(KtTemplateRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/offboarding/templates/1/kt-requirements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(30))
                .andExpect(jsonPath("$.data.title").value("Project Handover"));
    }

    @Test
    void testConfigureInterviewTemplate() throws Exception {
        InterviewTemplateRequest req = new InterviewTemplateRequest();
        req.setConductedByType(ClearanceAssignToType.HR_MANAGER);

        InterviewTemplateResponse resp = new InterviewTemplateResponse();
        resp.setId(40L);
        resp.setTemplateId(1L);
        resp.setConductedByType(ClearanceAssignToType.HR_MANAGER);

        when(templateService.configureInterviewTemplate(eq(1L), any(InterviewTemplateRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/offboarding/templates/1/exit-interview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(40));
    }

    @Test
    void testActivateTemplate() throws Exception {
        OffboardingTemplateResponse resp = new OffboardingTemplateResponse();
        resp.setId(1L);
        resp.setName("Standard Exit Template");
        resp.setStatus(OffboardingTemplateStatus.ACTIVE);

        when(templateService.activateTemplate(1L)).thenReturn(resp);

        mockMvc.perform(put("/api/v1/offboarding/templates/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void testDeactivateTemplate() throws Exception {
        OffboardingTemplateResponse resp = new OffboardingTemplateResponse();
        resp.setId(1L);
        resp.setName("Standard Exit Template");
        resp.setStatus(OffboardingTemplateStatus.INACTIVE);

        when(templateService.deactivateTemplate(1L)).thenReturn(resp);

        mockMvc.perform(put("/api/v1/offboarding/templates/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }
}
