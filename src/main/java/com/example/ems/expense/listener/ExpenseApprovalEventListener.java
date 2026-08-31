package com.example.ems.expense.listener;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalChangesRequestedEvent;
import com.example.ems.approval.event.ApprovalWorkflowCancelledEvent;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.event.ApprovalWorkflowRejectedEvent;
import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.entity.ExpenseStatus;
import com.example.ems.expense.entity.MyExpenseTimelineEvent;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.expense.repository.MyExpenseTimelineEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class ExpenseApprovalEventListener {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private MyExpenseTimelineEventRepository timelineEventRepository;

    @EventListener
    @Transactional
    public void handleWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (event.getWorkflowType() != WorkflowType.EXPENSE_APPROVAL) return;
        if (!"EXPENSE".equalsIgnoreCase(event.getBusinessReferenceType())) return;

        try {
            Long expenseId = Long.parseLong(event.getBusinessReferenceId());
            Expense expense = expenseRepository.findById(expenseId).orElse(null);
            if (expense == null) return;

            if (event.getStatus() == ApprovalStatus.APPROVED) {
                expense.setExpenseStatus(ExpenseStatus.APPROVED);
                expense.setStatus("APPROVED");
                expense.setUpdatedAt(LocalDateTime.now());
                expenseRepository.save(expense);

                timelineEventRepository.save(new MyExpenseTimelineEvent(expense, "APPROVED", "Approval Engine"));
            } else if (event.getStatus() == ApprovalStatus.REJECTED) {
                expense.setExpenseStatus(ExpenseStatus.REJECTED);
                expense.setStatus("REJECTED");
                expense.setUpdatedAt(LocalDateTime.now());
                expenseRepository.save(expense);

                timelineEventRepository.save(new MyExpenseTimelineEvent(expense, "REJECTED", "Approval Engine"));
            }
        } catch (NumberFormatException ignored) {}
    }

    @EventListener
    @Transactional
    public void handleWorkflowRejected(ApprovalWorkflowRejectedEvent event) {
        if (event.getWorkflowType() != WorkflowType.EXPENSE_APPROVAL) return;
        if (!"EXPENSE".equalsIgnoreCase(event.getBusinessReferenceType())) return;

        try {
            Long expenseId = Long.parseLong(event.getBusinessReferenceId());
            Expense expense = expenseRepository.findById(expenseId).orElse(null);
            if (expense == null) return;

            expense.setExpenseStatus(ExpenseStatus.REJECTED);
            expense.setStatus("REJECTED");
            if (event.getReason() != null) {
                expense.setRejectionReason(event.getReason());
            }
            expense.setUpdatedAt(LocalDateTime.now());
            expenseRepository.save(expense);

            timelineEventRepository.save(new MyExpenseTimelineEvent(expense, "REJECTED", "Approval Engine (" + event.getReason() + ")"));
        } catch (NumberFormatException ignored) {}
    }

    @EventListener
    @Transactional
    public void handleChangesRequested(ApprovalChangesRequestedEvent event) {
        if (event.getWorkflowType() != WorkflowType.EXPENSE_APPROVAL) return;
        if (!"EXPENSE".equalsIgnoreCase(event.getBusinessReferenceType())) return;

        try {
            Long expenseId = Long.parseLong(event.getBusinessReferenceId());
            Expense expense = expenseRepository.findById(expenseId).orElse(null);
            if (expense == null) return;

            expense.setExpenseStatus(ExpenseStatus.SENT_BACK);
            expense.setStatus("CHANGES_REQUESTED");
            if (event.getComments() != null) {
                expense.setSendBackReason(event.getComments());
            }
            expense.setUpdatedAt(LocalDateTime.now());
            expenseRepository.save(expense);

            timelineEventRepository.save(new MyExpenseTimelineEvent(expense, "CHANGES_REQUESTED", "Approval Engine (" + event.getComments() + ")"));
        } catch (NumberFormatException ignored) {}
    }

    @EventListener
    @Transactional
    public void handleWorkflowCancelled(ApprovalWorkflowCancelledEvent event) {
        if (event.getWorkflowType() != WorkflowType.EXPENSE_APPROVAL) return;
        if (!"EXPENSE".equalsIgnoreCase(event.getBusinessReferenceType())) return;

        try {
            Long expenseId = Long.parseLong(event.getBusinessReferenceId());
            Expense expense = expenseRepository.findById(expenseId).orElse(null);
            if (expense == null) return;

            expense.setStatus("WITHDRAWN");
            expense.setUpdatedAt(LocalDateTime.now());
            expenseRepository.save(expense);

            timelineEventRepository.save(new MyExpenseTimelineEvent(expense, "WITHDRAWN", "Employee (" + event.getReason() + ")"));
        } catch (NumberFormatException ignored) {}
    }
}
