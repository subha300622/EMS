package com.example.ems.recruitment.service;

import com.example.ems.recruitment.entity.ApplicationStatus;
import com.example.ems.recruitment.entity.JobStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RecruitmentWorkflowTest {

    @Test
    @DisplayName("Verify valid ApplicationStatus state machine transitions")
    public void testValidStatusTransitions() {
        // APPLIED -> SCREENING / SHORTLISTED / REJECTED / WITHDRAWN
        assertTrue(ApplicationStatus.APPLIED.isValidTransition(ApplicationStatus.SCREENING));
        assertTrue(ApplicationStatus.APPLIED.isValidTransition(ApplicationStatus.SHORTLISTED));
        assertTrue(ApplicationStatus.APPLIED.isValidTransition(ApplicationStatus.REJECTED));
        assertTrue(ApplicationStatus.APPLIED.isValidTransition(ApplicationStatus.WITHDRAWN));

        // SHORTLISTED -> INTERVIEW_SCHEDULED
        assertTrue(ApplicationStatus.SHORTLISTED.isValidTransition(ApplicationStatus.INTERVIEW_SCHEDULED));

        // INTERVIEW_SCHEDULED -> INTERVIEW_COMPLETED
        assertTrue(ApplicationStatus.INTERVIEW_SCHEDULED.isValidTransition(ApplicationStatus.INTERVIEW_COMPLETED));

        // INTERVIEW_COMPLETED -> SELECTED
        assertTrue(ApplicationStatus.INTERVIEW_COMPLETED.isValidTransition(ApplicationStatus.SELECTED));

        // SELECTED -> OFFER_SENT
        assertTrue(ApplicationStatus.SELECTED.isValidTransition(ApplicationStatus.OFFER_SENT));

        // OFFER_SENT -> OFFER_ACCEPTED
        assertTrue(ApplicationStatus.OFFER_SENT.isValidTransition(ApplicationStatus.OFFER_ACCEPTED));

        // OFFER_ACCEPTED -> JOINED
        assertTrue(ApplicationStatus.OFFER_ACCEPTED.isValidTransition(ApplicationStatus.JOINED));
    }

    @Test
    @DisplayName("Verify invalid ApplicationStatus state machine transitions are blocked")
    public void testInvalidStatusTransitions() {
        // REJECTED -> SHORTLISTED (Blocked)
        assertFalse(ApplicationStatus.REJECTED.isValidTransition(ApplicationStatus.SHORTLISTED));

        // JOINED -> REJECTED (Blocked)
        assertFalse(ApplicationStatus.JOINED.isValidTransition(ApplicationStatus.REJECTED));

        // APPLIED -> JOINED (Blocked without going through offer accept)
        assertFalse(ApplicationStatus.APPLIED.isValidTransition(ApplicationStatus.JOINED));
    }

    @Test
    @DisplayName("Verify JobStatus values")
    public void testJobStatuses() {
        assertEquals(JobStatus.DRAFT, JobStatus.valueOf("DRAFT"));
        assertEquals(JobStatus.PUBLISHED, JobStatus.valueOf("PUBLISHED"));
        assertEquals(JobStatus.CLOSED, JobStatus.valueOf("CLOSED"));
    }
}
