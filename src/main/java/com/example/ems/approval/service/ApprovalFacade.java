package com.example.ems.approval.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.dto.ApprovalTaskDto;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Unified Facade for all EMS Domain Modules into the Central Approval Platform.
 * Enforces the rule: Modules own business resources; Approval Facade delegates policy & workflow execution.
 */
@Component
public class ApprovalFacade {

    @Autowired
    private ApprovalWorkflowEngineService workflowEngineService;

    @Autowired
    private EmployeeRepository employeeRepository;

    public ApprovalWorkflowInstance startApproval(ApprovalContext context) {
        if (context == null) {
            throw new IllegalArgumentException("ApprovalContext is required");
        }

        Employee requester = null;
        if (context.getEmployeeId() != null) {
            requester = employeeRepository.findByEmployeeId(context.getEmployeeId()).orElse(null);
        }

        WorkflowType workflowType = WorkflowType.LEAVE_APPROVAL;
        if (context.getModule() != null) {
            try {
                String typeStr = context.getModule().toUpperCase() + "_APPROVAL";
                workflowType = WorkflowType.valueOf(typeStr);
            } catch (Exception ignored) {
                try {
                    workflowType = WorkflowType.valueOf(context.getModule().toUpperCase());
                } catch (Exception e) {
                    workflowType = WorkflowType.LEAVE_APPROVAL;
                }
            }
        }

        Map<String, Object> map = new HashMap<>();
        if (context.getAmount() != null) map.put("amount", context.getAmount());
        if (context.getDays() != null) map.put("days", context.getDays());
        if (context.getDepartmentId() != null) map.put("departmentId", context.getDepartmentId());
        if (context.getMetadata() != null) map.putAll(context.getMetadata());

        return workflowEngineService.startWorkflow(
                workflowType,
                context.getModule(),
                context.getResourceId(),
                requester,
                map
        );
    }

    public ApprovalTaskDto approve(User currentUser, String taskId, String comments) {
        return workflowEngineService.approveTask(currentUser, taskId, comments);
    }

    public ApprovalTaskDto reject(User currentUser, String taskId, String comments) {
        return workflowEngineService.rejectTask(currentUser, taskId, comments);
    }

    public ApprovalTaskDto sendBack(User currentUser, String taskId, String comments) {
        return workflowEngineService.requestChanges(currentUser, taskId, comments);
    }

    public void cancel(WorkflowType workflowType, String businessReferenceType, String businessReferenceId, String reason) {
        workflowEngineService.cancelWorkflowByBusinessRef(workflowType, businessReferenceType, businessReferenceId, reason);
    }
}
