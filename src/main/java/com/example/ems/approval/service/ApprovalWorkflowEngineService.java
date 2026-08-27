package com.example.ems.approval.service;

import com.example.ems.approval.dto.*;
import com.example.ems.approval.entity.*;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.repository.*;
import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApprovalWorkflowEngineService {

    @Autowired
    private ApprovalWorkflowDefinitionRepository definitionRepository;

    @Autowired
    private ApprovalWorkflowInstanceRepository instanceRepository;

    @Autowired
    private ApprovalTaskRepository taskRepository;

    @Autowired
    private ApprovalActionRepository actionRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ApproverResolver approverResolver;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public Employee resolveEmployeeForUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User context is required");
        }
        if (user.getWorkEmail() != null) {
            Optional<Employee> emp = employeeRepository.findByEmail(user.getWorkEmail());
            if (emp.isPresent()) return emp.get();
        }
        if (user.getUserId() != null) {
            Optional<Employee> emp = employeeRepository.findByEmployeeId(user.getUserId());
            if (emp.isPresent()) return emp.get();
        }
        throw new IllegalArgumentException("Employee profile not found for user: " + user.getWorkEmail());
    }

    public Long resolveOrganizationId(User user) {
        if (user == null) throw new IllegalArgumentException("User context is required");
        if (user.getOrganization() != null && user.getOrganization().getId() != null) {
            return user.getOrganization().getId();
        }
        if (user.getWorkEmail() != null) {
            Employee emp = employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
            if (emp != null && emp.getOrganization() != null && emp.getOrganization().getId() != null) {
                return emp.getOrganization().getId();
            }
        }
        return 1L;
    }

    private ApprovalTaskDto mapToTaskDto(ApprovalTask task) {
        String empIdStr = task.getApprover().getEmployeeId() != null 
                ? task.getApprover().getEmployeeId() 
                : "EMP-" + task.getApprover().getId();

        String stepName = task.getStep() != null ? task.getStep().getStepName() : "Step " + task.getStepOrder();

        return new ApprovalTaskDto(
                task.getApprovalTaskId(),
                task.getWorkflowInstance().getWorkflowInstanceId(),
                task.getWorkflowType(),
                task.getBusinessReferenceType(),
                task.getBusinessReferenceId(),
                task.getStepOrder(),
                stepName,
                empIdStr,
                task.getApprover().getFullName(),
                task.getStatus(),
                task.getAssignedAt() != null ? task.getAssignedAt().toString() : null,
                task.getDueAt() != null ? task.getDueAt().toString() : null
        );
    }

    @Transactional
    public ApprovalWorkflowInstance startWorkflow(
            WorkflowType workflowType,
            String businessReferenceType,
            String businessReferenceId,
            Employee requester,
            Map<String, Object> context) {

        Long orgId = requester != null && requester.getOrganization() != null 
                ? requester.getOrganization().getId() 
                : 1L;

        Organization org = organizationRepository.findById(orgId)
                .orElseGet(() -> {
                    Organization o = new Organization();
                    o.setId(orgId);
                    o.setName("Default Organization");
                    return o;
                });

        ApprovalWorkflowDefinition definition = definitionRepository.findActiveByWorkflowTypeAndOrganization(workflowType, orgId)
                .orElseThrow(() -> new IllegalStateException("No active workflow definition found for type: " + workflowType));

        String wfiId = "WFI-" + String.format("%05d", System.currentTimeMillis() % 100000);

        ApprovalWorkflowInstance instance = new ApprovalWorkflowInstance();
        instance.setWorkflowInstanceId(wfiId);
        instance.setWorkflowDefinition(definition);
        instance.setWorkflowType(workflowType);
        instance.setOrganization(org);
        instance.setBusinessReferenceType(businessReferenceType);
        instance.setBusinessReferenceId(businessReferenceId);
        instance.setStatus(ApprovalStatus.IN_PROGRESS);
        instance.setCurrentStep(1);
        instance.setStartedAt(Instant.now());

        ApprovalWorkflowInstance savedInstance = instanceRepository.save(instance);

        // Create tasks for Step 1
        createTasksForStep(savedInstance, 1, requester, context);

        return savedInstance;
    }

    private void createTasksForStep(ApprovalWorkflowInstance instance, Integer stepOrder, Employee requester, Map<String, Object> context) {
        ApprovalWorkflowDefinition def = instance.getWorkflowDefinition();
        if (def == null || def.getSteps() == null) return;

        Optional<ApprovalWorkflowStep> stepOpt = def.getSteps().stream()
                .filter(s -> s.getStepOrder().equals(stepOrder))
                .findFirst();

        if (stepOpt.isEmpty()) {
            // Workflow completed! No more steps remaining.
            instance.setStatus(ApprovalStatus.APPROVED);
            instance.setCompletedAt(Instant.now());
            instanceRepository.save(instance);

            // Publish Workflow Completed Event
            eventPublisher.publishEvent(new ApprovalWorkflowCompletedEvent(
                    this,
                    instance.getWorkflowInstanceId(),
                    instance.getWorkflowType(),
                    instance.getBusinessReferenceType(),
                    instance.getBusinessReferenceId(),
                    instance.getOrganization().getId()
            ));
            return;
        }

        ApprovalWorkflowStep step = stepOpt.get();
        Employee approver = approverResolver.resolveApprover(step, requester, context);

        String taskId = "AT-" + String.format("%05d", System.currentTimeMillis() % 100000);
        Instant now = Instant.now();
        Instant dueAt = step.getSlaHours() != null ? now.plus(step.getSlaHours(), ChronoUnit.HOURS) : now.plus(48, ChronoUnit.HOURS);

        ApprovalTask task = new ApprovalTask();
        task.setApprovalTaskId(taskId);
        task.setWorkflowInstance(instance);
        task.setStep(step);
        task.setStepOrder(stepOrder);
        task.setWorkflowType(instance.getWorkflowType());
        task.setBusinessReferenceType(instance.getBusinessReferenceType());
        task.setBusinessReferenceId(instance.getBusinessReferenceId());
        task.setApprover(approver);
        task.setStatus(ApprovalStatus.PENDING);
        task.setAssignedAt(now);
        task.setDueAt(dueAt);

        taskRepository.save(task);
    }

    public ApprovalInboxResponse getInbox(User currentUser, WorkflowType workflowType, ApprovalStatus status, int page, int size) {
        Employee currentEmp = resolveEmployeeForUser(currentUser);
        Long orgId = resolveOrganizationId(currentUser);

        Pageable pageable = PageRequest.of(page, size, Sort.by("assignedAt").descending());
        Page<ApprovalTask> taskPage = taskRepository.findInboxTasks(currentEmp.getId(), orgId, workflowType, status, pageable);

        List<ApprovalTaskDto> dtos = taskPage.getContent().stream().map(this::mapToTaskDto).collect(Collectors.toList());
        return new ApprovalInboxResponse(dtos, taskPage.getTotalElements(), taskPage.getNumber(), taskPage.getSize());
    }

    public ApprovalTaskDetailDto getTaskDetail(User currentUser, String approvalTaskId) {
        Employee currentEmp = resolveEmployeeForUser(currentUser);
        Long orgId = resolveOrganizationId(currentUser);

        ApprovalTask task = taskRepository.findByApprovalTaskId(approvalTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Approval task not found with ID: " + approvalTaskId));

        if (!orgId.equals(task.getWorkflowInstance().getOrganization().getId())) {
            throw new IllegalArgumentException("Approval task does not belong to user's organization");
        }

        if (!currentEmp.getId().equals(task.getApprover().getId())) {
            throw new IllegalArgumentException("User is not the assigned approver for this task");
        }

        List<String> actions = new ArrayList<>();
        if (task.getStatus() == ApprovalStatus.PENDING) {
            actions.addAll(List.of("APPROVE", "REJECT", "REQUEST_CHANGES"));
        }

        String stepName = task.getStep() != null ? task.getStep().getStepName() : "Step " + task.getStepOrder();

        return new ApprovalTaskDetailDto(
                task.getApprovalTaskId(),
                task.getWorkflowInstance().getWorkflowInstanceId(),
                task.getWorkflowType(),
                task.getBusinessReferenceType(),
                task.getBusinessReferenceId(),
                task.getStepOrder(),
                stepName,
                task.getStatus(),
                actions
        );
    }

    @Transactional
    public ApprovalTaskDto approveTask(User currentUser, String approvalTaskId, String comment) {
        Employee actor = resolveEmployeeForUser(currentUser);
        Long orgId = resolveOrganizationId(currentUser);

        ApprovalTask task = taskRepository.findByApprovalTaskId(approvalTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Approval task not found with ID: " + approvalTaskId));

        if (!orgId.equals(task.getWorkflowInstance().getOrganization().getId())) {
            throw new IllegalArgumentException("Approval task does not belong to user's organization");
        }

        if (!actor.getId().equals(task.getApprover().getId())) {
            throw new IllegalArgumentException("Access Denied: Only assigned approver can approve this task");
        }

        if (task.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Task is not in PENDING status");
        }

        task.setStatus(ApprovalStatus.APPROVED);
        task.setCompletedAt(Instant.now());
        taskRepository.save(task);

        ApprovalAction actionRecord = new ApprovalAction();
        actionRecord.setApprovalTask(task);
        actionRecord.setActor(actor);
        actionRecord.setAction(ApprovalStatus.APPROVED);
        actionRecord.setComment(comment != null ? comment : "Approved");
        actionRepository.save(actionRecord);

        // Advance to next step
        ApprovalWorkflowInstance instance = task.getWorkflowInstance();
        int nextStep = instance.getCurrentStep() + 1;
        instance.setCurrentStep(nextStep);
        instanceRepository.save(instance);

        createTasksForStep(instance, nextStep, actor, null);

        return mapToTaskDto(task);
    }

    @Transactional
    public ApprovalTaskDto rejectTask(User currentUser, String approvalTaskId, String comment) {
        Employee actor = resolveEmployeeForUser(currentUser);
        Long orgId = resolveOrganizationId(currentUser);

        ApprovalTask task = taskRepository.findByApprovalTaskId(approvalTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Approval task not found with ID: " + approvalTaskId));

        if (!orgId.equals(task.getWorkflowInstance().getOrganization().getId())) {
            throw new IllegalArgumentException("Approval task does not belong to user's organization");
        }

        if (!actor.getId().equals(task.getApprover().getId())) {
            throw new IllegalArgumentException("Access Denied: Only assigned approver can reject this task");
        }

        if (task.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Task is not in PENDING status");
        }

        task.setStatus(ApprovalStatus.REJECTED);
        task.setCompletedAt(Instant.now());
        taskRepository.save(task);

        ApprovalAction actionRecord = new ApprovalAction();
        actionRecord.setApprovalTask(task);
        actionRecord.setActor(actor);
        actionRecord.setAction(ApprovalStatus.REJECTED);
        actionRecord.setComment(comment != null ? comment : "Rejected");
        actionRepository.save(actionRecord);

        // Set Workflow Instance to REJECTED
        ApprovalWorkflowInstance instance = task.getWorkflowInstance();
        instance.setStatus(ApprovalStatus.REJECTED);
        instance.setCompletedAt(Instant.now());
        instanceRepository.save(instance);

        return mapToTaskDto(task);
    }

    @Transactional
    public ApprovalTaskDto requestChanges(User currentUser, String approvalTaskId, String comment) {
        Employee actor = resolveEmployeeForUser(currentUser);
        Long orgId = resolveOrganizationId(currentUser);

        ApprovalTask task = taskRepository.findByApprovalTaskId(approvalTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Approval task not found with ID: " + approvalTaskId));

        if (!orgId.equals(task.getWorkflowInstance().getOrganization().getId())) {
            throw new IllegalArgumentException("Approval task does not belong to user's organization");
        }

        if (!actor.getId().equals(task.getApprover().getId())) {
            throw new IllegalArgumentException("Access Denied: Only assigned approver can request changes on this task");
        }

        if (task.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Task is not in PENDING status");
        }

        task.setStatus(ApprovalStatus.REQUEST_CHANGES);
        task.setCompletedAt(Instant.now());
        taskRepository.save(task);

        ApprovalAction actionRecord = new ApprovalAction();
        actionRecord.setApprovalTask(task);
        actionRecord.setActor(actor);
        actionRecord.setAction(ApprovalStatus.REQUEST_CHANGES);
        actionRecord.setComment(comment != null ? comment : "Changes requested");
        actionRepository.save(actionRecord);

        // Set Workflow Instance to REQUEST_CHANGES
        ApprovalWorkflowInstance instance = task.getWorkflowInstance();
        instance.setStatus(ApprovalStatus.REQUEST_CHANGES);
        instanceRepository.save(instance);

        return mapToTaskDto(task);
    }
}
