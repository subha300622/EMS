package com.example.ems.organization.repository;

import com.example.ems.organization.entity.OrganizationCompensationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationCompensationConfigRepository extends JpaRepository<OrganizationCompensationConfig, Long> {

    Optional<OrganizationCompensationConfig> findByOrganizationId(Long organizationId);

    boolean existsByOrganizationId(Long organizationId);
}
