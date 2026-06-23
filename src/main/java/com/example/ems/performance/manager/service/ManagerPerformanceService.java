package com.example.ems.performance.manager.service;

import com.example.ems.performance.manager.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("orchestratedManagerPerformanceService")
public class ManagerPerformanceService {

    @Autowired
    private PerformanceQueryService queryService;

    @Autowired
    private PerformanceCommandService commandService;

    public TeamDashboardResponse getTeamDashboard(String managerEmail, String cycle) {
        return queryService.getTeamDashboard(managerEmail, cycle);
    }

    public ReviewDetailResponse getEmployeeReview(String authUserEmail, Long employeeId, String cycle) {
        return queryService.getEmployeeReview(authUserEmail, employeeId, cycle);
    }

    public TeamSummaryResponse getTeamSummary(String managerEmail, String cycle) {
        return queryService.getTeamSummary(managerEmail, cycle);
    }

    public SaveManagerRatingResponse saveManagerRating(String managerEmail, Long reviewId, SaveManagerRatingRequest request) {
        return commandService.saveManagerRating(managerEmail, reviewId, request);
    }

    public SubmitReviewResponse submitFinalReview(String managerEmail, Long reviewId) {
        return commandService.submitFinalReview(managerEmail, reviewId);
    }
}
