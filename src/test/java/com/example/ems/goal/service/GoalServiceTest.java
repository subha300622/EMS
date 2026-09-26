package com.example.ems.goal.service;

import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.goal.domain.Goal;
import com.example.ems.goal.domain.GoalApprovalPolicy;
import com.example.ems.goal.domain.GoalConfig;
import com.example.ems.goal.dto.CreateGoalRequest;
import com.example.ems.goal.dto.GoalResponse;
import com.example.ems.goal.repository.GoalRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalConfigService configService;

    @Mock
    private GoalApprovalPolicyService approvalPolicyService;

    @Mock
    private GoalActivityService activityService;

    @Mock
    private ApprovalFacade approvalFacade;

    @InjectMocks
    private GoalService goalService;

    private final Long orgId = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Scenario 3: Create -> No Approval -> ACTIVE")
    void testCreateValidGoal_NoApproval() {
        CreateGoalRequest req = new CreateGoalRequest();
        req.setGoalName("Q3 Sales Target");
        req.setCategory("SALES");
        req.setType("INDIVIDUAL");
        req.setPriority("HIGH");
        req.setWeightage(10);
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusMonths(3));

        GoalConfig config = new GoalConfig();
        config.setMinWeightage(1);
        config.setMaxWeightage(100);
        config.setRequireApprovalForCreate(false);

        when(configService.getOrCreateConfig()).thenReturn(config);
        when(configService.generateNextGoalNumber()).thenReturn("GOAL-1-00001");
        when(approvalPolicyService.evaluateApprovalPolicy(any(Goal.class), eq("CREATE")))
                .thenReturn(Optional.empty());
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> {
            Goal g = inv.getArgument(0);
            g.setId(100L);
            return g;
        });

        GoalResponse response = goalService.createGoal(req, 10L, "John Doe", "EMPLOYEE");

        assertNotNull(response);
        assertEquals("GOAL-1-00001", response.getGoalNumber());
        assertEquals("ACTIVE", response.getStatus());
        verify(activityService).logActivity(eq(100L), eq(10L), eq("John Doe"), eq("EMPLOYEE"), eq("GOAL_CREATED"), anyString(), any());
    }

    @Test
    @DisplayName("Scenario 1: Create -> Approval Required -> PENDING_APPROVAL")
    void testCreateGoal_RequiresApproval() {
        CreateGoalRequest req = new CreateGoalRequest();
        req.setGoalName("Enterprise Transformation");
        req.setType("ORGANIZATION");

        GoalConfig config = new GoalConfig();
        when(configService.getOrCreateConfig()).thenReturn(config);
        when(configService.generateNextGoalNumber()).thenReturn("GOAL-1-00002");

        GoalApprovalPolicy policy = new GoalApprovalPolicy();
        policy.setApprovalRequired(true);
        policy.setApprovalType("MULTI_LEVEL");
        policy.setApproverRole("HR");
        policy.setApprovalLevels(2);

        when(approvalPolicyService.evaluateApprovalPolicy(any(Goal.class), eq("CREATE")))
                .thenReturn(Optional.of(policy));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> {
            Goal g = inv.getArgument(0);
            g.setId(200L);
            return g;
        });

        GoalResponse response = goalService.createGoal(req, 10L, "John", "EMPLOYEE");

        assertEquals("PENDING_APPROVAL", response.getStatus());
        verify(approvalFacade).startApproval(any());
    }

    @Test
    @DisplayName("Scenario 4: Complete -> Completion Approval Required -> PENDING_APPROVAL")
    void testCompleteGoal_RequiresApproval() {
        Goal goal = new Goal();
        goal.setId(300L);
        goal.setOrganizationId(orgId);
        goal.setStatus("ACTIVE");

        GoalConfig config = new GoalConfig();
        when(configService.getOrCreateConfig()).thenReturn(config);
        when(goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(300L, orgId)).thenReturn(Optional.of(goal));

        GoalApprovalPolicy completionPolicy = new GoalApprovalPolicy();
        completionPolicy.setApprovalRequired(true);
        completionPolicy.setApprovalType("MANAGER");
        completionPolicy.setApproverRole("REPORTING_MANAGER");

        when(approvalPolicyService.evaluateApprovalPolicy(any(Goal.class), eq("COMPLETE")))
                .thenReturn(Optional.of(completionPolicy));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));

        GoalResponse response = goalService.completeGoal(300L, 10L, "John", "MANAGER");

        assertEquals("PENDING_APPROVAL", response.getStatus());
        verify(approvalFacade).startApproval(any());
    }

    @Test
    @DisplayName("Scenario 4: Complete -> No Completion Approval -> COMPLETED (100%)")
    void testCompleteGoal_DirectCompletion() {
        Goal goal = new Goal();
        goal.setId(350L);
        goal.setOrganizationId(orgId);
        goal.setStatus("ACTIVE");

        GoalConfig config = new GoalConfig();
        config.setRequireApprovalForComplete(false);
        when(configService.getOrCreateConfig()).thenReturn(config);
        when(goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(350L, orgId)).thenReturn(Optional.of(goal));
        when(approvalPolicyService.evaluateApprovalPolicy(any(Goal.class), eq("COMPLETE")))
                .thenReturn(Optional.empty());
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));

        GoalResponse response = goalService.completeGoal(350L, 10L, "John", "MANAGER");

        assertEquals("COMPLETED", response.getStatus());
        assertEquals(100, response.getProgress());
    }

    @Test
    @DisplayName("Scenario 1: Create Goal with invalid dates -> throws IllegalArgumentException")
    void testCreateGoal_InvalidDates() {
        CreateGoalRequest req = new CreateGoalRequest();
        req.setGoalName("Invalid Goal");
        req.setStartDate(LocalDate.now().plusDays(10));
        req.setEndDate(LocalDate.now());

        GoalConfig config = new GoalConfig();
        when(configService.getOrCreateConfig()).thenReturn(config);

        assertThrows(IllegalArgumentException.class, () -> goalService.createGoal(req, 10L, "John Doe", "EMPLOYEE"));
    }

    @Test
    @DisplayName("Scenario 3: State Machine -> Activate, Hold, Resume Transitions")
    void testGoalStateMachine() {
        Goal goal = new Goal();
        goal.setId(50L);
        goal.setOrganizationId(orgId);
        goal.setStatus("DRAFT");
        goal.setProgress(0);

        when(goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(50L, orgId)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));

        // Activate
        GoalResponse activeResp = goalService.activateGoal(50L, 10L, "John", "MANAGER");
        assertEquals("ACTIVE", activeResp.getStatus());

        // Hold
        GoalResponse holdResp = goalService.holdGoal(50L, 10L, "John", "MANAGER");
        assertEquals("ON_HOLD", holdResp.getStatus());

        // Resume
        GoalResponse resumeResp = goalService.resumeGoal(50L, 10L, "John", "MANAGER");
        assertEquals("ACTIVE", resumeResp.getStatus());
    }

    @Test
    @DisplayName("Scenario 2: Tenant Security -> Require Organization ID")
    void testTenantSecurityIsolation() {
        TenantContext.clear();
        CreateGoalRequest req = new CreateGoalRequest();
        req.setGoalName("Cross-Tenant Attack");

        assertThrows(IllegalStateException.class, () -> goalService.createGoal(req, 10L, "Attacker", "EMPLOYEE"));
    }
}
