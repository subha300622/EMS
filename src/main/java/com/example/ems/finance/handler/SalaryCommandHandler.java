package com.example.ems.finance.handler;

import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import com.example.ems.finance.dto.FinanceCommandEnvelope;
import com.example.ems.finance.service.EmployeeFinanceOnboardingService;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.repository.OnboardingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class SalaryCommandHandler implements FinanceCommandHandler {

    @Autowired
    private EmployeeFinanceOnboardingService service;

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Override
    public EmployeeFinanceOnboarding handle(Long onboardingId, FinanceCommandEnvelope command, String userEmail) {
        Onboarding ob = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + onboardingId));
        
        EmployeeFinanceOnboarding finance = service.getByEmployeeId(ob.getEmployee().getId());
        Map<String, Object> payload = command.getPayload();

        BigDecimal basic;
        BigDecimal hra;
        BigDecimal allowances;

        if (payload.containsKey("salaryStructureId")) {
            basic = BigDecimal.valueOf(50000.00);
            hra = BigDecimal.valueOf(25000.00);
            allowances = BigDecimal.valueOf(10000.00);
        } else {
            basic = new BigDecimal(payload.getOrDefault("basicSalary", "0").toString());
            hra = new BigDecimal(payload.getOrDefault("hra", "0").toString());
            allowances = new BigDecimal(payload.getOrDefault("allowances", "0").toString());
        }

        return service.assignSalaryStructure(finance.getId(), basic, hra, allowances, userEmail);
    }
}
