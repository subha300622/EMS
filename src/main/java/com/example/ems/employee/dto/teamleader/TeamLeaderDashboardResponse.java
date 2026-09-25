package com.example.ems.employee.dto.teamleader;

import java.util.List;

public class TeamLeaderDashboardResponse {

    private Summary summary;
    private List<TeamMemberRosterDto> teamRoster;
    private List<TeamPendingLeaveReviewDto> pendingLeaveReviews;

    public TeamLeaderDashboardResponse() {}

    public TeamLeaderDashboardResponse(Summary summary, List<TeamMemberRosterDto> teamRoster, List<TeamPendingLeaveReviewDto> pendingLeaveReviews) {
        this.summary = summary;
        this.teamRoster = teamRoster;
        this.pendingLeaveReviews = pendingLeaveReviews;
    }

    public static class Summary {
        private int teamMembers;
        private int activeDirectReports;
        private int presentToday;
        private int attendancePercentage;
        private int pendingLeaveReviews;
        private TeamPerformance teamPerformance;

        public Summary() {}

        public Summary(int teamMembers, int activeDirectReports, int presentToday, int attendancePercentage, int pendingLeaveReviews, TeamPerformance teamPerformance) {
            this.teamMembers = teamMembers;
            this.activeDirectReports = activeDirectReports;
            this.presentToday = presentToday;
            this.attendancePercentage = attendancePercentage;
            this.pendingLeaveReviews = pendingLeaveReviews;
            this.teamPerformance = teamPerformance;
        }

        public int getTeamMembers() { return teamMembers; }
        public void setTeamMembers(int teamMembers) { this.teamMembers = teamMembers; }

        public int getActiveDirectReports() { return activeDirectReports; }
        public void setActiveDirectReports(int activeDirectReports) { this.activeDirectReports = activeDirectReports; }

        public int getPresentToday() { return presentToday; }
        public void setPresentToday(int presentToday) { this.presentToday = presentToday; }

        public int getAttendancePercentage() { return attendancePercentage; }
        public void setAttendancePercentage(int attendancePercentage) { this.attendancePercentage = attendancePercentage; }

        public int getPendingLeaveReviews() { return pendingLeaveReviews; }
        public void setPendingLeaveReviews(int pendingLeaveReviews) { this.pendingLeaveReviews = pendingLeaveReviews; }

        public TeamPerformance getTeamPerformance() { return teamPerformance; }
        public void setTeamPerformance(TeamPerformance teamPerformance) { this.teamPerformance = teamPerformance; }
    }

    public static class TeamPerformance {
        private double rating;
        private double maxRating;
        private double sprintDeliveryIndex;

        public TeamPerformance() {}

        public TeamPerformance(double rating, double maxRating, double sprintDeliveryIndex) {
            this.rating = rating;
            this.maxRating = maxRating;
            this.sprintDeliveryIndex = sprintDeliveryIndex;
        }

        public double getRating() { return rating; }
        public void setRating(double rating) { this.rating = rating; }

        public double getMaxRating() { return maxRating; }
        public void setMaxRating(double maxRating) { this.maxRating = maxRating; }

        public double getSprintDeliveryIndex() { return sprintDeliveryIndex; }
        public void setSprintDeliveryIndex(double sprintDeliveryIndex) { this.sprintDeliveryIndex = sprintDeliveryIndex; }
    }

    public Summary getSummary() { return summary; }
    public void setSummary(Summary summary) { this.summary = summary; }

    public List<TeamMemberRosterDto> getTeamRoster() { return teamRoster; }
    public void setTeamRoster(List<TeamMemberRosterDto> teamRoster) { this.teamRoster = teamRoster; }

    public List<TeamPendingLeaveReviewDto> getPendingLeaveReviews() { return pendingLeaveReviews; }
    public void setPendingLeaveReviews(List<TeamPendingLeaveReviewDto> pendingLeaveReviews) { this.pendingLeaveReviews = pendingLeaveReviews; }
}
