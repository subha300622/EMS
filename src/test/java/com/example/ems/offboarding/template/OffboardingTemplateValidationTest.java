package com.example.ems.offboarding.template;

import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.entity.*;
import com.example.ems.offboarding.enums.*;
import com.example.ems.offboarding.repository.*;
import com.example.ems.offboarding.service.OffboardingTemplateService;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffboardingTemplateValidationTest {

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

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OffboardingTemplateService templateService;

    private static final Long ORG_A = 101L;
    private static final Long ORG_B = 102L;
    private static final Long TEMPLATE_ID = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_A);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // =========================================================================
    // 1. BASE TEMPLATE VALIDATIONS
    // =========================================================================

    @Test
    @DisplayName("Create template: null name throws IllegalArgumentException")
    void testCreateTemplate_NullName_ThrowsException() {
        OffboardingTemplateRequest req = new OffboardingTemplateRequest();
        req.setName(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.createTemplate(req));
        assertTrue(ex.getMessage().contains("Template name is required"));
    }

    @Test
    @DisplayName("Create template: empty name throws IllegalArgumentException")
    void testCreateTemplate_EmptyName_ThrowsException() {
        OffboardingTemplateRequest req = new OffboardingTemplateRequest();
        req.setName("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.createTemplate(req));
        assertTrue(ex.getMessage().contains("Template name is required"));
    }

    @Test
    @DisplayName("Create template: whitespace-only name throws IllegalArgumentException")
    void testCreateTemplate_WhitespaceName_ThrowsException() {
        OffboardingTemplateRequest req = new OffboardingTemplateRequest();
        req.setName("    ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.createTemplate(req));
        assertTrue(ex.getMessage().contains("Template name is required"));
    }

    @Test
    @DisplayName("Create template: duplicate name in same org throws IllegalArgumentException (409)")
    void testCreateTemplate_DuplicateNameSameOrg_ThrowsException() {
        OffboardingTemplateRequest req = new OffboardingTemplateRequest();
        req.setName("Standard Exit");

        when(templateRepository.existsByNameAndOrganizationId("Standard Exit", ORG_A)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.createTemplate(req));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Create template: same name in different org is allowed")
    void testCreateTemplate_SameNameDifferentOrg_Allowed() {
        OffboardingTemplateRequest req = new OffboardingTemplateRequest();
        req.setName("Standard Exit");

        // Set tenant to ORG_B
        TenantContext.setCurrentTenant(ORG_B);
        when(templateRepository.existsByNameAndOrganizationId("Standard Exit", ORG_B)).thenReturn(false);
        when(templateRepository.save(any(OffboardingTemplate.class))).thenAnswer(inv -> {
            OffboardingTemplate saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        OffboardingTemplateResponse resp = templateService.createTemplate(req);
        assertNotNull(resp);
        assertEquals(ORG_B, resp.getOrganizationId());
        assertEquals("Standard Exit", resp.getName());
    }

    @Test
    @DisplayName("Create template: negative notice period throws IllegalArgumentException")
    void testCreateTemplate_NegativeNoticePeriod_ThrowsException() {
        OffboardingTemplateRequest req = new OffboardingTemplateRequest();
        req.setName("Standard Exit");
        req.setNoticePeriodDefaultDays(-1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.createTemplate(req));
        assertTrue(ex.getMessage().contains("Notice period default days cannot be negative"));
    }

    @Test
    @DisplayName("Create template: zero notice period is allowed")
    void testCreateTemplate_ZeroNoticePeriod_Allowed() {
        OffboardingTemplateRequest req = new OffboardingTemplateRequest();
        req.setName("Zero Notice Exit");
        req.setNoticePeriodDefaultDays(0);

        when(templateRepository.existsByNameAndOrganizationId("Zero Notice Exit", ORG_A)).thenReturn(false);
        when(templateRepository.save(any(OffboardingTemplate.class))).thenAnswer(inv -> {
            OffboardingTemplate saved = inv.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        OffboardingTemplateResponse resp = templateService.createTemplate(req);
        assertNotNull(resp);
        assertEquals(0, resp.getNoticePeriodDefaultDays());
    }

    // =========================================================================
    // 2. CLEARANCE TASK VALIDATIONS
    // =========================================================================

    @Test
    @DisplayName("Clearance task: blank task name throws IllegalArgumentException")
    void testClearanceTask_BlankTaskName_ThrowsException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(new OffboardingTemplate()));

        ClearanceTaskTemplateRequest req = new ClearanceTaskTemplateRequest();
        req.setTaskName("   ");
        req.setAssignToType(ClearanceAssignToType.HR_MANAGER);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.addClearanceTask(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("Task name is required"));
    }

    @Test
    @DisplayName("Clearance task: null assignToType throws IllegalArgumentException")
    void testClearanceTask_NullAssignToType_ThrowsException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(new OffboardingTemplate()));

        ClearanceTaskTemplateRequest req = new ClearanceTaskTemplateRequest();
        req.setTaskName("HR Clearance");
        req.setAssignToType(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.addClearanceTask(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("Assign to type is required"));
    }

    @Test
    @DisplayName("Clearance task: negative dueBeforeLwdDays throws IllegalArgumentException")
    void testClearanceTask_NegativeDueBeforeLwdDays_ThrowsException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(new OffboardingTemplate()));

        ClearanceTaskTemplateRequest req = new ClearanceTaskTemplateRequest();
        req.setTaskName("HR Clearance");
        req.setAssignToType(ClearanceAssignToType.HR_MANAGER);
        req.setDueBeforeLwdDays(-2);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.addClearanceTask(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("Due before LWD days cannot be negative"));
    }

    @Test
    @DisplayName("Clearance task: sequence < 1 throws IllegalArgumentException")
    void testClearanceTask_SequenceLessThanOne_ThrowsException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(new OffboardingTemplate()));

        ClearanceTaskTemplateRequest req = new ClearanceTaskTemplateRequest();
        req.setTaskName("HR Clearance");
        req.setAssignToType(ClearanceAssignToType.HR_MANAGER);
        req.setSequence(0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.addClearanceTask(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("Sequence must be greater than or equal to 1"));
    }

    @Test
    @DisplayName("Clearance task: optional task configuration succeeds")
    void testClearanceTask_OptionalTask_Succeeds() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(new OffboardingTemplate()));

        ClearanceTaskTemplateRequest req = new ClearanceTaskTemplateRequest();
        req.setTaskName("Optional Library NOC");
        req.setAssignToType(ClearanceAssignToType.DEPARTMENT_HEAD);
        req.setMandatory(false);
        req.setSequence(5);

        when(clearanceTaskRepository.save(any(OffboardingClearanceTaskTemplate.class))).thenAnswer(inv -> {
            OffboardingClearanceTaskTemplate saved = inv.getArgument(0);
            saved.setId(105L);
            return saved;
        });

        ClearanceTaskTemplateResponse resp = templateService.addClearanceTask(TEMPLATE_ID, req);
        assertNotNull(resp);
        assertFalse(resp.getMandatory());
        assertEquals(5, resp.getSequence());
    }

    // =========================================================================
    // 3. ASSET REQUIREMENT VALIDATIONS
    // =========================================================================

    @Test
    @DisplayName("Asset requirement: blank asset name throws IllegalArgumentException")
    void testAssetRequirement_BlankName_ThrowsException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(new OffboardingTemplate()));

        AssetRequirementTemplateRequest req = new AssetRequirementTemplateRequest();
        req.setName("  ");
        req.setAssetType(OffboardingAssetType.LAPTOP);
        req.setAssignedToType(ClearanceAssignToType.IT_MANAGER);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.addAssetRequirement(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("Asset name is required"));
    }

    @Test
    @DisplayName("Asset requirement: null assetType throws IllegalArgumentException")
    void testAssetRequirement_NullAssetType_ThrowsException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(new OffboardingTemplate()));

        AssetRequirementTemplateRequest req = new AssetRequirementTemplateRequest();
        req.setName("MacBook");
        req.setAssetType(null);
        req.setAssignedToType(ClearanceAssignToType.IT_MANAGER);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.addAssetRequirement(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("Asset type is required"));
    }

    @Test
    @DisplayName("Asset requirement: null assignedToType throws IllegalArgumentException")
    void testAssetRequirement_NullAssignedToType_ThrowsException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(new OffboardingTemplate()));

        AssetRequirementTemplateRequest req = new AssetRequirementTemplateRequest();
        req.setName("MacBook");
        req.setAssetType(OffboardingAssetType.LAPTOP);
        req.setAssignedToType(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.addAssetRequirement(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("Assigned to type is required"));
    }

    @Test
    @DisplayName("Asset requirement: negative dueBeforeLwdDays throws IllegalArgumentException")
    void testAssetRequirement_NegativeDueDays_ThrowsException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(new OffboardingTemplate()));

        AssetRequirementTemplateRequest req = new AssetRequirementTemplateRequest();
        req.setName("MacBook");
        req.setAssetType(OffboardingAssetType.LAPTOP);
        req.setAssignedToType(ClearanceAssignToType.IT_MANAGER);
        req.setDueBeforeLwdDays(-3);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.addAssetRequirement(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("Due before LWD days cannot be negative"));
    }

    // =========================================================================
    // 4. DOCUMENT REQUIREMENT VALIDATIONS
    // =========================================================================

    @Test
    @DisplayName("Document requirement: blank name throws IllegalArgumentException")
    void testDocumentRequirement_BlankName_ThrowsException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(new OffboardingTemplate()));

        DocumentRequirementTemplateRequest req = new DocumentRequirementTemplateRequest();
        req.setDocumentName("");
        req.setDocumentType(OffboardingDocumentType.EXPERIENCE_LETTER);
        req.setOwnerType(ClearanceAssignToType.HR_MANAGER);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.addDocumentRequirement(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("Document name is required"));
    }

    @Test
    @DisplayName("Document requirement: null documentType throws IllegalArgumentException")
    void testDocumentRequirement_NullDocumentType_ThrowsException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(new OffboardingTemplate()));

        DocumentRequirementTemplateRequest req = new DocumentRequirementTemplateRequest();
        req.setDocumentName("Experience Letter");
        req.setDocumentType(null);
        req.setOwnerType(ClearanceAssignToType.HR_MANAGER);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.addDocumentRequirement(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("Document type is required"));
    }

    @Test
    @DisplayName("Document requirement: null ownerType throws IllegalArgumentException")
    void testDocumentRequirement_NullOwnerType_ThrowsException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(new OffboardingTemplate()));

        DocumentRequirementTemplateRequest req = new DocumentRequirementTemplateRequest();
        req.setDocumentName("Relieving Letter");
        req.setDocumentType(OffboardingDocumentType.RELIEVING_LETTER);
        req.setOwnerType(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.addDocumentRequirement(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("Owner type is required"));
    }

    // =========================================================================
    // 5. KT & EXIT INTERVIEW VALIDATIONS
    // =========================================================================

    @Test
    @DisplayName("KT template: blank title throws IllegalArgumentException")
    void testKtTemplate_BlankTitle_ThrowsException() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_A)).thenReturn(Optional.of(new OffboardingTemplate()));

        KtTemplateRequest req = new KtTemplateRequest();
        req.setTitle("   ");
        req.setAssignToType(ClearanceAssignToType.REPORTING_MANAGER);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.configureKtTemplate(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("KT title is required"));
    }

    @Test
    @DisplayName("Interview question: blank question text throws IllegalArgumentException")
    void testInterviewQuestion_BlankQuestion_ThrowsException() {
        when(interviewTemplateRepository.findByTemplateIdAndOrganizationId(TEMPLATE_ID, ORG_A))
                .thenReturn(Optional.of(new OffboardingInterviewTemplate()));

        InterviewQuestionRequest req = new InterviewQuestionRequest();
        req.setQuestion("  ");
        req.setQuestionType(InterviewQuestionType.RATING);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.addInterviewQuestion(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("Question text is required"));
    }

    @Test
    @DisplayName("Interview question: null questionType throws IllegalArgumentException")
    void testInterviewQuestion_NullQuestionType_ThrowsException() {
        when(interviewTemplateRepository.findByTemplateIdAndOrganizationId(TEMPLATE_ID, ORG_A))
                .thenReturn(Optional.of(new OffboardingInterviewTemplate()));

        InterviewQuestionRequest req = new InterviewQuestionRequest();
        req.setQuestion("Overall rating of your experience?");
        req.setQuestionType(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> templateService.addInterviewQuestion(TEMPLATE_ID, req));
        assertTrue(ex.getMessage().contains("Question type is required"));
    }
}
