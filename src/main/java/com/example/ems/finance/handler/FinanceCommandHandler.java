package com.example.ems.finance.handler;

import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import com.example.ems.finance.dto.FinanceCommandEnvelope;

public interface FinanceCommandHandler {
    EmployeeFinanceOnboarding handle(Long onboardingId, FinanceCommandEnvelope command, String userEmail);
}
