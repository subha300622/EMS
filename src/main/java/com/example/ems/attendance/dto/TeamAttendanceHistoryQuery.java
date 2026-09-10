package com.example.ems.attendance.dto;

import com.example.ems.attendance.entity.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Query parameters for filtering Team Attendance history")
public class TeamAttendanceHistoryQuery {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("date", "checkInTime", "checkOutTime", "status");
    private static final String DEFAULT_SORT_FIELD = "date";

    @Schema(description = "Start date for filtering (inclusive)", example = "2026-09-01")
    private LocalDate fromDate;

    @Schema(description = "End date for filtering (inclusive)", example = "2026-09-10")
    private LocalDate toDate;

    @Schema(description = "Attendance status filter", example = "COMPLETED")
    private AttendanceStatus status;

    @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0")
    private int page = 0;

    @Schema(description = "Page size", example = "20", defaultValue = "20")
    private int size = 20;

    @Schema(description = "Field to sort by (date, checkInTime, checkOutTime, status)", example = "date", defaultValue = "date")
    private String sortBy = DEFAULT_SORT_FIELD;

    @Schema(description = "Sort direction (ASC or DESC)", example = "DESC", defaultValue = "DESC")
    private String sortDirection = "DESC";

    public TeamAttendanceHistoryQuery() {}

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = (size <= 0 || size > 100) ? 20 : size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public Pageable toPageable() {
        if (sortBy != null && !sortBy.isBlank() && !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field '" + sortBy + "'. Allowed fields: " + ALLOWED_SORT_FIELDS);
        }
        String safeSortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, safeSortField));
    }
}
