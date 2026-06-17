package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.SalaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, Long> {
    Optional<SalaryComponent> findByName(String name);
}
