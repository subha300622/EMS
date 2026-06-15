package com.example.ems.security.logging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostApiPayloadLogRepository extends JpaRepository<PostApiPayloadLog, Long> {
}
