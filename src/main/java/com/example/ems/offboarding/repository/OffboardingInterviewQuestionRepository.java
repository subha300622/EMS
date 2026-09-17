package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.OffboardingInterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OffboardingInterviewQuestionRepository extends JpaRepository<OffboardingInterviewQuestion, Long> {

    List<OffboardingInterviewQuestion> findByInterviewTemplateIdAndOrganizationIdOrderBySequenceAsc(Long interviewTemplateId, Long organizationId);

    List<OffboardingInterviewQuestion> findByInterviewTemplateIdAndOrganizationIdAndActiveTrueOrderBySequenceAsc(Long interviewTemplateId, Long organizationId);

    Optional<OffboardingInterviewQuestion> findByIdAndOrganizationId(Long id, Long organizationId);

    void deleteByInterviewTemplateIdAndOrganizationId(Long interviewTemplateId, Long organizationId);
}
