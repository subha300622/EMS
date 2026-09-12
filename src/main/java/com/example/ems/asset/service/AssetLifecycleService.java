package com.example.ems.asset.service;

import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.asset.dto.AssetDtos.*;
import com.example.ems.asset.entity.*;
import com.example.ems.asset.event.*;
import com.example.ems.asset.repository.*;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class AssetLifecycleService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetLocationRepository locationRepository;

    @Autowired
    private AssetAssignmentRepository assignmentRepository;

    @Autowired
    private AssetTransferRepository transferRepository;

    @Autowired
    private AssetActionRequestRepository actionRequestRepository;

    @Autowired
    private AssetStateMachineService stateMachineService;

    @Autowired
    private AssetHistoryService historyService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired(required = false)
    private ApprovalWorkflowEngineService approvalEngineService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetService assetService;

    @Transactional
    public AssetResponse createAsset(Long organizationId, CreateAssetRequest request, String performedBy) {
        return assetService.createAsset(organizationId, request, performedBy);
    }

    @Transactional(readOnly = true)
    public AssetResponse getAssetById(Long organizationId, Long assetId) {
        return assetService.getAssetById(organizationId, assetId);
    }

    @Transactional
    public AssetResponse updateAsset(Long organizationId, Long assetId, EditAssetRequest request, String performedBy) {
        return assetService.updateAsset(organizationId, assetId, request, performedBy);
    }

    @Transactional
    public void deleteAsset(Long organizationId, Long assetId, String performedBy) {
        assetService.deleteAsset(organizationId, assetId, performedBy);
    }

    // --- State Machine Transitions ---

    @Transactional
    public AssetResponse activateAsset(Long organizationId, Long assetId, String performedBy) {
        Asset asset = getLockedAsset(organizationId, assetId);
        AssetStatus next = stateMachineService.getNextStatus(asset.getStatus(), AssetTransition.ACTIVATE);
        String oldStatus = asset.getStatus().name();
        asset.setStatus(next);
        asset = assetRepository.save(asset);

        historyService.recordHistory(organizationId, assetId, AssetEventType.ASSET_UPDATED, oldStatus, next.name(),
                null, null, null, null, performedBy, null, "Asset activated");
        return mapToResponse(asset);
    }

    @Transactional
    public AssetResponse makeAvailableAsset(Long organizationId, Long assetId, String performedBy) {
        Asset asset = getLockedAsset(organizationId, assetId);
        AssetStatus next = stateMachineService.getNextStatus(asset.getStatus(), AssetTransition.MAKE_AVAILABLE);
        String oldStatus = asset.getStatus().name();
        asset.setStatus(next);
        asset = assetRepository.save(asset);

        historyService.recordHistory(organizationId, assetId, AssetEventType.ASSET_UPDATED, oldStatus, next.name(),
                null, null, null, null, performedBy, null, "Asset restocked/inspected and marked AVAILABLE");
        return mapToResponse(asset);
    }

    @Transactional
    public AssetActionResultResponse assignAsset(Long organizationId, Long assetId, AssignAssetRequest request,
            Long requestingUserId, String performedBy) {
        Asset asset = getLockedAsset(organizationId, assetId);
        validateNoPendingAction(assetId);

        if (!stateMachineService.canTransition(asset.getStatus(), AssetTransition.ASSIGN)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "ASSET_NOT_AVAILABLE: Cannot assign asset in status " + asset.getStatus());
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Employee not found with id " + request.getEmployeeId()));

        Long empOrgId = employee.getOrganization() != null ? employee.getOrganization().getId() : null;
        if (!organizationId.equals(empOrgId) || !"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "EMPLOYEE_INACTIVE: Target employee is inactive or belongs to another organization.");
        }

        AssetLocation location = locationRepository.findByIdAndOrganizationId(request.getLocationId(), organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Location not found or belongs to another organization"));

        if (assignmentRepository.findByAssetIdAndStatus(assetId, AssignmentStatus.ACTIVE).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "ASSET_ALREADY_ASSIGNED: Active assignment already exists for asset.");
        }

        boolean requiresApproval = approvalEngineService != null;
        if (requiresApproval) {
            AssetActionRequest actionReq = createActionRequest(organizationId, assetId, AssetActionType.ASSIGN,
                    requestingUserId, request);
            try {
                ApprovalWorkflowInstance instance = approvalEngineService.startWorkflow(
                        WorkflowType.ASSET_ASSIGNMENT_APPROVAL, "ASSET", assetId.toString(), employee, null);
                if (instance != null) {
                    actionReq.setApprovalInstanceId(instance.getId());
                    actionRequestRepository.save(actionReq);
                    return new AssetActionResultResponse(actionReq.getId(), assetId, "ASSIGN", "PENDING_APPROVAL", true,
                            instance.getId(), "Assignment approval request submitted");
                }
            } catch (Exception e) {
                requiresApproval = false;
                actionRequestRepository.delete(actionReq);
            }
        }

        executeAssignmentInternal(organizationId, asset, employee, location, request.getAssignedDate(),
                request.getExpectedReturnDate(), request.getRemarks(), performedBy);
        return new AssetActionResultResponse(null, assetId, "ASSIGN", "COMPLETED", false, null,
                "Asset successfully assigned");
    }

    @Transactional
    public void executeAssignmentInternal(Long organizationId, Asset asset, Employee employee, AssetLocation location,
            LocalDate assignedDate, LocalDate expectedReturnDate, String remarks, String performedBy) {
        AssetStatus next = stateMachineService.getNextStatus(asset.getStatus(), AssetTransition.ASSIGN);
        AssetAssignment assignment = new AssetAssignment(organizationId, asset, employee, location, assignedDate,
                expectedReturnDate, remarks);
        assignment = assignmentRepository.save(assignment);

        String oldStatus = asset.getStatus().name();
        asset.setStatus(next);
        asset.setLocation(location);
        assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, asset.getId(), AssetEventType.ASSIGNED,
                oldStatus, next.name(),
                null, employee.getId(), null, location.getId(),
                performedBy, assignment.getId().toString(), remarks != null ? remarks : "Asset assigned");

        eventPublisher.publishEvent(
                new AssetAssignedEvent(asset.getId(), organizationId, employee.getId(), "EMPLOYEE", performedBy));
    }

    @Transactional
    public AssetResponse returnAsset(Long organizationId, Long assetId, ReturnAssetRequest request,
            String performedBy) {
        Asset asset = getLockedAsset(organizationId, assetId);
        AssetStatus next = stateMachineService.getNextStatus(asset.getStatus(), AssetTransition.RETURN);

        AssetAssignment activeAssign = assignmentRepository.findByAssetIdAndStatus(assetId, AssignmentStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "ACTIVE_ASSIGNMENT_NOT_FOUND: No active assignment found to return"));

        if (request.getReturnDate().isBefore(activeAssign.getAssignedDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "INVALID_RETURN: Return date cannot be before assigned date (" + activeAssign.getAssignedDate()
                            + ")");
        }

        activeAssign.setStatus(AssignmentStatus.RETURNED);
        activeAssign.setReturnedDate(request.getReturnDate());
        assignmentRepository.save(activeAssign);

        String oldStatus = asset.getStatus().name();
        if (request.getCondition() == AssetCondition.DAMAGED || request.getCondition() == AssetCondition.POOR) {
            next = AssetStatus.IN_MAINTENANCE;
        } else {
            next = AssetStatus.AVAILABLE;
        }
        asset.setStatus(next);
        if (request.getCondition() != null) asset.setCondition(request.getCondition());
        Asset saved = assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, assetId, AssetEventType.RETURNED,
                oldStatus, next.name(),
                activeAssign.getEmployee().getId(), null, activeAssign.getLocation().getId(), null,
                performedBy, activeAssign.getId().toString(), request.getRemarks() != null ? request.getRemarks()
                        : "Asset returned with condition " + request.getCondition());

        eventPublisher.publishEvent(new AssetReturnedEvent(assetId, organizationId, activeAssign.getEmployee().getId(),
                request.getCondition() != null ? request.getCondition().name() : "GOOD", request.getRemarks(),
                performedBy));
        return mapToResponse(saved);
    }

    @Transactional
    public AssetActionResultResponse transferAsset(Long organizationId, Long assetId, TransferAssetRequest request,
            Long requestingUserId, String performedBy) {
        Asset asset = getLockedAsset(organizationId, assetId);
        validateNoPendingAction(assetId);

        if (!stateMachineService.canTransition(asset.getStatus(), AssetTransition.TRANSFER)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "INVALID_TRANSFER: Only ASSIGNED assets can be transferred. Current status: " + asset.getStatus());
        }

        AssetAssignment activeAssign = assignmentRepository.findByAssetIdAndStatus(assetId, AssignmentStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "ACTIVE_ASSIGNMENT_NOT_FOUND: No active assignment found to transfer"));

        Employee targetEmployee = employeeRepository.findById(request.getToEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Target Employee not found with id " + request.getToEmployeeId()));

        Long targetEmpOrgId = targetEmployee.getOrganization() != null ? targetEmployee.getOrganization().getId()
                : null;
        if (!organizationId.equals(targetEmpOrgId) || !"ACTIVE".equalsIgnoreCase(targetEmployee.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "EMPLOYEE_INACTIVE: Target employee is inactive or belongs to another organization.");
        }

        if (activeAssign.getEmployee().getId().equals(request.getToEmployeeId())
                && activeAssign.getLocation().getId().equals(request.getToLocationId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "INVALID_TRANSFER: Cannot transfer asset to the exact same employee and location.");
        }

        AssetLocation targetLocation = locationRepository
                .findByIdAndOrganizationId(request.getToLocationId(), organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Target Location not found or belongs to another organization"));

        boolean requiresApproval = approvalEngineService != null;
        if (requiresApproval) {
            AssetActionRequest actionReq = createActionRequest(organizationId, assetId, AssetActionType.TRANSFER,
                    requestingUserId, request);
            try {
                ApprovalWorkflowInstance instance = approvalEngineService.startWorkflow(
                        WorkflowType.ASSET_TRANSFER_APPROVAL, "ASSET", assetId.toString(), targetEmployee, null);
                if (instance != null) {
                    actionReq.setApprovalInstanceId(instance.getId());
                    actionRequestRepository.save(actionReq);
                    return new AssetActionResultResponse(actionReq.getId(), assetId, "TRANSFER", "PENDING_APPROVAL",
                            true, instance.getId(), "Transfer approval request submitted");
                }
            } catch (Exception e) {
                requiresApproval = false;
                actionRequestRepository.delete(actionReq);
            }
        }

        executeTransferInternal(organizationId, asset, activeAssign, targetEmployee, targetLocation,
                request.getTransferDate(), request.getRemarks(), performedBy);
        return new AssetActionResultResponse(null, assetId, "TRANSFER", "COMPLETED", false, null,
                "Asset successfully transferred");
    }

    @Transactional
    public void executeTransferInternal(Long organizationId, Asset asset, AssetAssignment currentAssignment,
            Employee newEmployee, AssetLocation newLocation, LocalDate transferDate,
            String remarks, String performedBy) {
        Long fromEmpId = currentAssignment.getEmployee().getId();
        Long fromLocId = currentAssignment.getLocation().getId();

        currentAssignment.setStatus(AssignmentStatus.TRANSFERRED);
        currentAssignment.setReturnedDate(transferDate);
        assignmentRepository.saveAndFlush(currentAssignment);

        AssetTransfer transfer = new AssetTransfer(organizationId, asset, fromEmpId, newEmployee.getId(), fromLocId,
                newLocation.getId(), remarks, performedBy);
        transferRepository.save(transfer);

        AssetAssignment newAssignment = new AssetAssignment(organizationId, asset, newEmployee, newLocation,
                transferDate, null, remarks);
        newAssignment = assignmentRepository.save(newAssignment);

        asset.setLocation(newLocation);
        assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, asset.getId(), AssetEventType.TRANSFERRED,
                AssetStatus.ASSIGNED.name(), AssetStatus.ASSIGNED.name(),
                fromEmpId, newEmployee.getId(), fromLocId, newLocation.getId(),
                performedBy, newAssignment.getId().toString(), remarks != null ? remarks : "Asset transferred");

        eventPublisher.publishEvent(new AssetTransferredEvent(asset.getId(), organizationId, fromEmpId,
                newEmployee.getId(), fromLocId, newLocation.getId(), remarks, performedBy));
    }

    @Transactional
    public AssetResponse markLostAsset(Long organizationId, Long assetId, String remarks, String performedBy) {
        Asset asset = getLockedAsset(organizationId, assetId);
        AssetStatus next = stateMachineService.getNextStatus(asset.getStatus(), AssetTransition.MARK_LOST);
        String oldStatus = asset.getStatus().name();
        asset.setStatus(next);
        asset = assetRepository.save(asset);

        historyService.recordHistory(organizationId, assetId, AssetEventType.ASSET_UPDATED, oldStatus, next.name(),
                null, null, null, null, performedBy, null, remarks != null ? remarks : "Asset marked LOST");
        return mapToResponse(asset);
    }

    @Transactional
    public AssetResponse markDamagedAsset(Long organizationId, Long assetId, String remarks, String performedBy) {
        Asset asset = getLockedAsset(organizationId, assetId);
        AssetStatus next = stateMachineService.getNextStatus(asset.getStatus(), AssetTransition.MARK_DAMAGED);
        String oldStatus = asset.getStatus().name();
        asset.setStatus(next);
        asset.setCondition(AssetCondition.DAMAGED);
        asset = assetRepository.save(asset);

        historyService.recordHistory(organizationId, assetId, AssetEventType.ASSET_UPDATED, oldStatus, next.name(),
                null, null, null, null, performedBy, null, remarks != null ? remarks : "Asset marked DAMAGED");
        return mapToResponse(asset);
    }

    @Transactional
    public AssetResponse startRepairAsset(Long organizationId, Long assetId, String remarks, String performedBy) {
        Asset asset = getLockedAsset(organizationId, assetId);
        AssetStatus next = stateMachineService.getNextStatus(asset.getStatus(), AssetTransition.START_REPAIR);
        String oldStatus = asset.getStatus().name();
        asset.setStatus(next);
        asset = assetRepository.save(asset);

        historyService.recordHistory(organizationId, assetId, AssetEventType.ASSET_UPDATED, oldStatus, next.name(),
                null, null, null, null, performedBy, null, remarks != null ? remarks : "Asset repair initiated");
        return mapToResponse(asset);
    }

    @Transactional
    public AssetResponse startMaintenanceAsset(Long organizationId, Long assetId, String remarks, String performedBy) {
        Asset asset = getLockedAsset(organizationId, assetId);
        AssetStatus next = stateMachineService.getNextStatus(asset.getStatus(), AssetTransition.START_MAINTENANCE);
        String oldStatus = asset.getStatus().name();
        asset.setStatus(next);
        asset = assetRepository.save(asset);

        historyService.recordHistory(organizationId, assetId, AssetEventType.ASSET_UPDATED, oldStatus, next.name(),
                null, null, null, null, performedBy, null, remarks != null ? remarks : "Asset maintenance initiated");
        return mapToResponse(asset);
    }

    @Transactional
    public AssetResponse restoreAsset(Long organizationId, Long assetId, String remarks, String performedBy) {
        Asset asset = getLockedAsset(organizationId, assetId);
        AssetStatus next = stateMachineService.getNextStatus(asset.getStatus(), AssetTransition.RESTORE);
        String oldStatus = asset.getStatus().name();
        asset.setStatus(next);
        asset = assetRepository.save(asset);

        historyService.recordHistory(organizationId, assetId, AssetEventType.ASSET_UPDATED, oldStatus, next.name(),
                null, null, null, null, performedBy, null, remarks != null ? remarks : "Asset restored to AVAILABLE");
        return mapToResponse(asset);
    }

    @Transactional
    public AssetActionResultResponse retireAsset(Long organizationId, Long assetId, RetireAssetRequest request,
            Long requestingUserId, String performedBy) {
        Asset asset = getLockedAsset(organizationId, assetId);
        validateNoPendingAction(assetId);

        if (!stateMachineService.canTransition(asset.getStatus(), AssetTransition.RETIRE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "ASSET_NOT_RETIREABLE: Cannot retire asset in status " + asset.getStatus());
        }

        executeRetireInternal(organizationId, asset, request.getReason(), performedBy);
        return new AssetActionResultResponse(null, assetId, "RETIRE", "COMPLETED", false, null,
                "Asset successfully retired");
    }

    @Transactional
    public void executeRetireInternal(Long organizationId, Asset asset, String reason, String performedBy) {
        AssetStatus next = stateMachineService.getNextStatus(asset.getStatus(), AssetTransition.RETIRE);
        String oldStatus = asset.getStatus().name();
        asset.setStatus(next);
        assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, asset.getId(), AssetEventType.RETIRED,
                oldStatus, next.name(), null, null, null, null,
                performedBy, null, reason != null ? reason : "Asset retired");
    }

    @Transactional
    public AssetActionResultResponse disposeAsset(Long organizationId, Long assetId, DisposeAssetRequest request,
            Long requestingUserId, String performedBy) {
        Asset asset = getLockedAsset(organizationId, assetId);
        validateNoPendingAction(assetId);

        if (!stateMachineService.canTransition(asset.getStatus(), AssetTransition.DISPOSE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "ASSET_NOT_DISPOSABLE: Cannot dispose asset in status " + asset.getStatus());
        }

        executeDisposeInternal(organizationId, asset, request.getDisposalReason(), request.getDisposalCost(),
                performedBy);
        return new AssetActionResultResponse(null, assetId, "DISPOSE", "COMPLETED", false, null,
                "Asset successfully disposed");
    }

    @Transactional
    public void executeDisposeInternal(Long organizationId, Asset asset, String reason, BigDecimal disposalCost,
            String performedBy) {
        AssetStatus next = stateMachineService.getNextStatus(asset.getStatus(), AssetTransition.DISPOSE);
        String oldStatus = asset.getStatus().name();
        asset.setStatus(next);
        assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, asset.getId(), AssetEventType.DISPOSED,
                oldStatus, next.name(), null, null, null, null,
                performedBy, null, reason != null ? reason : "Asset disposed");

        eventPublisher.publishEvent(new AssetDisposedEvent(asset.getId(), organizationId, "SOLD_DISPOSED", disposalCost,
                reason, performedBy));
    }

    private Asset getLockedAsset(Long organizationId, Long assetId) {
        return assetRepository.findByIdAndOrganizationIdWithLock(assetId, organizationId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with id " + assetId));
    }

    private void validateNoPendingAction(Long assetId) {
        if (actionRequestRepository.existsByAssetIdAndStatus(assetId, AssetActionStatus.PENDING_APPROVAL)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "ASSET_ACTION_PENDING: A conflicting action request is currently pending approval for this asset.");
        }
    }

    private AssetActionRequest createActionRequest(Long organizationId, Long assetId, AssetActionType type,
            Long requestedBy, Object payload) {
        String payloadJson = null;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ignored) {
        }

        AssetActionRequest request = new AssetActionRequest(organizationId, assetId, type, requestedBy, payloadJson);
        return actionRequestRepository.save(request);
    }

    private AssetResponse mapToResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getAssetCode(),
                asset.getAssetName(),
                asset.getCategory() != null ? asset.getCategory().getId() : null,
                asset.getCategory() != null ? asset.getCategory().getCategoryName() : null,
                asset.getLocation() != null ? asset.getLocation().getId() : null,
                asset.getLocation() != null ? asset.getLocation().getLocationName() : null,
                asset.getSerialNumber(),
                asset.getBrand(),
                asset.getModel(),
                asset.getPurchaseDate(),
                asset.getPurchaseCost(),
                asset.getCurrentValue(),
                asset.getStatus(),
                asset.getCondition(),
                asset.getWarrantyStatus(),
                asset.getWarrantyExpiryDate(),
                asset.getVendor(),
                asset.getDescription(),
                asset.isDeleted(),
                asset.getVersion(),
                asset.getCreatedAt());
    }
}
