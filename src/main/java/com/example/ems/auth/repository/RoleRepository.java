package com.example.ems.auth.repository;

import com.example.ems.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Role r WHERE r.name = :name AND (r.isPlatformTemplate = true OR r.organization IS NULL)")
    Optional<Role> findByName(@org.springframework.data.repository.query.Param("name") String name);
    boolean existsByName(String name);

    // Multi-tenant additions
    List<Role> findByIsPlatformTemplateTrue();
    List<Role> findByOrganizationId(Long organizationId);
    Optional<Role> findByOrganizationIdAndName(Long organizationId, String name);
    boolean existsByOrganizationIdAndName(Long organizationId, String name);
    
    // Helper to find a template by name (since templates have organizationId IS NULL)
    Optional<Role> findByNameAndIsPlatformTemplateTrue(String name);
}
