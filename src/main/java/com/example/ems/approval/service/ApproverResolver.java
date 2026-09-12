package com.example.ems.approval.service;

import com.example.ems.approval.entity.ApprovalWorkflowStep;
import com.example.ems.approval.entity.ApproverType;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

        Employee resolved = switch (type) {
            case TARGET_EMPLOYEE, EMPLOYEE -> {
                if (context != null && context.containsKey("targetEmployeeId")) {
                    Object targetEmpIdObj = context.get("targetEmployeeId");
                    if (targetEmpIdObj instanceof Long) {
                        yield employeeRepository.findById((Long) targetEmpIdObj)
                                .orElse(null);
                    } else if (targetEmpIdObj != null) {
                        String empIdStr = targetEmpIdObj.toString();
                        try {
                            Long numId = Long.parseLong(empIdStr);
                            Optional<Employee> opt = employeeRepository.findById(numId);
                            if (opt.isPresent()) yield opt.get();
                        } catch (NumberFormatException ignored) {}
                        yield employeeRepository.findByEmployeeId(empIdStr).orElse(null);
                    }
                }
                yield requester;
            }

            case DIRECT_MANAGER, REPORTING_MANAGER -> {
                if (requester != null && requester.getManager() != null) {
                    yield requester.getManager();
                }
                if (context != null && context.containsKey("requesterId")) {
                    Object reqIdObj = context.get("requesterId");
                    Long reqNumId = reqIdObj instanceof Long ? (Long) reqIdObj : Long.parseLong(reqIdObj.toString());
                    Employee reqEmp = employeeRepository.findById(reqNumId).orElse(null);
                    if (reqEmp != null && reqEmp.getManager() != null) {
                        yield reqEmp.getManager();
                    }
                }
                yield requester;
            }

            case SPECIFIC_USER -> {
                if (step.getApproverConfig() != null && !step.getApproverConfig().trim().isEmpty()) {
                    String specIdStr = step.getApproverConfig().trim();
                    try {
                        Long numId = Long.parseLong(specIdStr);
                        Optional<Employee> opt = employeeRepository.findById(numId);
                        if (opt.isPresent()) yield opt.get();
                    } catch (NumberFormatException ignored) {}
                    yield employeeRepository.findByEmployeeId(specIdStr).orElse(null);
                }
                yield requester;
            }

            case DEPARTMENT_HEAD, DEPARTMENT, ROLE, CUSTOM_ROLE -> {
                Long targetOrgId = (requester != null && requester.getOrganization() != null)
                        ? requester.getOrganization().getId()
                        : (context != null && context.get("organizationId") instanceof Long ? (Long) context.get("organizationId") : null);

                if (context != null && context.containsKey("financeApproverId")) {
                    Object finIdObj = context.get("financeApproverId");
                    if (finIdObj instanceof Long) {
                        yield employeeRepository.findById((Long) finIdObj).orElse(requester);
                    }
                }
                if (targetOrgId != null) {
                    List<Employee> financeEmps = employeeRepository.findByOrganizationIdAndDepartment(targetOrgId, "Finance");
                    if (financeEmps != null && !financeEmps.isEmpty()) {
                        yield financeEmps.get(0);
                    }
                    List<Employee> acctEmps = employeeRepository.findByOrganizationIdAndDepartment(targetOrgId, "Accounting");
                    if (acctEmps != null && !acctEmps.isEmpty()) {
                        yield acctEmps.get(0);
                    }
                } else {
                    List<Employee> financeEmps = employeeRepository.findByDepartment("Finance");
                    if (financeEmps != null && !financeEmps.isEmpty()) {
                        yield financeEmps.get(0);
                    }
                    List<Employee> acctEmps = employeeRepository.findByDepartment("Accounting");
                    if (acctEmps != null && !acctEmps.isEmpty()) {
                        yield acctEmps.get(0);
                    }
                }
                if (requester != null && requester.getManager() != null) {
                    yield requester.getManager();
                }
                yield requester;
            }

            default -> (requester != null && requester.getManager() != null) ? requester.getManager() : requester;
        };

        if (resolved != null) {
            return resolved;
        }

        // Multi-tenant fallback: Find an active employee within the organization
        Long orgId = null;
        if (context != null && context.containsKey("organizationId")) {
            Object o = context.get("organizationId");
            if (o instanceof Long) orgId = (Long) o;
            else if (o != null) {
                try { orgId = Long.parseLong(o.toString()); } catch (Exception ignored) {}
            }
        }
        if (orgId == null) {
            orgId = com.example.ems.security.context.TenantContext.getOrganizationId();
        }

        if (orgId != null) {
            List<Employee> activeEmps = employeeRepository.findByOrganizationIdAndStatus(orgId, "ACTIVE");
            if (!activeEmps.isEmpty()) {
                return activeEmps.get(0);
            }
            List<Employee> allEmps = employeeRepository.findByOrganizationId(orgId);
            if (!allEmps.isEmpty()) {
                return allEmps.get(0);
            }
        }

        return null;
    }
}
