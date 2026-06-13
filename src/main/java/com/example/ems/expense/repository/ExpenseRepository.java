package com.example.ems.expense.repository;

import com.example.ems.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByStatus(String status);
    List<Expense> findByEmployeeId(Long employeeId);
}
