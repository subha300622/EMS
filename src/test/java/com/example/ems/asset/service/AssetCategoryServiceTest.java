package com.example.ems.asset.service;

import com.example.ems.asset.dto.AssetDtos.CategoryResponse;
import com.example.ems.asset.dto.AssetDtos.CreateCategoryRequest;
import com.example.ems.asset.entity.AssetCategory;
import com.example.ems.asset.repository.AssetCategoryRepository;
import com.example.ems.asset.repository.AssetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetCategoryServiceTest {

    @Mock
    private AssetCategoryRepository categoryRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetCategoryService categoryService;

    private Long orgId = 1L;

    @Test
    @DisplayName("Should successfully create a category when code and name are unique")
    void createCategory_Success() {
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setCategoryName("IT Equipment");
        req.setCategoryCode("IT-EQ");
        req.setDescription("Laptops and accessories");

        when(categoryRepository.existsByOrganizationIdAndCategoryCodeIgnoreCase(orgId, "IT-EQ")).thenReturn(false);
        when(categoryRepository.existsByOrganizationIdAndCategoryNameIgnoreCase(orgId, "IT Equipment"))
                .thenReturn(false);

        AssetCategory saved = new AssetCategory(orgId, "IT Equipment", "IT-EQ", "Laptops and accessories");
        saved.setId(10L);
        when(categoryRepository.save(any(AssetCategory.class))).thenReturn(saved);

        CategoryResponse resp = categoryService.createCategory(orgId, req);

        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        assertEquals("IT Equipment", resp.getCategoryName());
        assertEquals("IT-EQ", resp.getCategoryCode());
    }

    @Test
    @DisplayName("Should throw CONFLICT exception when duplicate category code is supplied")
    void createCategory_DuplicateCode_ThrowsConflict() {
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setCategoryName("IT Equipment");
        req.setCategoryCode("IT-EQ");

        when(categoryRepository.existsByOrganizationIdAndCategoryCodeIgnoreCase(orgId, "IT-EQ")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> categoryService.createCategory(orgId, req));
    }

    @Test
    @DisplayName("Should prevent deleting category when active assets exist")
    void deleteCategory_InUse_ThrowsConflict() {
        Long catId = 5L;
        AssetCategory cat = new AssetCategory(orgId, "Monitors", "MON", "Displays");
        cat.setId(catId);

        when(categoryRepository.findByIdAndOrganizationId(catId, orgId)).thenReturn(Optional.of(cat));
        when(assetRepository.existsByCategoryIdAndDeletedFalse(catId)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> categoryService.deleteCategory(orgId, catId));
    }
}
