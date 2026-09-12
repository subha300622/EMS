package com.example.ems.support.event;

/**
 * Integration event payload for ticket reply creation.
 * This is the stable contract published to Kafka for the support context.
 */
public class TicketReplyCreatedEvent {

    private Long ticketId;
    private String ticketNumber;
    private Long replyAuthorId;
    private String replyAuthorName;
    private String replyContent;
    private String ticketStatus;

    public TicketReplyCreatedEvent() {
    }

    public TicketReplyCreatedEvent(Long ticketId, String ticketNumber,
                                    Long replyAuthorId, String replyAuthorName,
                                    String replyContent, String ticketStatus) {
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.replyAuthorId = replyAuthorId;
        this.replyAuthorName = replyAuthorName;
        this.replyContent = replyContent;
        this.ticketStatus = ticketStatus;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public Long getReplyAuthorId() {
        return replyAuthorId;
    }

    public void setReplyAuthorId(Long replyAuthorId) {
        this.replyAuthorId = replyAuthorId;
    }

    public String getReplyAuthorName() {
        return replyAuthorName;
    }

    public void setReplyAuthorName(String replyAuthorName) {
        this.replyAuthorName = replyAuthorName;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }
}
