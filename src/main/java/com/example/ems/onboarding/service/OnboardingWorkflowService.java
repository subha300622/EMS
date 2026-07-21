package com.example.ems.onboarding.service;

import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.onboarding.dto.*;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.entity.OnboardingDocument;
import com.example.ems.onboarding.entity.OnboardingTask;
import com.example.ems.onboarding.entity.OnboardingTemplate;
import com.example.ems.onboarding.repository.OnboardingDocumentRepository;
import com.example.ems.onboarding.repository.OnboardingRepository;
import com.example.ems.onboarding.repository.OnboardingTaskRepository;
import com.example.ems.onboarding.repository.OnboardingTemplateRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OnboardingWorkflowService {

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private OnboardingTaskRepository onboardingTaskRepository;

    @Autowired
    private OnboardingDocumentRepository onboardingDocumentRepository;

    @Autowired
    private OnboardingTemplateRepository templateRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public OnboardingStatsResponse getStats() {
        List<Onboarding> onboardings = onboardingRepository.findAll();
        List<OnboardingTask> tasks = onboardingTaskRepository.findAll();
        List<OnboardingDocument> documents = onboardingDocumentRepository.findAll();

        long activeCount = onboardings.stream()
                .filter(o -> !"COMPLETED".equalsIgnoreCase(o.getStatus()) && !"APPROVED".equalsIgnoreCase(o.getStatus())
                        && !"ARCHIVED".equalsIgnoreCase(o.getStatus()))
                .count();

        long preJoiningCount = onboardings.stream()
                .filter(o -> "PRE_JOINING".equalsIgnoreCase(o.getStatus()) || "PENDING".equalsIgnoreCase(o.getStatus()))
                .count();

        long completedCount = onboardings.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()) || "APPROVED".equalsIgnoreCase(o.getStatus()))
                .count();

        LocalDate today = LocalDate.now();
        long overdueTasks = tasks.stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()) && t.getDueDate() != null
                        && t.getDueDate().isBefore(today))
                .count();

        long pendingDocuments = documents.stream()
                .filter(d -> d.getFileName() != null && d.getFileName().startsWith("[Pending Upload]"))
                .count();

        long uploadedDocuments = documents.stream()
                .filter(d -> d.getFileName() != null && !d.getFileName().startsWith("[Pending Upload]"))
                .count();

        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        long joiningThisWeek = onboardings.stream()
                .filter(o -> o.getJoiningDate() != null && !o.getJoiningDate().isBefore(startOfWeek)
                        && !o.getJoiningDate().isAfter(endOfWeek))
                .count();

        OnboardingStatsResponse stats = new OnboardingStatsResponse();
        stats.setActiveCount(activeCount);
        stats.setPreJoiningCount(preJoiningCount);
        stats.setCompletedCount(completedCount);
        stats.setOverdueTasks(overdueTasks);
        stats.setPendingDocuments(pendingDocuments);
        stats.setUploadedDocuments(uploadedDocuments);
        stats.setJoiningThisWeek(joiningThisWeek);

        return stats;
    }

    public OnboardingQueueResponse getOnboardingQueue(String status, String search, String department,
            LocalDate joiningFrom, LocalDate joiningTo, int page, int limit) {
        List<Onboarding> all = onboardingRepository.findAll();

        java.util.stream.Stream<Onboarding> stream = all.stream();

        if (status != null && !status.isBlank()) {
            if ("pre-joining".equalsIgnoreCase(status)) {
                stream = stream.filter(o -> "PRE_JOINING".equalsIgnoreCase(o.getStatus())
                        || "PENDING".equalsIgnoreCase(o.getStatus()));
            } else {
                stream = stream.filter(o -> status.equalsIgnoreCase(o.getStatus()));
            }
        }

        if (search != null && !search.isBlank()) {
            String lowerSearch = search.toLowerCase();
            stream = stream.filter(o -> o.getEmployee() != null
                    && (o.getEmployee().getFullName().toLowerCase().contains(lowerSearch) ||
                            o.getEmployee().getEmail().toLowerCase().contains(lowerSearch)));
        }

        if (department != null && !department.isBlank()) {
            stream = stream.filter(
                    o -> o.getEmployee() != null && department.equalsIgnoreCase(o.getEmployee().getDepartment()));
        }

        if (joiningFrom != null) {
            stream = stream.filter(o -> o.getJoiningDate() != null && !o.getJoiningDate().isBefore(joiningFrom));
        }

        if (joiningTo != null) {
            stream = stream.filter(o -> o.getJoiningDate() != null && !o.getJoiningDate().isAfter(joiningTo));
        }

        List<Onboarding> filtered = stream.collect(Collectors.toList());

        int total = filtered.size();
        int totalPages = (int) Math.ceil((double) total / limit);
        if (totalPages == 0)
            totalPages = 1;

        int fromIndex = (page - 1) * limit;
        List<Onboarding> pageItems = Collections.emptyList();
        if (fromIndex < total) {
            int toIndex = Math.min(fromIndex + limit, total);
            pageItems = filtered.subList(fromIndex, toIndex);
        }

        List<OnboardingQueueResponse.QueueItem> items = pageItems.stream()
                .map(this::mapToQueueItem)
                .collect(Collectors.toList());

        OnboardingQueueResponse response = new OnboardingQueueResponse();
        response.setItems(items);
        response.setPagination(new OnboardingQueueResponse.Pagination(page, limit, total, totalPages));

        return response;
    }

    @Transactional
    public OnboardingLaunchResponse launchOnboarding(OnboardingLaunchRequest request) {
        // Validate Employee
        Employee employee = employeeRepository.findByEmployeeId(request.getEmployeeId())
                .or(() -> employeeRepository.findByEmail(request.getEmail()))
                .orElseGet(() -> {
                    Employee e = new Employee();
                    e.setEmployeeId(request.getEmployeeId());
                    e.setFullName(request.getEmployeeName());
                    e.setEmail(request.getEmail());
                    e.setDepartment(request.getDepartment());
                    e.setDesignation(request.getDesignation());
                    e.setEmploymentType(request.getEmploymentType());
                    e.setStatus("ACTIVE");
                    return employeeRepository.save(e);
                });

        // Check duplicate active onboarding
        Optional<Onboarding> existing = onboardingRepository.findByEmployeeId(employee.getId());
        if (existing.isPresent()) {
            Onboarding existingOnb = existing.get();
            if (!"COMPLETED".equalsIgnoreCase(existingOnb.getStatus())
                    && !"APPROVED".equalsIgnoreCase(existingOnb.getStatus())
                    && !"ARCHIVED".equalsIgnoreCase(existingOnb.getStatus())) {
                throw new ConflictException("Active onboarding already exists for this employee");
            }
        }

        // Validate template
        OnboardingTemplate template = templateRepository.findByTemplateCode(request.getTemplateId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Template not found with ID: " + request.getTemplateId()));

        // Create onboarding record
        Onboarding onboarding = new Onboarding();
        onboarding.setEmployee(employee);
        onboarding.setJoiningDate(request.getJoiningDate());
        onboarding.setStartDate(LocalDate.now());
        onboarding.setStatus("PRE_JOINING");
        onboarding.setAssignedTemplateId(template.getTemplateCode());

        // Assign team members
        if (request.getTeamAssignments() != null) {
            onboarding.setHrOwnerId(request.getTeamAssignments().getHrOwnerId());
            onboarding.setBuddyIdString(request.getTeamAssignments().getBuddyId());
            onboarding.setItContactId(request.getTeamAssignments().getItContactId());
            onboarding.setFinanceContactId(request.getTeamAssignments().getFinanceContactId());
        }

        // Resolve reporting manager
        if (request.getReportingManager() != null && !request.getReportingManager().isBlank()) {
            employeeRepository.findByFullName(request.getReportingManager()).ifPresent(onboarding::setManager);
        }

        onboarding = onboardingRepository.save(onboarding);

        // Copy template tasks & documents
        List<OnboardingTemplateCreateRequest.SectionRequest> sections;
        List<OnboardingTemplateCreateRequest.DocumentRequest> documents;
        try {
            sections = objectMapper.readValue(template.getSectionsJson(),
                    new TypeReference<List<OnboardingTemplateCreateRequest.SectionRequest>>() {
                    });
            documents = objectMapper.readValue(template.getDocumentsJson(),
                    new TypeReference<List<OnboardingTemplateCreateRequest.DocumentRequest>>() {
                    });
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize template structures: " + e.getMessage());
        }

        int tasksCreated = 0;
        int documentsCreated = 0;

        for (OnboardingTemplateCreateRequest.SectionRequest sec : sections) {
            for (OnboardingTemplateCreateRequest.TaskRequest taskTemplate : sec.getTasks()) {
                OnboardingTask task = new OnboardingTask();
                task.setOnboarding(onboarding);
                task.setTitle(taskTemplate.getName());
                task.setDescription(taskTemplate.getDescription());
                task.setStatus("PENDING");
                task.setPhase(sec.getName());
                task.setOwner(taskTemplate.getOwnerType());
                task.setPriority(taskTemplate.getPriority());
                task.setDueDate(request.getJoiningDate().plusDays(taskTemplate.getDueDays()));
                onboardingTaskRepository.save(task);
                tasksCreated++;
            }
        }

        for (OnboardingTemplateCreateRequest.DocumentRequest docTemplate : documents) {
            OnboardingDocument doc = new OnboardingDocument();
            doc.setOnboarding(onboarding);
            doc.setFileName("[Pending Upload] - " + docTemplate.getName());
            doc.setVerificationStatus("PENDING");
            doc.setDocumentType(docTemplate.getName());
            onboardingDocumentRepository.save(doc);
            documentsCreated++;
        }

        OnboardingLaunchResponse resp = new OnboardingLaunchResponse();
        OnboardingLaunchResponse.OnboardingInfo info = new OnboardingLaunchResponse.OnboardingInfo();
        info.setId("onb-" + onboarding.getId());
        info.setEmployeeId(employee.getEmployeeId() != null ? employee.getEmployeeId() : "emp-" + employee.getId());
        info.setAssignedTemplateId(template.getTemplateCode());
        info.setStatus(onboarding.getStatus().toLowerCase());
        info.setProgress(0);
        info.setJoiningDate(onboarding.getJoiningDate());
        resp.setOnboarding(info);
        resp.setPhasesCreated(sections.size());
        resp.setTasksCreated(tasksCreated);
        resp.setDocumentsCreated(documentsCreated);

        return resp;
    }

    public OnboardingQueueResponse.QueueItem getOnboardingDetails(Long onboardingId) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Onboarding details not found with ID: " + onboardingId));
        return mapToQueueItem(onboarding);
    }

    @Transactional
    public OnboardingQueueResponse.QueueItem updateOnboarding(Long onboardingId, OnboardingUpdateRequest request) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found with ID: " + onboardingId));

        if (request.getJoiningDate() != null) {
            onboarding.setJoiningDate(request.getJoiningDate());
        }
        if (request.getStatus() != null) {
            onboarding.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getReportingManager() != null && !request.getReportingManager().isBlank()) {
            employeeRepository.findByFullName(request.getReportingManager())
                    .ifPresent(onboarding::setManager);
        }

        onboarding.setUpdatedAt(LocalDateTime.now());
        onboarding = onboardingRepository.save(onboarding);
        return mapToQueueItem(onboarding);
    }

    @Transactional
    public void deleteOnboarding(Long onboardingId) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found with ID: " + onboardingId));

        // Delete associated tasks
        List<OnboardingTask> tasks = onboardingTaskRepository.findByOnboardingId(onboardingId);
        onboardingTaskRepository.deleteAll(tasks);

        // Delete associated documents
        List<OnboardingDocument> docs = onboardingDocumentRepository.findByOnboardingId(onboardingId);
        onboardingDocumentRepository.deleteAll(docs);

        onboardingRepository.delete(onboarding);
    }

    @Transactional
    public java.util.Map<String, Object> assignTemplate(Long onboardingId, OnboardingAssignTemplateRequest request) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found with ID: " + onboardingId));

        OnboardingTemplate template = templateRepository.findByTemplateCode(request.getTemplateId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Template not found with ID: " + request.getTemplateId()));

        onboarding.setAssignedTemplateId(template.getTemplateCode());

        int phasesCreated = 0;
        int tasksCreated = 0;
        int documentsCreated = 0;

        if (request.isRegenerateWorkflow()) {
            List<OnboardingTask> existingTasks = onboardingTaskRepository.findByOnboardingId(onboardingId);
            for (OnboardingTask t : existingTasks) {
                if (!"COMPLETED".equalsIgnoreCase(t.getStatus())) {
                    onboardingTaskRepository.delete(t);
                }
            }

            List<OnboardingDocument> existingDocs = onboardingDocumentRepository.findByOnboardingId(onboardingId);
            for (OnboardingDocument d : existingDocs) {
                if ("PENDING".equalsIgnoreCase(d.getVerificationStatus())
                        && d.getFileName().startsWith("[Pending Upload]")) {
                    onboardingDocumentRepository.delete(d);
                }
            }

            List<OnboardingTemplateCreateRequest.SectionRequest> sections;
            List<OnboardingTemplateCreateRequest.DocumentRequest> documents;
            try {
                sections = objectMapper.readValue(template.getSectionsJson(),
                        new TypeReference<List<OnboardingTemplateCreateRequest.SectionRequest>>() {
                        });
                documents = objectMapper.readValue(template.getDocumentsJson(),
                        new TypeReference<List<OnboardingTemplateCreateRequest.DocumentRequest>>() {
                        });
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse template JSON: " + e.getMessage());
            }

            phasesCreated = sections.size();

            for (OnboardingTemplateCreateRequest.SectionRequest sec : sections) {
                for (OnboardingTemplateCreateRequest.TaskRequest taskTemplate : sec.getTasks()) {
                    boolean alreadyCompleted = existingTasks.stream()
                            .anyMatch(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())
                                    && t.getTitle().equalsIgnoreCase(taskTemplate.getName()));
                    if (!alreadyCompleted) {
                        OnboardingTask task = new OnboardingTask();
                        task.setOnboarding(onboarding);
                        task.setTitle(taskTemplate.getName());
                        task.setDescription(taskTemplate.getDescription());
                        task.setStatus("PENDING");
                        task.setPhase(sec.getName());
                        task.setOwner(taskTemplate.getOwnerType());
                        task.setPriority(taskTemplate.getPriority());
                        task.setDueDate(onboarding.getJoiningDate() != null
                                ? onboarding.getJoiningDate().plusDays(taskTemplate.getDueDays())
                                : LocalDate.now().plusDays(taskTemplate.getDueDays()));
                        onboardingTaskRepository.save(task);
                        tasksCreated++;
                    }
                }
            }

            for (OnboardingTemplateCreateRequest.DocumentRequest docTemplate : documents) {
                boolean alreadyUploaded = existingDocs.stream()
                        .anyMatch(d -> !"PENDING".equalsIgnoreCase(d.getVerificationStatus())
                                && d.getDocumentType() != null
                                && d.getDocumentType().equalsIgnoreCase(docTemplate.getName()));
                if (!alreadyUploaded) {
                    OnboardingDocument doc = new OnboardingDocument();
                    doc.setOnboarding(onboarding);
                    doc.setFileName("[Pending Upload] - " + docTemplate.getName());
                    doc.setVerificationStatus("PENDING");
                    doc.setDocumentType(docTemplate.getName());
                    onboardingDocumentRepository.save(doc);
                    documentsCreated++;
                }
            }
        }

        onboardingRepository.save(onboarding);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("onboardingId", "onb-" + onboarding.getId());
        response.put("assignedTemplateId", template.getTemplateCode());
        response.put("phasesCreated", phasesCreated);
        response.put("tasksCreated", tasksCreated);
        response.put("documentsCreated", documentsCreated);

        return response;
    }

    private OnboardingQueueResponse.QueueItem mapToQueueItem(Onboarding o) {
        Employee emp = o.getEmployee();

        OnboardingQueueResponse.QueueItem item = new OnboardingQueueResponse.QueueItem();
        item.setId("onb-" + o.getId());
        item.setEmployeeId(emp.getEmployeeId() != null ? emp.getEmployeeId() : "emp-" + emp.getId());
        item.setName(emp.getFullName());
        item.setEmail(emp.getEmail());
        item.setInitials(getInitials(emp.getFullName()));
        item.setAvatarColor(getAvatarColor(emp.getFullName()));
        item.setRole(emp.getDesignation());
        item.setDept(emp.getDepartment());
        item.setDeptColor("#00B87C");
        item.setJoiningDate(o.getJoiningDate());
        item.setProgress(o.getProgress());
        item.setStatus(o.getStatus().toLowerCase());

        long days = 0;
        if (o.getStartDate() != null) {
            days = ChronoUnit.DAYS.between(o.getStartDate(), LocalDate.now());
        }
        item.setDaysInOnboarding(Math.max(0, days));

        item.setExpectedCompletion("To be scheduled");

        String managerName = "To be assigned";
        if (o.getManager() != null) {
            managerName = o.getManager().getFullName();
        } else if (emp.getManager() != null) {
            managerName = emp.getManager().getFullName();
        }
        item.setManager(managerName);
        item.setAssignedTemplateId(o.getAssignedTemplateId());

        return item;
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty())
            return "EE";
        String[] parts = name.split("\\s+");
        return (parts[0].charAt(0) + "" +
                (parts.length > 1 ? parts[1].charAt(0) : ""))
                .toUpperCase();
    }

    private String getAvatarColor(String name) {
        if (name == null)
            return "#8B5CF6";
        String[] colors = { "#8B5CF6", "#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#EC4899" };
        return colors[Math.abs(name.hashCode()) % colors.length];
    }
}
