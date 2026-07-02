package com.example.ems.support.repository;

import com.example.ems.support.entity.SupportSla;
import com.example.ems.support.entity.SupportTicketPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface SupportSlaRepository extends JpaRepository<SupportSla, Long> {
    Optional<SupportSla> findByPriority(SupportTicketPriority priority);
    Optional<SupportSla> findByNameIgnoreCase(String name);
    Optional<SupportSla> findByIsDefaultTrue();
    List<SupportSla> findAllByIsDefaultTrue();

    @Query("SELECT s FROM SupportSla s WHERE s.deleted = false " +
           "AND (:search IS NULL OR LOWER(s.name) LIKE :search OR LOWER(s.description) LIKE :search)" +
           "AND (:priority IS NULL OR s.priority = :priority) " +
           "AND (:enabled IS NULL OR s.enabled = :enabled) " +
           "AND (:isDefault IS NULL OR s.isDefault = :isDefault)")
    Page<SupportSla> findByFilters(@Param("search") String search,
                                   @Param("priority") SupportTicketPriority priority,
                                   @Param("enabled") Boolean enabled,
                                   @Param("isDefault") Boolean isDefault,
                                   Pageable pageable);
}
