package com.example.ems.holiday.service;

import com.example.ems.auth.entity.User;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.holiday.dto.*;
import com.example.ems.holiday.entity.Holiday;
import com.example.ems.holiday.entity.HolidayStatus;
import com.example.ems.holiday.repository.HolidayRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class HolidayService {

    private static final AtomicLong ID_COUNTER = new AtomicLong(System.currentTimeMillis() % 100000);

    @Autowired
    private HolidayRepository holidayRepository;

    public Long resolveOrganizationId(User user) {
        Long tenantOrgId = TenantContext.getOrganizationId();
        if (tenantOrgId != null) {
            return tenantOrgId;
        }
        if (user != null && user.getOrganization() != null) {
            return user.getOrganization().getId();
        }
        throw new IllegalStateException("Organization context not found");
    }

    private HolidayResponseDto mapToResponseDto(Holiday holiday) {
        return new HolidayResponseDto(
                holiday.getHolidayId(),
                holiday.getName(),
                holiday.getHolidayDate(),
                holiday.getDescription(),
                holiday.getStatus()
        );
    }

    @Transactional
    public HolidayResponseDto createHoliday(User currentUser, HolidayCreateRequest request) {
        Long orgId = resolveOrganizationId(currentUser);

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
        if (request.getHolidayDate() == null) {
            throw new IllegalArgumentException("holidayDate is required");
        }

        // Duplicate holiday on date check
        if (holidayRepository.existsByOrganizationIdAndHolidayDate(orgId, request.getHolidayDate())) {
            throw new IllegalStateException("A holiday already exists on this date: " + request.getHolidayDate());
        }

        String holidayId = "HOL-" + String.format("%05d", Math.abs(ID_COUNTER.incrementAndGet()) % 100000);

        Holiday holiday = new Holiday();
        holiday.setHolidayId(holidayId);
        holiday.setOrganizationId(orgId);
        holiday.setName(request.getName().trim());
        holiday.setHolidayDate(request.getHolidayDate());
        holiday.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        holiday.setStatus(HolidayStatus.ACTIVE);
        if (currentUser != null) {
            holiday.setCreatedBy(currentUser.getWorkEmail());
            holiday.setUpdatedBy(currentUser.getWorkEmail());
        }

        holiday = holidayRepository.save(holiday);
        return mapToResponseDto(holiday);
    }

    @Transactional(readOnly = true)
    public HolidayResponseDto getHolidayById(User currentUser, String holidayId) {
        Long orgId = resolveOrganizationId(currentUser);

        Holiday holiday = holidayRepository.findByHolidayIdAndOrganizationId(holidayId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found with ID: " + holidayId));

        return mapToResponseDto(holiday);
    }

    @Transactional(readOnly = true)
    public HolidayListResponse listHolidays(User currentUser, Integer year, LocalDate fromDate, LocalDate toDate, int page, int size) {
        Long orgId = resolveOrganizationId(currentUser);
        Pageable pageable = PageRequest.of(page, size, Sort.by("holidayDate").ascending());

        Page<Holiday> holidayPage;

        if (year != null) {
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year, 12, 31);
            holidayPage = holidayRepository.findByOrganizationIdAndHolidayDateBetween(orgId, start, end, pageable);
        } else if (fromDate != null && toDate != null) {
            holidayPage = holidayRepository.findByOrganizationIdAndHolidayDateBetween(orgId, fromDate, toDate, pageable);
        } else {
            holidayPage = holidayRepository.findByOrganizationId(orgId, pageable);
        }

        List<HolidayResponseDto> dtos = holidayPage.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return new HolidayListResponse(
                dtos,
                holidayPage.getNumber(),
                holidayPage.getSize(),
                holidayPage.getTotalElements(),
                holidayPage.getTotalPages()
        );
    }

    @Transactional
    public HolidayResponseDto updateHoliday(User currentUser, String holidayId, HolidayUpdateRequest request) {
        Long orgId = resolveOrganizationId(currentUser);

        Holiday holiday = holidayRepository.findByHolidayIdAndOrganizationId(holidayId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found with ID: " + holidayId));

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
        if (request.getHolidayDate() == null) {
            throw new IllegalArgumentException("holidayDate is required");
        }

        // Duplicate date check across other holidays
        if (holidayRepository.existsByOrganizationIdAndHolidayDateAndHolidayIdNot(orgId, request.getHolidayDate(), holidayId)) {
            throw new IllegalStateException("Another holiday already exists on this date: " + request.getHolidayDate());
        }

        holiday.setName(request.getName().trim());
        holiday.setHolidayDate(request.getHolidayDate());
        holiday.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (currentUser != null) {
            holiday.setUpdatedBy(currentUser.getWorkEmail());
        }

        holiday = holidayRepository.save(holiday);
        return mapToResponseDto(holiday);
    }

    @Transactional
    public HolidayResponseDto deleteHoliday(User currentUser, String holidayId) {
        Long orgId = resolveOrganizationId(currentUser);

        Holiday holiday = holidayRepository.findByHolidayIdAndOrganizationId(holidayId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found with ID: " + holidayId));

        holiday.setStatus(HolidayStatus.INACTIVE);
        if (currentUser != null) {
            holiday.setUpdatedBy(currentUser.getWorkEmail());
        }

        holiday = holidayRepository.save(holiday);
        return mapToResponseDto(holiday);
    }

    @Transactional(readOnly = true)
    public HolidayCheckResponse checkHoliday(User currentUser, LocalDate date) {
        Long orgId = resolveOrganizationId(currentUser);

        return holidayRepository.findByOrganizationIdAndHolidayDateAndStatus(orgId, date, HolidayStatus.ACTIVE)
                .map(h -> new HolidayCheckResponse(date, true, h.getHolidayId(), h.getName()))
                .orElseGet(() -> new HolidayCheckResponse(date, false, null, null));
    }

    @Transactional(readOnly = true)
    public HolidayCalendarResponse getHolidayCalendar(User currentUser, int year) {
        Long orgId = resolveOrganizationId(currentUser);

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        List<Holiday> holidays = holidayRepository.findByOrganizationIdAndStatusAndHolidayDateBetweenOrderByHolidayDateAsc(
                orgId, HolidayStatus.ACTIVE, start, end);

        List<HolidayCalendarResponse.HolidayCalendarItem> items = holidays.stream()
                .map(h -> new HolidayCalendarResponse.HolidayCalendarItem(h.getHolidayId(), h.getHolidayDate(), h.getName()))
                .collect(Collectors.toList());

        return new HolidayCalendarResponse(year, items);
    }
}
