package com.example.ems.reports.export;

import com.example.ems.reports.common.ExportFormat;
import java.util.List;

public interface ReportExporter {
    ExportFormat getFormat();
    byte[] export(String title, List<String> headers, List<List<String>> rows);
}
