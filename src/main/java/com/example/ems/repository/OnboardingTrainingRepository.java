package com.example.ems.repository;

import com.example.ems.entity.OnboardingTraining;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OnboardingTrainingRepository extends JpaRepository<OnboardingTraining, Long> {
    List<OnboardingTraining> findByOnboardingId(Long onboardingId);
}
