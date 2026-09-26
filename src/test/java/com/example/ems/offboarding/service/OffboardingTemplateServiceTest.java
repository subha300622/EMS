package com.example.ems.offboarding.service;

import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.entity.*;
import com.example.ems.offboarding.enums.*;
import com.example.ems.offboarding.repository.*;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffboardingTemplateServiceTest {

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

    private static final Long ORG_ID = 100L;
    private static final Long TEMPLATE_ID = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testCreateTemplate_Success() {
        OffboardingTemplateRequest request = new OffboardingTemplateRequest();
        request.setName("Engineering Exit Policy");
        request.setDescription("Standard exit process for engineering");
        request.setDepartmentIds(List.of("1", "2"));
        request.setNoticePeriodDefaultDays(60);

        when(templateRepository.existsByNameAndOrganizationId("Engineering Exit Policy", ORG_ID)).thenReturn(false);
        when(templateRepository.save(any(OffboardingTemplate.class))).thenAnswer(invocation -> {
            OffboardingTemplate saved = invocation.getArgument(0);
            saved.setId(TEMPLATE_ID);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        OffboardingTemplateResponse response = templateService.createTemplate(request);

        assertNotNull(response);
        assertEquals(TEMPLATE_ID, response.getId());
        assertEquals(ORG_ID, response.getOrganizationId());
        assertEquals("Engineering Exit Policy", response.getName());
        assertEquals(60, response.getNoticePeriodDefaultDays());
        assertEquals(List.of("1", "2"), response.getDepartmentIds());
    }

    @Test
    void testCreateTemplate_DuplicateName_ThrowsException() {
        OffboardingTemplateRequest request = new OffboardingTemplateRequest();
        request.setName("Engineering Exit Policy");

        when(templateRepository.existsByNameAndOrganizationId("Engineering Exit Policy", ORG_ID)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> templateService.createTemplate(request));
    }

    @Test
    void testGetTemplateDetails_Success() {
        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(TEMPLATE_ID);
        template.setOrganizationId(ORG_ID);
        template.setName("Standard Template");
        template.setStatus(OffboardingTemplateStatus.ACTIVE);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_ID)).thenReturn(Optional.of(template));

        OffboardingClearanceTaskTemplate task = new OffboardingClearanceTaskTemplate();
        task.setId(10L);
        task.setOrganizationId(ORG_ID);
        task.setTemplateId(TEMPLATE_ID);
        task.setTaskName("Return Laptop");
        task.setAssignToType(ClearanceAssignToType.IT_MANAGER);
        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_ID, ORG_ID))
                .thenReturn(List.of(task));

        OffboardingAssetRequirementTemplate asset = new OffboardingAssetRequirementTemplate();
        asset.setId(20L);
        asset.setOrganizationId(ORG_ID);
        asset.setTemplateId(TEMPLATE_ID);
        asset.setAssetType(OffboardingAssetType.LAPTOP);
        asset.setName("MacBook Pro");
        asset.setAssignedToType(ClearanceAssignToType.IT_MANAGER);
        when(assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_ID, ORG_ID))
                .thenReturn(List.of(asset));

        when(documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_ID, ORG_ID))
                .thenReturn(Collections.emptyList());

        OffboardingKtTemplate kt = new OffboardingKtTemplate();
        kt.setId(30L);
        kt.setOrganizationId(ORG_ID);
        kt.setTemplateId(TEMPLATE_ID);
        kt.setTitle("Project Handover");
        kt.setAssignToType(ClearanceAssignToType.REPORTING_MANAGER);
        when(ktTemplateRepository.findByTemplateIdAndOrganizationId(TEMPLATE_ID, ORG_ID)).thenReturn(Optional.of(kt));

        OffboardingInterviewTemplate interview = new OffboardingInterviewTemplate();
        interview.setId(40L);
        interview.setOrganizationId(ORG_ID);
        interview.setTemplateId(TEMPLATE_ID);
        interview.setConductedByType(ClearanceAssignToType.HR_MANAGER);
        when(interviewTemplateRepository.findByTemplateIdAndOrganizationId(TEMPLATE_ID, ORG_ID)).thenReturn(Optional.of(interview));

        OffboardingInterviewQuestion q = new OffboardingInterviewQuestion();
        q.setId(50L);
        q.setOrganizationId(ORG_ID);
        q.setInterviewTemplateId(40L);
        q.setQuestion("Reason for leaving?");
        q.setQuestionType(InterviewQuestionType.TEXT);
        when(interviewQuestionRepository.findByInterviewTemplateIdAndOrganizationIdOrderBySequenceAsc(40L, ORG_ID))
                .thenReturn(List.of(q));

        OffboardingTemplateDetailResponse details = templateService.getTemplateDetails(TEMPLATE_ID);

        assertNotNull(details);
        assertEquals(TEMPLATE_ID, details.getId());
        assertEquals(1, details.getClearanceTasks().size());
        assertEquals("Return Laptop", details.getClearanceTasks().get(0).getTaskName());
        assertEquals(1, details.getAssetRequirements().size());
        assertEquals(OffboardingAssetType.LAPTOP, details.getAssetRequirements().get(0).getAssetType());
        assertNotNull(details.getKtTemplate());
        assertEquals("Project Handover", details.getKtTemplate().getTitle());
        assertNotNull(details.getInterviewTemplate());
        assertEquals(1, details.getInterviewTemplate().getQuestions().size());
    }

    @Test
    void testAddClearanceTask_Success() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_ID))
                .thenReturn(Optional.of(new OffboardingTemplate()));

        ClearanceTaskTemplateRequest request = new ClearanceTaskTemplateRequest();
        request.setTaskName("Revoke VPN Access");
        request.setAssignToType(ClearanceAssignToType.IT_MANAGER);
        request.setDueBeforeLwdDays(2);
        request.setPriority("HIGH");

        when(clearanceTaskRepository.save(any(OffboardingClearanceTaskTemplate.class))).thenAnswer(invocation -> {
            OffboardingClearanceTaskTemplate saved = invocation.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        ClearanceTaskTemplateResponse resp = templateService.addClearanceTask(TEMPLATE_ID, request);

        assertNotNull(resp);
        assertEquals(101L, resp.getId());
        assertEquals("Revoke VPN Access", resp.getTaskName());
        assertEquals(ClearanceAssignToType.IT_MANAGER, resp.getAssignToType());
    }

    @Test
    void testConfigureKtTemplate_Success() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_ID))
                .thenReturn(Optional.of(new OffboardingTemplate()));
        when(ktTemplateRepository.findByTemplateIdAndOrganizationId(TEMPLATE_ID, ORG_ID))
                .thenReturn(Optional.empty());

        KtTemplateRequest request = new KtTemplateRequest();
        request.setTitle("Technical Handover");
        request.setAssignToType(ClearanceAssignToType.REPORTING_MANAGER);
        request.setEmployeeResponsibilities(List.of("Documentation", "Code walkthrough"));
        request.setDueBeforeLwdDays(5);

        when(ktTemplateRepository.save(any(OffboardingKtTemplate.class))).thenAnswer(invocation -> {
            OffboardingKtTemplate saved = invocation.getArgument(0);
            saved.setId(201L);
            return saved;
        });

        KtTemplateResponse resp = templateService.configureKtTemplate(TEMPLATE_ID, request);

        assertNotNull(resp);
        assertEquals(201L, resp.getId());
        assertEquals("Technical Handover", resp.getTitle());
        assertEquals(2, resp.getEmployeeResponsibilities().size());
    }

    @Test
    void testCloneTemplate_Success() {
        OffboardingTemplate source = new OffboardingTemplate();
        source.setId(TEMPLATE_ID);
        source.setOrganizationId(ORG_ID);
        source.setName("Base Policy");
        source.setStatus(OffboardingTemplateStatus.ACTIVE);

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_ID, ORG_ID)).thenReturn(Optional.of(source));
        when(templateRepository.existsByNameAndOrganizationId("Base Policy (Copy)", ORG_ID)).thenReturn(false);

        when(templateRepository.save(any(OffboardingTemplate.class))).thenAnswer(invocation -> {
            OffboardingTemplate saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_ID, ORG_ID)).thenReturn(Collections.emptyList());
        when(assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_ID, ORG_ID)).thenReturn(Collections.emptyList());
        when(documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_ID, ORG_ID)).thenReturn(Collections.emptyList());
        when(ktTemplateRepository.findByTemplateIdAndOrganizationId(TEMPLATE_ID, ORG_ID)).thenReturn(Optional.empty());
        when(interviewTemplateRepository.findByTemplateIdAndOrganizationId(TEMPLATE_ID, ORG_ID)).thenReturn(Optional.empty());

        when(templateRepository.findByIdAndOrganizationId(2L, ORG_ID)).thenReturn(Optional.of(new OffboardingTemplate()));
        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(2L, ORG_ID)).thenReturn(Collections.emptyList());
        when(assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(2L, ORG_ID)).thenReturn(Collections.emptyList());
        when(documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(2L, ORG_ID)).thenReturn(Collections.emptyList());
        when(ktTemplateRepository.findByTemplateIdAndOrganizationId(2L, ORG_ID)).thenReturn(Optional.empty());
        when(interviewTemplateRepository.findByTemplateIdAndOrganizationId(2L, ORG_ID)).thenReturn(Optional.empty());

        OffboardingTemplateDetailResponse cloned = templateService.cloneTemplate(TEMPLATE_ID, "Base Policy (Copy)");

        assertNotNull(cloned);
        verify(templateRepository, times(1)).save(argThat(t -> "Base Policy (Copy)".equals(t.getName())));
    }
}
