package com.example.ems.employee.service.actioncenter;

import com.example.ems.employee.dto.dashboard.EmployeeActionCenterResponseDto;
import com.example.ems.employee.dto.dashboard.EmployeeActionDto;
import com.example.ems.employee.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class EmployeeActionCenterService {

    @Autowired
    private LeaveActionProvider leaveProvider;

    @Autowired
    private DocumentActionProvider documentProvider;

    @Autowired
    private PerformanceActionProvider performanceProvider;

    @Autowired
    private TrainingActionProvider trainingProvider;

    public EmployeeActionCenterResponseDto getActionCenterResponse(Employee employee, Long orgId) {
        List<EmployeeActionDto> pendingActions = getPendingActions(employee, orgId);
        return new EmployeeActionCenterResponseDto(pendingActions.size(), pendingActions);
    }

    public List<EmployeeActionDto> getPendingActions(Employee employee, Long orgId) {
        if (employee == null) {
            return List.of();
        }

        List<EmployeeActionDto> actions = new ArrayList<>();
        actions.addAll(leaveProvider.getActions(employee, orgId));
        actions.addAll(documentProvider.getActions(employee, orgId));
        actions.addAll(performanceProvider.getActions(employee, orgId));
        actions.addAll(trainingProvider.getActions(employee, orgId));

        return actions.stream()
                .filter(a -> a.status() != null && !"COMPLETED".equalsIgnoreCase(a.status()))
                .sorted(Comparator
                        .comparingInt((EmployeeActionDto a) -> priorityRank(a.priority()))
                        .thenComparing((EmployeeActionDto a) -> parseDateSafely(a.dueDate()),
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(EmployeeActionDto::id, Comparator.nullsLast(Comparator.naturalOrder()))
                )
                .toList();
    }

    private int priorityRank(String priority) {
        if (priority == null) return 2;
        return switch (priority.toUpperCase()) {
            case "HIGH", "CRITICAL", "URGENT" -> 1;
            case "NORMAL", "MEDIUM" -> 2;
            case "LOW" -> 3;
            default -> 2;
        };
    }

    private LocalDate parseDateSafely(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception ignored) {
            return null;
        }
    }
}
