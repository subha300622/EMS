package com.example.ems.onboarding.repository;

import com.example.ems.onboarding.entity.OnboardingTemplateTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OnboardingTemplateTaskRepository extends JpaRepository<OnboardingTemplateTask, Long> {
    List<OnboardingTemplateTask> findByTemplateId(Long templateId);
}
