package com.example.ems.reports.repository;

import com.example.ems.reports.revenue.dto.RevenueFilterRequest;
import com.example.ems.reports.revenue.repository.RevenueInvoiceRepository;
import com.example.ems.reports.revenue.specification.InvoiceSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class RevenueInvoiceRepositoryTest {

    @Autowired
    private RevenueInvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceSpecification invoiceSpecification;

    @Test
    public void testQueryInvoicesWithSpecification() {
        RevenueFilterRequest filters = new RevenueFilterRequest();
        filters.setInvoiceStatus("PAID");

        var spec = invoiceSpecification.withFilters(filters);
        var page = invoiceRepository.findAll(spec, PageRequest.of(0, 10));
        assertThat(page).isNotNull();
    }
}
