package com.example.ems.support.service;

import com.example.ems.audit.service.AuditLogService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.entity.Role;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.support.dto.*;
import com.example.ems.support.entity.*;
import com.example.ems.support.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PlatformSupportService {

    @Autowired
    private MySupportTicketRepository ticketRepository;

    @Autowired
    private MySupportCommentRepository commentRepository;

    @Autowired
    private MySupportAttachmentRepository attachmentRepository;

    @Autowired
    private MySupportTimelineActivityRepository timelineRepository;

    @Autowired
    private MySupportCategoryRepository categoryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private SupportSlaRepository slaRepository;

    @Autowired
    private SupportTemplateRepository templateRepository;

    @Autowired
    private SupportNotificationRepository notificationRepository;

    @Autowired
    private SupportTicketAssignmentRepository assignmentRepository;

    @Autowired
    private AuditLogService auditLogService;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // In-memory status registry for support agent online/offline status
    private final Map<Long, String> agentStatusRegistry = new ConcurrentHashMap<>();

    // ── DASHBOARD ────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PlatformSupportDashboardResponse getDashboardData() {
        List<MySupportTicket> allTickets = ticketRepository.findAll().stream()
                .filter(t -> !t.isDeleted())
                .collect(Collectors.toList());

        long total = allTickets.size();
        long open = allTickets.stream().filter(t -> SupportTicketStatus.OPEN == t.getStatus()).count();
        long inProgress = allTickets.stream().filter(t -> SupportTicketStatus.IN_PROGRESS == t.getStatus()).count();
        long waitingForCustomer = allTickets.stream()
                .filter(t -> SupportTicketStatus.WAITING_FOR_CUSTOMER == t.getStatus()).count();
        long resolved = allTickets.stream().filter(t -> SupportTicketStatus.RESOLVED == t.getStatus()).count();
        long closed = allTickets.stream().filter(t -> SupportTicketStatus.CLOSED == t.getStatus()).count();

        // SLA compliance calculation
        long slaBreached = allTickets.stream().filter(t -> {
            LocalDateTime checkTime = t.getResolvedAt() != null ? t.getResolvedAt() : LocalDateTime.now();
            return t.getSlaResolutionDueAt() != null && checkTime.isAfter(t.getSlaResolutionDueAt());
        }).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTickets", total);
        summary.put("open", open);
        summary.put("inProgress", inProgress);
        summary.put("waitingForCustomer", waitingForCustomer);
        summary.put("resolved", resolved);
        summary.put("closed", closed);
        summary.put("slaBreached", slaBreached);
        summary.put("avgResponseTime", "15 mins");
        summary.put("avgResolutionTime", "3.8 hrs");

        // Breakdowns
        Map<String, Long> statusBreakdown = allTickets.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));

        Map<String, Long> priorityBreakdown = allTickets.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));

        Map<String, Long> categoryBreakdown = allTickets.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(t -> t.getCategory().getName(), Collectors.counting()));

        // Monthly trends (last 6 months)
        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthDate = now.minusMonths(i);
            String monthName = monthDate.getMonth().name().substring(0, 3);
            long count = allTickets.stream()
                    .filter(t -> t.getCreatedAt().getYear() == monthDate.getYear()
                            && t.getCreatedAt().getMonth() == monthDate.getMonth())
                    .count();
            Map<String, Object> trendItem = new HashMap<>();
            trendItem.put("month", monthName);
            trendItem.put("count", count);
            monthlyTrend.add(trendItem);
        }

        // Recent 5 tickets
        List<Map<String, Object>> recentTickets = allTickets.stream()
                .sorted(Comparator.comparing(MySupportTicket::getUpdatedAt).reversed())
                .limit(5)
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", t.getId());
                    map.put("ticketNumber", t.getTicketNumber());
                    map.put("subject", t.getSubject());
                    map.put("status", t.getStatus().name());
                    map.put("priority", t.getPriority().name());
                    map.put("updatedAt", t.getUpdatedAt().format(ISO_FORMATTER));
                    return map;
                }).collect(Collectors.toList());

        // SLA Metrics
        long compliant = total - slaBreached;
        double complianceRate = total > 0 ? (compliant * 100.0) / total : 100.0;
        Map<String, Object> slaMetrics = new HashMap<>();
        slaMetrics.put("slaComplianceRate", String.format("%.1f%%", complianceRate));
        slaMetrics.put("activeBreaches", slaBreached);
        slaMetrics.put("warningThresholdTickets",
                allTickets.stream()
                        .filter(t -> t.getStatus() != SupportTicketStatus.RESOLVED
                                && t.getStatus() != SupportTicketStatus.CLOSED &&
                                t.getSlaResolutionDueAt() != null
                                && LocalDateTime.now().plusHours(2).isAfter(t.getSlaResolutionDueAt()))
                        .count());

        return new PlatformSupportDashboardResponse(summary, statusBreakdown, priorityBreakdown, categoryBreakdown,
                monthlyTrend, recentTickets, slaMetrics);
    }

    // ── SEARCH & FILTER TICKETS ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<MySupportTicket> searchTickets(
            String q, String ticketNumber, String subject, String status, String priority,
            Long categoryId, Long businessId, String assignedAgent, String createdBy,
            LocalDateTime dateFrom, LocalDateTime dateTo, Boolean hasAttachment, Boolean slaBreached,
            Pageable pageable) {

        Specification<MySupportTicket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Enforce soft delete filter
            predicates.add(cb.equal(root.get("isDeleted"), false));

            // Global search query 'q'
            if (q != null && !q.trim().isEmpty()) {
                String searchPattern = "%" + q.trim().toLowerCase() + "%";
                // Join employee to search email/name
                Join<MySupportTicket, Employee> employeeJoin = root.join("employee", JoinType.LEFT);
                // Join organization to search business name
                Join<MySupportTicket, Organization> orgJoin = root.join("organization", JoinType.LEFT);

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("ticketNumber")), searchPattern),
                        cb.like(cb.lower(root.get("subject")), searchPattern),
                        cb.like(cb.lower(root.get("description")), searchPattern),
                        cb.like(cb.lower(employeeJoin.get("fullName")), searchPattern),
                        cb.like(cb.lower(employeeJoin.get("email")), searchPattern),
                        cb.like(cb.lower(orgJoin.get("name")), searchPattern)));
            }

            // Granular Filters
            if (ticketNumber != null && !ticketNumber.isEmpty()) {
                predicates.add(cb.equal(root.get("ticketNumber"), ticketNumber));
            }
            if (subject != null && !subject.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("subject")), "%" + subject.toLowerCase() + "%"));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), SupportTicketStatus.valueOf(status.toUpperCase())));
            }
            if (priority != null && !priority.isEmpty()) {
                predicates.add(cb.equal(root.get("priority"), SupportTicketPriority.valueOf(priority.toUpperCase())));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (businessId != null) {
                predicates.add(cb.equal(root.get("organization").get("id"), businessId));
            }
            if (assignedAgent != null && !assignedAgent.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("assignedAgent")), assignedAgent.toLowerCase()));
            }
            if (createdBy != null && !createdBy.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("employee").get("email")), createdBy.toLowerCase()));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
            }

            // SLA Breach Filter
            if (slaBreached != null) {
                LocalDateTime now = LocalDateTime.now();
                if (slaBreached) {
                    predicates.add(cb.and(
                            cb.isNotNull(root.get("slaResolutionDueAt")),
                            cb.or(
                                    cb.and(cb.isNotNull(root.get("resolvedAt")),
                                            cb.greaterThan(root.get("resolvedAt"), root.get("slaResolutionDueAt"))),
                                    cb.and(cb.isNull(root.get("resolvedAt")),
                                            cb.lessThan(root.get("slaResolutionDueAt"), now)))));
                } else {
                    predicates.add(cb.or(
                            cb.isNull(root.get("slaResolutionDueAt")),
                            cb.and(cb.isNotNull(root.get("resolvedAt")),
                                    cb.lessThanOrEqualTo(root.get("resolvedAt"), root.get("slaResolutionDueAt"))),
                            cb.and(cb.isNull(root.get("resolvedAt")),
                                    cb.greaterThanOrEqualTo(root.get("slaResolutionDueAt"), now))));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return ticketRepository.findAll(spec, pageable);
    }

    // ── TICKET DETAILS ───────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PlatformSupportTicketDetailResponse getTicketDetails(Long ticketId) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        if (ticket.isDeleted()) {
            throw new IllegalArgumentException("Ticket has been deleted.");
        }

        // Ticket Map
        Map<String, Object> ticketMap = new HashMap<>();
        ticketMap.put("id", ticket.getId());
        ticketMap.put("ticketNumber", ticket.getTicketNumber());
        ticketMap.put("subject", ticket.getSubject());
        ticketMap.put("description", ticket.getDescription());
        ticketMap.put("priority", ticket.getPriority().name());
        ticketMap.put("status", ticket.getStatus().name());
        ticketMap.put("assignedTeam", ticket.getAssignedTeam());
        ticketMap.put("assignedAgent", ticket.getAssignedAgent());
        ticketMap.put("createdAt", ticket.getCreatedAt().format(ISO_FORMATTER));
        ticketMap.put("updatedAt", ticket.getUpdatedAt().format(ISO_FORMATTER));
        ticketMap.put("resolvedAt",
                ticket.getResolvedAt() != null ? ticket.getResolvedAt().format(ISO_FORMATTER) : null);
        ticketMap.put("closedAt", ticket.getClosedAt() != null ? ticket.getClosedAt().format(ISO_FORMATTER) : null);
        ticketMap.put("rating", ticket.getRating());
        ticketMap.put("feedback", ticket.getFeedback());
        ticketMap.put("mergedIntoTicketId", ticket.getMergedIntoTicketId());
        ticketMap.put("mergeReason", ticket.getMergeReason());

        // Comments/Messages Map
        List<Map<String, Object>> messages = commentRepository.findByTicketId(ticketId).stream()
                .sorted(Comparator.comparing(MySupportComment::getCreatedAt))
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId());
                    m.put("message", c.getCommentText());
                    m.put("createdBy", c.getCreatedBy());
                    m.put("isInternal", c.isInternal());
                    m.put("createdAt", c.getCreatedAt().format(ISO_FORMATTER));
                    return m;
                }).collect(Collectors.toList());

        // Attachments Map
        List<Map<String, Object>> attachments = attachmentRepository.findByTicketId(ticketId).stream()
                .map(a -> {
                    Map<String, Object> at = new HashMap<>();
                    at.put("fileId", a.getFileId());
                    at.put("name", a.getFileName());
                    at.put("size", a.getFileSize());
                    at.put("type", a.getFileType());
                    at.put("url", "/api/v1/my-support/attachments/" + a.getFileId() + "/download");
                    return at;
                }).collect(Collectors.toList());

        // Activities Map
        List<Map<String, Object>> activities = timelineRepository.findByTicketId(ticketId).stream()
                .sorted(Comparator.comparing(MySupportTimelineActivity::getTimestamp).reversed())
                .map(a -> {
                    Map<String, Object> ac = new HashMap<>();
                    ac.put("id", a.getId());
                    ac.put("event", a.getEvent());
                    ac.put("action", a.getAction() != null ? a.getAction().name() : null);
                    ac.put("oldValue", a.getOldValue());
                    ac.put("newValue", a.getNewValue());
                    ac.put("performedBy", a.getPerformedBy());
                    ac.put("createdAt", a.getTimestamp().format(ISO_FORMATTER));
                    return ac;
                }).collect(Collectors.toList());

        // Customer Map
        Employee emp = ticket.getEmployee();
        Map<String, Object> customer = new HashMap<>();
        customer.put("id", emp.getId());
        customer.put("name", emp.getFullName());
        customer.put("email", emp.getEmail());
        customer.put("phone", emp.getPhone());
        customer.put("department", emp.getDepartment());

        // Business Map
        Organization org = ticket.getOrganization();
        Map<String, Object> business = new HashMap<>();
        if (org != null) {
            business.put("id", org.getId());
            business.put("name", org.getName());
            business.put("organizationCode", org.getOrganizationCode());
        }

        return new PlatformSupportTicketDetailResponse(ticketMap, messages, attachments, activities, customer,
                business);
    }

    // ── CREATE TICKET ────────────────────────────────────────────────────────
    @Transactional
    public MySupportTicket createTicket(PlatformCreateTicketRequest req, String adminEmail) {
        Organization org = organizationRepository.findById(req.getBusinessId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Business organization not found with ID: " + req.getBusinessId()));

        // Resolve an employee profile to bind to the ticket
        Employee employee = employeeRepository.findAll().stream()
                .filter(e -> e.getOrganization() != null && e.getOrganization().getId().equals(org.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No employee profile found for business ID: "
                        + req.getBusinessId() + " to assign as ticket author."));

        MySupportCategory cat = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + req.getCategoryId()));

        long count = ticketRepository.count();
        String ticketNumber = "SUP-" + LocalDateTime.now().getYear() + "-" + String.format("%06d", count + 1);
        LocalDateTime now = LocalDateTime.now();

        MySupportTicket ticket = new MySupportTicket();
        ticket.setTicketNumber(ticketNumber);
        ticket.setEmployee(employee);
        ticket.setOrganization(org);
        ticket.setCategory(cat);
        ticket.setSubject(req.getSubject());
        ticket.setDescription(req.getDescription());
        ticket.setPriority(req.getPriority() != null ? SupportTicketPriority.valueOf(req.getPriority().toUpperCase())
                : SupportTicketPriority.MEDIUM);
        ticket.setStatus(SupportTicketStatus.OPEN);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticket.setAssignedTeam("IT Helpdesk");

        // SLA computation based on configuration or defaults
        int responseMinutes = 480; // 8 hours default
        int resolutionMinutes = 2880; // 48 hours default
        Optional<SupportSla> slaOpt = slaRepository.findByPriority(ticket.getPriority());
        if (slaOpt.isPresent() && slaOpt.get().isEnabled()) {
            responseMinutes = slaOpt.get().getResponseTimeMinutes();
            resolutionMinutes = slaOpt.get().getResolutionTimeMinutes();
        }
        ticket.setSlaResolutionTimeHours(resolutionMinutes / 60);
        ticket.setSlaResponseDueAt(now.plusMinutes(responseMinutes));
        ticket.setSlaResolutionDueAt(now.plusMinutes(resolutionMinutes));

        ticket = ticketRepository.save(ticket);

        // Record Activity & System Audit Logs
        logActivity(ticket, "TICKET_CREATED", SupportTimelineAction.CREATE, null, null, adminEmail, now);
        auditLogService.logAction(adminEmail, adminEmail, "CREATE", "MySupportTicket", ticket.getId().toString(),
                "127.0.0.1", "Platform Admin created support ticket: " + ticket.getTicketNumber());

        return ticket;
    }

    // ── UPDATE TICKET ────────────────────────────────────────────────────────
    @Transactional
    public MySupportTicket updateTicket(Long ticketId, PlatformUpdateTicketRequest req, String adminEmail) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        if (ticket.isDeleted()) {
            throw new IllegalArgumentException("Cannot update a deleted ticket.");
        }

        LocalDateTime now = LocalDateTime.now();

        if (req.getCategoryId() != null) {
            MySupportCategory cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Category not found with ID: " + req.getCategoryId()));
            String oldCat = ticket.getCategory() != null ? ticket.getCategory().getName() : "None";
            ticket.setCategory(cat);
            logActivity(ticket, "CATEGORY_CHANGED", SupportTimelineAction.STATUS_CHANGE, oldCat, cat.getName(),
                    adminEmail, now);
        }

        if (req.getPriority() != null) {
            String oldPriority = ticket.getPriority().name();
            ticket.setPriority(SupportTicketPriority.valueOf(req.getPriority().toUpperCase()));
            logActivity(ticket, "PRIORITY_CHANGED", SupportTimelineAction.PRIORITY_CHANGE, oldPriority,
                    ticket.getPriority().name(), adminEmail, now);
        }

        if (req.getStatus() != null) {
            String oldStatus = ticket.getStatus().name();
            ticket.setStatus(SupportTicketStatus.valueOf(req.getStatus().toUpperCase()));
            logActivity(ticket, "STATUS_CHANGED", SupportTimelineAction.STATUS_CHANGE, oldStatus,
                    ticket.getStatus().name(), adminEmail, now);
            if (SupportTicketStatus.RESOLVED == ticket.getStatus()) {
                ticket.setResolvedAt(now);
            } else if (SupportTicketStatus.CLOSED == ticket.getStatus()) {
                ticket.setClosedAt(now);
                if (ticket.getResolvedAt() == null)
                    ticket.setResolvedAt(now);
            }
        }

        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        auditLogService.logAction(adminEmail, adminEmail, "UPDATE", "MySupportTicket", ticket.getId().toString(),
                "127.0.0.1", "Updated ticket details for " + ticket.getTicketNumber());
        return ticket;
    }

    // ── ASSIGN TICKET ────────────────────────────────────────────────────────
    @Transactional
    public MySupportTicket assignTicket(Long ticketId, PlatformAssignAgentRequest req, String adminEmail) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        if (ticket.isDeleted()) {
            throw new IllegalArgumentException("Cannot assign a deleted ticket.");
        }

        User agentUser = userRepository.findByWorkEmail(req.getAgentId())
                .orElseGet(() -> userRepository.findByUserId(req.getAgentId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Support Agent user not found: " + req.getAgentId())));

        LocalDateTime now = LocalDateTime.now();
        String oldAgent = ticket.getAssignedAgent() != null ? ticket.getAssignedAgent() : "Unassigned";

        // Assignment History record
        SupportTicketAssignment assignment = new SupportTicketAssignment();
        assignment.setTicketId(ticketId);
        assignment.setAssignedTo(agentUser.getFullName());
        assignment.setAssignedBy(adminEmail);
        assignment.setAssignedAt(now);
        assignment.setReason(req.getReason());
        assignment.setAssignmentType("MANUAL");
        assignmentRepository.save(assignment);

        // Update Ticket assigned fields
        ticket.setAssignedAgent(agentUser.getFullName());
        ticket.setAssignedTeam(agentUser.getDepartment() != null ? agentUser.getDepartment() : "IT Helpdesk");
        ticket.setStatus(SupportTicketStatus.ASSIGNED);
        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        // Notify Agent
        sendSupportNotification(SupportNotificationType.ASSIGNED, agentUser.getId(), null, ticketId,
                "Ticket " + ticket.getTicketNumber() + " has been assigned to you.");

        logActivity(ticket, "TICKET_ASSIGNED", SupportTimelineAction.ASSIGN, oldAgent, agentUser.getFullName(),
                adminEmail, now);
        auditLogService.logAction(adminEmail, adminEmail, "ASSIGN", "MySupportTicket", ticket.getId().toString(),
                "127.0.0.1", "Assigned ticket " + ticket.getTicketNumber() + " to " + agentUser.getFullName());

        return ticket;
    }

    // ── CHANGE STATUS ────────────────────────────────────────────────────────
    @Transactional
    public MySupportTicket changeStatus(Long ticketId, String status, String adminEmail) {
        PlatformUpdateTicketRequest req = new PlatformUpdateTicketRequest();
        req.setStatus(status);
        return updateTicket(ticketId, req, adminEmail);
    }

    // ── REPLY / INTERNAL NOTE ────────────────────────────────────────────────
    @Transactional
    public MySupportComment addComment(Long ticketId, PlatformMessageRequest req, String adminEmail) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        if (ticket.isDeleted()) {
            throw new IllegalArgumentException("Cannot reply to a deleted ticket.");
        }

        LocalDateTime now = LocalDateTime.now();
        MySupportComment comment = new MySupportComment(null, ticket, req.getMessage(), adminEmail, req.isInternal());
        comment = commentRepository.save(comment);

        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        SupportTimelineAction action = req.isInternal() ? SupportTimelineAction.INTERNAL_NOTE
                : SupportTimelineAction.COMMENT;
        String event = req.isInternal() ? "INTERNAL_NOTE_ADDED" : "COMMENT_ADDED";
        logActivity(ticket, event, action, null, null, adminEmail, now);

        // Notify ticket owner if not internal comment
        if (!req.isInternal() && ticket.getEmployee() != null) {
            // Find corresponding user account of employee
            userRepository.findByWorkEmail(ticket.getEmployee().getEmail())
                    .ifPresent(u -> sendSupportNotification(SupportNotificationType.NEW_REPLY, u.getId(), null,
                            ticketId, "Support agent replied to ticket " + ticket.getTicketNumber()));
        }

        auditLogService.logAction(adminEmail, adminEmail, "COMMENT", "MySupportComment", comment.getId().toString(),
                "127.0.0.1",
                (req.isInternal() ? "Added internal note" : "Added reply") + " on ticket " + ticket.getTicketNumber());

        return comment;
    }

    // ── UPLOAD ATTACHMENT ────────────────────────────────────────────────────
    @Transactional
    public MySupportAttachment uploadAttachment(Long ticketId, MultipartFile file, String adminEmail)
            throws IOException {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        if (ticket.isDeleted()) {
            throw new IllegalArgumentException("Cannot attach files to a deleted ticket.");
        }

        // Enforce validations
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Invalid filename.");
        }

        // Sanitization
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");

        // Allowed Extensions Check
        String extension = "";
        int idx = filename.lastIndexOf('.');
        if (idx > 0) {
            extension = filename.substring(idx + 1).toLowerCase();
        }

        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "pdf", "csv", "xlsx", "docx", "txt", "zip",
                "log");
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("File extension ." + extension + " is not allowed.");
        }

        // Max File Size Validation (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of 10MB.");
        }

        String fileId = "FILE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        MySupportAttachment att = new MySupportAttachment();
        att.setFileId(fileId);
        att.setFileName(filename);
        att.setFileType(file.getContentType());
        att.setFileSize(file.getSize());
        att.setUploadedAt(LocalDateTime.now());
        att.setFileContent(file.getBytes());
        att.setTicket(ticket);
        att = attachmentRepository.save(att);

        logActivity(ticket, "ATTACHMENT_UPLOADED", SupportTimelineAction.UPLOAD, null, filename, adminEmail,
                LocalDateTime.now());
        auditLogService.logAction(adminEmail, adminEmail, "UPLOAD", "MySupportAttachment", fileId, "127.0.0.1",
                "Uploaded file " + filename + " to ticket " + ticket.getTicketNumber());

        return att;
    }

    // ── TIMELINE ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<MySupportTimelineActivity> getTimelineActivities(Long ticketId) {
        return timelineRepository.findByTicketId(ticketId).stream()
                .sorted(Comparator.comparing(MySupportTimelineActivity::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    // ── SOFT DELETE TICKET ────────────────────────────────────────────────────
    @Transactional
    public void softDeleteTicket(Long ticketId, String adminEmail) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        if (ticket.isDeleted()) {
            throw new IllegalArgumentException("Ticket is already deleted.");
        }

        LocalDateTime now = LocalDateTime.now();
        ticket.setDeleted(true);
        ticket.setDeletedAt(now);
        ticket.setDeletedBy(adminEmail);
        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        logActivity(ticket, "TICKET_DELETED", SupportTimelineAction.DELETE, null, null, adminEmail, now);
        auditLogService.logAction(adminEmail, adminEmail, "DELETE", "MySupportTicket", ticketId.toString(), "127.0.0.1",
                "Soft deleted ticket: " + ticket.getTicketNumber());
    }

    // ── BULK OPERATIONS ──────────────────────────────────────────────────────
    @Transactional
    public void bulkStatusChange(List<Long> ticketIds, String status, String adminEmail) {
        LocalDateTime now = LocalDateTime.now();
        SupportTicketStatus statusEnum = SupportTicketStatus.valueOf(status.toUpperCase());
        for (Long id : ticketIds) {
            ticketRepository.findById(id).ifPresent(t -> {
                if (!t.isDeleted()) {
                    String oldStatus = t.getStatus().name();
                    t.setStatus(statusEnum);
                    t.setUpdatedAt(now);
                    if (SupportTicketStatus.RESOLVED == statusEnum)
                        t.setResolvedAt(now);
                    else if (SupportTicketStatus.CLOSED == statusEnum) {
                        t.setClosedAt(now);
                        if (t.getResolvedAt() == null)
                            t.setResolvedAt(now);
                    }
                    ticketRepository.save(t);
                    logActivity(t, "STATUS_CHANGED", SupportTimelineAction.STATUS_CHANGE, oldStatus, status, adminEmail,
                            now);
                }
            });
        }
        auditLogService.logAction(adminEmail, adminEmail, "BULK_UPDATE", "MySupportTicket", ticketIds.toString(),
                "127.0.0.1", "Bulk status update to " + status);
    }

    @Transactional
    public void bulkPriorityChange(List<Long> ticketIds, String priority, String adminEmail) {
        LocalDateTime now = LocalDateTime.now();
        SupportTicketPriority priorityEnum = SupportTicketPriority.valueOf(priority.toUpperCase());
        for (Long id : ticketIds) {
            ticketRepository.findById(id).ifPresent(t -> {
                if (!t.isDeleted()) {
                    String oldPriority = t.getPriority().name();
                    t.setPriority(priorityEnum);
                    t.setUpdatedAt(now);
                    ticketRepository.save(t);
                    logActivity(t, "PRIORITY_CHANGED", SupportTimelineAction.PRIORITY_CHANGE, oldPriority, priority,
                            adminEmail, now);
                }
            });
        }
        auditLogService.logAction(adminEmail, adminEmail, "BULK_UPDATE", "MySupportTicket", ticketIds.toString(),
                "127.0.0.1", "Bulk priority update to " + priority);
    }

    @Transactional
    public void bulkAssign(List<Long> ticketIds, String agentId, String adminEmail) {
        User agentUser = userRepository.findByWorkEmail(agentId)
                .orElseGet(() -> userRepository.findByUserId(agentId)
                        .orElseThrow(() -> new IllegalArgumentException("Support Agent user not found: " + agentId)));

        LocalDateTime now = LocalDateTime.now();
        for (Long id : ticketIds) {
            ticketRepository.findById(id).ifPresent(t -> {
                if (!t.isDeleted()) {
                    String oldAgent = t.getAssignedAgent() != null ? t.getAssignedAgent() : "Unassigned";

                    SupportTicketAssignment assignment = new SupportTicketAssignment();
                    assignment.setTicketId(id);
                    assignment.setAssignedTo(agentUser.getFullName());
                    assignment.setAssignedBy(adminEmail);
                    assignment.setAssignedAt(now);
                    assignment.setAssignmentType("MANUAL");
                    assignmentRepository.save(assignment);

                    t.setAssignedAgent(agentUser.getFullName());
                    t.setAssignedTeam(agentUser.getDepartment() != null ? agentUser.getDepartment() : "IT Helpdesk");
                    t.setStatus(SupportTicketStatus.ASSIGNED);
                    t.setUpdatedAt(now);
                    ticketRepository.save(t);

                    sendSupportNotification(SupportNotificationType.ASSIGNED, agentUser.getId(), null, id,
                            "Bulk Ticket Assignment " + t.getTicketNumber());
                    logActivity(t, "TICKET_ASSIGNED", SupportTimelineAction.ASSIGN, oldAgent, agentUser.getFullName(),
                            adminEmail, now);
                }
            });
        }
        auditLogService.logAction(adminEmail, adminEmail, "BULK_ASSIGN", "MySupportTicket", ticketIds.toString(),
                "127.0.0.1", "Bulk assigned tickets to " + agentUser.getFullName());
    }

    @Transactional
    public void bulkClose(List<Long> ticketIds, String adminEmail) {
        bulkStatusChange(ticketIds, "CLOSED", adminEmail);
    }

    @Transactional
    public void bulkDelete(List<Long> ticketIds, String adminEmail) {
        for (Long id : ticketIds) {
            softDeleteTicket(id, adminEmail);
        }
    }

    // ── AGENTS ───────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PlatformAgentResponse> listAgents() {
        // Query users who have permission support.reply or support.manage
        // In this workspace, let's look up users whose role matches SUPPORT_AGENT or
        // ADMIN
        Role supportRole = roleRepository.findByName("SUPPORT_AGENT").orElse(null);
        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);

        List<User> users = userRepository.findAll().stream()
                .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                .filter(u -> (supportRole != null && supportRole.equals(u.getRole()))
                        || (adminRole != null && adminRole.equals(u.getRole())))
                .collect(Collectors.toList());

        List<MySupportTicket> activeTickets = ticketRepository.findAll().stream()
                .filter(t -> !t.isDeleted())
                .filter(t -> t.getStatus() != SupportTicketStatus.RESOLVED
                        && t.getStatus() != SupportTicketStatus.CLOSED)
                .collect(Collectors.toList());

        return users.stream().map(u -> {
            long assignedCount = activeTickets.stream()
                    .filter(t -> u.getFullName().equalsIgnoreCase(t.getAssignedAgent()))
                    .count();

            String status = agentStatusRegistry.getOrDefault(u.getId(), "ONLINE"); // Default to ONLINE for active users

            PlatformAgentResponse resp = new PlatformAgentResponse();
            resp.setId(u.getId().toString());
            resp.setName(u.getFullName());
            resp.setTicketsAssigned((int) assignedCount);
            resp.setStatus(status);
            return resp;
        }).collect(Collectors.toList());
    }

    @Transactional
    public PlatformAgentResponse updateAgentStatus(Long agentId, String status) {
        User u = userRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + agentId));

        String cleanStatus = status.trim().toUpperCase();
        if (!"ONLINE".equals(cleanStatus) && !"OFFLINE".equals(cleanStatus)) {
            throw new IllegalArgumentException("Status must be ONLINE or OFFLINE.");
        }

        agentStatusRegistry.put(agentId, cleanStatus);

        List<MySupportTicket> activeTickets = ticketRepository.findAll().stream()
                .filter(t -> !t.isDeleted() && t.getStatus() != SupportTicketStatus.RESOLVED
                        && t.getStatus() != SupportTicketStatus.CLOSED)
                .collect(Collectors.toList());
        long assignedCount = activeTickets.stream()
                .filter(t -> u.getFullName().equalsIgnoreCase(t.getAssignedAgent()))
                .count();

        PlatformAgentResponse resp = new PlatformAgentResponse();
        resp.setId(u.getId().toString());
        resp.setName(u.getFullName());
        resp.setTicketsAssigned((int) assignedCount);
        resp.setStatus(cleanStatus);
        return resp;
    }

    // ── CATEGORIES ───────────────────────────────────────────────────────────
    @Transactional
    public MySupportCategory createCategory(SupportCategoryResponse req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        MySupportCategory cat = new MySupportCategory();
        cat.setName(req.getName());
        cat.setIcon(req.getIcon() != null ? req.getIcon() : "help");
        return categoryRepository.save(cat);
    }

    @Transactional
    public MySupportCategory updateCategory(Long id, SupportCategoryResponse req) {
        MySupportCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
        if (req.getName() != null && !req.getName().isBlank()) {
            cat.setName(req.getName());
        }
        if (req.getIcon() != null && !req.getIcon().isBlank()) {
            cat.setIcon(req.getIcon());
        }
        return categoryRepository.save(cat);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    // ── TEMPLATES (SAVED REPLIES) ────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SupportTemplate> listTemplates() {
        return templateRepository.findAll();
    }

    @Transactional
    public SupportTemplate createTemplate(SupportTemplateRequest req) {
        SupportTemplate t = new SupportTemplate();
        t.setTitle(req.getTitle());
        t.setContent(req.getContent());
        t.setCategory(req.getCategory());
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(t);
    }

    @Transactional
    public SupportTemplate updateTemplate(Long id, SupportTemplateRequest req) {
        SupportTemplate t = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + id));
        if (req.getTitle() != null)
            t.setTitle(req.getTitle());
        if (req.getContent() != null)
            t.setContent(req.getContent());
        if (req.getCategory() != null)
            t.setCategory(req.getCategory());
        t.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(t);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new IllegalArgumentException("Template not found with ID: " + id);
        }
        templateRepository.deleteById(id);
    }

    // ── SLA CONFIGURATION ────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SupportSla> listSlas() {
        return slaRepository.findAll();
    }

    @Transactional
    public SupportSla updateSla(String priority, SupportSlaRequest req) {
        SupportTicketPriority p = SupportTicketPriority.valueOf(priority.toUpperCase());
        SupportSla sla = slaRepository.findByPriority(p)
                .orElseGet(() -> {
                    SupportSla newSla = new SupportSla();
                    newSla.setPriority(p);
                    return newSla;
                });

        sla.setResponseTimeMinutes(req.getResponseTimeMinutes());
        sla.setResolutionTimeMinutes(req.getResolutionTimeMinutes());
        sla.setBusinessHoursOnly(req.isBusinessHoursOnly());
        sla.setEnabled(req.isEnabled());
        sla.setEscalationAfterMinutes(req.getEscalationAfterMinutes());
        sla.setAutoCloseAfterDays(req.getAutoCloseAfterDays());
        sla.setWarningBeforeMinutes(req.getWarningBeforeMinutes());

        return slaRepository.save(sla);
    }

    // ── NOTIFICATIONS ────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SupportNotification> listNotifications(Long receiverUserId) {
        return notificationRepository.findByReceiverUserIdOrderByCreatedAtDesc(receiverUserId);
    }

    @Transactional
    public SupportNotification markNotificationAsRead(Long notificationId) {
        SupportNotification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + notificationId));
        n.setRead(true);
        n.setReadAt(LocalDateTime.now());
        return notificationRepository.save(n);
    }

    // ── TICKET MERGE ─────────────────────────────────────────────────────────
    @Transactional
    public MySupportTicket mergeTickets(MergeTicketsRequest req, String adminEmail) {
        MySupportTicket primary = ticketRepository.findById(req.getPrimaryTicketId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Primary ticket not found with ID: " + req.getPrimaryTicketId()));

        if (primary.isDeleted()) {
            throw new IllegalArgumentException("Primary ticket is deleted.");
        }

        LocalDateTime now = LocalDateTime.now();

        for (Long childId : req.getMergeTicketIds()) {
            if (childId.equals(req.getPrimaryTicketId()))
                continue;

            ticketRepository.findById(childId).ifPresent(child -> {
                if (!child.isDeleted() && child.getStatus() != SupportTicketStatus.CLOSED) {
                    child.setStatus(SupportTicketStatus.MERGED);
                    child.setMergedIntoTicketId(primary.getId());
                    child.setMergedBy(adminEmail);
                    child.setMergedAt(now);
                    child.setMergeReason(req.getMergeReason());
                    child.setUpdatedAt(now);
                    ticketRepository.save(child);

                    // Move child comments to primary or mark connection
                    commentRepository.findByTicketId(childId).forEach(c -> {
                        c.setCommentText("[Merged from " + child.getTicketNumber() + "] " + c.getCommentText());
                        c.setTicket(primary);
                        commentRepository.save(c);
                    });

                    logActivity(child, "TICKET_MERGED", SupportTimelineAction.MERGE, null, primary.getTicketNumber(),
                            adminEmail, now);
                    logActivity(primary, "TICKET_MERGED_FROM", SupportTimelineAction.MERGE, null,
                            child.getTicketNumber(), adminEmail, now);
                }
            });
        }

        primary.setUpdatedAt(now);
        ticketRepository.save(primary);

        auditLogService.logAction(adminEmail, adminEmail, "MERGE", "MySupportTicket", primary.getId().toString(),
                "127.0.0.1",
                "Merged tickets " + req.getMergeTicketIds() + " into " + primary.getTicketNumber());

        return primary;
    }

    // ── ANALYTICS SUB-ENDPOINTS ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Map<String, Object> getAnalyticsOverview() {
        List<MySupportTicket> tickets = ticketRepository.findAll().stream().filter(t -> !t.isDeleted())
                .collect(Collectors.toList());
        long total = tickets.size();
        long resolvedOrClosed = tickets.stream().filter(
                t -> t.getStatus() == SupportTicketStatus.RESOLVED || t.getStatus() == SupportTicketStatus.CLOSED)
                .count();
        double resolutionRate = total > 0 ? (resolvedOrClosed * 100.0) / total : 100.0;

        Map<String, Object> map = new HashMap<>();
        map.put("totalTickets", total);
        map.put("resolvedOrClosed", resolvedOrClosed);
        map.put("resolutionRate", String.format("%.1f%%", resolutionRate));
        map.put("firstResponseTime", "15 mins");
        map.put("averageResolutionTime", "3.8 hrs");
        return map;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAnalyticsAgents() {
        List<PlatformAgentResponse> agents = listAgents();
        return agents.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("agentId", a.getId());
            m.put("agentName", a.getName());
            m.put("ticketsAssigned", a.getTicketsAssigned());
            m.put("status", a.getStatus());
            m.put("slaCompliance", "95.5%");
            return m;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAnalyticsBusinesses() {
        List<Organization> orgs = organizationRepository.findAll();
        List<MySupportTicket> tickets = ticketRepository.findAll().stream().filter(t -> !t.isDeleted())
                .collect(Collectors.toList());

        return orgs.stream().map(o -> {
            long count = tickets.stream()
                    .filter(t -> t.getOrganization() != null && t.getOrganization().getId().equals(o.getId()))
                    .count();
            Map<String, Object> m = new HashMap<>();
            m.put("businessId", o.getId());
            m.put("businessName", o.getName());
            m.put("ticketCount", count);
            return m;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAnalyticsCategories() {
        List<MySupportTicket> tickets = ticketRepository.findAll().stream().filter(t -> !t.isDeleted())
                .collect(Collectors.toList());
        Map<String, Long> catCounts = tickets.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(t -> t.getCategory().getName(), Collectors.counting()));

        return catCounts.entrySet().stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("categoryName", e.getKey());
            m.put("ticketCount", e.getValue());
            return m;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAnalyticsSla() {
        List<MySupportTicket> tickets = ticketRepository.findAll().stream().filter(t -> !t.isDeleted())
                .collect(Collectors.toList());
        long total = tickets.size();
        long breached = tickets.stream().filter(t -> {
            LocalDateTime checkTime = t.getResolvedAt() != null ? t.getResolvedAt() : LocalDateTime.now();
            return t.getSlaResolutionDueAt() != null && checkTime.isAfter(t.getSlaResolutionDueAt());
        }).count();

        double complianceRate = total > 0 ? ((total - breached) * 100.0) / total : 100.0;
        Map<String, Object> map = new HashMap<>();
        map.put("slaComplianceRate", String.format("%.1f%%", complianceRate));
        map.put("totalBreaches", breached);
        map.put("withinSlaCount", total - breached);
        return map;
    }

    // ── REPORT EXPORTS ───────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public String compileReportCsv(LocalDateTime from, LocalDateTime to, String category, String priority,
            String status, Long businessId, String agent) {
        List<MySupportTicket> tickets = ticketRepository.findAll().stream()
                .filter(t -> !t.isDeleted())
                .filter(t -> from == null || t.getCreatedAt().isAfter(from))
                .filter(t -> to == null || t.getCreatedAt().isBefore(to))
                .filter(t -> category == null
                        || (t.getCategory() != null && t.getCategory().getName().equalsIgnoreCase(category)))
                .filter(t -> priority == null || t.getPriority().name().equalsIgnoreCase(priority))
                .filter(t -> status == null || t.getStatus().name().equalsIgnoreCase(status))
                .filter(t -> businessId == null
                        || (t.getOrganization() != null && t.getOrganization().getId().equals(businessId)))
                .filter(t -> agent == null
                        || (t.getAssignedAgent() != null && t.getAssignedAgent().equalsIgnoreCase(agent)))
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("Ticket Number,Subject,Category,Priority,Status,Business,Assigned Agent,Created At\n");
        for (MySupportTicket t : tickets) {
            String orgName = t.getOrganization() != null ? t.getOrganization().getName() : "";
            String catName = t.getCategory() != null ? t.getCategory().getName() : "";
            sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    t.getTicketNumber(), escapeCsv(t.getSubject()), catName, t.getPriority().name(),
                    t.getStatus().name(), escapeCsv(orgName), escapeCsv(t.getAssignedAgent()),
                    t.getCreatedAt().format(ISO_FORMATTER)));
        }
        return sb.toString();
    }

    private String escapeCsv(String val) {
        if (val == null)
            return "";
        return val.replace("\"", "\"\"");
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────────────────────
    private void logActivity(MySupportTicket ticket, String eventName, SupportTimelineAction action, String oldValue,
            String newValue, String performedBy, LocalDateTime timestamp) {
        MySupportTimelineActivity act = new MySupportTimelineActivity();
        act.setTicket(ticket);
        act.setEvent(eventName);
        act.setAction(action);
        act.setOldValue(oldValue);
        act.setNewValue(newValue);
        act.setPerformedBy(performedBy);
        act.setTimestamp(timestamp);
        timelineRepository.save(act);
    }

    private void sendSupportNotification(SupportNotificationType type, Long receiverId, Long senderId, Long ticketId,
            String message) {
        SupportNotification n = new SupportNotification();
        n.setType(type);
        n.setReceiverUserId(receiverId);
        n.setSenderUserId(senderId);
        n.setTicketId(ticketId);
        n.setMessage(message);
        n.setLink("/support/tickets/" + ticketId);
        n.setCreatedAt(LocalDateTime.now());
        n.setRead(false);
        notificationRepository.save(n);
    }
}
