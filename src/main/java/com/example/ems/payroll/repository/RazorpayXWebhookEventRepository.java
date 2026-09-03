package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.RazorpayXWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RazorpayXWebhookEventRepository extends JpaRepository<RazorpayXWebhookEvent, Long> {

    Optional<RazorpayXWebhookEvent> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}

