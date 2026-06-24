package com.example.ems.schedule.repository;

import com.example.ems.schedule.entity.MyScheduleChangeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyScheduleChangeRequestRepository extends JpaRepository<MyScheduleChangeRequest, Long> {

    List<MyScheduleChangeRequest> findByEmployeeEmail(String email);

    List<MyScheduleChangeRequest> findByEmployeeIdIn(List<Long> employeeIds);

    @Query("SELECT r FROM MyScheduleChangeRequest r WHERE r.employee.email = :email " +
           "AND (:status IS NULL OR r.status = :status)")
    Page<MyScheduleChangeRequest> findByFilters(@Param("email") String email,
                                                 @Param("status") String status,
                                                 Pageable pageable);
}
