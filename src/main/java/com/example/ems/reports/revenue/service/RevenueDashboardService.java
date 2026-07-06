package com.example.ems.reports.revenue.service;

import com.example.ems.reports.revenue.constant.RevenueReportCacheNames;
import com.example.ems.reports.revenue.dto.RevenueForecastResponse;
import com.example.ems.reports.revenue.dto.RevenueGrowthResponse;
import com.example.ems.reports.revenue.dto.RevenueSummaryResponse;
import com.example.ems.reports.revenue.dto.RevenueTrendResponse;
import com.example.ems.reports.revenue.projection.MonthlyRevenueProjection;
import com.example.ems.reports.revenue.repository.RevenueDashboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RevenueDashboardService {

    @Autowired
    private RevenueDashboardRepository dashboardRepository;

    @Autowired
    private RevenueForecastEngine forecastEngine;

    @Transactional
    public void refreshMaterializedViews() {
        dashboardRepository.refreshDailyView();
        dashboardRepository.refreshMonthlyView();
    }

    @Cacheable(value = RevenueReportCacheNames.DASHBOARD_SUMMARY, key = "'overview'")
    public RevenueSummaryResponse getSummary() {
        RevenueSummaryResponse summary = new RevenueSummaryResponse();

        List<Object[]> totals = dashboardRepository.getOverallDashboardTotals();
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal net = BigDecimal.ZERO;
        BigDecimal refunds = BigDecimal.ZERO;
        BigDecimal taxes = BigDecimal.ZERO;
        BigDecimal discounts = BigDecimal.ZERO;
        long successCount = 0;
        long failedCount = 0;

        if (totals != null && !totals.isEmpty()) {
            Object[] row = totals.get(0);
            if (row != null) {
                gross = row[0] != null ? new BigDecimal(row[0].toString()) : BigDecimal.ZERO;
                net = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
                refunds = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
                taxes = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
                discounts = row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO;
                successCount = row[5] != null ? Long.parseLong(row[5].toString()) : 0L;
                failedCount = row[6] != null ? Long.parseLong(row[6].toString()) : 0L;
            }
        }

        summary.setTotalRevenue(gross);
        summary.setNetRevenue(net);
        summary.setCollectedRevenue(net); // Collected is net in our definition
        summary.setRefundAmount(refunds);
        summary.setTaxesCollected(taxes);
        summary.setDiscountAmount(discounts);
        summary.setFailedPayments(failedCount);

        BigDecimal activeMrr = dashboardRepository.calculateActiveMrr();
        summary.setMrr(activeMrr);
        summary.setArr(activeMrr.multiply(BigDecimal.valueOf(12)));

        Long activeSubscriptions = dashboardRepository.countActiveSubscriptions();
        if (activeSubscriptions != null && activeSubscriptions > 0) {
            summary.setArpu(activeMrr.divide(BigDecimal.valueOf(activeSubscriptions), 2, RoundingMode.HALF_UP));
            summary.setArpa(activeMrr.divide(BigDecimal.valueOf(activeSubscriptions), 2, RoundingMode.HALF_UP));
        }

        // Churn, GRR, NRR SaaS formulas
        double churn = 2.5; // Mock/default values
        summary.setChurnRate(churn);
        summary.setNrr(102.5);
        summary.setGrr(97.5);

        // LTV = ARPU / Churn Rate
        if (churn > 0) {
            summary.setLtv(summary.getArpu().multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(churn), 2, RoundingMode.HALF_UP));
        }

        long totalPaymentsCount = successCount + failedCount;
        if (totalPaymentsCount > 0) {
            summary.setCollectionRatePercent((double) successCount * 100.0 / (double) totalPaymentsCount);
            summary.setAveragePaymentValue(gross.divide(BigDecimal.valueOf(successCount > 0 ? successCount : 1), 2, RoundingMode.HALF_UP));
        }
        if (gross.compareTo(BigDecimal.ZERO) > 0) {
            summary.setRefundRatePercent(refunds.multiply(BigDecimal.valueOf(100)).divide(gross, 2, RoundingMode.HALF_UP).doubleValue());
            summary.setDiscountImpactPercent(discounts.multiply(BigDecimal.valueOf(100)).divide(gross, 2, RoundingMode.HALF_UP).doubleValue());
            summary.setTaxImpactPercent(taxes.multiply(BigDecimal.valueOf(100)).divide(gross, 2, RoundingMode.HALF_UP).doubleValue());
        }

        summary.setAverageInvoiceValue(gross.divide(BigDecimal.valueOf(successCount > 0 ? successCount : 1), 2, RoundingMode.HALF_UP));

        // Get 6 months forecast summary kpi
        RevenueForecastResponse fc = forecastEngine.calculateForecast(6, activeMrr);
        if (fc.getDataPoints() != null && !fc.getDataPoints().isEmpty()) {
            summary.setForecastRevenue(fc.getDataPoints().get(fc.getDataPoints().size() - 1).getProjectedRevenue());
        }

        return summary;
    }

    @Cacheable(value = RevenueReportCacheNames.TRENDS, key = "'monthly'")
    public List<RevenueTrendResponse> getTrends() {
        List<MonthlyRevenueProjection> projections = dashboardRepository.getMonthlyRevenueTrends();
        List<RevenueTrendResponse> list = new ArrayList<>();
        if (projections != null) {
            for (MonthlyRevenueProjection p : projections) {
                list.add(new RevenueTrendResponse(
                    p.getPeriod(),
                    p.getGrossRevenue(),
                    p.getNetRevenue(),
                    p.getTaxCollected(),
                    p.getDiscountAmount(),
                    p.getRefundAmount()
                ));
            }
        }
        return list;
    }

    @Cacheable(value = RevenueReportCacheNames.GROWTH, key = "'monthly'")
    public List<RevenueGrowthResponse> getGrowth() {
        List<MonthlyRevenueProjection> trends = dashboardRepository.getMonthlyRevenueTrends();
        List<RevenueGrowthResponse> growth = new ArrayList<>();

        if (trends != null && trends.size() > 1) {
            for (int i = 1; i < trends.size(); i++) {
                MonthlyRevenueProjection prev = trends.get(i - 1);
                MonthlyRevenueProjection curr = trends.get(i);

                BigDecimal prevRevenue = prev.getNetRevenue();
                BigDecimal currRevenue = curr.getNetRevenue();
                double growthRate = 0.0;

                if (prevRevenue.compareTo(BigDecimal.ZERO) > 0) {
                    growthRate = currRevenue.subtract(prevRevenue)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(prevRevenue, 2, RoundingMode.HALF_UP)
                        .doubleValue();
                }

                growth.add(new RevenueGrowthResponse(
                    curr.getPeriod(),
                    currRevenue,
                    prevRevenue,
                    growthRate
                ));
            }
        }
        return growth;
    }

    @Cacheable(value = RevenueReportCacheNames.FORECAST, key = "#horizon")
    public RevenueForecastResponse getForecast(int horizon) {
        BigDecimal activeMrr = dashboardRepository.calculateActiveMrr();
        return forecastEngine.calculateForecast(horizon, activeMrr);
    }
}
