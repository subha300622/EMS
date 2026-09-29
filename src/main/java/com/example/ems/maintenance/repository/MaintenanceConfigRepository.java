package com.example.ems.maintenance.repository;

import com.example.ems.maintenance.entity.MaintenanceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaintenanceConfigRepository extends JpaRepository<MaintenanceConfig, Long> {

    default Optional<MaintenanceConfig> findDefaultConfig() {
        return findById(1L);
    }
}
