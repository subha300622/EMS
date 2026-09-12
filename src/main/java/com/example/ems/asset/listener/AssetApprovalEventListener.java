package com.example.ems.asset.listener;

import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.asset.event.AssetRequestApprovedEvent;
import com.example.ems.asset.event.AssetReturnRequestApprovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AssetApprovalEventListener {

    private static final Logger log = LoggerFactory.getLogger(AssetApprovalEventListener.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    public void handleApprovalWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (event == null || event.getBusinessReferenceType() == null) {
            return;
        }

        String refType = event.getBusinessReferenceType().toUpperCase();
        log.info("AssetApprovalEventListener received completed approval for businessType: {}, refId: {}", refType, event.getBusinessReferenceId());

        if (refType.contains("ASSET_REQUEST") && !refType.contains("RETURN")) {
            try {
                Long requestId = Long.parseLong(event.getBusinessReferenceId());
                AssetRequestApprovedEvent requestApprovedEvent = new AssetRequestApprovedEvent(
                        requestId,
                        null,
                        event.getOrganizationId(),
                        null,
                        "Approved by central workflow engine: " + event.getWorkflowInstanceId(),
                        "ApprovalEngine"
                );
                eventPublisher.publishEvent(requestApprovedEvent);
                log.info("Dispatched AssetRequestApprovedEvent for request #{}", requestId);
            } catch (NumberFormatException e) {
                log.warn("Invalid businessReferenceId for ASSET_REQUEST: {}", event.getBusinessReferenceId());
            }
        } else if (refType.contains("ASSET_RETURN_REQUEST") || refType.contains("RETURN")) {
            try {
                Long returnRequestId = Long.parseLong(event.getBusinessReferenceId());
                AssetReturnRequestApprovedEvent returnApprovedEvent = new AssetReturnRequestApprovedEvent(
                        returnRequestId,
                        null,
                        event.getOrganizationId(),
                        null,
                        "Approved by central workflow engine: " + event.getWorkflowInstanceId(),
                        "ApprovalEngine"
                );
                eventPublisher.publishEvent(returnApprovedEvent);
                log.info("Dispatched AssetReturnRequestApprovedEvent for return request #{}", returnRequestId);
            } catch (NumberFormatException e) {
                log.warn("Invalid businessReferenceId for ASSET_RETURN_REQUEST: {}", event.getBusinessReferenceId());
            }
        }
    }
}
