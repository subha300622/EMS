package com.example.ems.reports.revenue.specification;

import com.example.ems.reports.revenue.dto.RevenueFilterRequest;
import org.springframework.data.jpa.domain.Specification;

public interface BaseRevenueSpecification<T> {
    Specification<T> withFilters(RevenueFilterRequest filters);
}
