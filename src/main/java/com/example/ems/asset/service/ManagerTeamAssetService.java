package com.example.ems.asset.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.example.ems.asset.dto.TeamAssetDtos;
import com.example.ems.asset.entity.*;
import com.example.ems.asset.repository.*;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ManagerTeamAssetService {

    @Autowired
    private MyAssetRepository assetRepository;

    @Autowired
    private MyAssetRequestRepository requestRepository;

    @Autowired
    private MyAssetReturnRequestRepository returnRequestRepository;

    @Autowired
    private MyAssetAssignmentRepository myAssetAssignmentRepository;

    @Autowired
    private MyAssetActivityRepository activityRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public TeamAssetDtos.DashboardResponse getTeamAssetsDashboard(Employee manager) {
        List<Employee> team = employeeRepository.findByManagerId(manager.getId());
        if (team.isEmpty()) {
            return new TeamAssetDtos.DashboardResponse(true, new TeamAssetDtos.DashboardData(0, 0, BigDecimal.ZERO, "₹0.0L", 0, 0));
        }

        List<Long> teamMemberIds = team.stream().map(Employee::getId).collect(Collectors.toList());

        long teamMembersWithAssets = assetRepository.countTeamMembersWithAssets(teamMemberIds);
        long totalAssets = assetRepository.countTeamAssets(teamMemberIds);
        BigDecimal totalAssetValue = assetRepository.sumTeamAssetValue(teamMemberIds);

        long pendingReturns = returnRequestRepository.countPendingReturnsByTeamMemberIds(teamMemberIds);
        long pendingRequests = requestRepository.countPendingRequestsByTeamMemberIds(teamMemberIds);

        String totalAssetValueDisplay = formatAssetValueDisplay(totalAssetValue);

        return new TeamAssetDtos.DashboardResponse(true, new TeamAssetDtos.DashboardData(
            teamMembersWithAssets,
            totalAssets,
            totalAssetValue,
            totalAssetValueDisplay,
            pendingReturns,
            pendingRequests
        ));
    }

    @Transactional(readOnly = true)
    public Page<TeamAssetDtos.InventoryItem> getTeamAssets(Employee manager, String status, Long employeeId, String search, Pageable pageable) {
        List<Employee> team = employeeRepository.findByManagerId(manager.getId());
        if (employeeId != null) {
            team = team.stream().filter(e -> e.getId().equals(employeeId)).collect(Collectors.toList());
        }

        if (team.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> teamMemberIds = team.stream().map(Employee::getId).collect(Collectors.toList());
        Page<MyAsset> assetPage = assetRepository.findByTeamMemberIdsAndFilters(teamMemberIds, status, search, pageable);

        List<TeamAssetDtos.InventoryItem> content = assetPage.getContent().stream().map(asset -> {
            Long assignmentId = null;
            List<MyAssetAssignment> assigns = myAssetAssignmentRepository.findByAssetIdOrderByAssignedDateDesc(asset.getId());
            if (!assigns.isEmpty()) {
                assignmentId = assigns.get(0).getId();
            }

            TeamAssetDtos.EmployeeSummary empSummary = null;
            if (asset.getAssignedTo() != null) {
                empSummary = new TeamAssetDtos.EmployeeSummary(
                    asset.getAssignedTo().getId(),
                    asset.getAssignedTo().getEmployeeId(),
                    asset.getAssignedTo().getFullName(),
                    asset.getAssignedTo().getDesignation()
                );
            }

            return new TeamAssetDtos.InventoryItem(
                assignmentId,
                asset.getId(),
                asset.getAssetCode(),
                asset.getAssetName(),
                asset.getCategory(),
                asset.getSerialNumber(),
                asset.getCurrentValue() != null ? asset.getCurrentValue() : asset.getPurchasePrice(),
                asset.getStatus(),
                empSummary,
                asset.getAssignedDate()
            );
        }).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, assetPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public TeamAssetDtos.DetailResponse getTeamAssetDetails(Employee manager, Long assetId) {
        MyAsset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new IllegalArgumentException("Asset not found with ID: " + assetId));

        if (asset.getAssignedTo() == null || asset.getAssignedTo().getManager() == null || !asset.getAssignedTo().getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Access Denied: This asset is not assigned to your team.");
        }

        String notes = "";
        List<MyAssetAssignment> assigns = myAssetAssignmentRepository.findByAssetIdOrderByAssignedDateDesc(asset.getId());
        if (!assigns.isEmpty()) {
            notes = assigns.get(0).getRemarks();
        }

        TeamAssetDtos.EmployeeSummary assignedTo = new TeamAssetDtos.EmployeeSummary(
            asset.getAssignedTo().getId(),
            asset.getAssignedTo().getEmployeeId(),
            asset.getAssignedTo().getFullName(),
            asset.getAssignedTo().getDesignation()
        );

        return new TeamAssetDtos.DetailResponse(
            asset.getId(),
            asset.getAssetCode(),
            asset.getAssetName(),
            asset.getCategory(),
            asset.getAssignedTo().getDepartment(),
            asset.getSerialNumber(),
            asset.getStatus(),
            asset.getCurrentValue() != null ? asset.getCurrentValue() : asset.getPurchasePrice(),
            asset.getCondition(),
            notes,
            asset.getAssignedDate(),
            assignedTo
        );
    }

    @Transactional(readOnly = true)
    public List<TeamAssetDtos.TimelineEvent> getTeamAssetTimeline(Employee manager, Long assetId) {
        MyAsset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new IllegalArgumentException("Asset not found with ID: " + assetId));

        if (asset.getAssignedTo() == null || asset.getAssignedTo().getManager() == null || !asset.getAssignedTo().getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Access Denied: This asset is not assigned to your team.");
        }

        List<MyAssetActivity> activities = activityRepository.findByAssetIdOrderByDateDesc(assetId);
        return activities.stream()
            .map(act -> new TeamAssetDtos.TimelineEvent(
                act.getEvent(),
                act.getDate().toLocalDate(),
                act.getRemarks()
            ))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TeamAssetDtos.RequestItem> getPendingAssetRequests(Employee manager, Pageable pageable) {
        List<Employee> team = employeeRepository.findByManagerId(manager.getId());
        if (team.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> teamMemberIds = team.stream().map(Employee::getId).collect(Collectors.toList());
        Page<MyAssetRequest> requestPage = requestRepository.findByEmployeeIdsAndStatus(teamMemberIds, "PENDING_MANAGER_APPROVAL", pageable);

        List<TeamAssetDtos.RequestItem> content = requestPage.getContent().stream()
            .map(r -> new TeamAssetDtos.RequestItem(
                r.getId(),
                r.getEmployee().getId(),
                r.getEmployee().getFullName(),
                r.getEmployee().getEmployeeId(),
                r.getAssetCategory(),
                r.getRequestedModel(),
                r.getBusinessReason(),
                r.getRequestedAt().toLocalDate(),
                "PENDING"
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, requestPage.getTotalElements());
    }

    public void approveAssetRequest(Employee manager, Long requestId, String remarks) {
        MyAssetRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Asset request not found with ID: " + requestId));

        if (request.getEmployee().getManager() == null || !request.getEmployee().getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Access Denied: This request is not from your team.");
        }

        if (!"PENDING_MANAGER_APPROVAL".equals(request.getStatus())) {
            throw new IllegalStateException("Invalid request state: Request is already processed.");
        }

        request.setStatus("PENDING_IT_APPROVAL");
        request.setManagerComments(remarks != null ? remarks : "Approved by Manager");
        request.setExpectedApprovalDate(LocalDate.now().plusDays(3));
        request.setCurrentApprover("IT Administrator");
        requestRepository.save(request);
    }

    public void rejectAssetRequest(Employee manager, Long requestId, String remarks) {
        MyAssetRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Asset request not found with ID: " + requestId));

        if (request.getEmployee().getManager() == null || !request.getEmployee().getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Access Denied: This request is not from your team.");
        }

        if (!"PENDING_MANAGER_APPROVAL".equals(request.getStatus())) {
            throw new IllegalStateException("Invalid request state: Request is already processed.");
        }

        request.setStatus("REJECTED");
        request.setManagerComments(remarks != null ? remarks : "Rejected by Manager");
        request.setExpectedApprovalDate(LocalDate.now());
        request.setCurrentApprover(null);
        requestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public Page<TeamAssetDtos.ReturnRequestItem> getPendingReturnRequests(Employee manager, Pageable pageable) {
        List<Employee> team = employeeRepository.findByManagerId(manager.getId());
        if (team.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> teamMemberIds = team.stream().map(Employee::getId).collect(Collectors.toList());
        Page<MyAssetReturnRequest> returnPage = returnRequestRepository.findByEmployeeIdsAndStatus(teamMemberIds, "PENDING_IT_VERIFICATION", pageable);

        List<TeamAssetDtos.ReturnRequestItem> content = returnPage.getContent().stream()
            .map(r -> new TeamAssetDtos.ReturnRequestItem(
                r.getId(),
                r.getAsset().getId(),
                r.getAsset().getAssetCode(),
                r.getAsset().getAssetName(),
                r.getEmployee().getId(),
                r.getEmployee().getFullName(),
                r.getRequestedAt().toLocalDate(),
                r.getReturnReason(),
                "PENDING_RETURN"
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, returnPage.getTotalElements());
    }

    public void approveReturnRequest(Employee manager, Long returnRequestId, String remarks) {
        MyAssetReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Return request not found with ID: " + returnRequestId));

        if (returnRequest.getEmployee().getManager() == null || !returnRequest.getEmployee().getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Access Denied: This request is not from your team.");
        }

        if (!"PENDING_IT_VERIFICATION".equals(returnRequest.getStatus())) {
            throw new IllegalStateException("Invalid request state: Return is already processed.");
        }

        // Complete return request
        returnRequest.setStatus("COMPLETED");
        returnRequest.setComments(remarks != null ? remarks : "Return verified by Manager");
        returnRequestRepository.save(returnRequest);

        // Reset Asset assignment
        MyAsset asset = returnRequest.getAsset();
        asset.setStatus("AVAILABLE");
        asset.setAssignedTo(null);
        asset.setAssignedDate(null);
        asset.setUpdatedAt(LocalDateTime.now());
        assetRepository.save(asset);

        // Update Assignment record
        List<MyAssetAssignment> assignments = myAssetAssignmentRepository.findByAssetIdOrderByAssignedDateDesc(asset.getId());
        for (MyAssetAssignment active : assignments) {
            if ("ACTIVE".equalsIgnoreCase(active.getStatus())) {
                active.setStatus("RETURNED");
                active.setReturnedDate(LocalDate.now());
                active.setRemarks("Return approved by manager: " + remarks);
                myAssetAssignmentRepository.save(active);
            }
        }

        // Audit log event
        MyAssetActivity activity = new MyAssetActivity(
            asset,
            "RETURNED",
            manager.getFullName(),
            "Asset returned and approved by manager: " + remarks
        );
        activityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public TeamAssetDtos.AnalyticsResponse getTeamAssetsAnalytics(Employee manager) {
        List<Employee> team = employeeRepository.findByManagerId(manager.getId());
        if (team.isEmpty()) {
            return new TeamAssetDtos.AnalyticsResponse(true, new TeamAssetDtos.AnalyticsData(Collections.emptyList(), 0, 0, BigDecimal.ZERO));
        }

        List<Long> teamMemberIds = team.stream().map(Employee::getId).collect(Collectors.toList());

        List<Object[]> categoryCounts = assetRepository.countAssetsByCategory(teamMemberIds);
        List<TeamAssetDtos.CategoryCountItem> assetsByCategory = categoryCounts.stream()
            .map(row -> new TeamAssetDtos.CategoryCountItem(
                row[0] != null ? row[0].toString() : "UNKNOWN",
                ((Number) row[1]).longValue()
            ))
            .collect(Collectors.toList());

        long assignedAssets = assetRepository.countTeamAssets(teamMemberIds);
        BigDecimal totalAssetValue = assetRepository.sumTeamAssetValue(teamMemberIds);

        long pendingReturns = returnRequestRepository.countPendingReturnsByTeamMemberIds(teamMemberIds);

        return new TeamAssetDtos.AnalyticsResponse(true, new TeamAssetDtos.AnalyticsData(
            assetsByCategory,
            assignedAssets,
            pendingReturns,
            totalAssetValue
        ));
    }

    private String formatAssetValueDisplay(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return "₹0.0L";
        }
        double val = value.doubleValue();
        if (val >= 10000000.0) {
            return String.format("₹%.1fCr", val / 10000000.0);
        } else if (val >= 100000.0) {
            return String.format("₹%.1fL", val / 100000.0);
        } else if (val >= 1000.0) {
            return String.format("₹%.1fK", val / 1000.0);
        } else {
            return String.format("₹%.0f", val);
        }
    }
}
