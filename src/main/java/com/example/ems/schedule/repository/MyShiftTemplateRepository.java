package com.example.ems.schedule.repository;

import com.example.ems.schedule.entity.MyShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyShiftTemplateRepository extends JpaRepository<MyShiftTemplate, Long> {
}
