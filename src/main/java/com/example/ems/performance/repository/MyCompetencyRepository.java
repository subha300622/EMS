package com.example.ems.performance.repository;

import com.example.ems.performance.entity.MyCompetency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyCompetencyRepository extends JpaRepository<MyCompetency, Long> {
    List<MyCompetency> findByEmployeeEmail(String email);
}
