package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.OffboardingEmployeeTemplateAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OffboardingEmployeeTemplateAssignmentRepository extends JpaRepository<OffboardingEmployeeTemplateAssignment, Long> {

    Optional<OffboardingEmployeeTemplateAssignment> findByOrganizationIdAndEmployeeIdAndExitType(
            Long organizationId, Long employeeId, String exitType);

    List<OffboardingEmployeeTemplateAssignment> findByOrganizationIdAndEmployeeId(
            Long organizationId, Long employeeId);

    void deleteByOrganizationIdAndEmployeeIdAndExitType(
            Long organizationId, Long employeeId, String exitType);

    void deleteByOrganizationIdAndEmployeeId(
            Long organizationId, Long employeeId);

    boolean existsByOrganizationIdAndTemplateId(
            Long organizationId, Long templateId);
}
