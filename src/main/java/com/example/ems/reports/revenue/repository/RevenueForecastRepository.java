package com.example.ems.reports.revenue.repository;

import com.example.ems.organization.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RevenueForecastRepository extends JpaRepository<Payment, Long> {

    @Query(value = "SELECT TO_CHAR(COALESCE(p.paid_at, p.created_at), 'YYYY-MM') AS monthLabel, " +
            "SUM(p.amount) AS totalAmount " +
            "FROM payments p " +
            "WHERE p.status = 'SUCCESS' AND COALESCE(p.paid_at, p.created_at) >= :sinceDate " +
            "GROUP BY monthLabel " +
            "ORDER BY monthLabel ASC", nativeQuery = true)
    List<Object[]> getHistoricalMonthlyRevenue(@Param("sinceDate") Instant sinceDate);
}
