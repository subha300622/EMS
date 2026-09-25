package com.example.ems.support.service;

import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.enums.AuditModule;
import com.example.ems.audit.service.AuditLogService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.common.service.NotificationService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.service.PermissionCheckService;
import com.example.ems.support.dto.*;
import com.example.ems.support.entity.*;
import com.example.ems.support.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class SupportTicketService {

    @Autowired
    private MySupportTicketRepository ticketRepository;

    @Autowired
    private MySupportCategoryRepository categoryRepository;

    @Autowired
    private MySupportCommentRepository commentRepository;

    @Autowired
    private MySupportAttachmentRepository attachmentRepository;

    @Autowired
    private SupportTicketStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private SupportEscalationHistoryRepository escalationHistoryRepository;

    @Autowired
    private SupportWorkLogRepository workLogRepository;

    @Autowired
    private SupportTicketAssignmentRepository assignmentRepository;

    @Autowired
    private SupportSlaService slaService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired(required = false)
    private AuditLogService auditLogService;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Autowired
    private PermissionCheckService permissionCheckService;

    private static final AtomicLong TICKET_SEQUENCE = new AtomicLong(System.currentTimeMillis() % 1000000L);

    private static final DateTimeFormatter ISO_OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("+05:30");

    private String formatDateTime(LocalDateTime ldt) {
        if (ldt == null) return null;
        return ZonedDateTime.of(ldt, DEFAULT_ZONE).format(ISO_OFFSET_FORMATTER);
    }

    private Long getTenantId() {
        return TenantContext.requireOrganizationId();
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
    }

    private Employee getCurrentEmployee(Long tenantId) {
        String email = getCurrentUserEmail();
        if (email == null) return null;
        return employeeRepository.findByEmailAndOrganizationId(email, tenantId)
                .or(() -> employeeRepository.findByEmail(email))
                .orElse(null);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 1. Create Ticket (POST /api/v1/support/tickets)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional
    public SupportTicketDetailResponse createTicket(CreateSupportTicketRequest req) {
        Long tenantId = getTenantId();
        Organization org = organizationRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + tenantId));

        if (req.getSubject() == null || req.getSubject().isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (req.getSubject().length() > 250) {
            throw new IllegalArgumentException("Subject must not exceed 250 characters");
        }
        if (req.getDescription() == null || req.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }

        // Validate category
        MySupportCategory category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + req.getCategoryId()));
        if (category.getOrganization() != null && !category.getOrganization().getId().equals(tenantId)) {
            throw new IllegalArgumentException("Category does not belong to the current organization");
        }

        // Validate requester
        Employee requester = getCurrentEmployee(tenantId);
        if (requester == null) {
            // Fallback: look up by user email
            String email = getCurrentUserEmail();
            if (email != null) {
                requester = employeeRepository.findByEmail(email).orElse(null);
            }
        }
        if (requester == null) {
            throw new IllegalStateException("Authenticated requester employee record not found");
        }
        if (requester.getStatus() != null && !"ACTIVE".equalsIgnoreCase(requester.getStatus())) {
            throw new IllegalStateException("Requester must be an active employee");
        }

        // Validate priority
        SupportTicketPriority priority = SupportTicketPriority.MEDIUM;
        if (req.getPriority() != null && !req.getPriority().isBlank()) {
            try {
                priority = SupportTicketPriority.valueOf(req.getPriority().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid priority. Must be CRITICAL, HIGH, MEDIUM, or LOW");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        String ticketNumber = String.format("ST-%d-%06d", now.getYear(), TICKET_SEQUENCE.incrementAndGet() % 1000000L);

        MySupportTicket ticket = new MySupportTicket();
        ticket.setTicketNumber(ticketNumber);
        ticket.setSubject(req.getSubject());
        ticket.setDescription(req.getDescription());
        ticket.setCategory(category);
        ticket.setEmployee(requester);
        ticket.setOrganization(org);
        ticket.setPriority(priority);
        ticket.setStatus(SupportTicketStatus.NEW);
        ticket.setSource(req.getSource() != null ? req.getSource() : "WEB");
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        // Load SLA & calculate due date
        int slaHours = slaService.resolveSlaHours(tenantId, priority);
        ticket.setSlaHours(slaHours);
        ticket.setDueDate(slaService.calculateInitialDueDate(now, slaHours));
        ticket.setOverdue(false);
        ticket.setEscalated(false);
        ticket.setEscalationLevel(0);

        MySupportTicket savedTicket = ticketRepository.save(ticket);

        // Link attachments
        if (req.getAttachments() != null && !req.getAttachments().isEmpty()) {
            for (SupportAttachmentRefDto attDto : req.getAttachments()) {
                if (attDto.getFileId() != null) {
                    attachmentRepository.findById(attDto.getFileId()).ifPresent(att -> {
                        att.setTicket(savedTicket);
                        attachmentRepository.save(att);
                    });
                }
            }
        }

        // Status history: null -> NEW
        String creatorName = requester.getFullName() != null ? requester.getFullName() : requester.getEmail();
        statusHistoryRepository.save(new SupportTicketStatusHistory(savedTicket, null, "NEW", creatorName));

        // Audit record
        if (auditLogService != null) {
            auditLogService.success(AuditModule.SUPPORT, AuditAction.CREATE, "MySupportTicket",
                    String.valueOf(savedTicket.getId()), null, savedTicket.getTicketNumber(),
                    "Created support ticket: " + savedTicket.getTicketNumber());
        }

        // Notify Support Manager
        notifyManagers(org, "New Support Ticket: " + savedTicket.getTicketNumber(),
                "A new support ticket has been logged with subject: " + savedTicket.getSubject());

        return mapToDetailResponse(savedTicket);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 2. Get Ticket (GET /api/v1/support/tickets/{ticketId})
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public SupportTicketDetailResponse getTicketById(Long ticketId) {
        Long tenantId = getTenantId();
        MySupportTicket ticket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found or does not belong to organization: " + ticketId));

        return mapToDetailResponse(ticket);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 3. Ticket List / Filters (GET /api/v1/support/tickets)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<SupportTicketDetailResponse> getTickets(String status, String priority, String timeFilter,
                                                        String specialFilter, Pageable pageable) {
        Long tenantId = getTenantId();

        // Validate enums
        SupportTicketStatus statusEnum = null;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            try {
                statusEnum = SupportTicketStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status filter: " + status);
            }
        }

        SupportTicketPriority priorityEnum = null;
        if (priority != null && !priority.isBlank() && !"ALL".equalsIgnoreCase(priority)) {
            try {
                priorityEnum = SupportTicketPriority.valueOf(priority.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid priority filter: " + priority);
            }
        }

        // Validate time filter
        LocalDateTime timeStart = null;
        LocalDateTime timeEnd = null;
        if (timeFilter != null && !timeFilter.isBlank() && !"ALL".equalsIgnoreCase(timeFilter)) {
            LocalDate today = LocalDate.now();
            switch (timeFilter.toUpperCase()) {
                case "TODAY":
                    timeStart = today.atStartOfDay();
                    timeEnd = today.atTime(LocalTime.MAX);
                    break;
                case "THIS_WEEK":
                    timeStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
                    timeEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);
                    break;
                case "LAST_WEEK":
                    timeStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1).atStartOfDay();
                    timeEnd = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusDays(1).atTime(LocalTime.MAX);
                    break;
                case "THIS_MONTH":
                    timeStart = today.withDayOfMonth(1).atStartOfDay();
                    timeEnd = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid timeFilter: " + timeFilter + ". Supported: TODAY, THIS_WEEK, LAST_WEEK, THIS_MONTH, ALL");
            }
        }

        // Validate special filter
        Boolean filterOverdue = null;
        Boolean filterEscalated = null;
        if (specialFilter != null && !specialFilter.isBlank()) {
            switch (specialFilter.toUpperCase()) {
                case "OVERDUE":
                    filterOverdue = true;
                    break;
                case "ESCALATED":
                    filterEscalated = true;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid specialFilter: " + specialFilter + ". Supported: OVERDUE, ESCALATED");
            }
        }

        final SupportTicketStatus finalStatus = statusEnum;
        final SupportTicketPriority finalPriority = priorityEnum;
        final LocalDateTime finalTimeStart = timeStart;
        final LocalDateTime finalTimeEnd = timeEnd;
        final Boolean finalFilterOverdue = filterOverdue;
        final Boolean finalFilterEscalated = filterEscalated;

        Specification<MySupportTicket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organization").get("id"), tenantId));
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (finalStatus != null) {
                predicates.add(cb.equal(root.get("status"), finalStatus));
            }
            if (finalPriority != null) {
                predicates.add(cb.equal(root.get("priority"), finalPriority));
            }
            if (finalTimeStart != null && finalTimeEnd != null) {
                predicates.add(cb.between(root.get("createdAt"), finalTimeStart, finalTimeEnd));
            }
            if (Boolean.TRUE.equals(finalFilterOverdue)) {
                predicates.add(cb.or(
                        cb.equal(root.get("isOverdue"), true),
                        cb.and(
                                cb.notEqual(root.get("status"), SupportTicketStatus.CLOSED),
                                cb.notEqual(root.get("status"), SupportTicketStatus.RESOLVED),
                                cb.notEqual(root.get("status"), SupportTicketStatus.REJECTED),
                                cb.notEqual(root.get("status"), SupportTicketStatus.DUPLICATE),
                                cb.isNotNull(root.get("dueDate")),
                                cb.lessThan(root.get("dueDate"), LocalDateTime.now())
                        )
                ));
            }
            if (Boolean.TRUE.equals(finalFilterEscalated)) {
                predicates.add(cb.equal(root.get("isEscalated"), true));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<MySupportTicket> page = ticketRepository.findAll(spec, pageable);
        return page.map(this::mapToDetailResponse);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 4, 5, 6. Manager Review (POST /api/v1/support/tickets/{ticketId}/review)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional
    public SupportTicketDetailResponse reviewTicket(Long ticketId, SupportTicketReviewRequest req) {
        Long tenantId = getTenantId();
        MySupportTicket ticket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if (ticket.getStatus() != SupportTicketStatus.NEW) {
            throw new IllegalStateException("Ticket must be in NEW status to be reviewed. Current status: " + ticket.getStatus());
        }

        if (req.getAction() == null || req.getAction().isBlank()) {
            throw new IllegalArgumentException("Review action is required (ACCEPT, REJECT, DUPLICATE)");
        }

        String action = req.getAction().toUpperCase();
        String reviewer = getCurrentUserEmail();

        switch (action) {
            case "ACCEPT":
                if (req.getPriority() == null || req.getPriority().isBlank()) {
                    throw new IllegalArgumentException("Priority is required when accepting ticket");
                }
                SupportTicketPriority priority;
                try {
                    priority = SupportTicketPriority.valueOf(req.getPriority().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid priority: " + req.getPriority());
                }

                if (req.getEstimatedHours() == null || req.getEstimatedHours() <= 0) {
                    throw new IllegalArgumentException("Estimated hours must be greater than 0");
                }

                if (req.getCategoryId() != null) {
                    MySupportCategory cat = categoryRepository.findById(req.getCategoryId())
                            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.getCategoryId()));
                    if (cat.getOrganization() != null && !cat.getOrganization().getId().equals(tenantId)) {
                        throw new IllegalArgumentException("Category does not belong to organization");
                    }
                    ticket.setCategory(cat);
                }

                int slaHours = slaService.resolveSlaHours(tenantId, priority);
                ticket.setPriority(priority);
                ticket.setEstimatedHours(req.getEstimatedHours());
                ticket.setSlaHours(slaHours);
                ticket.setDueDate(slaService.calculateInitialDueDate(LocalDateTime.now(), slaHours));
                ticket.setUpdatedAt(LocalDateTime.now());
                ticketRepository.save(ticket);

                if (auditLogService != null) {
                    auditLogService.success(AuditModule.SUPPORT, AuditAction.REVIEW, "MySupportTicket",
                            String.valueOf(ticket.getId()), null, "ACCEPTED", "Accepted ticket: " + ticket.getTicketNumber());
                }
                break;

            case "REJECT":
                if (req.getReason() == null || req.getReason().isBlank()) {
                    throw new IllegalArgumentException("Rejection reason is required");
                }
                if (req.getReason().length() > 1000) {
                    throw new IllegalArgumentException("Rejection reason cannot exceed 1000 characters");
                }
                ticket.setStatus(SupportTicketStatus.REJECTED);
                ticket.setRejectionReason(req.getReason());
                ticket.setRejectedBy(reviewer);
                ticket.setRejectedAt(LocalDateTime.now());
                ticket.setUpdatedAt(LocalDateTime.now());
                ticketRepository.save(ticket);

                statusHistoryRepository.save(new SupportTicketStatusHistory(ticket, "NEW", "REJECTED", reviewer));

                if (auditLogService != null) {
                    auditLogService.success(AuditModule.SUPPORT, AuditAction.REJECT, "MySupportTicket",
                            String.valueOf(ticket.getId()), "NEW", "REJECTED", "Rejected ticket: " + ticket.getTicketNumber() + ". Reason: " + req.getReason());
                }
                break;

            case "DUPLICATE":
                if (req.getDuplicateOfTicketId() == null) {
                    throw new IllegalArgumentException("duplicateOfTicketId is required when marking duplicate");
                }
                if (ticket.getId().equals(req.getDuplicateOfTicketId())) {
                    throw new IllegalArgumentException("Ticket cannot reference itself as duplicate");
                }
                if (req.getReason() == null || req.getReason().isBlank()) {
                    throw new IllegalArgumentException("Reason is required when marking duplicate");
                }

                MySupportTicket originalTicket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(req.getDuplicateOfTicketId(), tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Original ticket not found in this organization: " + req.getDuplicateOfTicketId()));

                if (originalTicket.getStatus() == SupportTicketStatus.DUPLICATE) {
                    throw new IllegalStateException("Original ticket is already marked as DUPLICATE");
                }

                ticket.setStatus(SupportTicketStatus.DUPLICATE);
                ticket.setDuplicateOfTicketId(originalTicket.getId());
                ticket.setClosureComment(req.getReason());
                ticket.setUpdatedAt(LocalDateTime.now());
                ticketRepository.save(ticket);

                statusHistoryRepository.save(new SupportTicketStatusHistory(ticket, "NEW", "DUPLICATE", reviewer));

                if (auditLogService != null) {
                    auditLogService.success(AuditModule.SUPPORT, AuditAction.UPDATE, "MySupportTicket",
                            String.valueOf(ticket.getId()), "NEW", "DUPLICATE", "Marked as duplicate of #" + originalTicket.getTicketNumber());
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported review action: " + action + ". Allowed: ACCEPT, REJECT, DUPLICATE");
        }

        return mapToDetailResponse(ticket);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 7, 8. Assign / Reassign Engineer (PUT /api/v1/support/tickets/{ticketId}/assignment)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional
    public SupportTicketDetailResponse assignTicket(Long ticketId, SupportTicketAssignRequest req) {
        Long tenantId = getTenantId();
        MySupportTicket ticket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if (ticket.getStatus() == SupportTicketStatus.CLOSED ||
                ticket.getStatus() == SupportTicketStatus.REJECTED ||
                ticket.getStatus() == SupportTicketStatus.DUPLICATE) {
            throw new IllegalStateException("Cannot assign engineer to ticket in " + ticket.getStatus() + " status");
        }

        Employee engineer = employeeRepository.findByIdAndOrganizationId(req.getAssignedToId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Engineer not found or does not belong to organization: " + req.getAssignedToId()));

        if (engineer.getStatus() != null && !"ACTIVE".equalsIgnoreCase(engineer.getStatus())) {
            throw new IllegalStateException("Assigned engineer must be an active employee");
        }

        String assigner = getCurrentUserEmail();
        LocalDateTime now = LocalDateTime.now();
        Employee previousEngineer = ticket.getAssignedTo();
        boolean isReassignment = previousEngineer != null && !previousEngineer.getId().equals(engineer.getId());

        if (isReassignment) {
            if (req.getReason() == null || req.getReason().isBlank()) {
                throw new IllegalArgumentException("Reason is required when reassigning a ticket");
            }
        }

        SupportTicketAssignment assignment = new SupportTicketAssignment();
        assignment.setTicketId(ticket.getId());
        assignment.setAssignedTo(engineer.getFullName() != null ? engineer.getFullName() : engineer.getEmail());
        assignment.setAssignedBy(assigner != null ? assigner : "SYSTEM");
        assignment.setAssignedAt(now);
        assignment.setReason(req.getReason());
        assignment.setAssignmentType(isReassignment ? "REASSIGN" : "MANUAL");
        assignmentRepository.save(assignment);

        ticket.setAssignedTo(engineer);
        SupportTicketStatus prevStatus = ticket.getStatus();
        if (ticket.getStatus() == SupportTicketStatus.NEW || ticket.getStatus() == SupportTicketStatus.OPEN) {
            ticket.setStatus(SupportTicketStatus.IN_PROGRESS);
            statusHistoryRepository.save(new SupportTicketStatusHistory(ticket, prevStatus.name(), "IN_PROGRESS", assigner));
        }
        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        if (auditLogService != null) {
            auditLogService.success(AuditModule.SUPPORT, isReassignment ? AuditAction.UPDATE : AuditAction.ASSIGN,
                    "MySupportTicket", String.valueOf(ticket.getId()),
                    previousEngineer != null ? previousEngineer.getEmail() : null,
                    engineer.getEmail(),
                    (isReassignment ? "Reassigned" : "Assigned") + " ticket #" + ticket.getTicketNumber() + " to " + engineer.getFullName());
        }

        // Notify Engineer
        userRepository.findByWorkEmail(engineer.getEmail()).ifPresent(u -> {
            if (notificationService != null) {
                notificationService.sendNotification(u, "Support Ticket Assigned: " + ticket.getTicketNumber(),
                        "You have been assigned to support ticket: " + ticket.getSubject());
            }
        });

        return mapToDetailResponse(ticket);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 9. Update Priority (PATCH /api/v1/support/tickets/{ticketId}/priority)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional
    public SupportTicketDetailResponse updatePriority(Long ticketId, SupportTicketPriorityUpdateRequest req) {
        Long tenantId = getTenantId();
        MySupportTicket ticket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if (ticket.getStatus() == SupportTicketStatus.CLOSED ||
                ticket.getStatus() == SupportTicketStatus.REJECTED ||
                ticket.getStatus() == SupportTicketStatus.DUPLICATE) {
            throw new IllegalStateException("Cannot change priority of ticket in " + ticket.getStatus() + " status");
        }

        SupportTicketPriority newPriority;
        try {
            newPriority = SupportTicketPriority.valueOf(req.getPriority().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid priority: " + req.getPriority());
        }

        SupportTicketPriority oldPriority = ticket.getPriority();
        LocalDateTime newDueDate = slaService.recalculateDueDateOnPriorityChange(ticket, newPriority, tenantId);
        int newSlaHours = slaService.resolveSlaHours(tenantId, newPriority);

        ticket.setPriority(newPriority);
        ticket.setSlaHours(newSlaHours);
        ticket.setDueDate(newDueDate);
        ticket.setOverdue(newDueDate.isBefore(LocalDateTime.now()));
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        if (auditLogService != null) {
            auditLogService.success(AuditModule.SUPPORT, AuditAction.UPDATE, "MySupportTicket",
                    String.valueOf(ticket.getId()), oldPriority.name(), newPriority.name(),
                    "Priority updated to " + newPriority + ". Reason: " + req.getReason());
        }

        return mapToDetailResponse(ticket);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 10. Comments (POST & GET /api/v1/support/tickets/{ticketId}/comments)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional
    public SupportTicketCommentResponse addComment(Long ticketId, SupportTicketCommentRequest req) {
        Long tenantId = getTenantId();
        MySupportTicket ticket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if (req.getComment() == null || req.getComment().isBlank()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
        if (req.getComment().length() > 5000) {
            throw new IllegalArgumentException("Comment cannot exceed 5000 characters");
        }

        String author = getCurrentUserEmail();
        boolean isInternal = Boolean.TRUE.equals(req.getIsInternal());

        MySupportComment comment = new MySupportComment(null, ticket, req.getComment(), author, isInternal);
        MySupportComment saved = commentRepository.save(comment);

        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        return new SupportTicketCommentResponse(saved.getId(), saved.getCommentText(), saved.isInternal(),
                saved.getCreatedBy(), formatDateTime(saved.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public List<SupportTicketCommentResponse> getComments(Long ticketId) {
        Long tenantId = getTenantId();
        MySupportTicket ticket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        boolean canViewInternal = permissionCheckService.hasAnyPermission("SUPPORT_TICKET_VIEW", "SUPPORT_TICKET_ASSIGN", "system.manage");

        List<MySupportComment> comments = canViewInternal ?
                commentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId()) :
                commentRepository.findByTicketIdAndIsInternalFalseOrderByCreatedAtAsc(ticket.getId());

        return comments.stream().map(c -> new SupportTicketCommentResponse(
                c.getId(), c.getCommentText(), c.isInternal(), c.getCreatedBy(), formatDateTime(c.getCreatedAt())
        )).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 11. Work Logs (POST & GET /api/v1/support/tickets/{ticketId}/work-logs)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional
    public SupportWorkLogResponse addWorkLog(Long ticketId, SupportWorkLogRequest req) {
        Long tenantId = getTenantId();
        MySupportTicket ticket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if (ticket.getStatus() == SupportTicketStatus.CLOSED) {
            throw new IllegalStateException("Cannot log work on a CLOSED ticket");
        }

        if (req.getStartedAt() == null || req.getEndedAt() == null) {
            throw new IllegalArgumentException("startedAt and endedAt are required");
        }
        if (!req.getEndedAt().isAfter(req.getStartedAt())) {
            throw new IllegalArgumentException("endedAt must be strictly after startedAt");
        }
        if (req.getEndedAt().isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new IllegalArgumentException("Cannot log work in the future");
        }

        Employee engineer = getCurrentEmployee(tenantId);
        if (engineer == null && ticket.getAssignedTo() != null) {
            engineer = ticket.getAssignedTo();
        }
        if (engineer == null) {
            throw new IllegalStateException("Engineer employee record not found for authenticated caller");
        }

        // Validate overlap
        boolean overlap = workLogRepository.existsOverlappingWorkLog(engineer.getId(), req.getStartedAt(), req.getEndedAt());
        if (overlap) {
            throw new IllegalStateException("Work log overlaps with an existing entry for this engineer");
        }

        long minutes = Duration.between(req.getStartedAt(), req.getEndedAt()).toMinutes();
        double hours = Math.round((minutes / 60.0) * 10.0) / 10.0;

        SupportWorkLog workLog = new SupportWorkLog(ticket, engineer, req.getStartedAt(), req.getEndedAt(), hours, req.getDescription());
        SupportWorkLog saved = workLogRepository.save(workLog);

        // Update aggregate on ticket
        double currentActual = ticket.getActualHours() != null ? ticket.getActualHours() : 0.0;
        ticket.setActualHours(Math.round((currentActual + hours) * 10.0) / 10.0);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        return new SupportWorkLogResponse(saved.getId(), engineer.getId(), engineer.getFullName(),
                formatDateTime(saved.getStartedAt()), formatDateTime(saved.getEndedAt()),
                saved.getActualHours(), saved.getDescription(), formatDateTime(saved.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public List<SupportWorkLogResponse> getWorkLogs(Long ticketId) {
        Long tenantId = getTenantId();
        MySupportTicket ticket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        return workLogRepository.findByTicketIdOrderByStartedAtAsc(ticket.getId()).stream()
                .map(w -> new SupportWorkLogResponse(w.getId(),
                        w.getEngineer() != null ? w.getEngineer().getId() : null,
                        w.getEngineer() != null ? w.getEngineer().getFullName() : null,
                        formatDateTime(w.getStartedAt()), formatDateTime(w.getEndedAt()),
                        w.getActualHours(), w.getDescription(), formatDateTime(w.getCreatedAt())))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 12. Resolve Ticket (POST /api/v1/support/tickets/{ticketId}/resolve)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional
    public SupportTicketDetailResponse resolveTicket(Long ticketId, SupportTicketResolveRequest req) {
        Long tenantId = getTenantId();
        MySupportTicket ticket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if (ticket.getStatus() == SupportTicketStatus.CLOSED) {
            throw new IllegalStateException("Cannot resolve an already CLOSED ticket");
        }
        if (ticket.getStatus() != SupportTicketStatus.IN_PROGRESS && !ticket.isOverdue()) {
            throw new IllegalStateException("Ticket must be IN_PROGRESS to resolve. Current status: " + ticket.getStatus());
        }

        if (req.getResolution() == null || req.getResolution().isBlank()) {
            throw new IllegalArgumentException("Resolution is required");
        }
        if (req.getActualHours() != null && req.getActualHours() < 0) {
            throw new IllegalArgumentException("Actual hours cannot be negative");
        }

        String resolver = getCurrentUserEmail();
        LocalDateTime now = LocalDateTime.now();
        SupportTicketStatus prevStatus = ticket.getStatus();

        ticket.setStatus(SupportTicketStatus.RESOLVED);
        ticket.setResolution(req.getResolution());
        ticket.setResolvedBy(resolver);
        ticket.setResolvedAt(now);
        if (req.getActualHours() != null) {
            ticket.setActualHours(req.getActualHours());
        }
        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        statusHistoryRepository.save(new SupportTicketStatusHistory(ticket, prevStatus.name(), "RESOLVED", resolver));

        if (auditLogService != null) {
            auditLogService.success(AuditModule.SUPPORT, AuditAction.RESOLVE, "MySupportTicket",
                    String.valueOf(ticket.getId()), prevStatus.name(), "RESOLVED", "Resolved ticket: " + ticket.getTicketNumber());
        }

        // Notify requester
        if (ticket.getEmployee() != null) {
            userRepository.findByWorkEmail(ticket.getEmployee().getEmail()).ifPresent(u -> {
                if (notificationService != null) {
                    notificationService.sendNotification(u, "Ticket Resolved: " + ticket.getTicketNumber(),
                            "Your support ticket '" + ticket.getSubject() + "' has been marked resolved.");
                }
            });
        }

        return mapToDetailResponse(ticket);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 13. Close Ticket (POST /api/v1/support/tickets/{ticketId}/close)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional
    public SupportTicketDetailResponse closeTicket(Long ticketId, SupportTicketCloseRequest req) {
        Long tenantId = getTenantId();
        MySupportTicket ticket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if (ticket.getStatus() != SupportTicketStatus.RESOLVED) {
            throw new IllegalStateException("Only RESOLVED tickets can be closed. Current status: " + ticket.getStatus());
        }

        String closer = getCurrentUserEmail();
        LocalDateTime now = LocalDateTime.now();

        ticket.setStatus(SupportTicketStatus.CLOSED);
        ticket.setClosedBy(closer);
        ticket.setClosedAt(now);
        ticket.setClosureComment(req != null ? req.getComment() : null);
        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        statusHistoryRepository.save(new SupportTicketStatusHistory(ticket, "RESOLVED", "CLOSED", closer));

        if (auditLogService != null) {
            auditLogService.success(AuditModule.SUPPORT, AuditAction.CLOSE, "MySupportTicket",
                    String.valueOf(ticket.getId()), "RESOLVED", "CLOSED", "Closed ticket: " + ticket.getTicketNumber());
        }

        return mapToDetailResponse(ticket);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 18. Manual Escalation (POST /api/v1/support/tickets/{ticketId}/escalate)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional
    public SupportTicketDetailResponse manualEscalate(Long ticketId, SupportTicketEscalateRequest req) {
        Long tenantId = getTenantId();
        MySupportTicket ticket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if (ticket.getStatus() == SupportTicketStatus.CLOSED || ticket.getStatus() == SupportTicketStatus.REJECTED) {
            throw new IllegalStateException("Cannot escalate a CLOSED or REJECTED ticket");
        }
        if (req.getReason() == null || req.getReason().isBlank()) {
            throw new IllegalArgumentException("Escalation reason is required");
        }

        int newLevel = ticket.getEscalationLevel() + 1;
        if (escalationHistoryRepository.existsByTicketIdAndLevel(ticket.getId(), newLevel)) {
            throw new IllegalStateException("Ticket has already been escalated at level " + newLevel);
        }

        ticket.setEscalated(true);
        ticket.setEscalationLevel(newLevel);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        escalationHistoryRepository.save(new SupportEscalationHistory(ticket, newLevel, req.getReason(), "MANUAL_ESCALATION", "COMPLETED"));

        if (auditLogService != null) {
            auditLogService.success(AuditModule.SUPPORT, AuditAction.ESCALATE, "MySupportTicket",
                    String.valueOf(ticket.getId()), String.valueOf(newLevel - 1), String.valueOf(newLevel),
                    "Manual escalation of ticket: " + ticket.getTicketNumber() + ". Reason: " + req.getReason());
        }

        notifyManagers(ticket.getOrganization(), "Ticket Escalated: " + ticket.getTicketNumber(),
                "Support ticket #" + ticket.getTicketNumber() + " manually escalated to level " + newLevel + ". Reason: " + req.getReason());

        return mapToDetailResponse(ticket);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 20. Ticket Status History (GET /api/v1/support/tickets/{ticketId}/history)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SupportStatusHistoryResponse> getStatusHistory(Long ticketId) {
        Long tenantId = getTenantId();
        MySupportTicket ticket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        return statusHistoryRepository.findByTicketIdOrderByChangedAtAsc(ticket.getId()).stream()
                .map(h -> new SupportStatusHistoryResponse(h.getFromStatus(), h.getToStatus(),
                        h.getChangedBy(), formatDateTime(h.getChangedAt())))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 21. Escalation History (GET /api/v1/support/tickets/{ticketId}/escalations)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SupportEscalationHistoryResponse> getEscalationHistory(Long ticketId) {
        Long tenantId = getTenantId();
        MySupportTicket ticket = ticketRepository.findByIdAndOrganizationIdAndIsDeletedFalse(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        return escalationHistoryRepository.findByTicketIdOrderByTriggeredAtAsc(ticket.getId()).stream()
                .map(e -> new SupportEscalationHistoryResponse(e.getLevel(), e.getReason(),
                        formatDateTime(e.getTriggeredAt()), e.getAction(), e.getStatus()))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 22. Dashboard Summary (GET /api/v1/support/dashboard)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public SupportDashboardSummaryResponse getDashboardSummary() {
        Long tenantId = getTenantId();

        long total = ticketRepository.countByOrganizationIdAndIsDeletedFalse(tenantId);
        long newCount = ticketRepository.countByOrganizationIdAndStatusAndIsDeletedFalse(tenantId, SupportTicketStatus.NEW);
        long inProgress = ticketRepository.countByOrganizationIdAndStatusAndIsDeletedFalse(tenantId, SupportTicketStatus.IN_PROGRESS);
        long resolved = ticketRepository.countByOrganizationIdAndStatusAndIsDeletedFalse(tenantId, SupportTicketStatus.RESOLVED);
        long closed = ticketRepository.countByOrganizationIdAndStatusAndIsDeletedFalse(tenantId, SupportTicketStatus.CLOSED);
        long overdue = ticketRepository.countByOrganizationIdAndIsOverdueTrueAndIsDeletedFalse(tenantId);
        long escalated = ticketRepository.countByOrganizationIdAndIsEscalatedTrueAndIsDeletedFalse(tenantId);
        long critical = ticketRepository.countByOrganizationIdAndPriorityAndIsDeletedFalse(tenantId, SupportTicketPriority.CRITICAL);
        long high = ticketRepository.countByOrganizationIdAndPriorityAndIsDeletedFalse(tenantId, SupportTicketPriority.HIGH);

        return new SupportDashboardSummaryResponse(total, newCount, inProgress, resolved, closed, overdue, escalated, critical, high);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Notification & Mapping Helpers
    // ─────────────────────────────────────────────────────────────────────────────
    private void notifyManagers(Organization org, String title, String message) {
        if (notificationService == null || org == null) return;
        List<User> users = userRepository.findAll();
        for (User u : users) {
            if (org.getId().equals(u.getOrganizationId()) && u.getRole() != null) {
                String rName = u.getRole().getName();
                if (rName != null && (rName.toUpperCase().contains("MANAGER") || rName.toUpperCase().contains("ADMIN"))) {
                    notificationService.sendNotification(u, title, message);
                }
            }
        }
    }

    private SupportTicketDetailResponse mapToDetailResponse(MySupportTicket ticket) {
        SupportTicketDetailResponse res = new SupportTicketDetailResponse();
        res.setId(ticket.getId());
        res.setTicketNumber(ticket.getTicketNumber());
        res.setSubject(ticket.getSubject());
        res.setDescription(ticket.getDescription());

        if (ticket.getCategory() != null) {
            res.setCategory(new SupportTicketDetailResponse.CategoryInfo(ticket.getCategory().getId(), ticket.getCategory().getName()));
        }
        res.setPriority(ticket.getPriority() != null ? ticket.getPriority().name() : null);
        res.setEstimatedHours(ticket.getEstimatedHours());
        res.setActualHours(ticket.getActualHours() != null ? ticket.getActualHours() : 0.0);
        res.setSlaHours(ticket.getSlaHours());
        res.setDueDate(formatDateTime(ticket.getDueDate()));

        if (ticket.getAssignedTo() != null) {
            res.setAssignedTo(new SupportTicketDetailResponse.UserInfo(ticket.getAssignedTo().getId(),
                    ticket.getAssignedTo().getFullName() != null ? ticket.getAssignedTo().getFullName() : ticket.getAssignedTo().getEmail()));
        }

        res.setStatus(ticket.getStatus() != null ? ticket.getStatus().name() : null);

        // Check dynamically if overdue if not already closed
        boolean overdue = ticket.isOverdue();
        if (!overdue && ticket.getDueDate() != null && LocalDateTime.now().isAfter(ticket.getDueDate()) &&
                ticket.getStatus() != SupportTicketStatus.CLOSED &&
                ticket.getStatus() != SupportTicketStatus.RESOLVED &&
                ticket.getStatus() != SupportTicketStatus.REJECTED &&
                ticket.getStatus() != SupportTicketStatus.DUPLICATE) {
            overdue = true;
        }
        res.setIsOverdue(overdue);
        res.setIsEscalated(ticket.isEscalated());
        res.setEscalationLevel(ticket.getEscalationLevel());
        res.setCreatedAt(formatDateTime(ticket.getCreatedAt()));

        return res;
    }
}
