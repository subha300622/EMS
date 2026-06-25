package com.example.ems.auth.dto;

public class MyDashboardResponse {
    private AttendanceData attendance;
    private LeaveData leave;
    private ExpenseData expenses;
    private AssetData assets;
    private NotificationData notifications;
    private AnnouncementData announcements;
    private PerformanceData performance;
    private SupportData support;
    private ProfileData profile;

    public MyDashboardResponse() {}

    public MyDashboardResponse(AttendanceData attendance, LeaveData leave, ExpenseData expenses, AssetData assets,
                               NotificationData notifications, AnnouncementData announcements,
                               PerformanceData performance, SupportData support, ProfileData profile) {
        this.attendance = attendance;
        this.leave = leave;
        this.expenses = expenses;
        this.assets = assets;
        this.notifications = notifications;
        this.announcements = announcements;
        this.performance = performance;
        this.support = support;
        this.profile = profile;
    }

    public AttendanceData getAttendance() { return attendance; }
    public void setAttendance(AttendanceData attendance) { this.attendance = attendance; }

    public LeaveData getLeave() { return leave; }
    public void setLeave(LeaveData leave) { this.leave = leave; }

    public ExpenseData getExpenses() { return expenses; }
    public void setExpenses(ExpenseData expenses) { this.expenses = expenses; }

    public AssetData getAssets() { return assets; }
    public void setAssets(AssetData assets) { this.assets = assets; }

    public NotificationData getNotifications() { return notifications; }
    public void setNotifications(NotificationData notifications) { this.notifications = notifications; }

    public AnnouncementData getAnnouncements() { return announcements; }
    public void setAnnouncements(AnnouncementData announcements) { this.announcements = announcements; }

    public PerformanceData getPerformance() { return performance; }
    public void setPerformance(PerformanceData performance) { this.performance = performance; }

    public SupportData getSupport() { return support; }
    public void setSupport(SupportData support) { this.support = support; }

    public ProfileData getProfile() { return profile; }
    public void setProfile(ProfileData profile) { this.profile = profile; }

    public static class AttendanceData {
        private String todayStatus;
        private String checkIn;
        private String checkOut;
        private String workingHours;

        public AttendanceData() {}

        public AttendanceData(String todayStatus, String checkIn, String checkOut, String workingHours) {
            this.todayStatus = todayStatus;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.workingHours = workingHours;
        }

        public String getTodayStatus() { return todayStatus; }
        public void setTodayStatus(String todayStatus) { this.todayStatus = todayStatus; }

        public String getCheckIn() { return checkIn; }
        public void setCheckIn(String checkIn) { this.checkIn = checkIn; }

        public String getCheckOut() { return checkOut; }
        public void setCheckOut(String checkOut) { this.checkOut = checkOut; }

        public String getWorkingHours() { return workingHours; }
        public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
    }

    public static class LeaveData {
        private long pending;
        private long remaining;

        public LeaveData() {}

        public LeaveData(long pending, long remaining) {
            this.pending = pending;
            this.remaining = remaining;
        }

        public long getPending() { return pending; }
        public void setPending(long pending) { this.pending = pending; }

        public long getRemaining() { return remaining; }
        public void setRemaining(long remaining) { this.remaining = remaining; }
    }

    public static class ExpenseData {
        private long pending;

        public ExpenseData() {}

        public ExpenseData(long pending) {
            this.pending = pending;
        }

        public long getPending() { return pending; }
        public void setPending(long pending) { this.pending = pending; }
    }

    public static class AssetData {
        private long assigned;

        public AssetData() {}

        public AssetData(long assigned) {
            this.assigned = assigned;
        }

        public long getAssigned() { return assigned; }
        public void setAssigned(long assigned) { this.assigned = assigned; }
    }

    public static class NotificationData {
        private long unread;

        public NotificationData() {}

        public NotificationData(long unread) {
            this.unread = unread;
        }

        public long getUnread() { return unread; }
        public void setUnread(long unread) { this.unread = unread; }
    }

    public static class AnnouncementData {
        private long unread;

        public AnnouncementData() {}

        public AnnouncementData(long unread) {
            this.unread = unread;
        }

        public long getUnread() { return unread; }
        public void setUnread(long unread) { this.unread = unread; }
    }

    public static class PerformanceData {
        private long pendingReviews;

        public PerformanceData() {}

        public PerformanceData(long pendingReviews) {
            this.pendingReviews = pendingReviews;
        }

        public long getPendingReviews() { return pendingReviews; }
        public void setPendingReviews(long pendingReviews) { this.pendingReviews = pendingReviews; }
    }

    public static class SupportData {
        private long openTickets;

        public SupportData() {}

        public SupportData(long openTickets) {
            this.openTickets = openTickets;
        }

        public long getOpenTickets() { return openTickets; }
        public void setOpenTickets(long openTickets) { this.openTickets = openTickets; }
    }

    public static class ProfileData {
        private int completion;

        public ProfileData() {}

        public ProfileData(int completion) {
            this.completion = completion;
        }

        public int getCompletion() { return completion; }
        public void setCompletion(int completion) { this.completion = completion; }
    }
}
