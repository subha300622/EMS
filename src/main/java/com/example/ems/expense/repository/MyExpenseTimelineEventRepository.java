package com.example.ems.expense.repository;

import com.example.ems.expense.entity.MyExpenseTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MyExpenseTimelineEventRepository extends JpaRepository<MyExpenseTimelineEvent, Long> {
    List<MyExpenseTimelineEvent> findByExpenseIdOrderByDateAsc(Long expenseId);
}
