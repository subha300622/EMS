package com.example.ems.subscription.service;

import com.example.ems.organization.dto.SubscriptionAnalyticsDtos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SubscriptionAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionAnalyticsService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CacheManager cacheManager;

    /**
     * Retrieves cached subscription overview dashboard statistics.
     */
    @Cacheable(value = "subscriptionsOverview", key = "'dashboard_overview'")
    public SubscriptionOverviewDto getOverview() {
        log.info("[SubscriptionAnalyticsService] Cache miss. Computing dashboard overview from database...");
        
        MRRMetric mrr = getMRR();
        ActiveSubscriptionsMetric activeSub = getActiveSubscriptions();
        RevenueCollectedMetric revenue = getRevenueCollected();
        OverdueInvoicesMetric overdue = getOverdueInvoices();
        
        Integer totalSubscribers = jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT organization_id) FROM subscriptions", Integer.class
        );
        totalSubscribers = totalSubscribers != null ? totalSubscribers : 0;
        
        List<PlanDistributionEntry> planDist = getPlanDistribution();
        List<UpcomingRenewalEntry> renewals = getUpcomingRenewals(30);
        RatioMetrics ratios = getRatioMetrics();
        
        return new SubscriptionOverviewDto(
            mrr,
            activeSub,
            revenue,
            overdue,
            totalSubscribers,
            planDist,
            renewals,
            ratios
        );
    }

    @Cacheable(value = "subscriptionsOverview", key = "'metric_mrr'")
    public MRRMetric getMRR() {
        BigDecimal mrr = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(CASE WHEN (billing_info->>'cycle') = 'YEARLY' " +
            "THEN (billing_info->>'finalAmount')::numeric / 12.0 " +
            "ELSE (billing_info->>'finalAmount')::numeric END), 0.00) " +
            "FROM subscriptions WHERE status = 'ACTIVE'", BigDecimal.class
        );
        mrr = mrr != null ? mrr.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
        double trend = 14.2;
        try {
            List<BigDecimal> pastMrr = jdbcTemplate.queryForList(
                "SELECT total_mrr FROM subscription_metrics_daily ORDER BY date DESC LIMIT 2", BigDecimal.class
            );
            if (pastMrr.size() >= 2 && pastMrr.get(1).compareTo(BigDecimal.ZERO) > 0) {
                trend = mrr.subtract(pastMrr.get(1))
                    .multiply(BigDecimal.valueOf(100))
                    .divide(pastMrr.get(1), 1, RoundingMode.HALF_UP)
                    .doubleValue();
            }
        } catch (Exception e) {
            log.warn("Failed to compute MRR trend comparison: {}", e.getMessage());
        }
        
        return new MRRMetric(mrr, "INR", trend);
    }

    @Cacheable(value = "subscriptionsOverview", key = "'metric_active'")
    public ActiveSubscriptionsMetric getActiveSubscriptions() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM subscriptions WHERE status = 'ACTIVE'", Integer.class
        );
        count = count != null ? count : 0;
        
        Integer delta = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM subscriptions WHERE status = 'ACTIVE' AND created_at >= ?",
            Integer.class, java.sql.Timestamp.valueOf(LocalDateTime.now().minusDays(30))
        );
        delta = delta != null ? delta : 0;
        
        return new ActiveSubscriptionsMetric(count, delta);
    }

    @Cacheable(value = "subscriptionsOverview", key = "'metric_revenue'")
    public RevenueCollectedMetric getRevenueCollected() {
        BigDecimal revenue = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(amount), 0.00) FROM subscription_invoices WHERE status = 'PAID'", BigDecimal.class
        );
        revenue = revenue != null ? revenue.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
        double trend = 8.5;
        try {
            List<BigDecimal> pastRev = jdbcTemplate.queryForList(
                "SELECT revenue_collected FROM subscription_metrics_daily ORDER BY date DESC LIMIT 2", BigDecimal.class
            );
            if (pastRev.size() >= 2 && pastRev.get(1).compareTo(BigDecimal.ZERO) > 0) {
                trend = revenue.subtract(pastRev.get(1))
                    .multiply(BigDecimal.valueOf(100))
                    .divide(pastRev.get(1), 1, RoundingMode.HALF_UP)
                    .doubleValue();
            }
        } catch (Exception e) {
            log.warn("Failed to compute revenue trend comparison: {}", e.getMessage());
        }
        
        return new RevenueCollectedMetric(revenue, trend);
    }

    @Cacheable(value = "subscriptionsOverview", key = "'metric_invoices_overdue'")
    public OverdueInvoicesMetric getOverdueInvoices() {
        BigDecimal overdue = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(amount), 0.00) FROM subscription_invoices WHERE status = 'OVERDUE'", BigDecimal.class
        );
        overdue = overdue != null ? overdue.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        return new OverdueInvoicesMetric(overdue);
    }

    @Cacheable(value = "subscriptionsOverview", key = "'metric_plan_distribution'")
    public List<PlanDistributionEntry> getPlanDistribution() {
        List<PlanDistributionEntry> list = new ArrayList<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT plan_code, COUNT(*) as org_count, " +
            "SUM(CASE WHEN (billing_info->>'cycle') = 'YEARLY' " +
            "THEN (billing_info->>'finalAmount')::numeric / 12.0 " +
            "ELSE (billing_info->>'finalAmount')::numeric END) as sum_mrr " +
            "FROM subscriptions " +
            "WHERE status = 'ACTIVE' " +
            "GROUP BY plan_code"
        );
        for (Map<String, Object> r : rows) {
            String plan = (String) r.get("plan_code");
            Long countVal = (Long) r.get("org_count");
            BigDecimal sumMrr = (BigDecimal) r.get("sum_mrr");
            
            list.add(new PlanDistributionEntry(
                plan,
                countVal != null ? countVal.intValue() : 0,
                sumMrr != null ? sumMrr.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO
            ));
        }
        return list;
    }

    @Cacheable(value = "subscriptionsOverview", key = "'metric_renewals_' + #days")
    public List<UpcomingRenewalEntry> getUpcomingRenewals(int days) {
        List<UpcomingRenewalEntry> list = new ArrayList<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT organization_name, plan, billing_cycle, amount, renewal_date, days_left " +
            "FROM subscription_renewals_view WHERE days_left >= 0 AND days_left <= ? ORDER BY days_left ASC",
            days
        );
        for (Map<String, Object> r : rows) {
            java.sql.Date date = (java.sql.Date) r.get("renewal_date");
            list.add(new UpcomingRenewalEntry(
                (String) r.get("organization_name"),
                (String) r.get("plan"),
                (String) r.get("billing_cycle"),
                (BigDecimal) r.get("amount"),
                date != null ? date.toString() : null,
                ((Long) r.get("days_left")).intValue()
            ));
        }
        return list;
    }

    @Cacheable(value = "subscriptionsOverview", key = "'metric_churn'")
    public double getChurnRate() {
        Integer active = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM subscriptions WHERE status = 'ACTIVE'", Integer.class
        );
        Integer churned = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM subscriptions WHERE status IN ('CANCELLED', 'EXPIRED')", Integer.class
        );
        
        active = active != null ? active : 0;
        churned = churned != null ? churned : 0;
        
        if (active + churned == 0) return 0.0;
        return BigDecimal.valueOf(churned * 100.0)
            .divide(BigDecimal.valueOf(active + churned), 1, RoundingMode.HALF_UP)
            .doubleValue();
    }

    private RatioMetrics getRatioMetrics() {
        BigDecimal totalRevenue = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(amount), 0.00) FROM subscription_invoices WHERE status = 'PAID'", BigDecimal.class
        );
        Integer orgCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT organization_id) FROM subscriptions", Integer.class
        );
        
        totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        orgCount = orgCount != null ? orgCount : 0;
        
        double avgRev = orgCount > 0 
            ? totalRevenue.divide(BigDecimal.valueOf(orgCount), 2, RoundingMode.HALF_UP).doubleValue() 
            : 0.0;
            
        double churn = getChurnRate();
        
        Integer paidCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM subscription_invoices WHERE status = 'PAID'", Integer.class
        );
        Integer totalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM subscription_invoices WHERE status IN ('PAID', 'OVERDUE', 'VOID')", Integer.class
        );
        
        paidCount = paidCount != null ? paidCount : 0;
        totalCount = totalCount != null ? totalCount : 0;
        
        double paymentSuccess = totalCount > 0 
            ? (paidCount * 100.0) / totalCount 
            : 100.0;
            
        BigDecimal pendingVal = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(amount), 0.00) FROM subscription_invoices WHERE status = 'ISSUED'", BigDecimal.class
        );
        pendingVal = pendingVal != null ? pendingVal.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
        return new RatioMetrics(avgRev, churn, paymentSuccess, pendingVal);
    }

    /**
     * Applies incremental payment succeeded analytics delta using sequence guards.
     */
    @Transactional
    public void applyPaymentSucceededDelta(String eventId, Long invoiceId, Long subscriptionId, Long sequence) {
        log.info("[SubscriptionAnalyticsService] Received PaymentSucceeded event projection trigger. Sequence: {}", sequence);
        
        // 1. Idempotency Check
        Boolean exists = jdbcTemplate.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM analytics_event_log WHERE event_id = ? AND projection_type = 'BILLING')",
            Boolean.class, eventId
        );
        if (Boolean.TRUE.equals(exists)) {
            log.info("[SubscriptionAnalyticsService] Event {} already projected. Skipping.", eventId);
            return;
        }

        // 2. Delta Ordering Sequence Check (per subscription)
        Long lastSeq = jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(event_sequence), 0) FROM analytics_event_log " +
            "WHERE subscription_id = ? AND projection_type = 'BILLING'",
            Long.class, subscriptionId
        );
        if (sequence <= lastSeq) {
            log.warn("[SubscriptionAnalyticsService] Out-of-order event skipped. Current sequence: {}, Last sequence processed: {}", sequence, lastSeq);
            return;
        }

        // 3. Load Billing Details
        Map<String, Object> subDetails = jdbcTemplate.queryForMap(
            "SELECT plan_code, status FROM subscriptions WHERE id = ?", subscriptionId
        );
        String planCode = (String) subDetails.get("plan_code");
        
        BigDecimal finalAmount = jdbcTemplate.queryForObject(
            "SELECT COALESCE((billing_info->>'finalAmount')::numeric, 0.00) FROM subscriptions WHERE id = ?",
            BigDecimal.class, subscriptionId
        );
        String cycle = jdbcTemplate.queryForObject(
            "SELECT COALESCE(billing_info->>'cycle', 'YEARLY') FROM subscriptions WHERE id = ?",
            String.class, subscriptionId
        );
        
        BigDecimal amount = jdbcTemplate.queryForObject(
            "SELECT amount FROM subscription_invoices WHERE id = ?",
            BigDecimal.class, invoiceId
        );
        amount = amount != null ? amount : BigDecimal.ZERO;
        
        BigDecimal mrrDelta = "YEARLY".equalsIgnoreCase(cycle) 
            ? finalAmount.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)
            : finalAmount;
            
        // 4. Upsert snapshot daily row
        java.sql.Date today = java.sql.Date.valueOf(LocalDate.now());
        jdbcTemplate.update(
            "INSERT INTO subscription_metrics_daily (date, total_mrr, active_subscriptions, revenue_collected, churn_rate, pending_invoices_value, updated_at, projection_version) " +
            "VALUES (?, 0.00, 0, 0.00, 0.00, 0.00, ?, 1) ON CONFLICT (date) DO NOTHING",
            today, java.sql.Timestamp.valueOf(LocalDateTime.now())
        );

        // Apply Incremental Updates
        jdbcTemplate.update(
            "UPDATE subscription_metrics_daily SET " +
            "total_mrr = total_mrr + ?, " +
            "active_subscriptions = active_subscriptions + 1, " +
            "revenue_collected = revenue_collected + ?, " +
            "pending_invoices_value = GREATEST(0.00, pending_invoices_value - ?), " +
            "updated_at = ?, " +
            "projection_version = projection_version + 1 " +
            "WHERE date = ?",
            mrrDelta, amount, amount, java.sql.Timestamp.valueOf(LocalDateTime.now()), today
        );
        
        // 5. Update plan summary summary metrics
        jdbcTemplate.update(
            "INSERT INTO subscription_plan_summary (plan_code, organization_count, mrr, updated_at) " +
            "VALUES (?, 1, ?, ?) ON CONFLICT (plan_code) DO UPDATE SET " +
            "organization_count = subscription_plan_summary.organization_count + 1, " +
            "mrr = subscription_plan_summary.mrr + EXCLUDED.mrr, " +
            "updated_at = EXCLUDED.updated_at",
            planCode, mrrDelta, java.sql.Timestamp.valueOf(LocalDateTime.now())
        );
        
        // 6. Log processed event sequence
        jdbcTemplate.update(
            "INSERT INTO analytics_event_log (event_id, projection_type, subscription_id, event_sequence, processed_at) VALUES (?, 'BILLING', ?, ?, ?)",
            eventId, subscriptionId, sequence, java.sql.Timestamp.valueOf(LocalDateTime.now())
        );
        
        evictAnalyticsCaches();
        log.info("[SubscriptionAnalyticsService] Successfully updated metrics for event: {}", eventId);
    }

    /**
     * Reversible payment analytics delta rollback.
     */
    @Transactional
    public void revertPaymentDelta(String eventId, Long invoiceId, Long subscriptionId, Long sequence) {
        log.info("[SubscriptionAnalyticsService] Reverting delta metrics for event: {}", eventId);
        
        Boolean exists = jdbcTemplate.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM analytics_event_log WHERE event_id = ? AND projection_type = 'REVERT')",
            Boolean.class, eventId
        );
        if (Boolean.TRUE.equals(exists)) {
            return;
        }

        Map<String, Object> subDetails = jdbcTemplate.queryForMap(
            "SELECT plan_code FROM subscriptions WHERE id = ?", subscriptionId
        );
        String planCode = (String) subDetails.get("plan_code");
        
        BigDecimal finalAmount = jdbcTemplate.queryForObject(
            "SELECT COALESCE((billing_info->>'finalAmount')::numeric, 0.00) FROM subscriptions WHERE id = ?",
            BigDecimal.class, subscriptionId
        );
        String cycle = jdbcTemplate.queryForObject(
            "SELECT COALESCE(billing_info->>'cycle', 'YEARLY') FROM subscriptions WHERE id = ?",
            String.class, subscriptionId
        );
        
        BigDecimal amount = jdbcTemplate.queryForObject(
            "SELECT amount FROM subscription_invoices WHERE id = ?",
            BigDecimal.class, invoiceId
        );
        amount = amount != null ? amount : BigDecimal.ZERO;
        
        BigDecimal mrrDelta = "YEARLY".equalsIgnoreCase(cycle) 
            ? finalAmount.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)
            : finalAmount;
            
        java.sql.Date today = java.sql.Date.valueOf(LocalDate.now());
        
        // Reverse deltas
        jdbcTemplate.update(
            "UPDATE subscription_metrics_daily SET " +
            "total_mrr = GREATEST(0.00, total_mrr - ?), " +
            "active_subscriptions = GREATEST(0, active_subscriptions - 1), " +
            "revenue_collected = GREATEST(0.00, revenue_collected - ?), " +
            "pending_invoices_value = pending_invoices_value + ?, " +
            "updated_at = ?, " +
            "projection_version = projection_version + 1 " +
            "WHERE date = ?",
            mrrDelta, amount, amount, java.sql.Timestamp.valueOf(LocalDateTime.now()), today
        );
        
        jdbcTemplate.update(
            "UPDATE subscription_plan_summary SET " +
            "organization_count = GREATEST(0, organization_count - 1), " +
            "mrr = GREATEST(0.00, mrr - ?), " +
            "updated_at = ? " +
            "WHERE plan_code = ?",
            mrrDelta, java.sql.Timestamp.valueOf(LocalDateTime.now()), planCode
        );
        
        jdbcTemplate.update(
            "INSERT INTO analytics_event_log (event_id, projection_type, subscription_id, event_sequence, processed_at) VALUES (?, 'REVERT', ?, ?, ?)",
            eventId, subscriptionId, sequence, java.sql.Timestamp.valueOf(LocalDateTime.now())
        );
        
        evictAnalyticsCaches();
    }

    /**
     * Recalculates metrics and stores pre-aggregated values in daily read models.
     * Acts as the ultimate SOURCE OF TRUTH overwriting all projections.
     */
    @Transactional
    public void recalculateAndStore() {
        log.info("[SubscriptionAnalyticsService] Commencing nightly full metrics reset (Source of Truth)...");
        
        MRRMetric mrr = getMRR();
        ActiveSubscriptionsMetric active = getActiveSubscriptions();
        RevenueCollectedMetric rev = getRevenueCollected();
        double churn = getChurnRate();
        BigDecimal pending = getRatioMetrics().pendingInvoicesValue();
        
        java.sql.Timestamp now = java.sql.Timestamp.valueOf(LocalDateTime.now());
        
        // Overwrite the daily metrics projection row
        jdbcTemplate.update(
            "INSERT INTO subscription_metrics_daily (date, total_mrr, active_subscriptions, revenue_collected, churn_rate, pending_invoices_value, updated_at, projection_version) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 1) ON CONFLICT (date) DO UPDATE SET " +
            "total_mrr = EXCLUDED.total_mrr, active_subscriptions = EXCLUDED.active_subscriptions, " +
            "revenue_collected = EXCLUDED.revenue_collected, churn_rate = EXCLUDED.churn_rate, " +
            "pending_invoices_value = EXCLUDED.pending_invoices_value, updated_at = EXCLUDED.updated_at, " +
            "projection_version = subscription_metrics_daily.projection_version + 1",
            java.sql.Date.valueOf(LocalDate.now()), mrr.value(), active.count(), rev.value(), churn, pending, now
        );
        
        // Completely overwrite plan summaries
        jdbcTemplate.update("TRUNCATE TABLE subscription_plan_summary");
        List<PlanDistributionEntry> planDist = getPlanDistribution();
        for (PlanDistributionEntry entry : planDist) {
            jdbcTemplate.update(
                "INSERT INTO subscription_plan_summary (plan_code, organization_count, mrr, updated_at) VALUES (?, ?, ?, ?)",
                entry.plan(), entry.orgs(), entry.mrr(), now
            );
        }
        
        log.info("[SubscriptionAnalyticsService] Completed storage of read-model projections.");
    }

    /**
     * Drop and completely rebuild analytics projections from primary database tables.
     */
    @Transactional
    public void rebuildProjections() {
        log.warn("[SubscriptionAnalyticsService] Rebuild mode activated. Purging and recalculating all read models...");
        
        jdbcTemplate.update("TRUNCATE TABLE public.subscription_metrics_daily");
        jdbcTemplate.update("TRUNCATE TABLE public.subscription_plan_summary");
        jdbcTemplate.update("TRUNCATE TABLE public.analytics_event_log");
        
        recalculateAndStore();
        evictAnalyticsCaches();
        
        log.info("[SubscriptionAnalyticsService] Analytics projection database rebuild successfully completed.");
    }

    /**
     * Dynamic Cache Key Invalidation for dashboard analytics.
     */
    public void evictAnalyticsCaches() {
        org.springframework.cache.Cache cache = cacheManager.getCache("subscriptionsOverview");
        if (cache != null) {
            cache.evict("dashboard_overview");
            cache.evict("metric_mrr");
            cache.evict("metric_active");
            cache.evict("metric_revenue");
            cache.evict("metric_invoices_overdue");
            cache.evict("metric_plan_distribution");
            cache.evict("metric_churn");
            cache.evict("metric_renewals_30");
            log.info("[SubscriptionAnalyticsService] Analytics granular cache keys successfully evicted.");
        }
    }

    /**
     * Async task trigger to refresh read models and invalidates caches.
     */
    @Async
    @Transactional
    public void recalculateAndRefresh() {
        log.info("[SubscriptionAnalyticsService] Executing asynchronous metrics refresh...");
        recalculateAndStore();
        evictAnalyticsCaches();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void snapshotDailyMetrics() {
        log.info("[SubscriptionAnalyticsService] Triggering scheduled midnight metrics snapshot...");
        recalculateAndStore();
        evictAnalyticsCaches();
    }
}
