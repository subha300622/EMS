package com.example.ems.reports.revenue.repository;

import com.example.ems.organization.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RevenueExportRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p JOIN FETCH p.invoice i JOIN FETCH i.subscription s JOIN FETCH s.organization o ORDER BY p.id DESC")
    List<Payment> findAllPaymentsForExport();
}
