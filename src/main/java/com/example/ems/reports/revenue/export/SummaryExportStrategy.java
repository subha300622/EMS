package com.example.ems.reports.revenue.export;

import com.example.ems.reports.revenue.dto.RevenueSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("SUMMARY_ExportStrategy")
public class SummaryExportStrategy extends AbstractRevenueExportStrategy {

    @Override
    public String getTitle() {
        return "Revenue Summary KPIs";
    }

    @Override
    public List<String> getHeaders() {
        return Arrays.asList("Metric Name", "Metric Value");
    }

    @Override
    public List<List<String>> serializeRows(List<?> dataList) {
        List<List<String>> rows = new ArrayList<>();
        if (dataList == null || dataList.isEmpty()) return rows;

        Object item = dataList.get(0);
        if (item instanceof RevenueSummaryResponse) {
            RevenueSummaryResponse s = (RevenueSummaryResponse) item;
            addMetricRow(rows, "Total Revenue", formatBigDecimal(s.getTotalRevenue()));
            addMetricRow(rows, "Net Revenue", formatBigDecimal(s.getNetRevenue()));
            addMetricRow(rows, "Collected Revenue", formatBigDecimal(s.getCollectedRevenue()));
            addMetricRow(rows, "Pending Revenue", formatBigDecimal(s.getPendingRevenue()));
            addMetricRow(rows, "Failed Payments", formatObject(s.getFailedPayments()));
            addMetricRow(rows, "Refund Amount", formatBigDecimal(s.getRefundAmount()));
            addMetricRow(rows, "Taxes Collected", formatBigDecimal(s.getTaxesCollected()));
            addMetricRow(rows, "Discount Amount", formatBigDecimal(s.getDiscountAmount()));
            addMetricRow(rows, "MRR (Monthly Recurring Revenue)", formatBigDecimal(s.getMrr()));
            addMetricRow(rows, "ARR (Annual Recurring Revenue)", formatBigDecimal(s.getArr()));
            addMetricRow(rows, "ARPU (Average Rev Per Org)", formatBigDecimal(s.getArpu()));
            addMetricRow(rows, "ARPA", formatBigDecimal(s.getArpa()));
            addMetricRow(rows, "LTV", formatBigDecimal(s.getLtv()));
            addMetricRow(rows, "Revenue Growth %", formatObject(s.getRevenueGrowthPercent()) + "%");
            addMetricRow(rows, "Refund Rate %", formatObject(s.getRefundRatePercent()) + "%");
            addMetricRow(rows, "Collection Rate %", formatObject(s.getCollectionRatePercent()) + "%");
            addMetricRow(rows, "Average Invoice Value", formatBigDecimal(s.getAverageInvoiceValue()));
            addMetricRow(rows, "Average Payment Value", formatBigDecimal(s.getAveragePaymentValue()));
            addMetricRow(rows, "Forecast Revenue", formatBigDecimal(s.getForecastRevenue()));
            addMetricRow(rows, "NRR (Net Revenue Retention)", formatObject(s.getNrr()) + "%");
            addMetricRow(rows, "GRR (Gross Revenue Retention)", formatObject(s.getGrr()) + "%");
            addMetricRow(rows, "Revenue Churn Rate", formatObject(s.getChurnRate()) + "%");
        }
        return rows;
    }

    private void addMetricRow(List<List<String>> rows, String name, String value) {
        rows.add(Arrays.asList(name, value));
    }
}
