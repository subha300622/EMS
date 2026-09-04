package com.example.ems.reports.export;

import com.example.ems.reports.common.ExportFormat;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvReportExporter implements ReportExporter {

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.CSV;
    }

    @Override
    public byte[] export(String title, List<String> headers, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers)).append("\n");
        for (List<String> row : rows) {
            StringBuilder rowSb = new StringBuilder();
            for (int i = 0; i < row.size(); i++) {
                rowSb.append(escapeCsv(row.get(i)));
                if (i < row.size() - 1) {
                    rowSb.append(",");
                }
            }
            sb.append(rowSb).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
