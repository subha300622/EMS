package com.example.ems.asset.service;

import com.example.ems.asset.dto.AssetDtos.CategoryResponse;
import com.example.ems.asset.dto.AssetDtos.CreateCategoryRequest;
import com.example.ems.asset.entity.AssetCategory;
import com.example.ems.asset.repository.AssetCategoryRepository;
import com.example.ems.asset.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetCategoryService {

    @Autowired
    private AssetCategoryRepository categoryRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Transactional
    public CategoryResponse createCategory(Long organizationId, CreateCategoryRequest request) {
        String code = request.getCategoryCode().trim();
        String name = request.getCategoryName().trim();

        if (categoryRepository.existsByOrganizationIdAndCategoryCodeIgnoreCase(organizationId, code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category code '" + code + "' already exists in this organization");
        }
        if (categoryRepository.existsByOrganizationIdAndCategoryNameIgnoreCase(organizationId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name '" + name + "' already exists in this organization");
        }

        AssetCategory category = new AssetCategory(organizationId, name, code, request.getDescription() != null ? request.getDescription().trim() : null);
        category = categoryRepository.save(category);

        return mapToResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(Long organizationId, boolean activeOnly) {
        List<AssetCategory> list = activeOnly ?
                categoryRepository.findByOrganizationIdAndActiveTrue(organizationId) :
                categoryRepository.findByOrganizationId(organizationId);

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long organizationId, Long categoryId) {
        AssetCategory category = categoryRepository.findByIdAndOrganizationId(categoryId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset Category not found with id " + categoryId));
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long organizationId, Long categoryId, CreateCategoryRequest request) {
        AssetCategory category = categoryRepository.findByIdAndOrganizationId(categoryId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset Category not found with id " + categoryId));

        String code = request.getCategoryCode().trim();
        String name = request.getCategoryName().trim();

        if (categoryRepository.existsByOrganizationIdAndCategoryCodeIgnoreCaseAndIdNot(organizationId, code, categoryId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category code '" + code + "' already exists in this organization");
        }
        if (categoryRepository.existsByOrganizationIdAndCategoryNameIgnoreCaseAndIdNot(organizationId, name, categoryId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name '" + name + "' already exists in this organization");
        }

        category.setCategoryCode(code);
        category.setCategoryName(name);
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long organizationId, Long categoryId) {
        AssetCategory category = categoryRepository.findByIdAndOrganizationId(categoryId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset Category not found with id " + categoryId));

        if (assetRepository.existsByCategoryIdAndDeletedFalse(categoryId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CATEGORY_IN_USE: Cannot delete category that has active assets assigned to it.");
        }

        category.setActive(false);
        categoryRepository.save(category);
    }

    private CategoryResponse mapToResponse(AssetCategory category) {
        return new CategoryResponse(
                category.getId(),
                category.getCategoryName(),
                category.getCategoryCode(),
                category.getDescription(),
                category.isActive()
        );
    }
}
