package com.example.ems.appraisal.service;

import com.example.ems.appraisal.dto.AppraisalRequestReasonDto;
import com.example.ems.appraisal.dto.CreateRequestReasonDto;
import com.example.ems.appraisal.dto.UpdateRequestReasonDto;
import com.example.ems.appraisal.entity.AppraisalRequestReason;
import com.example.ems.appraisal.repository.AppraisalRequestReasonRepository;
import com.example.ems.appraisal.repository.AppraisalRequestRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppraisalRequestReasonService {

    @Autowired
    private AppraisalRequestReasonRepository reasonRepository;

    @Autowired
    private AppraisalRequestRepository requestRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    public List<AppraisalRequestReasonDto> getReasons(boolean activeOnly) {
        Long orgId = TenantContext.requireOrganizationId();
        List<AppraisalRequestReason> list = activeOnly
                ? reasonRepository.findByOrganizationIdAndActiveTrue(orgId)
                : reasonRepository.findByOrganizationId(orgId);
        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public AppraisalRequestReasonDto createReason(CreateRequestReasonDto dto) {
        Long orgId = TenantContext.requireOrganizationId();
        String code = dto.getCode().trim().toUpperCase();

        if (reasonRepository.existsByOrganizationIdAndCodeIgnoreCase(orgId, code)) {
            throw new IllegalArgumentException("Appraisal request reason with code '" + code + "' already exists for this organization");
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization not found: " + orgId));

        AppraisalRequestReason reason = new AppraisalRequestReason();
        reason.setOrganization(org);
        reason.setCode(code);
        reason.setName(dto.getName().trim());
        reason.setDescription(dto.getDescription());
        reason.setActive(true);
        reason.setCreatedAt(LocalDateTime.now());
        reason.setUpdatedAt(LocalDateTime.now());

        AppraisalRequestReason saved = reasonRepository.save(reason);
        return mapToDto(saved);
    }

    @Transactional
    public AppraisalRequestReasonDto updateReason(Long reasonId, UpdateRequestReasonDto dto) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalRequestReason reason = reasonRepository.findByIdAndOrganizationId(reasonId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal request reason not found with ID: " + reasonId));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            reason.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            reason.setDescription(dto.getDescription());
        }
        reason.setUpdatedAt(LocalDateTime.now());
        return mapToDto(reasonRepository.save(reason));
    }

    @Transactional
    public AppraisalRequestReasonDto updateStatus(Long reasonId, boolean active) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalRequestReason reason = reasonRepository.findByIdAndOrganizationId(reasonId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal request reason not found with ID: " + reasonId));

        reason.setActive(active);
        reason.setUpdatedAt(LocalDateTime.now());
        return mapToDto(reasonRepository.save(reason));
    }

    @Transactional
    public void deleteReason(Long reasonId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalRequestReason reason = reasonRepository.findByIdAndOrganizationId(reasonId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal request reason not found with ID: " + reasonId));

        // Enforce preservation rule: do not hard-delete if used by requests
        if (requestRepository.existsByReasonId(reasonId)) {
            reason.setActive(false);
            reason.setUpdatedAt(LocalDateTime.now());
            reasonRepository.save(reason);
        } else {
            reasonRepository.delete(reason);
        }
    }

    public AppraisalRequestReasonDto mapToDto(AppraisalRequestReason r) {
        return new AppraisalRequestReasonDto(
                r.getId(),
                r.getCode(),
                r.getName(),
                r.getDescription(),
                r.isActive()
        );
    }
}
