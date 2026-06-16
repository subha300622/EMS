package com.example.ems.expense.repository;

import com.example.ems.expense.entity.MyExpensePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyExpensePolicyRepository extends JpaRepository<MyExpensePolicy, Long> {
}
