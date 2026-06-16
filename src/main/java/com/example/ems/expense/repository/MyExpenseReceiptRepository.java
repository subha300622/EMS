package com.example.ems.expense.repository;

import com.example.ems.expense.entity.MyExpenseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MyExpenseReceiptRepository extends JpaRepository<MyExpenseReceipt, Long> {
    List<MyExpenseReceipt> findByEmployeeId(Long employeeId);
}
