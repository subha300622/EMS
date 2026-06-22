package com.example.ems.common.service;

import com.example.ems.auth.entity.User;
import com.example.ems.common.entity.Notification;
import com.example.ems.common.repository.NotificationRepository;
import com.example.ems.common.dto.manager.*;
import com.example.ems.employee.entity.Announcement;
import com.example.ems.employee.entity.AnnouncementComment;
import com.example.ems.employee.repository.AnnouncementRepository;
import com.example.ems.employee.repository.AnnouncementCommentRepository;
import com.example.ems.settings.entity.NotificationPreference;
import com.example.ems.settings.repository.NotificationPreferenceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ManagerNotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private AnnouncementCommentRepository announcementCommentRepository;

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationFeed(User user, int page, int size, String type, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notificationPage;

        boolean filterByRead = false;
        boolean isReadVal = false;
        if ("UNREAD".equalsIgnoreCase(status)) {
            filterByRead = true;
            isReadVal = false;
        } else if ("READ".equalsIgnoreCase(status)) {
            filterByRead = true;
            isReadVal = true;
        }

        boolean filterByType = type != null && !"ALL".equalsIgnoreCase(type);

        if (filterByType && filterByRead) {
            notificationPage = notificationRepository.findByUserIdAndTypeAndIsRead(user.getId(), type.toUpperCase(), isReadVal, pageable);
        } else if (filterByType) {
            notificationPage = notificationRepository.findByUserIdAndType(user.getId(), type.toUpperCase(), pageable);
        } else if (filterByRead) {
            notificationPage = notificationRepository.findByUserIdAndIsRead(user.getId(), isReadVal, pageable);
        } else {
            notificationPage = notificationRepository.findByUserId(user.getId(), pageable);
        }

        return notificationPage.map(this::mapToNotificationDto);
    }

    @Transactional(readOnly = true)
    public UnreadCountDto getUnreadCount(User user) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        return new UnreadCountDto(count);
    }

    @Transactional
    public void markAsRead(User user, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + id));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized to access this notification");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public int markAllAsRead(User user) {
        Page<Notification> unreadNotifs = notificationRepository.findByUserIdAndIsRead(
                user.getId(), false, PageRequest.of(0, Integer.MAX_VALUE));
        List<Notification> list = unreadNotifs.getContent();
        for (Notification n : list) {
            n.setRead(true);
        }
        notificationRepository.saveAll(list);
        return list.size();
    }

    @Transactional
    public void deleteNotification(User user, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + id));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized to access this notification");
        }
        notificationRepository.delete(notification);
    }

    @Transactional(readOnly = true)
    public Page<AnnouncementDto> getAnnouncements(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedDate"));
        Page<Announcement> announcementPage = announcementRepository.findByActiveTrue(pageable);
        return announcementPage.map(this::mapToAnnouncementDto);
    }

    @Transactional
    public AnnouncementDto getAnnouncementDetails(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found with ID: " + id));
        announcement.setViews(announcement.getViews() + 1);
        Announcement saved = announcementRepository.save(announcement);
        return mapToAnnouncementDto(saved);
    }

    @Transactional
    public AnnouncementDto createAnnouncement(User user, AnnouncementDto requestDto) {
        Announcement announcement = new Announcement();
        announcement.setTitle(requestDto.title());
        announcement.setContent(requestDto.description());
        announcement.setCategory(requestDto.category() != null ? requestDto.category().toUpperCase() : "GENERAL");
        announcement.setAuthor(user.getFullName() != null ? user.getFullName() : "HR Department");
        announcement.setPublishedDate(LocalDateTime.now());
        announcement.setActive(true);
        announcement.setLikes(0);
        announcement.setViews(0);

        Announcement saved = announcementRepository.save(announcement);
        return mapToAnnouncementDto(saved);
    }

    @Transactional
    public AnnouncementDto updateAnnouncement(Long id, AnnouncementDto requestDto) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found with ID: " + id));
        announcement.setTitle(requestDto.title());
        announcement.setContent(requestDto.description());
        if (requestDto.category() != null) {
            announcement.setCategory(requestDto.category().toUpperCase());
        }
        Announcement saved = announcementRepository.save(announcement);
        return mapToAnnouncementDto(saved);
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new IllegalArgumentException("Announcement not found with ID: " + id);
        }
        announcementRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Integer> likeAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found with ID: " + id));
        announcement.setLikes(announcement.getLikes() + 1);
        Announcement saved = announcementRepository.save(announcement);
        Map<String, Integer> res = new HashMap<>();
        res.put("likes", saved.getLikes());
        return res;
    }

    @Transactional(readOnly = true)
    public List<AnnouncementCommentDto> getComments(Long announcementId) {
        List<AnnouncementComment> comments = announcementCommentRepository.findByAnnouncementIdOrderByCreatedAtDesc(announcementId);
        return comments.stream().map(this::mapToCommentDto).collect(Collectors.toList());
    }

    @Transactional
    public AnnouncementCommentDto addComment(User user, Long announcementId, String content) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found with ID: " + announcementId));
        AnnouncementComment comment = new AnnouncementComment(announcement, content, user.getFullName());
        AnnouncementComment saved = announcementCommentRepository.save(comment);
        return mapToCommentDto(saved);
    }

    @Transactional
    public List<NotificationPreference> getOrCreateNotificationPreferences(String email) {
        List<NotificationPreference> list = preferenceRepository.findByUserEmail(email);
        if (list.isEmpty()) {
            List<String> categories = Arrays.asList("LEAVE", "PAYSLIP", "ATTENDANCE", "PERFORMANCE", "EXPENSE", "SCHEDULE", "ANNOUNCEMENT", "GOAL");
            list = new ArrayList<>();
            for (String cat : categories) {
                boolean sms = "PAYSLIP".equals(cat);
                NotificationPreference pref = new NotificationPreference(email, cat, true, true, sms);
                list.add(preferenceRepository.save(pref));
            }
        }
        return list;
    }

    @Transactional
    public NotificationPreferenceDto getPreferences(User user) {
        List<NotificationPreference> list = getOrCreateNotificationPreferences(user.getWorkEmail());
        Map<String, NotificationPreference> prefMap = list.stream()
                .collect(Collectors.toMap(NotificationPreference::getCategory, p -> p));

        boolean emailNotifications = prefMap.containsKey("ANNOUNCEMENT") && prefMap.get("ANNOUNCEMENT").getEmail();
        boolean pushNotifications = prefMap.containsKey("ANNOUNCEMENT") && prefMap.get("ANNOUNCEMENT").getPush();
        boolean approvalAlerts = prefMap.containsKey("LEAVE") && prefMap.get("LEAVE").getEmail();
        boolean systemAlerts = prefMap.containsKey("SCHEDULE") && prefMap.get("SCHEDULE").getEmail();
        boolean announcementAlerts = prefMap.containsKey("ANNOUNCEMENT") && prefMap.get("ANNOUNCEMENT").getEmail();

        return new NotificationPreferenceDto(
                emailNotifications,
                pushNotifications,
                approvalAlerts,
                systemAlerts,
                announcementAlerts
        );
    }

    @Transactional
    public NotificationPreferenceDto updatePreferences(User user, NotificationPreferenceDto dto) {
        List<NotificationPreference> list = getOrCreateNotificationPreferences(user.getWorkEmail());

        for (NotificationPreference pref : list) {
            String cat = pref.getCategory();
            if (dto.emailNotifications() != null) {
                pref.setEmail(dto.emailNotifications());
            }
            if (dto.pushNotifications() != null) {
                pref.setPush(dto.pushNotifications());
            }
            if ("LEAVE".equals(cat) || "EXPENSE".equals(cat) || "GOAL".equals(cat)) {
                if (dto.approvalAlerts() != null) {
                    pref.setEmail(dto.approvalAlerts());
                    pref.setPush(dto.approvalAlerts());
                }
            }
            if ("SCHEDULE".equals(cat)) {
                if (dto.systemAlerts() != null) {
                    pref.setEmail(dto.systemAlerts());
                    pref.setPush(dto.systemAlerts());
                }
            }
            if ("ANNOUNCEMENT".equals(cat)) {
                if (dto.announcementAlerts() != null) {
                    pref.setEmail(dto.announcementAlerts());
                    pref.setPush(dto.announcementAlerts());
                }
            }
            preferenceRepository.save(pref);
        }

        return getPreferences(user);
    }

    @Transactional(readOnly = true)
    public NotificationStatsDto getStats(User user) {
        long total = notificationRepository.countByUserId(user.getId());
        long unread = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        long approvals = notificationRepository.countByUserIdAndType(user.getId(), "APPROVAL");
        long mentions = notificationRepository.countByUserIdAndType(user.getId(), "MENTION");
        long system = notificationRepository.countByUserIdAndType(user.getId(), "SYSTEM");

        return new NotificationStatsDto(total, unread, approvals, mentions, system);
    }

    @Transactional
    public NotificationPageResponse getPageData(User user) {
        NotificationStatsDto stats = getStats(user);
        long unreadCount = stats.unread();

        Page<NotificationDto> notificationsPage = getNotificationFeed(user, 0, 20, "ALL", "ALL");
        List<NotificationDto> notifications = notificationsPage.getContent();

        Page<AnnouncementDto> announcementsPage = getAnnouncements(0, 10);
        List<AnnouncementDto> announcements = announcementsPage.getContent();

        NotificationPreferenceDto preferences = getPreferences(user);

        return new NotificationPageResponse(
                stats,
                unreadCount,
                notifications,
                announcements,
                preferences
        );
    }

    private NotificationDto mapToNotificationDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getPriority(),
                n.isRead(),
                n.getCreatedAt().format(DATETIME_FORMATTER)
        );
    }

    private AnnouncementDto mapToAnnouncementDto(Announcement a) {
        int commentCount = announcementCommentRepository.countByAnnouncementId(a.getId());
        return new AnnouncementDto(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getCategory(),
                a.getAuthor(),
                a.getPublishedDate().format(DATE_FORMATTER),
                a.getLikes(),
                commentCount,
                a.getViews()
        );
    }

    private AnnouncementCommentDto mapToCommentDto(AnnouncementComment c) {
        return new AnnouncementCommentDto(
                c.getId(),
                c.getAnnouncement().getId(),
                c.getContent(),
                c.getAuthorName(),
                c.getCreatedAt().format(DATETIME_FORMATTER)
        );
    }
}
