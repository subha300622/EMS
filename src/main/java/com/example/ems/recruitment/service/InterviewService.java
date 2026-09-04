package com.example.ems.recruitment.service;

import com.example.ems.audit.service.AuditLogService;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.recruitment.dto.*;
import com.example.ems.recruitment.entity.*;
import com.example.ems.recruitment.repository.*;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterviewService {

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private AuditLogService auditLogService;

    public InterviewResponse scheduleInterview(Long applicationId, InterviewScheduleRequest request) {
        Long orgId = TenantContext.requireOrganizationId();

        Application app = applicationRepository.findByOrganizationIdAndId(orgId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        if (!app.getStatus().isValidTransition(ApplicationStatus.INTERVIEW_SCHEDULED)) {
            throw new BadRequestException("Cannot schedule interview for application in status: " + app.getStatus());
        }

        if (request.getScheduledDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Interview date cannot be in the past");
        }

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        Employee interviewer = null;
        if (request.getInterviewerId() != null) {
            interviewer = employeeRepository.findById(request.getInterviewerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Interviewer employee not found with ID: " + request.getInterviewerId()));

            if (interviewer.getOrganization() != null && !orgId.equals(interviewer.getOrganization().getId())) {
                throw new BadRequestException("Interviewer does not belong to your organization");
            }

            List<Interview> interviewerConflicts = interviewRepository.findConflictingInterviewerSchedule(
                    orgId, interviewer.getId(), request.getScheduledDate(), request.getStartTime(), request.getEndTime());

            if (!interviewerConflicts.isEmpty()) {
                throw new ConflictException("Interviewer already has another interview scheduled at this time");
            }
        }

        List<Interview> candidateConflicts = interviewRepository.findConflictingCandidateSchedule(
                orgId, app.getId(), request.getScheduledDate(), request.getStartTime(), request.getEndTime());

        if (!candidateConflicts.isEmpty()) {
            throw new ConflictException("Candidate already has another interview scheduled at this time");
        }

        Interview interview = new Interview();
        interview.setOrganizationId(orgId);
        interview.setApplication(app);
        interview.setInterviewer(interviewer);
        if (interviewer != null) {
            interview.setInterviewerName(interviewer.getFirstName() + " " + interviewer.getLastName());
        } else {
            interview.setInterviewerName(request.getInterviewerName());
        }
        interview.setInterviewType(request.getInterviewType());
        interview.setScheduledDate(request.getScheduledDate());
        interview.setStartTime(request.getStartTime());
        interview.setEndTime(request.getEndTime());
        interview.setMeetingLink(request.getMeetingLink());
        interview.setStatus(InterviewStatus.SCHEDULED);

        Interview saved = interviewRepository.save(interview);

        ApplicationStatus oldStatus = app.getStatus();
        app.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(app);

        applicationService.recordStatusHistory(app, oldStatus, ApplicationStatus.INTERVIEW_SCHEDULED, "HR",
                "Scheduled " + request.getInterviewType() + " interview on " + request.getScheduledDate() + " at " + request.getStartTime());

        auditLogService.logAction("HR", "hr@company.com", "SCHEDULE_INTERVIEW", "Interview",
                saved.getId().toString(), getCurrentClientIp(), "Scheduled interview for application " + app.getApplicationNumber());

        return new InterviewResponse(saved);
    }

    public InterviewResponse completeInterview(Long interviewId) {
        Long orgId = TenantContext.requireOrganizationId();
        Interview interview = interviewRepository.findByOrganizationIdAndId(orgId, interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with ID: " + interviewId));

        if (interview.getStatus() != InterviewStatus.SCHEDULED) {
            throw new BadRequestException("Interview status is already " + interview.getStatus());
        }

        interview.setStatus(InterviewStatus.COMPLETED);
        Interview saved = interviewRepository.save(interview);

        Application app = interview.getApplication();
        if (app != null && app.getStatus().isValidTransition(ApplicationStatus.INTERVIEW_COMPLETED)) {
            ApplicationStatus oldStatus = app.getStatus();
            app.setStatus(ApplicationStatus.INTERVIEW_COMPLETED);
            applicationRepository.save(app);
            applicationService.recordStatusHistory(app, oldStatus, ApplicationStatus.INTERVIEW_COMPLETED, "HR", "Interview completed");
        }

        auditLogService.logAction("HR", "hr@company.com", "COMPLETE_INTERVIEW", "Interview",
                saved.getId().toString(), getCurrentClientIp(), "Completed interview ID: " + saved.getId());

        return new InterviewResponse(saved);
    }

    public InterviewResponse submitFeedback(Long interviewId, InterviewFeedbackRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        Interview interview = interviewRepository.findByOrganizationIdAndId(orgId, interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with ID: " + interviewId));

        if (request.getTechnicalRating() != null && (request.getTechnicalRating() < 1 || request.getTechnicalRating() > 5)) {
            throw new BadRequestException("Technical rating must be between 1 and 5");
        }
        if (request.getCommunicationRating() != null && (request.getCommunicationRating() < 1 || request.getCommunicationRating() > 5)) {
            throw new BadRequestException("Communication rating must be between 1 and 5");
        }
        if (request.getOverallRating() != null && (request.getOverallRating() < 1 || request.getOverallRating() > 5)) {
            throw new BadRequestException("Overall rating must be between 1 and 5");
        }

        interview.setTechnicalRating(request.getTechnicalRating());
        interview.setCommunicationRating(request.getCommunicationRating());
        interview.setOverallRating(request.getOverallRating());
        interview.setRecommendation(request.getRecommendation());
        interview.setComments(request.getComments());
        interview.setStatus(InterviewStatus.COMPLETED);

        Interview saved = interviewRepository.save(interview);

        Application app = interview.getApplication();
        if (app != null && app.getStatus().isValidTransition(ApplicationStatus.INTERVIEW_COMPLETED)) {
            ApplicationStatus oldStatus = app.getStatus();
            app.setStatus(ApplicationStatus.INTERVIEW_COMPLETED);
            applicationRepository.save(app);
            applicationService.recordStatusHistory(app, oldStatus, ApplicationStatus.INTERVIEW_COMPLETED, "INTERVIEWER",
                    "Feedback submitted. Recommendation: " + request.getRecommendation());
        }

        auditLogService.logAction("INTERVIEWER", "interviewer@company.com", "SUBMIT_INTERVIEW_FEEDBACK", "Interview",
                saved.getId().toString(), getCurrentClientIp(), "Submitted feedback for interview ID: " + saved.getId());

        return new InterviewResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsForApplication(Long applicationId) {
        Long orgId = TenantContext.requireOrganizationId();
        return interviewRepository.findByOrganizationIdAndApplicationId(orgId, applicationId).stream()
                .map(InterviewResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getAllInterviews() {
        Long orgId = TenantContext.requireOrganizationId();
        return interviewRepository.findByOrganizationId(orgId).stream()
                .map(InterviewResponse::new)
                .collect(Collectors.toList());
    }

    public InterviewResponse cancelInterview(Long interviewId) {
        Long orgId = TenantContext.requireOrganizationId();
        Interview interview = interviewRepository.findByOrganizationIdAndId(orgId, interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with ID: " + interviewId));

        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new BadRequestException("Interview is already CANCELLED");
        }

        interview.setStatus(InterviewStatus.CANCELLED);
        Interview saved = interviewRepository.save(interview);

        auditLogService.logAction("HR", "hr@company.com", "CANCEL_INTERVIEW", "Interview",
                saved.getId().toString(), getCurrentClientIp(), "Cancelled interview ID: " + saved.getId());

        return new InterviewResponse(saved);
    }

    public InterviewResponse rescheduleInterview(Long interviewId, InterviewScheduleRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        Interview interview = interviewRepository.findByOrganizationIdAndId(orgId, interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with ID: " + interviewId));

        if (interview.getStatus() != InterviewStatus.SCHEDULED) {
            throw new BadRequestException("Only SCHEDULED interviews can be rescheduled");
        }

        if (request.getScheduledDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Interview date cannot be in the past");
        }

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        Employee interviewer = null;
        if (request.getInterviewerId() != null) {
            interviewer = employeeRepository.findById(request.getInterviewerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Interviewer employee not found with ID: " + request.getInterviewerId()));

            if (interviewer.getOrganization() != null && !orgId.equals(interviewer.getOrganization().getId())) {
                throw new BadRequestException("Interviewer does not belong to your organization");
            }

            List<Interview> interviewerConflicts = interviewRepository.findConflictingInterviewerScheduleExcludingSelf(
                    orgId, interviewer.getId(), interviewId, request.getScheduledDate(), request.getStartTime(), request.getEndTime());

            if (!interviewerConflicts.isEmpty()) {
                throw new ConflictException("Interviewer already has another interview scheduled at this time");
            }
        }

        List<Interview> candidateConflicts = interviewRepository.findConflictingCandidateScheduleExcludingSelf(
                orgId, interview.getApplication().getId(), interviewId, request.getScheduledDate(), request.getStartTime(), request.getEndTime());

        if (!candidateConflicts.isEmpty()) {
            throw new ConflictException("Candidate already has another interview scheduled at this time");
        }

        interview.setInterviewer(interviewer);
        if (interviewer != null) {
            interview.setInterviewerName(interviewer.getFirstName() + " " + interviewer.getLastName());
        } else if (request.getInterviewerName() != null) {
            interview.setInterviewerName(request.getInterviewerName());
        }
        if (request.getInterviewType() != null) {
            interview.setInterviewType(request.getInterviewType());
        }
        interview.setScheduledDate(request.getScheduledDate());
        interview.setStartTime(request.getStartTime());
        interview.setEndTime(request.getEndTime());
        if (request.getMeetingLink() != null) {
            interview.setMeetingLink(request.getMeetingLink());
        }

        Interview saved = interviewRepository.save(interview);

        auditLogService.logAction("HR", "hr@company.com", "RESCHEDULE_INTERVIEW", "Interview",
                saved.getId().toString(), getCurrentClientIp(), "Rescheduled interview ID: " + saved.getId() + " to " + saved.getScheduledDate());

        return new InterviewResponse(saved);
    }

    private String getCurrentClientIp() {
        try {
            org.springframework.web.context.request.ServletRequestAttributes attrs =
                    (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return com.example.ems.common.util.ClientIpResolver.getClientIp(attrs.getRequest());
            }
        } catch (Exception ignored) {}
        return "0.0.0.0";
    }
}
