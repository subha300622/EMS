package com.example.ems.offboarding.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.EmployeeTemplateAssignmentRequest;
import com.example.ems.offboarding.dto.EmployeeTemplateAssignmentResponse;
import com.example.ems.offboarding.entity.OffboardingEmployeeTemplateAssignment;
import com.example.ems.offboarding.entity.OffboardingTemplate;
import com.example.ems.offboarding.enums.OffboardingTemplateStatus;
import com.example.ems.offboarding.repository.OffboardingEmployeeTemplateAssignmentRepository;
import com.example.ems.offboarding.repository.OffboardingTemplateRepository;
import com.example.ems.organization.entity.Organization;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OffboardingTemplateAssignmentServiceTest {

    @Mock
    private OffboardingEmployeeTemplateAssignmentRepository assignmentRepository;

    @Mock
    private OffboardingTemplateRepository templateRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OffboardingTemplateAssignmentService assignmentService;

    private static final Long ORG_ID = 100L;
    private Organization testOrg;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_ID);

        testOrg = new Organization();
        testOrg.setId(ORG_ID);
        testOrg.setName("Acme Corp");

        testEmployee = new Employee();
        testEmployee.setId(25L);
        testEmployee.setFullName("John Doe");
        testEmployee.setEmployeeId("EMP025");
        testEmployee.setDepartment("Engineering");
        testEmployee.setEmploymentType("FULL_TIME");
        testEmployee.setDesignation("SENIOR");
        testEmployee.setOrganization(testOrg);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("1. Assign template to individual employee successfully")
    void testAssignTemplateToEmployee_Success() {
        OffboardingTemplate template = new OffboardingTemplate();
        template.setId(4L);
        template.setName("Standard Resignation Template");
        template.setOrganizationId(ORG_ID);
        template.setStatus(OffboardingTemplateStatus.ACTIVE);

        when(employeeRepository.findById(25L)).thenReturn(Optional.of(testEmployee));
        when(templateRepository.findByIdAndOrganizationId(4L, ORG_ID)).thenReturn(Optional.of(template));
        when(assignmentRepository.findByOrganizationIdAndEmployeeIdAndExitType(ORG_ID, 25L, "RESIGNATION"))
                .thenReturn(Optional.empty());

        when(assignmentRepository.save(any(OffboardingEmployeeTemplateAssignment.class)))
                .thenAnswer(inv -> {
                    OffboardingEmployeeTemplateAssignment a = inv.getArgument(0);
                    a.setId(1L);
                    return a;
                });

        EmployeeTemplateAssignmentRequest req = new EmployeeTemplateAssignmentRequest(4L, "RESIGNATION");
        EmployeeTemplateAssignmentResponse response = assignmentService.assignTemplateToEmployee(25L, req);

        assertNotNull(response);
        assertEquals(25L, response.getEmployeeId());
        assertEquals("John Doe", response.getEmployeeName());
        assertEquals(4L, response.getTemplateId());
        assertEquals("Standard Resignation Template", response.getTemplateName());
        assertEquals("RESIGNATION", response.getExitType());
    }

    @Test
    @DisplayName("2. Cannot assign INACTIVE template")
    void testAssignTemplate_InactiveThrowsException() {
        OffboardingTemplate inactiveTemplate = new OffboardingTemplate();
        inactiveTemplate.setId(5L);
        inactiveTemplate.setName("Deprecated Template");
        inactiveTemplate.setOrganizationId(ORG_ID);
        inactiveTemplate.setStatus(OffboardingTemplateStatus.INACTIVE);

        when(employeeRepository.findById(25L)).thenReturn(Optional.of(testEmployee));
        when(templateRepository.findByIdAndOrganizationId(5L, ORG_ID)).thenReturn(Optional.of(inactiveTemplate));

        EmployeeTemplateAssignmentRequest req = new EmployeeTemplateAssignmentRequest(5L, "RESIGNATION");
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                assignmentService.assignTemplateToEmployee(25L, req));

        assertTrue(ex.getMessage().contains("Cannot assign an INACTIVE"));
    }

    @Test
    @DisplayName("3. Individual assignment overrides high-scoring automatic template")
    void testResolveTemplate_IndividualAssignmentWins() {
        OffboardingTemplate generalTemplate = new OffboardingTemplate();
        generalTemplate.setId(1L);
        generalTemplate.setName("Global Template");
        generalTemplate.setOrganizationId(ORG_ID);
        generalTemplate.setStatus(OffboardingTemplateStatus.ACTIVE);

        OffboardingTemplate highlySpecificTemplate = new OffboardingTemplate();
        highlySpecificTemplate.setId(2L);
        highlySpecificTemplate.setName("Engineering Senior Full-Time Template");
        highlySpecificTemplate.setOrganizationId(ORG_ID);
        highlySpecificTemplate.setStatus(OffboardingTemplateStatus.ACTIVE);
        highlySpecificTemplate.setDepartmentIdsJson("[\"Engineering\"]");
        highlySpecificTemplate.setEmploymentTypesJson("[\"FULL_TIME\"]");
        highlySpecificTemplate.setEmployeeTypesJson("[\"SENIOR\"]");

        OffboardingTemplate manuallyAssignedTemplate = new OffboardingTemplate();
        manuallyAssignedTemplate.setId(4L);
        manuallyAssignedTemplate.setName("Manually Assigned Custom Template");
        manuallyAssignedTemplate.setOrganizationId(ORG_ID);
        manuallyAssignedTemplate.setStatus(OffboardingTemplateStatus.ACTIVE);

        OffboardingEmployeeTemplateAssignment individualAssignment = new OffboardingEmployeeTemplateAssignment(
                ORG_ID, testEmployee, manuallyAssignedTemplate, "RESIGNATION"
        );

        when(assignmentRepository.findByOrganizationIdAndEmployeeIdAndExitType(ORG_ID, 25L, "RESIGNATION"))
                .thenReturn(Optional.of(individualAssignment));

        OffboardingTemplate resolved = assignmentService.resolveTemplateForEmployee(testEmployee, "RESIGNATION");

        assertNotNull(resolved);
        assertEquals(4L, resolved.getId());
        assertEquals("Manually Assigned Custom Template", resolved.getName());
    }

    @Test
    @DisplayName("4. Specificity scoring matches most specific template (Dept + EmpType + Designation)")
    void testResolveTemplate_SpecificityScoring() {
        when(assignmentRepository.findByOrganizationIdAndEmployeeIdAndExitType(ORG_ID, 25L, "RESIGNATION"))
                .thenReturn(Optional.empty());
        when(assignmentRepository.findByOrganizationIdAndEmployeeIdAndExitType(ORG_ID, 25L, "ALL"))
                .thenReturn(Optional.empty());

        OffboardingTemplate tplA = new OffboardingTemplate();
        tplA.setId(1L);
        tplA.setName("Global Default Template");
        tplA.setStatus(OffboardingTemplateStatus.ACTIVE);

        OffboardingTemplate tplB = new OffboardingTemplate();
        tplB.setId(2L);
        tplB.setName("Engineering Template");
        tplB.setStatus(OffboardingTemplateStatus.ACTIVE);
        tplB.setDepartmentIdsJson("[\"Engineering\"]"); // +100

        OffboardingTemplate tplC = new OffboardingTemplate();
        tplC.setId(3L);
        tplC.setName("Engineering + FullTime + Senior Template");
        tplC.setStatus(OffboardingTemplateStatus.ACTIVE);
        tplC.setDepartmentIdsJson("[\"Engineering\"]"); // +100
        tplC.setEmploymentTypesJson("[\"FULL_TIME\"]"); // +50
        tplC.setEmployeeTypesJson("[\"SENIOR\"]");      // +25 -> Total: 175

        when(templateRepository.findByOrganizationIdAndStatus(ORG_ID, OffboardingTemplateStatus.ACTIVE))
                .thenReturn(List.of(tplA, tplB, tplC));

        OffboardingTemplate resolved = assignmentService.resolveTemplateForEmployee(testEmployee, "RESIGNATION");

        assertNotNull(resolved);
        assertEquals(3L, resolved.getId());
        assertEquals("Engineering + FullTime + Senior Template", resolved.getName());
    }

    @Test
    @DisplayName("5. Hard matching disqualifies mismatched department even if other criteria match")
    void testResolveTemplate_HardMatchingDisqualifies() {
        when(assignmentRepository.findByOrganizationIdAndEmployeeIdAndExitType(ORG_ID, 25L, "RESIGNATION"))
                .thenReturn(Optional.empty());
        when(assignmentRepository.findByOrganizationIdAndEmployeeIdAndExitType(ORG_ID, 25L, "ALL"))
                .thenReturn(Optional.empty());

        OffboardingTemplate tplMismatched = new OffboardingTemplate();
        tplMismatched.setId(10L);
        tplMismatched.setName("Finance Senior FullTime Template");
        tplMismatched.setStatus(OffboardingTemplateStatus.ACTIVE);
        tplMismatched.setDepartmentIdsJson("[\"Finance\"]"); // Does NOT match John (Engineering)
        tplMismatched.setEmploymentTypesJson("[\"FULL_TIME\"]");
        tplMismatched.setEmployeeTypesJson("[\"SENIOR\"]");

        OffboardingTemplate tplGlobal = new OffboardingTemplate();
        tplGlobal.setId(1L);
        tplGlobal.setName("Global Default Template");
        tplGlobal.setStatus(OffboardingTemplateStatus.ACTIVE);

        when(templateRepository.findByOrganizationIdAndStatus(ORG_ID, OffboardingTemplateStatus.ACTIVE))
                .thenReturn(List.of(tplMismatched, tplGlobal));

        OffboardingTemplate resolved = assignmentService.resolveTemplateForEmployee(testEmployee, "RESIGNATION");

        assertNotNull(resolved);
        assertEquals(1L, resolved.getId());
        assertEquals("Global Default Template", resolved.getName());
    }

    @Test
    @DisplayName("6. Remove assignment with exitType parameter")
    void testRemoveAssignment_WithExitType() {
        when(employeeRepository.findById(25L)).thenReturn(Optional.of(testEmployee));

        assignmentService.removeAssignment(25L, "RESIGNATION");

        verify(assignmentRepository, times(1))
                .deleteByOrganizationIdAndEmployeeIdAndExitType(ORG_ID, 25L, "RESIGNATION");
    }
}
