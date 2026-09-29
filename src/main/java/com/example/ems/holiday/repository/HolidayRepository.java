package com.example.ems.holiday.repository;

import com.example.ems.holiday.entity.Holiday;
import com.example.ems.holiday.entity.HolidayStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    Optional<Holiday> findByHolidayIdAndOrganizationId(String holidayId, Long organizationId);

    boolean existsByOrganizationIdAndHolidayDate(Long organizationId, LocalDate holidayDate);

    boolean existsByOrganizationIdAndHolidayDateAndHolidayIdNot(Long organizationId, LocalDate holidayDate, String holidayId);

    Optional<Holiday> findByOrganizationIdAndHolidayDateAndStatus(Long organizationId, LocalDate holidayDate, HolidayStatus status);

    List<Holiday> findByOrganizationIdAndStatusAndHolidayDateBetweenOrderByHolidayDateAsc(
            Long organizationId, HolidayStatus status, LocalDate startDate, LocalDate endDate);

    Page<Holiday> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<Holiday> findByOrganizationIdAndHolidayDateBetween(
            Long organizationId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
