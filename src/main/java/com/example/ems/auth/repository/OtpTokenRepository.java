package com.example.ems.auth.repository;

import com.example.ems.auth.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByEmail(String email);
    Optional<OtpToken> findByResetToken(String resetToken);
    void deleteByEmail(String email);
}
