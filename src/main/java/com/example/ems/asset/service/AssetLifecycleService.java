package com.example.ems.asset.service;

import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.asset.dto.AssetDtos.*;
import com.example.ems.asset.entity.*;
import com.example.ems.asset.repository.*;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetLifecycleService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetCategoryRepository categoryRepository;

    @Autowired
    private AssetLocationRepository locationRepository;

    @Autowired
    private AssetAssignmentRepository assignmentRepository;

    @Autowired
    private AssetActionRequestRepository actionRequestRepository;

    @Autowired
    private AssetHistoryService historyService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired(required = false)
    private ApprovalWorkflowEngineService approvalEngineService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Asset Master CRUD ---

    @Transactional
    public AssetResponse createAsset(Long organizationId, CreateAssetRequest request, String performedBy) {
        String code = request.getAssetCode().trim();
        String name = request.getAssetName().trim();

        if (assetRepository.existsByOrganizationIdAndAssetCodeIgnoreCase(organizationId, code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset code '" + code + "' already exists in this organization");
        }

        if (request.getSerialNumber() != null && !request.getSerialNumber().trim().isEmpty()) {
            String serial = request.getSerialNumber().trim();
            if (assetRepository.existsByOrganizationIdAndSerialNumberIgnoreCaseAndDeletedFalse(organizationId, serial)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Serial number '" + serial + "' already exists in this organization");
            }
        }

        AssetCategory category = categoryRepository.findByIdAndOrganizationId(request.getCategoryId(), organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset Category not found or belongs to another organization"));

        if (!category.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected Asset Category is inactive");
        }

        AssetLocation location = locationRepository.findByIdAndOrganizationId(request.getLocationId(), organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset Location not found or belongs to another organization"));

        if (!location.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected Asset Location is inactive");
        }

        Asset asset = new Asset();
        asset.setOrganizationId(organizationId);
        asset.setAssetCode(code);
        asset.setAssetName(name);
        asset.setCategory(category);
        asset.setLocation(location);
        asset.setSerialNumber(request.getSerialNumber() != null ? request.getSerialNumber().trim() : null);
        asset.setBrand(request.getBrand() != null ? request.getBrand().trim() : null);
        asset.setModel(request.getModel() != null ? request.getModel().trim() : null);
        asset.setPurchaseDate(request.getPurchaseDate());
        asset.setPurchaseCost(request.getPurchaseCost());
        asset.setCurrentValue(request.getPurchaseCost());
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setCondition(AssetCondition.GOOD);
        asset.setWarrantyStatus(request.getWarrantyStatus() != null ? request.getWarrantyStatus() : "ACTIVE");
        asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        asset.setVendor(request.getVendor() != null ? request.getVendor().trim() : null);
        asset.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        asset = assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, asset.getId(), AssetEventType.ASSET_CREATED,
                null, AssetStatus.AVAILABLE.name(),
                null, null, null, location.getId(),
                performedBy, null, "Asset created with code: " + code
        );

        return mapToResponse(asset);
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getAssets(Long organizationId, AssetStatus status) {
        List<Asset> list = status != null ?
                assetRepository.findByOrganizationIdAndStatusAndDeletedFalse(organizationId, status) :
                assetRepository.findByOrganizationIdAndDeletedFalse(organizationId);

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssetResponse getAssetById(Long organizationId, Long assetId) {
        Asset asset = assetRepository.findByIdAndOrganizationIdAndDeletedFalse(assetId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with id " + assetId));
        return mapToResponse(asset);
    }

    @Transactional
    public AssetResponse updateAsset(Long organizationId, Long assetId, EditAssetRequest request, String performedBy) {
        Asset asset = assetRepository.findByIdAndOrganizationIdWithLock(assetId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with id " + assetId));

        if (asset.getStatus() == AssetStatus.DISPOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DISPOSED assets cannot be edited");
        }

        String name = request.getAssetName().trim();
        asset.setAssetName(name);

        if (!asset.getCategory().getId().equals(request.getCategoryId())) {
            AssetCategory newCategory = categoryRepository.findByIdAndOrganizationId(request.getCategoryId(), organizationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found or belongs to another organization"));
            if (!newCategory.isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected Category is inactive");
            }
            historyService.recordHistory(organizationId, assetId, AssetEventType.CATEGORY_CHANGED,
                    asset.getStatus().name(), asset.getStatus().name(),
                    null, null, null, null, performedBy, null,
                    "Category changed from " + asset.getCategory().getCategoryName() + " to " + newCategory.getCategoryName());
            asset.setCategory(newCategory);
        }

        asset.setBrand(request.getBrand() != null ? request.getBrand().trim() : null);
        asset.setModel(request.getModel() != null ? request.getModel().trim() : null);
        asset.setPurchaseCost(request.getPurchaseCost());
        asset.setWarrantyStatus(request.getWarrantyStatus() != null ? request.getWarrantyStatus() : asset.getWarrantyStatus());
        asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        asset.setVendor(request.getVendor() != null ? request.getVendor().trim() : null);
        asset.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Asset updated = assetRepository.save(asset);

        historyService.recordHistory(organizationId, assetId, AssetEventType.ASSET_UPDATED,
                asset.getStatus().name(), asset.getStatus().name(),
                null, null, null, null, performedBy, null, "Asset master updated");

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteAsset(Long organizationId, Long assetId, String performedBy) {
        Asset asset = assetRepository.findByIdAndOrganizationIdWithLock(assetId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with id " + assetId));

        if (asset.getStatus() == AssetStatus.ASSIGNED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete an ASSIGNED asset. Return it first.");
        }
        if (asset.getStatus() == AssetStatus.IN_MAINTENANCE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete an asset IN_MAINTENANCE.");
        }
        if (asset.getStatus() == AssetStatus.RETIRED || asset.getStatus() == AssetStatus.DISPOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete RETIRED or DISPOSED assets.");
        }

        if (assignmentRepository.findByAssetIdAndStatus(assetId, AssignmentStatus.ACTIVE).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Active assignment exists for asset. Cannot delete.");
        }

        asset.setDeleted(true);
        asset.setDeletedAt(LocalDateTime.now());
        asset.setDeletedBy(performedBy);
        assetRepository.save(asset);

        historyService.recordHistory(organizationId, assetId, AssetEventType.ASSET_DELETED,
                asset.getStatus().name(), "DELETED", null, null, null, null, performedBy, null, "Asset soft deleted");
    }

    // --- Assignment Lifecycle ---

    @Transactional
    public AssetActionResultResponse assignAsset(Long organizationId, Long assetId, AssignAssetRequest request, Long requestingUserId, String performedBy) {
        Asset asset = assetRepository.findByIdAndOrganizationIdWithLock(assetId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with id " + assetId));

        validateNoPendingAction(assetId);

        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ASSET_NOT_AVAILABLE: Only AVAILABLE assets can be assigned. Current status: " + asset.getStatus());
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found with id " + request.getEmployeeId()));

        Long empOrgId = employee.getOrganization() != null ? employee.getOrganization().getId() : null;
        if (!organizationId.equals(empOrgId) || !"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EMPLOYEE_INACTIVE: Target employee is inactive or belongs to another organization.");
        }

        AssetLocation location = locationRepository.findByIdAndOrganizationId(request.getLocationId(), organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location not found or belongs to another organization"));

        if (assignmentRepository.findByAssetIdAndStatus(assetId, AssignmentStatus.ACTIVE).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ASSET_ALREADY_ASSIGNED: Active assignment already exists for asset.");
        }

        boolean requiresApproval = approvalEngineService != null;

        if (requiresApproval) {
            AssetActionRequest actionReq = createActionRequest(organizationId, assetId, AssetActionType.ASSIGN, requestingUserId, request);
            try {
                ApprovalWorkflowInstance instance = approvalEngineService.startWorkflow(
                        WorkflowType.ASSET_ASSIGNMENT_APPROVAL, "ASSET", assetId.toString(), employee, null);
                if (instance != null) {
                    actionReq.setApprovalInstanceId(instance.getId());
                    actionRequestRepository.save(actionReq);
                    return new AssetActionResultResponse(actionReq.getId(), assetId, "ASSIGN", "PENDING_APPROVAL", true, instance.getId(), "Assignment approval request submitted");
                }
            } catch (Exception e) {
                // If no active workflow definition found for ASSET_ASSIGNMENT_APPROVAL, fallback to direct execution
                requiresApproval = false;
                actionRequestRepository.delete(actionReq);
            }
        }

        // Direct execution (no approval required or fallback)
        executeAssignmentInternal(organizationId, asset, employee, location, request.getAssignedDate(), request.getExpectedReturnDate(), request.getRemarks(), performedBy);
        return new AssetActionResultResponse(null, assetId, "ASSIGN", "COMPLETED", false, null, "Asset successfully assigned");
    }

    @Transactional
    public void executeAssignmentInternal(Long organizationId, Asset asset, Employee employee, AssetLocation location,
                                          LocalDate assignedDate, LocalDate expectedReturnDate, String remarks, String performedBy) {

        AssetAssignment assignment = new AssetAssignment(organizationId, asset, employee, location, assignedDate, expectedReturnDate, remarks);
        assignmentRepository.save(assignment);

        String oldStatus = asset.getStatus().name();
        asset.setStatus(AssetStatus.ASSIGNED);
        asset.setLocation(location);
        assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, asset.getId(), AssetEventType.ASSIGNED,
                oldStatus, AssetStatus.ASSIGNED.name(),
                null, employee.getId(), null, location.getId(),
                performedBy, assignment.getId().toString(), remarks != null ? remarks : "Asset assigned"
        );
    }

    // --- Transfer Lifecycle ---

    @Transactional
    public AssetActionResultResponse transferAsset(Long organizationId, Long assetId, TransferAssetRequest request, Long requestingUserId, String performedBy) {
        Asset asset = assetRepository.findByIdAndOrganizationIdWithLock(assetId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with id " + assetId));

        validateNoPendingAction(assetId);

        if (asset.getStatus() != AssetStatus.ASSIGNED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "INVALID_TRANSFER: Only ASSIGNED assets can be transferred. Current status: " + asset.getStatus());
        }

        AssetAssignment activeAssign = assignmentRepository.findByAssetIdAndStatus(assetId, AssignmentStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ACTIVE_ASSIGNMENT_NOT_FOUND: No active assignment found to transfer"));

        Employee targetEmployee = employeeRepository.findById(request.getToEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target Employee not found with id " + request.getToEmployeeId()));

        Long targetEmpOrgId = targetEmployee.getOrganization() != null ? targetEmployee.getOrganization().getId() : null;
        if (!organizationId.equals(targetEmpOrgId) || !"ACTIVE".equalsIgnoreCase(targetEmployee.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EMPLOYEE_INACTIVE: Target employee is inactive or belongs to another organization.");
        }

        if (activeAssign.getEmployee().getId().equals(request.getToEmployeeId()) && activeAssign.getLocation().getId().equals(request.getToLocationId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_TRANSFER: Cannot transfer asset to the exact same employee and location.");
        }

        if (request.getTransferDate().isBefore(activeAssign.getAssignedDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_TRANSFER: Transfer date cannot be prior to original assigned date (" + activeAssign.getAssignedDate() + ").");
        }

        AssetLocation targetLocation = locationRepository.findByIdAndOrganizationId(request.getToLocationId(), organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target Location not found or belongs to another organization"));

        boolean requiresApproval = approvalEngineService != null;

        if (requiresApproval) {
            AssetActionRequest actionReq = createActionRequest(organizationId, assetId, AssetActionType.TRANSFER, requestingUserId, request);
            try {
                ApprovalWorkflowInstance instance = approvalEngineService.startWorkflow(
                        WorkflowType.ASSET_TRANSFER_APPROVAL, "ASSET", assetId.toString(), targetEmployee, null);
                if (instance != null) {
                    actionReq.setApprovalInstanceId(instance.getId());
                    actionRequestRepository.save(actionReq);
                    return new AssetActionResultResponse(actionReq.getId(), assetId, "TRANSFER", "PENDING_APPROVAL", true, instance.getId(), "Transfer approval request submitted");
                }
            } catch (Exception e) {
                requiresApproval = false;
                actionRequestRepository.delete(actionReq);
            }
        }

        executeTransferInternal(organizationId, asset, activeAssign, targetEmployee, targetLocation, request.getTransferDate(), request.getRemarks(), performedBy);
        return new AssetActionResultResponse(null, assetId, "TRANSFER", "COMPLETED", false, null, "Asset successfully transferred");
    }

    @Transactional
    public void executeTransferInternal(Long organizationId, Asset asset, AssetAssignment currentAssignment,
                                        Employee newEmployee, AssetLocation newLocation, LocalDate transferDate,
                                        String remarks, String performedBy) {

        Long fromEmpId = currentAssignment.getEmployee().getId();
        Long fromLocId = currentAssignment.getLocation().getId();

        currentAssignment.setStatus(AssignmentStatus.TRANSFERRED);
        currentAssignment.setReturnedDate(transferDate);
        assignmentRepository.save(currentAssignment);

        AssetAssignment newAssignment = new AssetAssignment(organizationId, asset, newEmployee, newLocation, transferDate, null, remarks);
        assignmentRepository.save(newAssignment);

        asset.setLocation(newLocation);
        assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, asset.getId(), AssetEventType.TRANSFERRED,
                AssetStatus.ASSIGNED.name(), AssetStatus.ASSIGNED.name(),
                fromEmpId, newEmployee.getId(), fromLocId, newLocation.getId(),
                performedBy, newAssignment.getId().toString(), remarks != null ? remarks : "Asset transferred"
        );
    }

    // --- Return Lifecycle ---

    @Transactional
    public AssetResponse returnAsset(Long organizationId, Long assetId, ReturnAssetRequest request, String performedBy) {
        Asset asset = assetRepository.findByIdAndOrganizationIdWithLock(assetId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with id " + assetId));

        if (asset.getStatus() != AssetStatus.ASSIGNED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "INVALID_RETURN: Only ASSIGNED assets can be returned. Current status: " + asset.getStatus());
        }

        AssetAssignment activeAssign = assignmentRepository.findByAssetIdAndStatus(assetId, AssignmentStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ACTIVE_ASSIGNMENT_NOT_FOUND: No active assignment found to return"));

        if (request.getReturnDate().isBefore(activeAssign.getAssignedDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_RETURN: Return date cannot be before assigned date (" + activeAssign.getAssignedDate() + ")");
        }

        activeAssign.setStatus(AssignmentStatus.RETURNED);
        activeAssign.setReturnedDate(request.getReturnDate());
        assignmentRepository.save(activeAssign);

        AssetStatus newStatus;
        if (request.getCondition() == AssetCondition.DAMAGED || request.getCondition() == AssetCondition.POOR) {
            newStatus = AssetStatus.IN_MAINTENANCE;
        } else {
            newStatus = AssetStatus.AVAILABLE;
        }

        String oldStatus = asset.getStatus().name();
        asset.setStatus(newStatus);
        asset.setCondition(request.getCondition());
        Asset saved = assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, assetId, AssetEventType.RETURNED,
                oldStatus, newStatus.name(),
                activeAssign.getEmployee().getId(), null, activeAssign.getLocation().getId(), null,
                performedBy, activeAssign.getId().toString(), request.getRemarks() != null ? request.getRemarks() : "Asset returned with condition " + request.getCondition()
        );

        return mapToResponse(saved);
    }

    // --- Retirement Lifecycle ---

    @Transactional
    public AssetActionResultResponse retireAsset(Long organizationId, Long assetId, RetireAssetRequest request, Long requestingUserId, String performedBy) {
        Asset asset = assetRepository.findByIdAndOrganizationIdWithLock(assetId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with id " + assetId));

        validateNoPendingAction(assetId);

        if (asset.getStatus() != AssetStatus.AVAILABLE && asset.getStatus() != AssetStatus.DAMAGED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ASSET_NOT_RETIREABLE: Only AVAILABLE or DAMAGED assets can be retired. Current status: " + asset.getStatus());
        }

        boolean requiresApproval = approvalEngineService != null;

        if (requiresApproval) {
            AssetActionRequest actionReq = createActionRequest(organizationId, assetId, AssetActionType.RETIRE, requestingUserId, request);
            try {
                ApprovalWorkflowInstance instance = approvalEngineService.startWorkflow(
                        WorkflowType.ASSET_RETIREMENT_APPROVAL, "ASSET", assetId.toString(), null, null);
                if (instance != null) {
                    actionReq.setApprovalInstanceId(instance.getId());
                    actionRequestRepository.save(actionReq);
                    return new AssetActionResultResponse(actionReq.getId(), assetId, "RETIRE", "PENDING_APPROVAL", true, instance.getId(), "Retirement approval request submitted");
                }
            } catch (Exception e) {
                requiresApproval = false;
                actionRequestRepository.delete(actionReq);
            }
        }

        executeRetireInternal(organizationId, asset, request.getReason(), performedBy);
        return new AssetActionResultResponse(null, assetId, "RETIRE", "COMPLETED", false, null, "Asset successfully retired");
    }

    @Transactional
    public void executeRetireInternal(Long organizationId, Asset asset, String reason, String performedBy) {
        String oldStatus = asset.getStatus().name();
        asset.setStatus(AssetStatus.RETIRED);
        assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, asset.getId(), AssetEventType.RETIRED,
                oldStatus, AssetStatus.RETIRED.name(),
                null, null, null, null,
                performedBy, null, reason != null ? reason : "Asset retired"
        );
    }

    // --- Disposal Lifecycle ---

    @Transactional
    public AssetActionResultResponse disposeAsset(Long organizationId, Long assetId, DisposeAssetRequest request, Long requestingUserId, String performedBy) {
        Asset asset = assetRepository.findByIdAndOrganizationIdWithLock(assetId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with id " + assetId));

        validateNoPendingAction(assetId);

        if (asset.getStatus() != AssetStatus.RETIRED && asset.getStatus() != AssetStatus.DAMAGED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ASSET_NOT_DISPOSABLE: Only RETIRED or DAMAGED assets can be disposed. Current status: " + asset.getStatus());
        }

        boolean requiresApproval = approvalEngineService != null;

        if (requiresApproval) {
            AssetActionRequest actionReq = createActionRequest(organizationId, assetId, AssetActionType.DISPOSE, requestingUserId, request);
            try {
                ApprovalWorkflowInstance instance = approvalEngineService.startWorkflow(
                        WorkflowType.ASSET_DISPOSAL_APPROVAL, "ASSET", assetId.toString(), null, null);
                if (instance != null) {
                    actionReq.setApprovalInstanceId(instance.getId());
                    actionRequestRepository.save(actionReq);
                    return new AssetActionResultResponse(actionReq.getId(), assetId, "DISPOSE", "PENDING_APPROVAL", true, instance.getId(), "Disposal approval request submitted");
                }
            } catch (Exception e) {
                requiresApproval = false;
                actionRequestRepository.delete(actionReq);
            }
        }

        executeDisposeInternal(organizationId, asset, request.getDisposalReason(), performedBy);
        return new AssetActionResultResponse(null, assetId, "DISPOSE", "COMPLETED", false, null, "Asset successfully disposed");
    }

    @Transactional
    public void executeDisposeInternal(Long organizationId, Asset asset, String reason, String performedBy) {
        String oldStatus = asset.getStatus().name();
        asset.setStatus(AssetStatus.DISPOSED);
        assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, asset.getId(), AssetEventType.DISPOSED,
                oldStatus, AssetStatus.DISPOSED.name(),
                null, null, null, null,
                performedBy, null, reason != null ? reason : "Asset disposed"
        );
    }

    // --- Helper Validation & Mapping ---

    private void validateNoPendingAction(Long assetId) {
        if (actionRequestRepository.existsByAssetIdAndStatus(assetId, AssetActionStatus.PENDING_APPROVAL)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ASSET_ACTION_PENDING: A conflicting action request is currently pending approval for this asset.");
        }
    }

    private AssetActionRequest createActionRequest(Long organizationId, Long assetId, AssetActionType type, Long requestedBy, Object payload) {
        String payloadJson = null;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ignored) {}

        AssetActionRequest request = new AssetActionRequest(organizationId, assetId, type, requestedBy, payloadJson);
        return actionRequestRepository.save(request);
    }

    private AssetResponse mapToResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getAssetCode(),
                asset.getAssetName(),
                asset.getCategory().getId(),
                asset.getCategory().getCategoryName(),
                asset.getLocation().getId(),
                asset.getLocation().getLocationName(),
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
                asset.getCreatedAt()
        );
    }
}
