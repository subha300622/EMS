package com.example.ems.reports.export;

import com.example.ems.reports.common.ExportFormat;
import com.example.ems.reports.exception.UnsupportedExportFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ExporterRegistry {

    private final Map<ExportFormat, ReportExporter> exporters = new EnumMap<>(ExportFormat.class);

    @Autowired
    public ExporterRegistry(List<ReportExporter> exporterList) {
        for (ReportExporter exporter : exporterList) {
            exporters.put(exporter.getFormat(), exporter);
        }
    }

    public ReportExporter getExporter(ExportFormat format) {
        ReportExporter exporter = exporters.get(format);
        if (exporter == null) {
            throw new UnsupportedExportFormatException("No exporter registered for format: " + format);
        }
        return exporter;
    }
}
