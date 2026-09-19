package com.example.ems.reports.organization.projection;

import java.time.Instant;

public interface OrganizationSummaryProjection {
    Long getId();
    String getOrganizationCode();
    String getName();
    String getEmail();
    String getPhone();
    String getPlanCode();
    String getStatus();
    Instant getCreatedAt();
}
