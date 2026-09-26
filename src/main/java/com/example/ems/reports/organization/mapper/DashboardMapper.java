package com.example.ems.reports.organization.mapper;

import com.example.ems.reports.organization.dto.DashboardSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class DashboardMapper {

    public DashboardSummaryResponse toSummaryResponse(
            long total, long active, long trial, long suspended,
            long employees, long activeUsers, double growth, double storage) {
        return new DashboardSummaryResponse(total, active, trial, suspended, employees, activeUsers, growth, storage);
    }
}
