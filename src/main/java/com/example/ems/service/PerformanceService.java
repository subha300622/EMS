package com.example.ems.service;

import com.example.ems.dto.*;
import com.example.ems.entity.*;
import com.example.ems.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerformanceService {

    @Autowired private PerformanceCycleRepository cycleRepository;
    @Autowired private PerformanceGoalRepository goalRepository;
    @Autowired private PerformanceReviewRepository reviewRepository;
    @Autowired private PerformancePipRepository pipRepository;
    @Autowired private EmployeeRepository employeeRepository;

    // ── 1. DASHBOARD ────────────────────────────────────────────────────────
    @Cacheable(value = "performanceDashboard", key = "'stats'")
    public PerformanceDashboardResponse getDashboardStats() {
        PerformanceDashboardResponse stats = new PerformanceDashboardResponse();

        long totalCycles = cycleRepository.count();
        long totalActiveCycles = cycleRepository.findByStatus("ACTIVE").size();
        long totalClosedCycles = cycleRepository.findByStatus("CLOSED").size();

        long totalGoals = goalRepository.count();
        long achievedGoals = goalRepository.findByStatus("ACHIEVED").size();
        long inProgressGoals = goalRepository.findByStatus("IN_PROGRESS").size();
        long missedGoals = goalRepository.findByStatus("MISSED").size();
        double goalRate = totalGoals > 0 ? ((double) achievedGoals / totalGoals) * 100.0 : 0.0;
        double avgProgress = goalRepository.findAll().stream()
                .mapToInt(PerformanceGoal::getProgressPercent)
                .average()
                .orElse(0.0);

        long totalReviews = reviewRepository.count();
        long selfReviews = reviewRepository.findByReviewType("SELF").size();
        long managerReviews = reviewRepository.findByReviewType("MANAGER").size();
        long finalizedReviews = reviewRepository.findByStatus("FINALIZED").size();
        long pendingReviews = totalReviews - finalizedReviews;

        double avgRating = reviewRepository.findAll().stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(PerformanceReview::getRating)
                .average()
                .orElse(0.0);

        long totalPips = pipRepository.count();
        long activePips = pipRepository.findByStatus("ACTIVE").size();
        long completedPips = pipRepository.findByStatus("COMPLETED").size();

        stats.setTotalCycles(totalCycles);
        stats.setTotalActiveCycles(totalActiveCycles);
        stats.setTotalClosedCycles(totalClosedCycles);
        
        stats.setTotalGoals(totalGoals);
        stats.setAchievedGoals(achievedGoals);
        stats.setInProgressGoals(inProgressGoals);
        stats.setMissedGoals(missedGoals);
        stats.setGoalCompletionRate(Math.round(goalRate * 100.0) / 100.0);
        stats.setAverageGoalProgress(Math.round(avgProgress * 100.0) / 100.0);

        stats.setTotalReviews(totalReviews);
        stats.setSelfReviews(selfReviews);
        stats.setManagerReviews(managerReviews);
        stats.setPendingReviews(pendingReviews);
        stats.setFinalizedReviews(finalizedReviews);
        stats.setAverageRating(Math.round(avgRating * 10.0) / 10.0);

        stats.setTotalPips(totalPips);
        stats.setActivePips(activePips);
        stats.setCompletedPips(completedPips);

        return stats;
    }

    // ── 2. CYCLES ────────────────────────────────────────────────────────────
    public List<PerformanceCycleResponse> getCycles() {
        return cycleRepository.findAll().stream().map(cycle -> {
            PerformanceCycleResponse resp = new PerformanceCycleResponse(cycle);
            List<PerformanceGoal> goals = goalRepository.findByCycleId(cycle.getId());
            long totalG = goals.size();
            long achievedG = goals.stream().filter(g -> "ACHIEVED".equalsIgnoreCase(g.getStatus())).count();
            long inProgressG = goals.stream().filter(g -> "IN_PROGRESS".equalsIgnoreCase(g.getStatus())).count();
            long missedG = goals.stream().filter(g -> "MISSED".equalsIgnoreCase(g.getStatus())).count();
            resp.enrichGoalStats(totalG, achievedG, inProgressG, missedG);

            List<PerformanceReview> reviews = reviewRepository.findByCycleId(cycle.getId());
            long totalR = reviews.size();
            long finalizedR = reviews.stream().filter(r -> "FINALIZED".equalsIgnoreCase(r.getStatus())).count();
            resp.enrichReviewStats(totalR, finalizedR);
            return resp;
        }).collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "performanceDashboard", allEntries = true)
    public PerformanceCycleResponse createCycle(PerformanceCycleRequest request) {
        PerformanceCycle cycle = new PerformanceCycle();
        cycle.setName(request.getName());
        cycle.setStartDate(request.getStartDate());
        cycle.setEndDate(request.getEndDate());
        if (request.getStatus() != null) cycle.setStatus(request.getStatus().toUpperCase());
        PerformanceCycle saved = cycleRepository.save(cycle);
        PerformanceCycleResponse resp = new PerformanceCycleResponse(saved);
        resp.enrichGoalStats(0, 0, 0, 0);
        resp.enrichReviewStats(0, 0);
        return resp;
    }

    @Transactional
    @CacheEvict(value = "performanceDashboard", allEntries = true)
    public Optional<PerformanceCycleResponse> updateCycle(Long id, PerformanceCycleRequest request) {
        return cycleRepository.findById(id).map(cycle -> {
            if (request.getName() != null) cycle.setName(request.getName());
            if (request.getStartDate() != null) cycle.setStartDate(request.getStartDate());
            if (request.getEndDate() != null) cycle.setEndDate(request.getEndDate());
            if (request.getStatus() != null) cycle.setStatus(request.getStatus().toUpperCase());
            cycle.setUpdatedAt(LocalDateTime.now());
            PerformanceCycle saved = cycleRepository.save(cycle);
            PerformanceCycleResponse resp = new PerformanceCycleResponse(saved);

            List<PerformanceGoal> goals = goalRepository.findByCycleId(saved.getId());
            long totalG = goals.size();
            long achievedG = goals.stream().filter(g -> "ACHIEVED".equalsIgnoreCase(g.getStatus())).count();
            long inProgressG = goals.stream().filter(g -> "IN_PROGRESS".equalsIgnoreCase(g.getStatus())).count();
            long missedG = goals.stream().filter(g -> "MISSED".equalsIgnoreCase(g.getStatus())).count();
            resp.enrichGoalStats(totalG, achievedG, inProgressG, missedG);

            List<PerformanceReview> reviews = reviewRepository.findByCycleId(saved.getId());
            long totalR = reviews.size();
            long finalizedR = reviews.stream().filter(r -> "FINALIZED".equalsIgnoreCase(r.getStatus())).count();
            resp.enrichReviewStats(totalR, finalizedR);

            return resp;
        });
    }

    // ── 3. GOALS ─────────────────────────────────────────────────────────────
    public List<PerformanceGoalResponse> getGoals(String employeeEmail) {
        // If manager: return all; scoping applied at controller level
        return goalRepository.findAll().stream()
                .map(PerformanceGoalResponse::new)
                .collect(Collectors.toList());
    }

    public List<PerformanceGoalResponse> getGoalsByEmployee(Long employeeId) {
        return goalRepository.findByEmployeeId(employeeId).stream()
                .map(PerformanceGoalResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "performanceDashboard", allEntries = true)
    public PerformanceGoalResponse createGoal(PerformanceGoalRequest request) {
        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

        PerformanceGoal goal = new PerformanceGoal();
        goal.setEmployee(emp);
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setDueDate(request.getDueDate());

        if (request.getCycleId() != null) {
            cycleRepository.findById(request.getCycleId()).ifPresent(goal::setCycle);
        }

        return new PerformanceGoalResponse(goalRepository.save(goal));
    }

    @Transactional
    @CacheEvict(value = "performanceDashboard", allEntries = true)
    public Optional<PerformanceGoalResponse> updateGoalProgress(Long goalId, int progressPercent) {
        return goalRepository.findById(goalId).map(goal -> {
            goal.setProgressPercent(Math.min(100, Math.max(0, progressPercent)));
            if (goal.getProgressPercent() == 100) {
                goal.setStatus("ACHIEVED");
            }
            goal.setUpdatedAt(LocalDateTime.now());
            return new PerformanceGoalResponse(goalRepository.save(goal));
        });
    }

    @Transactional
    @CacheEvict(value = "performanceDashboard", allEntries = true)
    public boolean deleteGoal(Long goalId) {
        if (goalRepository.existsById(goalId)) {
            goalRepository.deleteById(goalId);
            return true;
        }
        return false;
    }

    // ── 4. SELF-REVIEWS ──────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "performanceDashboard", allEntries = true)
    public PerformanceReviewResponse submitSelfReview(SelfReviewRequest request) {
        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

        PerformanceReview review = new PerformanceReview();
        review.setEmployee(emp);
        review.setReviewType("SELF");
        review.setAchievements(request.getAchievements());
        review.setAreasForImprovement(request.getAreasForImprovement());
        review.setComments(request.getComments());
        review.setRating(request.getRating());

        if (request.getCycleId() != null) {
            cycleRepository.findById(request.getCycleId()).ifPresent(review::setCycle);
        }

        return new PerformanceReviewResponse(reviewRepository.save(review));
    }

    // ── 5. MANAGER REVIEWS ───────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "performanceDashboard", allEntries = true)
    public PerformanceReviewResponse submitManagerReview(ManagerReviewRequest request) {
        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

        Employee reviewer = employeeRepository.findById(request.getReviewerId())
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found with ID: " + request.getReviewerId()));

        PerformanceReview review = new PerformanceReview();
        review.setEmployee(emp);
        review.setReviewer(reviewer);
        review.setReviewType("MANAGER");
        review.setAchievements(request.getAchievements());
        review.setAreasForImprovement(request.getAreasForImprovement());
        review.setComments(request.getComments());
        review.setRating(request.getRating());

        if (request.getCycleId() != null) {
            cycleRepository.findById(request.getCycleId()).ifPresent(review::setCycle);
        }

        return new PerformanceReviewResponse(reviewRepository.save(review));
    }

    // ── 6. FEEDBACKS (all reviews visible to the current user) ───────────────
    public List<PerformanceReviewResponse> getFeedbacks() {
        return reviewRepository.findAll().stream()
                .map(PerformanceReviewResponse::new)
                .collect(Collectors.toList());
    }

    public List<PerformanceReviewResponse> getFeedbacksByEmployee(Long employeeId) {
        return reviewRepository.findByEmployeeId(employeeId).stream()
                .map(PerformanceReviewResponse::new)
                .collect(Collectors.toList());
    }

    // ── 7. FINALIZE REVIEW ───────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "performanceDashboard", allEntries = true)
    public Optional<PerformanceReviewResponse> finalizeReview(Long reviewId) {
        return reviewRepository.findById(reviewId).map(review -> {
            review.setStatus("FINALIZED");
            review.setUpdatedAt(LocalDateTime.now());
            return new PerformanceReviewResponse(reviewRepository.save(review));
        });
    }

    // ── 8. PIP ────────────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "performanceDashboard", allEntries = true)
    public PerformancePipResponse createPip(PipRequest request) {
        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

        PerformancePip pip = new PerformancePip();
        pip.setEmployee(emp);
        pip.setImprovementPlan(request.getImprovementPlan());
        pip.setStartDate(request.getStartDate());
        pip.setEndDate(request.getEndDate());

        return new PerformancePipResponse(pipRepository.save(pip));
    }

    // ── 9. REPORTS ───────────────────────────────────────────────────────────
    public Map<String, Object> getReportData(String reportType) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reportType", reportType);
        data.put("generatedAt", LocalDateTime.now());

        long totalGoals = goalRepository.count();
        long achievedGoals = goalRepository.findByStatus("ACHIEVED").size();
        long totalReviews = reviewRepository.count();
        long finalizedReviews = reviewRepository.findByStatus("FINALIZED").size();

        data.put("totalGoals", totalGoals);
        data.put("achievedGoals", achievedGoals);
        data.put("goalAchievementRate",
                totalGoals > 0 ? Math.round(((double) achievedGoals / totalGoals) * 100.0 * 100.0) / 100.0 : 0.0);
        data.put("totalReviews", totalReviews);
        data.put("finalizedReviews", finalizedReviews);
        data.put("activePips", pipRepository.findByStatus("ACTIVE").size());

        return data;
    }
}
