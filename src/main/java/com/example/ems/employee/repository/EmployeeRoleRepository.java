package com.example.ems.employee.repository;

import com.example.ems.employee.entity.EmployeeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRoleRepository extends JpaRepository<EmployeeRole, Long> {

    List<EmployeeRole> findByEmployeeIdAndStatus(Long employeeId, String status);

    Optional<EmployeeRole> findByEmployeeIdAndRoleIdAndStatus(Long employeeId, Long roleId, String status);

    boolean existsByEmployeeIdAndRoleIdAndStatus(Long employeeId, Long roleId, String status);

    @Query("SELECT COUNT(er) FROM EmployeeRole er WHERE er.role.name = :roleName AND er.employee.organization.id = :orgId AND er.status = 'ACTIVE'")
    long countActiveRoleInOrg(@Param("roleName") String roleName, @Param("orgId") Long orgId);

    List<EmployeeRole> findByEmployeeId(Long employeeId);
}
