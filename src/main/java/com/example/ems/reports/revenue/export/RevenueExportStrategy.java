package com.example.ems.reports.revenue.export;

import java.util.List;

public interface RevenueExportStrategy {
    String getTitle();
    List<String> getHeaders();
    List<List<String>> serializeRows(List<?> dataList);
}
