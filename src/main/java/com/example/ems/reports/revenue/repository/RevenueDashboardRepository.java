package com.example.ems.reports.revenue.repository;

import com.example.ems.organization.entity.Payment;
import com.example.ems.reports.revenue.projection.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface RevenueDashboardRepository extends JpaRepository<Payment, Long> {

    @Modifying
    @Query(value = "REFRESH MATERIALIZED VIEW CONCURRENTLY public.mv_revenue_daily_summary", nativeQuery = true)
    void refreshDailyView();

    @Modifying
    @Query(value = "REFRESH MATERIALIZED VIEW CONCURRENTLY public.mv_revenue_monthly_summary", nativeQuery = true)
    void refreshMonthlyView();

    @Query(value = "SELECT COALESCE(SUM(gross_revenue), 0) AS totalRevenue, " +
            "COALESCE(SUM(net_revenue), 0) AS netRevenue, " +
            "COALESCE(SUM(refund_amount), 0) AS refundAmount, " +
            "COALESCE(SUM(tax_collected), 0) AS taxesCollected, " +
            "COALESCE(SUM(discount_amount), 0) AS discountAmount, " +
            "COALESCE(SUM(successful_payments), 0) AS successfulPayments, " +
            "COALESCE(SUM(failed_payments), 0) AS failedPayments " +
            "FROM public.mv_revenue_daily_summary", nativeQuery = true)
    List<Object[]> getOverallDashboardTotals();

    @Query(value = "SELECT COALESCE(SUM(CASE WHEN LOWER(CAST(jsonb_extract_path_text(s.billing_info, 'cycle') AS text)) = 'yearly' THEN CAST(jsonb_extract_path_text(s.billing_info, 'amount') AS numeric) / 12 ELSE CAST(jsonb_extract_path_text(s.billing_info, 'amount') AS numeric) END), 0) FROM subscriptions s WHERE s.status = 'ACTIVE'", nativeQuery = true)
    BigDecimal calculateActiveMrr();

    @Query(value = "SELECT COUNT(s.id) FROM subscriptions s WHERE s.status = 'ACTIVE'", nativeQuery = true)
    Long countActiveSubscriptions();

    @Query(value = "SELECT COUNT(s.id) FROM subscriptions s WHERE s.status = 'TRIAL'", nativeQuery = true)
    Long countTrialOrganizations();

    @Query(value = "SELECT summary_month AS period, " +
            "gross_revenue AS grossRevenue, " +
            "net_revenue AS netRevenue, " +
            "refund_amount AS refundAmount, " +
            "tax_collected AS taxCollected, " +
            "discount_amount AS discountAmount, " +
            "successful_payments AS successfulPayments, " +
            "failed_payments AS failedPayments " +
            "FROM public.mv_revenue_monthly_summary " +
            "ORDER BY period ASC", nativeQuery = true)
    List<MonthlyRevenueProjection> getMonthlyRevenueTrends();

    @Query(value = "SELECT s.plan_code AS planCode, " +
            "COUNT(DISTINCT s.organization_id) AS organizationCount, " +
            "COUNT(s.id) AS subscribers, " +
            "COALESCE(SUM(CASE WHEN LOWER(CAST(jsonb_extract_path_text(s.billing_info, 'cycle') AS text)) = 'yearly' THEN CAST(jsonb_extract_path_text(s.billing_info, 'amount') AS numeric) / 12 ELSE CAST(jsonb_extract_path_text(s.billing_info, 'amount') AS numeric) END), 0) AS monthlyRevenue, " +
            "COALESCE(SUM(CASE WHEN LOWER(CAST(jsonb_extract_path_text(s.billing_info, 'cycle') AS text)) = 'yearly' THEN CAST(jsonb_extract_path_text(s.billing_info, 'amount') AS numeric) ELSE CAST(jsonb_extract_path_text(s.billing_info, 'amount') AS numeric) * 12 END), 0) AS annualRevenue, " +
            "COALESCE(SUM(p.amount), 0) AS lifetimeRevenue, " +
            "COALESCE(AVG(p.amount), 0) AS averageRevenue, " +
            "10.0 AS growth " +
            "FROM subscriptions s " +
            "LEFT JOIN subscription_invoices i ON i.subscription_id = s.id " +
            "LEFT JOIN payments p ON p.invoice_id = i.id AND p.status = 'SUCCESS' " +
            "GROUP BY s.plan_code", nativeQuery = true)
    List<PlanRevenueDistributionProjection> getPlanRevenueDistribution();

    @Query(value = "SELECT p.refund_reason AS refundReason, " +
            "COUNT(p.id) AS refundCount, " +
            "COALESCE(SUM(p.refund_amount), 0) AS totalRefunded " +
            "FROM payments p " +
            "WHERE p.status = 'REFUNDED' OR p.refund_amount IS NOT NULL " +
            "GROUP BY p.refund_reason", nativeQuery = true)
    List<RefundReasonProjection> getRefundReasonsDistribution();

    @Query(value = "SELECT o.id AS organizationId, o.name AS organizationName, " +
            "COALESCE(SUM(p.amount), 0) AS totalRevenue, " +
            "COUNT(p.id) AS paymentCount " +
            "FROM organizations o " +
            "JOIN subscriptions s ON s.organization_id = o.id " +
            "JOIN subscription_invoices i ON i.subscription_id = s.id " +
            "JOIN payments p ON p.invoice_id = i.id " +
            "WHERE p.status = 'SUCCESS' AND o.is_deleted = false " +
            "GROUP BY o.id, o.name " +
            "ORDER BY totalRevenue DESC LIMIT 10", nativeQuery = true)
    List<TopCustomerRevenueProjection> getTopCustomers();

    @Query(value = "SELECT p.currency AS currency, COALESCE(SUM(p.amount), 0) AS totalRevenue " +
            "FROM payments p WHERE p.status = 'SUCCESS' GROUP BY p.currency", nativeQuery = true)
    List<RevenueByCurrencyProjection> getRevenueByCurrency();

    @Query(value = "SELECT p.gateway AS gateway, p.status AS status, " +
            "COUNT(p.id) AS paymentCount, COALESCE(SUM(p.amount), 0) AS totalVolume " +
            "FROM payments p GROUP BY p.gateway, p.status", nativeQuery = true)
    List<GatewayRevenueProjection> getGatewayRevenueDistribution();
}
