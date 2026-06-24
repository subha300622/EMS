package com.example.ems.performance.manager.service;

import com.example.ems.common.exception.AccessDeniedException;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.performance.manager.calculator.PerformanceScoreCalculator;
import com.example.ems.performance.manager.dto.SaveManagerRatingRequest;
import com.example.ems.performance.manager.dto.SaveManagerRatingResponse;
import com.example.ems.performance.manager.dto.SubmitReviewResponse;
import com.example.ems.performance.manager.entity.CompetencyRating;
import com.example.ems.performance.manager.entity.PerformanceReview;
import com.example.ems.performance.manager.entity.ReviewStatus;
import com.example.ems.performance.manager.repository.CompetencyRatingRepository;
import com.example.ems.performance.manager.repository.PerformanceReviewRepository;
import com.example.ems.performance.manager.validator.ReviewStateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PerformanceCommandService {

    @Autowired
    private PerformanceReviewRepository reviewRepository;

    @Autowired
    private CompetencyRatingRepository competencyRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PerformanceScoreCalculator scoreCalculator;

    @Autowired
    private ReviewStateValidator stateValidator;

    @Transactional
    public SaveManagerRatingResponse saveManagerRating(String managerEmail, Long reviewId, SaveManagerRatingRequest request) {
        Employee manager = employeeRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new AccessDeniedException("Manager employee profile not found"));

        PerformanceReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BadRequestException("Performance review not found with ID: " + reviewId));

        // Security check: Only manager can submit ratings
        if (review.getManager() == null || !review.getManager().getId().equals(manager.getId())) {
            throw new AccessDeniedException("Access Denied: You are not the manager of this employee.");
        }

        // Validate state transition
        stateValidator.validateTransition(review.getStatus(), ReviewStatus.MANAGER_REVIEW);

        // Update competencies
        if (request.getCompetencyRatings() != null) {
            for (SaveManagerRatingRequest.CompetencyRatingInput input : request.getCompetencyRatings()) {
                Optional<CompetencyRating> existing = review.getCompetencies().stream()
                        .filter(c -> c.getCompetencyName().equalsIgnoreCase(input.getCompetency()))
                        .findFirst();

                if (existing.isPresent()) {
                    CompetencyRating c = existing.get();
                    if (input.getScore() != null) {
                        c.setManagerScore(input.getScore());
                    }
                    if (input.getComment() != null) {
                        c.setFeedback(input.getComment());
                    }
                    competencyRepository.save(c);
                } else {
                    CompetencyRating c = new CompetencyRating();
                    c.setReview(review);
                    c.setCompetencyName(input.getCompetency());
                    c.setManagerScore(input.getScore() != null ? input.getScore() : 0);
                    c.setFeedback(input.getComment());
                    c.setSelfScore(0);
                    competencyRepository.save(c);
                    review.getCompetencies().add(c);
                }
            }
        }

        // Recalculate
        double goalScore = scoreCalculator.calculateGoalScore(review.getGoals());
        double competencyScore = scoreCalculator.calculateCompetencyScore(review.getCompetencies());
        double finalScore = scoreCalculator.calculateFinalScore(goalScore, competencyScore);

        review.setManagerRating(competencyScore);
        review.setFinalScore(finalScore);
        review.setManagerComment(request.getManagerComment());
        review.setRecommendation(request.getRecommendation());
        review.setStatus(ReviewStatus.MANAGER_REVIEW);
        review.setReviewedAt(LocalDateTime.now());

        PerformanceReview saved = reviewRepository.save(review);

        SaveManagerRatingResponse response = new SaveManagerRatingResponse();
        response.setReviewId(saved.getId());
        response.setStatus("IN_PROGRESS"); // Returning IN_PROGRESS matching requirements
        response.setManagerRating(saved.getManagerRating());
        response.setFinalScore(saved.getFinalScore());
        response.setUpdatedAt(saved.getReviewedAt() != null ? saved.getReviewedAt() : LocalDateTime.now());

        return response;
    }

    @Transactional
    public SubmitReviewResponse submitFinalReview(String managerEmail, Long reviewId) {
        Employee manager = employeeRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new AccessDeniedException("Manager employee profile not found"));

        PerformanceReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BadRequestException("Performance review not found with ID: " + reviewId));

        // Security check: Only manager can submit reviews
        if (review.getManager() == null || !review.getManager().getId().equals(manager.getId())) {
            throw new AccessDeniedException("Access Denied: You are not the manager of this employee.");
        }

        // Validate state transition
        stateValidator.validateTransition(review.getStatus(), ReviewStatus.COMPLETED);

        // Update status to COMPLETED
        review.setStatus(ReviewStatus.COMPLETED);
        review.setSubmittedAt(LocalDateTime.now());
        
        PerformanceReview saved = reviewRepository.save(review);

        SubmitReviewResponse response = new SubmitReviewResponse();
        response.setReviewId(saved.getId());
        response.setStatus(saved.getStatus().name());
        response.setSubmittedAt(saved.getSubmittedAt());
        response.setFinalScore(saved.getFinalScore());

        return response;
    }
}
