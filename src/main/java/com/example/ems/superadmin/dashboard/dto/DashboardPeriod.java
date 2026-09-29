package com.example.ems.superadmin.dashboard.dto;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public enum DashboardPeriod {
    TODAY,
    WEEK,
    MONTH,
    QUARTER,
    YEAR,
    CUSTOM;

    public record DateRange(LocalDate from, LocalDate to, LocalDate previousFrom, LocalDate previousTo) {}

    public DateRange resolveDateRange(LocalDate customFrom, LocalDate customTo) {
        LocalDate today = LocalDate.now();

        if (this == CUSTOM) {
            LocalDate from = customFrom != null ? customFrom : today.withDayOfMonth(1);
            LocalDate to = customTo != null ? customTo : today;
            long days = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
            LocalDate prevTo = from.minusDays(1);
            LocalDate prevFrom = prevTo.minusDays(Math.max(0, days - 1));
            return new DateRange(from, to, prevFrom, prevTo);
        }

        switch (this) {
            case TODAY -> {
                LocalDate prev = today.minusDays(1);
                return new DateRange(today, today, prev, prev);
            }
            case WEEK -> {
                LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
                LocalDate prevStart = startOfWeek.minusWeeks(1);
                LocalDate prevEnd = startOfWeek.minusDays(1);
                return new DateRange(startOfWeek, today, prevStart, prevEnd);
            }
            case QUARTER -> {
                int currentMonth = today.getMonthValue();
                int quarterFirstMonth = ((currentMonth - 1) / 3) * 3 + 1;
                LocalDate startOfQuarter = LocalDate.of(today.getYear(), quarterFirstMonth, 1);
                LocalDate prevStart = startOfQuarter.minusMonths(3);
                LocalDate prevEnd = startOfQuarter.minusDays(1);
                return new DateRange(startOfQuarter, today, prevStart, prevEnd);
            }
            case YEAR -> {
                LocalDate startOfYear = today.with(TemporalAdjusters.firstDayOfYear());
                LocalDate prevStart = startOfYear.minusYears(1);
                LocalDate prevEnd = startOfYear.minusDays(1);
                return new DateRange(startOfYear, today, prevStart, prevEnd);
            }
            case MONTH -> {
                LocalDate startOfMonth = today.withDayOfMonth(1);
                LocalDate prevStart = startOfMonth.minusMonths(1);
                LocalDate prevEnd = startOfMonth.minusDays(1);
                return new DateRange(startOfMonth, today, prevStart, prevEnd);
            }
            default -> {
                LocalDate startOfMonth = today.withDayOfMonth(1);
                return new DateRange(startOfMonth, today, startOfMonth.minusMonths(1), startOfMonth.minusDays(1));
            }
        }
    }
}
