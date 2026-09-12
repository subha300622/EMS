package com.example.ems.organization.service;

import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.organization.dto.CompensationConfigRequest;
import com.example.ems.organization.dto.CompensationConfigResponse;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationCompensationConfig;
import com.example.ems.organization.repository.OrganizationCompensationConfigRepository;
import com.example.ems.organization.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrganizationCompensationConfigService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationCompensationConfigService.class);

    private final OrganizationCompensationConfigRepository configRepository;
    private final OrganizationRepository organizationRepository;

    public OrganizationCompensationConfigService(OrganizationCompensationConfigRepository configRepository,
                                                 OrganizationRepository organizationRepository) {
        this.configRepository = configRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public boolean isOvertimeEnabled(Long organizationId) {
        if (organizationId == null) return true;
        return configRepository.findByOrganizationId(organizationId)
                .map(OrganizationCompensationConfig::isOvertimeEnabled)
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public void requireOvertimeEnabled(Long organizationId) {
        if (!isOvertimeEnabled(organizationId)) {
            throw new com.example.ems.common.exception.ModuleDisabledException(
                    "OVERTIME_MODULE_DISABLED",
                    "Overtime module is not enabled for this organization"
            );
        }
    }

    @Transactional(readOnly = true)
    public boolean isIncentiveEnabled(Long organizationId) {
        if (organizationId == null) return true;
        return configRepository.findByOrganizationId(organizationId)
                .map(OrganizationCompensationConfig::isIncentiveEnabled)
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public void requireIncentiveEnabled(Long organizationId) {
        if (!isIncentiveEnabled(organizationId)) {
            throw new com.example.ems.common.exception.ModuleDisabledException(
                    "INCENTIVE_MODULE_DISABLED",
                    "Incentive module is not enabled for this organization"
            );
        }
    }

    @Transactional(readOnly = true)
    public boolean isBonusEnabled(Long organizationId) {
        if (organizationId == null) return true;
        return configRepository.findByOrganizationId(organizationId)
                .map(OrganizationCompensationConfig::isBonusEnabled)
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public void requireBonusEnabled(Long organizationId) {
        if (!isBonusEnabled(organizationId)) {
            throw new com.example.ems.common.exception.ModuleDisabledException(
                    "BONUS_MODULE_DISABLED",
                    "Bonus module is not enabled for this organization"
            );
        }
    }

    @Transactional(readOnly = true)
    public CompensationConfigResponse getConfig(Long organizationId) {
        return configRepository.findByOrganizationId(organizationId)
                .map(CompensationConfigResponse::fromEntity)
                .orElseGet(() -> CompensationConfigResponse.defaults(organizationId));
    }

    public CompensationConfigResponse updateConfig(Long organizationId, CompensationConfigRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + organizationId));

        OrganizationCompensationConfig config = configRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> new OrganizationCompensationConfig(organization));

        if (request.getOvertimeEnabled() != null) {
            config.setOvertimeEnabled(request.getOvertimeEnabled());
        }
        if (request.getIncentiveEnabled() != null) {
            config.setIncentiveEnabled(request.getIncentiveEnabled());
        }
        if (request.getBonusEnabled() != null) {
            config.setBonusEnabled(request.getBonusEnabled());
        }

        OrganizationCompensationConfig saved = configRepository.save(config);
        log.info("Updated Compensation Configuration for Org ID={}: OT={}, Incentive={}, Bonus={}",
                organizationId, saved.isOvertimeEnabled(), saved.isIncentiveEnabled(), saved.isBonusEnabled());

        return CompensationConfigResponse.fromEntity(saved);
    }
}
