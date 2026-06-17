package com.example.ems.offboarding.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.entity.*;
import com.example.ems.offboarding.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MyExitService {

    @Autowired
    private OffboardingRepository offboardingRepository;

    @Autowired
    private OffboardingTaskRepository offboardingTaskRepository;

    @Autowired
    private OffboardingAssetReturnRepository offboardingAssetReturnRepository;

    @Autowired
    private OffboardingSettlementRepository offboardingSettlementRepository;

    @Autowired
    private ExitInterviewRepository exitInterviewRepository;

    @Autowired
    private ExitDocumentRepository exitDocumentRepository;

    @Autowired
    private ExitAgreementRepository exitAgreementRepository;

    @Autowired
    private ExitTimelineEventRepository exitTimelineEventRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public MyExitDashboardResponse getMyExitDashboard(String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        Offboarding offboarding = offboardingRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No exit request found for your account."));

        MyExitDashboardResponse.EmployeeInfo empInfo = new MyExitDashboardResponse.EmployeeInfo(
                emp.getId(),
                emp.getEmployeeId(),
                emp.getFullName(),
                emp.getDepartment(),
                emp.getDesignation()
        );

        long daysRemaining = 0;
        if (offboarding.getExitDate() != null) {
            daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), offboarding.getExitDate());
            if (daysRemaining < 0) daysRemaining = 0;
        }

        MyExitDashboardResponse.ExitRequestInfo reqInfo = new MyExitDashboardResponse.ExitRequestInfo(
                offboarding.getId(),
                offboarding.getStatus(),
                offboarding.getResignationDate(),
                offboarding.getExitDate(),
                daysRemaining,
                offboarding.getCurrentStage() != null ? offboarding.getCurrentStage() : "IT_CLEARANCE"
        );

        List<OffboardingTask> tasks = offboardingTaskRepository.findByOffboardingId(offboarding.getId());
        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()))
                .count();
        int compPercentage = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;

        MyExitDashboardResponse.ProgressInfo progInfo = new MyExitDashboardResponse.ProgressInfo(
                completedTasks,
                totalTasks,
                compPercentage
        );

        return new MyExitDashboardResponse(empInfo, reqInfo, progInfo);
    }

    @Transactional
    public SubmitResignationResponse submitResignation(String email, SubmitResignationRequest request) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        Optional<Offboarding> existing = offboardingRepository.findByEmployeeEmail(email);
        if (existing.isPresent()) {
            throw new IllegalStateException("An exit request is already active for this account.");
        }

        Offboarding offboarding = new Offboarding();
        offboarding.setEmployee(emp);
        offboarding.setStatus("PENDING_MANAGER_APPROVAL");
        offboarding.setCurrentStage("MANAGER_APPROVAL");
        offboarding.setReason(request.getReason());
        offboarding.setReasonCategory(request.getReasonCategory());
        offboarding.setResignationDate(request.getResignationDate());
        offboarding.setRequestedLastWorkingDay(request.getRequestedLastWorkingDay());
        offboarding.setExitDate(request.getRequestedLastWorkingDay());
        offboarding.setComments(request.getComments());
        offboarding.setCreatedAt(LocalDateTime.now());
        offboarding.setUpdatedAt(LocalDateTime.now());
        offboarding = offboardingRepository.save(offboarding);

        // 1. Initialize assigned assets
        OffboardingAssetReturn asset1 = new OffboardingAssetReturn();
        asset1.setOffboarding(offboarding);
        asset1.setAssetName("Dell Latitude 5440");
        asset1.setAssetCategory("LAPTOP");
        asset1.setSerialNumber("DL12345");
        asset1.setReturnStatus("PENDING");
        asset1 = offboardingAssetReturnRepository.save(asset1);

        OffboardingAssetReturn asset2 = new OffboardingAssetReturn();
        asset2.setOffboarding(offboarding);
        asset2.setAssetName("iPhone 14 Pro");
        asset2.setAssetCategory("MOBILE");
        asset2.setSerialNumber("IP12345");
        asset2.setReturnStatus("PENDING");
        asset2 = offboardingAssetReturnRepository.save(asset2);

        // 2. Initialize checklist tasks
        OffboardingTask task1 = new OffboardingTask();
        task1.setOffboarding(offboarding);
        task1.setTitle("Submit Resignation Letter");
        task1.setDescription("Employee initiates exit process by submitting a formal resignation.");
        task1.setStatus("COMPLETED");
        task1.setCompletedAt(LocalDateTime.now());
        task1.setAssignedTo("EMPLOYEE");
        task1.setActionRequired(false);
        task1.setDueDate(offboarding.getExitDate());
        offboardingTaskRepository.save(task1);

        OffboardingTask task2 = new OffboardingTask();
        task2.setOffboarding(offboarding);
        task2.setTitle("Upload Clearance Documents");
        task2.setDescription("Upload NOC, ID cards, access card handover proof, etc.");
        task2.setStatus("IN_PROGRESS");
        task2.setAssignedTo("EMPLOYEE");
        task2.setActionRequired(true);
        task2.setAllowedActions("UPLOAD_DOCUMENT");
        task2.setDueDate(offboarding.getExitDate());
        offboardingTaskRepository.save(task2);

        OffboardingTask task3 = new OffboardingTask();
        task3.setOffboarding(offboarding);
        task3.setTitle("Return Company Laptop");
        task3.setDescription("Laptop return verification by IT.");
        task3.setStatus("PENDING");
        task3.setAssignedTo("IT");
        task3.setActionRequired(false);
        task3.setAssetId(asset1.getId());
        task3.setDueDate(offboarding.getExitDate());
        offboardingTaskRepository.save(task3);

        OffboardingTask task4 = new OffboardingTask();
        task4.setOffboarding(offboarding);
        task4.setTitle("Return Mobile Phone");
        task4.setDescription("Mobile phone return verification by IT.");
        task4.setStatus("PENDING");
        task4.setAssignedTo("IT");
        task4.setActionRequired(false);
        task4.setAssetId(asset2.getId());
        task4.setDueDate(offboarding.getExitDate());
        offboardingTaskRepository.save(task4);

        OffboardingTask task5 = new OffboardingTask();
        task5.setOffboarding(offboarding);
        task5.setTitle("Sign NDA / Exit Agreement");
        task5.setDescription("Employee signs the exit non-disclosure agreement.");
        task5.setStatus("PENDING");
        task5.setAssignedTo("EMPLOYEE");
        task5.setActionRequired(true);
        task5.setAllowedActions("SIGN_AGREEMENT");
        task5.setDueDate(offboarding.getExitDate());
        offboardingTaskRepository.save(task5);

        // 3. Initialize F&F Settlement
        OffboardingSettlement settlement = new OffboardingSettlement();
        settlement.setOffboarding(offboarding);
        settlement.setPendingSalary(BigDecimal.valueOf(87000));
        settlement.setGratuity(BigDecimal.valueOf(54000));
        settlement.setLeaveEncashment(BigDecimal.valueOf(28000));
        settlement.setReimbursements(BigDecimal.valueOf(4200));
        settlement.setDeductions(BigDecimal.ZERO);
        settlement.setTotalSettlementAmount(BigDecimal.valueOf(173200));
        settlement.setPaymentStatus("PENDING_FINANCE_APPROVAL");
        settlement.setExpectedSettlementDate(LocalDate.now().plusDays(20));
        offboardingSettlementRepository.save(settlement);

        // 4. Initialize Timeline Events
        ExitTimelineEvent event1 = new ExitTimelineEvent();
        event1.setOffboarding(offboarding);
        event1.setAction("Resignation Submitted");
        event1.setPerformedBy(emp.getFullName());
        event1.setEventDate(LocalDateTime.now());
        exitTimelineEventRepository.save(event1);

        ExitTimelineEvent event2 = new ExitTimelineEvent();
        event2.setOffboarding(offboarding);
        event2.setAction("Manager Approved Resignation");
        event2.setPerformedBy("Manager");
        event2.setEventDate(LocalDateTime.now().plusHours(1));
        exitTimelineEventRepository.save(event2);

        return new SubmitResignationResponse(
                "Resignation request submitted successfully",
                offboarding.getId(),
                offboarding.getStatus(),
                LocalDateTime.now()
        );
    }

    @Transactional(readOnly = true)
    public ExitChecklistResponse getExitChecklist(String email) {
        Offboarding offboarding = offboardingRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No exit request found for your account."));

        List<OffboardingTask> tasks = offboardingTaskRepository.findByOffboardingId(offboarding.getId());
        List<ExitChecklistResponse.ChecklistItem> checklist = tasks.stream().map(t -> {
            List<String> allowed = new ArrayList<>();
            if (t.getAllowedActions() != null && !t.getAllowedActions().isBlank()) {
                allowed = Arrays.asList(t.getAllowedActions().split(","));
            }
            return new ExitChecklistResponse.ChecklistItem(
                    t.getId(),
                    t.getTitle(),
                    t.getAssignedTo(),
                    t.getStatus(),
                    t.getCompletedAt(),
                    t.getActionRequired() != null ? t.getActionRequired() : false,
                    allowed,
                    t.getAssetId()
            );
        }).collect(Collectors.toList());

        int completed = (int) tasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        int pending = tasks.size() - completed;

        return new ExitChecklistResponse(checklist, new ExitChecklistResponse.ChecklistSummary(completed, pending));
    }

    @Transactional
    public UploadDocumentResponse uploadDocument(String email, String documentType, String fileName, String comments) {
        Offboarding offboarding = offboardingRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No exit request found for your account."));

        ExitDocument doc = new ExitDocument();
        doc.setOffboarding(offboarding);
        doc.setDocumentType(documentType);
        doc.setFileName(fileName);
        doc.setComments(comments);
        doc.setUploadedBy(offboarding.getEmployee().getFullName());
        doc.setUploadedAt(LocalDateTime.now());
        doc.setStatus("UNDER_VERIFICATION");
        doc = exitDocumentRepository.save(doc);

        // Update the checklist task if any matching upload task
        List<OffboardingTask> tasks = offboardingTaskRepository.findByOffboardingId(offboarding.getId());
        for (OffboardingTask t : tasks) {
            if (t.getTitle().toLowerCase().contains("document") || t.getTitle().toLowerCase().contains("clearance")) {
                t.setStatus("IN_PROGRESS");
                offboardingTaskRepository.save(t);
            }
        }

        return new UploadDocumentResponse(
                doc.getId(),
                doc.getFileName(),
                doc.getDocumentType(),
                doc.getUploadedBy(),
                doc.getUploadedAt(),
                doc.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public UploadedDocumentsResponse getUploadedDocuments(String email) {
        Offboarding offboarding = offboardingRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No exit request found for your account."));

        List<ExitDocument> docs = exitDocumentRepository.findByOffboardingId(offboarding.getId());
        List<UploadedDocumentsResponse.DocumentItem> items = docs.stream().map(d -> new UploadedDocumentsResponse.DocumentItem(
                d.getId(),
                d.getDocumentType(),
                d.getFileName(),
                d.getStatus(),
                d.getVerifiedBy(),
                d.getVerifiedAt()
        )).collect(Collectors.toList());

        return new UploadedDocumentsResponse(items);
    }

    @Transactional
    public AssetReturnConfirmResponse confirmAssetReturn(String email, Long assetId, AssetReturnConfirmRequest request) {
        Offboarding offboarding = offboardingRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No exit request found for your account."));

        OffboardingAssetReturn asset = offboardingAssetReturnRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found with ID: " + assetId));

        if (!asset.getOffboarding().getId().equals(offboarding.getId())) {
            throw new IllegalArgumentException("Asset does not belong to your offboarding record.");
        }

        asset.setReturnStatus("RETURN_PENDING_VERIFICATION");
        asset.setReturnDate(request.getReturnDate());
        asset.setCondition(request.getCondition());
        asset.setRemarks(request.getRemarks());
        asset.setReturnedAt(LocalDateTime.now());
        asset = offboardingAssetReturnRepository.save(asset);

        // Find corresponding task and update it
        List<OffboardingTask> tasks = offboardingTaskRepository.findByOffboardingId(offboarding.getId());
        for (OffboardingTask t : tasks) {
            if (assetId.equals(t.getAssetId())) {
                t.setStatus("IN_PROGRESS");
                offboardingTaskRepository.save(t);
            }
        }

        return new AssetReturnConfirmResponse(
                asset.getId(),
                asset.getAssetName(),
                asset.getReturnStatus(),
                asset.getVerifiedBy()
        );
    }

    @Transactional(readOnly = true)
    public AssignedAssetsResponse getAssignedAssets(String email) {
        Offboarding offboarding = offboardingRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No exit request found for your account."));

        List<OffboardingAssetReturn> assets = offboardingAssetReturnRepository.findByOffboardingId(offboarding.getId());
        List<AssignedAssetsResponse.AssetItem> items = assets.stream().map(a -> new AssignedAssetsResponse.AssetItem(
                a.getId(),
                a.getAssetName(),
                a.getAssetCategory() != null ? a.getAssetCategory() : "LAPTOP",
                a.getSerialNumber(),
                a.getReturnStatus()
        )).collect(Collectors.toList());

        return new AssignedAssetsResponse(items);
    }

    @Transactional
    public ExitInterviewScheduleResponse scheduleExitInterview(String email, ExitInterviewScheduleRequest request) {
        Offboarding offboarding = offboardingRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No exit request found for your account."));

        ExitInterview interview = new ExitInterview();
        interview.setOffboarding(offboarding);
        interview.setInterviewDate(request.getPreferredDate());
        interview.setPreferredTime(request.getPreferredTime());
        interview.setComments(request.getComments());
        interview.setStatus("SCHEDULE_REQUESTED");
        interview.setInterviewerName("HR Representative"); // placeholder
        interview = exitInterviewRepository.save(interview);

        return new ExitInterviewScheduleResponse(
                interview.getId(),
                interview.getStatus(),
                null
        );
    }

    @Transactional
    public SignAgreementResponse signAgreement(String email, SignAgreementRequest request) {
        Offboarding offboarding = offboardingRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No exit request found for your account."));

        ExitAgreement agreement = new ExitAgreement();
        agreement.setOffboarding(offboarding);
        agreement.setAgreementType(request.getAgreementType());
        agreement.setAccepted(request.getAccepted());
        agreement.setSignedAt(LocalDateTime.now());
        agreement.setStatus("SIGNED");
        agreement = exitAgreementRepository.save(agreement);

        // Update checklist task "Sign NDA / Exit Agreement"
        List<OffboardingTask> tasks = offboardingTaskRepository.findByOffboardingId(offboarding.getId());
        for (OffboardingTask t : tasks) {
            if (t.getTitle().toLowerCase().contains("sign nda") || t.getTitle().toLowerCase().contains("agreement")) {
                t.setStatus("COMPLETED");
                t.setCompletedAt(LocalDateTime.now());
                offboardingTaskRepository.save(t);
            }
        }

        return new SignAgreementResponse(
                agreement.getId(),
                agreement.getAgreementType(),
                agreement.getSignedAt(),
                agreement.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public SettlementDetailsResponse getSettlementDetails(String email) {
        Offboarding offboarding = offboardingRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No exit request found for your account."));

        List<OffboardingSettlement> settlements = offboardingSettlementRepository.findByOffboardingId(offboarding.getId());
        if (settlements.isEmpty()) {
            throw new IllegalArgumentException("F&F Settlement statements not found.");
        }

        OffboardingSettlement s = settlements.get(0);
        return new SettlementDetailsResponse(
                s.getPendingSalary(),
                s.getGratuity(),
                s.getLeaveEncashment(),
                s.getReimbursements(),
                s.getDeductions(),
                s.getTotalSettlementAmount(),
                s.getPaymentStatus(),
                s.getExpectedSettlementDate()
        );
    }

    @Transactional(readOnly = true)
    public ExitTimelineResponse getExitTimeline(String email) {
        Offboarding offboarding = offboardingRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No exit request found for your account."));

        List<ExitTimelineEvent> events = exitTimelineEventRepository.findByOffboardingIdOrderByEventDateAsc(offboarding.getId());
        List<ExitTimelineResponse.TimelineEventItem> items = events.stream().map(e -> new ExitTimelineResponse.TimelineEventItem(
                e.getEventDate(),
                e.getAction(),
                e.getPerformedBy()
        )).collect(Collectors.toList());

        return new ExitTimelineResponse(items);
    }

    @Transactional
    public CancelExitResponse cancelExitRequest(String email, CancelExitRequest request) {
        Offboarding offboarding = offboardingRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No exit request found for your account."));

        // Exit can only be cancelled before approval/processing (status: PENDING_MANAGER_APPROVAL)
        if (!"PENDING_MANAGER_APPROVAL".equalsIgnoreCase(offboarding.getStatus())) {
            throw new IllegalStateException("Exit request cannot be cancelled once it is approved or under processing.");
        }

        offboarding.setStatus("CANCELLED");
        offboarding.setUpdatedAt(LocalDateTime.now());
        offboarding = offboardingRepository.save(offboarding);

        // Add cancellation to timeline
        ExitTimelineEvent cancelEvent = new ExitTimelineEvent();
        cancelEvent.setOffboarding(offboarding);
        cancelEvent.setAction("Exit Request Cancelled: " + request.getReason());
        cancelEvent.setPerformedBy(offboarding.getEmployee().getFullName());
        cancelEvent.setEventDate(LocalDateTime.now());
        exitTimelineEventRepository.save(cancelEvent);

        return new CancelExitResponse(
                offboarding.getId(),
                offboarding.getStatus(),
                "Exit request cancelled successfully"
        );
    }
}
