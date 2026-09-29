package com.example.ems.attendance.repository;

import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.entity.AttendancePolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendancePolicyRepository extends JpaRepository<AttendancePolicy, Long> {

    Optional<AttendancePolicy> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<AttendancePolicy> findFirstByOrganizationIdAndStatus(Long organizationId, AttendancePolicyStatus status);

    Page<AttendancePolicy> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<AttendancePolicy> findByOrganizationIdAndStatus(Long organizationId, AttendancePolicyStatus status, Pageable pageable);

    @Query("SELECT p FROM AttendancePolicy p WHERE p.organization.id = :organizationId AND p.status = 'ACTIVE' ORDER BY p.updatedAt DESC")
    List<AttendancePolicy> findActivePoliciesForOrganization(@Param("organizationId") Long organizationId);
}
