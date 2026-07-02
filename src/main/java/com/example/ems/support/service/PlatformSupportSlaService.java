package com.example.ems.support.service;

import com.example.ems.auth.entity.User;
import com.example.ems.support.dto.SlaDashboardResponse;
import com.example.ems.support.dto.SlaResponse;
import com.example.ems.support.dto.SupportSlaRequest;
import com.example.ems.support.entity.SupportSla;
import com.example.ems.support.entity.SupportTicketPriority;
import com.example.ems.support.repository.MySupportTicketRepository;
import com.example.ems.support.repository.SupportSlaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlatformSupportSlaService {

    private final SupportSlaRepository slaRepository;
    private final MySupportTicketRepository ticketRepository;

    public PlatformSupportSlaService(SupportSlaRepository slaRepository,
                                     MySupportTicketRepository ticketRepository) {
        this.slaRepository = slaRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public Page<SlaResponse> getSlas(String search, String priorityStr, String statusStr, Boolean isDefault,
                                     String sortBy, String sortDirection, int page, int size) {
        
        SupportTicketPriority priority = null;
        if (priorityStr != null && !priorityStr.trim().isEmpty()) {
            priority = SupportTicketPriority.valueOf(priorityStr.toUpperCase());
        }

        Boolean enabled = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            enabled = "ACTIVE".equalsIgnoreCase(statusStr);
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        String searchPattern = (search != null && !search.trim().isEmpty()) ? "%" + search.trim().toLowerCase() + "%" : null;

        Page<SupportSla> slaPage = slaRepository.findByFilters(
                searchPattern,
                priority,
                enabled,
                isDefault,
                pageable
        );

        LocalDateTime now = LocalDateTime.now();
        return slaPage.map(sla -> {
            long assigned = ticketRepository.countByPriorityAndIsDeletedFalse(sla.getPriority());
            long breached = ticketRepository.countBreachedTicketsByPriority(sla.getPriority(), now);
            double compliance = assigned == 0 ? 100.0 : ((assigned - breached) * 100.0 / assigned);
            return new SlaResponse(sla, assigned, breached, compliance);
        });
    }

    @Transactional(readOnly = true)
    public SlaResponse getSlaDetails(Long id) {
        SupportSla sla = slaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SLA policy not found with ID: " + id));

        LocalDateTime now = LocalDateTime.now();
        long assigned = ticketRepository.countByPriorityAndIsDeletedFalse(sla.getPriority());
        long breached = ticketRepository.countBreachedTicketsByPriority(sla.getPriority(), now);
        double compliance = assigned == 0 ? 100.0 : ((assigned - breached) * 100.0 / assigned);

        return new SlaResponse(sla, assigned, breached, compliance);
    }

    @Transactional
    public SupportSla createSla(SupportSlaRequest req, User user) {
        // Validate name uniqueness
        validateNameUniqueness(req.getName(), null);

        // Validate priority
        SupportTicketPriority priority = parsePriority(req.getPriority());

        SupportSla sla = new SupportSla();
        sla.setName(req.getName().trim());
        sla.setDescription(req.getDescription());
        sla.setPriority(priority);
        sla.setResponseTimeMinutes(req.getFirstResponseMinutes());
        sla.setResolutionTimeMinutes(req.getResolutionMinutes());
        sla.setBusinessHoursOnly(false); // Default to false or map request if needed
        sla.setEnabled("ACTIVE".equalsIgnoreCase(req.getStatus()));
        
        sla.setCreatedBy(user);
        sla.setUpdatedBy(user);
        sla.setCreatedAt(LocalDateTime.now());
        sla.setUpdatedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(req.getIsDefault())) {
            resetOtherDefaults();
            sla.setDefault(true);
        } else {
            sla.setDefault(false);
        }

        return slaRepository.save(sla);
    }

    @Transactional
    public SupportSla updateSla(Long id, SupportSlaRequest req, User user) {
        SupportSla sla = slaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SLA policy not found with ID: " + id));

        // Validate name uniqueness
        validateNameUniqueness(req.getName(), id);

        // Validate priority
        SupportTicketPriority priority = parsePriority(req.getPriority());

        sla.setName(req.getName().trim());
        sla.setDescription(req.getDescription());
        sla.setPriority(priority);
        sla.setResponseTimeMinutes(req.getFirstResponseMinutes());
        sla.setResolutionTimeMinutes(req.getResolutionMinutes());
        sla.setEnabled("ACTIVE".equalsIgnoreCase(req.getStatus()));
        
        sla.setUpdatedBy(user);
        sla.setUpdatedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(req.getIsDefault())) {
            resetOtherDefaults();
            sla.setDefault(true);
        } else {
            // If turning off default, check if we need to auto-assign another default
            if (sla.isDefault()) {
                sla.setDefault(false);
                promoteNewDefault(id);
            }
        }

        return slaRepository.save(sla);
    }

    @Transactional
    public void deleteSla(Long id, User user) {
        SupportSla sla = slaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SLA policy not found with ID: " + id));

        sla.setDeleted(true);
        sla.setUpdatedBy(user);
        sla.setUpdatedAt(LocalDateTime.now());
        slaRepository.save(sla);

        // If the deleted SLA was the default, promote another active one to default
        if (sla.isDefault()) {
            promoteNewDefault(id);
        }
    }

    @Transactional
    public SupportSla updateStatus(Long id, String statusStr, User user) {
        SupportSla sla = slaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SLA policy not found with ID: " + id));

        sla.setEnabled("ACTIVE".equalsIgnoreCase(statusStr));
        sla.setUpdatedBy(user);
        sla.setUpdatedAt(LocalDateTime.now());
        return slaRepository.save(sla);
    }

    @Transactional
    public SupportSla setDefaultSla(Long id, User user) {
        SupportSla sla = slaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SLA policy not found with ID: " + id));

        if (!sla.isEnabled()) {
            throw new IllegalArgumentException("Cannot set inactive SLA policy as default");
        }

        resetOtherDefaults();
        sla.setDefault(true);
        sla.setUpdatedBy(user);
        sla.setUpdatedAt(LocalDateTime.now());
        return slaRepository.save(sla);
    }

    @Transactional
    public SupportSla duplicateSla(Long id, User user) {
        SupportSla origin = slaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SLA policy not found with ID: " + id));

        String newName = origin.getName() + " (Copy)";
        int count = 1;
        while (slaRepository.findByNameIgnoreCase(newName).isPresent()) {
            count++;
            newName = origin.getName() + " (Copy " + count + ")";
        }

        SupportSla copy = new SupportSla();
        copy.setName(newName);
        copy.setDescription(origin.getDescription());
        copy.setPriority(origin.getPriority());
        copy.setResponseTimeMinutes(origin.getResponseTimeMinutes());
        copy.setResolutionTimeMinutes(origin.getResolutionTimeMinutes());
        copy.setBusinessHoursOnly(origin.isBusinessHoursOnly());
        copy.setEnabled(origin.isEnabled());
        copy.setEscalationAfterMinutes(origin.getEscalationAfterMinutes());
        copy.setAutoCloseAfterDays(origin.getAutoCloseAfterDays());
        copy.setWarningBeforeMinutes(origin.getWarningBeforeMinutes());
        copy.setDefault(false); // Copied SLA is never default by default
        
        copy.setCreatedBy(user);
        copy.setUpdatedBy(user);
        copy.setCreatedAt(LocalDateTime.now());
        copy.setUpdatedAt(LocalDateTime.now());

        return slaRepository.save(copy);
    }

    @Transactional(readOnly = true)
    public List<SupportSlaResponsePriorityDto> getPriorities() {
        return Arrays.stream(SupportTicketPriority.values())
                .map(p -> new SupportSlaResponsePriorityDto(p.name(), p.name().substring(0, 1) + p.name().substring(1).toLowerCase()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SlaDashboardResponse getDashboard() {
        List<SupportSla> slas = slaRepository.findAll();
        
        long totalPolicies = slas.stream().filter(s -> !s.isDeleted()).count();
        long activePolicies = slas.stream().filter(s -> !s.isDeleted() && s.isEnabled()).count();
        long inactivePolicies = totalPolicies - activePolicies;
        
        String defaultPolicyName = slas.stream()
                .filter(s -> !s.isDeleted() && s.isDefault())
                .map(SupportSla::getName)
                .findFirst()
                .orElse("None");

        LocalDateTime now = LocalDateTime.now();
        long totalTickets = ticketRepository.countByIsDeletedFalse();
        long breached = ticketRepository.countBreachedTickets(now);
        long withinSla = totalTickets - breached;
        double compliance = totalTickets == 0 ? 100.0 : (withinSla * 100.0 / totalTickets);

        return new SlaDashboardResponse(
                new SlaDashboardResponse.SummaryDto(totalPolicies, activePolicies, inactivePolicies, defaultPolicyName),
                new SlaDashboardResponse.TicketMetricsDto(withinSla, breached, Math.round(compliance * 100.0) / 100.0)
        );
    }

    @Transactional(readOnly = true)
    public SupportSla getByPriority(String priorityStr) {
        SupportTicketPriority priority = parsePriority(priorityStr);
        return slaRepository.findByPriority(priority)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("No SLA configured for priority: " + priority));
    }

    private void validateNameUniqueness(String name, Long id) {
        Optional<SupportSla> existing = slaRepository.findByNameIgnoreCase(name.trim());
        if (existing.isPresent() && (id == null || !existing.get().getId().equals(id))) {
            throw new IllegalArgumentException("SLA policy with name '" + name + "' already exists");
        }
    }

    private SupportTicketPriority parsePriority(String priorityStr) {
        try {
            return SupportTicketPriority.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid priority code: " + priorityStr + ". Allowed values: LOW, MEDIUM, HIGH, CRITICAL");
        }
    }

    private void resetOtherDefaults() {
        List<SupportSla> defaults = slaRepository.findAllByIsDefaultTrue();
        for (SupportSla d : defaults) {
            d.setDefault(false);
            slaRepository.save(d);
        }
    }

    private void promoteNewDefault(Long excludeId) {
        List<SupportSla> active = slaRepository.findAll().stream()
                .filter(s -> !s.isDeleted() && s.isEnabled() && !s.getId().equals(excludeId))
                .toList();
        if (!active.isEmpty()) {
            SupportSla first = active.getFirst();
            first.setDefault(true);
            slaRepository.save(first);
        }
    }

    public static class SupportSlaResponsePriorityDto {
        private String code;
        private String label;

        public SupportSlaResponsePriorityDto(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() { return code; }
        public String getLabel() { return label; }
    }
}
