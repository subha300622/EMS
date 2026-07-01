package com.example.ems.organization.repository;

import com.example.ems.organization.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);
    boolean existsByGatewayPaymentId(String gatewayPaymentId);
}
