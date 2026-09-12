package com.example.ems.asset.service;

import com.example.ems.asset.entity.AssetAssignment;
import com.example.ems.asset.entity.AssetTransfer;
import com.example.ems.asset.repository.AssetAssignmentRepository;
import com.example.ems.asset.repository.AssetTransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AssetAssignmentService {

    @Autowired
    private AssetAssignmentRepository assignmentRepository;

    @Autowired
    private AssetTransferRepository transferRepository;

    @Transactional(readOnly = true)
    public List<AssetAssignment> getAssignments(Long organizationId) {
        return assignmentRepository.findByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public AssetAssignment getAssignmentById(Long organizationId, Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .filter(a -> organizationId.equals(a.getOrganizationId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found with ID: " + assignmentId));
    }

    @Transactional(readOnly = true)
    public List<AssetAssignment> getAssetAssignments(Long organizationId, Long assetId) {
        return assignmentRepository.findByAssetIdOrderByAssignedDateDesc(assetId);
    }

    @Transactional(readOnly = true)
    public List<AssetTransfer> getAssetTransfers(Long organizationId, Long assetId) {
        return transferRepository.findByOrganizationIdAndAssetIdOrderByTransferredAtDesc(organizationId, assetId);
    }
}
