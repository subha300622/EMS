package com.example.ems.employee.service.actioncenter;

import com.example.ems.employee.dto.dashboard.EmployeeActionDto;
import com.example.ems.employee.dto.dashboard.EmployeeActionLinkDto;
import com.example.ems.employee.entity.Employee;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class LeaveActionProvider implements EmployeeActionProvider {

    private static final Logger log = LoggerFactory.getLogger(LeaveActionProvider.class);

    @Autowired
    private LeaveRepository leaveRepository;

    @Override
    public List<EmployeeActionDto> getActions(Employee employee, Long orgId) {
        if (employee == null || employee.getId() == null) {
            return Collections.emptyList();
        }

        try {
            List<Leave> leaves = leaveRepository.findByEmployeeId(employee.getId());
            if (leaves == null || leaves.isEmpty()) {
                return Collections.emptyList();
            }

            List<EmployeeActionDto> actions = new ArrayList<>();
            for (Leave leave : leaves) {
                String status = leave.getStatus() != null ? leave.getStatus().toUpperCase() : "";

                if ("PENDING".equals(status) || "SUBMITTED".equals(status) || "PENDING_APPROVAL".equals(status)) {
                    String dueDate = leave.getStartDate() != null ? leave.getStartDate().toString() : null;
                    actions.add(new EmployeeActionDto(
                            "LEAVE-" + leave.getId(),
                            "LEAVE_APPROVAL",
                            "Leave request pending approval",
                            "PENDING",
                            "NORMAL",
                            dueDate,
                            "Manager review required for your PTO",
                            new EmployeeActionLinkDto("View Task", "/employee/leave/" + leave.getId())
                    ));
                } else if ("SENT_BACK".equals(status) || "NEEDS_REVISION".equals(status)) {
                    String dueDate = leave.getStartDate() != null ? leave.getStartDate().toString() : null;
                    actions.add(new EmployeeActionDto(
                            "LEAVE-" + leave.getId(),
                            "LEAVE_REVISION",
                            "Leave request returned for revision",
                            "PENDING",
                            "HIGH",
                            dueDate,
                            leave.getManagerComment() != null && !leave.getManagerComment().isBlank()
                                    ? leave.getManagerComment()
                                    : "Manager requested revision for your leave request",
                            new EmployeeActionLinkDto("View Task", "/employee/leave/" + leave.getId())
                    ));
                }
            }
            return actions;
        } catch (Exception ex) {
            log.warn("Error calculating leave actions for employee #{}: {}", employee.getId(), ex.getMessage());
            return Collections.emptyList();
        }
    }
}
