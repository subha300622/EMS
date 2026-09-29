package com.example.ems.organization.repository;

import com.example.ems.organization.entity.OrganizationAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationAddressRepository extends JpaRepository<OrganizationAddress, Long> {
}
