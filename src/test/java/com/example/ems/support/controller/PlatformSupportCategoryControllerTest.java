package com.example.ems.support.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.security.service.JwtService;
import com.example.ems.support.dto.*;
import com.example.ems.support.entity.*;
import com.example.ems.support.service.PlatformSupportCategoryService;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

                MySupportCategory cat1 = new MySupportCategory(12L, "Payroll Support", null);
                cat1.setDescription("Issues related to payroll, salary, payslips and compensation.");
                cat1.setStatus(CategoryStatus.ACTIVE);
                cat1.setDisplayOrder(1);
                cat1.setIsDefault(false);
                cat1.setIsSystem(false);

                MySupportCategory cat2 = new MySupportCategory(13L, "Attendance Support", null);
                cat2.setDescription("Issues related to attendance and check-in.");
                cat2.setStatus(CategoryStatus.ACTIVE);
                cat2.setDisplayOrder(2);
                cat2.setIsDefault(false);
                cat2.setIsSystem(false);

                when(categoryService.getCategories(any(), any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(cat1, cat2), PageRequest.of(0, 20), 2L));

                mockMvc.perform(get("/api/platform/support/categories")
                                .param("page", "0")
                                .param("size", "20")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(12))
                                .andExpect(jsonPath("$.content[0].name").value("Payroll Support"))
                                .andExpect(jsonPath("$.content[0].description").value("Issues related to payroll, salary, payslips and compensation."))
                                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                                .andExpect(jsonPath("$.content[0].displayOrder").value(1))
                                .andExpect(jsonPath("$.content[0].isDefault").value(false))
                                .andExpect(jsonPath("$.content[0].isSystem").value(false))
                                .andExpect(jsonPath("$.content[1].id").value(13))
                                .andExpect(jsonPath("$.content[1].name").value("Attendance Support"))
                                .andExpect(jsonPath("$.page").value(0))
                                .andExpect(jsonPath("$.size").value(20))
                                .andExpect(jsonPath("$.totalElements").value(2))
                                .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        public void testGetSingleCategorySuccess() throws Exception {
                setupMockPermissions(true);

                MySupportCategory category = new MySupportCategory(12L, "Payroll Support", "wallet");
                category.setDescription("Issues related to payroll, salary, payslips and compensation.");
                category.setStatus(CategoryStatus.ACTIVE);
                category.setDisplayOrder(1);
                category.setIsDefault(false);
                category.setIsSystem(false);
                category.setCreatedAt(LocalDateTime.of(2026, 9, 25, 11, 30, 0));
                category.setUpdatedAt(LocalDateTime.of(2026, 9, 25, 11, 30, 0));

                when(categoryService.getCategory(12L)).thenReturn(category);

                mockMvc.perform(get("/api/platform/support/categories/12")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(12))
                                .andExpect(jsonPath("$.name").value("Payroll Support"))
                                .andExpect(jsonPath("$.description").value("Issues related to payroll, salary, payslips and compensation."))
                                .andExpect(jsonPath("$.status").value("ACTIVE"))
                                .andExpect(jsonPath("$.displayOrder").value(1))
                                .andExpect(jsonPath("$.isDefault").value(false))
                                .andExpect(jsonPath("$.isSystem").value(false))
                                .andExpect(jsonPath("$.createdAt").exists())
                                .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        public void testCreateCategorySuccess() throws Exception {
                setupMockPermissions(true);

                PlatformCategoryRequest req = new PlatformCategoryRequest();
                req.setName("Payroll Support");
                req.setDescription("Issues related to payroll, salary, payslips and compensation.");

                MySupportCategory created = new MySupportCategory(12L, "Payroll Support", null);
                created.setDescription("Issues related to payroll, salary, payslips and compensation.");
                created.setStatus(CategoryStatus.ACTIVE);
                created.setDisplayOrder(1);
                created.setIsDefault(false);
                created.setIsSystem(false);
                created.setCreatedAt(LocalDateTime.of(2026, 9, 25, 11, 30, 0));
                created.setUpdatedAt(LocalDateTime.of(2026, 9, 25, 11, 30, 0));

                when(categoryService.createCategory(any(), eq(adminEmail))).thenReturn(created);

                mockMvc.perform(post("/api/platform/support/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(12))
                                .andExpect(jsonPath("$.name").value("Payroll Support"))
                                .andExpect(jsonPath("$.description").value("Issues related to payroll, salary, payslips and compensation."))
                                .andExpect(jsonPath("$.status").value("ACTIVE"))
                                .andExpect(jsonPath("$.displayOrder").value(1))
                                .andExpect(jsonPath("$.isDefault").value(false))
                                .andExpect(jsonPath("$.isSystem").value(false))
                                .andExpect(jsonPath("$.createdAt").exists())
                                .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        public void testUpdateCategorySuccess() throws Exception {
                setupMockPermissions(true);

                PlatformCategoryRequest req = new PlatformCategoryRequest();
                req.setName("Payroll & Salary Support");
                req.setDescription("Issues related to payroll, salary, payslips and compensation.");

                MySupportCategory updated = new MySupportCategory(12L, "Payroll & Salary Support", null);
                updated.setDescription("Issues related to payroll, salary, payslips and compensation.");
                updated.setStatus(CategoryStatus.ACTIVE);
                updated.setDisplayOrder(1);
                updated.setIsDefault(false);
                updated.setIsSystem(false);
                updated.setUpdatedAt(LocalDateTime.of(2026, 9, 25, 12, 0, 0));

                when(categoryService.updateCategory(eq(12L), any(), eq(adminEmail))).thenReturn(updated);

                mockMvc.perform(put("/api/platform/support/categories/12")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(12))
                                .andExpect(jsonPath("$.name").value("Payroll & Salary Support"))
                                .andExpect(jsonPath("$.description").value("Issues related to payroll, salary, payslips and compensation."))
                                .andExpect(jsonPath("$.status").value("ACTIVE"))
                                .andExpect(jsonPath("$.displayOrder").value(1))
                                .andExpect(jsonPath("$.isDefault").value(false))
                                .andExpect(jsonPath("$.isSystem").value(false))
                                .andExpect(jsonPath("$.createdAt").doesNotExist())
                                .andExpect(jsonPath("$.updatedAt").exists());
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
                                .andExpect(status().isNoContent());
        }

        @Test
        public void testCategoryOptionsSuccess() throws Exception {
                setupMockPermissions(true);

                List<PlatformCategoryOption> options = List.of(
                                new PlatformCategoryOption(12L, "Payroll Support"),
                                new PlatformCategoryOption(13L, "Attendance Support"),
                                new PlatformCategoryOption(14L, "Leave Support"));

                when(categoryService.getOptions()).thenReturn(options);

                mockMvc.perform(get("/api/platform/support/categories/options")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(12))
                                .andExpect(jsonPath("$[0].name").value("Payroll Support"))
                                .andExpect(jsonPath("$[1].id").value(13))
                                .andExpect(jsonPath("$[1].name").value("Attendance Support"));
        }

        @Test
        public void testReorderCategories() throws Exception {
                setupMockPermissions(true);

                PlatformCategoryReorderRequest reorder = new PlatformCategoryReorderRequest();
                reorder.setCategories(List.of(
                                new PlatformCategoryReorderRequest.CategoryOrderDto(12L, 1),
                                new PlatformCategoryReorderRequest.CategoryOrderDto(13L, 2),
                                new PlatformCategoryReorderRequest.CategoryOrderDto(14L, 3)));

                when(categoryService.reorderCategories(any())).thenReturn(List.of());

                mockMvc.perform(patch("/api/platform/support/categories/reorder")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reorder))
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Categories reordered successfully"));
        }

        @Test
        public void testChangeStatusSuccess() throws Exception {
                setupMockPermissions(true);

                MySupportCategory cat = new MySupportCategory(12L, "Payroll & Salary Support", null);
                cat.setStatus(CategoryStatus.INACTIVE);
                cat.setUpdatedAt(LocalDateTime.of(2026, 9, 25, 12, 15, 0));

                when(categoryService.changeStatus(eq(12L), eq("INACTIVE"), eq(adminEmail))).thenReturn(cat);

                mockMvc.perform(patch("/api/platform/support/categories/12/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"INACTIVE\"}")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(12))
                                .andExpect(jsonPath("$.name").value("Payroll & Salary Support"))
                                .andExpect(jsonPath("$.status").value("INACTIVE"))
                                .andExpect(jsonPath("$.updatedAt").exists());
        }
}
