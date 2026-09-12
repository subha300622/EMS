package com.example.ems.reports.revenue.export;

import com.example.ems.reports.revenue.dto.RevenueForecastResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("FORECAST_ExportStrategy")
public class ForecastExportStrategy extends AbstractRevenueExportStrategy {

    @Override
    public String getTitle() {
        return "Revenue Forecast Projections";
    }

    @Override
    public List<String> getHeaders() {
        return Arrays.asList("Period / Month", "Projected Revenue");
    }

    @Override
    public List<List<String>> serializeRows(List<?> dataList) {
        List<List<String>> rows = new ArrayList<>();
        if (dataList == null) return rows;

        for (Object item : dataList) {
            if (item instanceof RevenueForecastResponse.ForecastDataPoint) {
                RevenueForecastResponse.ForecastDataPoint p = (RevenueForecastResponse.ForecastDataPoint) item;
                List<String> row = new ArrayList<>();
                row.add(formatObject(p.getPeriod()));
                row.add(formatBigDecimal(p.getProjectedRevenue()));
                rows.add(row);
            }
        }
        return rows;
    }
}
