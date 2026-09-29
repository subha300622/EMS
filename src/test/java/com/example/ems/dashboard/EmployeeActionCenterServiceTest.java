package com.example.ems.dashboard;

import com.example.ems.employee.dto.dashboard.EmployeeActionCenterResponseDto;
import com.example.ems.employee.dto.dashboard.EmployeeActionDto;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.service.actioncenter.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeActionCenterServiceTest {

    @Mock
    private LeaveActionProvider leaveProvider;

    @Mock
    private DocumentActionProvider documentProvider;

    @Mock
    private PerformanceActionProvider performanceProvider;

    @Mock
    private TrainingActionProvider trainingProvider;

    @InjectMocks
    private EmployeeActionCenterService actionCenterService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(100L);
        employee.setFullName("John Doe");
    }

    private EmployeeActionDto createAction(String id, String type, String title, String status, String priority, String dueDate) {
        return new EmployeeActionDto(id, type, title, status, priority, dueDate, "Description for " + id, null);
    }

    @Test
    void testShouldAggregateEmployeeActions_AllProviders() {
        when(leaveProvider.getActions(employee, 1L))
                .thenReturn(List.of(createAction("ACT-LEAVE-1", "LEAVE_APPROVAL", "Pending Leave Approval", "PENDING", "NORMAL", "2026-10-01")));
        when(documentProvider.getActions(employee, 1L))
                .thenReturn(List.of(createAction("ACT-DOC-1", "DOCUMENT_UPLOAD", "Missing Passport Document", "OVERDUE", "HIGH", "2026-09-20")));
        when(performanceProvider.getActions(employee, 1L))
                .thenReturn(List.of(createAction("ACT-PERF-1", "SELF_REVIEW", "Appraisal Self-Review Due", "DUE_SOON", "HIGH", "2026-09-28")));
        when(trainingProvider.getActions(employee, 1L))
                .thenReturn(List.of(createAction("ACT-TRAIN-1", "TRAINING_COMPLETION", "Security Compliance Course", "PENDING", "NORMAL", "2026-10-05")));

        EmployeeActionCenterResponseDto response = actionCenterService.getActionCenterResponse(employee, 1L);

        assertNotNull(response);
        assertEquals(4, response.total());
        assertEquals(4, response.items().size());
    }

    @Test
    void testShouldFilterOutCompletedActions() {
        EmployeeActionDto pendingAction = createAction("ACT-1", "LEAVE_APPROVAL", "Pending Leave", "PENDING", "NORMAL", "2026-10-01");
        EmployeeActionDto completedAction = createAction("ACT-2", "DOCUMENT_UPLOAD", "Completed Upload", "COMPLETED", "NORMAL", "2026-09-15");

        when(leaveProvider.getActions(employee, 1L)).thenReturn(List.of(pendingAction));
        when(documentProvider.getActions(employee, 1L)).thenReturn(List.of(completedAction));
        when(performanceProvider.getActions(employee, 1L)).thenReturn(Collections.emptyList());
        when(trainingProvider.getActions(employee, 1L)).thenReturn(Collections.emptyList());

        List<EmployeeActionDto> actions = actionCenterService.getPendingActions(employee, 1L);

        assertEquals(1, actions.size());
        assertEquals("ACT-1", actions.get(0).id());
        assertFalse(actions.stream().anyMatch(a -> "COMPLETED".equalsIgnoreCase(a.status())));
    }

    @Test
    void testShouldPrioritizeHighAndOverdueActionsFirst() {
        EmployeeActionDto lowAction = createAction("LOW-1", "TRAIN", "Optional Webinar", "PENDING", "LOW", "2026-10-10");
        EmployeeActionDto highAction = createAction("HIGH-1", "DOC", "Urgent Tax Declaration", "OVERDUE", "HIGH", "2026-09-10");
        EmployeeActionDto normalAction = createAction("NORM-1", "LEAVE", "Leave Request", "PENDING", "NORMAL", "2026-10-01");

        when(leaveProvider.getActions(employee, 1L)).thenReturn(List.of(lowAction));
        when(documentProvider.getActions(employee, 1L)).thenReturn(List.of(highAction));
        when(performanceProvider.getActions(employee, 1L)).thenReturn(List.of(normalAction));
        when(trainingProvider.getActions(employee, 1L)).thenReturn(Collections.emptyList());

        List<EmployeeActionDto> actions = actionCenterService.getPendingActions(employee, 1L);

        assertEquals(3, actions.size());
        // High priority must come first
        assertEquals("HIGH-1", actions.get(0).id());
        assertEquals("NORM-1", actions.get(1).id());
        assertEquals("LOW-1", actions.get(2).id());
    }

    @Test
    void testShouldReturnEmptyListWhenNoPendingTasks() {
        when(leaveProvider.getActions(employee, 1L)).thenReturn(Collections.emptyList());
        when(documentProvider.getActions(employee, 1L)).thenReturn(Collections.emptyList());
        when(performanceProvider.getActions(employee, 1L)).thenReturn(Collections.emptyList());
        when(trainingProvider.getActions(employee, 1L)).thenReturn(Collections.emptyList());

        EmployeeActionCenterResponseDto response = actionCenterService.getActionCenterResponse(employee, 1L);

        assertNotNull(response);
        assertEquals(0, response.total());
        assertTrue(response.items().isEmpty());
    }

    @Test
    void testShouldHandleNullEmployeeGracefully() {
        List<EmployeeActionDto> actions = actionCenterService.getPendingActions(null, 1L);
        assertNotNull(actions);
        assertTrue(actions.isEmpty());
    }

    @Test
    void testActionStatusProgression_PendingToCompletedDisappears() {
        // Step 1: Document action is pending/overdue -> appears
        EmployeeActionDto pendingDoc = createAction("DOC-101", "DOCUMENT_UPLOAD", "Upload Degree Certificate", "OVERDUE", "HIGH", "2026-09-20");
        when(documentProvider.getActions(employee, 1L)).thenReturn(List.of(pendingDoc));
        when(leaveProvider.getActions(employee, 1L)).thenReturn(Collections.emptyList());
        when(performanceProvider.getActions(employee, 1L)).thenReturn(Collections.emptyList());
        when(trainingProvider.getActions(employee, 1L)).thenReturn(Collections.emptyList());

        List<EmployeeActionDto> beforeCompletion = actionCenterService.getPendingActions(employee, 1L);
        assertEquals(1, beforeCompletion.size());
        assertEquals("DOC-101", beforeCompletion.get(0).id());

        // Step 2: Employee completes upload -> status becomes COMPLETED -> filtered out
        EmployeeActionDto completedDoc = createAction("DOC-101", "DOCUMENT_UPLOAD", "Upload Degree Certificate", "COMPLETED", "HIGH", "2026-09-20");
        when(documentProvider.getActions(employee, 1L)).thenReturn(List.of(completedDoc));

        List<EmployeeActionDto> afterCompletion = actionCenterService.getPendingActions(employee, 1L);
        assertEquals(0, afterCompletion.size(), "Completed action must no longer appear in action center");
    }
}
