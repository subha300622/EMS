package com.example.ems.attendance.dto.late;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Query parameters for filtering Late Attendance report")
public class LateAttendanceQuery {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("date", "checkInTime", "status", "lateByMinutes");
    private static final String DEFAULT_SORT_FIELD = "date";

    @Schema(description = "Specific date filter", example = "2026-09-10")
    private LocalDate date;

    @Schema(description = "Start date for filtering", example = "2026-09-01")
    private LocalDate fromDate;

    @Schema(description = "End date for filtering", example = "2026-09-10")
    private LocalDate toDate;

    @Schema(description = "Filter by department ID", example = "1")
    private Long departmentId;

    @Schema(description = "Filter by team ID", example = "2")
    private Long teamId;

    @Schema(description = "Filter by employee ID", example = "125")
    private Long employeeId;

    @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0")
    private int page = 0;

    @Schema(description = "Page size", example = "20", defaultValue = "20")
    private int size = 20;

    @Schema(description = "Sort by field", example = "date", defaultValue = "date")
    private String sortBy = DEFAULT_SORT_FIELD;

    @Schema(description = "Sort direction (ASC or DESC)", example = "DESC", defaultValue = "DESC")
    private String sortDirection = "DESC";

    public LateAttendanceQuery() {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(0, page); }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = (size <= 0 || size > 100) ? 20 : size; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }

    public Pageable toPageable() {
        if (sortBy != null && !sortBy.isBlank() && !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field '" + sortBy + "'. Allowed fields: " + ALLOWED_SORT_FIELDS);
        }
        String safeSortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, safeSortField));
    }
}
