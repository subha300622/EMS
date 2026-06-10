package com.example.ems.repository;

import com.example.ems.entity.OffboardingAssetReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OffboardingAssetReturnRepository extends JpaRepository<OffboardingAssetReturn, Long> {
    List<OffboardingAssetReturn> findByOffboardingId(Long offboardingId);
}
