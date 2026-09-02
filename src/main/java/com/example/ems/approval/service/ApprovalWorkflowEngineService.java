package com.example.ems.approval.service;

import com.example.ems.approval.dto.*;
import com.example.ems.approval.entity.*;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.event.ApprovalChangesRequestedEvent;
import com.example.ems.approval.event.ApprovalWorkflowRejectedEvent;
import com.example.ems.approval.event.ApprovalWorkflowCancelledEvent;
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
        Employee synthetic = new Employee();
        synthetic.setFullName(user.getFullName() != null ? user.getFullName() : user.getWorkEmail());
        synthetic.setEmail(user.getWorkEmail());
        synthetic.setEmployeeId(user.getUserId() != null ? user.getUserId() : "EMP-" + user.getId());
        synthetic.setOrganization(user.getOrganization());
        synthetic.setStatus("ACTIVE");
        return synthetic;
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
        String empIdStr = task.getApprover() != null && task.getApprover().getEmployeeId() != null 
                ? task.getApprover().getEmployeeId() 
                : (task.getApprover() != null ? "EMP-" + task.getApprover().getId() : "UNASSIGNED");

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
                task.getApprover() != null ? task.getApprover().getFullName() : "System",
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
                .orElseGet(() -> {
                    // Default fallback workflow definition if none configured
                    ApprovalWorkflowDefinition defaultDef = new ApprovalWorkflowDefinition();
                    defaultDef.setName("Default " + workflowType + " Workflow");
                    defaultDef.setWorkflowType(workflowType);
                    defaultDef.setOrganization(org);
                    defaultDef.setStatus("ACTIVE");
                    defaultDef = definitionRepository.save(defaultDef);

                    ApprovalWorkflowStep step1 = new ApprovalWorkflowStep();
                    step1.setStepOrder(1);
                    step1.setStepName("Manager Approval");
                    step1.setApproverType(ApproverType.DIRECT_MANAGER);
                    defaultDef.addStep(step1);

                    if (workflowType == WorkflowType.EXPENSE_APPROVAL) {
                        ApprovalWorkflowStep step2 = new ApprovalWorkflowStep();
                        step2.setStepOrder(2);
                        step2.setStepName("Finance Approval");
                        step2.setApproverType(ApproverType.ROLE);
                        step2.setApproverConfig("FINANCE");
                        defaultDef.addStep(step2);
                    }

                    return definitionRepository.save(defaultDef);
                });

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
                    instance.getOrganization().getId(),
                    ApprovalStatus.APPROVED
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

    @Transactional(readOnly = true)
    public ApprovalInboxResponse getInbox(User currentUser, WorkflowType workflowType, ApprovalStatus status, int page, int size) {
        Employee currentEmp = resolveEmployeeForUser(currentUser);
        Long orgId = resolveOrganizationId(currentUser);

        Pageable pageable = PageRequest.of(page, size, Sort.by("assignedAt").descending());
        Page<ApprovalTask> taskPage = taskRepository.findInboxTasks(currentEmp.getId(), orgId, workflowType, status, pageable);

        List<ApprovalTaskDto> dtos = taskPage.getContent().stream().map(this::mapToTaskDto).collect(Collectors.toList());
        return new ApprovalInboxResponse(dtos, taskPage.getTotalElements(), taskPage.getNumber(), taskPage.getSize());
    }

    @Transactional(readOnly = true)
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

        ApprovalTask task = taskRepository.findByApprovalTaskIdWithLock(approvalTaskId)
                .orElseGet(() -> taskRepository.findByApprovalTaskId(approvalTaskId)
                        .orElseThrow(() -> new IllegalArgumentException("Approval task not found with ID: " + approvalTaskId)));

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

        ApprovalTask task = taskRepository.findByApprovalTaskIdWithLock(approvalTaskId)
                .orElseGet(() -> taskRepository.findByApprovalTaskId(approvalTaskId)
                        .orElseThrow(() -> new IllegalArgumentException("Approval task not found with ID: " + approvalTaskId)));

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

        // Publish Workflow Completed Event with REJECTED status and dedicated ApprovalWorkflowRejectedEvent
        eventPublisher.publishEvent(new ApprovalWorkflowCompletedEvent(
                this,
                instance.getWorkflowInstanceId(),
                instance.getWorkflowType(),
                instance.getBusinessReferenceType(),
                instance.getBusinessReferenceId(),
                instance.getOrganization().getId(),
                ApprovalStatus.REJECTED
        ));

        eventPublisher.publishEvent(new ApprovalWorkflowRejectedEvent(
                this,
                instance.getWorkflowInstanceId(),
                instance.getWorkflowType(),
                instance.getBusinessReferenceType(),
                instance.getBusinessReferenceId(),
                instance.getOrganization().getId(),
                comment != null ? comment : "Rejected"
        ));

        return mapToTaskDto(task);
    }

    @Transactional
    public ApprovalTaskDto requestChanges(User currentUser, String approvalTaskId, String comment) {
        Employee actor = resolveEmployeeForUser(currentUser);
        Long orgId = resolveOrganizationId(currentUser);

        ApprovalTask task = taskRepository.findByApprovalTaskIdWithLock(approvalTaskId)
                .orElseGet(() -> taskRepository.findByApprovalTaskId(approvalTaskId)
                        .orElseThrow(() -> new IllegalArgumentException("Approval task not found with ID: " + approvalTaskId)));

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

        eventPublisher.publishEvent(new ApprovalChangesRequestedEvent(
                this,
                instance.getWorkflowInstanceId(),
                instance.getWorkflowType(),
                instance.getBusinessReferenceType(),
                instance.getBusinessReferenceId(),
                instance.getOrganization().getId(),
                comment != null ? comment : "Changes requested"
        ));

        return mapToTaskDto(task);
    }

    @Transactional
    public void cancelWorkflowByBusinessRef(WorkflowType workflowType, String businessReferenceType, String businessReferenceId, String reason) {
        List<ApprovalWorkflowInstance> instances = instanceRepository.findAll().stream()
                .filter(i -> i.getWorkflowType() == workflowType 
                          && businessReferenceType.equalsIgnoreCase(i.getBusinessReferenceType())
                          && businessReferenceId.equalsIgnoreCase(i.getBusinessReferenceId())
                          && (i.getStatus() == ApprovalStatus.IN_PROGRESS || i.getStatus() == ApprovalStatus.PENDING || i.getStatus() == ApprovalStatus.REQUEST_CHANGES))
                .collect(Collectors.toList());

        for (ApprovalWorkflowInstance instance : instances) {
            instance.setStatus(ApprovalStatus.CANCELLED);
            instance.setCompletedAt(Instant.now());
            instanceRepository.save(instance);

            List<ApprovalTask> pendingTasks = taskRepository.findByWorkflowInstanceIdAndStepOrder(instance.getId(), instance.getCurrentStep());
            for (ApprovalTask t : pendingTasks) {
                if (t.getStatus() == ApprovalStatus.PENDING) {
                    t.setStatus(ApprovalStatus.CANCELLED);
                    t.setCompletedAt(Instant.now());
                    taskRepository.save(t);
                }
            }

            eventPublisher.publishEvent(new ApprovalWorkflowCancelledEvent(
                    this,
                    instance.getWorkflowInstanceId(),
                    instance.getWorkflowType(),
                    instance.getBusinessReferenceType(),
                    instance.getBusinessReferenceId(),
                    instance.getOrganization() != null ? instance.getOrganization().getId() : 1L,
                    reason
            ));
        }
    }

    @Transactional
    public void resubmitWorkflowByBusinessRef(WorkflowType workflowType, String businessReferenceType, String businessReferenceId, Employee requester, Map<String, Object> context) {
        List<ApprovalWorkflowInstance> instances = instanceRepository.findAll().stream()
                .filter(i -> i.getWorkflowType() == workflowType 
                          && businessReferenceType.equalsIgnoreCase(i.getBusinessReferenceType())
                          && businessReferenceId.equalsIgnoreCase(i.getBusinessReferenceId())
                          && (i.getStatus() == ApprovalStatus.REQUEST_CHANGES || i.getStatus() == ApprovalStatus.CANCELLED || i.getStatus() == ApprovalStatus.REJECTED))
                .collect(Collectors.toList());

        for (ApprovalWorkflowInstance instance : instances) {
            instance.setStatus(ApprovalStatus.IN_PROGRESS);
            instance.setCurrentStep(1);
            instance.setCompletedAt(null);
            instanceRepository.save(instance);

            createTasksForStep(instance, 1, requester, context);
        }

        if (instances.isEmpty()) {
            startWorkflow(workflowType, businessReferenceType, businessReferenceId, requester, context);
        }
    }

    // ── WORKFLOW CONFIGURATION APIS ──────────────────────────────────────────

    @Transactional
    public ApprovalWorkflowDefinition createWorkflow(User currentUser, CreateApprovalWorkflowRequest request) {
        Long orgId = resolveOrganizationId(currentUser);
        Organization org = organizationRepository.findById(orgId).orElse(null);

        ApprovalWorkflowDefinition def = new ApprovalWorkflowDefinition();
        def.setName(request.getName());
        def.setWorkflowType(request.getWorkflowType());
        def.setOrganization(org);
        def.setStatus(request.isEnabled() ? "ACTIVE" : "INACTIVE");

        if (request.getSteps() != null) {
            int seq = 1;
            for (CreateApprovalWorkflowRequest.StepRequest sReq : request.getSteps()) {
                ApprovalWorkflowStep step = new ApprovalWorkflowStep();
                step.setStepOrder(sReq.getSequence() != null ? sReq.getSequence() : seq);
                step.setStepName("Step " + step.getStepOrder());
                step.setApproverType(sReq.getApproverType() != null ? sReq.getApproverType() : ApproverType.DIRECT_MANAGER);
                step.setApproverConfig(sReq.getApproverConfig());
                step.setSlaHours(sReq.getSlaHours() != null ? sReq.getSlaHours() : 48);
                def.addStep(step);
                seq++;
            }
        }
        return definitionRepository.save(def);
    }

    public List<ApprovalWorkflowDefinition> getWorkflows(User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        return definitionRepository.findAll().stream()
                .filter(d -> d.getOrganization() == null || orgId.equals(d.getOrganization().getId()))
                .collect(Collectors.toList());
    }

    public ApprovalWorkflowDefinition getWorkflow(User currentUser, Long workflowId) {
        return definitionRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow definition not found: " + workflowId));
    }

    @Transactional
    public ApprovalWorkflowDefinition updateWorkflow(User currentUser, Long workflowId, CreateApprovalWorkflowRequest request) {
        ApprovalWorkflowDefinition def = getWorkflow(currentUser, workflowId);
        def.setName(request.getName());
        if (request.getWorkflowType() != null) def.setWorkflowType(request.getWorkflowType());
        def.setStatus(request.isEnabled() ? "ACTIVE" : "INACTIVE");
        return definitionRepository.save(def);
    }

    @Transactional
    public void deleteWorkflow(User currentUser, Long workflowId) {
        ApprovalWorkflowDefinition def = getWorkflow(currentUser, workflowId);
        def.setStatus("INACTIVE");
        definitionRepository.save(def);
    }

    @Transactional
    public ApprovalWorkflowStep addWorkflowStep(User currentUser, Long workflowId, CreateApprovalWorkflowRequest.StepRequest stepRequest) {
        ApprovalWorkflowDefinition def = getWorkflow(currentUser, workflowId);
        ApprovalWorkflowStep step = new ApprovalWorkflowStep();
        step.setStepOrder(stepRequest.getSequence() != null ? stepRequest.getSequence() : def.getSteps().size() + 1);
        step.setStepName("Step " + step.getStepOrder());
        step.setApproverType(stepRequest.getApproverType() != null ? stepRequest.getApproverType() : ApproverType.DIRECT_MANAGER);
        step.setApproverConfig(stepRequest.getApproverConfig());
        step.setSlaHours(stepRequest.getSlaHours() != null ? stepRequest.getSlaHours() : 48);
        def.addStep(step);
        definitionRepository.save(def);
        return step;
    }

    // ── APPROVAL INSTANCE APIS ───────────────────────────────────────────────

    @Transactional
    public ApprovalWorkflowInstance startWorkflowInstance(User currentUser, StartApprovalInstanceRequest request) {
        Employee requester = resolveEmployeeForUser(currentUser);
        WorkflowType wType = WorkflowType.LEAVE_APPROVAL;
        if (request.getEntityType() != null) {
            try {
                wType = WorkflowType.valueOf(request.getEntityType().toUpperCase());
            } catch (Exception ignored) {}
        }
        return startWorkflow(wType, request.getEntityType(), request.getEntityId(), requester, request.getContext());
    }

    public List<ApprovalWorkflowInstance> getInstances(User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        return instanceRepository.findAll().stream()
                .filter(i -> i.getOrganization() != null && orgId.equals(i.getOrganization().getId()))
                .collect(Collectors.toList());
    }

    public ApprovalWorkflowInstance getInstance(User currentUser, String approvalId) {
        Long orgId = resolveOrganizationId(currentUser);
        return instanceRepository.findByWorkflowInstanceIdAndOrganizationId(approvalId, orgId)
                .or(() -> instanceRepository.findByWorkflowInstanceId(approvalId))
                .orElseThrow(() -> new IllegalArgumentException("Approval instance not found with ID: " + approvalId));
    }

    @Transactional
    public ApprovalTaskDto approveInstanceTask(User currentUser, String approvalId, String comment) {
        ApprovalWorkflowInstance instance = getInstance(currentUser, approvalId);

        // Find current pending task for this instance
        List<ApprovalTask> tasks = taskRepository.findByWorkflowInstanceIdAndStepOrder(instance.getId(), instance.getCurrentStep());
        if (tasks.isEmpty()) {
            throw new IllegalStateException("No pending task found for approval instance: " + approvalId);
        }
        ApprovalTask currentTask = tasks.get(0);
        return approveTask(currentUser, currentTask.getApprovalTaskId(), comment);
    }

    @Transactional
    public ApprovalTaskDto rejectInstanceTask(User currentUser, String approvalId, String comment) {
        ApprovalWorkflowInstance instance = getInstance(currentUser, approvalId);

        List<ApprovalTask> tasks = taskRepository.findByWorkflowInstanceIdAndStepOrder(instance.getId(), instance.getCurrentStep());
        if (tasks.isEmpty()) {
            throw new IllegalStateException("No pending task found for approval instance: " + approvalId);
        }
        ApprovalTask currentTask = tasks.get(0);
        return rejectTask(currentUser, currentTask.getApprovalTaskId(), comment);
    }
}

