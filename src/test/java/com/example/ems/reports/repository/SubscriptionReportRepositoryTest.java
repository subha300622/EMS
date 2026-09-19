package com.example.ems.reports.repository;

import com.example.ems.organization.entity.Subscription;
import com.example.ems.reports.subscription.repository.SubscriptionReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class SubscriptionReportRepositoryTest {

    @Autowired
    private SubscriptionReportRepository reportRepository;

    @Test
    public void testFindExpiringSubscriptions() {
        LocalDate now = LocalDate.now();
        LocalDate target = now.plusDays(30);
        Page<Subscription> page = reportRepository.findExpiringSubscriptions(now, target, PageRequest.of(0, 10));
        assertThat(page).isNotNull();
    }

    @Test
    public void testFindTrialSubscriptions() {
        Page<Subscription> page = reportRepository.findTrialSubscriptions(PageRequest.of(0, 10));
        assertThat(page).isNotNull();
    }

    @Test
    public void testFindByOrganizationId() {
        // Querying an ID that might or might not exist, verifying it runs cleanly
        var opt = reportRepository.findByOrganizationIdWithOrganization(16L);
        assertThat(opt).isNotNull();
    }
}
