package com.example.ems.onboarding.controller;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.onboarding.entity.*;
import com.example.ems.onboarding.repository.*;
import com.example.ems.onboarding.service.TeamOnboardingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TeamOnboardingWorkflowTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private OnboardingTaskRepository onboardingTaskRepository;

    @Autowired
    private OnboardingPhaseRepository onboardingPhaseRepository;

    @Autowired
    private OnboardingTemplateRepository templateRepository;


    @Autowired
    private OnboardingTemplateSnapshotRepository templateSnapshotRepository;


    @Autowired
    private TeamOnboardingService teamOnboardingService;

    @Autowired
    private OnboardingDocumentRepository onboardingDocumentRepository;

    private Employee manager;
    private Employee employee;

    @BeforeEach
    public void setup() {
        // Create manager
        manager = new Employee();
        manager.setFullName("Onboarding Manager");
        manager.setEmail("manager.onboarding@test.com");
        manager.setEmployeeId("MGR_ONB_001");
        manager.setStatus("ACTIVE");
        manager = employeeRepository.save(manager);

        // Create new joiner employee
        employee = new Employee();
        employee.setFullName("New Joiner");
        employee.setEmail("joiner.new@test.com");
        employee.setEmployeeId("EMP_ONB_001");
        employee.setManager(manager);
        employee.setJoiningDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee = employeeRepository.save(employee);
    }

    @Test
    public void testOnboardingAutoSeedingAndTemplateVersioning() {
        // 1. Verify template seeder seeded the default template on startup
        List<OnboardingTemplate> templates = templateRepository.findAll();
        assertFalse(templates.isEmpty(), "At least one template should be seeded on startup.");
        OnboardingTemplate activeTemplate = templateRepository.findByIsActiveTrue()
                .orElseThrow(() -> new IllegalStateException("Active template should exist."));

        assertEquals(1, activeTemplate.getVersion());

        // 2. Initialize onboarding
        teamOnboardingService.initializeOnboardingForEmployee(employee);

        Onboarding onboarding = onboardingRepository.findByEmployeeId(employee.getId())
                .orElseThrow(() -> new AssertionError("Onboarding record was not initialized."));

        assertEquals("INITIATED", onboarding.getStatus());
        assertEquals(0, onboarding.getProgress());

        // 3. Verify snapshot is created
        Optional<OnboardingTemplateSnapshot> snapshotOpt = templateSnapshotRepository.findByOnboardingId(onboarding.getId());
        assertTrue(snapshotOpt.isPresent());
        OnboardingTemplateSnapshot snapshot = snapshotOpt.get();
        assertEquals(activeTemplate.getId(), snapshot.getTemplateId());
        assertEquals(1, snapshot.getVersion());

        // 4. Verify exactly 33 task instances were generated
        List<OnboardingTask> tasks = onboardingTaskRepository.findByOnboardingId(onboarding.getId());
        assertEquals(33, tasks.size(), "Checklist tasks count should be exactly 33.");

        // Verify phase caches were created
        List<OnboardingPhase> phases = onboardingPhaseRepository.findByOnboardingId(onboarding.getId());
        assertEquals(4, phases.size());
    }

    @Test
    public void testBuddyAssignmentAndTaskProgression() {
        // Initialize
        teamOnboardingService.initializeOnboardingForEmployee(employee);
        Onboarding onboarding = onboardingRepository.findByEmployeeId(employee.getId()).get();

        // Assign buddy
        Employee buddy = new Employee();
        buddy.setFullName("Buddy Engineer");
        buddy.setEmail("buddy.onboarding@test.com");
        buddy.setEmployeeId("BDY_ONB_001");
        buddy = employeeRepository.save(buddy);

        teamOnboardingService.assignBuddy(onboarding.getId(), buddy.getId());

        // Reload onboarding
        Onboarding updated = onboardingRepository.findById(onboarding.getId()).get();
        assertEquals(buddy.getId(), updated.getBuddy().getId());
        assertEquals("IN_PROGRESS", updated.getStatus(), "Status should transition to IN_PROGRESS upon buddy assignment.");

        // Get Pre-joining tasks (should be 6 tasks)
        List<OnboardingTask> preJoiningTasks = onboardingTaskRepository.findByOnboardingIdAndPhase(onboarding.getId(), "PRE_JOINING");
        assertEquals(6, preJoiningTasks.size());

        // Complete a task
        OnboardingTask firstTask = preJoiningTasks.get(0);
        teamOnboardingService.completeTask(firstTask.getId(), manager.getId());

        // Progress check (PRE_JOINING completed count = 1 / 6 = 16.66%. Overall Progress = 16.66% * 0.20 = 3.33% => rounds to 3%)
        Onboarding progressChecked = onboardingRepository.findById(onboarding.getId()).get();
        assertTrue(progressChecked.getProgress() > 0);
        assertEquals(3, progressChecked.getProgress());

        // Verify phase cache updated
        OnboardingPhase phase = onboardingPhaseRepository.findByOnboardingIdAndPhaseName(onboarding.getId(), "PRE_JOINING").get();
        assertEquals(1, phase.getCompletedTasks());
        assertEquals("IN_PROGRESS", phase.getStatus());
    }

    @Test
    public void testStateValidatorTransitionsAndPauseControls() {
        teamOnboardingService.initializeOnboardingForEmployee(employee);
        Onboarding onboarding = onboardingRepository.findByEmployeeId(employee.getId()).get();

        // Transition to IN_PROGRESS first via buddy assignment
        Employee buddy = new Employee();
        buddy.setFullName("Test Buddy");
        buddy.setEmail("test.buddy@test.com");
        buddy.setEmployeeId("T_BDY_001");
        buddy = employeeRepository.save(buddy);
        teamOnboardingService.assignBuddy(onboarding.getId(), buddy.getId());

        // Test Pause
        teamOnboardingService.pauseOnboarding(onboarding.getId());
        Onboarding paused = onboardingRepository.findById(onboarding.getId()).get();
        assertEquals("ON_HOLD", paused.getStatus());

        // Trying to complete a task in ON_HOLD must fail under State Machine Guard
        List<OnboardingTask> preJoiningTasks = onboardingTaskRepository.findByOnboardingIdAndPhase(onboarding.getId(), "PRE_JOINING");
        OnboardingTask task = preJoiningTasks.get(0);

        assertThrows(BadRequestException.class, () -> {
            teamOnboardingService.completeTask(task.getId(), manager.getId());
        });

        // Resume
        teamOnboardingService.resumeOnboarding(onboarding.getId());
        Onboarding resumed = onboardingRepository.findById(onboarding.getId()).get();
        assertEquals("IN_PROGRESS", resumed.getStatus());

        // Task completion should work now
        teamOnboardingService.completeTask(task.getId(), manager.getId());
        Onboarding completedTaskOnb = onboardingRepository.findById(onboarding.getId()).get();
        assertTrue(completedTaskOnb.getProgress() > 0);
    }

    @Test
    public void testProgressReconciliationJob() {
        teamOnboardingService.initializeOnboardingForEmployee(employee);
        Onboarding onboarding = onboardingRepository.findByEmployeeId(employee.getId()).get();

        // Induce drift manually in the phase cache table
        OnboardingPhase phase = onboardingPhaseRepository.findByOnboardingIdAndPhaseName(onboarding.getId(), "PRE_JOINING").get();
        phase.setCompletedTasks(99); // Drift count
        onboardingPhaseRepository.save(phase);

        // Run reconciliation job
        teamOnboardingService.reconcileProgressCache(onboarding.getId());

        // Verify corrected cache counts
        OnboardingPhase reconciledPhase = onboardingPhaseRepository.findByOnboardingIdAndPhaseName(onboarding.getId(), "PRE_JOINING").get();
        assertEquals(0, reconciledPhase.getCompletedTasks(), "Nightly reconciliation must self-heal counts from task records.");
    }

    @Test
    public void testCompletionGatesAndOverdueScan() {
        teamOnboardingService.initializeOnboardingForEmployee(employee);
        Onboarding onboarding = onboardingRepository.findByEmployeeId(employee.getId()).get();

        // Complete all 33 tasks
        List<OnboardingTask> allTasks = onboardingTaskRepository.findByOnboardingId(onboarding.getId());
        for (OnboardingTask t : allTasks) {
            t.setStatus("COMPLETED");
            t.setCompletedAt(LocalDateTime.now());
            t.setCompletedBy(manager);
            onboardingTaskRepository.save(t);
        }

        // Trigger progress update (simulate completions)
        teamOnboardingService.reconcileProgressCache(onboarding.getId());

        // Progress is 100%, but no verified documents. Onboarding must remain IN_PROGRESS!
        Onboarding refreshedOnb = onboardingRepository.findById(onboarding.getId()).get();
        assertEquals(100, refreshedOnb.getProgress());
        assertEquals("IN_PROGRESS", refreshedOnb.getStatus(), "Onboarding completion must be document-verification gated.");

        // Upload and verify document
        OnboardingDocument doc = new OnboardingDocument();
        doc.setOnboarding(onboarding);
        doc.setDocumentType("AADHAR");
        doc.setFileName("aadhar.pdf");
        doc.setVerificationStatus("UPLOADED");
        onboardingDocumentRepository.save(doc);

        // Verify doc
        teamOnboardingService.verifyDocument(doc.getId(), "VERIFIED", "Approved");

        // Verify onboarding transitions to COMPLETED status
        Onboarding finalOnboarding = onboardingRepository.findById(onboarding.getId()).get();
        assertEquals("COMPLETED", finalOnboarding.getStatus(), "Onboarding transitions to COMPLETED when tasks and documents are all verified.");
    }
}
