package com.example.ems.reports.revenue.export;

import com.example.ems.reports.revenue.dto.RevenuePlanDistributionResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("PLAN_REVENUE_ExportStrategy")
public class PlanRevenueExportStrategy extends AbstractRevenueExportStrategy {

    @Override
    public String getTitle() {
        return "Plan Revenue Distribution Report";
    }

    @Override
    public List<String> getHeaders() {
        return Arrays.asList(
            "Plan Code", "Organizations Count", "Subscribers", "Monthly Revenue", "Annual Revenue", "Lifetime Revenue", "Average Revenue", "Growth"
        );
    }

    @Override
    public List<List<String>> serializeRows(List<?> dataList) {
        List<List<String>> rows = new ArrayList<>();
        if (dataList == null) return rows;

        for (Object item : dataList) {
            if (item instanceof RevenuePlanDistributionResponse) {
                RevenuePlanDistributionResponse p = (RevenuePlanDistributionResponse) item;
                List<String> row = new ArrayList<>();
                row.add(formatObject(p.getPlan()));
                row.add(formatObject(p.getOrganizations()));
                row.add(formatObject(p.getSubscribers()));
                row.add(formatBigDecimal(p.getMonthlyRevenue()));
                row.add(formatBigDecimal(p.getAnnualRevenue()));
                row.add(formatBigDecimal(p.getLifetimeRevenue()));
                row.add(formatBigDecimal(p.getAverageRevenue()));
                row.add(p.getGrowth() != null ? p.getGrowth().toString() + "%" : "0%");
                rows.add(row);
            }
        }
        return rows;
    }
}
