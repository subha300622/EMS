package com.example.ems.recruitment.repository;

import com.example.ems.recruitment.entity.Offer;
import com.example.ems.recruitment.entity.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    Optional<Offer> findByOrganizationIdAndId(Long organizationId, Long id);

    Optional<Offer> findByOrganizationIdAndApplicationId(Long organizationId, Long applicationId);

    Optional<Offer> findByAcceptanceToken(String acceptanceToken);

    List<Offer> findByOrganizationIdAndApplicationIdAndStatusIn(Long organizationId, Long applicationId, List<OfferStatus> statuses);

    boolean existsByOrganizationIdAndApplicationIdAndStatusIn(Long organizationId, Long applicationId, List<OfferStatus> statuses);
}
