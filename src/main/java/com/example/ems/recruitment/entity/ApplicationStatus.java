package com.example.ems.recruitment.entity;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum ApplicationStatus {
    APPLIED,
    SCREENING,
    SHORTLISTED,
    INTERVIEW_SCHEDULED,
    INTERVIEW_COMPLETED,
    SELECTED,
    REJECTED,
    OFFER_SENT,
    OFFER_ACCEPTED,
    JOINING_PENDING,
    JOINED,
    WITHDRAWN;

    private static Map<ApplicationStatus, Set<ApplicationStatus>> allowedTransitionsMap;

    private static synchronized Map<ApplicationStatus, Set<ApplicationStatus>> getAllowedTransitions() {
        if (allowedTransitionsMap == null) {
            allowedTransitionsMap = new EnumMap<>(ApplicationStatus.class);
            allowedTransitionsMap.put(APPLIED, EnumSet.of(SCREENING, SHORTLISTED, REJECTED, WITHDRAWN));
            allowedTransitionsMap.put(SCREENING, EnumSet.of(SHORTLISTED, REJECTED, WITHDRAWN));
            allowedTransitionsMap.put(SHORTLISTED, EnumSet.of(INTERVIEW_SCHEDULED, REJECTED, WITHDRAWN));
            allowedTransitionsMap.put(INTERVIEW_SCHEDULED, EnumSet.of(INTERVIEW_COMPLETED, INTERVIEW_SCHEDULED, REJECTED, WITHDRAWN));
            allowedTransitionsMap.put(INTERVIEW_COMPLETED, EnumSet.of(SELECTED, INTERVIEW_SCHEDULED, REJECTED, WITHDRAWN));
            allowedTransitionsMap.put(SELECTED, EnumSet.of(OFFER_SENT, REJECTED, WITHDRAWN));
            allowedTransitionsMap.put(OFFER_SENT, EnumSet.of(OFFER_ACCEPTED, REJECTED, WITHDRAWN));
            allowedTransitionsMap.put(OFFER_ACCEPTED, EnumSet.of(JOINING_PENDING, JOINED, WITHDRAWN));
            allowedTransitionsMap.put(JOINING_PENDING, EnumSet.of(JOINED, WITHDRAWN));
            allowedTransitionsMap.put(JOINED, EnumSet.noneOf(ApplicationStatus.class));
            allowedTransitionsMap.put(REJECTED, EnumSet.noneOf(ApplicationStatus.class));
            allowedTransitionsMap.put(WITHDRAWN, EnumSet.noneOf(ApplicationStatus.class));
        }
        return allowedTransitionsMap;
    }

    public boolean isValidTransition(ApplicationStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        if (this == targetStatus) {
            return true; // Self transition allowed for updates
        }
        Set<ApplicationStatus> allowed = getAllowedTransitions().get(this);
        return allowed != null && allowed.contains(targetStatus);
    }
}
