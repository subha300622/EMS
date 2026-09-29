package com.example.ems.goal.service;

import com.example.ems.goal.domain.Goal;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalCascadeServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private GoalCascadeService cascadeService;

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
    @DisplayName("Scenario 5: Cascading Goals -> Weighted-average calculation child progress updates parent")
    void testRecalculateParentProgress_WeightedAverage() {
        Long parentId = 10L;

        Goal parentGoal = new Goal();
        parentGoal.setId(parentId);
        parentGoal.setOrganizationId(orgId);
        parentGoal.setProgress(0);

        // Child 1: Progress 80%, Weight 2
        Goal child1 = new Goal();
        child1.setId(11L);
        child1.setProgress(80);
        child1.setWeightage(2);

        // Child 2: Progress 50%, Weight 1
        Goal child2 = new Goal();
        child2.setId(12L);
        child2.setProgress(50);
        child2.setWeightage(1);

        // Weighted Average = ((80 * 2) + (50 * 1)) / (2 + 1) = 210 / 3 = 70%
        when(goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(parentId, orgId)).thenReturn(Optional.of(parentGoal));
        when(goalRepository.findByOrganizationIdAndParentGoalIdAndIsDeletedFalse(orgId, parentId)).thenReturn(List.of(child1, child2));

        cascadeService.recalculateParentProgress(parentId);

        assertEquals(70, parentGoal.getProgress());
        verify(goalRepository).save(parentGoal);
    }
}
