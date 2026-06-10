package com.example.ems.repository;

import com.example.ems.entity.IncrementPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IncrementPolicyRepository extends JpaRepository<IncrementPolicy, Long> {
    Optional<IncrementPolicy> findByRating(Integer rating);
}
