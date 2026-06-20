package com.example.ems.expense.repository;

import com.example.ems.expense.entity.ExpenseAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseAuditLogRepository extends JpaRepository<ExpenseAuditLog, Long> {
    List<ExpenseAuditLog> findByExpenseIdOrderByUpdatedAtAsc(Long expenseId);
}
