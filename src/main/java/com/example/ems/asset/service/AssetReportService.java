package com.example.ems.asset.service;

import com.example.ems.asset.dto.AssetDtos.AssetDashboardResponse;
import com.example.ems.asset.dto.AssetDtos.AssetResponse;
import com.example.ems.asset.entity.Asset;
import com.example.ems.asset.entity.AssetStatus;
import com.example.ems.asset.repository.AssetRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetReportService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetService assetService;

    @Transactional(readOnly = true)
    public AssetDashboardResponse getDashboard(Long organizationId) {
        Long total = getCountByStatus(organizationId, null);
        Long available = getCountByStatus(organizationId, AssetStatus.AVAILABLE);
        Long assigned = getCountByStatus(organizationId, AssetStatus.ASSIGNED);
        Long returned = getCountByStatus(organizationId, AssetStatus.RETURNED);
        Long maintenance = getCountByStatus(organizationId, AssetStatus.IN_MAINTENANCE) + getCountByStatus(organizationId, AssetStatus.REPAIR);
        Long lost = getCountByStatus(organizationId, AssetStatus.LOST);
        Long damaged = getCountByStatus(organizationId, AssetStatus.DAMAGED);
        Long retired = getCountByStatus(organizationId, AssetStatus.RETIRED);
        Long disposed = getCountByStatus(organizationId, AssetStatus.DISPOSED);

        BigDecimal totalCost = (BigDecimal) entityManager.createQuery(
                "SELECT SUM(a.purchaseCost) FROM Asset a WHERE a.organizationId = :orgId AND a.deleted = false")
                .setParameter("orgId", organizationId)
                .getSingleResult();

        return new AssetDashboardResponse(
                total != null ? total : 0,
                available != null ? available : 0,
                assigned != null ? assigned : 0,
                returned != null ? returned : 0,
                maintenance != null ? maintenance : 0,
                lost != null ? lost : 0,
                damaged != null ? damaged : 0,
                retired != null ? retired : 0,
                disposed != null ? disposed : 0,
                totalCost != null ? totalCost : BigDecimal.ZERO
        );
    }

    private Long getCountByStatus(Long organizationId, AssetStatus status) {
        if (status == null) {
            return (Long) entityManager.createQuery(
                    "SELECT COUNT(a) FROM Asset a WHERE a.organizationId = :orgId AND a.deleted = false")
                    .setParameter("orgId", organizationId)
                    .getSingleResult();
        }
        return (Long) entityManager.createQuery(
                "SELECT COUNT(a) FROM Asset a WHERE a.organizationId = :orgId AND a.status = :status AND a.deleted = false")
                .setParameter("orgId", organizationId)
                .setParameter("status", status)
                .getSingleResult();
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getExpiringWarranties(Long organizationId, int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        List<Asset> list = entityManager.createQuery(
                "SELECT a FROM Asset a WHERE a.organizationId = :orgId AND a.deleted = false AND a.warrantyExpiryDate <= :cutoff ORDER BY a.warrantyExpiryDate ASC", Asset.class)
                .setParameter("orgId", organizationId)
                .setParameter("cutoff", cutoff)
                .getResultList();
        return list.stream().map(assetService::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getMaintenanceDue(Long organizationId) {
        List<Asset> list = entityManager.createQuery(
                "SELECT a FROM Asset a WHERE a.organizationId = :orgId AND a.deleted = false AND a.status IN (:statuses)", Asset.class)
                .setParameter("orgId", organizationId)
                .setParameter("statuses", List.of(AssetStatus.IN_MAINTENANCE, AssetStatus.REPAIR, AssetStatus.DAMAGED))
                .getResultList();
        return list.stream().map(assetService::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getLostAssets(Long organizationId) {
        List<Asset> list = assetRepository.findByOrganizationIdAndStatusAndDeletedFalse(organizationId, AssetStatus.LOST);
        return list.stream().map(assetService::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getDamagedAssets(Long organizationId) {
        List<Asset> list = assetRepository.findByOrganizationIdAndStatusAndDeletedFalse(organizationId, AssetStatus.DAMAGED);
        return list.stream().map(assetService::mapToResponse).collect(Collectors.toList());
    }
}
