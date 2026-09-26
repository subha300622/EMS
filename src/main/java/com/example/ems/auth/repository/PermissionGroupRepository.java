package com.example.ems.auth.repository;

import com.example.ems.auth.entity.PermissionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, Long> {
    Optional<PermissionGroup> findByCode(String code);
    boolean existsByCode(String code);
    List<PermissionGroup> findByIdIn(List<Long> ids);
}
