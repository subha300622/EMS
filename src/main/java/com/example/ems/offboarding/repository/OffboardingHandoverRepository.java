package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.OffboardingHandover;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OffboardingHandoverRepository extends JpaRepository<OffboardingHandover, Long> {
    List<OffboardingHandover> findByOffboardingId(Long offboardingId);
}
