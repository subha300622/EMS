package com.example.ems.recruitment.service;

import com.example.ems.recruitment.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecruitmentService {

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private OfferService offerService;

    @Autowired
    private EmployeeConversionService employeeConversionService;

    @Autowired
    private TalentPoolService talentPoolService;

    @Autowired
    private RecruitmentDashboardService recruitmentDashboardService;

    public RecruitmentDashboardResponse getDashboardStats() {
        return recruitmentDashboardService.getDashboardStats();
    }

    public JobResponse createJob(JobCreateRequest request) {
        return jobService.createJob(request);
    }

    public List<JobResponse> getJobs() {
        return jobService.getHRJobs();
    }

    public JobResponse getJobById(Long id) {
        return jobService.getHRJobById(id);
    }

    public JobResponse updateJob(Long id, JobUpdateRequest request) {
        return jobService.updateJob(id, request);
    }

    public JobResponse publishJob(Long id) {
        return jobService.publishJob(id);
    }

    public ApplicationResponse applyForJob(Long jobId, JobApplicationRequest request) {
        return applicationService.applyForJob(jobId, request);
    }

    public ApplicationResponse shortlistCandidate(Long applicationId) {
        return applicationService.shortlistCandidate(applicationId);
    }

    public ApplicationResponse rejectCandidate(Long applicationId, String reason) {
        return applicationService.rejectCandidate(applicationId, reason);
    }

    public InterviewResponse scheduleInterview(Long applicationId, InterviewScheduleRequest request) {
        return interviewService.scheduleInterview(applicationId, request);
    }

    public InterviewResponse completeInterview(Long interviewId) {
        return interviewService.completeInterview(interviewId);
    }

    public InterviewResponse submitFeedback(Long interviewId, InterviewFeedbackRequest request) {
        return interviewService.submitFeedback(interviewId, request);
    }

    public OfferResponse generateOffer(Long applicationId, OfferGenerateRequest request) {
        return offerService.generateOffer(applicationId, request);
    }

    public OfferResponse sendOffer(Long offerId) {
        return offerService.sendOffer(offerId);
    }

    public OfferResponse acceptOfferPublic(String token) {
        return offerService.acceptOfferPublic(token);
    }

    public ApplicationResponse convertToEmployee(Long applicationId, CandidateConversionRequest request) {
        return employeeConversionService.convertToEmployee(applicationId, request);
    }

    public Page<TalentPoolCandidateResponse> searchTalentPool(
            String skill, Double experienceMin, Double experienceMax, String location, String search, Pageable pageable) {
        return talentPoolService.searchTalentPool(skill, experienceMin, experienceMax, location, search, pageable);
    }

    public void inviteCandidate(Long candidateId, TalentPoolInviteRequest request) {
        talentPoolService.inviteCandidate(candidateId, request);
    }
}
