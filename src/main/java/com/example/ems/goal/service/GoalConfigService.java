package com.example.ems.goal.service;

import com.example.ems.goal.domain.*;
import com.example.ems.goal.repository.*;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoalConfigService {

    @Autowired
    private GoalConfigRepository goalConfigRepository;

    @Autowired
    private GoalCategoryRepository categoryRepository;

    @Autowired
    private GoalTypeRepository typeRepository;

    @Autowired
    private GoalPriorityRepository priorityRepository;

    @Autowired
    private GoalStatusRepository statusRepository;

    @Autowired
    private GoalAssignmentRuleRepository assignmentRuleRepository;

    @Autowired
    private GoalNotificationSettingRepository notificationSettingRepository;

    @Autowired
    private GoalNumberFormatRepository numberFormatRepository;

    @Autowired
    private GoalVisibilitySettingRepository visibilitySettingRepository;

    @Transactional
    public GoalConfig getOrCreateConfig() {
        Long orgId = TenantContext.requireOrganizationId();
        return goalConfigRepository.findByOrganizationId(orgId).orElseGet(() -> {
            GoalConfig config = new GoalConfig();
            config.setOrganizationId(orgId);
            return goalConfigRepository.save(config);
        });
    }

    @Transactional
    public String generateNextGoalNumber() {
        Long orgId = TenantContext.requireOrganizationId();
        GoalNumberFormat format = numberFormatRepository.findByOrganizationId(orgId).orElseGet(() -> {
            GoalNumberFormat fmt = new GoalNumberFormat();
            fmt.setOrganizationId(orgId);
            fmt.setPrefix("GOAL");
            fmt.setSequenceLength(5);
            fmt.setCurrentSequence(1L);
            return numberFormatRepository.save(fmt);
        });

        long seq = format.getCurrentSequence();
        format.setCurrentSequence(seq + 1);
        numberFormatRepository.save(format);

        String paddedSeq = String.format("%0" + format.getSequenceLength() + "d", seq);
        return format.getPrefix() + "-" + orgId + "-" + paddedSeq;
    }

    public List<GoalCategory> getCategories() {
        Long orgId = TenantContext.requireOrganizationId();
        return categoryRepository.findByOrganizationIdAndIsActiveTrue(orgId);
    }

    public List<GoalTypeEntity> getTypes() {
        Long orgId = TenantContext.requireOrganizationId();
        return typeRepository.findByOrganizationIdAndIsActiveTrue(orgId);
    }

    public List<GoalPriorityEntity> getPriorities() {
        Long orgId = TenantContext.requireOrganizationId();
        return priorityRepository.findByOrganizationIdAndIsActiveTrueOrderBySortOrderAsc(orgId);
    }

    public List<GoalStatusEntity> getStatuses() {
        Long orgId = TenantContext.requireOrganizationId();
        return statusRepository.findByOrganizationIdAndIsActiveTrue(orgId);
    }

    public List<GoalVisibilitySetting> getVisibilities() {
        Long orgId = TenantContext.requireOrganizationId();
        return visibilitySettingRepository.findByOrganizationIdAndIsActiveTrue(orgId);
    }

    public List<GoalAssignmentRule> getAssignmentRules() {
        Long orgId = TenantContext.requireOrganizationId();
        return assignmentRuleRepository.findByOrganizationId(orgId);
    }

    public List<GoalNotificationSetting> getNotificationSettings() {
        Long orgId = TenantContext.requireOrganizationId();
        return notificationSettingRepository.findByOrganizationId(orgId);
    }
}
