package com.example.ems.appraisal.service;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.*;
import com.example.ems.appraisal.repository.*;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppraisalConfigurationExtendedService {

    @Autowired
    private AppraisalConfigurationRepository configRepository;

    @Autowired
    private AppraisalReviewStageRepository reviewStageRepository;

    @Autowired
    private AppraisalRatingScaleRepository ratingScaleRepository;

    @Autowired
    private AppraisalRatingScaleLevelRepository ratingScaleLevelRepository;

    @Autowired
    private AppraisalCriterionRepository criterionRepository;

    @Autowired
    private AppraisalPerformanceCategoryRepository performanceCategoryRepository;

    @Autowired
    private AppraisalIncrementPolicyRepository incrementPolicyRepository;

    @Autowired
    private AppraisalIncrementRuleRepository incrementRuleRepository;

    @Autowired
    private AppraisalConfigurationVersionRepository versionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ── 1. REVIEW STAGES GRANULAR CRUD ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ReviewStageConfigurationDto> getReviewStages() {
        Long orgId = TenantContext.requireOrganizationId();
        List<AppraisalReviewStage> stages = reviewStageRepository.findByOrganizationIdOrderByStageOrderAsc(orgId);
        if (stages.isEmpty()) {
            return List.of(
                    new ReviewStageConfigurationDto(1, "Team Lead Review", "APPRAISAL_REVIEW", true),
                    new ReviewStageConfigurationDto(2, "Manager Review", "APPRAISAL_REVIEW", true),
                    new ReviewStageConfigurationDto(3, "HR Review", "APPRAISAL_REVIEW", true));
        }
        return stages.stream().map(s -> {
            ReviewStageConfigurationDto dto = new ReviewStageConfigurationDto(
                    s.getStageOrder(), s.getStageName(), s.getRequiredPermission(), s.isRequired());
            dto.setId(s.getId());
            dto.setWeightage(s.getWeightage());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public ReviewStageConfigurationDto createReviewStage(ReviewStageConfigurationDto dto) {
        Long orgId = TenantContext.requireOrganizationId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (dto.getStageOrder() == null || dto.getStageOrder() <= 0) {
            throw new IllegalArgumentException("Stage order must be a positive integer");
        }
        if (dto.getStageName() == null || dto.getStageName().isBlank()) {
            throw new IllegalArgumentException("Stage name cannot be blank");
        }

        // Check for duplicate stage order
        List<AppraisalReviewStage> existing = reviewStageRepository.findByOrganizationIdOrderByStageOrderAsc(orgId);
        boolean duplicateOrder = existing.stream().anyMatch(s -> s.getStageOrder().equals(dto.getStageOrder()));
        if (duplicateOrder) {
            throw new IllegalArgumentException("Duplicate review stage order: " + dto.getStageOrder());
        }

        AppraisalReviewStage stage = new AppraisalReviewStage();
        stage.setOrganization(org);
        stage.setStageOrder(dto.getStageOrder());
        stage.setStageName(dto.getStageName().trim());
        stage.setRequiredPermission(
                dto.getRequiredPermission() != null ? dto.getRequiredPermission() : "APPRAISAL_REVIEW");
        stage.setRequired(dto.isRequired());
        stage.setWeightage(dto.getWeightage() != null ? dto.getWeightage() : 1.0);
        stage = reviewStageRepository.save(stage);

        ReviewStageConfigurationDto result = new ReviewStageConfigurationDto(
                stage.getStageOrder(), stage.getStageName(), stage.getRequiredPermission(), stage.isRequired());
        result.setId(stage.getId());
        result.setWeightage(stage.getWeightage());
        return result;
    }

    @Transactional
    public ReviewStageConfigurationDto updateReviewStage(Long stageId, ReviewStageConfigurationDto dto) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalReviewStage stage = reviewStageRepository.findById(stageId)
                .filter(s -> s.getOrganization().getId().equals(orgId))
                .orElseThrow(() -> new ResourceNotFoundException("Review stage not found"));

        if (dto.getStageName() != null && !dto.getStageName().isBlank()) {
            stage.setStageName(dto.getStageName().trim());
        }
        if (dto.getRequiredPermission() != null) {
            stage.setRequiredPermission(dto.getRequiredPermission());
        }
        stage.setRequired(dto.isRequired());
        if (dto.getWeightage() != null) {
            stage.setWeightage(dto.getWeightage());
        }
        stage.setUpdatedAt(LocalDateTime.now());
        stage = reviewStageRepository.save(stage);

        ReviewStageConfigurationDto result = new ReviewStageConfigurationDto(
                stage.getStageOrder(), stage.getStageName(), stage.getRequiredPermission(), stage.isRequired());
        result.setId(stage.getId());
        result.setWeightage(stage.getWeightage());
        return result;
    }

    @Transactional
    public void deleteReviewStage(Long stageId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalReviewStage stage = reviewStageRepository.findById(stageId)
                .filter(s -> s.getOrganization().getId().equals(orgId))
                .orElseThrow(() -> new ResourceNotFoundException("Review stage not found"));
        reviewStageRepository.delete(stage);
    }

    @Transactional
    public ReviewStageConfigurationDto updateReviewStageStatus(Long stageId, boolean required) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalReviewStage stage = reviewStageRepository.findById(stageId)
                .filter(s -> s.getOrganization().getId().equals(orgId))
                .orElseThrow(() -> new ResourceNotFoundException("Review stage not found"));
        stage.setRequired(required);
        stage.setUpdatedAt(LocalDateTime.now());
        stage = reviewStageRepository.save(stage);

        ReviewStageConfigurationDto result = new ReviewStageConfigurationDto(
                stage.getStageOrder(), stage.getStageName(), stage.getRequiredPermission(), stage.isRequired());
        result.setId(stage.getId());
        result.setWeightage(stage.getWeightage());
        return result;
    }

    // ── 2. RATING SCALE & LEVELS CRUD ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public RatingScaleDto getRatingScale() {
        Long orgId = TenantContext.requireOrganizationId();
        Optional<AppraisalRatingScale> scaleOpt = ratingScaleRepository.findByOrganizationIdAndActiveTrue(orgId);
        if (scaleOpt.isEmpty()) {
            // Default 1-5 Numeric Rating Scale
            RatingScaleDto def = new RatingScaleDto();
            def.setName("Standard 1-5 Scale");
            def.setScaleType("NUMERIC");
            def.setMinRating(1.0);
            def.setMaxRating(5.0);
            def.setStepValue(0.1);
            def.setActive(true);
            def.setLevels(List.of(
                    createLevelDto(null, "Unsatisfactory", 1.0, 1.9, "Below requirements", 1),
                    createLevelDto(null, "Needs Improvement", 2.0, 2.9, "Approaching requirements", 2),
                    createLevelDto(null, "Meets Expectations", 3.0, 3.9, "Consistently meets standards", 3),
                    createLevelDto(null, "Exceeds Expectations", 4.0, 4.7, "Exceeds standards", 4),
                    createLevelDto(null, "Outstanding", 4.8, 5.0, "Exceptional performance", 5)));
            return def;
        }

        AppraisalRatingScale scale = scaleOpt.get();
        RatingScaleDto dto = new RatingScaleDto();
        dto.setId(scale.getId());
        dto.setName(scale.getName());
        dto.setScaleType(scale.getScaleType());
        dto.setMinRating(scale.getMinRating());
        dto.setMaxRating(scale.getMaxRating());
        dto.setStepValue(scale.getStepValue());
        dto.setActive(scale.getActive());

        List<AppraisalRatingScaleLevel> levels = ratingScaleLevelRepository
                .findByRatingScaleIdOrderByLevelOrderAsc(scale.getId());
        dto.setLevels(levels.stream().map(l -> {
            RatingScaleLevelDto lDto = new RatingScaleLevelDto();
            lDto.setId(l.getId());
            lDto.setRatingScaleId(scale.getId());
            lDto.setLabel(l.getLabel());
            lDto.setMinScore(l.getMinScore());
            lDto.setMaxScore(l.getMaxScore());
            lDto.setDescription(l.getDescription());
            lDto.setLevelOrder(l.getLevelOrder());
            return lDto;
        }).collect(Collectors.toList()));

        return dto;
    }

    private RatingScaleLevelDto createLevelDto(Long id, String label, double min, double max, String desc, int order) {
        RatingScaleLevelDto d = new RatingScaleLevelDto();
        d.setId(id);
        d.setLabel(label);
        d.setMinScore(min);
        d.setMaxScore(max);
        d.setDescription(desc);
        d.setLevelOrder(order);
        return d;
    }

    @Transactional
    public RatingScaleDto saveOrUpdateRatingScale(RatingScaleDto dto) {
        Long orgId = TenantContext.requireOrganizationId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (dto.getMinRating() >= dto.getMaxRating()) {
            throw new IllegalArgumentException("minRating must be less than maxRating");
        }

        AppraisalRatingScale scale = ratingScaleRepository.findByOrganizationIdAndActiveTrue(orgId)
                .orElseGet(() -> {
                    AppraisalRatingScale s = new AppraisalRatingScale();
                    s.setOrganization(org);
                    return s;
                });

        scale.setName(dto.getName() != null ? dto.getName().trim() : "Custom Rating Scale");
        scale.setScaleType(dto.getScaleType() != null ? dto.getScaleType() : "NUMERIC");
        scale.setMinRating(dto.getMinRating());
        scale.setMaxRating(dto.getMaxRating());
        scale.setStepValue(dto.getStepValue() != null ? dto.getStepValue() : 0.1);
        scale.setActive(dto.getActive() != null ? dto.getActive() : true);
        scale.setUpdatedAt(LocalDateTime.now());
        scale = ratingScaleRepository.save(scale);

        if (dto.getLevels() != null && !dto.getLevels().isEmpty()) {
            // Replace levels
            ratingScaleLevelRepository
                    .deleteAll(ratingScaleLevelRepository.findByRatingScaleIdOrderByLevelOrderAsc(scale.getId()));
            for (int i = 0; i < dto.getLevels().size(); i++) {
                RatingScaleLevelDto lDto = dto.getLevels().get(i);
                AppraisalRatingScaleLevel level = new AppraisalRatingScaleLevel();
                level.setOrganization(org);
                level.setRatingScale(scale);
                level.setLabel(lDto.getLabel());
                level.setMinScore(lDto.getMinScore());
                level.setMaxScore(lDto.getMaxScore());
                level.setDescription(lDto.getDescription());
                level.setLevelOrder(lDto.getLevelOrder() != null ? lDto.getLevelOrder() : i + 1);
                ratingScaleLevelRepository.save(level);
            }
        }

        return getRatingScale();
    }

    // ── 3. CRITERIA & WEIGHTAGE CRUD ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AppraisalCriterionDto> getCriteria() {
        Long orgId = TenantContext.requireOrganizationId();
        List<AppraisalCriterion> criteria = criterionRepository.findByOrganizationId(orgId);
        if (criteria.isEmpty()) {
            return List.of(
                    createCriterionDto(null, "Technical Excellence", "Coding quality, architecture, problem solving",
                            30.0, true, true),
                    createCriterionDto(null, "Delivery & Reliability", "On-time delivery, execution, sprint velocity",
                            30.0, true, true),
                    createCriterionDto(null, "Collaboration & Leadership",
                            "Team mentoring, cross-functional communication", 40.0, true, true));
        }
        return criteria.stream().map(this::mapToCriterionDto).collect(Collectors.toList());
    }

    private AppraisalCriterionDto createCriterionDto(Long id, String name, String desc, double weight, boolean req,
            boolean act) {
        AppraisalCriterionDto d = new AppraisalCriterionDto();
        d.setId(id);
        d.setName(name);
        d.setDescription(desc);
        d.setWeight(weight);
        d.setRequired(req);
        d.setActive(act);
        return d;
    }

    private AppraisalCriterionDto mapToCriterionDto(AppraisalCriterion c) {
        AppraisalCriterionDto d = new AppraisalCriterionDto();
        d.setId(c.getId());
        d.setName(c.getName());
        d.setDescription(c.getDescription());
        d.setWeight(c.getWeight());
        d.setRequired(c.getRequired());
        d.setActive(c.getActive());
        return d;
    }

    @Transactional
    public AppraisalCriterionDto createCriterion(AppraisalCriterionDto dto) {
        Long orgId = TenantContext.requireOrganizationId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Criterion name cannot be blank");
        }
        if (dto.getWeight() < 0 || dto.getWeight() > 100) {
            throw new IllegalArgumentException("Criterion weight must be between 0 and 100%");
        }

        criterionRepository.findByOrganizationIdAndName(orgId, dto.getName().trim()).ifPresent(c -> {
            throw new IllegalArgumentException("Criterion with name '" + dto.getName() + "' already exists");
        });

        AppraisalCriterion criterion = new AppraisalCriterion();
        criterion.setOrganization(org);
        criterion.setName(dto.getName().trim());
        criterion.setDescription(dto.getDescription());
        criterion.setWeight(dto.getWeight());
        criterion.setRequired(dto.getRequired() != null ? dto.getRequired() : true);
        criterion.setActive(dto.getActive() != null ? dto.getActive() : true);
        criterion = criterionRepository.save(criterion);

        return mapToCriterionDto(criterion);
    }

    @Transactional
    public AppraisalCriterionDto updateCriterion(Long criterionId, AppraisalCriterionDto dto) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalCriterion criterion = criterionRepository.findByIdAndOrganizationId(criterionId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Criterion not found"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            criterion.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            criterion.setDescription(dto.getDescription());
        }
        if (dto.getWeight() != null) {
            if (dto.getWeight() < 0 || dto.getWeight() > 100) {
                throw new IllegalArgumentException("Weight must be between 0 and 100%");
            }
            criterion.setWeight(dto.getWeight());
        }
        if (dto.getRequired() != null) {
            criterion.setRequired(dto.getRequired());
        }
        if (dto.getActive() != null) {
            criterion.setActive(dto.getActive());
        }
        criterion.setUpdatedAt(LocalDateTime.now());
        criterion = criterionRepository.save(criterion);

        return mapToCriterionDto(criterion);
    }

    @Transactional
    public void deleteCriterion(Long criterionId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalCriterion criterion = criterionRepository.findByIdAndOrganizationId(criterionId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Criterion not found"));
        criterionRepository.delete(criterion);
    }

    // ── 4. PERFORMANCE CATEGORIES CRUD ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PerformanceCategoryDto> getPerformanceCategories() {
        Long orgId = TenantContext.requireOrganizationId();
        List<AppraisalPerformanceCategory> categories = performanceCategoryRepository
                .findByOrganizationIdOrderByMinRatingDesc(orgId);
        if (categories.isEmpty()) {
            return List.of(
                    createCategoryDto(null, "Outstanding", 4.5, 5.0,
                            "Exceptional performance exceeding all expectations", "#10B981", true),
                    createCategoryDto(null, "Exceeds Expectations", 3.8, 4.49,
                            "Consistently performs above required baseline", "#3B82F6", true),
                    createCategoryDto(null, "Meets Expectations", 3.0, 3.79, "Reliably meets all key objectives",
                            "#F59E0B", true),
                    createCategoryDto(null, "Needs Improvement", 1.0, 2.99,
                            "Performance below expectations; requires development plan", "#EF4444", true));
        }
        return categories.stream().map(this::mapToCategoryDto).collect(Collectors.toList());
    }

    private PerformanceCategoryDto createCategoryDto(Long id, String name, double min, double max, String desc,
            String color, boolean act) {
        PerformanceCategoryDto d = new PerformanceCategoryDto();
        d.setId(id);
        d.setName(name);
        d.setMinRating(min);
        d.setMaxRating(max);
        d.setDescription(desc);
        d.setColorCode(color);
        d.setActive(act);
        return d;
    }

    private PerformanceCategoryDto mapToCategoryDto(AppraisalPerformanceCategory c) {
        PerformanceCategoryDto d = new PerformanceCategoryDto();
        d.setId(c.getId());
        d.setName(c.getName());
        d.setMinRating(c.getMinRating());
        d.setMaxRating(c.getMaxRating());
        d.setDescription(c.getDescription());
        d.setColorCode(c.getColorCode());
        d.setActive(c.getActive());
        return d;
    }

    @Transactional
    public PerformanceCategoryDto createPerformanceCategory(PerformanceCategoryDto dto) {
        Long orgId = TenantContext.requireOrganizationId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Performance category name cannot be blank");
        }
        if (dto.getMinRating() >= dto.getMaxRating()) {
            throw new IllegalArgumentException("minRating must be strictly less than maxRating");
        }

        performanceCategoryRepository.findByOrganizationIdAndName(orgId, dto.getName().trim()).ifPresent(c -> {
            throw new IllegalArgumentException("Category with name '" + dto.getName() + "' already exists");
        });

        AppraisalPerformanceCategory category = new AppraisalPerformanceCategory();
        category.setOrganization(org);
        category.setName(dto.getName().trim());
        category.setMinRating(dto.getMinRating());
        category.setMaxRating(dto.getMaxRating());
        category.setDescription(dto.getDescription());
        category.setColorCode(dto.getColorCode() != null ? dto.getColorCode() : "#3B82F6");
        category.setActive(dto.getActive() != null ? dto.getActive() : true);
        category = performanceCategoryRepository.save(category);

        return mapToCategoryDto(category);
    }

    @Transactional
    public PerformanceCategoryDto updatePerformanceCategory(Long categoryId, PerformanceCategoryDto dto) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalPerformanceCategory category = performanceCategoryRepository
                .findByIdAndOrganizationId(categoryId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance category not found"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            category.setName(dto.getName().trim());
        }
        if (dto.getMinRating() != null && dto.getMaxRating() != null) {
            if (dto.getMinRating() >= dto.getMaxRating()) {
                throw new IllegalArgumentException("minRating must be strictly less than maxRating");
            }
            category.setMinRating(dto.getMinRating());
            category.setMaxRating(dto.getMaxRating());
        }
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
        if (dto.getColorCode() != null) {
            category.setColorCode(dto.getColorCode());
        }
        if (dto.getActive() != null) {
            category.setActive(dto.getActive());
        }
        category.setUpdatedAt(LocalDateTime.now());
        category = performanceCategoryRepository.save(category);

        return mapToCategoryDto(category);
    }

    @Transactional
    public void deletePerformanceCategory(Long categoryId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalPerformanceCategory category = performanceCategoryRepository
                .findByIdAndOrganizationId(categoryId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance category not found"));
        performanceCategoryRepository.delete(category);
    }

    // ── 5. INCREMENT POLICY & RULES CRUD ────────────────────────────────────────

    @Transactional(readOnly = true)
    public IncrementPolicyDto getIncrementPolicy() {
        Long orgId = TenantContext.requireOrganizationId();
        Optional<AppraisalIncrementPolicy> policyOpt = incrementPolicyRepository
                .findByOrganizationIdAndActiveTrue(orgId);
        if (policyOpt.isEmpty()) {
            IncrementPolicyDto def = new IncrementPolicyDto();
            def.setName("Standard Performance Increment Policy");
            def.setActive(true);
            def.setRules(List.of(
                    createRuleDto(null, "Outstanding", 4.5, 5.0, true, 20.0, 10.0),
                    createRuleDto(null, "Exceeds Expectations", 3.8, 4.49, true, 15.0, 5.0),
                    createRuleDto(null, "Meets Expectations", 3.0, 3.79, true, 10.0, 0.0),
                    createRuleDto(null, "Needs Improvement", 1.0, 2.99, false, 0.0, 0.0)));
            return def;
        }

        AppraisalIncrementPolicy policy = policyOpt.get();
        IncrementPolicyDto dto = new IncrementPolicyDto();
        dto.setId(policy.getId());
        dto.setName(policy.getName());
        dto.setActive(policy.getActive());
        dto.setEffectiveFrom(policy.getEffectiveFrom());

        List<AppraisalIncrementRule> rules = incrementRuleRepository.findByPolicyIdOrderByMinRatingDesc(policy.getId());
        dto.setRules(rules.stream().map(r -> {
            IncrementRuleDto rDto = new IncrementRuleDto();
            rDto.setId(r.getId());
            rDto.setPolicyId(policy.getId());
            rDto.setPerformanceCategoryId(
                    r.getPerformanceCategory() != null ? r.getPerformanceCategory().getId() : null);
            rDto.setPerformanceCategoryName(r.getPerformanceCategoryName());
            rDto.setMinRating(r.getMinRating());
            rDto.setMaxRating(r.getMaxRating());
            rDto.setEligible(r.getEligible());
            rDto.setIncrementPercentage(r.getIncrementPercentage());
            rDto.setBonusPercentage(r.getBonusPercentage());
            rDto.setActive(r.getActive());
            return rDto;
        }).collect(Collectors.toList()));

        return dto;
    }

    private IncrementRuleDto createRuleDto(Long id, String catName, double min, double max, boolean eligible,
            double inc, double bonus) {
        IncrementRuleDto d = new IncrementRuleDto();
        d.setId(id);
        d.setPerformanceCategoryName(catName);
        d.setMinRating(min);
        d.setMaxRating(max);
        d.setEligible(eligible);
        d.setIncrementPercentage(inc);
        d.setBonusPercentage(bonus);
        d.setActive(true);
        return d;
    }

    @Transactional
    public IncrementPolicyDto saveOrUpdateIncrementPolicy(IncrementPolicyDto dto) {
        Long orgId = TenantContext.requireOrganizationId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        AppraisalIncrementPolicy policy = incrementPolicyRepository.findByOrganizationIdAndActiveTrue(orgId)
                .orElseGet(() -> {
                    AppraisalIncrementPolicy p = new AppraisalIncrementPolicy();
                    p.setOrganization(org);
                    return p;
                });

        policy.setName(dto.getName() != null ? dto.getName().trim() : "Standard Increment Policy");
        policy.setActive(dto.getActive() != null ? dto.getActive() : true);
        policy.setEffectiveFrom(dto.getEffectiveFrom());
        policy.setUpdatedAt(LocalDateTime.now());
        policy = incrementPolicyRepository.save(policy);

        if (dto.getRules() != null && !dto.getRules().isEmpty()) {
            incrementRuleRepository
                    .deleteAll(incrementRuleRepository.findByPolicyIdOrderByMinRatingDesc(policy.getId()));
            for (IncrementRuleDto rDto : dto.getRules()) {
                AppraisalIncrementRule rule = new AppraisalIncrementRule();
                rule.setOrganization(org);
                rule.setPolicy(policy);
                rule.setPerformanceCategoryName(rDto.getPerformanceCategoryName());
                rule.setMinRating(rDto.getMinRating());
                rule.setMaxRating(rDto.getMaxRating());
                rule.setEligible(rDto.getEligible() != null ? rDto.getEligible() : true);
                rule.setIncrementPercentage(rDto.getIncrementPercentage());
                rule.setBonusPercentage(rDto.getBonusPercentage());
                rule.setActive(rDto.getActive() != null ? rDto.getActive() : true);
                incrementRuleRepository.save(rule);
            }
        }

        return getIncrementPolicy();
    }

    // ── 6. CONFIGURATION VALIDATION ENGINE ──────────────────────────────────────

    @Transactional(readOnly = true)
    public AppraisalConfigurationValidationResponseDto validateConfiguration() {
        TenantContext.requireOrganizationId();
        List<ValidationErrorDetailDto> errors = new ArrayList<>();

        // 1. Validate Review Stages
        List<ReviewStageConfigurationDto> stages = getReviewStages();
        if (stages.isEmpty()) {
            errors.add(new ValidationErrorDetailDto("reviewStages", "NO_STAGES",
                    "At least one review stage must be configured"));
        } else {
            Set<Integer> orders = new HashSet<>();
            boolean hasRequired = false;
            for (ReviewStageConfigurationDto s : stages) {
                if (!orders.add(s.getStageOrder())) {
                    errors.add(new ValidationErrorDetailDto("reviewStages", "DUPLICATE_ORDER",
                            "Duplicate stage order detected: " + s.getStageOrder()));
                }
                if (s.isRequired()) {
                    hasRequired = true;
                }
            }
            if (!hasRequired) {
                errors.add(new ValidationErrorDetailDto("reviewStages", "NO_REQUIRED_STAGE",
                        "At least one review stage must be marked required"));
            }
        }

        // 2. Validate Criteria & Weights
        List<AppraisalCriterionDto> criteria = getCriteria();
        if (!criteria.isEmpty()) {
            double totalWeight = criteria.stream().filter(AppraisalCriterionDto::getActive)
                    .mapToDouble(AppraisalCriterionDto::getWeight).sum();
            if (Math.abs(totalWeight - 100.0) > 0.01) {
                errors.add(new ValidationErrorDetailDto("criteria", "WEIGHT_TOTAL_INVALID",
                        "Active criteria weights must total exactly 100%. Current total: " + totalWeight + "%"));
            }
        }

        // 3. Validate Performance Categories
        List<PerformanceCategoryDto> categories = getPerformanceCategories();
        if (categories.isEmpty()) {
            errors.add(new ValidationErrorDetailDto("performanceCategories", "NO_CATEGORIES",
                    "At least one performance category must be configured"));
        } else {
            // Check for overlapping rating intervals
            List<PerformanceCategoryDto> activeCats = categories.stream().filter(PerformanceCategoryDto::getActive)
                    .sorted(Comparator.comparing(PerformanceCategoryDto::getMinRating)).collect(Collectors.toList());
            for (int i = 0; i < activeCats.size() - 1; i++) {
                PerformanceCategoryDto cur = activeCats.get(i);
                PerformanceCategoryDto next = activeCats.get(i + 1);
                if (cur.getMaxRating() >= next.getMinRating()) {
                    errors.add(new ValidationErrorDetailDto("performanceCategories", "OVERLAPPING_RANGE",
                            "Rating ranges overlap between '" + cur.getName() + "' (" + cur.getMinRating() + "-"
                                    + cur.getMaxRating() + ") and '" + next.getName() + "' (" + next.getMinRating()
                                    + "-" + next.getMaxRating() + ")"));
                }
            }
        }

        // 4. Validate Increment Rules
        IncrementPolicyDto incPolicy = getIncrementPolicy();
        if (incPolicy.getRules() != null && !incPolicy.getRules().isEmpty()) {
            List<IncrementRuleDto> activeRules = incPolicy.getRules().stream().filter(IncrementRuleDto::getActive)
                    .sorted(Comparator.comparing(IncrementRuleDto::getMinRating)).collect(Collectors.toList());
            for (int i = 0; i < activeRules.size() - 1; i++) {
                IncrementRuleDto cur = activeRules.get(i);
                IncrementRuleDto next = activeRules.get(i + 1);
                if (cur.getMaxRating() >= next.getMinRating()) {
                    errors.add(new ValidationErrorDetailDto("incrementRules", "OVERLAPPING_RULE_RANGE",
                            "Increment rule ranges overlap between (" + cur.getMinRating() + "-" + cur.getMaxRating()
                                    + ") and (" + next.getMinRating() + "-" + next.getMaxRating() + ")"));
                }
            }
        }

        boolean valid = errors.isEmpty();
        return new AppraisalConfigurationValidationResponseDto(valid, errors);
    }

    // ── 7. IMMUTABLE SNAPSHOT & VERSIONING ──────────────────────────────────────

    @Transactional
    public AppraisalConfigurationVersionResponseDto createSnapshot(Employee creator, String description) {
        Long orgId = TenantContext.requireOrganizationId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        // Validate configuration before snapshotting
        AppraisalConfigurationValidationResponseDto val = validateConfiguration();
        if (!val.isValid()) {
            String errorSummary = val.getErrors().stream().map(ValidationErrorDetailDto::getMessage)
                    .collect(Collectors.joining("; "));
            throw new IllegalStateException(
                    "Cannot create configuration snapshot due to validation errors: " + errorSummary);
        }

        // Determine next version number
        Integer nextVer = versionRepository.findFirstByOrganizationIdOrderByVersionNumberDesc(orgId)
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        AppraisalConfigurationSnapshotDto snapshotDto = buildLiveSnapshot(nextVer);

        try {
            String json = objectMapper.writeValueAsString(snapshotDto);

            AppraisalConfigurationVersion ver = new AppraisalConfigurationVersion();
            ver.setOrganization(org);
            ver.setVersionNumber(nextVer);
            ver.setSnapshotJson(json);
            ver.setDescription(description != null ? description : "Appraisal Configuration Snapshot v" + nextVer);
            ver.setCreatedBy(creator);
            ver.setCreatedAt(LocalDateTime.now());
            ver = versionRepository.save(ver);

            AppraisalConfigurationVersionResponseDto response = new AppraisalConfigurationVersionResponseDto();
            response.setId(ver.getId());
            response.setVersionNumber(ver.getVersionNumber());
            response.setDescription(ver.getDescription());
            response.setCreatedById(creator != null ? creator.getId() : null);
            response.setCreatedByName(creator != null ? creator.getFullName() : "System Admin");
            response.setCreatedAt(ver.getCreatedAt());
            response.setSnapshot(snapshotDto);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize configuration snapshot: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public AppraisalConfigurationSnapshotDto buildLiveSnapshot(Integer versionNumber) {
        Long orgId = TenantContext.requireOrganizationId();
        Optional<AppraisalConfiguration> configOpt = configRepository.findByOrganizationId(orgId);

        AppraisalConfigurationSnapshotDto snapshot = new AppraisalConfigurationSnapshotDto();
        snapshot.setVersionNumber(versionNumber != null ? versionNumber : 1);
        snapshot.setInitiationMode(configOpt.map(c -> c.getInitiationMode().name()).orElse("HR_AND_EMPLOYEE"));
        snapshot.setEmployeeRequestEnabled(
                configOpt.map(AppraisalConfiguration::isEmployeeRequestEnabled).orElse(true));
        snapshot.setMinServiceMonths(configOpt.map(AppraisalConfiguration::getMinServiceMonths).orElse(6));
        snapshot.setMinGapMonths(configOpt.map(AppraisalConfiguration::getMinGapMonths).orElse(6));
        snapshot.setReviewStages(getReviewStages());
        snapshot.setRatingScale(getRatingScale());
        snapshot.setCriteria(getCriteria());
        snapshot.setPerformanceCategories(getPerformanceCategories());
        snapshot.setIncrementPolicy(getIncrementPolicy());
        snapshot.setSnapshottedAt(LocalDateTime.now());
        return snapshot;
    }

    @Transactional(readOnly = true)
    public AppraisalConfigurationSnapshotDto getSnapshotByVersion(Integer versionNumber) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalConfigurationVersion ver = versionRepository.findByOrganizationIdAndVersionNumber(orgId, versionNumber)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Configuration version " + versionNumber + " not found"));

        try {
            return objectMapper.readValue(ver.getSnapshotJson(), AppraisalConfigurationSnapshotDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize configuration version snapshot: " + e.getMessage(), e);
        }
    }
}
