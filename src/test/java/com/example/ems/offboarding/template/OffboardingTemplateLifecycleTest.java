package com.example.ems.offboarding.template;

import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.offboarding.dto.OffboardingTemplateResponse;
import com.example.ems.offboarding.entity.OffboardingClearanceTaskTemplate;
import com.example.ems.offboarding.entity.OffboardingTemplate;
import com.example.ems.offboarding.enums.OffboardingTemplateStatus;
import com.example.ems.offboarding.repository.*;
import com.example.ems.offboarding.service.OffboardingTemplateService;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffboardingTemplateLifecycleTest {

    @Mock
    private OffboardingTemplateRepository templateRepository;
    @Mock
    private OffboardingClearanceTaskTemplateRepository clearanceTaskRepository;
    @Mock
    private OffboardingAssetRequirementTemplateRepository assetRequirementRepository;
    @Mock
    private OffboardingDocumentRequirementTemplateRepository documentRequirementRepository;
    @Mock
    private OffboardingKtTemplateRepository ktTemplateRepository;
    @Mock
    private OffboardingInterviewTemplateRepository interviewTemplateRepository;
    @Mock
    private OffboardingInterviewQuestionRepository interviewQuestionRepository;

    @org.mockito.Spy
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @InjectMocks
    private OffboardingTemplateService templateService;

    private static final Long ORG_A = 101L;
    private static final Long TEMPLATE_ID = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_A);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Lifecycle: DRAFT to ACTIVE with clearance tasks succeeds")
    void testActivateTemplate_DraftToActive_Success() {
        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(TEMPLATE_ID);
        template.setOrganizationId(ORG_A);
        template.setName("Standard Policy");
        template.setStatus(OffboardingTemplateStatus.DRAFT);

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(template));
        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_ID, ORG_A))
                .thenReturn(List.of(new OffboardingClearanceTaskTemplate()));
        when(templateRepository.save(any(OffboardingTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        OffboardingTemplateResponse resp = templateService.activateTemplate(TEMPLATE_ID);
        assertNotNull(resp);
        assertEquals(OffboardingTemplateStatus.ACTIVE, resp.getStatus());
    }

    @Test
    @DisplayName("Lifecycle: INACTIVE to ACTIVE reactivation succeeds")
    void testActivateTemplate_InactiveToActive_Success() {
        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(TEMPLATE_ID);
        template.setOrganizationId(ORG_A);
        template.setName("Standard Policy");
        template.setStatus(OffboardingTemplateStatus.INACTIVE);

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(template));
        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_ID, ORG_A))
                .thenReturn(List.of(new OffboardingClearanceTaskTemplate()));
        when(templateRepository.save(any(OffboardingTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        OffboardingTemplateResponse resp = templateService.activateTemplate(TEMPLATE_ID);
        assertNotNull(resp);
        assertEquals(OffboardingTemplateStatus.ACTIVE, resp.getStatus());
    }

    @Test
    @DisplayName("Lifecycle: activation fails when template has no clearance tasks (incomplete)")
    void testActivateTemplate_Incomplete_ThrowsException() {
        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(TEMPLATE_ID);
        template.setOrganizationId(ORG_A);
        template.setName("Standard Policy");
        template.setStatus(OffboardingTemplateStatus.DRAFT);

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(template));
        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_ID, ORG_A))
                .thenReturn(Collections.emptyList());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> templateService.activateTemplate(TEMPLATE_ID));
        assertTrue(ex.getMessage().contains("Template configuration is incomplete"));
    }

    @Test
    @DisplayName("Lifecycle: activating already ACTIVE template throws IllegalStateException")
    void testActivateTemplate_AlreadyActive_ThrowsException() {
        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(TEMPLATE_ID);
        template.setOrganizationId(ORG_A);
        template.setStatus(OffboardingTemplateStatus.ACTIVE);

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(template));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> templateService.activateTemplate(TEMPLATE_ID));
        assertTrue(ex.getMessage().contains("already active"));
    }

    @Test
    @DisplayName("Lifecycle: activating ARCHIVED template throws IllegalStateException")
    void testActivateTemplate_Archived_ThrowsException() {
        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(TEMPLATE_ID);
        template.setOrganizationId(ORG_A);
        template.setStatus(OffboardingTemplateStatus.ARCHIVED);

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(template));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> templateService.activateTemplate(TEMPLATE_ID));
        assertTrue(ex.getMessage().contains("Archived template cannot be activated"));
    }

    @Test
    @DisplayName("Lifecycle: ACTIVE to INACTIVE deactivation succeeds")
    void testDeactivateTemplate_ActiveToInactive_Success() {
        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(TEMPLATE_ID);
        template.setOrganizationId(ORG_A);
        template.setName("Standard Policy");
        template.setStatus(OffboardingTemplateStatus.ACTIVE);

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(template));
        when(templateRepository.save(any(OffboardingTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        OffboardingTemplateResponse resp = templateService.deactivateTemplate(TEMPLATE_ID);
        assertNotNull(resp);
        assertEquals(OffboardingTemplateStatus.INACTIVE, resp.getStatus());
    }

    @Test
    @DisplayName("Lifecycle: deactivating already INACTIVE template throws IllegalStateException")
    void testDeactivateTemplate_AlreadyInactive_ThrowsException() {
        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(TEMPLATE_ID);
        template.setOrganizationId(ORG_A);
        template.setStatus(OffboardingTemplateStatus.INACTIVE);

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(template));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> templateService.deactivateTemplate(TEMPLATE_ID));
        assertTrue(ex.getMessage().contains("already inactive"));
    }

    @Test
    @DisplayName("Lifecycle: deactivating DRAFT template throws IllegalStateException")
    void testDeactivateTemplate_Draft_ThrowsException() {
        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(TEMPLATE_ID);
        template.setOrganizationId(ORG_A);
        template.setStatus(OffboardingTemplateStatus.DRAFT);

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(template));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> templateService.deactivateTemplate(TEMPLATE_ID));
        assertTrue(ex.getMessage().contains("Draft template cannot be deactivated"));
    }

    @Test
    @DisplayName("Lifecycle: deactivating ARCHIVED template throws IllegalStateException")
    void testDeactivateTemplate_Archived_ThrowsException() {
        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(TEMPLATE_ID);
        template.setOrganizationId(ORG_A);
        template.setStatus(OffboardingTemplateStatus.ARCHIVED);

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(template));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> templateService.deactivateTemplate(TEMPLATE_ID));
        assertTrue(ex.getMessage().contains("Archived template cannot be deactivated"));
    }

    @Test
    @DisplayName("Tenant isolation: Org A cannot activate Org B's template")
    void testActivateTemplate_CrossTenant_ThrowsResourceNotFoundException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> templateService.activateTemplate(TEMPLATE_ID));
    }

    @Test
    @DisplayName("Tenant isolation: Org A cannot deactivate Org B's template")
    void testDeactivateTemplate_CrossTenant_ThrowsResourceNotFoundException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> templateService.deactivateTemplate(TEMPLATE_ID));
    }

    @Test
    @DisplayName("Tenant isolation: Org A cannot retrieve Org B's template")
    void testGetTemplate_CrossTenant_ThrowsResourceNotFoundException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> templateService.getTemplate(TEMPLATE_ID));
    }

    @Test
    @DisplayName("Tenant isolation: Org A cannot retrieve Org B's template details")
    void testGetTemplateDetails_CrossTenant_ThrowsResourceNotFoundException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> templateService.getTemplateDetails(TEMPLATE_ID));
    }

    @Test
    @DisplayName("Tenant isolation: Org A cannot update Org B's template")
    void testUpdateTemplate_CrossTenant_ThrowsResourceNotFoundException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.empty());

        com.example.ems.offboarding.dto.OffboardingTemplateRequest req = new com.example.ems.offboarding.dto.OffboardingTemplateRequest();
        req.setName("Attempted Update");

        assertThrows(ResourceNotFoundException.class, () -> templateService.updateTemplate(TEMPLATE_ID, req));
    }

    @Test
    @DisplayName("Tenant isolation: Org A cannot delete Org B's template")
    void testDeleteTemplate_CrossTenant_ThrowsResourceNotFoundException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> templateService.deleteTemplate(TEMPLATE_ID));
    }
}
