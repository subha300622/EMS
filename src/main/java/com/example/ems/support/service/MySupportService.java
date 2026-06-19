package com.example.ems.support.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.support.dto.*;
import com.example.ems.support.entity.*;
import com.example.ems.support.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MySupportService {

    @Autowired
    private MySupportCategoryRepository categoryRepository;

    @Autowired
    private MySupportSubCategoryRepository subCategoryRepository;

    @Autowired
    private MySupportTicketRepository ticketRepository;

    @Autowired
    private MySupportCommentRepository commentRepository;

    @Autowired
    private MySupportAttachmentRepository attachmentRepository;

    @Autowired
    private MySupportTimelineActivityRepository timelineRepository;

    @Autowired
    private MyKnowledgeBaseArticleRepository articleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Transactional
    public void seedSupportData(String email) {
        if (categoryRepository.count() == 0) {
            MySupportCategory itSupport = new MySupportCategory();
            itSupport.setName("IT Support");
            itSupport.setIcon("computer");
            itSupport = categoryRepository.save(itSupport);

            MySupportSubCategory hardware = new MySupportSubCategory();
            hardware.setName("Hardware");
            hardware.setCategory(itSupport);
            subCategoryRepository.save(hardware);

            MySupportSubCategory software = new MySupportSubCategory();
            software.setName("Software");
            software.setCategory(itSupport);
            subCategoryRepository.save(software);

            MySupportCategory hrSupport = new MySupportCategory();
            hrSupport.setName("HR Support");
            hrSupport.setIcon("people");
            hrSupport = categoryRepository.save(hrSupport);

            MySupportSubCategory leaveIssue = new MySupportSubCategory();
            leaveIssue.setName("Leave Issue");
            leaveIssue.setCategory(hrSupport);
            subCategoryRepository.save(leaveIssue);

            MySupportSubCategory payrollQuery = new MySupportSubCategory();
            payrollQuery.setName("Payroll Query");
            payrollQuery.setCategory(hrSupport);
            subCategoryRepository.save(payrollQuery);
        }

        if (articleRepository.count() == 0) {
            MyKnowledgeBaseArticle resetVpn = new MyKnowledgeBaseArticle();
            resetVpn.setTitle("How to reset VPN connection");
            resetVpn.setContent("To reset your VPN connection, disconnect from the client, clear your browser cache, and reconnect using your primary employee credentials. If it still fails, contact IT Support.");
            resetVpn.setCategory("IT Support");
            resetVpn.setViews(1200);
            resetVpn.setHelpfulCount(1104);
            resetVpn.setNotHelpfulCount(96);
            articleRepository.save(resetVpn);

            MyKnowledgeBaseArticle payrollArticle = new MyKnowledgeBaseArticle();
            payrollArticle.setTitle("When is monthly payroll processed?");
            payrollArticle.setContent("Payroll is typically processed on the 25th of each month. If the 25th falls on a weekend, it will be processed on the preceding Friday.");
            payrollArticle.setCategory("HR Support");
            payrollArticle.setViews(500);
            payrollArticle.setHelpfulCount(480);
            payrollArticle.setNotHelpfulCount(20);
            articleRepository.save(payrollArticle);
        }

        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        if (employee != null && ticketRepository.findByEmployeeEmail(email).isEmpty() && ticketRepository.findByTicketNumber("SUP-2026-0001").isEmpty()) {
            List<MySupportCategory> cats = categoryRepository.findAll();
            MySupportCategory itSupport = cats.stream().filter(c -> c.getName().equals("IT Support")).findFirst().orElse(null);
            MySupportCategory hrSupport = cats.stream().filter(c -> c.getName().equals("HR Support")).findFirst().orElse(null);
            List<MySupportSubCategory> subCats = subCategoryRepository.findAll();
            MySupportSubCategory hardware = subCats.stream().filter(s -> s.getName().equals("Hardware")).findFirst().orElse(null);
            MySupportSubCategory software = subCats.stream().filter(s -> s.getName().equals("Software")).findFirst().orElse(null);
            MySupportSubCategory leaveIssue = subCats.stream().filter(s -> s.getName().equals("Leave Issue")).findFirst().orElse(null);
            MySupportSubCategory payrollQuery = subCats.stream().filter(s -> s.getName().equals("Payroll Query")).findFirst().orElse(null);
            LocalDateTime now = LocalDateTime.now();

            createSeededTicket("SUP-2026-0001", employee, itSupport, hardware, "Laptop display screen flickering", "Screen flickers randomly", "HIGH", "OPEN", now.minusDays(5), null, null, 32, null, null);
            createSeededTicket("SUP-2026-0002", employee, itSupport, software, "Unable to access GitLab", "Getting 403 forbidden error on repository clone", "MEDIUM", "OPEN", now.minusHours(1), null, null, 48, null, null);
            createSeededTicket("SUP-2026-0003", employee, hrSupport, leaveIssue, "Correction in sick leave balance", "Balance shows 2 instead of 5 days", "LOW", "OPEN", now.minusHours(2), null, null, 72, null, null);
            createSeededTicket("SUP-2026-0004", employee, itSupport, software, "Docker service fails on startup", "Docker daemon fails with context deadline exceeded", "HIGH", "IN_PROGRESS", now.minusDays(4), null, null, 32, null, null);
            createSeededTicket("SUP-2026-0005", employee, hrSupport, payrollQuery, "Tax deduction query on payslip", "Form 16 mapping discrepancy", "MEDIUM", "IN_PROGRESS", now.minusHours(3), null, null, 48, null, null);
            createSeededTicket("SUP-2026-0006", employee, itSupport, hardware, "External monitor not working", "HDMI port issue", "HIGH", "RESOLVED", now.minusDays(10), now.minusDays(10).plusHours(10), null, 32, 4, "Prompt support");
            createSeededTicket("SUP-2026-0007", employee, itSupport, software, "IntelliJ license expired", "Need license renewal", "HIGH", "RESOLVED", now.minusDays(9), now.minusDays(9).plusHours(12), null, 32, 5, "Instant fix");
            createSeededTicket("SUP-2026-0008", employee, hrSupport, leaveIssue, "Maternity leave apply issue", "Getting system validation error", "HIGH", "RESOLVED", now.minusDays(8), now.minusDays(8).plusHours(14), null, 32, 4, "Resolved quickly");
            createSeededTicket("SUP-2026-0009", employee, hrSupport, payrollQuery, "PF account transfer status", "PF UAN mapping check", "HIGH", "RESOLVED", now.minusDays(7), now.minusDays(7).plusHours(15), null, 32, 4, "Good assistance");
            createSeededTicket("SUP-2026-0010", employee, itSupport, hardware, "Keyboard replacement request", "Some keys not working", "HIGH", "RESOLVED", now.minusDays(6), now.minusDays(6).plusHours(16), null, 32, 5, "Perfect");
            createSeededTicket("SUP-2026-0011", employee, itSupport, software, "VPN connection fails", "Unable to log in to corporate network", "HIGH", "RESOLVED", now.minusDays(5), now.minusDays(5).plusHours(18), null, 32, 4, "Fine");
            createSeededTicket("SUP-2026-0012", employee, hrSupport, leaveIssue, "Loss of pay query", "Incorrect LOP marked for May", "HIGH", "RESOLVED", now.minusDays(4), now.minusDays(4).plusHours(20), null, 32, 5, "Very helpful");
            createSeededTicket("SUP-2026-0013", employee, hrSupport, payrollQuery, "Reimbursement claims pending", "Internet allowance bill validation", "HIGH", "RESOLVED", now.minusDays(3), now.minusDays(3).plusHours(22), null, 32, 4, "Done");
            createSeededTicket("SUP-2026-0014", employee, itSupport, hardware, "Mouse pointer jumping", "Bluetooth mouse lags", "HIGH", "CLOSED", now.minusDays(12), now.minusDays(12).plusHours(25), now.minusDays(12).plusHours(27), 32, 5, "Excellent work");
            createSeededTicket("SUP-2026-0015", employee, hrSupport, payrollQuery, "Incorrect bank details in HRMS", "Need to update bank account", "HIGH", "CLOSED", now.minusDays(11), now.minusDays(11).plusHours(28), now.minusDays(11).plusHours(30), 32, 5, "Resolved, thank you");
        }
    }

    private void createSeededTicket(String number, Employee employee, MySupportCategory cat, MySupportSubCategory subCat,
                                    String subject, String description, String priority, String status,
                                    LocalDateTime createdAt, LocalDateTime resolvedAt, LocalDateTime closedAt,
                                    int slaHours, Integer rating, String feedback) {
        MySupportTicket t = new MySupportTicket();
        t.setTicketNumber(number);
        t.setEmployee(employee);
        t.setCategory(cat);
        t.setSubCategory(subCat);
        t.setSubject(subject);
        t.setDescription(description);
        t.setPriority(priority);
        t.setStatus(status);
        t.setCreatedAt(createdAt);
        t.setUpdatedAt(closedAt != null ? closedAt : (resolvedAt != null ? resolvedAt : createdAt));
        t.setResolvedAt(resolvedAt);
        t.setClosedAt(closedAt);
        t.setSlaResolutionTimeHours(slaHours);
        int responseHours = priority.equals("HIGH") ? 2 : (priority.equals("CRITICAL") ? 1 : (priority.equals("MEDIUM") ? 8 : 24));
        t.setSlaResponseDueAt(createdAt.plusHours(responseHours));
        t.setSlaResolutionDueAt(createdAt.plusHours(slaHours));
        t.setRating(rating);
        t.setFeedback(feedback);
        t.setAssignedAgent("Agent Smith");
        t.setAssignedTeam(cat.getName().equals("IT Support") ? "IT Helpdesk" : "HR Operations");
        t = ticketRepository.save(t);

        MySupportTimelineActivity act = new MySupportTimelineActivity();
        act.setTicket(t); act.setEvent("TICKET_CREATED"); act.setPerformedBy(employee.getEmail()); act.setTimestamp(createdAt);
        timelineRepository.save(act);
        if (resolvedAt != null) {
            MySupportTimelineActivity actRes = new MySupportTimelineActivity();
            actRes.setTicket(t); actRes.setEvent("TICKET_RESOLVED"); actRes.setPerformedBy("Agent Smith"); actRes.setTimestamp(resolvedAt);
            timelineRepository.save(actRes);
        }
        if (closedAt != null) {
            MySupportTimelineActivity actClose = new MySupportTimelineActivity();
            actClose.setTicket(t); actClose.setEvent("TICKET_CLOSED"); actClose.setPerformedBy(employee.getEmail()); actClose.setTimestamp(closedAt);
            timelineRepository.save(actClose);
        }
    }

    // ── EXISTING METHODS ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SupportDashboardResponse getDashboard(String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        List<MySupportTicket> tickets = ticketRepository.findByEmployeeEmail(email);
        long open = tickets.stream().filter(t -> "OPEN".equals(t.getStatus())).count();
        long inProgress = tickets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long resolved = tickets.stream().filter(t -> "RESOLVED".equals(t.getStatus())).count();
        long closed = tickets.stream().filter(t -> "CLOSED".equals(t.getStatus())).count();
        long total = tickets.size();

        List<MySupportTicket> resolvedOrClosed = tickets.stream()
                .filter(t -> ("RESOLVED".equals(t.getStatus()) || "CLOSED".equals(t.getStatus())) && t.getResolvedAt() != null)
                .collect(Collectors.toList());
        long totalHours = resolvedOrClosed.stream()
                .mapToLong(t -> java.time.Duration.between(t.getCreatedAt(), t.getResolvedAt()).toHours()).sum();
        long avgHours = resolvedOrClosed.isEmpty() ? 0 : totalHours / resolvedOrClosed.size();

        long breached = tickets.stream().filter(t -> {
            LocalDateTime checkTime = t.getResolvedAt() != null ? t.getResolvedAt() : LocalDateTime.now();
            return t.getSlaResolutionDueAt() != null && checkTime.isAfter(t.getSlaResolutionDueAt());
        }).count();

        List<SupportDashboardResponse.RecentTicketDto> recent = tickets.stream()
                .sorted(Comparator.comparing(MySupportTicket::getUpdatedAt).reversed()).limit(5)
                .map(t -> new SupportDashboardResponse.RecentTicketDto(t.getId(), t.getTicketNumber(), t.getSubject(),
                        t.getStatus(), t.getPriority(), t.getUpdatedAt().format(ISO_FORMATTER)))
                .collect(Collectors.toList());

        return new SupportDashboardResponse(
                new SupportDashboardResponse.EmployeeDto(emp.getId(), emp.getEmployeeId(), emp.getFullName()),
                new SupportDashboardResponse.SummaryDto(total, open, inProgress, resolved, closed, avgHours),
                new SupportDashboardResponse.SlaDto(total - breached, breached),
                recent);
    }

    @Transactional
    public CreateTicketResponse createTicket(String email, CreateTicketRequest req) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found."));
        MySupportCategory cat = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + req.getCategoryId()));
        MySupportSubCategory subCat = subCategoryRepository.findById(req.getSubCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Subcategory not found with ID: " + req.getSubCategoryId()));

        long count = ticketRepository.count();
        String ticketNumber = "SUP-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", count + 1);
        LocalDateTime now = LocalDateTime.now();

        MySupportTicket ticket = new MySupportTicket();
        ticket.setTicketNumber(ticketNumber);
        ticket.setEmployee(emp);
        ticket.setCategory(cat);
        ticket.setSubCategory(subCat);
        ticket.setSubject(req.getSubject());
        ticket.setDescription(req.getDescription());
        ticket.setPriority(req.getPriority() != null ? req.getPriority() : "MEDIUM");
        ticket.setStatus("OPEN");
        ticket.setPreferredContactMethod(req.getPreferredContactMethod() != null ? req.getPreferredContactMethod() : "EMAIL");
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticket.setAssignedTeam(cat.getName().equals("IT Support") ? "IT Helpdesk" : "HR Operations");

        int responseHours = 8, resolutionHours = 48;
        if ("HIGH".equalsIgnoreCase(ticket.getPriority()))          { responseHours = 2;  resolutionHours = 32; }
        else if ("CRITICAL".equalsIgnoreCase(ticket.getPriority())) { responseHours = 1;  resolutionHours = 12; }
        else if ("LOW".equalsIgnoreCase(ticket.getPriority()))      { responseHours = 24; resolutionHours = 72; }
        ticket.setSlaResolutionTimeHours(resolutionHours);
        ticket.setSlaResponseDueAt(now.plusHours(responseHours));
        ticket.setSlaResolutionDueAt(now.plusHours(resolutionHours));
        ticket = ticketRepository.save(ticket);

        if (req.getAttachments() != null) {
            final MySupportTicket savedTicket = ticket;
            req.getAttachments().forEach(ref -> attachmentRepository.findById(ref.getFileId()).ifPresent(att -> {
                att.setTicket(savedTicket);
                attachmentRepository.save(att);
            }));
        }

        MySupportTimelineActivity activity = new MySupportTimelineActivity();
        activity.setTicket(ticket); activity.setEvent("TICKET_CREATED"); activity.setPerformedBy(email); activity.setTimestamp(now);
        timelineRepository.save(activity);

        return new CreateTicketResponse(ticket.getId(), ticket.getTicketNumber(), ticket.getStatus(), ticket.getPriority(),
                ticket.getCreatedAt().format(ISO_FORMATTER), ticket.getSlaResponseDueAt().format(ISO_FORMATTER),
                ticket.getSlaResolutionDueAt().format(ISO_FORMATTER), "Support ticket created successfully");
    }

    @Transactional(readOnly = true)
    public MyTicketsResponse getMyTickets(String email, String status, String priority, Long categoryId, String search, Pageable pageable) {
        Page<MySupportTicket> page = ticketRepository.findByFilters(email, status, priority, categoryId, search, pageable);
        List<MyTicketsResponse.TicketListItem> items = page.getContent().stream()
                .map(t -> new MyTicketsResponse.TicketListItem(t.getId(), t.getTicketNumber(), t.getSubject(),
                        t.getCategory() != null ? t.getCategory().getName() : null,
                        t.getPriority(), t.getStatus(), t.getAssignedTeam(),
                        t.getCreatedAt().format(ISO_FORMATTER), t.getUpdatedAt().format(ISO_FORMATTER)))
                .collect(Collectors.toList());
        return new MyTicketsResponse(items, new MyTicketsResponse.PaginationDto(
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.hasNext()));
    }

    @Transactional(readOnly = true)
    public TicketDetailsResponse getTicketDetails(String email, Long ticketId) {
        MySupportTicket t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        if (!t.getEmployee().getEmail().equals(email))
            throw new IllegalArgumentException("Access Denied: You do not own this ticket.");

        List<TicketDetailsResponse.AttachmentDto> attachmentDtos = attachmentRepository.findByTicketId(ticketId).stream()
                .map(a -> new TicketDetailsResponse.AttachmentDto(a.getFileId(), a.getFileName()))
                .collect(Collectors.toList());
        LocalDateTime checkTime = t.getResolvedAt() != null ? t.getResolvedAt() : LocalDateTime.now();
        String slaStatus = (t.getSlaResolutionDueAt() != null && checkTime.isAfter(t.getSlaResolutionDueAt())) ? "BREACHED" : "WITHIN_SLA";

        return new TicketDetailsResponse(t.getId(), t.getTicketNumber(), t.getSubject(), t.getDescription(),
                t.getCategory() != null ? t.getCategory().getName() : null, t.getPriority(), t.getStatus(),
                new TicketDetailsResponse.CreatedByDto(t.getEmployee().getId(), t.getEmployee().getFullName()),
                new TicketDetailsResponse.AssignedToDto(t.getAssignedTeam(), t.getAssignedAgent()),
                attachmentDtos,
                new TicketDetailsResponse.SlaDetailDto(
                        t.getSlaResponseDueAt() != null ? t.getSlaResponseDueAt().format(ISO_FORMATTER) : null,
                        t.getSlaResolutionDueAt() != null ? t.getSlaResolutionDueAt().format(ISO_FORMATTER) : null,
                        slaStatus));
    }

    @Transactional
    public AddCommentResponse addComment(String email, Long ticketId, AddCommentRequest req) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        if (!ticket.getEmployee().getEmail().equals(email))
            throw new IllegalArgumentException("Access Denied: You cannot comment on this ticket.");

        LocalDateTime now = LocalDateTime.now();
        MySupportComment comment = new MySupportComment();
        comment.setTicket(ticket); comment.setCommentText(req.getCommentText()); comment.setCreatedBy(email); comment.setCreatedAt(now);
        comment = commentRepository.save(comment);

        if (req.getAttachments() != null) {
            final MySupportTicket t = ticket;
            req.getAttachments().forEach(fileId -> attachmentRepository.findById(fileId).ifPresent(att -> { att.setTicket(t); attachmentRepository.save(att); }));
        }

        MySupportTimelineActivity act = new MySupportTimelineActivity();
        act.setTicket(ticket); act.setEvent("COMMENT_ADDED"); act.setPerformedBy(email); act.setTimestamp(now);
        timelineRepository.save(act);
        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        return new AddCommentResponse(comment.getId(), comment.getCreatedBy(), comment.getCreatedAt().format(ISO_FORMATTER), "Comment added successfully");
    }

    @Transactional
    public EscalateTicketResponse escalateTicket(String email, Long ticketId, EscalateTicketRequest req) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        if (!ticket.getEmployee().getEmail().equals(email))
            throw new IllegalArgumentException("Access Denied: You cannot escalate this ticket.");

        LocalDateTime now = LocalDateTime.now();
        String previousPriority = ticket.getPriority();
        ticket.setOldPriority(previousPriority);
        ticket.setPriority("CRITICAL");
        ticket.setEscalationReason(req.getEscalationReason());
        ticket.setUpdatedAt(now);
        ticket.setSlaResponseDueAt(now.plusHours(1));
        ticket.setSlaResolutionDueAt(now.plusHours(12));
        ticket.setSlaResolutionTimeHours(12);
        ticketRepository.save(ticket);

        MySupportTimelineActivity act = new MySupportTimelineActivity();
        act.setTicket(ticket); act.setEvent("TICKET_ESCALATED"); act.setPerformedBy(email); act.setTimestamp(now);
        timelineRepository.save(act);

        return new EscalateTicketResponse(ticket.getId(), ticket.getTicketNumber(), previousPriority,
                ticket.getPriority(), now.format(ISO_FORMATTER), "Ticket escalated to CRITICAL priority");
    }

    @Transactional
    public CloseTicketResponse closeTicket(String email, Long ticketId, CloseTicketRequest req) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        if (!ticket.getEmployee().getEmail().equals(email))
            throw new IllegalArgumentException("Access Denied: You cannot close this ticket.");

        LocalDateTime now = LocalDateTime.now();
        ticket.setStatus("CLOSED");
        ticket.setClosedAt(now);
        if (ticket.getResolvedAt() == null) ticket.setResolvedAt(now);
        if (req.getRating() != null) ticket.setRating(req.getRating());
        if (req.getFeedback() != null) ticket.setFeedback(req.getFeedback());
        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        MySupportTimelineActivity act = new MySupportTimelineActivity();
        act.setTicket(ticket); act.setEvent("TICKET_CLOSED"); act.setPerformedBy(email); act.setTimestamp(now);
        timelineRepository.save(act);

        return new CloseTicketResponse(ticket.getId(), ticket.getTicketNumber(), ticket.getStatus(),
                ticket.getClosedAt().format(ISO_FORMATTER), ticket.getRating(), ticket.getFeedback(), "Ticket closed successfully");
    }

    @Transactional(readOnly = true)
    public List<SupportCategoryResponse> getCategories() {
        return categoryRepository.findAll().stream().map(c -> {
            List<SupportCategoryResponse.SubCategoryDto> subDtos = subCategoryRepository.findByCategory(c).stream()
                    .map(s -> new SupportCategoryResponse.SubCategoryDto(s.getId(), s.getName()))
                    .collect(Collectors.toList());
            return new SupportCategoryResponse(c.getId(), c.getName(), c.getIcon(), subDtos);
        }).collect(Collectors.toList());
    }

    @Transactional
    public AttachmentUploadResponse uploadAttachment(MultipartFile file) throws IOException {
        String fileId = "FILE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        MySupportAttachment att = new MySupportAttachment();
        att.setFileId(fileId);
        att.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "attachment");
        att.setFileType(file.getContentType());
        att.setFileSize(file.getSize());
        att.setUploadedAt(LocalDateTime.now());
        att.setFileContent(file.getBytes()); // store raw bytes for download
        att = attachmentRepository.save(att);
        return new AttachmentUploadResponse(att.getFileId(), att.getFileName(), att.getFileType(), att.getFileSize(), att.getUploadedAt().format(ISO_FORMATTER));
    }

    @Transactional(readOnly = true)
    public TicketTimelineResponse getTicketTimeline(String email, Long ticketId) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        if (!ticket.getEmployee().getEmail().equals(email))
            throw new IllegalArgumentException("Access Denied: You cannot view this ticket's timeline.");

        List<TicketTimelineResponse.TimelineActivityDto> actDtos = timelineRepository.findByTicketId(ticketId).stream()
                .sorted(Comparator.comparing(MySupportTimelineActivity::getTimestamp))
                .map(a -> new TicketTimelineResponse.TimelineActivityDto(a.getId(), a.getEvent(), a.getPerformedBy(), a.getTimestamp().format(ISO_FORMATTER)))
                .collect(Collectors.toList());
        return new TicketTimelineResponse(ticket.getId(), ticket.getTicketNumber(), actDtos);
    }

    @Transactional(readOnly = true)
    public FAQSearchResponse searchFAQ(String keyword) {
        List<MyKnowledgeBaseArticle> list = (keyword == null || keyword.trim().isEmpty())
                ? articleRepository.findAll()
                : articleRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);
        return new FAQSearchResponse(mapArticles(list));
    }

    // ── NEW METHODS ───────────────────────────────────────────────────────────

    /** GET /tickets/{ticketId}/comments */
    @Transactional(readOnly = true)
    public GetCommentsResponse getComments(String email, Long ticketId) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        if (!ticket.getEmployee().getEmail().equals(email))
            throw new IllegalArgumentException("Access Denied: You do not own this ticket.");

        List<GetCommentsResponse.CommentDto> dtos = commentRepository.findByTicketId(ticketId).stream()
                .sorted(Comparator.comparing(MySupportComment::getCreatedAt))
                .map(c -> new GetCommentsResponse.CommentDto(c.getId(), c.getCommentText(), c.getCreatedBy(), c.getCreatedAt().format(ISO_FORMATTER)))
                .collect(Collectors.toList());
        return new GetCommentsResponse(ticket.getId(), ticket.getTicketNumber(), dtos);
    }

    /** PUT /tickets/{ticketId} */
    @Transactional
    public UpdateTicketResponse updateTicket(String email, Long ticketId, UpdateTicketRequest req) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        if (!ticket.getEmployee().getEmail().equals(email))
            throw new IllegalArgumentException("Access Denied: You do not own this ticket.");
        if (!"OPEN".equalsIgnoreCase(ticket.getStatus()))
            throw new IllegalArgumentException("Only OPEN tickets can be edited (current status: " + ticket.getStatus() + ").");

        LocalDateTime now = LocalDateTime.now();
        if (req.getSubject() != null && !req.getSubject().isBlank()) ticket.setSubject(req.getSubject());
        if (req.getDescription() != null && !req.getDescription().isBlank()) ticket.setDescription(req.getDescription());
        if (req.getPriority() != null && !req.getPriority().isBlank()) ticket.setPriority(req.getPriority().toUpperCase());
        if (req.getPreferredContactMethod() != null && !req.getPreferredContactMethod().isBlank()) ticket.setPreferredContactMethod(req.getPreferredContactMethod());
        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        MySupportTimelineActivity act = new MySupportTimelineActivity();
        act.setTicket(ticket); act.setEvent("TICKET_UPDATED"); act.setPerformedBy(email); act.setTimestamp(now);
        timelineRepository.save(act);

        return new UpdateTicketResponse(ticket.getId(), ticket.getTicketNumber(), ticket.getSubject(),
                ticket.getPriority(), ticket.getStatus(), now.format(ISO_FORMATTER), "Ticket updated successfully");
    }

    /** PATCH /tickets/{ticketId}/reopen */
    @Transactional
    public ReopenTicketResponse reopenTicket(String email, Long ticketId) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        if (!ticket.getEmployee().getEmail().equals(email))
            throw new IllegalArgumentException("Access Denied: You do not own this ticket.");
        if (!"CLOSED".equalsIgnoreCase(ticket.getStatus()))
            throw new IllegalArgumentException("Only CLOSED tickets can be reopened (current status: " + ticket.getStatus() + ").");

        String previousStatus = ticket.getStatus();
        LocalDateTime now = LocalDateTime.now();
        ticket.setStatus("OPEN");
        ticket.setClosedAt(null);
        ticket.setUpdatedAt(now);
        int responseHours = 8, resolutionHours = 48;
        if ("HIGH".equalsIgnoreCase(ticket.getPriority()))          { responseHours = 2;  resolutionHours = 32; }
        else if ("CRITICAL".equalsIgnoreCase(ticket.getPriority())) { responseHours = 1;  resolutionHours = 12; }
        else if ("LOW".equalsIgnoreCase(ticket.getPriority()))      { responseHours = 24; resolutionHours = 72; }
        ticket.setSlaResponseDueAt(now.plusHours(responseHours));
        ticket.setSlaResolutionDueAt(now.plusHours(resolutionHours));
        ticket.setSlaResolutionTimeHours(resolutionHours);
        ticketRepository.save(ticket);

        MySupportTimelineActivity act = new MySupportTimelineActivity();
        act.setTicket(ticket); act.setEvent("TICKET_REOPENED"); act.setPerformedBy(email); act.setTimestamp(now);
        timelineRepository.save(act);

        return new ReopenTicketResponse(ticket.getId(), ticket.getTicketNumber(), previousStatus,
                ticket.getStatus(), now.format(ISO_FORMATTER), "Ticket reopened successfully");
    }

    /** DELETE /tickets/{ticketId} — only OPEN tickets */
    @Transactional
    public void deleteTicket(String email, Long ticketId) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        if (!ticket.getEmployee().getEmail().equals(email))
            throw new IllegalArgumentException("Access Denied: You do not own this ticket.");
        if (!"OPEN".equalsIgnoreCase(ticket.getStatus()))
            throw new IllegalArgumentException("Only OPEN tickets can be deleted (current status: " + ticket.getStatus() + ").");

        timelineRepository.deleteAll(timelineRepository.findByTicketId(ticketId));
        commentRepository.deleteAll(commentRepository.findByTicketId(ticketId));
        attachmentRepository.deleteAll(attachmentRepository.findByTicketId(ticketId));
        ticketRepository.delete(ticket);
    }

    /** GET /attachments/{attachmentId}/download */
    @Transactional(readOnly = true)
    public MySupportAttachment getAttachmentForDownload(String email, String attachmentId) {
        MySupportAttachment att = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found: " + attachmentId));
        if (att.getTicket() != null && !att.getTicket().getEmployee().getEmail().equals(email))
            throw new IllegalArgumentException("Access Denied: You cannot download this attachment.");
        if (att.getFileContent() == null || att.getFileContent().length == 0)
            throw new IllegalArgumentException("Attachment has no stored content.");
        return att;
    }

    /** GET /tickets/export → CSV */
    @Transactional(readOnly = true)
    public String exportTicketsCsv(String email) {
        List<MySupportTicket> tickets = ticketRepository.findByEmployeeEmail(email);
        StringBuilder sb = new StringBuilder();
        sb.append("Ticket Number,Subject,Category,Priority,Status,Created At,Updated At\n");
        tickets.stream().sorted(Comparator.comparing(MySupportTicket::getCreatedAt).reversed()).forEach(t ->
                sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        t.getTicketNumber(), escape(t.getSubject()),
                        t.getCategory() != null ? t.getCategory().getName() : "",
                        t.getPriority(), t.getStatus(),
                        t.getCreatedAt().format(ISO_FORMATTER), t.getUpdatedAt().format(ISO_FORMATTER))));
        return sb.toString();
    }

    private String escape(String v) { return v == null ? "" : v.replace("\"", "\"\""); }

    /** POST /tickets/{ticketId}/feedback */
    @Transactional
    public TicketFeedbackResponse submitFeedback(String email, Long ticketId, TicketFeedbackRequest req) {
        MySupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        if (!ticket.getEmployee().getEmail().equals(email))
            throw new IllegalArgumentException("Access Denied: You do not own this ticket.");
        if (!"RESOLVED".equalsIgnoreCase(ticket.getStatus()) && !"CLOSED".equalsIgnoreCase(ticket.getStatus()))
            throw new IllegalArgumentException("Feedback can only be submitted on RESOLVED or CLOSED tickets.");

        LocalDateTime now = LocalDateTime.now();
        ticket.setRating(req.getRating());
        ticket.setFeedback(req.getComment());
        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        MySupportTimelineActivity act = new MySupportTimelineActivity();
        act.setTicket(ticket); act.setEvent("FEEDBACK_SUBMITTED"); act.setPerformedBy(email); act.setTimestamp(now);
        timelineRepository.save(act);

        return new TicketFeedbackResponse(ticket.getId(), ticket.getTicketNumber(),
                ticket.getRating(), ticket.getFeedback(), now.format(ISO_FORMATTER), "Feedback submitted successfully");
    }

    /** GET /knowledge-base/articles */
    @Transactional(readOnly = true)
    public FAQSearchResponse getKnowledgeBaseArticles(String category) {
        List<MyKnowledgeBaseArticle> list = (category == null || category.isBlank())
                ? articleRepository.findAll()
                : articleRepository.findByCategoryIgnoreCase(category);
        return new FAQSearchResponse(mapArticles(list));
    }

    /** GET /knowledge-base/articles/{id} — bumps view count */
    @Transactional
    public FAQSearchResponse.FAQArticleDto getKnowledgeBaseArticleById(Long articleId) {
        MyKnowledgeBaseArticle a = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found with ID: " + articleId));
        a.setViews(a.getViews() + 1);
        articleRepository.save(a);
        int totalVotes = a.getHelpfulCount() + a.getNotHelpfulCount();
        double score = totalVotes > 0 ? (a.getHelpfulCount() * 100.0) / totalVotes : 0.0;
        return new FAQSearchResponse.FAQArticleDto(a.getArticleId(), a.getTitle(), a.getContent(), a.getCategory(), a.getViews(), score);
    }

    private List<FAQSearchResponse.FAQArticleDto> mapArticles(List<MyKnowledgeBaseArticle> list) {
        return list.stream().map(a -> {
            int totalVotes = a.getHelpfulCount() + a.getNotHelpfulCount();
            double score = totalVotes > 0 ? (a.getHelpfulCount() * 100.0) / totalVotes : 0.0;
            return new FAQSearchResponse.FAQArticleDto(a.getArticleId(), a.getTitle(), a.getContent(), a.getCategory(), a.getViews(), score);
        }).collect(Collectors.toList());
    }
}
