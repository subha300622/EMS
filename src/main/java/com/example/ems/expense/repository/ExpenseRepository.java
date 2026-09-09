package com.example.ems.expense.repository;

import com.example.ems.expense.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import com.example.ems.expense.entity.ExpenseStatus;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByStatus(ExpenseStatus status);
    List<Expense> findByStatusIn(List<ExpenseStatus> statuses);
    List<Expense> findByEmployeeId(Long employeeId);

    Optional<Expense> findByExpenseNumber(String expenseNumber);

    @Query("SELECT e FROM Expense e WHERE e.employee.id = :employeeId " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:categoryCode IS NULL OR e.category.code = :categoryCode) " +
           "AND (:fromDate IS NULL OR e.expenseDate >= :fromDate) " +
           "AND (:toDate IS NULL OR e.expenseDate <= :toDate)")
    Page<Expense> findByFilters(
        @Param("employeeId") Long employeeId,
        @Param("status") ExpenseStatus status,
        @Param("categoryCode") String categoryCode,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );
}

