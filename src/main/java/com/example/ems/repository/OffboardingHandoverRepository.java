package com.example.ems.repository;

import com.example.ems.entity.OffboardingHandover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OffboardingHandoverRepository extends JpaRepository<OffboardingHandover, Long> {
    List<OffboardingHandover> findByOffboardingId(Long offboardingId);
}
