package com.example.ems.performance.manager.repository;

import com.example.ems.performance.manager.entity.CompetencyRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("managerCompetencyRatingRepository")
public interface CompetencyRatingRepository extends JpaRepository<CompetencyRating, Long> {
    List<CompetencyRating> findByReviewId(Long reviewId);
}
