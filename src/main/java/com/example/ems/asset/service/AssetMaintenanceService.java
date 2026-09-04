package com.example.ems.asset.service;

import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.asset.dto.AssetDtos.*;
import com.example.ems.asset.entity.*;
import com.example.ems.asset.repository.AssetActionRequestRepository;
import com.example.ems.asset.repository.AssetMaintenanceRepository;
import com.example.ems.asset.repository.AssetRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetMaintenanceService {

    private static final BigDecimal HIGH_COST_APPROVAL_THRESHOLD = new BigDecimal("10000.00");

    @Autowired
    private AssetMaintenanceRepository maintenanceRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetActionRequestRepository actionRequestRepository;

    @Autowired
    private AssetHistoryService historyService;

    @Autowired(required = false)
    private ApprovalWorkflowEngineService approvalEngineService;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public AssetActionResultResponse scheduleMaintenance(Long organizationId, Long assetId, CreateMaintenanceRequest request, Long requestingUserId, String performedBy) {
        Asset asset = assetRepository.findByIdAndOrganizationIdWithLock(assetId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with id " + assetId));

        if (asset.getStatus() == AssetStatus.DISPOSED || asset.getStatus() == AssetStatus.RETIRED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot schedule maintenance for RETIRED or DISPOSED asset.");
        }

        boolean highCost = request.getEstimatedCost() != null && request.getEstimatedCost().compareTo(HIGH_COST_APPROVAL_THRESHOLD) >= 0;
        boolean requiresApproval = highCost && approvalEngineService != null;

        if (requiresApproval) {
            String payloadJson = null;
            try {
                payloadJson = objectMapper.writeValueAsString(request);
            } catch (JsonProcessingException ignored) {}

            AssetActionRequest actionReq = new AssetActionRequest(organizationId, assetId, AssetActionType.MAINTENANCE, requestingUserId, payloadJson);
            actionReq = actionRequestRepository.save(actionReq);

            try {
                ApprovalWorkflowInstance instance = approvalEngineService.startWorkflow(
                        WorkflowType.ASSET_MAINTENANCE_APPROVAL, "ASSET", assetId.toString(), null, null);
                if (instance != null) {
                    actionReq.setApprovalInstanceId(instance.getId());
                    actionRequestRepository.save(actionReq);
                    return new AssetActionResultResponse(actionReq.getId(), assetId, "MAINTENANCE", "PENDING_APPROVAL", true, instance.getId(), "High cost maintenance approval request submitted");
                }
            } catch (Exception e) {
                requiresApproval = false;
                actionRequestRepository.delete(actionReq);
            }
        }

        AssetMaintenance maintenance = executeScheduleMaintenanceInternal(organizationId, asset, request, performedBy);
        return new AssetActionResultResponse(maintenance.getId(), assetId, "MAINTENANCE", "SCHEDULED", false, null, "Maintenance successfully scheduled");
    }

    @Transactional
    public AssetMaintenance executeScheduleMaintenanceInternal(Long organizationId, Asset asset, CreateMaintenanceRequest request, String performedBy) {
        AssetMaintenance maintenance = new AssetMaintenance();
        maintenance.setOrganizationId(organizationId);
        maintenance.setAsset(asset);
        maintenance.setMaintenanceType(request.getMaintenanceType());
        maintenance.setDescription(request.getDescription().trim());
        maintenance.setStatus(MaintenanceStatus.SCHEDULED);
        maintenance.setScheduledDate(request.getScheduledDate());
        maintenance.setEstimatedCost(request.getEstimatedCost() != null ? request.getEstimatedCost() : BigDecimal.ZERO);
        maintenance.setVendor(request.getVendor() != null ? request.getVendor().trim() : null);
        maintenance.setTechnician(request.getTechnician() != null ? request.getTechnician().trim() : null);
        maintenance.setRemarks(request.getRemarks() != null ? request.getRemarks().trim() : null);

        maintenance = maintenanceRepository.save(maintenance);

        historyService.recordHistory(
                organizationId, asset.getId(), AssetEventType.MAINTENANCE_CREATED,
                asset.getStatus().name(), asset.getStatus().name(),
                null, null, null, null,
                performedBy, maintenance.getId().toString(), "Scheduled " + request.getMaintenanceType() + " maintenance"
        );

        return maintenance;
    }

    @Transactional
    public AssetMaintenanceResponse startMaintenance(Long organizationId, Long maintenanceId, String performedBy) {
        AssetMaintenance maintenance = maintenanceRepository.findByIdAndOrganizationId(maintenanceId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance record not found with id " + maintenanceId));

        if (maintenance.getStatus() != MaintenanceStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "INVALID_MAINTENANCE_STATUS: Only SCHEDULED maintenance can be started. Current status: " + maintenance.getStatus());
        }

        Asset asset = assetRepository.findByIdAndOrganizationIdWithLock(maintenance.getAsset().getId(), organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));

        maintenance.setStatus(MaintenanceStatus.IN_PROGRESS);
        maintenance.setStartDate(LocalDate.now());
        maintenanceRepository.save(maintenance);

        String oldStatus = asset.getStatus().name();
        asset.setStatus(AssetStatus.IN_MAINTENANCE);
        assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, asset.getId(), AssetEventType.MAINTENANCE_STARTED,
                oldStatus, AssetStatus.IN_MAINTENANCE.name(),
                null, null, null, null,
                performedBy, maintenance.getId().toString(), "Maintenance started"
        );

        return mapToResponse(maintenance);
    }

    @Transactional
    public AssetMaintenanceResponse completeMaintenance(Long organizationId, Long maintenanceId, CompleteMaintenanceRequest request, String performedBy) {
        AssetMaintenance maintenance = maintenanceRepository.findByIdAndOrganizationId(maintenanceId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance record not found with id " + maintenanceId));

        if (maintenance.getStatus() != MaintenanceStatus.IN_PROGRESS && maintenance.getStatus() != MaintenanceStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "INVALID_MAINTENANCE_STATUS: Only IN_PROGRESS or SCHEDULED maintenance can be completed. Current status: " + maintenance.getStatus());
        }

        Asset asset = assetRepository.findByIdAndOrganizationIdWithLock(maintenance.getAsset().getId(), organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));

        if (request.getCompletedDate().isBefore(maintenance.getScheduledDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completed date cannot be before scheduled date (" + maintenance.getScheduledDate() + ")");
        }

        maintenance.setStatus(MaintenanceStatus.COMPLETED);
        maintenance.setCompletedDate(request.getCompletedDate());
        maintenance.setActualCost(request.getActualCost() != null ? request.getActualCost() : BigDecimal.ZERO);
        maintenance.setResult(request.getResult().trim());
        if (request.getRemarks() != null) {
            maintenance.setRemarks(request.getRemarks().trim());
        }
        maintenanceRepository.save(maintenance);

        String oldStatus = asset.getStatus().name();
        AssetStatus newAssetStatus;
        if ("DAMAGED".equalsIgnoreCase(request.getResult()) || "FAILED".equalsIgnoreCase(request.getResult())) {
            newAssetStatus = AssetStatus.DAMAGED;
            asset.setCondition(AssetCondition.DAMAGED);
        } else {
            newAssetStatus = AssetStatus.AVAILABLE;
            asset.setCondition(AssetCondition.GOOD);
        }

        asset.setStatus(newAssetStatus);
        assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, asset.getId(), AssetEventType.MAINTENANCE_COMPLETED,
                oldStatus, newAssetStatus.name(),
                null, null, null, null,
                performedBy, maintenance.getId().toString(), "Maintenance completed with result: " + request.getResult()
        );

        return mapToResponse(maintenance);
    }

    @Transactional
    public AssetMaintenanceResponse cancelMaintenance(Long organizationId, Long maintenanceId, String remarks, String performedBy) {
        AssetMaintenance maintenance = maintenanceRepository.findByIdAndOrganizationId(maintenanceId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance record not found with id " + maintenanceId));

        if (maintenance.getStatus() == MaintenanceStatus.COMPLETED || maintenance.getStatus() == MaintenanceStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot cancel a " + maintenance.getStatus() + " maintenance.");
        }

        Asset asset = assetRepository.findByIdAndOrganizationIdWithLock(maintenance.getAsset().getId(), organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));

        maintenance.setStatus(MaintenanceStatus.CANCELLED);
        if (remarks != null) {
            maintenance.setRemarks(remarks.trim());
        }
        maintenanceRepository.save(maintenance);

        if (asset.getStatus() == AssetStatus.IN_MAINTENANCE) {
            String oldStatus = asset.getStatus().name();
            asset.setStatus(AssetStatus.AVAILABLE);
            assetRepository.save(asset);

            historyService.recordHistory(
                    organizationId, asset.getId(), AssetEventType.MAINTENANCE_CANCELLED,
                    oldStatus, AssetStatus.AVAILABLE.name(),
                    null, null, null, null,
                    performedBy, maintenance.getId().toString(), remarks != null ? remarks : "Maintenance cancelled"
            );
        }

        return mapToResponse(maintenance);
    }

    @Transactional(readOnly = true)
    public List<AssetMaintenanceResponse> getMaintenancesByAsset(Long organizationId, Long assetId) {
        List<AssetMaintenance> list = maintenanceRepository.findByAssetIdAndOrganizationId(assetId, organizationId);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssetMaintenanceResponse> getMaintenancesByStatus(Long organizationId, MaintenanceStatus status) {
        List<AssetMaintenance> list = maintenanceRepository.findByOrganizationIdAndStatus(organizationId, status);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private AssetMaintenanceResponse mapToResponse(AssetMaintenance m) {
        return new AssetMaintenanceResponse(
                m.getId(),
                m.getAsset().getId(),
                m.getAsset().getAssetCode(),
                m.getAsset().getAssetName(),
                m.getMaintenanceType(),
                m.getDescription(),
                m.getStatus(),
                m.getScheduledDate(),
                m.getStartDate(),
                m.getCompletedDate(),
                m.getEstimatedCost(),
                m.getActualCost(),
                m.getVendor(),
                m.getTechnician(),
                m.getResult(),
                m.getRemarks()
        );
    }
}
