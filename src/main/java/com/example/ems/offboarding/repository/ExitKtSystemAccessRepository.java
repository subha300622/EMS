package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.ExitKtSystemAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExitKtSystemAccessRepository extends JpaRepository<ExitKtSystemAccess, Long> {
}
