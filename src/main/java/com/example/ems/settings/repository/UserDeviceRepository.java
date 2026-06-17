package com.example.ems.settings.repository;

import com.example.ems.settings.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    List<UserDevice> findByUserEmail(String userEmail);
    Optional<UserDevice> findByUserEmailAndId(String userEmail, Long id);
}
