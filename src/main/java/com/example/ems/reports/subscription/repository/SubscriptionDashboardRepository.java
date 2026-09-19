package com.example.ems.reports.subscription.repository;

import com.example.ems.organization.entity.Subscription;
import com.example.ems.organization.entity.SubscriptionStatus;
import com.example.ems.reports.subscription.projection.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface SubscriptionDashboardRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT COUNT(o) FROM Organization o")
    Long countTotalOrganizations();

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = :status")
    Long countByStatus(@Param("status") SubscriptionStatus status);

    @Query(value = "SELECT COALESCE(SUM(CASE WHEN LOWER(CAST(jsonb_extract_path_text(s.billing_info, 'cycle') AS text)) = 'yearly' THEN CAST(jsonb_extract_path_text(s.billing_info, 'amount') AS numeric) / 12 ELSE CAST(jsonb_extract_path_text(s.billing_info, 'amount') AS numeric) END), 0) FROM subscriptions s WHERE s.status = 'ACTIVE'", nativeQuery = true)
    BigDecimal calculateMonthlyRevenue();

    @Query("SELECT s.status AS statusName, COUNT(s) AS count FROM Subscription s GROUP BY s.status")
    List<SubscriptionStatusProjection> getStatusDistribution();

    @Query(value = "SELECT s.plan_code AS planCode, COUNT(s) AS organizationCount, (COUNT(s) * 100.0 / COALESCE((SELECT COUNT(*) FROM subscriptions), 1)) AS percentage FROM subscriptions s GROUP BY s.plan_code", nativeQuery = true)
    List<PlanDistributionProjection> getPlanDistribution();

    @Query(value = "SELECT s.plan_code AS planCode, COALESCE(SUM(i.amount), 0) AS revenue FROM subscriptions s JOIN subscription_invoices i ON i.subscription_id = s.id WHERE i.status = 'PAID' GROUP BY s.plan_code", nativeQuery = true)
    List<PlanRevenueProjection> getPlanRevenue();

    @Query(value = "SELECT TO_CHAR(h.performed_at, :pattern) AS periodLabel, CAST(SUM(CASE WHEN h.action = 'CREATED' THEN 1 ELSE 0 END) AS integer) AS newSubscriptions, CAST(SUM(CASE WHEN h.action = 'RENEWED' THEN 1 ELSE 0 END) AS integer) AS renewals, CAST(SUM(CASE WHEN h.new_status = 'CANCELLED' THEN 1 ELSE 0 END) AS integer) AS cancellations FROM subscription_history h WHERE h.performed_at BETWEEN :start AND :end GROUP BY periodLabel ORDER BY periodLabel", nativeQuery = true)
    List<SubscriptionGrowthProjection> getGrowthTrend(@Param("start") Instant start, @Param("end") Instant end, @Param("pattern") String pattern);

    @Query(value = "SELECT TO_CHAR(i.paid_at, :pattern) AS period, COALESCE(SUM(CASE WHEN LOWER(CAST(jsonb_extract_path_text(s.billing_info, 'cycle') AS text)) = 'yearly' THEN i.amount / 12 ELSE i.amount END), 0) AS monthlyRevenue, COALESCE(SUM(CASE WHEN LOWER(CAST(jsonb_extract_path_text(s.billing_info, 'cycle') AS text)) = 'yearly' THEN i.amount ELSE i.amount * 12 END), 0) AS annualRevenue, COALESCE(SUM(i.amount), 0) AS totalRevenue FROM subscription_invoices i JOIN subscriptions s ON i.subscription_id = s.id WHERE i.status = 'PAID' AND i.paid_at BETWEEN :start AND :end GROUP BY period ORDER BY period", nativeQuery = true)
    List<RevenueTrendProjection> getRevenueTrend(@Param("start") Instant start, @Param("end") Instant end, @Param("pattern") String pattern);
}
