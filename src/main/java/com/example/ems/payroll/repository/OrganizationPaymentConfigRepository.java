package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.OrganizationPaymentConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationPaymentConfigRepository extends JpaRepository<OrganizationPaymentConfig, Long> {

    Optional<OrganizationPaymentConfig> findByOrganizationId(Long organizationId);

    boolean existsByOrganizationId(Long organizationId);
}
