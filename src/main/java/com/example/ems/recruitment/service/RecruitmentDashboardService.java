package com.example.ems.recruitment.service;

import com.example.ems.recruitment.dto.RecruitmentDashboardResponse;
import com.example.ems.recruitment.entity.ApplicationStatus;
import com.example.ems.recruitment.entity.JobStatus;
import com.example.ems.recruitment.repository.ApplicationRepository;
import com.example.ems.recruitment.repository.JobRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RecruitmentDashboardService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    public RecruitmentDashboardResponse getDashboardStats() {
        Long orgId = TenantContext.requireOrganizationId();
        RecruitmentDashboardResponse stats = new RecruitmentDashboardResponse();

        long openJobs = jobRepository.countByOrganizationIdAndStatus(orgId, JobStatus.PUBLISHED);
        long totalApps = applicationRepository.countByOrganizationId(orgId);
        long screening = applicationRepository.countByOrganizationIdAndStatus(orgId, ApplicationStatus.SCREENING);
        long shortlisted = applicationRepository.countByOrganizationIdAndStatus(orgId, ApplicationStatus.SHORTLISTED);
        long interviewSched = applicationRepository.countByOrganizationIdAndStatus(orgId, ApplicationStatus.INTERVIEW_SCHEDULED);
        long interviewComp = applicationRepository.countByOrganizationIdAndStatus(orgId, ApplicationStatus.INTERVIEW_COMPLETED);
        long selected = applicationRepository.countByOrganizationIdAndStatus(orgId, ApplicationStatus.SELECTED);
        long offerSent = applicationRepository.countByOrganizationIdAndStatus(orgId, ApplicationStatus.OFFER_SENT);
        long offerAccept = applicationRepository.countByOrganizationIdAndStatus(orgId, ApplicationStatus.OFFER_ACCEPTED);
        long joined = applicationRepository.countByOrganizationIdAndStatus(orgId, ApplicationStatus.JOINED);
        long rejected = applicationRepository.countByOrganizationIdAndStatus(orgId, ApplicationStatus.REJECTED);

        double conversionRate = totalApps > 0 ? ((double) joined / totalApps) * 100.0 : 0.0;

        stats.setOpenJobs(openJobs);
        stats.setApplications(totalApps);
        stats.setScreening(screening);
        stats.setShortlisted(shortlisted);
        stats.setInterviews(interviewSched + interviewComp);
        stats.setSelected(selected);
        stats.setOffers(offerSent + offerAccept);
        stats.setJoined(joined);
        stats.setRejected(rejected);
        stats.setConversionRate(Math.round(conversionRate * 100.0) / 100.0);

        return stats;
    }
}
