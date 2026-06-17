package com.example.ems.employee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_employee_messages")
public class MyEmployeeMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Employee sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Employee recipient;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String status = "SENT";

    private LocalDateTime sentAt = LocalDateTime.now();

    public MyEmployeeMessage() {}

    public MyEmployeeMessage(Long id, Employee sender, Employee recipient, String subject, String message, String status, LocalDateTime sentAt) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getSender() { return sender; }
    public void setSender(Employee sender) { this.sender = sender; }

    public Employee getRecipient() { return recipient; }
    public void setRecipient(Employee recipient) { this.recipient = recipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
