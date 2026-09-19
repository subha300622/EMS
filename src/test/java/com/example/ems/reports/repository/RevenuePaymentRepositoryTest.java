package com.example.ems.reports.repository;

import com.example.ems.reports.revenue.dto.RevenueFilterRequest;
import com.example.ems.reports.revenue.repository.RevenuePaymentRepository;
import com.example.ems.reports.revenue.specification.PaymentSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class RevenuePaymentRepositoryTest {

    @Autowired
    private RevenuePaymentRepository paymentRepository;

    @Autowired
    private PaymentSpecification paymentSpecification;

    @Test
    public void testQueryPaymentsWithSpecification() {
        RevenueFilterRequest filters = new RevenueFilterRequest();
        filters.setCurrency("INR");
        filters.setPaymentStatus("SUCCESS");

        var spec = paymentSpecification.withFilters(filters);
        var page = paymentRepository.findAll(spec, PageRequest.of(0, 10));
        assertThat(page).isNotNull();
    }
}
