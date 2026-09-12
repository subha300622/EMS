package com.example.ems.reports.subscription.export;

import com.example.ems.reports.subscription.dto.OrgSubscriptionListItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SubscriptionExportMapper {

    public List<String> getHeaders() {
        return Arrays.asList(
                "Organization ID", 
                "Organization Name", 
                "Plan Code", 
                "Status", 
                "Billing Cycle", 
                "Subscription Start Date", 
                "Subscription End Date", 
                "Amount", 
                "Auto Renew"
        );
    }

    public List<List<String>> mapToRows(List<OrgSubscriptionListItem> items) {
        List<List<String>> rows = new ArrayList<>();
        if (items == null) {
            return rows;
        }

        for (OrgSubscriptionListItem item : items) {
            rows.add(Arrays.asList(
                    String.valueOf(item.getOrganizationId() != null ? item.getOrganizationId() : ""),
                    item.getOrganizationName() != null ? item.getOrganizationName() : "",
                    item.getPlan() != null ? item.getPlan() : "",
                    item.getStatus() != null ? item.getStatus() : "",
                    item.getBillingCycle() != null ? item.getBillingCycle() : "",
                    item.getSubscriptionStart() != null ? item.getSubscriptionStart() : "",
                    item.getSubscriptionEnd() != null ? item.getSubscriptionEnd() : "",
                    item.getAmount() != null ? item.getAmount().toString() : "0.00",
                    String.valueOf(item.isAutoRenew())
            ));
        }
        return rows;
    }
}
