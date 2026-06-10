package com.example.ems.repository;

import com.example.ems.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByStatus(String status);
    List<Offer> findByCandidateId(Long candidateId);
}
