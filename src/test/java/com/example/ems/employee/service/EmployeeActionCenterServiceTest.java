package com.example.ems.employee.service;

import com.example.ems.employee.dto.dashboard.EmployeeActionCenterResponseDto;
import com.example.ems.employee.dto.dashboard.EmployeeActionDto;
import com.example.ems.employee.dto.dashboard.EmployeeActionLinkDto;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.service.actioncenter.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void testGetPendingActions_AggregationSortingAndExclusion() {
        Employee employee = new Employee();
        employee.setId(100L);
        Long orgId = 1L;

        // Leave: Normal priority, due 2026-09-25
        EmployeeActionDto leaveAction = new EmployeeActionDto(
                "LEAVE-501", "LEAVE_APPROVAL", "Leave pending", "PENDING", "NORMAL", "2026-09-25",
                "Manager review required", new EmployeeActionLinkDto("View Task", "/employee/leave/501")
        );
        when(leaveProvider.getActions(employee, orgId)).thenReturn(List.of(leaveAction));

        // Document: High priority, overdue, null due date
        EmployeeActionDto docAction = new EmployeeActionDto(
                "DOC-201", "DOCUMENT_UPLOAD", "Upload document", "OVERDUE", "HIGH", null,
                "Mandatory document", new EmployeeActionLinkDto("View Task", "/employee/documents")
        );
        when(documentProvider.getActions(employee, orgId)).thenReturn(List.of(docAction));

        // Performance: Normal priority, due 2026-09-20 (earlier than leave)
        EmployeeActionDto perfAction = new EmployeeActionDto(
                "APP-301", "SELF_REVIEW", "Complete self-review", "DUE_SOON", "NORMAL", "2026-09-20",
                "Appraisal cycle", new EmployeeActionLinkDto("View Task", "/employee/performance/self-reviews/301")
        );
        when(performanceProvider.getActions(employee, orgId)).thenReturn(List.of(perfAction));

        // Training: Completed item (should be filtered out!)
        EmployeeActionDto completedTraining = new EmployeeActionDto(
                "TRN-401", "TRAINING", "Finished course", "COMPLETED", "NORMAL", "2026-09-10",
                "Completed", new EmployeeActionLinkDto("View Task", "/employee/training/401")
        );
        when(trainingProvider.getActions(employee, orgId)).thenReturn(List.of(completedTraining));

        EmployeeActionCenterResponseDto response = actionCenterService.getActionCenterResponse(employee, orgId);

        assertThat(response).isNotNull();
        // Completed training must be excluded, total = 3
        assertThat(response.total()).isEqualTo(3);
        assertThat(response.items()).hasSize(3);

        // Sorting check:
        // Rank 1: HIGH priority (DOC-201)
        assertThat(response.items().get(0).id()).isEqualTo("DOC-201");
        assertThat(response.items().get(0).priority()).isEqualTo("HIGH");

        // Rank 2: NORMAL priority with earlier dueDate (APP-301, due 2026-09-20)
        assertThat(response.items().get(1).id()).isEqualTo("APP-301");

        // Rank 3: NORMAL priority with later dueDate (LEAVE-501, due 2026-09-25)
        assertThat(response.items().get(2).id()).isEqualTo("LEAVE-501");
    }

    @Test
    void testEmptyActionCenterWorks() {
        Employee employee = new Employee();
        employee.setId(100L);
        Long orgId = 1L;

        when(leaveProvider.getActions(employee, orgId)).thenReturn(Collections.emptyList());
        when(documentProvider.getActions(employee, orgId)).thenReturn(Collections.emptyList());
        when(performanceProvider.getActions(employee, orgId)).thenReturn(Collections.emptyList());
        when(trainingProvider.getActions(employee, orgId)).thenReturn(Collections.emptyList());

        EmployeeActionCenterResponseDto response = actionCenterService.getActionCenterResponse(employee, orgId);

        assertThat(response).isNotNull();
        assertThat(response.total()).isEqualTo(0);
        assertThat(response.items()).isEmpty();
    }
}
