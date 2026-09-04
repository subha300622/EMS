package com.example.ems.asset.service;

import com.example.ems.asset.dto.AssetDtos.*;
import com.example.ems.asset.entity.Asset;
import com.example.ems.asset.entity.AssetCategory;
import com.example.ems.asset.entity.AssetLocation;
import com.example.ems.asset.entity.AssetStatus;
import com.example.ems.asset.repository.AssetCategoryRepository;
import com.example.ems.asset.repository.AssetLocationRepository;
import com.example.ems.asset.repository.AssetRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetCategoryRepository categoryRepository;

    @Autowired
    private AssetLocationRepository locationRepository;

    @Autowired
    private AssetHistoryService historyService;

    @Transactional
    public AssetResponse createAsset(Long organizationId, CreateAssetRequest request, String performedBy) {
        String code = request.getAssetCode().trim();
        String name = request.getAssetName().trim();

        if (assetRepository.existsByOrganizationIdAndAssetCodeIgnoreCase(organizationId, code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset code '" + code + "' already exists in this organization");
        }
        if (request.getSerialNumber() != null && !request.getSerialNumber().trim().isEmpty()) {
            String serial = request.getSerialNumber().trim();
            if (assetRepository.existsByOrganizationIdAndSerialNumberIgnoreCaseAndDeletedFalse(organizationId, serial)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Serial number '" + serial + "' already exists in this organization");
            }
        }

        AssetCategory category = categoryRepository.findByIdAndOrganizationId(request.getCategoryId(), organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset Category not found or belongs to another organization"));

        if (!category.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected Asset Category is inactive");
        }

        AssetLocation location = locationRepository.findByIdAndOrganizationId(request.getLocationId(), organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset Location not found or belongs to another organization"));

        if (!location.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected Asset Location is inactive");
        }

        Asset asset = new Asset();
        asset.setOrganizationId(organizationId);
        asset.setAssetCode(code);
        asset.setAssetName(name);
        asset.setCategory(category);
        asset.setLocation(location);
        asset.setSerialNumber(request.getSerialNumber() != null ? request.getSerialNumber().trim() : null);
        asset.setBrand(request.getBrand() != null ? request.getBrand().trim() : null);
        asset.setModel(request.getModel() != null ? request.getModel().trim() : null);
        asset.setPurchaseDate(request.getPurchaseDate());
        asset.setPurchaseCost(request.getPurchaseCost());
        asset.setCurrentValue(request.getPurchaseCost());
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setWarrantyStatus(request.getWarrantyStatus() != null ? request.getWarrantyStatus() : "ACTIVE");
        asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        asset.setVendor(request.getVendor() != null ? request.getVendor().trim() : null);
        asset.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        asset = assetRepository.save(asset);

        historyService.recordHistory(
                organizationId, asset.getId(), com.example.ems.asset.entity.AssetEventType.ASSET_CREATED,
                null, AssetStatus.AVAILABLE.name(),
                null, null, null, location.getId(),
                performedBy, null, "Asset created with code: " + code
        );

        return mapToResponse(asset);
    }

    @Transactional(readOnly = true)
    public PaginatedAssetResponse getAssets(Long organizationId, AssetStatus status, Long categoryId, String search, String sortField, String sortDir, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir != null ? sortDir : "DESC"), sortField != null ? sortField : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Asset> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organizationId"), organizationId));
            predicates.add(cb.equal(root.get("deleted"), false));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("assetName")), pattern);
                Predicate codeLike = cb.like(cb.lower(root.get("assetCode")), pattern);
                Predicate serialLike = cb.like(cb.lower(root.get("serialNumber")), pattern);
                predicates.add(cb.or(nameLike, codeLike, serialLike));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Asset> pagedResult = assetRepository.findAll(spec, pageable);
        List<AssetResponse> content = pagedResult.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
        return new PaginatedAssetResponse(content, pagedResult.getNumber(), pagedResult.getSize(), pagedResult.getTotalElements(), pagedResult.getTotalPages());
    }

    @Transactional(readOnly = true)
    public AssetResponse getAssetById(Long organizationId, Long id) {
        Asset asset = assetRepository.findByIdAndOrganizationIdAndDeletedFalse(id, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with ID: " + id));
        return mapToResponse(asset);
    }

    @Transactional
    public AssetResponse updateAsset(Long organizationId, Long id, EditAssetRequest request, String performedBy) {
        Asset asset = assetRepository.findByIdAndOrganizationIdAndDeletedFalse(id, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with ID: " + id));

        AssetCategory category = categoryRepository.findByIdAndOrganizationIdAndActiveTrue(request.getCategoryId(), organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + request.getCategoryId()));

        asset.setAssetName(request.getAssetName());
        asset.setCategory(category);
        asset.setBrand(request.getBrand());
        asset.setModel(request.getModel());
        asset.setPurchaseCost(request.getPurchaseCost());
        if (request.getWarrantyStatus() != null) asset.setWarrantyStatus(request.getWarrantyStatus());
        asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        asset.setVendor(request.getVendor());
        asset.setDescription(request.getDescription());

        // Note: status is strictly untouched here per architectural invariants
        asset = assetRepository.save(asset);
        return mapToResponse(asset);
    }

    @Transactional
    public void deleteAsset(Long organizationId, Long id, String performedBy) {
        Asset asset = assetRepository.findByIdAndOrganizationIdAndDeletedFalse(id, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with ID: " + id));

        asset.setDeleted(true);
        asset.setDeletedAt(LocalDateTime.now());
        asset.setDeletedBy(performedBy);
        assetRepository.save(asset);
    }

    @Transactional(readOnly = true)
    public AssetVerificationResponse verifyAsset(String assetCode) {
        Asset asset = assetRepository.findByAssetCodeIgnoreCaseAndDeletedFalse(assetCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found for verification code: " + assetCode));
        String categoryName = asset.getCategory() != null ? asset.getCategory().getCategoryName() : "General";
        return new AssetVerificationResponse(asset.getAssetCode(), asset.getAssetName(), categoryName, asset.getStatus(), true);
    }

    public AssetResponse mapToResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getAssetCode(),
                asset.getAssetName(),
                asset.getCategory() != null ? asset.getCategory().getId() : null,
                asset.getCategory() != null ? asset.getCategory().getCategoryName() : null,
                asset.getLocation() != null ? asset.getLocation().getId() : null,
                asset.getLocation() != null ? asset.getLocation().getLocationName() : null,
                asset.getSerialNumber(),
                asset.getBrand(),
                asset.getModel(),
                asset.getPurchaseDate(),
                asset.getPurchaseCost(),
                asset.getCurrentValue(),
                asset.getStatus(),
                asset.getCondition(),
                asset.getWarrantyStatus(),
                asset.getWarrantyExpiryDate(),
                asset.getVendor(),
                asset.getDescription(),
                asset.isDeleted(),
                asset.getVersion(),
                asset.getCreatedAt()
        );
    }
}
