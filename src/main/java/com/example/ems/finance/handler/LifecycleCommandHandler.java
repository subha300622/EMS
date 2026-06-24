package com.example.ems.finance.handler;

import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import com.example.ems.finance.dto.FinanceCommandEnvelope;
import com.example.ems.finance.service.EmployeeFinanceOnboardingService;
import com.example.ems.finance.repository.EmployeeFinanceOnboardingRepository;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.repository.OnboardingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LifecycleCommandHandler implements FinanceCommandHandler {

    @Autowired
    private EmployeeFinanceOnboardingService service;

    @Autowired
    private EmployeeFinanceOnboardingRepository repository;

    @Autowired
    private OnboardingRepository onboardingRepository;

    private Long resolveFinanceOnboardingIdFromOnboardingId(Long onboardingId) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + onboardingId));
        com.example.ems.employee.entity.Employee employee = onboarding.getEmployee();

        return repository.findByEmployeeId(employee.getId())
                .orElseGet(() -> {
                    EmployeeFinanceOnboarding ob = new EmployeeFinanceOnboarding();
                    ob.setEmployee(employee);
                    ob.setStatus("DRAFT");
                    return repository.save(ob);
                }).getId();
    }

    @Override
    public EmployeeFinanceOnboarding handle(Long onboardingId, FinanceCommandEnvelope command, String userEmail) {
        Long financeOnboardingId = resolveFinanceOnboardingIdFromOnboardingId(onboardingId);
        String action = command.getAction().toUpperCase();
        Map<String, Object> payload = command.getPayload();

        switch (action) {
            case "SUBMIT":
                return service.submit(financeOnboardingId, userEmail);
            case "APPROVE":
                String notesApprove = payload != null && payload.containsKey("notes") ? (String) payload.get("notes") : "Approved by finance manager";
                return service.approve(financeOnboardingId, userEmail, notesApprove);
            case "REJECT":
                String notesReject = payload != null && payload.containsKey("notes") ? (String) payload.get("notes") : "Rejected by finance manager";
                return service.reject(financeOnboardingId, userEmail, notesReject);
            case "REINITIALIZE":
                return service.reinitialize(financeOnboardingId, userEmail);
            case "ACTIVATE":
                return service.activatePayroll(financeOnboardingId, userEmail);
            default:
                throw new IllegalArgumentException("Unsupported lifecycle action: " + action);
        }
    }
}
