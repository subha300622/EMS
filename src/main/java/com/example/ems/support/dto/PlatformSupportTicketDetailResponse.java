package com.example.ems.support.dto;

import java.util.List;
import java.util.Map;

public class PlatformSupportTicketDetailResponse {

    private Map<String, Object> ticket;
    private List<Map<String, Object>> messages;
    private List<Map<String, Object>> attachments;
    private List<Map<String, Object>> activities;
    private Map<String, Object> customer;
    private Map<String, Object> business;

    public PlatformSupportTicketDetailResponse() {}

    public PlatformSupportTicketDetailResponse(Map<String, Object> ticket, List<Map<String, Object>> messages,
                                               List<Map<String, Object>> attachments, List<Map<String, Object>> activities,
                                               Map<String, Object> customer, Map<String, Object> business) {
        this.ticket = ticket;
        this.messages = messages;
        this.attachments = attachments;
        this.activities = activities;
        this.customer = customer;
        this.business = business;
    }

    public Map<String, Object> getTicket() { return ticket; }
    public void setTicket(Map<String, Object> ticket) { this.ticket = ticket; }

    public List<Map<String, Object>> getMessages() { return messages; }
    public void setMessages(List<Map<String, Object>> messages) { this.messages = messages; }

    public List<Map<String, Object>> getAttachments() { return attachments; }
    public void setAttachments(List<Map<String, Object>> attachments) { this.attachments = attachments; }

    public List<Map<String, Object>> getActivities() { return activities; }
    public void setActivities(List<Map<String, Object>> activities) { this.activities = activities; }

    public Map<String, Object> getCustomer() { return customer; }
    public void setCustomer(Map<String, Object> customer) { this.customer = customer; }

    public Map<String, Object> getBusiness() { return business; }
    public void setBusiness(Map<String, Object> business) { this.business = business; }
}
