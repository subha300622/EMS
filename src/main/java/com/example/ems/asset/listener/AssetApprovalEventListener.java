package com.example.ems.asset.listener;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalChangesRequestedEvent;
import com.example.ems.approval.event.ApprovalWorkflowCancelledEvent;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.event.ApprovalWorkflowRejectedEvent;
import com.example.ems.asset.dto.AssetDtos.*;
import com.example.ems.asset.entity.*;
import com.example.ems.asset.repository.AssetActionRequestRepository;
import com.example.ems.asset.repository.AssetAssignmentRepository;
import com.example.ems.asset.repository.AssetLocationRepository;
import com.example.ems.asset.repository.AssetRepository;
import com.example.ems.asset.service.AssetLifecycleService;
import com.example.ems.asset.service.AssetMaintenanceService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class AssetApprovalEventListener {

    private static final Set<WorkflowType> ASSET_WORKFLOW_TYPES = Set.of(
            WorkflowType.ASSET_ASSIGNMENT_APPROVAL,
            WorkflowType.ASSET_TRANSFER_APPROVAL,
            WorkflowType.ASSET_RETIREMENT_APPROVAL,
            WorkflowType.ASSET_DISPOSAL_APPROVAL,
            WorkflowType.ASSET_MAINTENANCE_APPROVAL
    );

    @Autowired
    private AssetActionRequestRepository actionRequestRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetLocationRepository locationRepository;

    @Autowired
    private AssetAssignmentRepository assignmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AssetLifecycleService lifecycleService;

    @Autowired
    private AssetMaintenanceService maintenanceService;

    @Autowired
    private ObjectMapper objectMapper;

    @EventListener
    @Transactional
    public void handleWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (!ASSET_WORKFLOW_TYPES.contains(event.getWorkflowType())) return;

        AssetActionRequest actionRequest = findPendingActionRequest(event.getWorkflowInstanceId(), event.getBusinessReferenceId());
        if (actionRequest == null || actionRequest.getStatus() != AssetActionStatus.PENDING_APPROVAL) {
            // Idempotent skip if already processed
            return;
        }

        if (event.getStatus() == ApprovalStatus.APPROVED) {
            actionRequest.setStatus(AssetActionStatus.APPROVED);
            actionRequestRepository.save(actionRequest);

            executeApprovedAction(actionRequest);
        } else if (event.getStatus() == ApprovalStatus.REJECTED) {
            actionRequest.setStatus(AssetActionStatus.REJECTED);
            actionRequestRepository.save(actionRequest);
        }
    }

    @EventListener
    @Transactional
    public void handleWorkflowRejected(ApprovalWorkflowRejectedEvent event) {
        if (!ASSET_WORKFLOW_TYPES.contains(event.getWorkflowType())) return;

        AssetActionRequest actionRequest = findPendingActionRequest(event.getWorkflowInstanceId(), event.getBusinessReferenceId());
        if (actionRequest == null || actionRequest.getStatus() != AssetActionStatus.PENDING_APPROVAL) {
            return;
        }

        actionRequest.setStatus(AssetActionStatus.REJECTED);
        actionRequestRepository.save(actionRequest);
    }

    @EventListener
    @Transactional
    public void handleWorkflowCancelled(ApprovalWorkflowCancelledEvent event) {
        if (!ASSET_WORKFLOW_TYPES.contains(event.getWorkflowType())) return;

        AssetActionRequest actionRequest = findPendingActionRequest(event.getWorkflowInstanceId(), event.getBusinessReferenceId());
        if (actionRequest == null || actionRequest.getStatus() != AssetActionStatus.PENDING_APPROVAL) {
            return;
        }

        actionRequest.setStatus(AssetActionStatus.CANCELLED);
        actionRequestRepository.save(actionRequest);
    }

    @EventListener
    @Transactional
    public void handleChangesRequested(ApprovalChangesRequestedEvent event) {
        if (!ASSET_WORKFLOW_TYPES.contains(event.getWorkflowType())) return;
        // Asset action requests don't change state on changes requested, stay PENDING_APPROVAL
    }

    private AssetActionRequest findPendingActionRequest(String workflowInstanceId, String businessRefId) {
        if (workflowInstanceId != null) {
            try {
                Long instanceId = Long.parseLong(workflowInstanceId);
                var opt = actionRequestRepository.findByApprovalInstanceId(instanceId);
                if (opt.isPresent()) return opt.get();
            } catch (NumberFormatException ignored) {}
        }
        if (businessRefId != null) {
            try {
                Long assetId = Long.parseLong(businessRefId);
                var opt = actionRequestRepository.findByAssetIdAndStatus(assetId, AssetActionStatus.PENDING_APPROVAL);
                if (opt.isPresent()) return opt.get();
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private void executeApprovedAction(AssetActionRequest actionRequest) {
        Long orgId = actionRequest.getOrganizationId();
        Long assetId = actionRequest.getAssetId();
        String performedBy = "Approval Engine (Req #" + actionRequest.getId() + ")";

        Asset asset = assetRepository.findByIdAndOrganizationIdWithLock(assetId, orgId).orElse(null);
        if (asset == null) return;

        try {
            switch (actionRequest.getRequestType()) {
                case ASSIGN -> {
                    AssignAssetRequest req = objectMapper.readValue(actionRequest.getPayloadJson(), AssignAssetRequest.class);
                    Employee emp = employeeRepository.findById(req.getEmployeeId()).orElse(null);
                    AssetLocation loc = locationRepository.findByIdAndOrganizationId(req.getLocationId(), orgId).orElse(null);
                    if (emp != null && loc != null && asset.getStatus() == AssetStatus.AVAILABLE) {
                        lifecycleService.executeAssignmentInternal(orgId, asset, emp, loc, req.getAssignedDate(), req.getExpectedReturnDate(), req.getRemarks(), performedBy);
                        actionRequest.setStatus(AssetActionStatus.COMPLETED);
                    }
                }
                case TRANSFER -> {
                    TransferAssetRequest req = objectMapper.readValue(actionRequest.getPayloadJson(), TransferAssetRequest.class);
                    AssetAssignment activeAssign = assignmentRepository.findByAssetIdAndStatus(assetId, AssignmentStatus.ACTIVE).orElse(null);
                    Employee targetEmp = employeeRepository.findById(req.getToEmployeeId()).orElse(null);
                    AssetLocation targetLoc = locationRepository.findByIdAndOrganizationId(req.getToLocationId(), orgId).orElse(null);
                    if (activeAssign != null && targetEmp != null && targetLoc != null && asset.getStatus() == AssetStatus.ASSIGNED) {
                        lifecycleService.executeTransferInternal(orgId, asset, activeAssign, targetEmp, targetLoc, req.getTransferDate(), req.getRemarks(), performedBy);
                        actionRequest.setStatus(AssetActionStatus.COMPLETED);
                    }
                }
                case RETIRE -> {
                    RetireAssetRequest req = objectMapper.readValue(actionRequest.getPayloadJson(), RetireAssetRequest.class);
                    if (asset.getStatus() == AssetStatus.AVAILABLE || asset.getStatus() == AssetStatus.DAMAGED) {
                        lifecycleService.executeRetireInternal(orgId, asset, req.getReason(), performedBy);
                        actionRequest.setStatus(AssetActionStatus.COMPLETED);
                    }
                }
                case DISPOSE -> {
                    DisposeAssetRequest req = objectMapper.readValue(actionRequest.getPayloadJson(), DisposeAssetRequest.class);
                    if (asset.getStatus() == AssetStatus.RETIRED || asset.getStatus() == AssetStatus.DAMAGED) {
                        lifecycleService.executeDisposeInternal(orgId, asset, req.getDisposalReason(), performedBy);
                        actionRequest.setStatus(AssetActionStatus.COMPLETED);
                    }
                }
                case MAINTENANCE -> {
                    CreateMaintenanceRequest req = objectMapper.readValue(actionRequest.getPayloadJson(), CreateMaintenanceRequest.class);
                    if (asset.getStatus() != AssetStatus.DISPOSED && asset.getStatus() != AssetStatus.RETIRED) {
                        maintenanceService.executeScheduleMaintenanceInternal(orgId, asset, req, performedBy);
                        actionRequest.setStatus(AssetActionStatus.COMPLETED);
                    }
                }
            }
            actionRequestRepository.save(actionRequest);
        } catch (Exception e) {
            // Log & leave for manual retry/admin investigation
        }
    }
}
