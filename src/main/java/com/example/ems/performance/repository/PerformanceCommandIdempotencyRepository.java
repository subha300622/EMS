package com.example.ems.performance.repository;

import com.example.ems.performance.entity.PerformanceCommandIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerformanceCommandIdempotencyRepository extends JpaRepository<PerformanceCommandIdempotency, Long> {

    Optional<PerformanceCommandIdempotency> findByOrganizationIdAndIdempotencyKey(Long organizationId, String idempotencyKey);
}
