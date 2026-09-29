package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.service.AppraisalConfigurationExtendedService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AppraisalConfigurationExtendedIntegrationTest {

    @Autowired
    private AppraisalConfigurationExtendedService extendedService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Organization org;
    private Employee adminEmp;

    @BeforeEach
    public void setup() {
        long ts = System.currentTimeMillis();
        org = new Organization();
        org.setName("Config Deep Org " + ts);
        org.setOrganizationCode("CFG-" + ts);
        org = organizationRepository.save(org);

        TenantContext.setCurrentTenant(org.getId());

        Role adminRole = roleRepository.findByName("PLATFORM_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("PLATFORM_ADMIN");
                    r.setOrganization(org);
                    return roleRepository.save(r);
                });

        adminEmp = new Employee();
        adminEmp.setOrganization(org);
        adminEmp.setFirstName("Admin");
        adminEmp.setLastName("User");
        adminEmp.setEmail("admin.cfg." + ts + "@test.com");
        adminEmp.setDepartment("HR");
        adminEmp.setDesignation("HR Admin");
        adminEmp.setJoiningDate(LocalDate.now().minusYears(2));
        adminEmp = employeeRepository.save(adminEmp);

        User u = new User();
        u.setWorkEmail(adminEmp.getEmail());
        u.setRole(adminRole);
        u.setOrganization(org);
        userRepository.save(u);
    }

    @AfterEach
    public void cleanup() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("CFG-EXT-01: Granular Review Stage CRUD")
    public void testReviewStageGranularCrud() {
        TenantContext.setCurrentTenant(org.getId());

        // 1. Create stage
        ReviewStageConfigurationDto stage1 = new ReviewStageConfigurationDto(1, "Technical Evaluation", "APPRAISAL_REVIEW", true);
        ReviewStageConfigurationDto created1 = extendedService.createReviewStage(stage1);
        assertNotNull(created1.getId());
        assertEquals("Technical Evaluation", created1.getStageName());

        ReviewStageConfigurationDto stage2 = new ReviewStageConfigurationDto(2, "Department Head Approval", "APPRAISAL_APPROVE", true);
        ReviewStageConfigurationDto created2 = extendedService.createReviewStage(stage2);
        assertNotNull(created2.getId());

        // 2. Reject duplicate stage order
        assertThrows(IllegalArgumentException.class, () -> {
            extendedService.createReviewStage(new ReviewStageConfigurationDto(1, "Duplicate Order Stage", "APPRAISAL_REVIEW", true));
        });

        // 3. Update stage
        created1.setStageName("Principal Engineer Evaluation");
        ReviewStageConfigurationDto updated = extendedService.updateReviewStage(created1.getId(), created1);
        assertEquals("Principal Engineer Evaluation", updated.getStageName());

        // 4. List stages
        List<ReviewStageConfigurationDto> stages = extendedService.getReviewStages();
        assertEquals(2, stages.size());

        // 5. Delete stage
        extendedService.deleteReviewStage(created2.getId());
        assertEquals(1, extendedService.getReviewStages().size());
    }

    @Test
    @DisplayName("CFG-EXT-02: Rating Scale & Levels CRUD")
    public void testRatingScaleAndLevels() {
        TenantContext.setCurrentTenant(org.getId());

        RatingScaleDto dto = new RatingScaleDto();
        dto.setName("Enterprise 1-10 Scale");
        dto.setScaleType("NUMERIC");
        dto.setMinRating(1.0);
        dto.setMaxRating(10.0);
        dto.setStepValue(0.5);
        dto.setLevels(List.of(
                createLevel("Unsatisfactory", 1.0, 3.9, 1),
                createLevel("Meets Expectations", 4.0, 7.9, 2),
                createLevel("Outstanding", 8.0, 10.0, 3)
        ));

        RatingScaleDto saved = extendedService.saveOrUpdateRatingScale(dto);
        assertNotNull(saved.getId());
        assertEquals("Enterprise 1-10 Scale", saved.getName());
        assertEquals(10.0, saved.getMaxRating());
        assertEquals(3, saved.getLevels().size());
    }

    private RatingScaleLevelDto createLevel(String label, double min, double max, int order) {
        RatingScaleLevelDto l = new RatingScaleLevelDto();
        l.setLabel(label);
        l.setMinScore(min);
        l.setMaxScore(max);
        l.setLevelOrder(order);
        return l;
    }

    @Test
    @DisplayName("CFG-EXT-03: Criteria & Weights Validation")
    public void testCriteriaAndWeights() {
        TenantContext.setCurrentTenant(org.getId());

        AppraisalCriterionDto c1 = new AppraisalCriterionDto();
        c1.setName("System Reliability");
        c1.setWeight(50.0);
        c1.setRequired(true);
        AppraisalCriterionDto saved1 = extendedService.createCriterion(c1);
        assertNotNull(saved1.getId());

        AppraisalCriterionDto c2 = new AppraisalCriterionDto();
        c2.setName("Domain Execution");
        c2.setWeight(50.0);
        c2.setRequired(true);
        AppraisalCriterionDto saved2 = extendedService.createCriterion(c2);
        assertNotNull(saved2.getId());

        List<AppraisalCriterionDto> list = extendedService.getCriteria();
        assertEquals(2, list.size());

        // Validate configuration returns valid because weights total 100%
        AppraisalConfigurationValidationResponseDto val = extendedService.validateConfiguration();
        assertTrue(val.isValid(), "Configuration should be valid when criteria weights total 100%");
    }

    @Test
    @DisplayName("CFG-EXT-04: Proactive Validation Detects Overlapping Performance Categories")
    public void testValidationDetectsOverlappingCategories() {
        TenantContext.setCurrentTenant(org.getId());

        // Create overlapping categories
        PerformanceCategoryDto cat1 = new PerformanceCategoryDto();
        cat1.setName("Meets Expectations");
        cat1.setMinRating(3.0);
        cat1.setMaxRating(4.5); // Overlaps with cat2 minRating of 4.0!
        extendedService.createPerformanceCategory(cat1);

        PerformanceCategoryDto cat2 = new PerformanceCategoryDto();
        cat2.setName("Exceeds Expectations");
        cat2.setMinRating(4.0);
        cat2.setMaxRating(5.0);
        extendedService.createPerformanceCategory(cat2);

        AppraisalConfigurationValidationResponseDto val = extendedService.validateConfiguration();
        assertFalse(val.isValid(), "Validation must fail when performance category ranges overlap");
        assertTrue(val.getErrors().stream().anyMatch(e -> "OVERLAPPING_RANGE".equals(e.getCode())));
    }

    @Test
    @DisplayName("CFG-EXT-05: Immutable Configuration Snapshot & Versioning")
    public void testConfigurationSnapshotVersioning() {
        TenantContext.setCurrentTenant(org.getId());

        // Create valid criteria & performance categories
        AppraisalCriterionDto c1 = new AppraisalCriterionDto();
        c1.setName("Technical Excellence");
        c1.setWeight(100.0);
        extendedService.createCriterion(c1);

        // Snapshot v1
        AppraisalConfigurationVersionResponseDto snapshot1 = extendedService.createSnapshot(adminEmp, "Initial 2026 Policy v1");
        assertNotNull(snapshot1.getId());
        assertEquals(1, snapshot1.getVersionNumber());
        assertEquals("Initial 2026 Policy v1", snapshot1.getDescription());

        // Snapshot v2
        AppraisalConfigurationVersionResponseDto snapshot2 = extendedService.createSnapshot(adminEmp, "Updated 2026 Policy v2");
        assertEquals(2, snapshot2.getVersionNumber());

        // Retrieve historical snapshot v1
        AppraisalConfigurationSnapshotDto retrievedV1 = extendedService.getSnapshotByVersion(1);
        assertNotNull(retrievedV1);
        assertEquals(1, retrievedV1.getVersionNumber());
    }
}
