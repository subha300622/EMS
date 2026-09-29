package com.example.ems.employee.dto.teamleader;

import java.util.List;

public class TeamLeaderPerformanceResponse {

    private double rating;
    private double maxRating;
    private double sprintDeliveryIndex;
    private List<MemberPerformanceDto> memberRatings;

    public TeamLeaderPerformanceResponse() {}

    public TeamLeaderPerformanceResponse(double rating, double maxRating, double sprintDeliveryIndex, List<MemberPerformanceDto> memberRatings) {
        this.rating = rating;
        this.maxRating = maxRating;
        this.sprintDeliveryIndex = sprintDeliveryIndex;
        this.memberRatings = memberRatings;
    }

    public static class MemberPerformanceDto {
        private Long employeeId;
        private String employeeName;
        private Double rating;

        public MemberPerformanceDto() {}

        public MemberPerformanceDto(Long employeeId, String employeeName, Double rating) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.rating = rating;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
    }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public double getMaxRating() { return maxRating; }
    public void setMaxRating(double maxRating) { this.maxRating = maxRating; }

    public double getSprintDeliveryIndex() { return sprintDeliveryIndex; }
    public void setSprintDeliveryIndex(double sprintDeliveryIndex) { this.sprintDeliveryIndex = sprintDeliveryIndex; }

    public List<MemberPerformanceDto> getMemberRatings() { return memberRatings; }
    public void setMemberRatings(List<MemberPerformanceDto> memberRatings) { this.memberRatings = memberRatings; }
}
