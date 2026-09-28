package com.example.ems.common.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipientContext {

    private Long organizationId;

    /**
     * Priority 1: Explicit target user ID
     */
    private Long explicitUserId;

    /**
     * Priority 2: Approval task identifier or approver employee ID
     */
    private String approvalTaskId;
    private Long approverEmployeeId;

    /**
     * Priority 3: Entity assignee user or employee ID
     */
    private Long assigneeUserId;
    private Long assigneeEmployeeId;
    private String entityType;
    private String entityId;

    /**
     * Priority 4: Requester employee ID to resolve reporting manager hierarchy
     */
    private Long requesterEmployeeId;
    private boolean resolveManager;

    /**
     * Priority 5: Fallback permission name for authorized group fallback
     */
    private String requiredPermission;
}
