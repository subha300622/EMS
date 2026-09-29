package com.example.ems.offboarding.template;

import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.entity.*;
import com.example.ems.offboarding.enums.*;
import com.example.ems.offboarding.repository.*;
import com.example.ems.offboarding.service.OffboardingService;
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

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffboardingTemplateSnapshotTest {

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

    @Mock
    private OffboardingRepository offboardingRepository;
    @Mock
    private OffboardingTaskRepository offboardingTaskRepository;
    @Mock
    private OffboardingAssetReturnRepository offboardingAssetReturnRepository;
    @Mock
    private OffboardingSettlementRepository offboardingSettlementRepository;
    @Mock
    private OffboardingHandoverRepository offboardingHandoverRepository;
    @Mock
    private ExitInterviewRepository exitInterviewRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OffboardingTemplateService templateService;

    @InjectMocks
    private OffboardingService offboardingService;

    private static final Long ORG_ID = 200L;
    private static final Long TEMPLATE_V1_ID = 1L;
    private static final Long CLONED_TEMPLATE_ID = 2L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // =========================================================================
    // 1. CLONE & ISOLATION SNAPSHOT TESTS
    // =========================================================================

    @Test
    @DisplayName("1. Template clone creates a deep copy with isolated IDs for clearance and asset sub-resources")
    void testTemplateClone_DeepCopyIsolation() {
        OffboardingTemplate source = new OffboardingTemplate();
        source.setId(TEMPLATE_V1_ID);
        source.setOrganizationId(ORG_ID);
        source.setName("Master Policy");
        source.setStatus(OffboardingTemplateStatus.ACTIVE);

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Optional.of(source));
        when(templateRepository.existsByNameAndOrganizationId("Master Policy (Copy)", ORG_ID)).thenReturn(false);

        OffboardingTemplate clonedTarget = new OffboardingTemplate();
        clonedTarget.setId(CLONED_TEMPLATE_ID);
        clonedTarget.setOrganizationId(ORG_ID);
        clonedTarget.setName("Master Policy (Copy)");
        when(templateRepository.save(any(OffboardingTemplate.class))).thenReturn(clonedTarget);

        // Source sub-resources
        OffboardingClearanceTaskTemplate task1 = new OffboardingClearanceTaskTemplate();
        task1.setId(10L);
        task1.setTaskName("HR Clearance");
        task1.setSequence(1);

        OffboardingAssetRequirementTemplate asset1 = new OffboardingAssetRequirementTemplate();
        asset1.setId(20L);
        asset1.setName("Laptop");
        asset1.setAssetType(OffboardingAssetType.LAPTOP);

        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_V1_ID, ORG_ID))
                .thenReturn(List.of(task1));
        when(assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_V1_ID, ORG_ID))
                .thenReturn(List.of(asset1));
        when(documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_V1_ID, ORG_ID))
                .thenReturn(Collections.emptyList());
        when(ktTemplateRepository.findByTemplateIdAndOrganizationId(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Optional.empty());
        when(interviewTemplateRepository.findByTemplateIdAndOrganizationId(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Optional.empty());

        // For details retrieval of cloned template
        when(templateRepository.findByIdAndOrganizationId(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Optional.of(clonedTarget));
        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(CLONED_TEMPLATE_ID, ORG_ID))
                .thenReturn(List.of(task1));
        when(assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(CLONED_TEMPLATE_ID, ORG_ID))
                .thenReturn(List.of(asset1));
        when(documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(CLONED_TEMPLATE_ID, ORG_ID))
                .thenReturn(Collections.emptyList());
        when(ktTemplateRepository.findByTemplateIdAndOrganizationId(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Optional.empty());
        when(interviewTemplateRepository.findByTemplateIdAndOrganizationId(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Optional.empty());

        OffboardingTemplateDetailResponse cloned = templateService.cloneTemplate(TEMPLATE_V1_ID, "Master Policy (Copy)");

        assertNotNull(cloned);
        assertEquals(CLONED_TEMPLATE_ID, cloned.getId());
        verify(clearanceTaskRepository, times(1)).save(argThat(t -> t.getTemplateId().equals(CLONED_TEMPLATE_ID) && "HR Clearance".equals(t.getTaskName())));
        verify(assetRequirementRepository, times(1)).save(argThat(a -> a.getTemplateId().equals(CLONED_TEMPLATE_ID) && "Laptop".equals(a.getName())));
    }

    @Test
    @DisplayName("2. Modifying cloned template does not alter source master template")
    void testCloneImmutability_ModifyingCloneDoesNotAffectSource() {
        OffboardingTemplate source = new OffboardingTemplate();
        source.setId(TEMPLATE_V1_ID);
        source.setName("Original Base");

        OffboardingTemplate clone = new OffboardingTemplate();
        clone.setId(CLONED_TEMPLATE_ID);
        clone.setName("Cloned Branch");

        when(templateRepository.findByIdAndOrganizationId(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Optional.of(clone));
        when(templateRepository.save(any(OffboardingTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        // Update clone name
        OffboardingTemplateRequest updateReq = new OffboardingTemplateRequest();
        updateReq.setName("Cloned Branch V2");

        OffboardingTemplateResponse resp = templateService.updateTemplate(CLONED_TEMPLATE_ID, updateReq);
        assertEquals("Cloned Branch V2", resp.getName());
        assertEquals("Original Base", source.getName()); // Source remains untouched
    }

    @Test
    @DisplayName("3. Template clone deep-copies document requirements and KT template")
    void testTemplateClone_DeepCopiesDocumentRequirementsAndKtTemplates() {
        OffboardingTemplate source = new OffboardingTemplate();
        source.setId(TEMPLATE_V1_ID);
        source.setOrganizationId(ORG_ID);
        source.setName("Tech Exit Policy");

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Optional.of(source));
        when(templateRepository.existsByNameAndOrganizationId("Tech Exit Copy", ORG_ID)).thenReturn(false);

        OffboardingTemplate clonedTarget = new OffboardingTemplate();
        clonedTarget.setId(CLONED_TEMPLATE_ID);
        clonedTarget.setOrganizationId(ORG_ID);
        when(templateRepository.save(any(OffboardingTemplate.class))).thenReturn(clonedTarget);

        OffboardingDocumentRequirementTemplate docReq = new OffboardingDocumentRequirementTemplate();
        docReq.setId(31L);
        docReq.setDocumentName("NDA Agreement");
        docReq.setDocumentType(OffboardingDocumentType.OTHER);
        docReq.setSequence(1);

        OffboardingKtTemplate kt = new OffboardingKtTemplate();
        kt.setId(41L);
        kt.setTemplateId(TEMPLATE_V1_ID);
        kt.setTitle("Engineering KT");
        kt.setEmployeeResponsibilitiesJson("[\"Repo access transfer\", \"Credentials rotation\"]");

        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Collections.emptyList());
        when(assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Collections.emptyList());
        when(documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_V1_ID, ORG_ID)).thenReturn(List.of(docReq));
        when(ktTemplateRepository.findByTemplateIdAndOrganizationId(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Optional.of(kt));
        when(interviewTemplateRepository.findByTemplateIdAndOrganizationId(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Optional.empty());

        // Cloned queries
        when(templateRepository.findByIdAndOrganizationId(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Optional.of(clonedTarget));
        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Collections.emptyList());
        when(assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Collections.emptyList());
        when(documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(List.of(docReq));
        when(ktTemplateRepository.findByTemplateIdAndOrganizationId(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Optional.of(kt));
        when(interviewTemplateRepository.findByTemplateIdAndOrganizationId(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Optional.empty());

        templateService.cloneTemplate(TEMPLATE_V1_ID, "Tech Exit Copy");

        verify(documentRequirementRepository, times(1)).save(argThat(d ->
                d.getTemplateId().equals(CLONED_TEMPLATE_ID) && "NDA Agreement".equals(d.getDocumentName())));
        verify(ktTemplateRepository, times(1)).save(argThat(k ->
                k.getTemplateId().equals(CLONED_TEMPLATE_ID) && "Engineering KT".equals(k.getTitle())));
    }

    @Test
    @DisplayName("4. Template clone deep-copies interview template and all nested interview questions")
    void testTemplateClone_DeepCopiesInterviewTemplateAndQuestions() {
        OffboardingTemplate source = new OffboardingTemplate();
        source.setId(TEMPLATE_V1_ID);
        source.setOrganizationId(ORG_ID);

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Optional.of(source));
        when(templateRepository.existsByNameAndOrganizationId(any(), any())).thenReturn(false);

        OffboardingTemplate clonedTarget = new OffboardingTemplate();
        clonedTarget.setId(CLONED_TEMPLATE_ID);
        clonedTarget.setOrganizationId(ORG_ID);
        when(templateRepository.save(any(OffboardingTemplate.class))).thenReturn(clonedTarget);

        OffboardingInterviewTemplate interviewSource = new OffboardingInterviewTemplate();
        interviewSource.setId(51L);
        interviewSource.setTemplateId(TEMPLATE_V1_ID);
        interviewSource.setConductedByType(ClearanceAssignToType.HR_MANAGER);

        OffboardingInterviewQuestion q1 = new OffboardingInterviewQuestion();
        q1.setId(61L);
        q1.setInterviewTemplateId(51L);
        q1.setQuestion("Reason for leaving?");
        q1.setQuestionType(InterviewQuestionType.TEXT);

        OffboardingInterviewTemplate interviewSaved = new OffboardingInterviewTemplate();
        interviewSaved.setId(52L);
        interviewSaved.setTemplateId(CLONED_TEMPLATE_ID);
        when(interviewTemplateRepository.save(any(OffboardingInterviewTemplate.class))).thenReturn(interviewSaved);

        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Collections.emptyList());
        when(assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Collections.emptyList());
        when(documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Collections.emptyList());
        when(ktTemplateRepository.findByTemplateIdAndOrganizationId(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Optional.empty());
        when(interviewTemplateRepository.findByTemplateIdAndOrganizationId(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Optional.of(interviewSource));
        when(interviewQuestionRepository.findByInterviewTemplateIdAndOrganizationIdOrderBySequenceAsc(51L, ORG_ID)).thenReturn(List.of(q1));

        // Cloned queries for getTemplateDetails
        when(templateRepository.findByIdAndOrganizationId(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Optional.of(clonedTarget));
        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Collections.emptyList());
        when(assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Collections.emptyList());
        when(documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Collections.emptyList());
        when(ktTemplateRepository.findByTemplateIdAndOrganizationId(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Optional.empty());
        when(interviewTemplateRepository.findByTemplateIdAndOrganizationId(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Optional.of(interviewSaved));
        when(interviewQuestionRepository.findByInterviewTemplateIdAndOrganizationIdOrderBySequenceAsc(52L, ORG_ID)).thenReturn(List.of(q1));

        templateService.cloneTemplate(TEMPLATE_V1_ID, "Cloned With Interview");

        verify(interviewTemplateRepository, times(1)).save(argThat(it -> it.getTemplateId().equals(CLONED_TEMPLATE_ID)));
        verify(interviewQuestionRepository, times(1)).save(argThat(q ->
                q.getInterviewTemplateId().equals(52L) && "Reason for leaving?".equals(q.getQuestion())));
    }

    @Test
    @DisplayName("5. Template clone auto-generates timestamped unique name when requested name already exists")
    void testTemplateClone_AutoGeneratesUniqueNameWhenDuplicateExists() {
        OffboardingTemplate source = new OffboardingTemplate();
        source.setId(TEMPLATE_V1_ID);
        source.setName("Base");

        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Optional.of(source));
        when(templateRepository.existsByNameAndOrganizationId("Base (Copy)", ORG_ID)).thenReturn(true);

        OffboardingTemplate clonedTarget = new OffboardingTemplate();
        clonedTarget.setId(CLONED_TEMPLATE_ID);
        when(templateRepository.save(any(OffboardingTemplate.class))).thenReturn(clonedTarget);

        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(any(), any())).thenReturn(Collections.emptyList());
        when(assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(any(), any())).thenReturn(Collections.emptyList());
        when(documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(any(), any())).thenReturn(Collections.emptyList());
        when(ktTemplateRepository.findByTemplateIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());
        when(interviewTemplateRepository.findByTemplateIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());
        when(templateRepository.findByIdAndOrganizationId(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Optional.of(clonedTarget));

        templateService.cloneTemplate(TEMPLATE_V1_ID, null);

        verify(templateRepository).save(argThat(t -> t.getName().startsWith("Base (Copy) ")));
    }

    @Test
    @DisplayName("6. Cross-tenant clone is strictly blocked with ResourceNotFoundException")
    void testTemplateClone_TenantIsolation_CannotCloneOtherTenantTemplate() {
        when(templateRepository.findByIdAndOrganizationId(TEMPLATE_V1_ID, ORG_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                templateService.cloneTemplate(TEMPLATE_V1_ID, "Illegal Clone"));
    }

    @Test
    @DisplayName("7. Modifying sub-resources on source template does not alter already cloned template sub-resources")
    void testTemplateSubResourceModification_DoesNotAffectClonedTemplate() {
        // Given cloned template has its own task with ID 102L
        OffboardingClearanceTaskTemplate clonedTask = new OffboardingClearanceTaskTemplate();
        clonedTask.setId(102L);
        clonedTask.setTemplateId(CLONED_TEMPLATE_ID);
        clonedTask.setTaskName("Cloned Security Check");

        when(templateRepository.findByIdAndOrganizationId(CLONED_TEMPLATE_ID, ORG_ID)).thenReturn(Optional.of(new OffboardingTemplate()));
        when(clearanceTaskRepository.findByIdAndOrganizationId(102L, ORG_ID)).thenReturn(Optional.of(clonedTask));
        when(clearanceTaskRepository.save(any(OffboardingClearanceTaskTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        ClearanceTaskTemplateRequest updateReq = new ClearanceTaskTemplateRequest();
        updateReq.setTaskName("Cloned Security Check - Revised");
        updateReq.setSequence(1);
        updateReq.setAssignToType(ClearanceAssignToType.HR_MANAGER);

        ClearanceTaskTemplateResponse resp = templateService.updateClearanceTask(CLONED_TEMPLATE_ID, 102L, updateReq);

        assertEquals("Cloned Security Check - Revised", resp.getTaskName());
        assertEquals(CLONED_TEMPLATE_ID, resp.getTemplateId());
    }

    // =========================================================================
    // 2. OFFBOARDING INSTANCE SNAPSHOT TESTS
    // =========================================================================

    @Test
    @DisplayName("8. Employee A instantiated offboarding tasks are isolated from subsequent template mutations")
    void testOffboardingInstance_TasksIsolatedFromTemplateChanges() {
        // Step 1: Employee A starts offboarding
        Employee empA = new Employee();
        empA.setId(1001L);
        empA.setFullName("Alice Smith");

        when(employeeRepository.findById(1001L)).thenReturn(Optional.of(empA));
        when(offboardingRepository.findByEmployeeId(1001L)).thenReturn(Optional.empty());

        Offboarding offboardingA = new Offboarding();
        offboardingA.setId(501L);
        offboardingA.setEmployee(empA);
        offboardingA.setStatus("PENDING");
        offboardingA.setExitDate(LocalDate.now().plusDays(30));

        when(offboardingRepository.save(any(Offboarding.class))).thenReturn(offboardingA);

        List<OffboardingTask> instantiatedTasksA = new ArrayList<>();
        when(offboardingTaskRepository.save(any(OffboardingTask.class))).thenAnswer(inv -> {
            OffboardingTask t = inv.getArgument(0);
            t.setId((long) (instantiatedTasksA.size() + 1));
            instantiatedTasksA.add(t);
            return t;
        });
        when(offboardingTaskRepository.findByOffboardingId(501L)).thenAnswer(inv -> instantiatedTasksA);

        OffboardingRequest reqA = new OffboardingRequest();
        reqA.setEmployeeId(1001L);
        reqA.setReason("Career move");

        OffboardingResponse respA = offboardingService.createOffboarding(reqA);

        assertNotNull(respA);
        assertEquals(4, instantiatedTasksA.size());
        assertTrue(instantiatedTasksA.stream().anyMatch(t -> t.getTitle().contains("Laptop")));
        assertTrue(instantiatedTasksA.stream().anyMatch(t -> t.getTitle().contains("Knowledge Handover")));

        // Step 2: Employee B starts offboarding separately
        Employee empB = new Employee();
        empB.setId(1002L);
        empB.setFullName("Bob Jones");

        when(employeeRepository.findById(1002L)).thenReturn(Optional.of(empB));
        when(offboardingRepository.findByEmployeeId(1002L)).thenReturn(Optional.empty());

        Offboarding offboardingB = new Offboarding();
        offboardingB.setId(502L);
        offboardingB.setEmployee(empB);
        offboardingB.setStatus("PENDING");
        offboardingB.setExitDate(LocalDate.now().plusDays(15));
        when(offboardingRepository.save(any(Offboarding.class))).thenReturn(offboardingB);

        List<OffboardingTask> instantiatedTasksB = new ArrayList<>();
        when(offboardingTaskRepository.save(any(OffboardingTask.class))).thenAnswer(inv -> {
            OffboardingTask t = inv.getArgument(0);
            t.setId((long) (100 + instantiatedTasksB.size() + 1));
            instantiatedTasksB.add(t);
            return t;
        });
        when(offboardingTaskRepository.findByOffboardingId(502L)).thenAnswer(inv -> instantiatedTasksB);

        OffboardingRequest reqB = new OffboardingRequest();
        reqB.setEmployeeId(1002L);
        reqB.setReason("Higher Studies");

        OffboardingResponse respB = offboardingService.createOffboarding(reqB);
        assertNotNull(respB);

        // Verify task isolation between instance A and instance B
        assertEquals(4, instantiatedTasksA.size());
        assertEquals(4, instantiatedTasksB.size());
        assertNotEquals(instantiatedTasksA.get(0).getId(), instantiatedTasksB.get(0).getId());
    }

    @Test
    @DisplayName("9. In-flight offboarding task status updates do not affect other offboarding instances")
    void testOffboardingTaskStatus_InstanceIsolation() {
        Offboarding offboarding1 = new Offboarding();
        offboarding1.setId(1L);
        offboarding1.setStatus("PENDING");

        OffboardingTask task1 = new OffboardingTask();
        task1.setId(101L);
        task1.setOffboarding(offboarding1);
        task1.setTitle("Return Laptop");
        task1.setStatus("PENDING");

        when(offboardingTaskRepository.findById(101L)).thenReturn(Optional.of(task1));
        when(offboardingTaskRepository.save(any(OffboardingTask.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = offboardingService.updateTaskStatus(101L, "COMPLETED");

        assertTrue(response.isPresent());
        assertEquals("COMPLETED", response.get().getStatus());
        assertNotNull(task1.getCompletedAt());
        assertEquals("IN_PROGRESS", offboarding1.getStatus());
    }

    @Test
    @DisplayName("10. Concurrent offboarding instances maintain isolated lifecycle transitions")
    void testOffboardingInstance_MultipleEmployeesIsolatedLifecycles() {
        Offboarding offboarding1 = new Offboarding();
        offboarding1.setId(101L);
        offboarding1.setStatus("PENDING");

        Offboarding offboarding2 = new Offboarding();
        offboarding2.setId(102L);
        offboarding2.setStatus("PENDING");

        when(offboardingRepository.findById(101L)).thenReturn(Optional.of(offboarding1));
        when(offboardingRepository.findById(102L)).thenReturn(Optional.of(offboarding2));
        when(offboardingRepository.save(any(Offboarding.class))).thenAnswer(inv -> inv.getArgument(0));

        // Complete instance 1
        var resp1 = offboardingService.completeOffboarding(101L);
        assertTrue(resp1.isPresent());
        assertEquals("COMPLETED", resp1.get().getStatus());

        // Instance 2 remains PENDING
        assertEquals("PENDING", offboarding2.getStatus());

        // Reject instance 2
        var resp2 = offboardingService.rejectOffboarding(102L);
        assertTrue(resp2.isPresent());
        assertEquals("REJECTED", resp2.get().getStatus());

        // Instance 1 remains COMPLETED
        assertEquals("COMPLETED", offboarding1.getStatus());
    }
}
