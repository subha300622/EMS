package com.example.ems.reports.organization.mapper;

import com.example.ems.reports.export.ReportExportHistory;
import com.example.ems.reports.organization.dto.ExportHistoryResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class ExportHistoryMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public ExportHistoryResponse toResponse(ReportExportHistory entity) {
        String createdTime = entity.getCreatedAt() != null ? DATE_FORMATTER.format(entity.getCreatedAt()) : "N/A";
        return new ExportHistoryResponse(
                entity.getId(),
                entity.getReportName(),
                entity.getCreatedBy(),
                entity.getReportType().name(),
                entity.getExportFormat().name(),
                entity.getStatus().name(),
                entity.getDownloadUrl(),
                createdTime
        );
    }
}
