package com.example.ems.organization.repository;

import com.example.ems.organization.entity.OrganizationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationSettingsRepository extends JpaRepository<OrganizationSettings, Long> {
}
