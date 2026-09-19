package com.example.ems.employee.domain;

import com.example.ems.employee.entity.EmployeeStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class EmployeeStatusTransitionValidator {

    private static final Map<EmployeeStatus, Set<EmployeeStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(EmployeeStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(EmployeeStatus.ONBOARDING, EnumSet.of(EmployeeStatus.PROBATION, EmployeeStatus.ACTIVE));
        ALLOWED_TRANSITIONS.put(EmployeeStatus.PROBATION, EnumSet.of(EmployeeStatus.ACTIVE, EmployeeStatus.TERMINATED));
        ALLOWED_TRANSITIONS.put(EmployeeStatus.ACTIVE, EnumSet.of(
                EmployeeStatus.SUSPENDED,
                EmployeeStatus.NOTICE_PERIOD,
                EmployeeStatus.RETIRED,
                EmployeeStatus.TERMINATED
        ));
        ALLOWED_TRANSITIONS.put(EmployeeStatus.SUSPENDED, EnumSet.of(EmployeeStatus.ACTIVE, EmployeeStatus.TERMINATED));
        ALLOWED_TRANSITIONS.put(EmployeeStatus.NOTICE_PERIOD, EnumSet.of(EmployeeStatus.TERMINATED));
        ALLOWED_TRANSITIONS.put(EmployeeStatus.RETIRED, EnumSet.of(EmployeeStatus.TERMINATED));
        ALLOWED_TRANSITIONS.put(EmployeeStatus.TERMINATED, EnumSet.noneOf(EmployeeStatus.class));
    }

    public void validateTransition(EmployeeStatus from, EmployeeStatus to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Current status and target status must not be null");
        }
        if (from == to) {
            throw new IllegalStateException("Employee is already in " + from + " status");
        }
        Set<EmployeeStatus> targets = ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(EmployeeStatus.class));
        if (!targets.contains(to)) {
            throw new IllegalStateException("Invalid status transition from " + from + " to " + to);
        }
    }

    public boolean isTransitionAllowed(EmployeeStatus from, EmployeeStatus to) {
        if (from == null || to == null || from == to) {
            return false;
        }
        Set<EmployeeStatus> targets = ALLOWED_TRANSITIONS.get(from);
        return targets != null && targets.contains(to);
    }
}
