package com.example.ems.schedule.repository;

import com.example.ems.schedule.entity.MyEmployeeAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyEmployeeAvailabilityRepository extends JpaRepository<MyEmployeeAvailability, Long> {
    List<MyEmployeeAvailability> findByEmployeeEmail(String email);
    void deleteByEmployeeEmail(String email);
}
