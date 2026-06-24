package com.example.ems.onboarding.event;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.event.EmployeeCreatedEvent;
import com.example.ems.onboarding.service.TeamOnboardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OnboardingEventListener {

    @Autowired
    private TeamOnboardingService teamOnboardingService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        Employee employee = event.getEmployee();
        teamOnboardingService.initializeOnboardingForEmployee(employee);
    }
}
