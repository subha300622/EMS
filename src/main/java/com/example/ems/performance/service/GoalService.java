package com.example.ems.performance.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.performance.dto.*;
import com.example.ems.performance.entity.*;
import com.example.ems.performance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private GoalKeyResultRepository keyResultRepository;

    @Autowired
    private GoalCommentRepository commentRepository;

    @Autowired
    private GoalProgressHistoryRepository progressHistoryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public Map<String, Object> createGoal(CreateGoalRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found with ID: " + request.getManagerId()));
        }

        Goal goal = new Goal();
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setGoalType(request.getGoalType());
        goal.setPriority(request.getPriority());
        goal.setWeightage(request.getWeightage());
        goal.setStartDate(request.getStartDate());
        goal.setTargetDate(request.getTargetDate());
        goal.setEmployee(employee);
        goal.setManager(manager);
        goal.setGoalCode("TEMP-" + UUID.randomUUID().toString().substring(0, 8));

        goal = goalRepository.save(goal);

        // Generate final goal code: GOAL-{year}-{id}
        int year = request.getStartDate().getYear();
        goal.setGoalCode("GOAL-" + year + "-" + goal.getId());
        goal = goalRepository.save(goal);

        if (request.getKeyResults() != null) {
            for (CreateGoalRequest.KeyResultItem item : request.getKeyResults()) {
                GoalKeyResult kr = new GoalKeyResult();
                kr.setGoal(goal);
                kr.setTitle(item.getTitle());
                kr.setTargetValue(item.getTargetValue());
                kr.setUnit(item.getUnit());
                kr.setCurrentValue(0);
                keyResultRepository.save(kr);
                goal.getKeyResults().add(kr);
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("goalId", goal.getId());
        res.put("goalCode", goal.getGoalCode());
        res.put("status", goal.getStatus());
        res.put("createdAt", goal.getCreatedAt());
        return res;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMyGoals(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        List<Goal> goals = goalRepository.findByEmployeeId(employee.getId());

        int totalGoals = goals.size();
        int completedGoals = 0;
        int inProgressGoals = 0;
        int overdueGoals = 0;

        List<Map<String, Object>> goalList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Goal g : goals) {
            if ("COMPLETED".equalsIgnoreCase(g.getStatus())) {
                completedGoals++;
            } else if ("IN_PROGRESS".equalsIgnoreCase(g.getStatus())) {
                inProgressGoals++;
            }
            
            if (!"COMPLETED".equalsIgnoreCase(g.getStatus()) && g.getTargetDate() != null && g.getTargetDate().isBefore(today)) {
                overdueGoals++;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("goalId", g.getId());
            item.put("title", g.getTitle());
            item.put("progress", g.getProgress());
            item.put("priority", g.getPriority());
            item.put("status", g.getStatus());
            item.put("targetDate", g.getTargetDate());
            goalList.add(item);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalGoals", totalGoals);
        summary.put("completedGoals", completedGoals);
        summary.put("inProgressGoals", inProgressGoals);
        summary.put("overdueGoals", overdueGoals);

        Map<String, Object> data = new HashMap<>();
        data.put("summary", summary);
        data.put("goals", goalList);

        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getGoalDetails(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));

        Map<String, Object> data = new HashMap<>();
        data.put("goalId", goal.getId());
        data.put("title", goal.getTitle());
        data.put("description", goal.getDescription());
        data.put("status", goal.getStatus());
        data.put("progress", goal.getProgress());
        data.put("priority", goal.getPriority());
        data.put("weightage", goal.getWeightage());
        data.put("startDate", goal.getStartDate());
        data.put("targetDate", goal.getTargetDate());

        Map<String, Object> empMap = new HashMap<>();
        empMap.put("employeeId", goal.getEmployee().getId());
        empMap.put("name", goal.getEmployee().getFullName());
        data.put("employee", empMap);

        List<Map<String, Object>> krs = goal.getKeyResults().stream().map(kr -> {
            Map<String, Object> krMap = new HashMap<>();
            krMap.put("id", kr.getId());
            krMap.put("title", kr.getTitle());
            krMap.put("currentValue", kr.getCurrentValue());
            krMap.put("targetValue", kr.getTargetValue());
            krMap.put("unit", kr.getUnit());
            return krMap;
        }).collect(Collectors.toList());
        data.put("keyResults", krs);

        return data;
    }

    @Transactional
    public void updateGoal(Long goalId, UpdateGoalRequest request) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));

        if (request.getTitle() != null) goal.setTitle(request.getTitle());
        if (request.getDescription() != null) goal.setDescription(request.getDescription());
        if (request.getPriority() != null) goal.setPriority(request.getPriority());
        if (request.getWeightage() != null) goal.setWeightage(request.getWeightage());
        if (request.getTargetDate() != null) goal.setTargetDate(request.getTargetDate());
        if (request.getStartDate() != null) goal.setStartDate(request.getStartDate());
        if (request.getGoalType() != null) goal.setGoalType(request.getGoalType());

        goal.setUpdatedAt(LocalDateTime.now());
        goalRepository.save(goal);
    }

    @Transactional
    public Map<String, Object> updateGoalProgress(Long goalId, UpdateGoalProgressRequest request, String updaterName) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));

        int previousProgress = goal.getProgress();
        int currentProgress = request.getProgress();
        goal.setProgress(currentProgress);
        
        if (currentProgress == 100) {
            goal.setStatus("COMPLETED");
        } else if (currentProgress > 0 && "DRAFT".equalsIgnoreCase(goal.getStatus())) {
            goal.setStatus("IN_PROGRESS");
        }

        // Keep key results in sync proportionally
        for (GoalKeyResult kr : goal.getKeyResults()) {
            int newVal = (currentProgress * kr.getTargetValue()) / 100;
            kr.setCurrentValue(newVal);
            keyResultRepository.save(kr);
        }

        GoalProgressHistory history = new GoalProgressHistory();
        history.setGoal(goal);
        history.setProgress(currentProgress);
        history.setComment(request.getComment());
        history.setUpdatedBy(updaterName);
        history.setUpdatedAt(LocalDateTime.now());
        progressHistoryRepository.save(history);

        goal.setUpdatedAt(LocalDateTime.now());
        goalRepository.save(goal);

        Map<String, Object> data = new HashMap<>();
        data.put("goalId", goal.getId());
        data.put("previousProgress", previousProgress);
        data.put("currentProgress", currentProgress);
        return data;
    }

    @Transactional
    public void submitGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));
        goal.setStatus("SUBMITTED");
        goal.setUpdatedAt(LocalDateTime.now());
        goalRepository.save(goal);
    }

    @Transactional
    public void approveGoal(Long goalId, GoalDecisionRequest decision, Employee manager) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));
        
        goal.setStatus("APPROVED");
        goal.setUpdatedAt(LocalDateTime.now());
        goalRepository.save(goal);

        if (decision != null && decision.getComments() != null && !decision.getComments().isBlank()) {
            GoalComment comment = new GoalComment();
            comment.setGoal(goal);
            comment.setEmployee(manager);
            comment.setComment("Approved: " + decision.getComments());
            comment.setCreatedAt(LocalDateTime.now());
            commentRepository.save(comment);
        }
    }

    @Transactional
    public void rejectGoal(Long goalId, GoalDecisionRequest decision, Employee manager) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));
        
        goal.setStatus("REJECTED");
        goal.setUpdatedAt(LocalDateTime.now());
        goalRepository.save(goal);

        if (decision != null && decision.getReason() != null && !decision.getReason().isBlank()) {
            GoalComment comment = new GoalComment();
            comment.setGoal(goal);
            comment.setEmployee(manager);
            comment.setComment("Rejected: " + decision.getReason());
            comment.setCreatedAt(LocalDateTime.now());
            commentRepository.save(comment);
        }
    }

    @Transactional
    public Map<String, Object> addComment(Long goalId, GoalCommentRequest request, Employee commenter) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));

        GoalComment comment = new GoalComment();
        comment.setGoal(goal);
        comment.setEmployee(commenter);
        comment.setComment(request.getComment());
        comment.setCreatedAt(LocalDateTime.now());
        comment = commentRepository.save(comment);

        Map<String, Object> data = new HashMap<>();
        data.put("commentId", comment.getId());
        data.put("createdAt", comment.getCreatedAt());
        return data;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getHistory(Long goalId) {
        List<GoalProgressHistory> list = progressHistoryRepository.findByGoalIdOrderByUpdatedAtDesc(goalId);
        return list.stream().map(h -> {
            Map<String, Object> m = new HashMap<>();
            m.put("date", h.getUpdatedAt().toLocalDate().toString());
            m.put("progress", h.getProgress());
            m.put("updatedBy", h.getUpdatedBy());
            return m;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardData(String email, boolean hasAllAccess) {
        List<Goal> goals;
        if (hasAllAccess) {
            goals = goalRepository.findAll();
        } else {
            Employee employee = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found for email: " + email));
            goals = goalRepository.findByEmployeeId(employee.getId());
        }

        int totalGoals = goals.size();
        int completedGoals = 0;
        int inProgressGoals = 0;
        int overdueGoals = 0;
        double sumProgress = 0;

        LocalDate today = LocalDate.now();
        for (Goal g : goals) {
            if ("COMPLETED".equalsIgnoreCase(g.getStatus())) {
                completedGoals++;
            } else if ("IN_PROGRESS".equalsIgnoreCase(g.getStatus()) || "APPROVED".equalsIgnoreCase(g.getStatus())) {
                inProgressGoals++;
            }

            if (!"COMPLETED".equalsIgnoreCase(g.getStatus()) && g.getTargetDate() != null && g.getTargetDate().isBefore(today)) {
                overdueGoals++;
            }

            sumProgress += g.getProgress() != null ? g.getProgress() : 0;
        }

        double avgCompletion = totalGoals > 0 ? (sumProgress / totalGoals) : 0.0;
        avgCompletion = Math.round(avgCompletion * 10.0) / 10.0; // Round to 1 decimal place

        Map<String, Object> data = new HashMap<>();
        data.put("totalGoals", totalGoals);
        data.put("completedGoals", completedGoals);
        data.put("inProgressGoals", inProgressGoals);
        data.put("overdueGoals", overdueGoals);
        data.put("averageCompletion", avgCompletion);
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAnalytics() {
        List<Goal> goals = goalRepository.findAll();
        int total = goals.size();
        int completed = (int) goals.stream().filter(g -> "COMPLETED".equalsIgnoreCase(g.getStatus())).count();

        double completionRate = total > 0 ? ((double) completed / total * 100.0) : 0.0;
        completionRate = Math.round(completionRate * 10.0) / 10.0;

        // Group by department
        Map<String, List<Goal>> byDept = goals.stream()
                .collect(Collectors.groupingBy(g -> g.getEmployee().getDepartment() != null ? g.getEmployee().getDepartment() : "General"));

        List<Map<String, Object>> deptPerformance = new ArrayList<>();
        for (Map.Entry<String, List<Goal>> entry : byDept.entrySet()) {
            List<Goal> deptGoals = entry.getValue();
            long deptCompleted = deptGoals.stream().filter(g -> "COMPLETED".equalsIgnoreCase(g.getStatus())).count();
            double deptRate = deptGoals.size() > 0 ? ((double) deptCompleted / deptGoals.size() * 100.0) : 0.0;
            
            Map<String, Object> deptMap = new HashMap<>();
            deptMap.put("department", entry.getKey());
            deptMap.put("completionRate", Math.round(deptRate));
            deptPerformance.add(deptMap);
        }

        // Group by employee for Top Performers
        Map<Employee, List<Goal>> byEmp = goals.stream()
                .collect(Collectors.groupingBy(Goal::getEmployee));

        List<Map<String, Object>> topPerformers = new ArrayList<>();
        for (Map.Entry<Employee, List<Goal>> entry : byEmp.entrySet()) {
            Employee emp = entry.getKey();
            List<Goal> empGoals = entry.getValue();
            long empCompleted = empGoals.stream().filter(g -> "COMPLETED".equalsIgnoreCase(g.getStatus())).count();
            double empRate = empGoals.size() > 0 ? ((double) empCompleted / empGoals.size() * 100.0) : 0.0;

            Map<String, Object> empMap = new HashMap<>();
            empMap.put("employeeId", emp.getId());
            empMap.put("employeeName", emp.getFullName());
            empMap.put("goalCompletion", Math.round(empRate));
            topPerformers.add(empMap);
        }

        // Sort top performers by completion rate descending
        topPerformers.sort((a, b) -> Long.compare(
                ((Number) b.get("goalCompletion")).longValue(),
                ((Number) a.get("goalCompletion")).longValue()
        ));

        // Limit to top 5
        if (topPerformers.size() > 5) {
            topPerformers = topPerformers.subList(0, 5);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("completionRate", completionRate);
        data.put("departmentPerformance", deptPerformance);
        data.put("topPerformers", topPerformers);
        return data;
    }
}
