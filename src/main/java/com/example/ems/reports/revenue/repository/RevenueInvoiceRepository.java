package com.example.ems.reports.revenue.repository;

import com.example.ems.organization.entity.SubscriptionInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RevenueInvoiceRepository extends JpaRepository<SubscriptionInvoice, Long>, JpaSpecificationExecutor<SubscriptionInvoice> {

    @Query("SELECT i FROM SubscriptionInvoice i JOIN FETCH i.subscription s JOIN FETCH s.organization o WHERE i.id = :id")
    Optional<SubscriptionInvoice> findByIdWithSubscription(@Param("id") Long id);
}
