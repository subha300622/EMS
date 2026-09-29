package com.example.ems.support.service;

import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.support.dto.SupportEscalationRulesDto;
import com.example.ems.support.dto.SupportSlaConfigDto;
import com.example.ems.support.entity.*;
import com.example.ems.support.repository.SupportEscalationRuleRepository;
import com.example.ems.support.repository.SupportSlaConfigRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SupportSlaService {

    @Autowired
    private SupportSlaConfigRepository slaConfigRepository;

    @Autowired
    private SupportEscalationRuleRepository escalationRuleRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private static final Map<SupportTicketPriority, Integer> DEFAULT_SLA_HOURS = Map.of(
            SupportTicketPriority.CRITICAL, 2,
            SupportTicketPriority.HIGH, 4,
            SupportTicketPriority.MEDIUM, 8,
            SupportTicketPriority.LOW, 24
    );

    private static final Set<String> ALLOWED_ACTIONS = Set.of(
            "NOTIFY_MANAGER", "NOTIFY_SUPPORT_HEAD", "REASSIGN"
    );

    public SupportSlaConfigDto getSlaConfig() {
        return getSlaConfig(TenantContext.requireOrganizationId());
    }

    public SupportSlaConfigDto updateSlaConfig(SupportSlaConfigDto dto) {
        return updateSlaConfig(TenantContext.requireOrganizationId(), dto);
    }

    public SupportEscalationRulesDto getEscalationRules() {
        return getEscalationRules(TenantContext.requireOrganizationId());
    }

    public SupportEscalationRulesDto updateEscalationRules(SupportEscalationRulesDto dto) {
        return updateEscalationRules(TenantContext.requireOrganizationId(), dto);
    }

    @Transactional(readOnly = true)
    public SupportSlaConfigDto getSlaConfig(Long organizationId) {
        Optional<SupportSlaConfig> configOpt = slaConfigRepository.findByOrganizationId(organizationId);
        if (configOpt.isPresent()) {
            SupportSlaConfig config = configOpt.get();
            List<SupportSlaConfigDto.SlaRuleItem> items = new ArrayList<>();
            for (SupportSlaRule r : config.getRules()) {
                items.add(new SupportSlaConfigDto.SlaRuleItem(r.getPriority().name(), r.getSlaHours()));
            }
            return new SupportSlaConfigDto(config.isEnabled(), items);
        }

        // Return default configuration
        List<SupportSlaConfigDto.SlaRuleItem> defaultRules = List.of(
                new SupportSlaConfigDto.SlaRuleItem("CRITICAL", 2),
                new SupportSlaConfigDto.SlaRuleItem("HIGH", 4),
                new SupportSlaConfigDto.SlaRuleItem("MEDIUM", 8),
                new SupportSlaConfigDto.SlaRuleItem("LOW", 24)
        );
        return new SupportSlaConfigDto(true, defaultRules);
    }

    @Transactional
    public SupportSlaConfigDto updateSlaConfig(Long organizationId, SupportSlaConfigDto dto) {
        if (dto.getRules() == null || dto.getRules().isEmpty()) {
            throw new IllegalArgumentException("SLA rules cannot be empty");
        }

        Set<SupportTicketPriority> seen = new HashSet<>();
        List<SupportSlaRule> newRules = new ArrayList<>();

        for (SupportSlaConfigDto.SlaRuleItem item : dto.getRules()) {
            if (item.getPriority() == null || item.getPriority().isBlank()) {
                throw new IllegalArgumentException("Priority is required for each SLA rule");
            }
            SupportTicketPriority priority;
            try {
                priority = SupportTicketPriority.valueOf(item.getPriority().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid priority: " + item.getPriority());
            }

            if (seen.contains(priority)) {
                throw new IllegalArgumentException("Duplicate SLA rule for priority: " + priority);
            }
            seen.add(priority);

            if (item.getSlaHours() == null || item.getSlaHours() <= 0) {
                throw new IllegalArgumentException("SLA hours must be greater than 0 for priority: " + priority);
            }

            newRules.add(new SupportSlaRule(priority, item.getSlaHours()));
        }

        if (seen.size() != 4 || !seen.containsAll(Arrays.asList(SupportTicketPriority.values()))) {
            throw new IllegalArgumentException("SLA configuration must contain exactly one rule for each priority (CRITICAL, HIGH, MEDIUM, LOW)");
        }

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));

        SupportSlaConfig config = slaConfigRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> new SupportSlaConfig(org, dto.isEnabled()));

        config.setEnabled(dto.isEnabled());
        config.setRules(newRules);
        config.setUpdatedAt(LocalDateTime.now());
        SupportSlaConfig saved = slaConfigRepository.save(config);

        List<SupportSlaConfigDto.SlaRuleItem> resultRules = new ArrayList<>();
        for (SupportSlaRule r : saved.getRules()) {
            resultRules.add(new SupportSlaConfigDto.SlaRuleItem(r.getPriority().name(), r.getSlaHours()));
        }
        return new SupportSlaConfigDto(saved.isEnabled(), resultRules);
    }

    @Transactional(readOnly = true)
    public SupportEscalationRulesDto getEscalationRules(Long organizationId) {
        List<SupportEscalationRule> rules = escalationRuleRepository.findByOrganizationIdOrderByLevelAsc(organizationId);
        if (rules != null && !rules.isEmpty()) {
            List<SupportEscalationRulesDto.EscalationRuleItem> items = new ArrayList<>();
            for (SupportEscalationRule r : rules) {
                items.add(new SupportEscalationRulesDto.EscalationRuleItem(r.getLevel(), r.getTriggerAfterMinutes(), r.getAction()));
            }
            return new SupportEscalationRulesDto(items);
        }

        // Return default escalation rules
        List<SupportEscalationRulesDto.EscalationRuleItem> defaultRules = List.of(
                new SupportEscalationRulesDto.EscalationRuleItem(1, 30, "NOTIFY_MANAGER"),
                new SupportEscalationRulesDto.EscalationRuleItem(2, 60, "NOTIFY_SUPPORT_HEAD"),
                new SupportEscalationRulesDto.EscalationRuleItem(3, 120, "REASSIGN")
        );
        return new SupportEscalationRulesDto(defaultRules);
    }

    @Transactional
    public SupportEscalationRulesDto updateEscalationRules(Long organizationId, SupportEscalationRulesDto dto) {
        if (dto.getRules() == null || dto.getRules().isEmpty()) {
            throw new IllegalArgumentException("Escalation rules cannot be empty");
        }

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));

        // Sort by level
        List<SupportEscalationRulesDto.EscalationRuleItem> sorted = new ArrayList<>(dto.getRules());
        sorted.sort(Comparator.comparing(SupportEscalationRulesDto.EscalationRuleItem::getLevel, Comparator.nullsLast(Comparator.naturalOrder())));

        int expectedLevel = 1;
        int prevTriggerMinutes = 0;

        List<SupportEscalationRule> entities = new ArrayList<>();
        for (SupportEscalationRulesDto.EscalationRuleItem item : sorted) {
            if (item.getLevel() == null || item.getLevel() != expectedLevel) {
                throw new IllegalArgumentException("Escalation levels must be sequential starting from 1 (expected level: " + expectedLevel + ")");
            }
            if (item.getTriggerAfterMinutes() == null || item.getTriggerAfterMinutes() <= 0) {
                throw new IllegalArgumentException("Trigger minutes must be greater than 0 for level " + item.getLevel());
            }
            if (item.getTriggerAfterMinutes() <= prevTriggerMinutes) {
                throw new IllegalArgumentException("Trigger times must strictly increase by level (level " + item.getLevel() + " must be > " + prevTriggerMinutes + " mins)");
            }
            if (item.getAction() == null || !ALLOWED_ACTIONS.contains(item.getAction().toUpperCase())) {
                throw new IllegalArgumentException("Unsupported action for level " + item.getLevel() + ": " + item.getAction() + ". Supported: " + ALLOWED_ACTIONS);
            }

            prevTriggerMinutes = item.getTriggerAfterMinutes();
            expectedLevel++;

            entities.add(new SupportEscalationRule(org, item.getLevel(), item.getTriggerAfterMinutes(), item.getAction().toUpperCase()));
        }

        escalationRuleRepository.deleteByOrganizationId(organizationId);
        List<SupportEscalationRule> saved = escalationRuleRepository.saveAll(entities);

        List<SupportEscalationRulesDto.EscalationRuleItem> items = new ArrayList<>();
        for (SupportEscalationRule r : saved) {
            items.add(new SupportEscalationRulesDto.EscalationRuleItem(r.getLevel(), r.getTriggerAfterMinutes(), r.getAction()));
        }
        return new SupportEscalationRulesDto(items);
    }

    @Transactional(readOnly = true)
    public int resolveSlaHours(Long organizationId, SupportTicketPriority priority) {
        if (organizationId != null) {
            Optional<SupportSlaConfig> configOpt = slaConfigRepository.findByOrganizationId(organizationId);
            if (configOpt.isPresent() && configOpt.get().isEnabled()) {
                for (SupportSlaRule r : configOpt.get().getRules()) {
                    if (r.getPriority() == priority) {
                        return r.getSlaHours();
                    }
                }
            }
        }
        return DEFAULT_SLA_HOURS.getOrDefault(priority, 8);
    }

    public LocalDateTime calculateInitialDueDate(LocalDateTime startTime, int slaHours) {
        return (startTime != null ? startTime : LocalDateTime.now()).plusHours(slaHours);
    }

    /**
     * Calculates the new due date when priority changes, without resetting the SLA clock.
     * Takes elapsed time into account: newDueDate = createdAt + newSlaHours.
     */
    public LocalDateTime recalculateDueDateOnPriorityChange(MySupportTicket ticket, SupportTicketPriority newPriority, Long organizationId) {
        int newSlaHours = resolveSlaHours(organizationId, newPriority);
        LocalDateTime baseTime = ticket.getCreatedAt() != null ? ticket.getCreatedAt() : LocalDateTime.now();
        return baseTime.plusHours(newSlaHours);
    }
}
