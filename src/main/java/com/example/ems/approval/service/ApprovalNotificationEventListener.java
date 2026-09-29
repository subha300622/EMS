package com.example.ems.approval.service;

import com.example.ems.approval.event.ApprovalActionRequiredEvent;
import com.example.ems.common.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ApprovalNotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(ApprovalNotificationEventListener.class);

    @Autowired
    private NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApprovalActionRequired(ApprovalActionRequiredEvent event) {
        log.info("Received ApprovalActionRequiredEvent AFTER_COMMIT for task {} (workflow: {}, approverEmpId: {})",
                event.getApprovalTaskId(), event.getWorkflowType(), event.getApproverEmployeeId());
        notificationService.createApprovalNotification(event);
    }
}
