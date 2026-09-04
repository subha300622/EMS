package com.example.ems.recruitment.dto;

public class RecruitmentDashboardResponse {

    private long openJobs;
    private long applications;
    private long screening;
    private long shortlisted;
    private long interviews;
    private long selected;
    private long offers;
    private long joined;
    private long rejected;
    private double conversionRate;

    public long getOpenJobs() { return openJobs; }
    public void setOpenJobs(long openJobs) { this.openJobs = openJobs; }

    public long getApplications() { return applications; }
    public void setApplications(long applications) { this.applications = applications; }

    public long getScreening() { return screening; }
    public void setScreening(long screening) { this.screening = screening; }

    public long getShortlisted() { return shortlisted; }
    public void setShortlisted(long shortlisted) { this.shortlisted = shortlisted; }

    public long getInterviews() { return interviews; }
    public void setInterviews(long interviews) { this.interviews = interviews; }

    public long getSelected() { return selected; }
    public void setSelected(long selected) { this.selected = selected; }

    public long getOffers() { return offers; }
    public void setOffers(long offers) { this.offers = offers; }

    public long getJoined() { return joined; }
    public void setJoined(long joined) { this.joined = joined; }

    public long getRejected() { return rejected; }
    public void setRejected(long rejected) { this.rejected = rejected; }

    public double getConversionRate() { return conversionRate; }
    public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
}
