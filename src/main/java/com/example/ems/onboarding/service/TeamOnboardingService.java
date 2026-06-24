package com.example.ems.onboarding.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.onboarding.dto.*;
import com.example.ems.onboarding.entity.*;
import com.example.ems.onboarding.event.OnboardingCompletedEvent;
import com.example.ems.onboarding.event.PhaseCompletedEvent;
import com.example.ems.onboarding.event.TaskCompletedEvent;
import com.example.ems.onboarding.event.DocumentVerifiedEvent;
import com.example.ems.onboarding.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamOnboardingService {

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private OnboardingTaskRepository onboardingTaskRepository;

    @Autowired
    private OnboardingPhaseRepository onboardingPhaseRepository;

    @Autowired
    private OnboardingDocumentRepository onboardingDocumentRepository;

    @Autowired
    private OnboardingTemplateRepository templateRepository;

    @Autowired
    private OnboardingTemplateTaskRepository templateTaskRepository;

    @Autowired
    private OnboardingTemplateSnapshotRepository templateSnapshotRepository;

    @Autowired
    private OnboardingTemplateTaskSnapshotRepository templateTaskSnapshotRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private static final Map<String, Double> PHASE_WEIGHTS = new LinkedHashMap<>();
    static {
        PHASE_WEIGHTS.put("PRE_JOINING", 0.20);
        PHASE_WEIGHTS.put("DAY_1", 0.30);
        PHASE_WEIGHTS.put("WEEK_1", 0.30);
        PHASE_WEIGHTS.put("MONTH_1", 0.20);
    }

    // ── 1. INITIALIZE ONBOARDING FOR NEW EMPLOYEE ───────────────────────────
    @Transactional
    public void initializeOnboardingForEmployee(Employee employee) {
        if (onboardingRepository.findByEmployeeId(employee.getId()).isPresent()) {
            return; // Already initialized
        }

        // 1. Resolve ACTIVE onboarding template
        OnboardingTemplate template = templateRepository.findByIsActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active Onboarding Template found in the system."));

        // 2. Create Onboarding record
        Onboarding onboarding = new Onboarding();
        onboarding.setEmployee(employee);
        onboarding.setStatus("INITIATED");
        onboarding.setJoiningDate(employee.getJoiningDate() != null ? employee.getJoiningDate() : LocalDate.now());
        onboarding.setManager(employee.getManager());
        onboarding.setStartDate(LocalDate.now());
        Onboarding savedOnboarding = onboardingRepository.save(onboarding);

        // 3. Create Template Snapshot
        OnboardingTemplateSnapshot snapshot = new OnboardingTemplateSnapshot();
        snapshot.setOnboarding(savedOnboarding);
        snapshot.setTemplateId(template.getId());
        snapshot.setVersion(template.getVersion());
        OnboardingTemplateSnapshot savedSnapshot = templateSnapshotRepository.save(snapshot);

        // 4. Generate snapshot tasks and instance tasks
        List<OnboardingTemplateTask> templateTasks = templateTaskRepository.findByTemplateId(template.getId());
        for (OnboardingTemplateTask tempTask : templateTasks) {
            // Save Task Snapshot
            OnboardingTemplateTaskSnapshot taskSnap = new OnboardingTemplateTaskSnapshot();
            taskSnap.setSnapshot(savedSnapshot);
            taskSnap.setTitle(tempTask.getTitle());
            taskSnap.setDescription(tempTask.getDescription());
            taskSnap.setPhase(tempTask.getPhase());
            taskSnap.setOwner(tempTask.getOwner());
            taskSnap.setEstimatedTime(tempTask.getEstimatedTime());
            taskSnap.setDueDaysAfterJoining(tempTask.getDueDaysAfterJoining());
            taskSnap.setPriority(tempTask.getPriority());
            taskSnap.setSlaHours(tempTask.getSlaHours());
            templateTaskSnapshotRepository.save(taskSnap);

            // Save Onboarding Task Instance
            OnboardingTask task = new OnboardingTask();
            task.setOnboarding(savedOnboarding);
            task.setTitle(tempTask.getTitle());
            task.setDescription(tempTask.getDescription());
            task.setStatus("PENDING");
            task.setPhase(tempTask.getPhase());
            task.setOwner(tempTask.getOwner());
            task.setEstimatedTime(tempTask.getEstimatedTime());
            task.setPriority(tempTask.getPriority());
            task.setSlaHours(tempTask.getSlaHours());
            task.setDueDate(savedOnboarding.getJoiningDate().plusDays(tempTask.getDueDaysAfterJoining()));
            onboardingTaskRepository.save(task);
        }

        // 5. Initialize 4 phase cache rows
        for (Map.Entry<String, Double> entry : PHASE_WEIGHTS.entrySet()) {
            String phaseName = entry.getKey();
            Double weight = entry.getValue();

            long totalTasks = templateTasks.stream()
                    .filter(t -> phaseName.equalsIgnoreCase(t.getPhase()))
                    .count();

            OnboardingPhase phase = new OnboardingPhase();
            phase.setOnboarding(savedOnboarding);
            phase.setPhaseName(phaseName);
            phase.setTotalTasks((int) totalTasks);
            phase.setCompletedTasks(0);
            phase.setWeightProgress(weight);
            phase.setStatus("NOT_STARTED");
            onboardingPhaseRepository.save(phase);
        }
    }

    // ── 2. MANUAL FALLBACK INITIALIZATION ──────────────────────────────────
    @Transactional
    public TeamOnboardingCreateResponse createOnboardingManual(TeamOnboardingCreateRequest request) {
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new BadRequestException("Employee not found with ID: " + request.employeeId()));

        if (onboardingRepository.findByEmployeeId(employee.getId()).isPresent()) {
            throw new BadRequestException("Onboarding workflow already exists for employee with ID: " + request.employeeId());
        }

        if (request.managerId() != null) {
            Employee manager = employeeRepository.findById(request.managerId())
                    .orElseThrow(() -> new BadRequestException("Manager not found with ID: " + request.managerId()));
            employee.setManager(manager);
            employeeRepository.save(employee);
        }

        if (request.joiningDate() != null) {
            employee.setJoiningDate(request.joiningDate());
            employeeRepository.save(employee);
        }

        initializeOnboardingForEmployee(employee);

        Onboarding onboarding = onboardingRepository.findByEmployeeId(employee.getId()).get();
        return new TeamOnboardingCreateResponse(onboarding.getId(), onboarding.getStatus(), "Onboarding created successfully");
    }

    // ── 3. MANAGER ONBOARDING DASHBOARD ─────────────────────────────────────
    public Map<String, Object> getManagerDashboard(Long managerId) {
        List<Onboarding> teamOnboardings = onboardingRepository.findAll().stream()
                .filter(o -> o.getManager() != null && o.getManager().getId().equals(managerId))
                .collect(Collectors.toList());

        long newJoinersCount = teamOnboardings.stream()
                .filter(o -> !"COMPLETED".equalsIgnoreCase(o.getStatus()))
                .count();

        long pendingTasksCount = teamOnboardings.stream()
                .flatMap(o -> onboardingTaskRepository.findByOnboardingId(o.getId()).stream())
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()))
                .count();

        double avgProgress = teamOnboardings.isEmpty() ? 0.0 :
                teamOnboardings.stream().mapToInt(Onboarding::getProgress).average().orElse(0.0);

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("newJoinersCount", newJoinersCount);
        dashboard.put("pendingTasksCount", pendingTasksCount);
        dashboard.put("avgProgress", Math.round(avgProgress * 100.0) / 100.0);
        return dashboard;
    }

    // ── 4. TEAM ONBOARDING LIST ─────────────────────────────────────────────
    public List<Map<String, Object>> getTeamOnboardingList(Long managerId) {
        List<Onboarding> teamOnboardings = onboardingRepository.findAll().stream()
                .filter(o -> o.getManager() != null && o.getManager().getId().equals(managerId))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Onboarding o : teamOnboardings) {
            Employee emp = o.getEmployee();
            long pendingTasks = onboardingTaskRepository.findByOnboardingId(o.getId()).stream()
                    .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()))
                    .count();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("employeeId", emp.getId());
            map.put("name", emp.getFullName());
            map.put("designation", emp.getDesignation() != null ? emp.getDesignation() : "Software Engineer");
            map.put("location", emp.getLocation() != null ? emp.getLocation() : "HQ");
            map.put("joiningDate", o.getJoiningDate().toString());
            map.put("progress", o.getProgress());
            map.put("status", o.getStatus().equalsIgnoreCase("IN_PROGRESS") ? "ONBOARDING_IN_PROGRESS" : o.getStatus());
            map.put("pendingTasks", pendingTasks);
            result.add(map);
        }
        return result;
    }

    // ── 5. ASSIGN BUDDY ─────────────────────────────────────────────────────
    @Transactional
    public Map<String, String> assignBuddy(Long onboardingId, Long buddyEmployeeId) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new BadRequestException("Onboarding record not found with ID: " + onboardingId));

        OnboardingStateValidator.validateTransition(onboarding.getStatus(), "IN_PROGRESS");

        Employee buddy = employeeRepository.findById(buddyEmployeeId)
                .orElseThrow(() -> new BadRequestException("Buddy employee not found with ID: " + buddyEmployeeId));

        onboarding.setBuddy(buddy);

        if ("INITIATED".equalsIgnoreCase(onboarding.getStatus())) {
            onboarding.setStatus("IN_PROGRESS");
        }
        onboarding.setUpdatedAt(LocalDateTime.now());
        onboardingRepository.save(onboarding);

        return Map.of("message", "Buddy assigned successfully", "status", "UPDATED");
    }

    // ── 6. GET FULL ONBOARDING DETAILS (CORE DETAILS API) ───────────────────
    public TeamOnboardingDetailResponse getOnboardingDetails(Long onboardingId) {
        Onboarding o = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new BadRequestException("Onboarding record not found with ID: " + onboardingId));

        Employee emp = o.getEmployee();
        TeamOnboardingDetailResponse.EmployeeInfo empInfo = new TeamOnboardingDetailResponse.EmployeeInfo(
                emp.getId(),
                emp.getFullName(),
                emp.getDesignation() != null ? emp.getDesignation() : "Developer",
                emp.getDepartment() != null ? emp.getDepartment() : "Engineering",
                o.getManager() != null ? o.getManager().getFullName() : "N/A",
                o.getJoiningDate()
        );

        TeamOnboardingDetailResponse.BuddyInfo buddyInfo;
        if (o.getBuddy() != null) {
            buddyInfo = new TeamOnboardingDetailResponse.BuddyInfo(true, o.getBuddy().getId(), o.getBuddy().getFullName());
        } else {
            buddyInfo = new TeamOnboardingDetailResponse.BuddyInfo(false, null, null);
        }

        // Determine current phase: first phase that is not COMPLETED
        List<OnboardingPhase> phases = onboardingPhaseRepository.findByOnboardingId(onboardingId);
        String currentPhase = "PRE_JOINING";
        for (String phaseName : PHASE_WEIGHTS.keySet()) {
            Optional<OnboardingPhase> pOpt = phases.stream().filter(p -> p.getPhaseName().equalsIgnoreCase(phaseName)).findFirst();
            if (pOpt.isPresent() && !"COMPLETED".equalsIgnoreCase(pOpt.get().getStatus())) {
                currentPhase = phaseName;
                break;
            }
        }

        TeamOnboardingDetailResponse.ProgressInfo progInfo = new TeamOnboardingDetailResponse.ProgressInfo(
                o.getProgress(),
                o.getJoiningDate().plusDays(14), // expected completion is joiningDate + 14 days
                currentPhase
        );

        List<TeamOnboardingDetailResponse.PhaseInfo> phaseInfos = new ArrayList<>();
        for (String phaseName : PHASE_WEIGHTS.keySet()) {
            OnboardingPhase phase = phases.stream()
                    .filter(p -> p.getPhaseName().equalsIgnoreCase(phaseName))
                    .findFirst()
                    .orElseGet(() -> {
                        OnboardingPhase dummy = new OnboardingPhase();
                        dummy.setPhaseName(phaseName);
                        dummy.setStatus("NOT_STARTED");
                        return dummy;
                    });
            phaseInfos.add(new TeamOnboardingDetailResponse.PhaseInfo(
                    phase.getPhaseName(),
                    phase.getStatus(),
                    phase.getCompletedTasks(),
                    phase.getTotalTasks()
            ));
        }

        return new TeamOnboardingDetailResponse(empInfo, buddyInfo, progInfo, phaseInfos);
    }

    // ── 7. TASK MANAGEMENT ENDPOINTS ────────────────────────────────────────
    public List<TeamOnboardingTaskResponse> getTasksByPhase(Long onboardingId, String phase) {
        List<OnboardingTask> tasks = onboardingTaskRepository.findByOnboardingIdAndPhase(onboardingId, phase);
        return tasks.stream()
                .map(t -> new TeamOnboardingTaskResponse(
                        t.getId(),
                        t.getTitle(),
                        t.getPhase(),
                        t.getOwner() != null ? t.getOwner() : "EMPLOYEE",
                        t.getStatus(),
                        t.getDueDate(),
                        t.getEstimatedTime()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void completeTask(Long taskId, Long completedById) {
        OnboardingTask task = onboardingTaskRepository.findById(taskId)
                .orElseThrow(() -> new BadRequestException("Onboarding task not found with ID: " + taskId));

        // Idempotency check
        if ("COMPLETED".equalsIgnoreCase(task.getStatus())) {
            return; // Already completed, ignore to prevent duplicate event loops
        }

        Onboarding onboarding = task.getOnboarding();
        if ("ON_HOLD".equalsIgnoreCase(onboarding.getStatus())) {
            throw new BadRequestException("Task execution is frozen because onboarding is ON_HOLD.");
        }
        OnboardingStateValidator.validateTransition(onboarding.getStatus(), "IN_PROGRESS");

        // Manager / Buddy Lock check (frozen after IN_PROGRESS)
        if ("INITIATED".equalsIgnoreCase(onboarding.getStatus())) {
            onboarding.setStatus("IN_PROGRESS");
            onboardingRepository.save(onboarding);
        }

        Employee completedBy = employeeRepository.findById(completedById)
                .orElseThrow(() -> new BadRequestException("Completer employee not found with ID: " + completedById));

        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        task.setCompletedBy(completedBy);
        onboardingTaskRepository.save(task);

        // Publish event for propagation
        eventPublisher.publishEvent(new TaskCompletedEvent(this, onboarding.getId(), task.getId(), task.getPhase()));
    }

    @Transactional
    public void updateProgressOnTaskCompletion(Long onboardingId, Long taskId, String phaseName) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new BadRequestException("Onboarding not found with ID: " + onboardingId));

        OnboardingPhase phase = onboardingPhaseRepository.findByOnboardingIdAndPhaseName(onboardingId, phaseName)
                .orElseThrow(() -> new BadRequestException("Phase not found: " + phaseName));

        // Recalculate phase completion counts
        List<OnboardingTask> phaseTasks = onboardingTaskRepository.findByOnboardingIdAndPhase(onboardingId, phaseName);
        int total = phaseTasks.size();
        int completed = (int) phaseTasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();

        phase.setTotalTasks(total);
        phase.setCompletedTasks(completed);

        if (completed == total && total > 0) {
            phase.setStatus("COMPLETED");
            eventPublisher.publishEvent(new PhaseCompletedEvent(this, onboardingId, phaseName));
        } else if (completed > 0) {
            phase.setStatus("IN_PROGRESS");
        } else {
            phase.setStatus("NOT_STARTED");
        }
        onboardingPhaseRepository.save(phase);

        // Recalculate overall weighted progress
        List<OnboardingPhase> allPhases = onboardingPhaseRepository.findByOnboardingId(onboardingId);
        double overallProgress = 0.0;
        for (OnboardingPhase op : allPhases) {
            double completionRate = op.getTotalTasks() > 0 ? (double) op.getCompletedTasks() / op.getTotalTasks() : 1.0;
            double weight = PHASE_WEIGHTS.getOrDefault(op.getPhaseName(), 0.0);
            overallProgress += completionRate * weight;
        }

        onboarding.setProgress((int) Math.round(overallProgress * 100.0));
        onboarding.setUpdatedAt(LocalDateTime.now());

        // Check overall completion
        checkAndTriggerOnboardingCompletion(onboarding);
        onboardingRepository.save(onboarding);
    }

    private void checkAndTriggerOnboardingCompletion(Onboarding onboarding) {
        List<OnboardingTask> allTasks = onboardingTaskRepository.findByOnboardingId(onboarding.getId());
        boolean allTasksCompleted = allTasks.stream().allMatch(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()));

        List<OnboardingDocument> docs = onboardingDocumentRepository.findByOnboardingId(onboarding.getId());
        boolean allDocsVerified = !docs.isEmpty() && docs.stream().allMatch(d -> "VERIFIED".equalsIgnoreCase(d.getVerificationStatus()));

        // Onboarding is completed when all tasks are complete AND all documents verified
        if (allTasksCompleted && allDocsVerified && !"COMPLETED".equalsIgnoreCase(onboarding.getStatus())) {
            OnboardingStateValidator.validateTransition(onboarding.getStatus(), "COMPLETED");
            onboarding.setStatus("COMPLETED");
            onboarding.setCompletionDate(LocalDate.now());
            eventPublisher.publishEvent(new OnboardingCompletedEvent(this, onboarding.getId()));
        }
    }

    // ── 8. PAUSE / RESUME / RETRY LIFECYCLE ──────────────────────────────────
    @Transactional
    public void pauseOnboarding(Long id) {
        Onboarding o = onboardingRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Onboarding not found with ID: " + id));

        OnboardingStateValidator.validateTransition(o.getStatus(), "ON_HOLD");
        o.setStatus("ON_HOLD");
        o.setUpdatedAt(LocalDateTime.now());
        onboardingRepository.save(o);
    }

    @Transactional
    public void resumeOnboarding(Long id) {
        Onboarding o = onboardingRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Onboarding not found with ID: " + id));

        OnboardingStateValidator.validateTransition(o.getStatus(), "IN_PROGRESS");
        o.setStatus("IN_PROGRESS");
        o.setUpdatedAt(LocalDateTime.now());
        onboardingRepository.save(o);
    }

    @Transactional
    public void retryOnboarding(Long id) {
        Onboarding o = onboardingRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Onboarding not found with ID: " + id));

        // Rebuild phases/tasks from snapshot if present, otherwise from active template
        List<OnboardingTask> tasks = onboardingTaskRepository.findByOnboardingId(id);
        if (tasks.isEmpty()) {
            // Delete phase caches and rebuild
            List<OnboardingPhase> phases = onboardingPhaseRepository.findByOnboardingId(id);
            onboardingPhaseRepository.deleteAll(phases);

            Optional<OnboardingTemplateSnapshot> snapOpt = templateSnapshotRepository.findByOnboardingId(id);
            if (snapOpt.isPresent()) {
                OnboardingTemplateSnapshot snap = snapOpt.get();
                List<OnboardingTemplateTaskSnapshot> snapTasks = templateTaskSnapshotRepository.findBySnapshotId(snap.getId());
                for (OnboardingTemplateTaskSnapshot st : snapTasks) {
                    OnboardingTask t = new OnboardingTask();
                    t.setOnboarding(o);
                    t.setTitle(st.getTitle());
                    t.setDescription(st.getDescription());
                    t.setStatus("PENDING");
                    t.setPhase(st.getPhase());
                    t.setOwner(st.getOwner());
                    t.setEstimatedTime(st.getEstimatedTime());
                    t.setPriority(st.getPriority());
                    t.setSlaHours(st.getSlaHours());
                    t.setDueDate(o.getJoiningDate().plusDays(st.getDueDaysAfterJoining()));
                    onboardingTaskRepository.save(t);
                }

                // Restore phases
                for (Map.Entry<String, Double> entry : PHASE_WEIGHTS.entrySet()) {
                    String phaseName = entry.getKey();
                    long totalTasks = snapTasks.stream().filter(t -> phaseName.equalsIgnoreCase(t.getPhase())).count();

                    OnboardingPhase phase = new OnboardingPhase();
                    phase.setOnboarding(o);
                    phase.setPhaseName(phaseName);
                    phase.setTotalTasks((int) totalTasks);
                    phase.setCompletedTasks(0);
                    phase.setWeightProgress(entry.getValue());
                    phase.setStatus("NOT_STARTED");
                    onboardingPhaseRepository.save(phase);
                }
            }
        }
    }

    // ── 9. DOCUMENT ENDPOINTS ───────────────────────────────────────────────
    public List<Map<String, Object>> getDocuments(Long onboardingId) {
        List<OnboardingDocument> docs = onboardingDocumentRepository.findByOnboardingId(onboardingId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (OnboardingDocument d : docs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", d.getDocumentType() != null ? d.getDocumentType() : "OTHER");
            map.put("status", d.getVerificationStatus());
            map.put("uploadedBy", d.getFileName() != null ? d.getFileName() : "Employee");
            result.add(map);
        }

        // Always ensure PAN and AADHAR slots appear in response
        boolean hasAadhar = result.stream().anyMatch(m -> "AADHAR".equalsIgnoreCase((String) m.get("type")));
        boolean hasPan = result.stream().anyMatch(m -> "PAN".equalsIgnoreCase((String) m.get("type")));

        if (!hasAadhar) {
            Map<String, Object> aadhar = new LinkedHashMap<>();
            aadhar.put("type", "AADHAR");
            aadhar.put("status", "PENDING");
            result.add(aadhar);
        }
        if (!hasPan) {
            Map<String, Object> pan = new LinkedHashMap<>();
            pan.put("type", "PAN");
            pan.put("status", "PENDING");
            result.add(pan);
        }

        return result;
    }

    @Transactional
    public Map<String, Object> addDocument(Long onboardingId, String documentType, String fileName, String contentType, String downloadUrl) {
        Onboarding o = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new BadRequestException("Onboarding not found with ID: " + onboardingId));

        OnboardingDocument doc = new OnboardingDocument();
        doc.setOnboarding(o);
        doc.setDocumentType(documentType);
        doc.setFileName(fileName);
        doc.setFileType(contentType);
        doc.setDownloadUrl(downloadUrl);
        doc.setVerificationStatus("UPLOADED");
        onboardingDocumentRepository.save(doc);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("documentType", documentType);
        response.put("status", "UPLOADED");
        response.put("uploadedBy", o.getEmployee().getFullName());
        return response;
    }

    @Transactional
    public Map<String, String> verifyDocument(Long documentId, String status, String notes) {
        OnboardingDocument doc = onboardingDocumentRepository.findById(documentId)
                .orElseThrow(() -> new BadRequestException("Document not found with ID: " + documentId));

        doc.setVerificationStatus(status.toUpperCase());
        doc.setVerificationNotes(notes);
        onboardingDocumentRepository.save(doc);

        eventPublisher.publishEvent(new DocumentVerifiedEvent(this, documentId, status.toUpperCase(), notes));

        return Map.of("message", "Document verified successfully", "status", status.toUpperCase());
    }

    // ── 10. HR ANALYTICS & DASHBOARD SUMMARY ────────────────────────────────
    public Map<String, Object> getHrSummary() {
        List<Onboarding> onboardings = onboardingRepository.findAll();

        long activeOnboardings = onboardings.stream()
                .filter(o -> !"COMPLETED".equalsIgnoreCase(o.getStatus()))
                .count();

        // Onboardings completed in current month
        LocalDate startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        long completedThisMonth = onboardings.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()) && o.getCompletionDate() != null
                        && !o.getCompletionDate().isBefore(startOfMonth) && !o.getCompletionDate().isAfter(endOfMonth))
                .count();

        // Tasks overdue
        long tasksOverdue = onboardingTaskRepository.findAll().stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()) && t.getDueDate().isBefore(LocalDate.now()))
                .count();

        // Joining this week (Monday to Sunday)
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY);

        long joiningThisWeek = onboardings.stream()
                .filter(o -> !o.getJoiningDate().isBefore(startOfWeek) && !o.getJoiningDate().isAfter(endOfWeek))
                .count();

        // Pending verifications
        long pendingDocuments = onboardingDocumentRepository.findAll().stream()
                .filter(d -> "PENDING".equalsIgnoreCase(d.getVerificationStatus()) || "UPLOADED".equalsIgnoreCase(d.getVerificationStatus()))
                .count();

        // Average completion days
        List<Onboarding> completedList = onboardings.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()) && o.getCompletionDate() != null)
                .collect(Collectors.toList());

        double avgCompletionTimeDays = completedList.isEmpty() ? 14.0 :
                completedList.stream()
                        .mapToLong(o -> java.time.temporal.ChronoUnit.DAYS.between(o.getJoiningDate(), o.getCompletionDate()))
                        .average()
                        .orElse(14.0);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("activeOnboardings", activeOnboardings);
        summary.put("completedThisMonth", completedThisMonth);
        summary.put("tasksOverdue", tasksOverdue);
        summary.put("joiningThisWeek", joiningThisWeek);
        summary.put("pendingDocuments", pendingDocuments);
        summary.put("avgCompletionTimeDays", (int) Math.round(avgCompletionTimeDays));
        return summary;
    }

    // ── 11. HR ACTIVE LIST ──────────────────────────────────────────────────
    public List<Map<String, Object>> getHrActiveList() {
        List<Onboarding> onboardings = onboardingRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Onboarding o : onboardings) {
            Employee emp = o.getEmployee();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("onboardingId", o.getId());
            map.put("employeeId", emp.getId());
            map.put("employeeCode", emp.getEmployeeId() != null ? emp.getEmployeeId() : "EMP");
            map.put("name", emp.getFullName());
            map.put("role", emp.getDesignation() != null ? emp.getDesignation() : "Developer");
            map.put("department", emp.getDepartment() != null ? emp.getDepartment() : "Engineering");
            map.put("joiningDate", o.getJoiningDate().toString());
            map.put("progress", o.getProgress());
            map.put("status", o.getStatus());
            map.put("managerName", o.getManager() != null ? o.getManager().getFullName() : "N/A");
            result.add(map);
        }
        return result;
    }

    // ── 12. HR OVERDUE LIST ─────────────────────────────────────────────────
    public List<Map<String, Object>> getHrOverdueTasks() {
        List<OnboardingTask> overdueTasks = onboardingTaskRepository.findAll().stream()
                .filter(t -> !"COMPLETED".equalsIgnoreCase(t.getStatus()) && t.getDueDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (OnboardingTask t : overdueTasks) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("employeeName", t.getOnboarding().getEmployee().getFullName());
            map.put("task", t.getTitle());
            map.put("dueDate", t.getDueDate().toString());
            map.put("status", "OVERDUE");
            result.add(map);
        }
        return result;
    }

    // ── 13. RECONCILIATION FOR DRIFT ────────────────────────────────────────
    @Transactional
    public void reconcileProgressCache(Long onboardingId) {
        Onboarding o = onboardingRepository.findById(onboardingId).orElse(null);
        if (o == null) return;

        List<OnboardingPhase> phases = onboardingPhaseRepository.findByOnboardingId(onboardingId);
        List<OnboardingTask> tasks = onboardingTaskRepository.findByOnboardingId(onboardingId);

        double overallProgress = 0.0;
        for (OnboardingPhase op : phases) {
            List<OnboardingTask> pTasks = tasks.stream()
                    .filter(t -> op.getPhaseName().equalsIgnoreCase(t.getPhase()))
                    .collect(Collectors.toList());

            int total = pTasks.size();
            int completed = (int) pTasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();

            op.setTotalTasks(total);
            op.setCompletedTasks(completed);

            if (completed == total && total > 0) {
                op.setStatus("COMPLETED");
            } else if (completed > 0) {
                op.setStatus("IN_PROGRESS");
            } else {
                op.setStatus("NOT_STARTED");
            }
            onboardingPhaseRepository.save(op);

            double completionRate = total > 0 ? (double) completed / total : 1.0;
            double weight = PHASE_WEIGHTS.getOrDefault(op.getPhaseName(), 0.0);
            overallProgress += completionRate * weight;
        }

        o.setProgress((int) Math.round(overallProgress * 100.0));
        if ("INITIATED".equalsIgnoreCase(o.getStatus()) && o.getProgress() > 0) {
            o.setStatus("IN_PROGRESS");
        }
        o.setUpdatedAt(LocalDateTime.now());
        checkAndTriggerOnboardingCompletion(o);
        onboardingRepository.save(o);
    }

    public List<OnboardingDocument> getPendingVerifications() {
        return onboardingDocumentRepository.findAll().stream()
                .filter(d -> "PENDING".equalsIgnoreCase(d.getVerificationStatus()) || "UPLOADED".equalsIgnoreCase(d.getVerificationStatus()))
                .collect(Collectors.toList());
    }
}
