package com.example.ems.organization.service;

import com.example.ems.config.BaseCacheService;
import com.example.ems.organization.dto.OrganizationDetailResponse;
import com.example.ems.organization.dto.OrganizationListItemResponse;
import com.example.ems.organization.dto.OrganizationStatisticsResponse;
import com.example.ems.organization.dto.OrganizationSummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrganizationCacheService extends BaseCacheService {

    private static final String PREFIX = "ems:%s:organization:v1:";

    @Autowired
    private OrganizationService organizationService;

    // ── Key Builders ─────────────────────────────────────────────────────────

    private String keyDetails(Long id) {
        return String.format(PREFIX + "details:%d", env, id);
    }

    private String keySearch(String search, String status, String plan, int page, int size) {
        return String.format(PREFIX + "search:%s:%s:%s:%d:%d", env, 
                search != null ? search : "none", 
                status != null ? status : "none", 
                plan != null ? plan : "none", 
                page, size);
    }

    private String keyStatistics(Long id) {
        return String.format(PREFIX + "statistics:%d", env, id);
    }

    private String keySummary() {
        return String.format(PREFIX + "summary", env);
    }

    // ── Cached GETs ──────────────────────────────────────────────────────────

    public OrganizationDetailResponse getOrganizationDetails(Long id) {
        return get(keyDetails(id), CacheCategory.PROFILE, OrganizationDetailResponse.class,
                () -> organizationService.getOrganizationDetails(id));
    }

    @SuppressWarnings("unchecked")
    public Page<OrganizationListItemResponse> searchOrganizations(String search, String status, String plan, Pageable pageable) {
        return (Page<OrganizationListItemResponse>) get(
                keySearch(search, status, plan, pageable.getPageNumber(), pageable.getPageSize()),
                CacheCategory.LIST,
                Page.class,
                () -> organizationService.searchOrganizations(search, status, plan, pageable)
        );
    }

    public OrganizationStatisticsResponse getStatistics(Long id) {
        return get(keyStatistics(id), CacheCategory.DASHBOARD, OrganizationStatisticsResponse.class,
                () -> organizationService.getStatistics(id));
    }

    public OrganizationSummaryResponse getSummary() {
        return get(keySummary(), CacheCategory.DASHBOARD, OrganizationSummaryResponse.class,
                () -> organizationService.getSummary());
    }

    // ── Eviction ─────────────────────────────────────────────────────────────

    public void evictCache(Long id) {
        if (id != null) {
            evict(keyDetails(id), CacheCategory.PROFILE);
            evict(keyStatistics(id), CacheCategory.DASHBOARD);
        }
        evict(keySummary(), CacheCategory.DASHBOARD);
        clearL1(CacheCategory.LIST);
    }
}
