package com.example.ems.reports.revenue.export;

import com.example.ems.organization.entity.SubscriptionInvoice;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("INVOICES_ExportStrategy")
public class InvoiceExportStrategy extends AbstractRevenueExportStrategy {

    @Override
    public String getTitle() {
        return "Invoice Summary Report";
    }

    @Override
    public List<String> getHeaders() {
        return Arrays.asList(
            "Invoice Number", "Organization", "Subscription Plan", "Issue Date", "Due Date",
            "Status", "Subtotal", "Tax", "Discount", "Grand Total"
        );
    }

    @Override
    public List<List<String>> serializeRows(List<?> dataList) {
        List<List<String>> rows = new ArrayList<>();
        if (dataList == null) return rows;

        for (Object item : dataList) {
            if (item instanceof SubscriptionInvoice) {
                SubscriptionInvoice inv = (SubscriptionInvoice) item;
                List<String> row = new ArrayList<>();
                row.add(formatObject(inv.getInvoiceNumber()));
                row.add(inv.getSubscription() != null && inv.getSubscription().getOrganization() != null 
                    ? inv.getSubscription().getOrganization().getName() : "N/A");
                row.add(inv.getSubscription() != null ? inv.getSubscription().getPlanCode() : "N/A");
                row.add(formatInstant(inv.getIssuedAt()));
                row.add(inv.getDueAt() != null ? inv.getDueAt().toString() : "N/A");
                row.add(inv.getStatus() != null ? inv.getStatus().name() : "N/A");

                java.math.BigDecimal subtotal = inv.getAmount().subtract(inv.getTax() != null ? inv.getTax() : java.math.BigDecimal.ZERO);
                row.add(formatBigDecimal(subtotal));
                row.add(formatBigDecimal(inv.getTax()));
                row.add(formatBigDecimal(inv.getDiscount()));
                row.add(formatBigDecimal(inv.getAmount()));
                rows.add(row);
            }
        }
        return rows;
    }
}
