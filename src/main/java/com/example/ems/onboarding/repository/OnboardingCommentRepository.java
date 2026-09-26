package com.example.ems.onboarding.repository;

import com.example.ems.onboarding.entity.OnboardingComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OnboardingCommentRepository extends JpaRepository<OnboardingComment, Long> {
    List<OnboardingComment> findByOnboardingIdOrderByCreatedAtDesc(Long onboardingId);
}
