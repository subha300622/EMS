package com.example.ems.common.service;

import com.example.ems.auth.entity.User;
import com.example.ems.common.dto.manager.*;
import com.example.ems.common.entity.Notification;
import com.example.ems.common.repository.NotificationRepository;
import com.example.ems.employee.entity.Announcement;
import com.example.ems.employee.repository.AnnouncementCommentRepository;
import com.example.ems.employee.repository.AnnouncementRepository;
import com.example.ems.settings.entity.NotificationPreference;
import com.example.ems.settings.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagerNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private AnnouncementCommentRepository announcementCommentRepository;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @InjectMocks
    private ManagerNotificationService managerNotificationService;

    private User testUser;
    private Notification testNotification;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(10L);
        testUser.setFullName("John Doe");
        testUser.setWorkEmail("john.doe@example.com");

        testNotification = new Notification();
        testNotification.setId(100L);
        testNotification.setUser(testUser);
        testNotification.setTitle("Test Alert");
        testNotification.setMessage("This is a test notification message");
        testNotification.setType("APPROVAL");
        testNotification.setPriority("HIGH");
        testNotification.setRead(false);
        testNotification.setCreatedAt(LocalDateTime.of(2026, 6, 22, 10, 0, 0));
    }

    @Test
    @DisplayName("getNotificationFeed with type=ALL and status=ALL fetches all notifications for user")
    public void testGetNotificationFeed_All() {
        Page<Notification> page = new PageImpl<>(List.of(testNotification));
        when(notificationRepository.findByUserId(eq(10L), any(Pageable.class))).thenReturn(page);

        Page<NotificationDto> result = managerNotificationService.getNotificationFeed(testUser, 0, 10, "ALL", "ALL");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Alert", result.getContent().get(0).title());
        verify(notificationRepository).findByUserId(eq(10L), any(Pageable.class));
    }

    @Test
    @DisplayName("getNotificationFeed with status=UNREAD filters by isRead=false")
    public void testGetNotificationFeed_FilterUnread() {
        Page<Notification> page = new PageImpl<>(List.of(testNotification));
        when(notificationRepository.findByUserIdAndIsRead(eq(10L), eq(false), any(Pageable.class))).thenReturn(page);

        Page<NotificationDto> result = managerNotificationService.getNotificationFeed(testUser, 0, 10, "ALL", "UNREAD");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(notificationRepository).findByUserIdAndIsRead(eq(10L), eq(false), any(Pageable.class));
    }

    @Test
    @DisplayName("getNotificationFeed with type=APPROVAL and status=UNREAD filters by type and isRead")
    public void testGetNotificationFeed_FilterTypeAndUnread() {
        Page<Notification> page = new PageImpl<>(List.of(testNotification));
        when(notificationRepository.findByUserIdAndTypeAndIsRead(eq(10L), eq("APPROVAL"), eq(false), any(Pageable.class))).thenReturn(page);

        Page<NotificationDto> result = managerNotificationService.getNotificationFeed(testUser, 0, 10, "APPROVAL", "UNREAD");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(notificationRepository).findByUserIdAndTypeAndIsRead(eq(10L), eq("APPROVAL"), eq(false), any(Pageable.class));
    }

    @Test
    @DisplayName("getNotificationFeed with type=APPROVAL and status=ALL filters by type only")
    public void testGetNotificationFeed_FilterTypeOnly() {
        Page<Notification> page = new PageImpl<>(List.of(testNotification));
        when(notificationRepository.findByUserIdAndType(eq(10L), eq("APPROVAL"), any(Pageable.class))).thenReturn(page);

        Page<NotificationDto> result = managerNotificationService.getNotificationFeed(testUser, 0, 10, "APPROVAL", "ALL");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(notificationRepository).findByUserIdAndType(eq(10L), eq("APPROVAL"), any(Pageable.class));
    }

    @Test
    @DisplayName("getUnreadCount returns correct count")
    public void testGetUnreadCount() {
        when(notificationRepository.countByUserIdAndIsReadFalse(10L)).thenReturn(7L);

        UnreadCountDto countDto = managerNotificationService.getUnreadCount(testUser);

        assertNotNull(countDto);
        assertEquals(7L, countDto.count());
        verify(notificationRepository).countByUserIdAndIsReadFalse(10L);
    }

    @Test
    @DisplayName("markAsRead updates notification read status")
    public void testMarkAsRead_Success() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        managerNotificationService.markAsRead(testUser, 100L);

        assertTrue(testNotification.isRead());
        verify(notificationRepository).save(testNotification);
    }

    @Test
    @DisplayName("markAsRead throws exception when notification not found")
    public void testMarkAsRead_NotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                managerNotificationService.markAsRead(testUser, 999L));

        assertTrue(ex.getMessage().contains("Notification not found with ID: 999"));
    }

    @Test
    @DisplayName("markAsRead throws exception when user is not the owner")
    public void testMarkAsRead_UnauthorizedUser() {
        User otherUser = new User();
        otherUser.setId(99L);
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(testNotification));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                managerNotificationService.markAsRead(otherUser, 100L));

        assertEquals("Unauthorized to access this notification", ex.getMessage());
    }

    @Test
    @DisplayName("markAllAsRead marks all unread notifications as read")
    public void testMarkAllAsRead() {
        Notification n1 = new Notification();
        n1.setUser(testUser);
        n1.setRead(false);
        Notification n2 = new Notification();
        n2.setUser(testUser);
        n2.setRead(false);

        when(notificationRepository.findByUserIdAndIsRead(eq(10L), eq(false), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(n1, n2)));

        int count = managerNotificationService.markAllAsRead(testUser);

        assertEquals(2, count);
        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
        verify(notificationRepository).saveAll(List.of(n1, n2));
    }

    @Test
    @DisplayName("deleteNotification deletes notification when user is owner")
    public void testDeleteNotification_Success() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(testNotification));

        managerNotificationService.deleteNotification(testUser, 100L);

        verify(notificationRepository).delete(testNotification);
    }

    @Test
    @DisplayName("deleteNotification throws exception when notification not found")
    public void testDeleteNotification_NotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                managerNotificationService.deleteNotification(testUser, 999L));

        assertTrue(ex.getMessage().contains("Notification not found with ID: 999"));
    }

    @Test
    @DisplayName("deleteNotification throws exception when user is not owner")
    public void testDeleteNotification_UnauthorizedUser() {
        User otherUser = new User();
        otherUser.setId(99L);
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(testNotification));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                managerNotificationService.deleteNotification(otherUser, 100L));

        assertEquals("Unauthorized to access this notification", ex.getMessage());
    }

    @Test
    @DisplayName("getPreferences creates default preferences if none exist")
    public void testGetPreferences_CreatesDefaultsWhenEmpty() {
        when(preferenceRepository.findByUserEmail("john.doe@example.com"))
                .thenReturn(new ArrayList<>())
                .thenReturn(List.of(
                        new NotificationPreference("john.doe@example.com", "ANNOUNCEMENT", true, true, false),
                        new NotificationPreference("john.doe@example.com", "LEAVE", true, true, false),
                        new NotificationPreference("john.doe@example.com", "SCHEDULE", true, true, false)
                ));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(i -> i.getArgument(0));

        NotificationPreferenceDto dto = managerNotificationService.getPreferences(testUser);

        assertNotNull(dto);
        assertTrue(dto.emailNotifications());
        assertTrue(dto.pushNotifications());
        assertTrue(dto.approvalAlerts());
        assertTrue(dto.systemAlerts());
        assertTrue(dto.announcementAlerts());
    }

    @Test
    @DisplayName("updatePreferences updates category preferences properly")
    public void testUpdatePreferences_Success() {
        List<NotificationPreference> existingPrefs = List.of(
                new NotificationPreference("john.doe@example.com", "ANNOUNCEMENT", true, true, false),
                new NotificationPreference("john.doe@example.com", "LEAVE", true, true, false),
                new NotificationPreference("john.doe@example.com", "SCHEDULE", true, true, false)
        );
        when(preferenceRepository.findByUserEmail("john.doe@example.com")).thenReturn(existingPrefs);

        NotificationPreferenceDto updateRequest = new NotificationPreferenceDto(false, false, false, false, false);
        NotificationPreferenceDto result = managerNotificationService.updatePreferences(testUser, updateRequest);

        assertNotNull(result);
        assertFalse(result.emailNotifications());
        assertFalse(result.pushNotifications());
        assertFalse(result.approvalAlerts());
        assertFalse(result.systemAlerts());
        assertFalse(result.announcementAlerts());
        verify(preferenceRepository, atLeastOnce()).save(any(NotificationPreference.class));
    }

    @Test
    @DisplayName("getStats calculates counts across all types")
    public void testGetStats() {
        when(notificationRepository.countByUserId(10L)).thenReturn(40L);
        when(notificationRepository.countByUserIdAndIsReadFalse(10L)).thenReturn(8L);
        when(notificationRepository.countByUserIdAndType(10L, "APPROVAL")).thenReturn(15L);
        when(notificationRepository.countByUserIdAndType(10L, "MENTION")).thenReturn(5L);
        when(notificationRepository.countByUserIdAndType(10L, "SYSTEM")).thenReturn(12L);

        NotificationStatsDto stats = managerNotificationService.getStats(testUser);

        assertNotNull(stats);
        assertEquals(40, stats.total());
        assertEquals(8, stats.unread());
        assertEquals(15, stats.approvals());
        assertEquals(5, stats.mentions());
        assertEquals(12, stats.system());
    }

    @Test
    @DisplayName("getPageData compiles consolidated page response")
    public void testGetPageData() {
        when(notificationRepository.countByUserId(10L)).thenReturn(10L);
        when(notificationRepository.countByUserIdAndIsReadFalse(10L)).thenReturn(2L);
        when(notificationRepository.countByUserIdAndType(eq(10L), anyString())).thenReturn(3L);

        Page<Notification> notifPage = new PageImpl<>(List.of(testNotification));
        when(notificationRepository.findByUserId(eq(10L), any(Pageable.class))).thenReturn(notifPage);

        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setTitle("Company Event");
        announcement.setContent("Annual picnic");
        announcement.setCategory("EVENT");
        announcement.setAuthor("HR");
        announcement.setPublishedDate(LocalDateTime.of(2026, 6, 20, 9, 0));
        announcement.setLikes(10);
        announcement.setViews(50);
        Page<Announcement> annPage = new PageImpl<>(List.of(announcement));
        when(announcementRepository.findByActiveTrue(any(Pageable.class))).thenReturn(annPage);
        when(announcementCommentRepository.countByAnnouncementId(1L)).thenReturn(4);

        when(preferenceRepository.findByUserEmail("john.doe@example.com")).thenReturn(List.of(
                new NotificationPreference("john.doe@example.com", "ANNOUNCEMENT", true, true, false),
                new NotificationPreference("john.doe@example.com", "LEAVE", true, true, false),
                new NotificationPreference("john.doe@example.com", "SCHEDULE", true, true, false)
        ));

        NotificationPageResponse response = managerNotificationService.getPageData(testUser);

        assertNotNull(response);
        assertEquals(2, response.unreadCount());
        assertEquals(10, response.stats().total());
        assertEquals(1, response.notifications().size());
        assertEquals(1, response.announcements().size());
        assertEquals("Company Event", response.announcements().get(0).title());
        assertEquals(4, response.announcements().get(0).comments());
        assertNotNull(response.preferences());
    }
}
