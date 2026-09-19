package com.example.ems.increment.repository;

import com.example.ems.increment.entity.IncrementLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncrementLetterRepository extends JpaRepository<IncrementLetter, Long> {

    @Query("SELECT l FROM IncrementLetter l WHERE l.recommendation.id = :recId AND l.organization.id = :orgId")
    Optional<IncrementLetter> findByRecommendationIdAndOrgId(@Param("recId") Long recId, @Param("orgId") Long orgId);

    @Query("SELECT l FROM IncrementLetter l WHERE l.employee.id = :empId AND l.organization.id = :orgId ORDER BY l.generatedAt DESC")
    List<IncrementLetter> findAllByEmployeeIdAndOrgId(@Param("empId") Long empId, @Param("orgId") Long orgId);
}
