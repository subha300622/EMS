package com.example.ems.reports.revenue.repository;

import com.example.ems.organization.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RevenuePaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    @Query("SELECT p FROM Payment p JOIN FETCH p.invoice i JOIN FETCH i.subscription s JOIN FETCH s.organization o WHERE p.id = :id")
    Optional<Payment> findByIdWithInvoiceAndSubscription(@Param("id") Long id);

    @Query("SELECT p FROM Payment p JOIN FETCH p.invoice i JOIN FETCH i.subscription s JOIN FETCH s.organization o WHERE p.status = 'REFUNDED'")
    List<Payment> findRefundedPayments();
}
