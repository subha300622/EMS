package com.example.ems.schedule.dto;

public class SchedulePoliciesResponse {

    private PolicyInfo policy;

    public SchedulePoliciesResponse() {}

    public SchedulePoliciesResponse(PolicyInfo policy) {
        this.policy = policy;
    }

    public static class PolicyInfo {
        private Integer workingDaysPerWeek;
        private String standardWorkingHours;
        private Boolean flexibleWorkingAllowed;
        private Integer minimumShiftChangeNoticeHours;
        private Boolean remoteWorkAllowed;

        public PolicyInfo() {}

        public PolicyInfo(Integer workingDaysPerWeek, String standardWorkingHours, Boolean flexibleWorkingAllowed, Integer minimumShiftChangeNoticeHours, Boolean remoteWorkAllowed) {
            this.workingDaysPerWeek = workingDaysPerWeek;
            this.standardWorkingHours = standardWorkingHours;
            this.flexibleWorkingAllowed = flexibleWorkingAllowed;
            this.minimumShiftChangeNoticeHours = minimumShiftChangeNoticeHours;
            this.remoteWorkAllowed = remoteWorkAllowed;
        }

        // Getters and Setters
        public Integer getWorkingDaysPerWeek() { return workingDaysPerWeek; }
        public void setWorkingDaysPerWeek(Integer workingDaysPerWeek) { this.workingDaysPerWeek = workingDaysPerWeek; }

        public String getStandardWorkingHours() { return standardWorkingHours; }
        public void setStandardWorkingHours(String standardWorkingHours) { this.standardWorkingHours = standardWorkingHours; }

        public Boolean getFlexibleWorkingAllowed() { return flexibleWorkingAllowed; }
        public void setFlexibleWorkingAllowed(Boolean flexibleWorkingAllowed) { this.flexibleWorkingAllowed = flexibleWorkingAllowed; }

        public Integer getMinimumShiftChangeNoticeHours() { return minimumShiftChangeNoticeHours; }
        public void setMinimumShiftChangeNoticeHours(Integer minimumShiftChangeNoticeHours) { this.minimumShiftChangeNoticeHours = minimumShiftChangeNoticeHours; }

        public Boolean getRemoteWorkAllowed() { return remoteWorkAllowed; }
        public void setRemoteWorkAllowed(Boolean remoteWorkAllowed) { this.remoteWorkAllowed = remoteWorkAllowed; }
    }

    public PolicyInfo getPolicy() { return policy; }
    public void setPolicy(PolicyInfo policy) { this.policy = policy; }
}
