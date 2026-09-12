package com.example.ems.common.event;

/**
 * Centralized event type constants organized by bounded context.
 * Topic names follow the pattern: ems.{context}.{entity}.{action}
 */
public final class EventTypes {

    private EventTypes() {
        // Prevent instantiation
    }

    // Support context
    public static final String TICKET_CREATED = "ems.support.ticket.created";
    public static final String TICKET_REPLY_CREATED = "ems.support.ticket.reply.created";

    // Notification context
    public static final String NOTIFICATION_CREATED = "ems.notification.created";

    // Leave context
    public static final String LEAVE_APPROVED = "ems.leave.approved";
    public static final String LEAVE_REJECTED = "ems.leave.rejected";

    // Payroll context
    public static final String PAYROLL_GENERATED = "ems.payroll.generated";

    // Employee context
    public static final String EMPLOYEE_CREATED = "ems.employee.created";

    // Dead Letter Queue suffix
    public static final String DLQ_SUFFIX = ".dlq";
}
