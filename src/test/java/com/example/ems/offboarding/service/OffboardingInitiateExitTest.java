package com.example.ems.offboarding.service;

import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.InitiateExitRequest;
import com.example.ems.offboarding.dto.InitiateExitResponse;
import com.example.ems.offboarding.entity.Offboarding;
import com.example.ems.offboarding.entity.OffboardingClearanceTaskTemplate;
import com.example.ems.offboarding.entity.OffboardingTask;
import com.example.ems.offboarding.entity.OffboardingTemplate;
import com.example.ems.offboarding.enums.OffboardingTemplateStatus;
import com.example.ems.offboarding.repository.OffboardingClearanceTaskTemplateRepository;
import com.example.ems.offboarding.repository.OffboardingRepository;
import com.example.ems.offboarding.repository.OffboardingTaskRepository;
import com.example.ems.offboarding.repository.OffboardingTemplateRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffboardingInitiateExitTest {

    @Mock
    private OffboardingRepository offboardingRepository;

    @Mock
    private OffboardingTaskRepository offboardingTaskRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OffboardingTemplateAssignmentService templateAssignmentService;

    @Mock
    private OffboardingTemplateRepository templateRepository;

    @Mock
    private OffboardingClearanceTaskTemplateRepository clearanceTaskRepository;

    @InjectMocks
    private OffboardingService offboardingService;

    private static final Long ORG_ID = 100L;
    private Organization organization;
    private Employee employee;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_ID);

        organization = new Organization();
        organization.setId(ORG_ID);

        employee = new Employee();
        employee.setId(25L);
        employee.setFullName("John Doe");
        employee.setEmployeeId("EMP025");
        employee.setOrganization(organization);
        employee.setDepartment("Engineering");
        employee.setEmploymentType("FULL_TIME");
        employee.setDesignation("Senior Software Engineer");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Initiate Exit - Success with auto-resolved template and clearance tasks")
    void testInitiateExit_Success_AutoResolvedTemplate() {
        InitiateExitRequest request = new InitiateExitRequest(
                25L, "RESIGNATION", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 9, 15),
                30, "CAREER_GROWTH", "Moving to new job", 12L, List.of("EMPLOYEE", "HR")
        );

        when(employeeRepository.findById(25L)).thenReturn(Optional.of(employee));
        when(offboardingRepository.findByEmployeeId(25L)).thenReturn(Optional.empty());

        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(4L);
        template.setName("Engineering Standard Offboarding");
        template.setStatus(OffboardingTemplateStatus.ACTIVE);

        when(templateAssignmentService.resolveTemplateForEmployee(employee, "RESIGNATION"))
                .thenReturn(template);

        OffboardingClearanceTaskTemplate ct1 = new OffboardingClearanceTaskTemplate();
        ct1.setTaskName("Return Laptop");
        ct1.setDescription("IT hardware return");
        ct1.setAssignToType(com.example.ems.offboarding.enums.ClearanceAssignToType.IT_MANAGER);
        ct1.setMandatory(true);

        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdAndActiveTrueOrderBySequenceAsc(4L, ORG_ID))
                .thenReturn(List.of(ct1));

        List<OffboardingTask> savedTasks = new ArrayList<>();
        when(offboardingRepository.save(any(Offboarding.class))).thenAnswer(inv -> {
            Offboarding ob = inv.getArgument(0);
            ob.setId(101L);
            return ob;
        });

        when(offboardingTaskRepository.save(any(OffboardingTask.class))).thenAnswer(inv -> {
            OffboardingTask t = inv.getArgument(0);
            savedTasks.add(t);
            return t;
        });

        InitiateExitResponse response = offboardingService.initiateExit(request);

        assertNotNull(response);
        assertEquals(101L, response.getOffboardingId());
        assertEquals(25L, response.getEmployeeId());
        assertEquals("John Doe", response.getEmployeeName());
        assertEquals(4L, response.getTemplateId());
        assertEquals("Engineering Standard Offboarding", response.getTemplateName());
        assertEquals("RESIGNATION", response.getExitType());
        assertEquals("PENDING", response.getStatus());
        assertEquals(1, savedTasks.size());
        assertEquals("Return Laptop", savedTasks.get(0).getTitle());
    }

    @Test
    @DisplayName("Initiate Exit - Fails when employee is from a different tenant")
    void testInitiateExit_TenantIsolation() {
        Organization otherOrg = new Organization();
        otherOrg.setId(999L);
        employee.setOrganization(otherOrg);

        when(employeeRepository.findById(25L)).thenReturn(Optional.of(employee));

        InitiateExitRequest request = new InitiateExitRequest(
                25L, "RESIGNATION", LocalDate.of(2026, 10, 10), null, null, null, null, null, null
        );

        assertThrows(ResourceNotFoundException.class, () -> offboardingService.initiateExit(request));
    }

    @Test
    @DisplayName("Initiate Exit - Fails when active offboarding is already in progress")
    void testInitiateExit_AlreadyActive() {
        when(employeeRepository.findById(25L)).thenReturn(Optional.of(employee));

        Offboarding activeOffboarding = new Offboarding();
        activeOffboarding.setId(50L);
        activeOffboarding.setStatus("PENDING");

        when(offboardingRepository.findByEmployeeId(25L)).thenReturn(Optional.of(activeOffboarding));

        InitiateExitRequest request = new InitiateExitRequest(
                25L, "RESIGNATION", LocalDate.of(2026, 10, 10), null, null, null, null, null, null
        );

        assertThrows(IllegalStateException.class, () -> offboardingService.initiateExit(request));
    }

    @Test
    @DisplayName("Initiate Exit - TERMINATION exit type does not require resignationDate")
    void testInitiateExit_Termination() {
        InitiateExitRequest request = new InitiateExitRequest(
                25L, "TERMINATION", LocalDate.of(2026, 9, 30), null,
                0, "PERFORMANCE", "Involuntary termination", 12L, null
        );

        when(employeeRepository.findById(25L)).thenReturn(Optional.of(employee));
        when(offboardingRepository.findByEmployeeId(25L)).thenReturn(Optional.empty());

        when(offboardingRepository.save(any(Offboarding.class))).thenAnswer(inv -> {
            Offboarding ob = inv.getArgument(0);
            ob.setId(102L);
            return ob;
        });

        InitiateExitResponse response = offboardingService.initiateExit(request);

        assertNotNull(response);
        assertEquals(102L, response.getOffboardingId());
        assertEquals("TERMINATION", response.getExitType());
        assertEquals(LocalDate.of(2026, 9, 30), response.getLastWorkingDate());
        assertNull(response.getResignationDate());
    }

    @Test
    @DisplayName("Assign Template to Request - Success and generates checklist tasks")
    void testAssignTemplateToRequest_Success() {
        Offboarding offboarding = new Offboarding();
        offboarding.setId(101L);
        offboarding.setEmployee(employee);
        offboarding.setStatus("PENDING");
        offboarding.setExitDate(LocalDate.of(2026, 10, 15));

        when(offboardingRepository.findById(101L)).thenReturn(Optional.of(offboarding));

        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(4L);
        template.setName("Senior Engineering Offboarding");
        template.setStatus(OffboardingTemplateStatus.ACTIVE);

        when(templateRepository.findByIdAndOrganizationId(4L, ORG_ID)).thenReturn(Optional.of(template));

        OffboardingClearanceTaskTemplate ct = new OffboardingClearanceTaskTemplate();
        ct.setTaskName("Revoke GitHub Access");
        ct.setDescription("IT Security Revocation");
        ct.setAssignToType(com.example.ems.offboarding.enums.ClearanceAssignToType.IT_MANAGER);
        ct.setMandatory(true);

        when(clearanceTaskRepository.findByTemplateIdAndOrganizationIdAndActiveTrueOrderBySequenceAsc(4L, ORG_ID))
                .thenReturn(List.of(ct));

        when(offboardingRepository.save(any(Offboarding.class))).thenAnswer(inv -> inv.getArgument(0));

        com.example.ems.offboarding.dto.AssignTemplateToRequestDto dto =
                new com.example.ems.offboarding.dto.AssignTemplateToRequestDto(4L);

        InitiateExitResponse response = offboardingService.assignTemplateToRequest(101L, dto);

        assertNotNull(response);
        assertEquals(101L, response.getOffboardingId());
        assertEquals(4L, response.getTemplateId());
        assertEquals("Senior Engineering Offboarding", response.getTemplateName());
        verify(offboardingTaskRepository).deleteByOffboardingId(101L);
        verify(offboardingTaskRepository).save(any(OffboardingTask.class));
    }

    @Test
    @DisplayName("Assign Template to Request - Throws error when template is INACTIVE")
    void testAssignTemplateToRequest_InactiveTemplate() {
        Offboarding offboarding = new Offboarding();
        offboarding.setId(101L);
        offboarding.setEmployee(employee);
        offboarding.setStatus("PENDING");

        when(offboardingRepository.findById(101L)).thenReturn(Optional.of(offboarding));

        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(4L);
        template.setName("Draft Policy");
        template.setStatus(OffboardingTemplateStatus.DRAFT);

        when(templateRepository.findByIdAndOrganizationId(4L, ORG_ID)).thenReturn(Optional.of(template));

        com.example.ems.offboarding.dto.AssignTemplateToRequestDto dto =
                new com.example.ems.offboarding.dto.AssignTemplateToRequestDto(4L);

        assertThrows(IllegalStateException.class, () -> offboardingService.assignTemplateToRequest(101L, dto));
    }
}
