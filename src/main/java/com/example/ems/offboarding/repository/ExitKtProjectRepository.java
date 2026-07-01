package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.ExitKtProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExitKtProjectRepository extends JpaRepository<ExitKtProject, Long> {
}
