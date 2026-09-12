package com.example.ems.organization.repository;

import com.example.ems.organization.entity.SubscriptionInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionInvoiceRepository extends JpaRepository<SubscriptionInvoice, Long>, JpaSpecificationExecutor<SubscriptionInvoice> {
    List<SubscriptionInvoice> findBySubscriptionId(Long subscriptionId);
}
