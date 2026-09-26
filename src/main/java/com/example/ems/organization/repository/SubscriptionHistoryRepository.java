package com.example.ems.organization.repository;

import com.example.ems.organization.entity.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long>, JpaSpecificationExecutor<SubscriptionHistory> {
    List<SubscriptionHistory> findBySubscriptionId(Long subscriptionId);
}
