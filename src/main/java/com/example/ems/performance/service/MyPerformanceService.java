package com.example.ems.performance.service;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalCycle;
import com.example.ems.appraisal.entity.Increment;
import com.example.ems.appraisal.repository.AppraisalCycleRepository;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.appraisal.repository.IncrementRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.performance.dto.*;
import com.example.ems.performance.entity.*;
import com.example.ems.performance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MyPerformanceService {

    @Autowired
    private MyGoalRepository goalRepository;

    @Autowired
    private MyGoalMilestoneRepository milestoneRepository;

    @Autowired
    private MyPerformanceFeedbackRepository feedbackRepository;

    @Autowired
    private MyCompetencyRepository competencyRepository;

    @Autowired
    private MyPerformanceTimelineEventRepository timelineRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private AppraisalCycleRepository cycleRepository;

    @Autowired
    private IncrementRepository incrementRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public void seedPerformanceData(String email) {
        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        if (employee == null) return;

        // 1. Seed Active Cycle 2026
        AppraisalCycle cycle = cycleRepository.findByName("Annual Appraisal 2026")
                .orElseGet(() -> {
                    AppraisalCycle c = new AppraisalCycle();
                    c.setName("Annual Appraisal 2026");
                    c.setStartDate(LocalDate.of(2026, 1, 1));
                    c.setEndDate(LocalDate.of(2026, 12, 31));
                    c.setStatus("ACTIVE");
                    return cycleRepository.save(c);
                });

        // 2. Seed Appraisal Record
        appraisalRepository.findByEmployeeEmailAndCycleId(email, cycle.getId())
                .orElseGet(() -> {
                    Appraisal a = new Appraisal();
                    a.setEmployee(employee);
                    a.setCycle(cycle);
                    a.setStatus("PENDING");
                    return appraisalRepository.save(a);
                });

        // 3. Seed 10 Goals (8 completed, 2 in progress)
        if (goalRepository.findByEmployeeEmailAndCycleId(email, cycle.getId()).isEmpty()) {
            for (int i = 1; i <= 10; i++) {
                MyGoal goal = new MyGoal();
                goal.setGoalCode("GOAL-2026-" + i);
                goal.setTitle("Strategic Goal " + i);
                goal.setDescription("Description for strategic performance goal " + i);
                goal.setCategory(i % 2 == 0 ? "TECHNICAL" : "BEHAVIORAL");
                goal.setWeightage(10);
                goal.setTarget("Target metric for goal " + i);
                goal.setEmployee(employee);
                goal.setCycle(cycle);
                goal.setPriority(i <= 3 ? "HIGH" : (i <= 7 ? "MEDIUM" : "LOW"));
                goal.setStartDate(LocalDate.of(2026, 1, 1));
                goal.setDueDate(LocalDate.of(2026, 12, 31));

                if (i <= 8) {
                    goal.setStatus("COMPLETED");
                    goal.setProgressPercentage(100.0);
                    goal.setAchievement("Fully achieved goal " + i);
                } else {
                    goal.setStatus("IN_PROGRESS");
                    goal.setProgressPercentage(45.0);
                    goal.setAchievement("Working on target for goal " + i);
                }
                goalRepository.save(goal);

                // Add milestones
                MyGoalMilestone m1 = new MyGoalMilestone();
                m1.setGoal(goal);
                m1.setTitle("Milestone 1 for Goal " + i);
                m1.setStatus("COMPLETED");
                milestoneRepository.save(m1);

                MyGoalMilestone m2 = new MyGoalMilestone();
                m2.setGoal(goal);
                m2.setTitle("Milestone 2 for Goal " + i);
                m2.setStatus(i <= 8 ? "COMPLETED" : "PENDING");
                milestoneRepository.save(m2);
            }
        }

        // 4. Seed 5 Feedback Items
        if (feedbackRepository.findByEmployeeEmailAndCycleId(email, cycle.getId()).isEmpty()) {
            List<String> types = Arrays.asList("MANAGER", "PEER", "360", "MANAGER", "PEER");
            for (int i = 0; i < 5; i++) {
                MyPerformanceFeedback feedback = new MyPerformanceFeedback();
                feedback.setEmployee(employee);
                feedback.setCycle(cycle);
                feedback.setFeedbackType(types.get(i));
                feedback.setRating(4 + (i % 2));
                feedback.setComments("Great performance on recent projects. Demonstrates strong " + (i % 2 == 0 ? "technical" : "collaboration") + " skills.");
                feedback.setReceivedDate(LocalDateTime.now().minusDays(10L * i));
                feedbackRepository.save(feedback);
            }
        }

        // 5. Seed Competencies
        if (competencyRepository.findByEmployeeEmail(email).isEmpty()) {
            String[] names = {"Java Development", "System Design", "Communication", "Teamwork"};
            for (int i = 0; i < names.length; i++) {
                MyCompetency comp = new MyCompetency();
                comp.setEmployee(employee);
                comp.setName(names[i]);
                comp.setType(i < 2 ? "TECHNICAL" : "BEHAVIORAL");
                comp.setExpectedLevel(5);
                comp.setCurrentLevel(4);
                comp.setGap(1);
                comp.setImprovementPlan("Participate in advanced " + names[i] + " workshops and take on lead roles in related tasks.");
                competencyRepository.save(comp);
            }
        }

        // 6. Seed Timeline Events
        if (timelineRepository.findByEmployeeEmailOrderByDateDesc(email).isEmpty()) {
            String[] events = {"GOAL_ASSIGNED", "MID_YEAR_REVIEW_COMPLETED", "FEEDBACK_RECEIVED", "GOAL_COMPLETED"};
            for (int i = 0; i < events.length; i++) {
                MyPerformanceTimelineEvent event = new MyPerformanceTimelineEvent();
                event.setEmployee(employee);
                event.setEvent(events[i]);
                event.setPerformedBy("HR Manager");
                event.setDate(LocalDateTime.now().minusMonths(6 - i));
                event.setDescription("Recorded " + events[i].replace("_", " ").toLowerCase() + " event in performance system.");
                timelineRepository.save(event);
            }
        }
    }

    public MyPerformanceDashboardResponse getDashboard(String email) {
        Employee employee = employeeRepository.findByEmail(email).orElseThrow();
        MyPerformanceDashboardResponse res = new MyPerformanceDashboardResponse();
        res.setEmployeeName(employee.getFullName());
        res.setEmployeeId(employee.getEmployeeId());
        res.setDesignation(employee.getDesignation());
        res.setDepartment(employee.getDepartment());

        // Aggregate stats
        AppraisalCycle activeCycle = cycleRepository.findByName("Annual Appraisal 2026").orElse(null);
        if (activeCycle != null) {
            List<MyGoal> goals = goalRepository.findByEmployeeEmailAndCycleId(email, activeCycle.getId());
            long completed = goals.stream().filter(g -> "COMPLETED".equals(g.getStatus())).count();
            res.setActiveGoals(goals.size());
            res.setCompletedGoals((int) completed);
            res.setGoalCompletionPercentage(goals.isEmpty() ? 0.0 : (completed * 100.0 / goals.size()));
            res.setOverallRating(4.5); // Mocked as per requirement
            res.setNextReviewDate(activeCycle.getEndDate().toString());
        }

        res.setRecentActivities(timelineRepository.findByEmployeeEmailOrderByDateDesc(email).stream()
                .limit(5)
                .map(e -> new MyPerformanceDashboardResponse.RecentActivity(e.getEvent(), e.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                .collect(Collectors.toList()));

        return res;
    }

    public MyGoalListResponse getGoals(String email, Long cycleId, String status, String category, Pageable pageable) {
        Page<MyGoal> page = goalRepository.findByFilters(email, cycleId, status, category, pageable);
        List<MyGoalItem> items = page.getContent().stream().map(g -> {
            MyGoalItem item = new MyGoalItem();
            item.setId(g.getId());
            item.setGoalCode(g.getGoalCode());
            item.setTitle(g.getTitle());
            item.setCategory(g.getCategory());
            item.setStatus(g.getStatus());
            item.setPriority(g.getPriority());
            item.setProgressPercentage(g.getProgressPercentage());
            item.setDueDate(g.getDueDate().toString());
            return item;
        }).collect(Collectors.toList());

        return new MyGoalListResponse(items, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    public GoalDetailsResponse getGoalDetails(String email, Long goalId) {
        MyGoal g = goalRepository.findByIdAndEmployeeEmail(goalId, email).orElseThrow();
        GoalDetailsResponse res = new GoalDetailsResponse();
        res.setId(g.getId());
        res.setGoalCode(g.getGoalCode());
        res.setTitle(g.getTitle());
        res.setDescription(g.getDescription());
        res.setCategory(g.getCategory());
        res.setWeightage(g.getWeightage());
        res.setTarget(g.getTarget());
        res.setAchievement(g.getAchievement());
        res.setProgressPercentage(g.getProgressPercentage());
        res.setStatus(g.getStatus());
        res.setPriority(g.getPriority());
        res.setStartDate(g.getStartDate().toString());
        res.setDueDate(g.getDueDate().toString());

        res.setMilestones(g.getMilestones().stream()
                .map(m -> new GoalDetailsResponse.MilestoneDTO(m.getId(), m.getTitle(), m.getStatus()))
                .collect(Collectors.toList()));

        res.setEmployeeReview(new GoalDetailsResponse.ReviewDTO(g.getEmployeeRating(), ""));
        res.setManagerReview(new GoalDetailsResponse.ReviewDTO(g.getManagerRating(), g.getManagerComments()));

        return res;
    }

    @Transactional
    public UpdateGoalProgressResponse updateGoalProgress(String email, Long goalId, UpdateGoalProgressRequest req) {
        MyGoal g = goalRepository.findByIdAndEmployeeEmail(goalId, email).orElseThrow();
        if (req.getAchievement() != null) g.setAchievement(req.getAchievement());
        if (req.getProgressPercentage() != null) g.setProgressPercentage(req.getProgressPercentage());
        if (req.getStatus() != null) g.setStatus(req.getStatus());
        goalRepository.save(g);

        // Log to timeline
        MyPerformanceTimelineEvent event = new MyPerformanceTimelineEvent();
        event.setEmployee(g.getEmployee());
        event.setEvent("GOAL_PROGRESS_UPDATED");
        event.setPerformedBy("Employee");
        event.setDate(LocalDateTime.now());
        event.setDescription("Updated progress for goal: " + g.getGoalCode());
        timelineRepository.save(event);

        return new UpdateGoalProgressResponse("Goal progress updated successfully", g.getProgressPercentage(), g.getStatus());
    }

    public ReviewCyclesResponse getReviewCycles(String email) {
        List<Appraisal> appraisals = appraisalRepository.findByEmployeeEmail(email);
        ReviewCyclesResponse res = new ReviewCyclesResponse();
        res.setCycles(appraisals.stream().map(a -> {
            ReviewCyclesResponse.CycleSummary s = new ReviewCyclesResponse.CycleSummary();
            s.setReviewId(a.getId());
            s.setCycleName(a.getCycle().getName());
            s.setPeriod(a.getCycle().getStartDate().getYear() + "");
            s.setStatus(a.getStatus());
            s.setDueDate(a.getCycle().getEndDate().toString());
            s.setCompletionPercentage("SELF_REVIEWED".equals(a.getStatus()) || "MANAGER_REVIEWED".equals(a.getStatus()) || "FINALIZED".equals(a.getStatus()) ? 100 : 50);
            return s;
        }).collect(Collectors.toList()));
        return res;
    }

    @Transactional
    public SelfAssessmentResponse submitSelfAssessment(String email, Long reviewId, SelfAssessmentRequest req) {
        Appraisal a = appraisalRepository.findById(reviewId).orElseThrow();
        if (!a.getEmployee().getEmail().equals(email)) throw new RuntimeException("Unauthorized");

        a.setSelfRating(req.getSelfRating());
        a.setSelfReview(req.getSelfReview());
        a.setAchievements(req.getAchievements());
        a.setStrengths(req.getStrengths());
        a.setImprovementAreas(req.getImprovementAreas());
        a.setStatus("SELF_REVIEWED");
        a.setSelfReviewSubmittedAt(LocalDateTime.now());
        appraisalRepository.save(a);

        // Log to timeline
        MyPerformanceTimelineEvent event = new MyPerformanceTimelineEvent();
        event.setEmployee(a.getEmployee());
        event.setEvent("SELF_ASSESSMENT_SUBMITTED");
        event.setPerformedBy("Employee");
        event.setDate(LocalDateTime.now());
        event.setDescription("Submitted self-assessment for cycle: " + a.getCycle().getName());
        timelineRepository.save(event);

        List<String> summary = new ArrayList<>();
        summary.add("Rating: " + req.getSelfRating());
        summary.add("Achievements: " + (req.getAchievements() != null ? req.getAchievements().size() : 0));

        return new SelfAssessmentResponse("Self-assessment submitted successfully", "SUBMITTED", LocalDateTime.now().toString(), summary);
    }

    public FeedbackListResponse getFeedback(String email) {
        List<MyPerformanceFeedback> feedbacks = feedbackRepository.findByEmployeeEmail(email);
        FeedbackListResponse res = new FeedbackListResponse();
        res.setFeedback(feedbacks.stream().map(f -> {
            FeedbackListResponse.FeedbackItem item = new FeedbackListResponse.FeedbackItem();
            item.setId(f.getId());
            item.setFeedbackType(f.getFeedbackType());
            item.setRating(f.getRating());
            item.setComments(f.getComments());
            item.setReceivedDate(f.getReceivedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            return item;
        }).collect(Collectors.toList()));
        return res;
    }

    public AppraisalHistoryResponse getHistory(String email) {
        List<Increment> increments = incrementRepository.findByEmployeeEmail(email);
        AppraisalHistoryResponse res = new AppraisalHistoryResponse();
        res.setHistory(increments.stream().map(i -> {
            String year = i.getEffectiveDate().getYear() + "";
            String cycleName = i.getAppraisal() != null ? i.getAppraisal().getCycle().getName() : "Annual Review " + year;
            Integer rating = i.getAppraisal() != null ? i.getAppraisal().getFinalRating() : 4;
            return new AppraisalHistoryResponse.HistoryItem(
                    year,
                    cycleName,
                    rating,
                    i.getIncrementPercentage() + "%",
                    "$" + i.getNewSalary()
            );
        }).collect(Collectors.toList()));
        return res;
    }

    public CompetenciesResponse getCompetencies(String email) {
        List<MyCompetency> comps = competencyRepository.findByEmployeeEmail(email);
        CompetenciesResponse res = new CompetenciesResponse();
        res.setCompetencies(comps.stream().map(c -> {
            CompetenciesResponse.CompetencyItem item = new CompetenciesResponse.CompetencyItem();
            item.setName(c.getName());
            item.setType(c.getType());
            item.setExpectedLevel(c.getExpectedLevel());
            item.setCurrentLevel(c.getCurrentLevel());
            item.setGap(c.getGap());
            item.setImprovementPlan(c.getImprovementPlan());
            return item;
        }).collect(Collectors.toList()));
        return res;
    }

    public PerformanceTimelineResponse getTimeline(String email) {
        List<MyPerformanceTimelineEvent> events = timelineRepository.findByEmployeeEmailOrderByDateDesc(email);
        PerformanceTimelineResponse res = new PerformanceTimelineResponse();
        res.setTimeline(events.stream().map(e -> {
            PerformanceTimelineResponse.TimelineItem item = new PerformanceTimelineResponse.TimelineItem();
            item.setEvent(e.getEvent());
            item.setPerformedBy(e.getPerformedBy());
            item.setDate(e.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            item.setDescription(e.getDescription());
            return item;
        }).collect(Collectors.toList()));
        return res;
    }

    public PerformancePolicyResponse getPolicies() {
        PerformancePolicyResponse res = new PerformancePolicyResponse();
        res.setPolicyTitle("Employee Performance Management Policy");
        res.setLastUpdated("2026-01-15");
        res.setSections(Arrays.asList(
                new PerformancePolicyResponse.PolicySection("Objective", "To provide a transparent and objective framework for evaluating and improving employee performance."),
                new PerformancePolicyResponse.PolicySection("Review Cycle", "The annual review cycle runs from January to December. Mid-year check-ins are mandatory."),
                new PerformancePolicyResponse.PolicySection("Self Assessment", "Employees are required to submit a self-assessment before the manager review phase.")
        ));
        res.setRatingScales(Arrays.asList(
                new PerformancePolicyResponse.RatingScale(5, "Outstanding", "Significantly exceeds all expectations."),
                new PerformancePolicyResponse.RatingScale(4, "Exceeds Expectations", "Consistently exceeds expectations in most areas."),
                new PerformancePolicyResponse.RatingScale(3, "Meets Expectations", "Successfully meets all core requirements and expectations.")
        ));
        return res;
    }
}
