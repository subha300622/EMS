package com.example.ems.finance.handler;

import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import com.example.ems.finance.dto.FinanceCommandEnvelope;
import com.example.ems.finance.service.EmployeeFinanceOnboardingService;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.repository.OnboardingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BankCommandHandler implements FinanceCommandHandler {

    @Autowired
    private EmployeeFinanceOnboardingService service;

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Override
    public EmployeeFinanceOnboarding handle(Long onboardingId, FinanceCommandEnvelope command, String userEmail) {
        Onboarding ob = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + onboardingId));
        
        EmployeeFinanceOnboarding finance = service.getByEmployeeId(ob.getEmployee().getId());
        command.getPayload().put("status", finance.getStatus());
        
        EmployeeFinanceOnboarding updatedBank = service.update(finance.getId(), command.getPayload(), userEmail);
        updatedBank.setBankVerificationStatus("PENDING");
        service.verifyBank(updatedBank.getId(), "PENDING", "Details updated by user", userEmail);
        return updatedBank;
    }
}
