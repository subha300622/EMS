package com.example.ems.organization.repository;

import com.example.ems.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>, JpaSpecificationExecutor<Organization> {
    Optional<Organization> findByOrganizationCode(String organizationCode);
    boolean existsByOrganizationCode(String organizationCode);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.organization.id = :orgId AND e.status = 'ACTIVE'")
    long countActiveEmployees(@Param("orgId") Long orgId);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.organization.id = :orgId")
    long countEmployees(@Param("orgId") Long orgId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.organization.id = :orgId AND (u.role.name = 'ADMIN' OR u.role.name = 'SUPER_ADMIN')")
    long countAdmins(@Param("orgId") Long orgId);

    @Query("SELECT COUNT(d) FROM Department d WHERE d.organization.id = :orgId")
    long countDepartments(@Param("orgId") Long orgId);
}
