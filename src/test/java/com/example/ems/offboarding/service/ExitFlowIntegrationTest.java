package com.example.ems.offboarding.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.entity.*;
import com.example.ems.offboarding.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ExitFlowIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;


    @Autowired
    private ExitKtProjectRepository projectRepository;

    @Autowired
    private ExitKtSystemAccessRepository systemAccessRepository;



    @Autowired
    private OffboardingRepository offboardingRepository;

    @Autowired
    private ExitKtService exitKtService;

    @Autowired
    private ExitRecommendationService exitRecommendationService;

    @Test
    public void testLazyKTCreationAndDefaultSeeding() {
        // Setup employee
        Employee employee = new Employee();
        employee.setFullName("James Carter");
        employee.setEmail("james.carter@company.com");
        employee.setEmployeeId("EMP55");
        employee.setDesignation("Senior Finance Analyst");
        employee = employeeRepository.save(employee);

        // Fetch plan (should lazily trigger creation and seed defaults)
        ExitKtPlan plan = exitKtService.getOrCreateKTPlan(employee.getId());

        assertNotNull(plan);
        assertNotNull(plan.getId());
        assertEquals("IN_PROGRESS", plan.getStatus());

        // Verify seeded project
        assertEquals(1, plan.getProjects().size());
        ExitKtProject project = plan.getProjects().get(0);
        assertEquals("Financial Forecasting Revamp", project.getProjectName());
        assertEquals("IN_PROGRESS", project.getStatus());
        assertEquals("MEDIUM", project.getRiskLevel());

        // Verify seeded contact
        assertEquals(1, plan.getContacts().size());
        ExitKtContact contact = plan.getContacts().get(0);
        assertEquals("Anita Sharma", contact.getName());
        assertEquals("Finance Manager", contact.getRole());

        // Verify seeded system credential
        assertEquals(1, plan.getSystemAccesses().size());
        ExitKtSystemAccess sys = plan.getSystemAccesses().get(0);
        assertEquals("SAP Finance", sys.getSystemName());
        assertEquals("PENDING", sys.getHandoverStatus());

        // Verify seeded task
        assertEquals(1, plan.getTasks().size());
        ExitKtTask task = plan.getTasks().get(0);
        assertEquals("Complete KT documentation", task.getTaskName());
        assertEquals("PENDING", task.getStatus());
    }

    @Test
    public void testSectionCompletionValidation() {
        // Setup employee
        Employee employee = new Employee();
        employee.setFullName("James Carter");
        employee.setEmail("james.carter@company.com");
        employee.setEmployeeId("EMP55");
        final Employee savedEmployee = employeeRepository.save(employee);

        // Fetch plan and default seed
        ExitKtPlan plan = exitKtService.getOrCreateKTPlan(savedEmployee.getId());
        ExitKtProject project = plan.getProjects().get(0);
        ExitKtSystemAccess system = plan.getSystemAccesses().get(0);

        // 1. Projects Section: Verify failure if handover notes are empty/null
        project.setHandoverNotes("");
        projectRepository.save(project);

        Exception ex1 = assertThrows(IllegalArgumentException.class, () -> {
            exitKtService.completeSection(savedEmployee.getId(), "PROJECTS");
        });
        assertTrue(ex1.getMessage().contains("Handover notes are required"));

        // Document project and verify PROJECTS section completion succeeds
        project.setHandoverNotes("Final forecasting model uploaded.");
        projectRepository.save(project);

        plan = exitKtService.completeSection(savedEmployee.getId(), "PROJECTS");
        assertTrue(plan.isProjectsCompleted());

        // 2. Systems Section: Verify failure if status/handover status is PENDING
        system.setHandoverStatus("PENDING");
        systemAccessRepository.save(system);

        Exception ex2 = assertThrows(IllegalArgumentException.class, () -> {
            exitKtService.completeSection(savedEmployee.getId(), "SYSTEM_CREDENTIALS");
        });
        assertTrue(ex2.getMessage().contains("Handover status is pending"));

        // Mark system transferred and verify SYSTEM_CREDENTIALS section completion succeeds
        system.setHandoverStatus("TRANSFERRED");
        systemAccessRepository.save(system);

        plan = exitKtService.completeSection(savedEmployee.getId(), "SYSTEM_CREDENTIALS");
        assertTrue(plan.isSystemCredentialsCompleted());
    }

    @Test
    public void testRecommendationLockingOnExitComplete() {
        // Setup employee & manager
        Employee manager = new Employee();
        manager.setFullName("Michael Scott");
        manager.setEmail("michael.scott@company.com");
        manager.setEmployeeId("MGR12");
        final Employee savedManager = employeeRepository.save(manager);

        Employee employee = new Employee();
        employee.setFullName("James Carter");
        employee.setEmail("james.carter@company.com");
        employee.setEmployeeId("EMP55");
        employee.setManager(savedManager);
        final Employee savedEmployee = employeeRepository.save(employee);

        // Initiate offboarding exit record
        Offboarding offboarding = new Offboarding();
        offboarding.setEmployee(savedEmployee);
        offboarding.setStatus("PENDING");
        offboarding.setCreatedAt(LocalDateTime.now());
        offboarding.setUpdatedAt(LocalDateTime.now());
        offboarding = offboardingRepository.save(offboarding);

        // 1. Submit recommendation when exit status is PENDING (should succeed)
        ExitRecommendation rec = exitRecommendationService.submitRecommendation(
                savedEmployee.getId(),
                4.5,
                "James has excellent skills.",
                savedManager.getId()
        );
        assertNotNull(rec);
        assertEquals(4.5, rec.getRating());
        assertFalse(rec.isLocked());

        // 2. Mark offboarding exit COMPLETED
        offboarding.setStatus("COMPLETED");
        offboardingRepository.save(offboarding);

        // 3. Try to submit/update recommendation when locked (should throw exception)
        Exception ex = assertThrows(IllegalStateException.class, () -> {
            exitRecommendationService.updateRecommendation(
                    savedEmployee.getId(),
                    4.8,
                    "Updated recommendation text."
            );
        });
        assertTrue(ex.getMessage().contains("Recommendation is locked"));
    }
}
