package com.example.ems.support.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.support.dto.*;
import com.example.ems.support.entity.*;
import com.example.ems.support.service.PlatformSupportCategoryService;
import com.example.ems.security.service.JwtService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlatformSupportCategoryControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper = new ObjectMapper();

        @Mock
        private PlatformSupportCategoryService categoryService;
        @Mock
        private UserRepository userRepository;
        @Mock
        private JwtService jwtService;
        @Mock
        private RoleService roleService;

        @InjectMocks
        private PlatformSupportCategoryController controller;

        private final String adminEmail = "admin@company.com";
        private final String token = "admin-mock-token";

        @BeforeEach
        public void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

                User adminUser = new User();
                adminUser.setId(1L);
                adminUser.setWorkEmail(adminEmail);
                adminUser.setFullName("Platform Admin");

                when(jwtService.validateAccessToken(token)).thenReturn(true);
                when(jwtService.getEmailFromToken(token)).thenReturn(adminEmail);
                when(userRepository.findByWorkEmail(adminEmail)).thenReturn(Optional.of(adminUser));
                when(roleService.isSuperAdmin(adminEmail)).thenReturn(true);
        }

        private void setupMockPermissions(boolean allowed) {
                when(roleService.isSuperAdmin(adminEmail)).thenReturn(false);
                when(roleService.hasPermission(eq(adminEmail), any(String.class))).thenReturn(allowed);
        }

        @Test
        public void testGetStatsUnauthorized() throws Exception {
                when(jwtService.validateAccessToken(any())).thenReturn(false);

                mockMvc.perform(get("/api/platform/support/categories/stats")
                                .header("Authorization", "Bearer invalid-token"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        public void testGetStatsForbidden() throws Exception {
                setupMockPermissions(false);

                mockMvc.perform(get("/api/platform/support/categories/stats")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isForbidden());
        }

        @Test
        public void testGetStatsSuccess() throws Exception {
                setupMockPermissions(true);

                PlatformCategoryStatsResponse stats = new PlatformCategoryStatsResponse(
                                5L, 4L, 1L,
                                new PlatformCategoryStatsResponse.MostUsedCategory(1L, "Technical", 12L, 60.0));
                when(categoryService.getDashboardStats()).thenReturn(stats);

                mockMvc.perform(get("/api/platform/support/categories/stats")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.totalCategories").value(5))
                                .andExpect(jsonPath("$.data.mostUsedCategory.name").value("Technical"));
        }

        @Test
        public void testGetCategoriesPaginated() throws Exception {
                setupMockPermissions(true);

                MySupportCategory category = new MySupportCategory(1L, "Technical", "tool");
                category.setStatus(CategoryStatus.ACTIVE);

                when(categoryService.getCategories(any(), any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(category), PageRequest.of(0, 10), 1L));

                mockMvc.perform(get("/api/platform/support/categories")
                                .param("page", "0")
                                .param("limit", "10")
                                .param("status", "ACTIVE")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.items[0].name").value("Technical"))
                                .andExpect(jsonPath("$.data.pagination.totalItems").value(1));
        }

        @Test
        public void testCreateCategorySuccess() throws Exception {
                setupMockPermissions(true);

                PlatformCategoryRequest req = new PlatformCategoryRequest();
                req.setName("Billing");
                req.setDescription("Payments related");
                req.setIcon("credit-card");
                req.setColor("#2563EB");
                req.setStatus("ACTIVE");

                MySupportCategory created = new MySupportCategory(2L, "Billing", "credit-card");
                when(categoryService.createCategory(any(), eq(adminEmail))).thenReturn(created);

                mockMvc.perform(post("/api/platform/support/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.name").value("Billing"));
        }

        @Test
        public void testCreateCategoryInvalidColor() throws Exception {
                setupMockPermissions(true);

                PlatformCategoryRequest req = new PlatformCategoryRequest();
                req.setName("Billing");
                req.setColor("blue-invalid"); // Regex check should fail

                mockMvc.perform(post("/api/platform/support/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void testDeleteCategoryFailedWithTickets() throws Exception {
                setupMockPermissions(true);

                doThrow(new IllegalArgumentException("Category is assigned to 15 support tickets."))
                                .when(categoryService).deleteCategory(1L);

                mockMvc.perform(delete("/api/platform/support/categories/1")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.message")
                                                .value("Category is assigned to 15 support tickets."));
        }

        @Test
        public void testDeleteCategorySuccess() throws Exception {
                setupMockPermissions(true);

                doNothing().when(categoryService).deleteCategory(1L);

                mockMvc.perform(delete("/api/platform/support/categories/1")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        public void testReorderCategories() throws Exception {
                setupMockPermissions(true);

                PlatformCategoryReorderRequest reorder = new PlatformCategoryReorderRequest();
                reorder.setCategories(List.of(
                                new PlatformCategoryReorderRequest.CategoryOrderDto(1L, 2),
                                new PlatformCategoryReorderRequest.CategoryOrderDto(2L, 1)));

                MySupportCategory cat1 = new MySupportCategory(1L, "Technical", "tool");
                cat1.setDisplayOrder(2);
                MySupportCategory cat2 = new MySupportCategory(2L, "Billing", "credit-card");
                cat2.setDisplayOrder(1);

                when(categoryService.reorderCategories(any())).thenReturn(List.of(cat1, cat2));

                mockMvc.perform(patch("/api/platform/support/categories/reorder")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reorder))
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].id").value(1))
                                .andExpect(jsonPath("$.data[0].displayOrder").value(2))
                                .andExpect(jsonPath("$.data[1].id").value(2))
                                .andExpect(jsonPath("$.data[1].displayOrder").value(1));
        }

        @Test
        public void testChangeStatusSuccess() throws Exception {
                setupMockPermissions(true);

                MySupportCategory cat = new MySupportCategory(1L, "Technical", "tool");
                cat.setStatus(CategoryStatus.INACTIVE);

                when(categoryService.changeStatus(eq(1L), eq("INACTIVE"), eq(adminEmail))).thenReturn(cat);

                mockMvc.perform(patch("/api/platform/support/categories/1/status")
                                .param("status", "INACTIVE")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
        }
}
