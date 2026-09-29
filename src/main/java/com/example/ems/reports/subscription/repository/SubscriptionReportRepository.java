package com.example.ems.reports.subscription.repository;

import com.example.ems.organization.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SubscriptionReportRepository extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.expiryDate >= :now AND s.expiryDate <= :targetDate")
    Page<Subscription> findExpiringSubscriptions(
            @Param("now") LocalDate now, 
            @Param("targetDate") LocalDate targetDate, 
            Pageable pageable
    );

    @Query("SELECT s FROM Subscription s WHERE s.status = 'TRIAL'")
    Page<Subscription> findTrialSubscriptions(Pageable pageable);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.organization WHERE s.organization.id = :orgId")
    Optional<Subscription> findByOrganizationIdWithOrganization(@Param("orgId") Long orgId);
}
