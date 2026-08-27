package com.example.ems.approval.service;

import com.example.ems.approval.entity.ApprovalWorkflowStep;
import com.example.ems.approval.entity.ApproverType;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class ApproverResolver {

    @Autowired
    private EmployeeRepository employeeRepository;

    public Employee resolveApprover(ApprovalWorkflowStep step, Employee requester, Map<String, Object> context) {
        ApproverType type = step.getApproverType();
        if (type == null) {
            throw new IllegalArgumentException("Approver type is required for step " + step.getStepName());
        }

        switch (type) {
            case TARGET_EMPLOYEE:
                if (context != null && context.containsKey("targetEmployeeId")) {
                    Object targetEmpIdObj = context.get("targetEmployeeId");
                    if (targetEmpIdObj instanceof Long) {
                        return employeeRepository.findById((Long) targetEmpIdObj)
                                .orElseThrow(() -> new IllegalArgumentException("Target employee not found with ID: " + targetEmpIdObj));
                    } else if (targetEmpIdObj != null) {
                        String empIdStr = targetEmpIdObj.toString();
                        try {
                            Long numId = Long.parseLong(empIdStr);
                            Optional<Employee> opt = employeeRepository.findById(numId);
                            if (opt.isPresent()) return opt.get();
                        } catch (NumberFormatException ignored) {}
                        return employeeRepository.findByEmployeeId(empIdStr)
                                .orElseThrow(() -> new IllegalArgumentException("Target employee not found with ID: " + empIdStr));
                    }
                }
                throw new IllegalArgumentException("targetEmployeeId context missing for TARGET_EMPLOYEE step");

            case DIRECT_MANAGER:
                if (requester != null && requester.getManager() != null) {
                    return requester.getManager();
                }
                if (context != null && context.containsKey("requesterId")) {
                    Object reqIdObj = context.get("requesterId");
                    Long reqNumId = reqIdObj instanceof Long ? (Long) reqIdObj : Long.parseLong(reqIdObj.toString());
                    Employee reqEmp = employeeRepository.findById(reqNumId).orElse(null);
                    if (reqEmp != null && reqEmp.getManager() != null) {
                        return reqEmp.getManager();
                    }
                }
                // Fallback to requester if no direct manager is assigned
                return requester;

            case SPECIFIC_USER:
                if (step.getApproverConfig() != null && !step.getApproverConfig().trim().isEmpty()) {
                    String specIdStr = step.getApproverConfig().trim();
                    try {
                        Long numId = Long.parseLong(specIdStr);
                        Optional<Employee> opt = employeeRepository.findById(numId);
                        if (opt.isPresent()) return opt.get();
                    } catch (NumberFormatException ignored) {}
                    return employeeRepository.findByEmployeeId(specIdStr)
                            .orElseThrow(() -> new IllegalArgumentException("Specific approver employee not found: " + specIdStr));
                }
                throw new IllegalArgumentException("Approver config required for SPECIFIC_USER step");

            case DEPARTMENT_HEAD:
            case ROLE:
            default:
                // Fallback: If manager exists use manager, otherwise return requester
                if (requester != null && requester.getManager() != null) {
                    return requester.getManager();
                }
                return requester;
        }
    }
}
