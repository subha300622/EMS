package com.example.ems.schedule.swap.listener;

import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.schedule.swap.service.ScheduleSwapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ScheduleSwapEventListener {

    @Autowired
    private ScheduleSwapService scheduleSwapService;

    @EventListener
    public void handleApprovalWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (event.getWorkflowType() == WorkflowType.SCHEDULE_SWAP &&
            "SCHEDULE_SWAP_REQUEST".equalsIgnoreCase(event.getBusinessReferenceType())) {
            
            scheduleSwapService.executeAtomicSwap(event.getBusinessReferenceId());
        }
    }
}
