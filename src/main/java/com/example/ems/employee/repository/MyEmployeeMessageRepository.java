package com.example.ems.employee.repository;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.MyEmployeeMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MyEmployeeMessageRepository extends JpaRepository<MyEmployeeMessage, Long> {
    List<MyEmployeeMessage> findByRecipient(Employee recipient);
    List<MyEmployeeMessage> findBySender(Employee sender);
}
