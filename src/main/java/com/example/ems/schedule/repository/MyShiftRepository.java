package com.example.ems.schedule.repository;

import com.example.ems.schedule.entity.MyShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MyShiftRepository extends JpaRepository<MyShift, Long> {

    List<MyShift> findByEmployeeEmail(String email);

    List<MyShift> findByEmployeeEmailAndDateBetween(String email, LocalDate startDate, LocalDate endDate);

    Optional<MyShift> findByEmployeeEmailAndDate(String email, LocalDate date);

    @Query("SELECT s FROM MyShift s WHERE s.employee.email = :email " +
           "AND (:month IS NULL OR TO_CHAR(s.date, 'YYYY-MM') = :month) " +
           "AND (:status IS NULL OR s.status = :status)")
    List<MyShift> findByFilters(@Param("email") String email,
                                @Param("month") String month,
                                @Param("status") String status);
}
