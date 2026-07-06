package com.example.ems.reports.revenue.export;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public abstract class AbstractRevenueExportStrategy implements RevenueExportStrategy {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    protected String formatBigDecimal(BigDecimal amount) {
        if (amount == null) return "0.00";
        return amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    protected String formatInstant(Instant instant) {
        if (instant == null) return "N/A";
        return DATE_FORMATTER.format(instant);
    }

    protected String formatObject(Object val) {
        if (val == null) return "N/A";
        return val.toString();
    }
}
