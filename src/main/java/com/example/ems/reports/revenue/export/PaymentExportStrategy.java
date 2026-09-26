package com.example.ems.reports.revenue.export;

import com.example.ems.organization.entity.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("PAYMENTS_ExportStrategy")
public class PaymentExportStrategy extends AbstractRevenueExportStrategy {

    @Override
    public String getTitle() {
        return "Payments Transactions Report";
    }

    @Override
    public List<String> getHeaders() {
        return Arrays.asList(
            "Transaction ID", "Organization", "Subscription Plan", "Invoice Number",
            "Gateway", "Status", "Currency", "Amount", "Tax", "Discount", "Net Amount", "Paid Date"
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
                row.add(formatObject(p.getId()));
                row.add(p.getInvoice() != null && p.getInvoice().getSubscription() != null && p.getInvoice().getSubscription().getOrganization() != null 
                    ? p.getInvoice().getSubscription().getOrganization().getName() : "N/A");
                row.add(p.getInvoice() != null && p.getInvoice().getSubscription() != null 
                    ? p.getInvoice().getSubscription().getPlanCode() : "N/A");
                row.add(p.getInvoice() != null ? p.getInvoice().getInvoiceNumber() : "N/A");
                row.add(formatObject(p.getGateway()));
                row.add(formatObject(p.getStatus()));
                row.add(formatObject(p.getCurrency()));
                row.add(formatBigDecimal(p.getAmount()));
                
                BigDecimal tax = p.getInvoice() != null ? p.getInvoice().getTax() : BigDecimal.ZERO;
                BigDecimal discount = p.getInvoice() != null ? p.getInvoice().getDiscount() : BigDecimal.ZERO;
                BigDecimal net = p.getAmount().subtract(tax != null ? tax : BigDecimal.ZERO);

                row.add(formatBigDecimal(tax));
                row.add(formatBigDecimal(discount));
                row.add(formatBigDecimal(net));
                row.add(formatInstant(p.getPaidAt()));
                rows.add(row);
            }
        }
        return rows;
    }
}
