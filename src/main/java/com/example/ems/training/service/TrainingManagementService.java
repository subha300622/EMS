package com.example.ems.training.service;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.User;
import com.example.ems.common.event.DomainEventPublisher;
import com.example.ems.common.event.EventEnvelope;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.TeamRepository;
import com.example.ems.training.dto.*;
import com.example.ems.training.entity.*;
import com.example.ems.training.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrainingManagementService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingManagementService.class);

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingApprovalAuditRepository auditRepository;

    @Autowired
    private TrainingRecurrenceConfigRepository recurrenceConfigRepository;

    @Autowired
    private TrainingSessionRepository sessionRepository;

    @Autowired
    private TrainingParticipantRepository participantRepository;

    @Autowired
    private TrainingAttendanceRepository attendanceRepository;

    @Autowired
    private TrainingMaterialRepository materialRepository;

    @Autowired
    private TrainingLibraryResourceRepository libraryResourceRepository;

    @Autowired
    private TrainingFeedbackRepository feedbackRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TrainingAssignmentScopeRepository scopeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired(required = false)
    private ApprovalWorkflowEngineService approvalWorkflowEngineService;

    @Autowired(required = false)
    private DomainEventPublisher domainEventPublisher;

    // ── Helper: Resolve User Organization & Employee ID ───────────────────────
    public Long resolveOrganizationId(User user) {
        if (user == null)
            throw new IllegalArgumentException("User context is required");
        if (user.getOrganization() != null) {
            return user.getOrganization().getId();
        }
        if (user.getEmployeeId() != null) {
            Optional<Employee> emp = employeeRepository.findByEmployeeId(user.getEmployeeId());
            if (emp.isPresent() && emp.get().getOrganization() != null) {
                return emp.get().getOrganization().getId();
            }
        }
        if (user.getWorkEmail() != null) {
            Optional<Employee> emp = employeeRepository.findByEmail(user.getWorkEmail());
            if (emp.isPresent() && emp.get().getOrganization() != null) {
                return emp.get().getOrganization().getId();
            }
        }
        throw new IllegalStateException("Organization context not found for user: " + user.getWorkEmail());
    }

    public Long resolveEmployeeDbId(User user) {
        if (user == null)
            return null;
        if (user.getEmployeeId() != null) {
            Optional<Long> idOpt = employeeRepository.findByEmployeeId(user.getEmployeeId()).map(Employee::getId);
            if (idOpt.isPresent()) return idOpt.get();
        }
        if (user.getWorkEmail() != null) {
            Optional<Long> idOpt = employeeRepository.findByEmail(user.getWorkEmail()).map(Employee::getId);
            if (idOpt.isPresent()) return idOpt.get();
        }
        return null;
    }

    private void publishOutboxEvent(String eventType, String aggregateId, Map<String, Object> payload) {
        if (domainEventPublisher != null) {
            EventEnvelope<Map<String, Object>> envelope = new EventEnvelope<>(
                    eventType, "TRAINING", aggregateId, payload);
            domainEventPublisher.publish(envelope);
        }
    }

    // ── 1. Create Training ───────────────────────────────────────────────────
    @Transactional
    public Training createTraining(TrainingCreateRequest request, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Long actorId = resolveEmployeeDbId(currentUser);
        if (actorId == null)
            actorId = 1L; // Fallback for system admin

        if (DeliveryMethod.ONLINE.equals(request.getDeliveryMethod())
                && (request.getMeetingLink() == null || request.getMeetingLink().isBlank())) {
            throw new IllegalArgumentException("Meeting link is required for ONLINE training");
        }
        if (DeliveryMethod.OFFLINE.equals(request.getDeliveryMethod())
                && (request.getVenue() == null || request.getVenue().isBlank())) {
            throw new IllegalArgumentException("Venue is required for OFFLINE training");
        }
        if (request.getEndDateTime().isBefore(request.getStartDateTime())) {
            throw new IllegalArgumentException("End date time must be after start date time");
        }

        Training training = new Training();
        training.setOrganizationId(orgId);
        training.setTitle(request.getTitle());
        training.setDescription(request.getDescription());
        training.setCategory(request.getCategory());
        training.setTrainingType(request.getTrainingType());
        training.setDepartmentId(request.getDepartmentId());
        training.setTeamId(request.getTeamId());
        training.setTrainerId(request.getTrainerId());
        training.setDeliveryMethod(request.getDeliveryMethod());
        training.setStartDateTime(request.getStartDateTime());
        training.setEndDateTime(request.getEndDateTime());
        training.setIsRecurring(Boolean.TRUE.equals(request.getIsRecurring()));
        training.setMeetingLink(request.getMeetingLink());
        training.setVenue(request.getVenue());
        training.setApprovalRequired(Boolean.TRUE.equals(request.getApprovalRequired()));
        training.setCreatedBy(actorId);

        if (Boolean.TRUE.equals(request.getApprovalRequired())) {
            training.setStatus(TrainingStatus.PENDING_APPROVAL);
        } else {
            training.setStatus(TrainingStatus.APPROVED);
        }

        Training saved = trainingRepository.save(training);

        // Recurrence Configuration & Session generation
        if (Boolean.TRUE.equals(saved.getIsRecurring()) && request.getRecurrenceConfig() != null) {
            RecurrenceConfigRequest rReq = request.getRecurrenceConfig();
            TrainingRecurrenceConfig config = new TrainingRecurrenceConfig();
            config.setTrainingId(saved.getId());
            config.setFrequency(rReq.getFrequency());
            config.setIntervalVal(rReq.getIntervalVal() != null ? rReq.getIntervalVal() : 1);
            config.setDaysOfWeek(rReq.getDaysOfWeek());
            config.setStartDate(rReq.getStartDate());
            config.setEndDate(rReq.getEndDate());
            recurrenceConfigRepository.save(config);

            generateRecurrenceSessions(saved, config);
        } else {
            // Single session
            TrainingSession session = new TrainingSession();
            session.setTrainingId(saved.getId());
            session.setSessionNumber(1);
            session.setTitle(saved.getTitle() + " - Session 1");
            session.setStartDateTime(saved.getStartDateTime());
            session.setEndDateTime(saved.getEndDateTime());
            session.setMeetingLink(saved.getMeetingLink());
            session.setVenue(saved.getVenue());
            session.setStatus("SCHEDULED");
            sessionRepository.save(session);
        }

        // Outbox event
        Map<String, Object> payload = new HashMap<>();
        payload.put("trainingId", saved.getId());
        payload.put("title", saved.getTitle());
        payload.put("status", saved.getStatus().name());
        publishOutboxEvent("TRAINING_CREATED", saved.getId().toString(), payload);

        return saved;
    }

    private void generateRecurrenceSessions(Training training, TrainingRecurrenceConfig config) {
        LocalTime startTime = training.getStartDateTime().toLocalTime();
        LocalTime endTime = training.getEndDateTime().toLocalTime();
        LocalDate current = config.getStartDate();
        int sessionNum = 1;

        while (!current.isAfter(config.getEndDate())) {
            boolean createSession = false;
            if (RecurrenceFrequency.DAILY.equals(config.getFrequency())) {
                createSession = true;
            } else if (RecurrenceFrequency.WEEKLY.equals(config.getFrequency())) {
                if (config.getDaysOfWeek() != null
                        && config.getDaysOfWeek().toUpperCase().contains(current.getDayOfWeek().name())) {
                    createSession = true;
                }
            } else if (RecurrenceFrequency.MONTHLY.equals(config.getFrequency())) {
                if (current.getDayOfMonth() == config.getStartDate().getDayOfMonth()) {
                    createSession = true;
                }
            } else {
                createSession = true;
            }

            if (createSession) {
                TrainingSession session = new TrainingSession();
                session.setTrainingId(training.getId());
                session.setSessionNumber(sessionNum++);
                session.setTitle(training.getTitle() + " - Session " + session.getSessionNumber());
                session.setStartDateTime(LocalDateTime.of(current, startTime));
                session.setEndDateTime(LocalDateTime.of(current, endTime));
                session.setMeetingLink(training.getMeetingLink());
                session.setVenue(training.getVenue());
                session.setStatus("SCHEDULED");
                sessionRepository.save(session);
            }

            current = current.plusDays(config.getIntervalVal() != null ? config.getIntervalVal() : 1);
        }
    }

    // ── 2. Lifecycle State Machine Operations ────────────────────────────────
    @Transactional
    public Training submitForApproval(Long trainingId, User currentUser) {
        Training training = getTrainingById(trainingId, currentUser);
        if (!TrainingStatus.DRAFT.equals(training.getStatus())
                && !TrainingStatus.CHANGES_REQUESTED.equals(training.getStatus())) {
            throw new IllegalStateException("Only DRAFT or CHANGES_REQUESTED trainings can be submitted for approval");
        }
        training.setStatus(TrainingStatus.PENDING_APPROVAL);
        Training saved = trainingRepository.save(training);

        Long actorId = resolveEmployeeDbId(currentUser);
        if (actorId == null)
            actorId = 1L;
        logAudit(saved.getId(), "SUBMIT", actorId, "Submitted for approval");

        if (approvalWorkflowEngineService != null) {
            Employee requester = null;
            if (currentUser != null && currentUser.getWorkEmail() != null) {
                requester = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
            }
            if (requester == null && actorId != null) {
                requester = employeeRepository.findById(actorId).orElse(null);
            }
            try {
                approvalWorkflowEngineService.startWorkflow(
                        WorkflowType.TRAINING_APPROVAL,
                        "TRAINING",
                        saved.getId().toString(),
                        requester,
                        Map.of("trainingTitle", saved.getTitle()));
            } catch (Exception e) {
                log.warn("Approval workflow start failed/skipped: {}", e.getMessage());
            }
        }

        Map<String, Object> payload = Map.of("trainingId", saved.getId(), "status", saved.getStatus().name());
        publishOutboxEvent("TRAINING_SUBMITTED_FOR_APPROVAL", saved.getId().toString(), payload);

        return saved;
    }

    @EventListener
    @Transactional
    public void handleApprovalWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (WorkflowType.TRAINING_APPROVAL.equals(event.getWorkflowType())
                || "TRAINING".equalsIgnoreCase(event.getBusinessReferenceType())) {
            try {
                Long trainingId = Long.parseLong(event.getBusinessReferenceId());
                Training training = trainingRepository.findById(trainingId).orElse(null);
                if (training != null && TrainingStatus.PENDING_APPROVAL.equals(training.getStatus())) {
                    if (ApprovalStatus.APPROVED.equals(event.getStatus())) {
                        training.setStatus(TrainingStatus.APPROVED);
                        trainingRepository.save(training);
                        publishOutboxEvent("TRAINING_APPROVED", trainingId.toString(),
                                Map.of("trainingId", trainingId, "status", "APPROVED"));
                    } else if (ApprovalStatus.REJECTED.equals(event.getStatus())) {
                        training.setStatus(TrainingStatus.REJECTED);
                        trainingRepository.save(training);
                        publishOutboxEvent("TRAINING_REJECTED", trainingId.toString(),
                                Map.of("trainingId", trainingId, "status", "REJECTED"));
                    } else if (ApprovalStatus.REQUEST_CHANGES.equals(event.getStatus())) {
                        training.setStatus(TrainingStatus.CHANGES_REQUESTED);
                        trainingRepository.save(training);
                        publishOutboxEvent("TRAINING_CHANGES_REQUESTED", trainingId.toString(),
                                Map.of("trainingId", trainingId, "status", "CHANGES_REQUESTED"));
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Transactional
    public Training approveTraining(Long trainingId, String comment, User currentUser) {
        Training training = getTrainingById(trainingId, currentUser);
        if (!TrainingStatus.PENDING_APPROVAL.equals(training.getStatus())) {
            throw new IllegalStateException("Only PENDING_APPROVAL trainings can be approved");
        }
        training.setStatus(TrainingStatus.APPROVED);
        Training saved = trainingRepository.save(training);

        Long actorId = resolveEmployeeDbId(currentUser);
        if (actorId == null)
            actorId = 1L;
        logAudit(saved.getId(), "APPROVE", actorId, comment);

        Map<String, Object> payload = Map.of("trainingId", saved.getId(), "status", saved.getStatus().name(), "comment",
                comment != null ? comment : "");
        publishOutboxEvent("TRAINING_APPROVED", saved.getId().toString(), payload);

        return saved;
    }

    @Transactional
    public Training rejectTraining(Long trainingId, String comment, User currentUser) {
        Training training = getTrainingById(trainingId, currentUser);
        if (!TrainingStatus.PENDING_APPROVAL.equals(training.getStatus())) {
            throw new IllegalStateException("Only PENDING_APPROVAL trainings can be rejected");
        }
        training.setStatus(TrainingStatus.REJECTED);
        Training saved = trainingRepository.save(training);

        Long actorId = resolveEmployeeDbId(currentUser);
        if (actorId == null)
            actorId = 1L;
        logAudit(saved.getId(), "REJECT", actorId, comment);

        Map<String, Object> payload = Map.of("trainingId", saved.getId(), "status", saved.getStatus().name(), "comment",
                comment != null ? comment : "");
        publishOutboxEvent("TRAINING_REJECTED", saved.getId().toString(), payload);

        return saved;
    }

    @Transactional
    public Training sendBackTraining(Long trainingId, String comment, User currentUser) {
        Training training = getTrainingById(trainingId, currentUser);
        if (!TrainingStatus.PENDING_APPROVAL.equals(training.getStatus())) {
            throw new IllegalStateException("Only PENDING_APPROVAL trainings can be sent back");
        }
        training.setStatus(TrainingStatus.CHANGES_REQUESTED);
        Training saved = trainingRepository.save(training);

        Long actorId = resolveEmployeeDbId(currentUser);
        if (actorId == null)
            actorId = 1L;
        logAudit(saved.getId(), "REQUEST_CHANGES", actorId, comment);

        Map<String, Object> payload = Map.of("trainingId", saved.getId(), "status", saved.getStatus().name(), "comment",
                comment != null ? comment : "");
        publishOutboxEvent("TRAINING_CHANGES_REQUESTED", saved.getId().toString(), payload);

        return saved;
    }

    @Transactional
    public Training publishTraining(Long trainingId, User currentUser) {
        Training training = getTrainingById(trainingId, currentUser);
        if (!TrainingStatus.APPROVED.equals(training.getStatus())
                && !TrainingStatus.DRAFT.equals(training.getStatus())) {
            throw new IllegalStateException("Only APPROVED or DRAFT trainings can be published");
        }
        training.setStatus(TrainingStatus.PUBLISHED);
        Training saved = trainingRepository.save(training);

        Long actorId = resolveEmployeeDbId(currentUser);
        if (actorId == null)
            actorId = 1L;
        logAudit(saved.getId(), "PUBLISH", actorId, "Training published to participants");

        Map<String, Object> payload = Map.of("trainingId", saved.getId(), "status", saved.getStatus().name());
        publishOutboxEvent("TRAINING_PUBLISHED", saved.getId().toString(), payload);

        return saved;
    }

    @Transactional
    public Training cancelTraining(Long trainingId, String comment, User currentUser) {
        Training training = getTrainingById(trainingId, currentUser);
        training.setStatus(TrainingStatus.CANCELLED);
        Training saved = trainingRepository.save(training);

        Long actorId = resolveEmployeeDbId(currentUser);
        if (actorId == null)
            actorId = 1L;
        logAudit(saved.getId(), "CANCEL", actorId, comment != null ? comment : "Training cancelled");

        Map<String, Object> payload = Map.of("trainingId", saved.getId(), "status", saved.getStatus().name());
        publishOutboxEvent("TRAINING_CANCELLED", saved.getId().toString(), payload);

        return saved;
    }

    private void logAudit(Long trainingId, String action, Long actorId, String comment) {
        TrainingApprovalAudit audit = new TrainingApprovalAudit();
        audit.setTrainingId(trainingId);
        audit.setAction(action);
        audit.setActorId(actorId);
        audit.setComment(comment);
        auditRepository.save(audit);
    }

    // ── 3. Query & Update Training ───────────────────────────────────────────
    public Training getTrainingById(Long trainingId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new IllegalArgumentException("Training not found with ID: " + trainingId));
        if (!training.getOrganizationId().equals(orgId)) {
            throw new SecurityException("Access Denied: Cross-tenant training access is strictly forbidden");
        }
        return training;
    }

    public List<Training> getTrainingsWithFilters(TrainingStatus status, String category, Long trainerId,
            User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        return trainingRepository.findWithFilters(orgId, status, category, trainerId);
    }

    @Transactional
    public Training updateTraining(Long trainingId, TrainingCreateRequest request, User currentUser) {
        Training training = getTrainingById(trainingId, currentUser);
        if (TrainingStatus.COMPLETED.equals(training.getStatus())
                || TrainingStatus.CANCELLED.equals(training.getStatus())) {
            throw new IllegalStateException("Cannot update completed or cancelled training");
        }
        training.setTitle(request.getTitle());
        training.setDescription(request.getDescription());
        training.setCategory(request.getCategory());
        training.setTrainingType(request.getTrainingType());
        training.setDepartmentId(request.getDepartmentId());
        training.setTeamId(request.getTeamId());
        training.setTrainerId(request.getTrainerId());
        training.setDeliveryMethod(request.getDeliveryMethod());
        training.setStartDateTime(request.getStartDateTime());
        training.setEndDateTime(request.getEndDateTime());
        training.setMeetingLink(request.getMeetingLink());
        training.setVenue(request.getVenue());
        return trainingRepository.save(training);
    }

    // ── 4. Participant Assignment & Deduplication ────────────────────────────
    @Transactional
    public List<TrainingParticipant> assignParticipants(Long trainingId, ParticipantAssignRequest request,
            User currentUser) {
        Training training = getTrainingById(trainingId, currentUser);
        Long orgId = resolveOrganizationId(currentUser);

        List<Employee> targetEmployees = new ArrayList<>();

        if (AssignmentTargetType.EMPLOYEE.equals(request.getAssignmentType())) {
            if (request.getEmployeeIds() != null) {
                targetEmployees = employeeRepository.findAllById(request.getEmployeeIds()).stream()
                        .filter(e -> e.getOrganization() != null && e.getOrganization().getId().equals(orgId))
                        .toList();
            }
        } else if (AssignmentTargetType.DEPARTMENT.equals(request.getAssignmentType())) {
            if (request.getDepartmentId() != null) {
                targetEmployees = employeeRepository.findByOrganizationId(orgId).stream()
                        .filter(e -> e.getDepartment() != null)
                        .toList();
            }
        } else if (AssignmentTargetType.TEAM.equals(request.getAssignmentType())) {
            if (request.getTeamId() != null) {
                targetEmployees = employeeRepository.findByOrganizationId(orgId).stream()
                        .filter(e -> e.getTeam() != null && e.getTeam().getId().equals(request.getTeamId()))
                        .toList();
            }
        } else if (AssignmentTargetType.DESIGNATION.equals(request.getAssignmentType())) {
            if (request.getDesignationId() != null) {
                targetEmployees = employeeRepository.findByOrganizationId(orgId);
            }
        }

        List<TrainingParticipant> assignedList = new ArrayList<>();
        for (Employee emp : targetEmployees) {
            // Deduplication check via database unique constraint / repository lookup
            if (!participantRepository.existsByTrainingIdAndEmployeeId(training.getId(), emp.getId())) {
                TrainingParticipant participant = new TrainingParticipant();
                participant.setTrainingId(training.getId());
                participant.setEmployeeId(emp.getId());
                participant.setAssignmentTargetType(request.getAssignmentType());
                participant.setTargetId(
                        request.getDepartmentId() != null ? request.getDepartmentId() : request.getTeamId());
                participant.setParticipationStatus(ParticipationStatus.ASSIGNED);
                assignedList.add(participantRepository.save(participant));
            }
        }

        if (Boolean.TRUE.equals(request.getSendNotification())) {
            Map<String, Object> payload = Map.of(
                    "trainingId", training.getId(),
                    "assignedCount", assignedList.size(),
                    "targetType", request.getAssignmentType().name());
            publishOutboxEvent("TRAINING_ASSIGNED", training.getId().toString(), payload);
        }

        return assignedList;
    }

    public List<TrainingParticipant> getParticipants(Long trainingId, User currentUser) {
        getTrainingById(trainingId, currentUser);
        return participantRepository.findByTrainingId(trainingId);
    }

    // ── Helper: Parse Long Safely ─────────────────────────────────────────────
    private Long tryParseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ── Scope & Execution Unified Assignment ────────────────────────────
    @Transactional
    public List<TrainingParticipant> assignUnified(Long trainingId, TrainingUnifiedAssignmentRequest request, User currentUser) {
        Training training = getTrainingById(trainingId, currentUser);
        Long orgId = resolveOrganizationId(currentUser);
        Long actorId = resolveEmployeeDbId(currentUser);

        if (request.getTargetIds() == null || request.getTargetIds().isEmpty()) {
            throw new IllegalArgumentException("Target IDs list cannot be empty");
        }

        Set<Employee> resolvedEmployeeSet = new LinkedHashSet<>();
        List<TrainingAssignmentScope> scopesToSave = new ArrayList<>();

        for (String rawTargetId : request.getTargetIds()) {
            if (rawTargetId == null || rawTargetId.isBlank()) continue;
            String targetId = rawTargetId.trim();

            Optional<TrainingAssignmentScope> existingScopeOpt = scopeRepository
                    .findByTrainingIdAndAssignmentTypeAndTargetIdAndStatus(trainingId, request.getAssignmentType(), targetId, "ACTIVE");

            if (existingScopeOpt.isEmpty()) {
                TrainingAssignmentScope scope = new TrainingAssignmentScope();
                scope.setOrganizationId(orgId);
                scope.setTrainingId(trainingId);
                scope.setAssignmentType(request.getAssignmentType());
                scope.setTargetId(targetId);
                scope.setMandatory(Boolean.TRUE.equals(request.getMandatory()));
                scope.setDueDate(request.getDueDate());
                scope.setCreatedBy(actorId);
                scope.setStatus("ACTIVE");
                scopesToSave.add(scope);
            }

            resolvedEmployeeSet.addAll(resolveEmployeesForScope(orgId, request.getAssignmentType(), targetId));
        }

        if (!scopesToSave.isEmpty()) {
            scopeRepository.saveAll(scopesToSave);
        }

        List<TrainingParticipant> newlyAssignedList = new ArrayList<>();
        List<Long> newlyAssignedEmployeeIds = new ArrayList<>();

        for (Employee emp : resolvedEmployeeSet) {
            boolean alreadyAssigned = participantRepository.existsByTrainingIdAndEmployeeId(trainingId, emp.getId());
            if (!alreadyAssigned) {
                TrainingParticipant tp = new TrainingParticipant();
                tp.setTrainingId(trainingId);
                tp.setEmployeeId(emp.getId());
                tp.setAssignmentTargetType(request.getAssignmentType());
                tp.setParticipationStatus(ParticipationStatus.ASSIGNED);
                tp.setAssignedAt(LocalDateTime.now());
                newlyAssignedList.add(tp);
                newlyAssignedEmployeeIds.add(emp.getId());
            }
        }

        if (!newlyAssignedList.isEmpty()) {
            participantRepository.saveAll(newlyAssignedList);

            Map<String, Object> payload = Map.of(
                    "trainingId", trainingId,
                    "title", training.getTitle(),
                    "employeeIds", newlyAssignedEmployeeIds,
                    "assignmentType", request.getAssignmentType().name(),
                    "mandatory", Boolean.TRUE.equals(request.getMandatory()),
                    "dueDate", request.getDueDate() != null ? request.getDueDate().toString() : ""
            );
            publishOutboxEvent("TRAINING_ASSIGNED", trainingId.toString(), payload);
        }

        return participantRepository.findByTrainingId(trainingId);
    }

    private Set<Employee> resolveEmployeesForScope(Long orgId, AssignmentTargetType targetType, String targetId) {
        Set<Employee> employees = new LinkedHashSet<>();
        if (targetId == null || targetId.isBlank()) return employees;
        String idStr = targetId.trim();

        if (AssignmentTargetType.DEPARTMENT.equals(targetType)) {
            Long deptId = tryParseLong(idStr);
            String deptName = deptId != null ? departmentRepository.findById(deptId).map(com.example.ems.employee.entity.Department::getName).orElse(idStr) : idStr;
            List<Employee> list = employeeRepository.findByOrganizationId(orgId).stream()
                    .filter(e -> e.getDepartment() != null && e.getDepartment().equalsIgnoreCase(deptName))
                    .toList();
            employees.addAll(list);
        } else if (AssignmentTargetType.TEAM.equals(targetType)) {
            Long teamId = tryParseLong(idStr);
            List<Employee> list = employeeRepository.findByOrganizationId(orgId).stream()
                    .filter(e -> e.getTeam() != null &&
                            (teamId != null ? teamId.equals(e.getTeam().getId()) : idStr.equalsIgnoreCase(e.getTeam().getTeamName())))
                    .toList();
            employees.addAll(list);
        } else if (AssignmentTargetType.EMPLOYEE.equals(targetType)) {
            Long empDbId = tryParseLong(idStr);
            if (empDbId != null) {
                employeeRepository.findById(empDbId)
                        .filter(e -> e.getOrganization() != null && e.getOrganization().getId().equals(orgId))
                        .ifPresent(employees::add);
            } else {
                employeeRepository.findByEmployeeId(idStr)
                        .filter(e -> e.getOrganization() != null && e.getOrganization().getId().equals(orgId))
                        .ifPresent(employees::add);
            }
        }
        return employees;
    }

    @Transactional
    public void deleteAssignmentScope(Long trainingId, AssignmentTargetType targetType, String targetId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        getTrainingById(trainingId, currentUser);

        String trimmedTargetId = targetId != null ? targetId.trim() : "";

        List<TrainingAssignmentScope> activeScopesForType = scopeRepository
                .findByTrainingIdAndStatus(trainingId, "ACTIVE").stream()
                .filter(s -> s.getAssignmentType().equals(targetType))
                .toList();

        for (TrainingAssignmentScope scope : activeScopesForType) {
            boolean matches = scope.getTargetId().equalsIgnoreCase(trimmedTargetId);
            if (!matches && AssignmentTargetType.DEPARTMENT.equals(targetType)) {
                Long deptId = tryParseLong(trimmedTargetId);
                if (deptId != null) {
                    departmentRepository.findById(deptId).ifPresent(d -> {
                        if (d.getName().equalsIgnoreCase(scope.getTargetId()) || d.getName().toLowerCase().startsWith(scope.getTargetId().toLowerCase())) {
                            scope.setStatus("REMOVED");
                            scopeRepository.save(scope);
                        }
                    });
                }
            } else if (matches) {
                scope.setStatus("REMOVED");
                scopeRepository.save(scope);
            }
        }

        List<TrainingAssignmentScope> activeScopes = scopeRepository
                .findByTrainingIdAndStatus(trainingId, "ACTIVE");

        Set<Long> coveredEmployeeIds = new HashSet<>();
        for (TrainingAssignmentScope scope : activeScopes) {
            Set<Employee> emps = resolveEmployeesForScope(orgId, scope.getAssignmentType(), scope.getTargetId());
            emps.forEach(e -> coveredEmployeeIds.add(e.getId()));
        }

        List<TrainingParticipant> participants = participantRepository.findByTrainingId(trainingId);
        for (TrainingParticipant tp : participants) {
            if (!coveredEmployeeIds.contains(tp.getEmployeeId())) {
                if (ParticipationStatus.COMPLETED.equals(tp.getParticipationStatus())) {
                    continue;
                }
                tp.setParticipationStatus(ParticipationStatus.REVOKED);
                participantRepository.save(tp);
            }
        }
    }

    public DepartmentProgressResponse getDepartmentProgress(Long departmentId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        com.example.ems.employee.entity.Department dept = departmentRepository.findById(departmentId)
                .filter(d -> d.getOrganization() != null && d.getOrganization().getId().equals(orgId))
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

        List<Employee> deptEmployees = employeeRepository.findByOrganizationId(orgId).stream()
                .filter(e -> e.getDepartment() != null && e.getDepartment().equalsIgnoreCase(dept.getName()))
                .toList();

        long totalEmployees = deptEmployees.size();
        Set<Long> empIds = deptEmployees.stream().map(Employee::getId).collect(Collectors.toSet());

        List<TrainingParticipant> deptParticipants = empIds.isEmpty() ? List.of() :
                participantRepository.findAll().stream()
                        .filter(tp -> empIds.contains(tp.getEmployeeId()))
                        .toList();

        long assignedEmployees = deptParticipants.stream().map(TrainingParticipant::getEmployeeId).distinct().count();
        long completed = deptParticipants.stream().filter(tp -> ParticipationStatus.COMPLETED.equals(tp.getParticipationStatus())).count();
        long inProgress = deptParticipants.stream().filter(tp -> ParticipationStatus.IN_PROGRESS.equals(tp.getParticipationStatus()) || ParticipationStatus.ACCEPTED.equals(tp.getParticipationStatus())).count();
        long pending = deptParticipants.stream().filter(tp -> ParticipationStatus.ASSIGNED.equals(tp.getParticipationStatus()) || ParticipationStatus.PENDING.equals(tp.getParticipationStatus())).count();

        double pct = assignedEmployees > 0 ? ((double) completed / assignedEmployees) * 100.0 : 0.0;

        DepartmentProgressResponse resp = new DepartmentProgressResponse();
        resp.setDepartmentId(departmentId.toString());
        resp.setDepartmentName(dept.getName());
        resp.setTotalEmployees(totalEmployees);
        resp.setAssignedEmployees(assignedEmployees);
        resp.setCompleted(completed);
        resp.setInProgress(inProgress);
        resp.setPending(pending);
        resp.setCompletionPercentage(Math.round(pct * 100.0) / 100.0);
        return resp;
    }

    public TeamProgressResponse getTeamProgress(Long teamId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        com.example.ems.employee.entity.Team team = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));

        List<Employee> teamEmployees = employeeRepository.findByOrganizationId(orgId).stream()
                .filter(e -> e.getTeam() != null && e.getTeam().getId().equals(teamId))
                .toList();

        long totalEmployees = teamEmployees.size();
        Set<Long> empIds = teamEmployees.stream().map(Employee::getId).collect(Collectors.toSet());

        List<TrainingParticipant> teamParticipants = empIds.isEmpty() ? List.of() :
                participantRepository.findAll().stream()
                        .filter(tp -> empIds.contains(tp.getEmployeeId()))
                        .toList();

        long assignedEmployees = teamParticipants.stream().map(TrainingParticipant::getEmployeeId).distinct().count();
        long completed = teamParticipants.stream().filter(tp -> ParticipationStatus.COMPLETED.equals(tp.getParticipationStatus())).count();
        long inProgress = teamParticipants.stream().filter(tp -> ParticipationStatus.IN_PROGRESS.equals(tp.getParticipationStatus()) || ParticipationStatus.ACCEPTED.equals(tp.getParticipationStatus())).count();
        long pending = teamParticipants.stream().filter(tp -> ParticipationStatus.ASSIGNED.equals(tp.getParticipationStatus()) || ParticipationStatus.PENDING.equals(tp.getParticipationStatus())).count();

        double pct = assignedEmployees > 0 ? ((double) completed / assignedEmployees) * 100.0 : 0.0;

        TeamProgressResponse resp = new TeamProgressResponse();
        resp.setTeamId(teamId.toString());
        resp.setTeamName(team.getTeamName());
        resp.setTotalEmployees(totalEmployees);
        resp.setAssignedEmployees(assignedEmployees);
        resp.setCompleted(completed);
        resp.setInProgress(inProgress);
        resp.setPending(pending);
        resp.setCompletionPercentage(Math.round(pct * 100.0) / 100.0);
        return resp;
    }

    public EmployeeReportResponse getEmployeeReport(Long employeeDbId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Employee emp = employeeRepository.findById(employeeDbId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeDbId));
        if (emp.getOrganization() == null || !emp.getOrganization().getId().equals(orgId)) {
            throw new SecurityException("Access Denied: Employee belongs to a different organization");
        }

        List<TrainingParticipant> assignments = participantRepository.findByEmployeeId(employeeDbId);

        long total = assignments.size();
        long completed = assignments.stream().filter(tp -> ParticipationStatus.COMPLETED.equals(tp.getParticipationStatus())).count();
        long inProgress = assignments.stream().filter(tp -> ParticipationStatus.IN_PROGRESS.equals(tp.getParticipationStatus()) || ParticipationStatus.ACCEPTED.equals(tp.getParticipationStatus())).count();
        long pending = assignments.stream().filter(tp -> ParticipationStatus.ASSIGNED.equals(tp.getParticipationStatus()) || ParticipationStatus.PENDING.equals(tp.getParticipationStatus())).count();
        long overdue = 0;

        double pct = total > 0 ? ((double) completed / total) * 100.0 : 0.0;

        EmployeeReportResponse resp = new EmployeeReportResponse();
        resp.setEmployeeId(emp.getEmployeeId() != null ? emp.getEmployeeId() : employeeDbId.toString());
        resp.setEmployeeName(emp.getFullName());
        resp.setTotalTrainings(total);
        resp.setMandatoryTrainings(total);
        resp.setCompleted(completed);
        resp.setInProgress(inProgress);
        resp.setPending(pending);
        resp.setOverdue(overdue);
        resp.setCompletionPercentage(Math.round(pct * 100.0) / 100.0);
        return resp;
    }

    @Transactional
    public void removeParticipant(Long trainingId, Long employeeId, User currentUser) {
        getTrainingById(trainingId, currentUser);
        participantRepository.deleteByTrainingIdAndEmployeeId(trainingId, employeeId);
    }

    @Transactional
    public TrainingParticipant recordParticipantResponse(Long trainingId, ParticipationStatus responseStatus,
            String note, User currentUser) {
        Long empId = resolveEmployeeDbId(currentUser);
        if (empId == null)
            throw new IllegalArgumentException("Employee record not found for logged in user");

        TrainingParticipant participant = participantRepository.findByTrainingIdAndEmployeeId(trainingId, empId)
                .orElseThrow(() -> new IllegalArgumentException("You are not assigned to training ID: " + trainingId));

        if (!ParticipationStatus.ACCEPTED.equals(responseStatus)
                && !ParticipationStatus.DECLINED.equals(responseStatus)) {
            throw new IllegalArgumentException("Response status must be ACCEPTED or DECLINED");
        }

        participant.setParticipationStatus(responseStatus);
        participant.setRespondedAt(LocalDateTime.now());
        participant.setResponseNote(note);
        TrainingParticipant saved = participantRepository.save(participant);

        Map<String, Object> payload = Map.of("trainingId", trainingId, "employeeId", empId, "responseStatus",
                responseStatus.name());
        publishOutboxEvent(
                ParticipationStatus.ACCEPTED.equals(responseStatus) ? "TRAINING_ACCEPTED" : "TRAINING_DECLINED",
                trainingId.toString(), payload);

        return saved;
    }

    // ── 5. Attendance Management ─────────────────────────────────────────────
    @Transactional
    public List<TrainingAttendance> bulkRecordAttendance(Long trainingId, AttendanceBulkMarkRequest request,
            User currentUser) {
        getTrainingById(trainingId, currentUser);
        Long markerId = resolveEmployeeDbId(currentUser);
        if (markerId == null)
            markerId = 1L;

        List<TrainingAttendance> savedList = new ArrayList<>();
        for (AttendanceItemRequest item : request.getItems()) {
            Optional<TrainingAttendance> existing = attendanceRepository.findByTrainingIdAndSessionIdAndEmployeeId(
                    trainingId, request.getSessionId(), item.getEmployeeId());
            TrainingAttendance attendance = existing.orElseGet(TrainingAttendance::new);
            attendance.setTrainingId(trainingId);
            attendance.setSessionId(request.getSessionId());
            attendance.setEmployeeId(item.getEmployeeId());
            attendance.setAttendanceStatus(item.getAttendanceStatus());
            attendance.setCheckInTime(item.getCheckInTime());
            attendance.setCheckOutTime(item.getCheckOutTime());
            attendance.setDurationMinutes(item.getDurationMinutes());
            attendance.setMarkedBy(markerId);
            attendance.setRemarks(item.getRemarks());
            savedList.add(attendanceRepository.save(attendance));
        }
        return savedList;
    }

    public List<TrainingAttendance> getAttendanceRecords(Long trainingId, User currentUser) {
        getTrainingById(trainingId, currentUser);
        return attendanceRepository.findByTrainingId(trainingId);
    }

    @Transactional
    public TrainingAttendance updateParticipantAttendance(Long trainingId, Long employeeId,
            AttendanceStatus attendanceStatus, String remarks, User currentUser) {
        getTrainingById(trainingId, currentUser);
        Long markerId = resolveEmployeeDbId(currentUser);
        if (markerId == null)
            markerId = 1L;

        TrainingAttendance attendance = attendanceRepository.findByTrainingIdAndEmployeeId(trainingId, employeeId)
                .orElseGet(() -> {
                    TrainingAttendance a = new TrainingAttendance();
                    a.setTrainingId(trainingId);
                    a.setEmployeeId(employeeId);
                    return a;
                });
        attendance.setAttendanceStatus(attendanceStatus);
        attendance.setMarkedBy(markerId);
        attendance.setRemarks(remarks);
        return attendanceRepository.save(attendance);
    }

    // ── 6. Course Materials ──────────────────────────────────────────────────
    @Transactional
    public TrainingMaterial addMaterial(Long trainingId, MaterialCreateRequest request, User currentUser) {
        getTrainingById(trainingId, currentUser);
        Long actorId = resolveEmployeeDbId(currentUser);
        if (actorId == null)
            actorId = 1L;

        TrainingMaterial material = new TrainingMaterial();
        material.setTrainingId(trainingId);
        material.setTitle(request.getTitle());
        material.setMaterialType(request.getMaterialType());
        material.setUrlOrFilePath(request.getUrlOrFilePath());
        material.setFileSizeBytes(request.getFileSizeBytes());
        material.setCreatedBy(actorId);
        TrainingMaterial saved = materialRepository.save(material);

        Map<String, Object> payload = Map.of("trainingId", trainingId, "materialId", saved.getId(), "title",
                saved.getTitle());
        publishOutboxEvent("TRAINING_MATERIAL_UPLOADED", trainingId.toString(), payload);

        return saved;
    }

    public List<TrainingMaterial> getMaterials(Long trainingId, User currentUser) {
        getTrainingById(trainingId, currentUser);
        return materialRepository.findByTrainingId(trainingId);
    }

    @Transactional
    public void deleteMaterial(Long trainingId, Long materialId, User currentUser) {
        getTrainingById(trainingId, currentUser);
        materialRepository.deleteById(materialId);
    }

    // ── 7. Training Library ──────────────────────────────────────────────────
    @Transactional
    public TrainingLibraryResource createLibraryResource(LibraryResourceCreateRequest request, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Long actorId = resolveEmployeeDbId(currentUser);
        if (actorId == null)
            actorId = 1L;

        TrainingLibraryResource resource = new TrainingLibraryResource();
        resource.setOrganizationId(orgId);
        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setCategory(request.getCategory());
        resource.setTechnology(request.getTechnology());
        resource.setMaterialType(request.getMaterialType());
        resource.setResourceUrl(request.getResourceUrl());
        resource.setTrainerId(request.getTrainerId());
        resource.setDepartmentId(request.getDepartmentId());
        resource.setCreatedBy(actorId);
        return libraryResourceRepository.save(resource);
    }

    public List<TrainingLibraryResource> getLibraryResources(String category, String technology, Long trainerId,
            User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        return libraryResourceRepository.findWithFilters(orgId, category, technology, trainerId);
    }

    @Transactional
    public TrainingLibraryResource updateLibraryResource(Long resourceId, LibraryResourceCreateRequest request,
            User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        TrainingLibraryResource resource = libraryResourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Library resource not found ID: " + resourceId));
        if (!resource.getOrganizationId().equals(orgId)) {
            throw new SecurityException("Access Denied");
        }
        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setCategory(request.getCategory());
        resource.setTechnology(request.getTechnology());
        resource.setMaterialType(request.getMaterialType());
        resource.setResourceUrl(request.getResourceUrl());
        resource.setTrainerId(request.getTrainerId());
        resource.setDepartmentId(request.getDepartmentId());
        return libraryResourceRepository.save(resource);
    }

    @Transactional
    public void deleteLibraryResource(Long resourceId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        TrainingLibraryResource resource = libraryResourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Library resource not found ID: " + resourceId));
        if (!resource.getOrganizationId().equals(orgId)) {
            throw new SecurityException("Access Denied");
        }
        libraryResourceRepository.delete(resource);
    }

    // ── 8. Feedback ─────────────────────────────────────────────────────────
    @Transactional
    public TrainingFeedback submitFeedback(Long trainingId, FeedbackSubmitRequest request, User currentUser) {
        getTrainingById(trainingId, currentUser);
        Long empId = resolveEmployeeDbId(currentUser);
        if (empId == null)
            throw new IllegalArgumentException("Employee record not found for logged in user");

        if (feedbackRepository.existsByTrainingIdAndEmployeeId(trainingId, empId)) {
            throw new IllegalStateException("Feedback already submitted for this training");
        }

        TrainingFeedback feedback = new TrainingFeedback();
        feedback.setTrainingId(trainingId);
        feedback.setEmployeeId(empId);
        feedback.setRating(request.getRating());
        feedback.setContentQualityRating(request.getContentQualityRating());
        feedback.setTrainerRating(request.getTrainerRating());
        feedback.setOverallExperienceRating(request.getOverallExperienceRating());
        feedback.setComments(request.getComments());

        return feedbackRepository.save(feedback);
    }

    public Map<String, Object> getFeedbackSummary(Long trainingId, User currentUser) {
        getTrainingById(trainingId, currentUser);
        List<TrainingFeedback> feedbackList = feedbackRepository.findByTrainingId(trainingId);
        double avgRating = feedbackList.stream().mapToInt(TrainingFeedback::getRating).average().orElse(0.0);
        double avgContent = feedbackList.stream().filter(f -> f.getContentQualityRating() != null)
                .mapToInt(TrainingFeedback::getContentQualityRating).average().orElse(0.0);
        double avgTrainer = feedbackList.stream().filter(f -> f.getTrainerRating() != null)
                .mapToInt(TrainingFeedback::getTrainerRating).average().orElse(0.0);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("trainingId", trainingId);
        summary.put("totalResponses", feedbackList.size());
        summary.put("averageRating", Math.round(avgRating * 10.0) / 10.0);
        summary.put("averageContentQuality", Math.round(avgContent * 10.0) / 10.0);
        summary.put("averageTrainerRating", Math.round(avgTrainer * 10.0) / 10.0);
        summary.put("feedbacks", feedbackList);
        return summary;
    }

    // ── 9. My Trainings ──────────────────────────────────────────────────────
    public MyTrainingsResponse getMyTrainings(User currentUser) {
        Long empId = resolveEmployeeDbId(currentUser);
        MyTrainingsResponse response = new MyTrainingsResponse();
        if (empId == null)
            return response;

        List<TrainingParticipant> participants = participantRepository.findByEmployeeId(empId);
        LocalDateTime now = LocalDateTime.now();

        for (TrainingParticipant p : participants) {
            Optional<Training> tOpt = trainingRepository.findById(p.getTrainingId());
            if (tOpt.isPresent()) {
                Training t = tOpt.get();
                MyTrainingsResponse.MyTrainingItem item = new MyTrainingsResponse.MyTrainingItem(t,
                        p.getParticipationStatus());

                boolean hasAttended = attendanceRepository.findByTrainingIdAndEmployeeId(t.getId(), empId)
                        .map(a -> AttendanceStatus.ATTENDED.equals(a.getAttendanceStatus()))
                        .orElse(false);

                if (TrainingStatus.COMPLETED.equals(t.getStatus())) {
                    if (hasAttended) {
                        response.getCompleted().add(item);
                    } else {
                        response.getMissed().add(item);
                    }
                } else if (t.getStartDateTime().toLocalDate().equals(now.toLocalDate())) {
                    response.getToday().add(item);
                } else if (t.getStartDateTime().isAfter(now)) {
                    response.getUpcoming().add(item);
                } else if (t.getEndDateTime().isBefore(now)) {
                    if (hasAttended) {
                        response.getCompleted().add(item);
                    } else {
                        response.getMissed().add(item);
                    }
                } else {
                    response.getUpcoming().add(item);
                }
            }
        }
        return response;
    }

    // ── 10. Calendar Views ───────────────────────────────────────────────────
    public List<CalendarEventResponse> getCalendarEvents(LocalDateTime start, LocalDateTime end, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        List<Training> trainings = trainingRepository.findByCalendarRange(orgId, start, end);
        List<CalendarEventResponse> events = new ArrayList<>();
        for (Training t : trainings) {
            CalendarEventResponse event = new CalendarEventResponse();
            event.setTrainingId(t.getId());
            event.setTitle(t.getTitle());
            event.setStart(t.getStartDateTime());
            event.setEnd(t.getEndDateTime());
            event.setDeliveryMethod(t.getDeliveryMethod());
            event.setStatus(t.getStatus());
            event.setCategory(t.getCategory());
            event.setTrainerId(t.getTrainerId());
            events.add(event);
        }
        return events;
    }

    // ── 11. Dashboard Metrics ────────────────────────────────────────────────
    public TrainingDashboardMetricsResponse getDashboardMetrics(User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Long empId = resolveEmployeeDbId(currentUser);

        TrainingDashboardMetricsResponse metrics = new TrainingDashboardMetricsResponse();
        metrics.setTotalTrainings(trainingRepository.countByOrganizationId(orgId));
        metrics.setDraftTrainings(trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.DRAFT));
        metrics.setPendingApproval(
                trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.PENDING_APPROVAL));
        metrics.setUpcomingTrainings(trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.APPROVED)
                + trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.PUBLISHED));
        metrics.setOngoingTrainings(trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.ONGOING));
        metrics.setCompletedTrainings(
                trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.COMPLETED));
        metrics.setCancelledTrainings(
                trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.CANCELLED));

        if (empId != null) {
            metrics.setMyAssignedTrainings(participantRepository.findByEmployeeId(empId).size());
            metrics.setMyPendingResponses(participantRepository
                    .findByEmployeeIdAndParticipationStatus(empId, ParticipationStatus.PENDING).size());
        }
        return metrics;
    }

    // ── 12. Reports ──────────────────────────────────────────────────────────
    public TrainingReportSummaryResponse getReportSummary(User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        TrainingReportSummaryResponse summary = new TrainingReportSummaryResponse();
        summary.setTotalTrainings(trainingRepository.countByOrganizationId(orgId));
        summary.setCompletedTrainings(
                trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.COMPLETED));
        summary.setCancelledTrainings(
                trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.CANCELLED));
        summary.setUpcomingTrainings(
                trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.PUBLISHED));
        summary.setOngoingTrainings(trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.ONGOING));
        return summary;
    }

    public List<ParticipationReportResponse> getParticipationReport(User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        List<Training> trainings = trainingRepository.findByOrganizationId(orgId);
        List<ParticipationReportResponse> reports = new ArrayList<>();
        for (Training t : trainings) {
            ParticipationReportResponse r = new ParticipationReportResponse();
            r.setGroupName(t.getTitle());
            r.setTotalAssigned(participantRepository.countByTrainingId(t.getId()));
            r.setTotalAccepted(participantRepository.countByTrainingIdAndParticipationStatus(t.getId(),
                    ParticipationStatus.ACCEPTED));
            r.setTotalDeclined(participantRepository.countByTrainingIdAndParticipationStatus(t.getId(),
                    ParticipationStatus.DECLINED));
            double rate = r.getTotalAssigned() > 0 ? ((double) r.getTotalAccepted() / r.getTotalAssigned()) * 100.0
                    : 0.0;
            r.setResponseRate(Math.round(rate * 10.0) / 10.0);
            reports.add(r);
        }
        return reports;
    }

    public List<AttendanceReportResponse> getAttendanceReport(User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        List<Training> trainings = trainingRepository.findByOrganizationId(orgId);
        List<AttendanceReportResponse> reports = new ArrayList<>();
        for (Training t : trainings) {
            AttendanceReportResponse r = new AttendanceReportResponse();
            r.setDepartmentName(t.getTitle());
            r.setTotalAssigned(participantRepository.countByTrainingId(t.getId()));
            r.setTotalAttended(
                    attendanceRepository.countByTrainingIdAndAttendanceStatus(t.getId(), AttendanceStatus.ATTENDED));
            r.setTotalAbsent(
                    attendanceRepository.countByTrainingIdAndAttendanceStatus(t.getId(), AttendanceStatus.ABSENT));
            double completion = r.getTotalAssigned() > 0
                    ? ((double) r.getTotalAttended() / r.getTotalAssigned()) * 100.0
                    : 0.0;
            r.setCompletionPercentage(Math.round(completion * 10.0) / 10.0);
            reports.add(r);
        }
        return reports;
    }
}
