package com.example.ems.asset.service;

import com.example.ems.asset.dto.*;
import com.example.ems.asset.entity.*;
import com.example.ems.asset.repository.*;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MyAssetService {

    @Autowired
    private MyAssetCategoryRepository categoryRepository;

    @Autowired
    private MyAssetPolicyRepository policyRepository;

    @Autowired
    private MyAssetRepository assetRepository;

    @Autowired
    private MyAssetMaintenanceRepository maintenanceRepository;

    @Autowired
    private MyAssetRequestRepository requestRepository;

    @Autowired
    private MyAssetIssueRepository issueRepository;

    @Autowired
    private MyAssetReturnRequestRepository returnRequestRepository;

    @Autowired
    private MyAssetActivityRepository activityRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private com.example.ems.auth.repository.UserRepository userRepository;

    @Autowired
    private MyAssetAssignmentRepository myAssetAssignmentRepository;

    @EventListener(ContextRefreshedEvent.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void seedDatabase() {
        // 1. Seed Categories if empty
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new MyAssetCategory("LAPTOP", "Laptop", 1, true));
            categoryRepository.save(new MyAssetCategory("MOBILE", "Mobile Device", 1, true));
            categoryRepository.save(new MyAssetCategory("MONITOR", "Display Monitor", 2, true));
            categoryRepository.save(new MyAssetCategory("ACCESSORIES", "Accessories", 5, true));
            categoryRepository.save(new MyAssetCategory("SERVERS", "Servers", 5, true));
            System.out.println("Seeded Asset Categories.");
        }

        // 2. Seed Policies if empty
        if (policyRepository.count() == 0) {
            policyRepository.save(new MyAssetPolicy(
                    "Company Asset Usage Policy",
                    "Employees are responsible for the physical security and maintenance of assigned assets. Any damages must be reported immediately to the IT helpdesk. Returning assets is required upon offboarding or request.",
                    false
            ));
            System.out.println("Seeded Asset Policies.");
        }

        // 3. Seed Mock Active Assets for employee if empty
        Optional<Employee> mockEmployeeOpt = employeeRepository.findAll().stream()
                .filter(e -> {
                    com.example.ems.auth.entity.User u = userRepository.findByWorkEmail(e.getEmail()).orElse(null);
                    return u != null && u.getRole() != null && "EMPLOYEE".equalsIgnoreCase(u.getRole().getName());
                })
                .findFirst();
        if (mockEmployeeOpt.isPresent()) {
            Employee emp = mockEmployeeOpt.get();
            List<MyAsset> existingAssets = assetRepository.findByAssignedToId(emp.getId());
            if (existingAssets.isEmpty() && assetRepository.findByAssetCode("SN-DL2024-421").isEmpty()) {
                // Dell XPS 15
                MyAsset asset1 = new MyAsset();
                asset1.setAssetCode("SN-DL2024-421");
                asset1.setAssetName("Dell XPS 15");
                asset1.setCategory("LAPTOP");
                asset1.setBrand("Dell");
                asset1.setModel("XPS 15 9530");
                asset1.setSerialNumber("CN-0XX123-DELL1");
                asset1.setPurchaseDate(LocalDate.now().minusMonths(6));
                asset1.setPurchasePrice(BigDecimal.valueOf(120000.00));
                asset1.setCurrentValue(BigDecimal.valueOf(115000.00));
                asset1.setAssignedTo(emp);
                asset1.setAssignedDate(LocalDate.now().minusMonths(6));
                asset1.setAssignedBy("IT Admin");
                asset1.setLocation("Headquarters");
                asset1.setCondition("EXCELLENT");
                asset1.setWarrantyStatus("ACTIVE");
                asset1.setWarrantyExpiryDate(LocalDate.now().plusYears(2));
                asset1.setStatus("ASSIGNED");
                asset1.setDepreciationPercentage(BigDecimal.valueOf(15.00));
                assetRepository.save(asset1);

                MyAssetActivity act1 = new MyAssetActivity(asset1, "ASSIGNED", "IT Admin", "Initial asset assignment during onboarding.");
                act1.setDate(LocalDateTime.now().minusMonths(6));
                activityRepository.save(act1);

                // iPhone 14 Pro
                MyAsset asset2 = new MyAsset();
                asset2.setAssetCode("SN-IP14P-902");
                asset2.setAssetName("iPhone 14 Pro");
                asset2.setCategory("MOBILE");
                asset2.setBrand("Apple");
                asset2.setModel("iPhone 14 Pro 256GB");
                asset2.setSerialNumber("APPLE-IPH14-PRO2");
                asset2.setPurchaseDate(LocalDate.now().minusMonths(12));
                asset2.setPurchasePrice(BigDecimal.valueOf(110000.00));
                asset2.setCurrentValue(BigDecimal.valueOf(80000.00));
                asset2.setAssignedTo(emp);
                asset2.setAssignedDate(LocalDate.now().minusMonths(12));
                asset2.setAssignedBy("IT Admin");
                asset2.setLocation("Headquarters");
                asset2.setCondition("GOOD");
                asset2.setWarrantyStatus("ACTIVE");
                asset2.setWarrantyExpiryDate(LocalDate.now().plusMonths(6));
                asset2.setStatus("ASSIGNED");
                asset2.setDepreciationPercentage(BigDecimal.valueOf(20.00));
                assetRepository.save(asset2);

                MyAssetActivity act2 = new MyAssetActivity(asset2, "ASSIGNED", "IT Admin", "Initial asset assignment during onboarding.");
                act2.setDate(LocalDateTime.now().minusMonths(12));
                activityRepository.save(act2);

                // LG 4K 27inch
                MyAsset asset3 = new MyAsset();
                asset3.setAssetCode("SN-LG4K-503");
                asset3.setAssetName("LG 4K 27inch");
                asset3.setCategory("MONITOR");
                asset3.setBrand("LG");
                asset3.setModel("LG UltraFine 27UL850");
                asset3.setSerialNumber("LG-27UL-MON3");
                asset3.setPurchaseDate(LocalDate.now().minusMonths(3));
                asset3.setPurchasePrice(BigDecimal.valueOf(45000.00));
                asset3.setCurrentValue(BigDecimal.valueOf(40000.00));
                asset3.setAssignedTo(emp);
                asset3.setAssignedDate(LocalDate.now().minusMonths(3));
                asset3.setAssignedBy("IT Admin");
                asset3.setLocation("Remote");
                asset3.setCondition("EXCELLENT");
                asset3.setWarrantyStatus("ACTIVE");
                asset3.setWarrantyExpiryDate(LocalDate.now().plusYears(1));
                asset3.setStatus("ASSIGNED");
                asset3.setDepreciationPercentage(BigDecimal.valueOf(10.00));
                assetRepository.save(asset3);

                MyAssetActivity act3 = new MyAssetActivity(asset3, "ASSIGNED", "IT Admin", "Initial asset assignment during remote setup.");
                act3.setDate(LocalDateTime.now().minusMonths(3));
                activityRepository.save(act3);

                // Dell Latitude 5400 (Replacement Due Laptop)
                MyAsset asset4 = new MyAsset();
                asset4.setAssetCode("AST-1001");
                asset4.setAssetName("Dell Latitude 5400");
                asset4.setCategory("LAPTOP");
                asset4.setBrand("Dell");
                asset4.setModel("Latitude 5400");
                asset4.setSerialNumber("CN-DL5400-LAT");
                asset4.setPurchaseDate(LocalDate.now().minusYears(6));
                asset4.setPurchasePrice(BigDecimal.valueOf(80000.00));
                asset4.setCurrentValue(BigDecimal.valueOf(10000.00));
                asset4.setAssignedTo(emp);
                asset4.setAssignedDate(LocalDate.now().minusYears(6));
                asset4.setAssignedBy("IT Admin");
                asset4.setLocation("Headquarters");
                asset4.setCondition("GOOD");
                asset4.setWarrantyStatus("EXPIRED");
                asset4.setWarrantyExpiryDate(LocalDate.now().minusYears(3));
                asset4.setStatus("ASSIGNED");
                asset4.setDepreciationPercentage(BigDecimal.valueOf(15.00));
                assetRepository.save(asset4);

                MyAssetActivity act4 = new MyAssetActivity(asset4, "ASSIGNED", "IT Admin", "Assigned older laptop.");
                act4.setDate(LocalDateTime.now().minusYears(6));
                activityRepository.save(act4);

                // Logitech MX Master 3S
                MyAsset asset5 = new MyAsset();
                asset5.setAssetCode("AST-1002");
                asset5.setAssetName("Logitech MX Master 3S");
                asset5.setCategory("ACCESSORIES");
                asset5.setBrand("Logitech");
                asset5.setModel("MX Master 3S");
                asset5.setSerialNumber("LOGI-MX3S-991");
                asset5.setPurchaseDate(LocalDate.now().minusYears(2));
                asset5.setPurchasePrice(BigDecimal.valueOf(15000.00));
                asset5.setCurrentValue(BigDecimal.valueOf(12000.00));
                asset5.setAssignedTo(emp);
                asset5.setAssignedDate(LocalDate.now().minusYears(2));
                asset5.setAssignedBy("IT Admin");
                asset5.setLocation("Headquarters");
                asset5.setCondition("GOOD");
                asset5.setWarrantyStatus("ACTIVE");
                asset5.setWarrantyExpiryDate(LocalDate.now().plusYears(1));
                asset5.setStatus("ASSIGNED");
                asset5.setDepreciationPercentage(BigDecimal.valueOf(10.00));
                assetRepository.save(asset5);

                MyAssetActivity act5 = new MyAssetActivity(asset5, "ASSIGNED", "IT Admin", "Assigned accessory.");
                act5.setDate(LocalDateTime.now().minusYears(2));
                activityRepository.save(act5);

                // Jabra Evolve2 65
                MyAsset asset6 = new MyAsset();
                asset6.setAssetCode("AST-1003");
                asset6.setAssetName("Jabra Evolve2 65");
                asset6.setCategory("ACCESSORIES");
                asset6.setBrand("Jabra");
                asset6.setModel("Evolve2 65");
                asset6.setSerialNumber("JABRA-EVO65-882");
                asset6.setPurchaseDate(LocalDate.now().minusYears(1));
                asset6.setPurchasePrice(BigDecimal.valueOf(22000.00));
                asset6.setCurrentValue(BigDecimal.valueOf(18000.00));
                asset6.setAssignedTo(emp);
                asset6.setAssignedDate(LocalDate.now().minusYears(1));
                asset6.setAssignedBy("IT Admin");
                asset6.setLocation("Remote");
                asset6.setCondition("GOOD");
                asset6.setWarrantyStatus("ACTIVE");
                asset6.setWarrantyExpiryDate(LocalDate.now().plusYears(2));
                asset6.setStatus("ASSIGNED");
                asset6.setDepreciationPercentage(BigDecimal.valueOf(10.00));
                assetRepository.save(asset6);

                MyAssetActivity act6 = new MyAssetActivity(asset6, "ASSIGNED", "IT Admin", "Assigned headset.");
                act6.setDate(LocalDateTime.now().minusYears(1));
                activityRepository.save(act6);

                // Server PowerEdge R740
                MyAsset asset7 = new MyAsset();
                asset7.setAssetCode("AST-1004");
                asset7.setAssetName("PowerEdge R740");
                asset7.setCategory("SERVERS");
                asset7.setBrand("Dell");
                asset7.setModel("PowerEdge R740");
                asset7.setSerialNumber("CN-SRV-R740");
                asset7.setPurchaseDate(LocalDate.now().minusYears(3));
                asset7.setPurchasePrice(BigDecimal.valueOf(500000.00));
                asset7.setCurrentValue(BigDecimal.valueOf(350000.00));
                asset7.setAssignedTo(emp);
                asset7.setAssignedDate(LocalDate.now().minusYears(3));
                asset7.setAssignedBy("IT Admin");
                asset7.setLocation("Data Center");
                asset7.setCondition("EXCELLENT");
                asset7.setWarrantyStatus("ACTIVE");
                asset7.setWarrantyExpiryDate(LocalDate.now().plusYears(2));
                asset7.setStatus("ASSIGNED");
                asset7.setDepreciationPercentage(BigDecimal.valueOf(15.00));
                assetRepository.save(asset7);

                MyAssetActivity act7 = new MyAssetActivity(asset7, "ASSIGNED", "IT Admin", "Assigned server.");
                act7.setDate(LocalDateTime.now().minusYears(3));
                activityRepository.save(act7);

                // Seed Maintenances
                MyAssetMaintenance maint1 = new MyAssetMaintenance(asset1, "Screen replacement", "Dell Service Center", BigDecimal.valueOf(5000));
                maint1.setActualCost(BigDecimal.valueOf(5000));
                maint1.setStatus("COMPLETED");
                maint1.setCompletedDate(LocalDate.now().minusMonths(2));
                maintenanceRepository.save(maint1);

                MyAssetMaintenance maint2 = new MyAssetMaintenance(asset4, "Battery replacement", "Dell Service Center", BigDecimal.valueOf(8000));
                maint2.setActualCost(BigDecimal.valueOf(8000));
                maint2.setStatus("COMPLETED");
                maint2.setCompletedDate(LocalDate.now().minusMonths(4));
                maintenanceRepository.save(maint2);

                MyAssetMaintenance maint3 = new MyAssetMaintenance(asset7, "RAM Upgrade", "Enterprise IT Solutions", BigDecimal.valueOf(35000));
                maint3.setActualCost(BigDecimal.valueOf(35000));
                maint3.setStatus("COMPLETED");
                maint3.setCompletedDate(LocalDate.now().minusMonths(1));
                maintenanceRepository.save(maint3);
                MyAssetMaintenance maint4 = new MyAssetMaintenance(asset2, "Battery health check", "Apple Service", BigDecimal.valueOf(4000));
                maint4.setStatus("UNDER_MAINTENANCE");
                maintenanceRepository.save(maint4);

                System.out.println("Seeded Rich Mock Active Assets and Maintenances for " + emp.getEmail() + ".");
            }
        }

        // Ensure all assigned assets have a corresponding active assignment record
        List<MyAsset> assignedAssets = assetRepository.findAll().stream()
                .filter(asset -> asset.getAssignedTo() != null)
                .collect(Collectors.toList());
        for (MyAsset asset : assignedAssets) {
            List<MyAssetAssignment> assigns = myAssetAssignmentRepository.findByAssetIdOrderByAssignedDateDesc(asset.getId());
            if (assigns.isEmpty()) {
                MyAssetAssignment assign = new MyAssetAssignment(
                    asset, 
                    asset.getAssignedTo(), 
                    asset.getAssignedDate() != null ? asset.getAssignedDate() : LocalDate.now(), 
                    null, 
                    "ACTIVE", 
                    "Healed assignment for seeded asset"
                );
                myAssetAssignmentRepository.save(assign);
                System.out.println("Healed missing assignment for asset: " + asset.getAssetCode());
            }
        }
    }

    @Transactional(readOnly = true)
    public MyAssetsDashboardResponse getDashboard(Employee employee) {
        List<MyAsset> assets = assetRepository.findByAssignedToId(employee.getId());

        int assignedAssetsCount = assets.size();
        int activeAssetsCount = 0;
        BigDecimal totalAssetValue = BigDecimal.ZERO;
        int upcomingReturns = 0;

        for (MyAsset asset : assets) {
            if ("ASSIGNED".equals(asset.getStatus()) || "RETURN_REQUESTED".equals(asset.getStatus())) {
                activeAssetsCount++;
                if (asset.getCurrentValue() != null) {
                    totalAssetValue = totalAssetValue.add(asset.getCurrentValue());
                }
            }
            if ("RETURN_REQUESTED".equals(asset.getStatus())) {
                upcomingReturns++;
            }
        }

        List<MyAssetIssue> issues = issueRepository.findByEmployeeIdOrderByReportedAtDesc(employee.getId());
        int openIssueTickets = (int) issues.stream()
                .filter(issue -> "OPEN".equals(issue.getStatus()) || "IN_PROGRESS".equals(issue.getStatus()))
                .count();

        MyAssetsDashboardResponse.EmployeeInfo empInfo = new MyAssetsDashboardResponse.EmployeeInfo(
                employee.getId(),
                employee.getEmployeeId(),
                employee.getFullName(),
                employee.getDepartment()
        );

        MyAssetsDashboardResponse.SummaryInfo summary = new MyAssetsDashboardResponse.SummaryInfo(
                assignedAssetsCount,
                activeAssetsCount,
                totalAssetValue,
                "INR",
                upcomingReturns,
                openIssueTickets
        );

        return new MyAssetsDashboardResponse(empInfo, summary, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Page<MyAssignedAssetItem> getAssignedAssets(Employee employee, String status, String category, String condition, Pageable pageable) {
        Page<MyAsset> assetPage = assetRepository.findByFilters(employee.getId(), status, category, condition, pageable);
        return assetPage.map(asset -> new MyAssignedAssetItem(
                asset.getId(),
                asset.getAssetCode(),
                asset.getAssetName(),
                asset.getCategory(),
                asset.getBrand(),
                asset.getModel(),
                asset.getSerialNumber(),
                asset.getPurchaseDate(),
                asset.getPurchasePrice(),
                asset.getCurrentValue(),
                asset.getAssignedDate(),
                asset.getCondition(),
                asset.getStatus()
        ));
    }

    @Transactional(readOnly = true)
    public MyAssetDetailsResponse getAssetDetails(Long assetId, Employee employee) {
        MyAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found with ID: " + assetId));

        return new MyAssetDetailsResponse(
                asset.getId(),
                asset.getAssetCode(),
                asset.getAssetName(),
                asset.getCategory(),
                asset.getBrand(),
                asset.getModel(),
                asset.getSerialNumber(),
                asset.getPurchaseDate(),
                asset.getPurchasePrice(),
                asset.getCurrentValue(),
                asset.getAssignedDate(),
                asset.getAssignedBy(),
                asset.getLocation(),
                asset.getCondition(),
                asset.getWarrantyStatus(),
                asset.getWarrantyExpiryDate(),
                asset.getStatus(),
                asset.getCreatedAt()
        );
    }

    public AssetRequestResponse requestAsset(CreateAssetRequest request, Employee employee) {
        MyAssetRequest assetRequest = new MyAssetRequest();
        assetRequest.setRequestNumber("REQ-ASSET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        assetRequest.setEmployee(employee);
        assetRequest.setAssetCategory(request.getAssetCategory());
        assetRequest.setRequestedModel(request.getRequestedModel());
        assetRequest.setBusinessReason(request.getBusinessReason());
        assetRequest.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        assetRequest.setRequiredByDate(request.getRequiredByDate());
        assetRequest.setStatus("PENDING_MANAGER_APPROVAL");
        assetRequest.setRequestedAt(LocalDateTime.now());
        assetRequest.setExpectedApprovalDate(LocalDate.now().plusDays(7));
        assetRequest.setCurrentApprover("Line Manager");

        MyAssetRequest saved = requestRepository.save(assetRequest);

        return new AssetRequestResponse(
                saved.getId(),
                saved.getRequestNumber(),
                saved.getAssetCategory(),
                saved.getRequestedModel(),
                saved.getBusinessReason(),
                saved.getPriority(),
                saved.getRequiredByDate(),
                saved.getManagerComments(),
                saved.getStatus(),
                saved.getRequestedAt(),
                saved.getExpectedApprovalDate(),
                saved.getCurrentApprover()
        );
    }

    @Transactional(readOnly = true)
    public AssetRequestsListResponse getAssetRequests(Employee employee) {
        List<MyAssetRequest> requests = requestRepository.findByEmployeeIdOrderByRequestedAtDesc(employee.getId());
        List<AssetRequestResponse> dtoList = requests.stream()
                .map(r -> new AssetRequestResponse(
                        r.getId(),
                        r.getRequestNumber(),
                        r.getAssetCategory(),
                        r.getRequestedModel(),
                        r.getBusinessReason(),
                        r.getPriority(),
                        r.getRequiredByDate(),
                        r.getManagerComments(),
                        r.getStatus(),
                        r.getRequestedAt(),
                        r.getExpectedApprovalDate(),
                        r.getCurrentApprover()
                ))
                .collect(Collectors.toList());

        return new AssetRequestsListResponse(dtoList);
    }

    public ReportIssueResponse reportIssue(Long assetId, ReportIssueRequest request, Employee employee) {
        MyAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found with ID: " + assetId));

        MyAssetIssue issue = new MyAssetIssue();
        issue.setTicketId("TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        issue.setAsset(asset);
        issue.setEmployee(employee);
        issue.setIssueType(request.getIssueType());
        issue.setSeverity(request.getSeverity() != null ? request.getSeverity() : "MEDIUM");
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setStatus("OPEN");
        issue.setReportedAt(LocalDateTime.now());
        issue.setAssignedTeam("IT Support Team");
        issue.setResolutionETA(LocalDate.now().plusDays(3));

        MyAssetIssue saved = issueRepository.save(issue);

        // Add Activity Audit
        MyAssetActivity activity = new MyAssetActivity(
                asset,
                "ISSUE_REPORTED",
                employee.getFullName(),
                "Support ticket logged: " + saved.getTicketId() + " - " + saved.getTitle()
        );
        activityRepository.save(activity);

        return new ReportIssueResponse(
                saved.getId(),
                saved.getTicketId(),
                saved.getAsset().getId(),
                saved.getAsset().getAssetName(),
                saved.getIssueType(),
                saved.getSeverity(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getStatus(),
                saved.getReportedAt(),
                saved.getAssignedTeam(),
                saved.getResolutionETA()
        );
    }

    @Transactional(readOnly = true)
    public AssetIssuesListResponse getAssetIssues(Employee employee) {
        List<MyAssetIssue> issues = issueRepository.findByEmployeeIdOrderByReportedAtDesc(employee.getId());
        List<ReportIssueResponse> dtoList = issues.stream()
                .map(i -> new ReportIssueResponse(
                        i.getId(),
                        i.getTicketId(),
                        i.getAsset().getId(),
                        i.getAsset().getAssetName(),
                        i.getIssueType(),
                        i.getSeverity(),
                        i.getTitle(),
                        i.getDescription(),
                        i.getStatus(),
                        i.getReportedAt(),
                        i.getAssignedTeam(),
                        i.getResolutionETA()
                ))
                .collect(Collectors.toList());

        return new AssetIssuesListResponse(dtoList);
    }

    public AssetReturnResponse submitReturnRequest(Long assetId, AssetReturnFormRequest request, Employee employee) {
        MyAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found with ID: " + assetId));

        if ("RETURNED".equals(asset.getStatus())) {
            throw new IllegalStateException("Asset is already returned");
        }

        Optional<MyAssetReturnRequest> existing = returnRequestRepository.findByAssetId(assetId);
        if (existing.isPresent()) {
            throw new IllegalStateException("Return request already exists for this asset");
        }

        MyAssetReturnRequest returnRequest = new MyAssetReturnRequest();
        returnRequest.setReturnReference("RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        returnRequest.setAsset(asset);
        returnRequest.setEmployee(employee);
        returnRequest.setReturnReason(request.getReturnReason());
        returnRequest.setAssetCondition(request.getAssetCondition());
        returnRequest.setAccessoriesReturned(request.getAccessoriesReturned() != null ? request.getAccessoriesReturned() : new ArrayList<>());
        returnRequest.setComments(request.getComments());
        returnRequest.setStatus("PENDING_IT_VERIFICATION");
        returnRequest.setRequestedAt(LocalDateTime.now());

        MyAssetReturnRequest saved = returnRequestRepository.save(returnRequest);

        // Update asset status
        asset.setStatus("RETURN_REQUESTED");
        assetRepository.save(asset);

        // Add Activity Audit
        MyAssetActivity activity = new MyAssetActivity(
                asset,
                "RETURN_REQUESTED",
                employee.getFullName(),
                "Return request submitted: " + saved.getReturnReference()
        );
        activityRepository.save(activity);

        return new AssetReturnResponse(
                saved.getId(),
                saved.getReturnReference(),
                saved.getAsset().getId(),
                saved.getAsset().getAssetName(),
                saved.getReturnReason(),
                saved.getAssetCondition(),
                saved.getAccessoriesReturned(),
                saved.getComments(),
                saved.getStatus(),
                saved.getRequestedAt()
        );
    }

    @Transactional(readOnly = true)
    public AssetTimelineResponse getAssetTimeline(Long assetId, Employee employee) {
        if (!assetRepository.existsById(assetId)) {
            throw new IllegalArgumentException("Asset not found with ID: " + assetId);
        }

        List<MyAssetActivity> activities = activityRepository.findByAssetIdOrderByDateDesc(assetId);
        List<AssetTimelineResponse.TimelineEventItem> events = activities.stream()
                .map(act -> new AssetTimelineResponse.TimelineEventItem(
                        act.getEvent(),
                        act.getPerformedBy(),
                        act.getDate(),
                        act.getRemarks()
                ))
                .collect(Collectors.toList());

        return new AssetTimelineResponse(events);
    }

    @Transactional(readOnly = true)
    public AssetCategoriesResponse getCategories() {
        List<MyAssetCategory> categories = categoryRepository.findAll();
        List<AssetCategoriesResponse.CategoryItem> items = categories.stream()
                .map(c -> new AssetCategoriesResponse.CategoryItem(
                        c.getId(),
                        c.getCode(),
                        c.getName(),
                        c.getMaximumAllowed(),
                        c.isRequestEnabled()
                ))
                .collect(Collectors.toList());

        return new AssetCategoriesResponse(items);
    }

    @Transactional(readOnly = true)
    public AssetPoliciesResponse getPolicies() {
        List<MyAssetPolicy> policies = policyRepository.findAll();
        List<AssetPoliciesResponse.PolicyItem> items = policies.stream()
                .map(p -> new AssetPoliciesResponse.PolicyItem(
                        p.getId(),
                        p.getTitle(),
                        p.getDescription(),
                        p.isAcknowledged()
                ))
                .collect(Collectors.toList());

        return new AssetPoliciesResponse(items);
    }
}
