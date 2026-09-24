package com.example.ems.employee.service.actioncenter;

import com.example.ems.employee.dto.dashboard.EmployeeActionDto;
import com.example.ems.employee.dto.dashboard.EmployeeActionLinkDto;
import com.example.ems.employee.entity.Employee;
import com.example.ems.training.entity.TrainingProgress;
import com.example.ems.training.entity.TrainingStatus;
import com.example.ems.training.repository.TrainingProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TrainingActionProvider implements EmployeeActionProvider {

    private static final Logger log = LoggerFactory.getLogger(TrainingActionProvider.class);

    @Autowired
    private TrainingProgressRepository progressRepository;

    @Override
    public List<EmployeeActionDto> getActions(Employee employee, Long orgId) {
        if (employee == null || employee.getId() == null) {
            return Collections.emptyList();
        }

        try {
            List<TrainingProgress> list = progressRepository.findByEmployeeId(employee.getId());
            if (list == null || list.isEmpty()) {
                if (employee.getEmail() != null) {
                    list = progressRepository.findByEmployeeEmail(employee.getEmail());
                }
            }

            if (list == null || list.isEmpty()) {
                return Collections.emptyList();
            }

            List<EmployeeActionDto> actions = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (TrainingProgress tp : list) {
                if (tp.getStatus() == TrainingStatus.COMPLETED || tp.getStatus() == TrainingStatus.CERTIFIED) {
                    continue;
                }
                if (tp.getAssignment() == null || tp.getAssignment().getCourse() == null) {
                    continue;
                }

                Long assignmentId = tp.getAssignment().getId();
                String courseTitle = tp.getAssignment().getCourse().getTitle();
                LocalDate dueDate = tp.getAssignment().getDueDate();
                String assignmentPriority = tp.getAssignment().getPriority();

                String status = "PENDING";
                String priority = (assignmentPriority != null && !assignmentPriority.isBlank())
                        ? assignmentPriority.toUpperCase()
                        : "NORMAL";

                if (dueDate != null) {
                    if (dueDate.isBefore(today)) {
                        status = "OVERDUE";
                        priority = "HIGH";
                    } else if (!dueDate.isAfter(today.plusDays(7))) {
                        status = "DUE_SOON";
                    }
                }

                actions.add(new EmployeeActionDto(
                        "TRN-" + assignmentId,
                        "TRAINING",
                        "Complete \"" + courseTitle + "\" course",
                        status,
                        priority,
                        dueDate != null ? dueDate.toString() : null,
                        "Self-learning module due by " + (dueDate != null ? dueDate.toString() : "scheduled date"),
                        new EmployeeActionLinkDto("View Task", "/employee/training/" + assignmentId)
                ));
            }

            return actions;
        } catch (Exception ex) {
            log.warn("Error calculating training actions for employee #{}: {}", employee.getId(), ex.getMessage());
            return Collections.emptyList();
        }
    }
}
