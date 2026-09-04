package com.example.ems.employee.repository;

import com.example.ems.employee.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {
    Optional<Designation> findByDesignationIgnoreCase(String designation);
    boolean existsByDesignationIgnoreCase(String designation);

    java.util.List<Designation> findByOrganizationId(Long organizationId);
    Optional<Designation> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<Designation> findByDesignationIgnoreCaseAndOrganizationId(String designation, Long organizationId);
    boolean existsByDesignationIgnoreCaseAndOrganizationId(String designation, Long organizationId);
}
