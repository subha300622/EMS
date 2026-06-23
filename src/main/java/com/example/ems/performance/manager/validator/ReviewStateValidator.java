package com.example.ems.performance.manager.validator;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.performance.manager.entity.ReviewStatus;
import org.springframework.stereotype.Component;

@Component
public class ReviewStateValidator {

    public void validateTransition(ReviewStatus currentStatus, ReviewStatus newStatus) {
        if (currentStatus == ReviewStatus.COMPLETED) {
            throw new BadRequestException("Review is finalized and cannot be modified or transitioned from COMPLETED status.");
        }

        // Additional optional state transition logic can go here.
        // For example, if we want to enforce the exact workflow:
        // NOT_STARTED -> SELF_REVIEW -> MANAGER_REVIEW -> SUBMITTED -> COMPLETED
        // Let's implement validation if desired, or keep it flexible but secure.
    }
}
