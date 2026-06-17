package com.example.ems.settings.repository;

import com.example.ems.settings.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    List<NotificationPreference> findByUserEmail(String userEmail);
    Optional<NotificationPreference> findByUserEmailAndCategory(String userEmail, String category);
}
