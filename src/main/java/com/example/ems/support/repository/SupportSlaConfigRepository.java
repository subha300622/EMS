package com.example.ems.support.repository;

import com.example.ems.support.entity.SupportSlaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupportSlaConfigRepository extends JpaRepository<SupportSlaConfig, Long> {
    Optional<SupportSlaConfig> findByOrganizationId(Long organizationId);
}
