package com.example.ems.performance.manager.service;

import com.example.ems.common.exception.AccessDeniedException;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.performance.manager.calculator.PerformanceScoreCalculator;
import com.example.ems.performance.manager.dto.ReviewDetailResponse;
import com.example.ems.performance.manager.dto.TeamDashboardResponse;
import com.example.ems.performance.manager.dto.TeamSummaryResponse;
import com.example.ems.performance.manager.entity.CompetencyRating;
import com.example.ems.performance.manager.entity.PerformanceGoal;
import com.example.ems.performance.manager.entity.PerformanceReview;
import com.example.ems.performance.manager.entity.ReviewStatus;
import com.example.ems.performance.manager.repository.CompetencyRatingRepository;
import com.example.ems.performance.manager.repository.PerformanceGoalRepository;
import com.example.ems.performance.manager.repository.PerformanceReviewRepository;
import com.example.ems.performance.entity.MyGoal;
import com.example.ems.performance.repository.MyGoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PerformanceQueryService {

    @Autowired
    private PerformanceReviewRepository reviewRepository;

    @Autowired
    private CompetencyRatingRepository competencyRepository;

    @Autowired
    private PerformanceGoalRepository goalRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MyGoalRepository myGoalRepository;

    @Autowired
    private com.example.ems.performance.repository.PerformanceGoalRepository oldGoalRepository;

    @Autowired
    private PerformanceScoreCalculator scoreCalculator;

    @Transactional
    public TeamDashboardResponse getTeamDashboard(String managerEmail, String cycle) {
        Employee manager = employeeRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new AccessDeniedException(
                        "Manager employee profile not found for email: " + managerEmail));

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());

        List<TeamDashboardResponse.ReviewSummary> summaries = new ArrayList<>();
        int completed = 0;
        int pending = 0;
        double teamRatingSum = 0.0;
        int teamRatingCount = 0;

        for (Employee emp : directReports) {
            PerformanceReview review = getOrCreateReview(emp, manager, cycle);
            TeamDashboardResponse.ReviewSummary summary = new TeamDashboardResponse.ReviewSummary();
            summary.setEmployeeId(emp.getId());
            summary.setEmployeeName(emp.getFullName());
            summary.setDesignation(emp.getDesignation());
            summary.setSelfRating(review.getSelfRating());
            summary.setManagerRating(review.getManagerRating());
            summary.setGoalsMet(review.getGoalsMet());
            summary.setFinalScore(scoreCalculator.getFinalScoreLabel(review.getFinalScore()));
            summary.setStatus(review.getStatus().name());

            if (review.getStatus() == ReviewStatus.COMPLETED) {
                completed++;
            } else {
                pending++;
            }

            if (review.getManagerRating() != null && review.getManagerRating() > 0.0) {
                teamRatingSum += review.getManagerRating();
                teamRatingCount++;
            }

            summaries.add(summary);
        }

        double avgTeamRating = teamRatingCount > 0 ? (teamRatingSum / teamRatingCount) : 0.0;
        // Keep to 1 decimal place
        avgTeamRating = Math.round(avgTeamRating * 10.0) / 10.0;

        TeamDashboardResponse response = new TeamDashboardResponse();
        response.setTotalReports(directReports.size());
        response.setCompleted(completed);
        response.setPending(pending);
        response.setAvgTeamRating(avgTeamRating);
        response.setTeamBand(getTeamBand(avgTeamRating));
        response.setReviews(summaries);

        return response;
    }

    @Transactional
    public ReviewDetailResponse getEmployeeReview(String authUserEmail, Long employeeId, String cycle) {
        Employee authEmployee = employeeRepository.findByEmail(authUserEmail)
                .orElseThrow(() -> new AccessDeniedException("Authenticated employee profile not found"));

        Employee targetEmployee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BadRequestException("Employee not found with ID: " + employeeId));

        // Security check:
        // Employee can only see own review.
        // Manager can only see direct reports.
        boolean isSelf = targetEmployee.getId().equals(authEmployee.getId());
        boolean isManager = targetEmployee.getManager() != null
                && targetEmployee.getManager().getId().equals(authEmployee.getId());

        if (!isSelf && !isManager) {
            throw new AccessDeniedException("Access Denied: You do not have permission to view this review.");
        }

        Employee manager = targetEmployee.getManager();
        if (manager == null && isSelf) {
            // fallback: find target employee's department manager or a default manager if
            // any, otherwise treat self as manager
            manager = authEmployee;
        }

        PerformanceReview review = getOrCreateReview(targetEmployee, manager, cycle);

        ReviewDetailResponse response = new ReviewDetailResponse();

        ReviewDetailResponse.EmployeeInfo empInfo = new ReviewDetailResponse.EmployeeInfo();
        empInfo.setId(targetEmployee.getId());
        empInfo.setName(targetEmployee.getFullName());
        empInfo.setDepartment(targetEmployee.getDepartment());
        empInfo.setRole(targetEmployee.getDesignation());
        response.setEmployee(empInfo);

        ReviewDetailResponse.ReviewSummary summary = new ReviewDetailResponse.ReviewSummary();
        summary.setFinalScore(scoreCalculator.getFinalScoreLabel(review.getFinalScore()));
        summary.setManagerRating(review.getManagerRating());
        summary.setSelfRating(review.getSelfRating());
        summary.setGoalsMet(review.getGoalsMet());
        response.setSummary(summary);

        List<ReviewDetailResponse.CompetencyInfo> competencies = new ArrayList<>();
        for (CompetencyRating c : review.getCompetencies()) {
            ReviewDetailResponse.CompetencyInfo cInfo = new ReviewDetailResponse.CompetencyInfo();
            cInfo.setName(c.getCompetencyName());
            cInfo.setSelfScore(c.getSelfScore() != null ? c.getSelfScore().doubleValue() : 0.0);
            cInfo.setManagerScore(c.getManagerScore() != null ? c.getManagerScore().doubleValue() : 0.0);
            cInfo.setFeedback(c.getFeedback());
            competencies.add(cInfo);
        }
        response.setCompetencies(competencies);

        List<ReviewDetailResponse.GoalInfo> goals = new ArrayList<>();
        for (PerformanceGoal g : review.getGoals()) {
            ReviewDetailResponse.GoalInfo gInfo = new ReviewDetailResponse.GoalInfo();
            gInfo.setTitle(g.getTitle());
            gInfo.setProgress(g.getProgress());
            gInfo.setStatus(Boolean.TRUE.equals(g.getAchieved()) ? "MET" : "NOT_MET");
            goals.add(gInfo);
        }
        response.setGoals(goals);

        return response;
    }

    @Transactional
    public TeamSummaryResponse getTeamSummary(String managerEmail, String cycle) {
        Employee manager = employeeRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new AccessDeniedException(
                        "Manager employee profile not found for email: " + managerEmail));

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());

        int completed = 0;
        int pending = 0;
        double teamRatingSum = 0.0;
        int teamRatingCount = 0;
        int promotionEligible = 0;

        for (Employee emp : directReports) {
            PerformanceReview review = getOrCreateReview(emp, manager, cycle);
            if (review.getStatus() == ReviewStatus.COMPLETED) {
                completed++;
            } else {
                pending++;
            }

            if (review.getManagerRating() != null && review.getManagerRating() > 0.0) {
                teamRatingSum += review.getManagerRating();
                teamRatingCount++;
            }

            if ("PROMOTION".equalsIgnoreCase(review.getRecommendation())) {
                promotionEligible++;
            }
        }

        double avgTeamRating = teamRatingCount > 0 ? (teamRatingSum / teamRatingCount) : 0.0;
        avgTeamRating = Math.round(avgTeamRating * 10.0) / 10.0;

        TeamSummaryResponse response = new TeamSummaryResponse();
        response.setReviewsCompleted(completed);
        response.setPendingReviews(pending);
        response.setAvgTeamRating(avgTeamRating);
        response.setTeamBand(getTeamBand(avgTeamRating));
        response.setPromotionEligibleCount(promotionEligible);

        return response;
    }

    @Transactional
    public PerformanceReview getOrCreateReview(Employee employee, Employee manager, String cycle) {
        Optional<PerformanceReview> existing = reviewRepository.findByEmployeeIdAndReviewCycle(employee.getId(), cycle);
        if (existing.isPresent()) {
            return existing.get();
        }

        PerformanceReview review = new PerformanceReview();
        review.setEmployee(employee);
        review.setManager(manager);
        review.setReviewCycle(cycle);
        review.setStatus(ReviewStatus.NOT_STARTED);
        review.setSelfRating(4.2); // Seed a default self rating
        review.setGoalsMet(0);
        review = reviewRepository.save(review);

        // Seed default competencies
        String[] competencies = { "Technical Skills", "Communication", "Teamwork", "Problem Solving", "Leadership" };
        double[] defaultSelfScores = { 4.0, 4.5, 4.0, 4.0, 3.5 };
        List<CompetencyRating> list = new ArrayList<>();
        for (int i = 0; i < competencies.length; i++) {
            CompetencyRating c = new CompetencyRating();
            c.setReview(review);
            c.setCompetencyName(competencies[i]);
            c.setSelfScore((int) defaultSelfScores[i]);
            c.setManagerScore(0);
            c.setFeedback("");
            list.add(competencyRepository.save(c));
        }
        review.setCompetencies(list);

        // Seed goals
        List<PerformanceGoal> reviewGoals = new ArrayList<>();
        List<MyGoal> employeeGoals = myGoalRepository.findByEmployeeId(employee.getId());
        if (!employeeGoals.isEmpty()) {
            for (MyGoal g : employeeGoals) {
                PerformanceGoal pg = new PerformanceGoal();
                pg.setReview(review);
                pg.setTitle(g.getTitle());
                pg.setWeight(g.getWeightage() != null ? g.getWeightage() : 10);
                pg.setProgress(g.getProgressPercentage() != null ? g.getProgressPercentage().intValue() : 0);
                pg.setAchieved("COMPLETED".equals(g.getStatus()) || pg.getProgress() >= 100);
                reviewGoals.add(goalRepository.save(pg));
            }
        } else {
            List<com.example.ems.performance.entity.PerformanceGoal> oldGoals = oldGoalRepository
                    .findByEmployeeId(employee.getId());
            if (!oldGoals.isEmpty()) {
                for (com.example.ems.performance.entity.PerformanceGoal g : oldGoals) {
                    PerformanceGoal pg = new PerformanceGoal();
                    pg.setReview(review);
                    pg.setTitle(g.getTitle());
                    pg.setWeight(10);
                    pg.setProgress(g.getProgressPercent());
                    pg.setAchieved("ACHIEVED".equals(g.getStatus()) || pg.getProgress() >= 100);
                    reviewGoals.add(goalRepository.save(pg));
                }
            }
        }

        // Fallback to default mock goal if none found
        if (reviewGoals.isEmpty()) {
            PerformanceGoal pg = new PerformanceGoal();
            pg.setReview(review);
            pg.setTitle("Complete React Migration");
            pg.setWeight(10);
            pg.setProgress(100);
            pg.setAchieved(true);
            reviewGoals.add(goalRepository.save(pg));
        }
        review.setGoals(reviewGoals);

        // Calculate goals met count
        int metCount = 0;
        for (PerformanceGoal g : reviewGoals) {
            if (Boolean.TRUE.equals(g.getAchieved())) {
                metCount++;
            }
        }
        review.setGoalsMet(metCount);

        // Calculate initial scores
        double goalScore = scoreCalculator.calculateGoalScore(reviewGoals);
        double competencyScore = scoreCalculator.calculateCompetencyScore(review.getCompetencies());
        review.setFinalScore(scoreCalculator.calculateFinalScore(goalScore, competencyScore));

        return reviewRepository.save(review);
    }

    private String getTeamBand(double rating) {
        if (rating >= 4.5)
            return "A";
        if (rating >= 4.0)
            return "B";
        if (rating >= 3.0)
            return "C";
        if (rating >= 2.0)
            return "D";
        return "F";
    }
}
