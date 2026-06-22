package com.example.ems.onboarding.service;

import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.onboarding.dto.OnboardingAssetRequest;
import com.example.ems.onboarding.dto.OnboardingDashboardResponse;
import com.example.ems.onboarding.dto.OnboardingDocumentResponse;
import com.example.ems.onboarding.dto.OnboardingRequest;
import com.example.ems.onboarding.dto.OnboardingResponse;
import com.example.ems.onboarding.dto.OnboardingTaskResponse;
import com.example.ems.onboarding.dto.OnboardingTrainingRequest;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.entity.OnboardingAsset;
import com.example.ems.onboarding.entity.OnboardingDocument;
import com.example.ems.onboarding.entity.OnboardingTask;
import com.example.ems.onboarding.entity.OnboardingTraining;
import com.example.ems.onboarding.repository.OnboardingAssetRepository;
import com.example.ems.onboarding.repository.OnboardingDocumentRepository;
import com.example.ems.onboarding.repository.OnboardingRepository;
import com.example.ems.onboarding.repository.OnboardingTaskRepository;
import com.example.ems.onboarding.repository.OnboardingTrainingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OnboardingService {

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private OnboardingTaskRepository onboardingTaskRepository;

    @Autowired
    private OnboardingDocumentRepository onboardingDocumentRepository;

    @Autowired
    private OnboardingAssetRepository onboardingAssetRepository;

    @Autowired
    private OnboardingTrainingRepository onboardingTrainingRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private OnboardingResponse buildResponse(Onboarding onboarding) {
        List<OnboardingTask> tasks = onboardingTaskRepository.findByOnboardingId(onboarding.getId());
        List<OnboardingDocument> docs = onboardingDocumentRepository.findByOnboardingId(onboarding.getId());
        List<OnboardingAsset> assets = onboardingAssetRepository.findByOnboardingId(onboarding.getId());
        List<OnboardingTraining> trainings = onboardingTrainingRepository.findByOnboardingId(onboarding.getId());
        return new OnboardingResponse(onboarding, tasks, docs, assets, trainings);
    }

    // ── 1. DASHBOARD STATS ──────────────────────────────────────────────────
    @Cacheable(value = "onboardingDashboard", key = "'stats'")
    public OnboardingDashboardResponse getDashboardStats() {
        OnboardingDashboardResponse stats = new OnboardingDashboardResponse();

        long total = onboardingRepository.count();
        long pending = onboardingRepository.findByStatus("PENDING").size();
        long inProgress = onboardingRepository.findByStatus("IN_PROGRESS").size();
        long completed = onboardingRepository.findByStatus("COMPLETED").size();
        long approved = onboardingRepository.findByStatus("APPROVED").size();

        long totalTasks = onboardingTaskRepository.count();
        long completedTasks = onboardingTaskRepository.findAll().stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()))
                .count();

        double taskRate = totalTasks > 0 ? ((double) completedTasks / totalTasks) * 100.0 : 0.0;

        long pendingVerifications = onboardingDocumentRepository.findAll().stream()
                .filter(d -> "PENDING".equalsIgnoreCase(d.getVerificationStatus()))
                .count();

        stats.setTotalOnboardings(total);
        stats.setPendingOnboardings(pending);
        stats.setInProgressOnboardings(inProgress);
        stats.setCompletedOnboardings(completed);
        stats.setApprovedOnboardings(approved);
        stats.setTotalTasksAssigned(totalTasks);
        stats.setCompletedTasksCount(completedTasks);
        stats.setTaskCompletionRate(Math.round(taskRate * 100.0) / 100.0);
        stats.setPendingVerifications(pendingVerifications);

        return stats;
    }

    // ── 2. CREATE ONBOARDING ────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "onboardingDashboard", allEntries = true)
    public OnboardingResponse createOnboarding(OnboardingRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

        Optional<Onboarding> existing = onboardingRepository.findByEmployeeId(request.getEmployeeId());
        if (existing.isPresent()) {
            throw new IllegalStateException("Onboarding already initialized for employee: " + employee.getFullName());
        }

        Onboarding onboarding = new Onboarding();
        onboarding.setEmployee(employee);
        onboarding.setStatus("PENDING");
        onboarding.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        
        Onboarding saved = onboardingRepository.save(onboarding);

        // Populate Default Tasks
        createDefaultTasks(saved);

        // Populate Default Trainings
        createDefaultTrainings(saved);

        return buildResponse(saved);
    }

    private void createDefaultTasks(Onboarding onboarding) {
        String[] titles = {
            "Submit Personal Documents",
            "Request Hardware/Assets",
            "Complete Compliance Trainings",
            "Schedule Manager Sync"
        };
        String[] descriptions = {
            "Please upload your Government ID and Bank Account details.",
            "Select hardware requirements (Laptop size, Keyboard, Monitor).",
            "Watch compliance and information security webinars.",
            "Setup a 30-minute sync meeting with your direct reporting manager."
        };

        for (int i = 0; i < titles.length; i++) {
            OnboardingTask task = new OnboardingTask();
            task.setOnboarding(onboarding);
            task.setTitle(titles[i]);
            task.setDescription(descriptions[i]);
            task.setStatus("PENDING");
            task.setDueDate(LocalDate.now().plusDays(7));
            onboardingTaskRepository.save(task);
        }
    }

    private void createDefaultTrainings(Onboarding onboarding) {
        String[] courses = {
            "Code of Business Conduct",
            "Information Security & Privacy Awareness",
            "Workplace Health and Safety"
        };

        for (String course : courses) {
            OnboardingTraining t = new OnboardingTraining();
            t.setOnboarding(onboarding);
            t.setCourseName(course);
            t.setStatus("ASSIGNED");
            onboardingTrainingRepository.save(t);
        }
    }

    public List<OnboardingResponse> getOnboardings() {
        return onboardingRepository.findAll().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    public Optional<OnboardingResponse> getOnboardingById(Long id) {
        return onboardingRepository.findById(id).map(this::buildResponse);
    }

    public Optional<OnboardingResponse> getOnboardingByEmployeeEmail(String email) {
        return onboardingRepository.findByEmployeeEmail(email).map(this::buildResponse);
    }

    // ── 3. UPDATE ONBOARDING PROFILE ────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "onboardingDashboard", allEntries = true)
    public OnboardingResponse updateOnboardingProfile(Long onboardingId, Map<String, Object> fields) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding not found with ID: " + onboardingId));

        Employee emp = onboarding.getEmployee();
        if (fields.containsKey("phone")) emp.setPhone((String) fields.get("phone"));
        if (fields.containsKey("gender")) emp.setGender((String) fields.get("gender"));
        if (fields.containsKey("address")) emp.setAddress((String) fields.get("address"));
        if (fields.containsKey("emergencyContact")) emp.setEmergencyContact((String) fields.get("emergencyContact"));
        if (fields.containsKey("location")) emp.setLocation((String) fields.get("location"));
        if (fields.containsKey("dob")) {
            String dobStr = (String) fields.get("dob");
            if (dobStr != null && !dobStr.isBlank()) {
                emp.setDob(LocalDate.parse(dobStr));
            }
        }

        employeeRepository.save(emp);
        
        onboarding.setStatus("IN_PROGRESS");
        onboarding.setUpdatedAt(LocalDateTime.now());
        onboardingRepository.save(onboarding);

        return buildResponse(onboarding);
    }

    // ── 4. DOCUMENTS ────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "onboardingDashboard", allEntries = true)
    public OnboardingDocumentResponse addDocument(Long onboardingId, String fileName, String contentType, String downloadUrl) {
        return addDocument(onboardingId, null, fileName, contentType, downloadUrl);
    }

    @Transactional
    @CacheEvict(value = "onboardingDashboard", allEntries = true)
    public OnboardingDocumentResponse addDocument(Long onboardingId, String documentType, String fileName, String contentType, String downloadUrl) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding not found with ID: " + onboardingId));

        OnboardingDocument doc = new OnboardingDocument();
        doc.setOnboarding(onboarding);
        doc.setDocumentType(documentType);
        doc.setFileName(fileName);
        doc.setFileType(contentType);
        doc.setDownloadUrl(downloadUrl);
        doc.setVerificationStatus("PENDING");

        OnboardingDocument saved = onboardingDocumentRepository.save(doc);

        onboarding.setStatus("IN_PROGRESS");
        onboarding.setUpdatedAt(LocalDateTime.now());
        onboardingRepository.save(onboarding);

        return new OnboardingDocumentResponse(saved);
    }

    public List<OnboardingDocumentResponse> getDocuments(Long onboardingId) {
        return onboardingDocumentRepository.findByOnboardingId(onboardingId).stream()
                .map(OnboardingDocumentResponse::new)
                .collect(Collectors.toList());
    }

    // ── 5. TASKS ────────────────────────────────────────────────────────────
    public List<OnboardingTaskResponse> getTasks(Long onboardingId) {
        return onboardingTaskRepository.findByOnboardingId(onboardingId).stream()
                .map(OnboardingTaskResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "onboardingDashboard", allEntries = true)
    public Optional<OnboardingTaskResponse> updateTaskStatus(Long taskId, String status) {
        return onboardingTaskRepository.findById(taskId).map(task -> {
            task.setStatus(status.toUpperCase());
            if ("COMPLETED".equalsIgnoreCase(status)) {
                task.setCompletedAt(LocalDateTime.now());
            } else {
                task.setCompletedAt(null);
            }
            return new OnboardingTaskResponse(onboardingTaskRepository.save(task));
        });
    }

    // ── 6. COMPLETE & APPROVE ────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "onboardingDashboard", allEntries = true)
    public Optional<OnboardingResponse> completeOnboarding(Long id) {
        return onboardingRepository.findById(id).map(onboarding -> {
            onboarding.setStatus("COMPLETED");
            onboarding.setCompletionDate(LocalDate.now());
            onboarding.setUpdatedAt(LocalDateTime.now());
            return buildResponse(onboardingRepository.save(onboarding));
        });
    }

    @Transactional
    @CacheEvict(value = "onboardingDashboard", allEntries = true)
    public Optional<OnboardingResponse> approveOnboarding(Long id) {
        return onboardingRepository.findById(id).map(onboarding -> {
            onboarding.setStatus("APPROVED");
            onboarding.setUpdatedAt(LocalDateTime.now());
            return buildResponse(onboardingRepository.save(onboarding));
        });
    }

    // ── 7. VERIFY DOCUMENT ──────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "onboardingDashboard", allEntries = true)
    public Optional<OnboardingDocumentResponse> verifyDocument(Long documentId, String status, String notes) {
        return onboardingDocumentRepository.findById(documentId).map(doc -> {
            doc.setVerificationStatus(status.toUpperCase());
            doc.setVerificationNotes(notes);
            return new OnboardingDocumentResponse(onboardingDocumentRepository.save(doc));
        });
    }

    // ── 8. PROVISION ASSETS & TRAININGS ──────────────────────────────────────
    @Transactional
    @CacheEvict(value = "onboardingDashboard", allEntries = true)
    public OnboardingResponse requestAsset(OnboardingAssetRequest request) {
        Onboarding onboarding = onboardingRepository.findById(request.getOnboardingId())
                .orElseThrow(() -> new IllegalArgumentException("Onboarding not found with ID: " + request.getOnboardingId()));

        OnboardingAsset asset = new OnboardingAsset();
        asset.setOnboarding(onboarding);
        asset.setAssetName(request.getAssetName());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setStatus("REQUESTED");

        onboardingAssetRepository.save(asset);
        
        onboarding.setStatus("IN_PROGRESS");
        onboarding.setUpdatedAt(LocalDateTime.now());
        onboardingRepository.save(onboarding);

        return buildResponse(onboarding);
    }

    @Transactional
    @CacheEvict(value = "onboardingDashboard", allEntries = true)
    public OnboardingResponse assignTraining(OnboardingTrainingRequest request) {
        Onboarding onboarding = onboardingRepository.findById(request.getOnboardingId())
                .orElseThrow(() -> new IllegalArgumentException("Onboarding not found with ID: " + request.getOnboardingId()));

        OnboardingTraining training = new OnboardingTraining();
        training.setOnboarding(onboarding);
        training.setCourseName(request.getCourseName());
        training.setStatus("ASSIGNED");

        onboardingTrainingRepository.save(training);
        
        onboarding.setStatus("IN_PROGRESS");
        onboarding.setUpdatedAt(LocalDateTime.now());
        onboardingRepository.save(onboarding);

        return buildResponse(onboarding);
    }

    @Transactional
    @CacheEvict(value = "onboardingDashboard", allEntries = true)
    public Map<String, Object> provisionAccess(Long id, Map<String, Object> body) {
        Onboarding onboarding = onboardingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding not found with ID: " + id));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("onboardingId", id);
        response.put("employeeName", onboarding.getEmployee().getFullName());
        response.put("provisionedAt", LocalDateTime.now());
        response.put("slackAccount", "CREATED");
        response.put("gitHubAccess", "GRANTED");
        response.put("corporateEmail", onboarding.getEmployee().getEmail());
        response.put("status", "SUCCESS");

        onboarding.setStatus("IN_PROGRESS");
        onboarding.setUpdatedAt(LocalDateTime.now());
        onboardingRepository.save(onboarding);

        return response;
    }

    // ── 9. REPORTS ──────────────────────────────────────────────────────────
    public Map<String, Object> getReportData(String reportType) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reportType", reportType);
        data.put("generatedAt", LocalDateTime.now());

        if ("summary".equalsIgnoreCase(reportType) || "dashboard".equalsIgnoreCase(reportType)) {
            data.put("totalOnboardingProfiles", onboardingRepository.count());
            data.put("completedCount", onboardingRepository.findByStatus("COMPLETED").size());
            data.put("inProgressCount", onboardingRepository.findByStatus("IN_PROGRESS").size());
            data.put("pendingCount", onboardingRepository.findByStatus("PENDING").size());
        } else {
            data.put("onboardingStats", getDashboardStats());
        }
        return data;
    }

    // ── 10. NOTIFICATIONS ────────────────────────────────────────────────────
    public Map<String, Object> triggerNotification(Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "TRIGGERED");
        result.put("sentAt", LocalDateTime.now());
        result.put("subject", body.getOrDefault("subject", "Onboarding Welcome Alert"));
        result.put("recipient", body.get("recipient"));
        return result;
    }

    @Transactional
    public Employee getOrCreateEmployeeForUser(User user) {
        return employeeRepository.findByEmail(user.getWorkEmail())
                .orElseGet(() -> {
                    Employee employee = new Employee();
                    employee.setFullName(user.getFullName());
                    employee.setEmail(user.getWorkEmail());
                    employee.setEmployeeId(user.getUserId() != null ? user.getUserId() : "EMP" + String.format("%03d", user.getId()));
                    employee.setDepartment(user.getDepartment());
                    employee.setLocation(user.getLocation());
                    employee.setStatus("ACTIVE");
                    return employeeRepository.save(employee);
                });
    }

    @Transactional
    public Onboarding getOrCreateOnboardingForEmployee(Employee employee) {
        return onboardingRepository.findByEmployeeEmail(employee.getEmail())
                .orElseGet(() -> {
                    Onboarding onboarding = new Onboarding();
                    onboarding.setEmployee(employee);
                    onboarding.setStatus("PENDING");
                    onboarding.setStartDate(LocalDate.now());
                    
                    Onboarding saved = onboardingRepository.save(onboarding);
                    createDefaultTasks(saved);
                    createDefaultTrainings(saved);
                    return saved;
                });
    }

    @Transactional
    @CacheEvict(value = "onboardingDashboard", allEntries = true)
    public Optional<OnboardingResponse> submitOnboarding(Long id) {
        return onboardingRepository.findById(id).map(onboarding -> {
            onboarding.setStatus("UNDER_REVIEW");
            onboarding.setCompletionDate(LocalDate.now());
            onboarding.setUpdatedAt(LocalDateTime.now());
            return buildResponse(onboardingRepository.save(onboarding));
        });
    }

    public List<Map<String, Object>> getOnboardingTimeline(Long onboardingId) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + onboardingId));

        List<Map<String, Object>> timeline = new ArrayList<>();

        // 1. Initialized
        Map<String, Object> init = new LinkedHashMap<>();
        init.put("date", onboarding.getCreatedAt().toString());
        init.put("type", "INITIALIZED");
        init.put("title", "Onboarding Initialized");
        init.put("description", "Onboarding process initialized for employee: " + onboarding.getEmployee().getFullName());
        timeline.add(init);

        // 2. Tasks completed
        List<OnboardingTask> tasks = onboardingTaskRepository.findByOnboardingId(onboardingId);
        for (OnboardingTask task : tasks) {
            if ("COMPLETED".equalsIgnoreCase(task.getStatus()) && task.getCompletedAt() != null) {
                Map<String, Object> tEvent = new LinkedHashMap<>();
                tEvent.put("date", task.getCompletedAt().toString());
                tEvent.put("type", "TASK_COMPLETED");
                tEvent.put("title", "Task Completed");
                tEvent.put("description", "Completed task: '" + task.getTitle() + "'");
                timeline.add(tEvent);
            }
        }

        // 3. Status changes
        if ("COMPLETED".equalsIgnoreCase(onboarding.getStatus()) && onboarding.getCompletionDate() != null) {
            Map<String, Object> comp = new LinkedHashMap<>();
            comp.put("date", onboarding.getCompletionDate().atStartOfDay().toString());
            comp.put("type", "COMPLETED");
            comp.put("title", "Onboarding Completed");
            comp.put("description", "Employee completed all self-service onboarding steps.");
            timeline.add(comp);
        }

        if ("APPROVED".equalsIgnoreCase(onboarding.getStatus())) {
            Map<String, Object> app = new LinkedHashMap<>();
            app.put("date", onboarding.getUpdatedAt().toString());
            app.put("type", "APPROVED");
            app.put("title", "HR Approved");
            app.put("description", "HR manager approved the employee onboarding profile.");
            timeline.add(app);
        }

        // Sort by date ascending
        timeline.sort((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")));

        return timeline;
    }
}
