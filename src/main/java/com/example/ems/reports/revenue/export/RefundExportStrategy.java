package com.example.ems.reports.revenue.export;

import com.example.ems.organization.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("REFUNDS_ExportStrategy")
public class RefundExportStrategy extends AbstractRevenueExportStrategy {

    @Override
    public String getTitle() {
        return "Refund Activity Report";
    }

    @Override
    public List<String> getHeaders() {
        return Arrays.asList(
            "Refund ID", "Organization", "Payment ID", "Refund Amount", "Refund Reason", "Refund Date", "Gateway"
        );
    }

    @Override
    public List<List<String>> serializeRows(List<?> dataList) {
        List<List<String>> rows = new ArrayList<>();
        if (dataList == null) return rows;

        for (Object item : dataList) {
            if (item instanceof Payment) {
                Payment p = (Payment) item;
                List<String> row = new ArrayList<>();
                row.add(formatObject(p.getGatewayRefundId()));
                row.add(p.getInvoice() != null && p.getInvoice().getSubscription() != null && p.getInvoice().getSubscription().getOrganization() != null 
                    ? p.getInvoice().getSubscription().getOrganization().getName() : "N/A");
                row.add(formatObject(p.getId()));
                row.add(formatBigDecimal(p.getRefundAmount()));
                row.add(formatObject(p.getRefundReason()));
                row.add(formatInstant(p.getRefundedAt()));
                row.add(formatObject(p.getGateway()));
                rows.add(row);
            }
        }
        return rows;
    }
}
