package com.example.ems.support.repository;

import com.example.ems.support.entity.SupportEscalationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportEscalationRuleRepository extends JpaRepository<SupportEscalationRule, Long> {
    List<SupportEscalationRule> findByOrganizationIdOrderByLevelAsc(Long organizationId);
    void deleteByOrganizationId(Long organizationId);
}
