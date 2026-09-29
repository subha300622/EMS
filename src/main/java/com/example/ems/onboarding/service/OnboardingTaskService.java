package com.example.ems.onboarding.service;

import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.onboarding.dto.task.*;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.entity.OnboardingTask;
import com.example.ems.onboarding.repository.OnboardingTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OnboardingTaskService {

    @Autowired
    private OnboardingTaskRepository taskRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OnboardingSecurityValidator securityValidator;

    @Autowired
    private OnboardingAuditLogService auditLogService;

    public OnboardingTaskListResponse getTasks(Long onboardingId) {
        securityValidator.validateAndGetOnboarding(onboardingId);
        List<OnboardingTask> tasks = taskRepository.findByOnboardingId(onboardingId);

        int total = tasks.size();
        int completed = (int) tasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        int pending = total - completed;

        OnboardingTaskListResponse response = new OnboardingTaskListResponse();
        response.setOnboardingId(onboardingId);
        response.setTotalTasks(total);
        response.setCompletedTasks(completed);
        response.setPendingTasks(pending);

        List<OnboardingTaskListResponse.TaskItem> items = tasks.stream().map(t -> {
            OnboardingTaskListResponse.TaskItem item = new OnboardingTaskListResponse.TaskItem();
            item.setTaskId(t.getId());
            item.setTitle(t.getTitle());
            item.setDescription(t.getDescription());
            item.setPhaseId(t.getPhaseId());
            item.setPhaseName(t.getPhase());
            item.setStatus(t.getStatus());
            item.setDueDate(t.getDueDate());
            item.setCompletedAt(t.getCompletedAt());
            item.setRequiresDocument(t.getDocumentId() != null);
            item.setDocumentId(t.getDocumentId());

            if (t.getAssignedTo() != null) {
                item.setAssignedTo(new OnboardingTaskListResponse.AssignedUser(
                        t.getAssignedTo().getEmployeeId(), t.getAssignedTo().getFullName()));
            }
            return item;
        }).collect(Collectors.toList());

        response.setTasks(items);
        return response;
    }

    public OnboardingTaskListResponse.TaskItem getTaskDetails(Long onboardingId, Long taskId) {
        securityValidator.validateAndGetOnboarding(onboardingId);
        OnboardingTask task = validateAndGetTask(onboardingId, taskId);

        OnboardingTaskListResponse.TaskItem item = new OnboardingTaskListResponse.TaskItem();
        item.setTaskId(task.getId());
        item.setTitle(task.getTitle());
        item.setDescription(task.getDescription());
        item.setPhaseId(task.getPhaseId());
        item.setPhaseName(task.getPhase());
        item.setStatus(task.getStatus());
        item.setDueDate(task.getDueDate());
        item.setCompletedAt(task.getCompletedAt());
        item.setRequiresDocument(task.getDocumentId() != null);
        item.setDocumentId(task.getDocumentId());

        if (task.getAssignedTo() != null) {
            item.setAssignedTo(new OnboardingTaskListResponse.AssignedUser(
                    task.getAssignedTo().getEmployeeId(), task.getAssignedTo().getFullName()));
        }
        return item;
    }

    @Transactional
    public OnboardingTaskListResponse.TaskItem updateTask(Long onboardingId, Long taskId, TaskUpdateRequest request) {
        Onboarding onboarding = securityValidator.validateAndGetOnboarding(onboardingId);
        OnboardingTask task = validateAndGetTask(onboardingId, taskId);

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getRemarks() != null) {
            task.setRemarks(request.getRemarks());
        }

        taskRepository.save(task);
        recalculateOnboardingProgress(onboarding);

        auditLogService.logAction(onboarding, "TASK_UPDATED", "ONBOARDING_TASK", taskId, "Updated task details");
        return getTaskDetails(onboardingId, taskId);
    }

    @Transactional
    public OnboardingTaskListResponse.TaskItem completeTask(Long onboardingId, Long taskId, TaskCompleteRequest request) {
        Onboarding onboarding = securityValidator.validateAndGetOnboarding(onboardingId);
        OnboardingTask task = validateAndGetTask(onboardingId, taskId);

        if ("COMPLETED".equalsIgnoreCase(task.getStatus())) {
            throw new IllegalArgumentException("Task is already completed");
        }

        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        if (request != null && request.getRemarks() != null) {
            task.setRemarks(request.getRemarks());
        }

        taskRepository.save(task);
        recalculateOnboardingProgress(onboarding);

        auditLogService.logAction(onboarding, "TASK_COMPLETED", "ONBOARDING_TASK", taskId,
                request != null ? request.getRemarks() : "Task completed");

        return getTaskDetails(onboardingId, taskId);
    }

    @Transactional
    public OnboardingTaskListResponse.TaskItem assignTask(Long onboardingId, Long taskId, TaskAssignRequest request) {
        Onboarding onboarding = securityValidator.validateAndGetOnboarding(onboardingId);
        OnboardingTask task = validateAndGetTask(onboardingId, taskId);

        Employee targetEmployee = employeeRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        // Validate organization tenant match for assignee
        if (targetEmployee.getOrganization() != null && onboarding.getEmployee().getOrganization() != null) {
            if (!targetEmployee.getOrganization().getId().equals(onboarding.getEmployee().getOrganization().getId())) {
                throw new AccessDeniedException("Cannot assign task to employee from a different organization");
            }
        }

        task.setAssignedTo(targetEmployee);
        taskRepository.save(task);

        auditLogService.logAction(onboarding, "TASK_ASSIGNED", "ONBOARDING_TASK", taskId,
                "Assigned task to " + targetEmployee.getFullName() + " (" + targetEmployee.getEmployeeId() + ")");

        return getTaskDetails(onboardingId, taskId);
    }

    private OnboardingTask validateAndGetTask(Long onboardingId, Long taskId) {
        OnboardingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (!task.getOnboarding().getId().equals(onboardingId)) {
            throw new ResourceNotFoundException("Task ID " + taskId + " does not belong to Onboarding ID " + onboardingId);
        }
        return task;
    }

    private void recalculateOnboardingProgress(Onboarding onboarding) {
        List<OnboardingTask> tasks = taskRepository.findByOnboardingId(onboarding.getId());
        if (tasks.isEmpty()) return;

        long completed = tasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        int progress = (int) Math.round(((double) completed / tasks.size()) * 100);
        onboarding.setProgress(progress);
    }
}
