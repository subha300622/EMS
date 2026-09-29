package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.*;
import com.example.ems.appraisal.repository.*;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class AppraisalValidationIntegrationTest {

    private MockMvc configMockMvc;
    private MockMvc cycleMockMvc;
    private MockMvc requestMockMvc;
    private MockMvc evalMockMvc;

    @Autowired
    private AppraisalConfigurationController configController;

    @Autowired
    private AppraisalCycleController cycleController;

    @Autowired
    private AppraisalRequestController requestController;

    @Autowired
    private AppraisalEvaluationController evalController;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalConfigurationRepository configRepository;

    @Autowired
    private AppraisalRequestReasonRepository reasonRepository;

    @Autowired
    private AppraisalCycleRepository cycleRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Organization org1;
    private Organization org2;
    private Employee emp1;
    private Employee emp2;
    private Employee empOtherOrg;
    private User emp1User;
    private User hrUser;
    private User empOtherOrgUser;
    private String emp1Token;
    private String hrToken;
    private String otherOrgToken;

    private AppraisalRequestReason validReason;

    @BeforeEach
    public void setup() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        configMockMvc = MockMvcBuilders.standaloneSetup(configController).setControllerAdvice(handler).build();
        cycleMockMvc = MockMvcBuilders.standaloneSetup(cycleController).setControllerAdvice(handler).build();
        requestMockMvc = MockMvcBuilders.standaloneSetup(requestController).setControllerAdvice(handler).build();
        evalMockMvc = MockMvcBuilders.standaloneSetup(evalController).setControllerAdvice(handler).build();

        long ts = System.currentTimeMillis();

        org1 = new Organization();
        org1.setName("Org One " + ts);
        org1.setOrganizationCode("ORG-V1-" + ts);
        org1 = organizationRepository.save(org1);

        org2 = new Organization();
        org2.setName("Org Two " + ts);
        org2.setOrganizationCode("ORG-V2-" + ts);
        org2 = organizationRepository.save(org2);

        TenantContext.setCurrentTenant(org1.getId());

        Role hrRole = roleRepository.findByName("PLATFORM_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("PLATFORM_ADMIN");
                    r.setOrganization(org1);
                    return roleRepository.save(r);
                });

        Role empRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE");
                    r.setOrganization(org1);
                    return roleRepository.save(r);
                });

        emp1 = new Employee();
        emp1.setOrganization(org1);
        emp1.setFirstName("Alice");
        emp1.setLastName("Applicant");
        emp1.setEmail("alice.app." + ts + "@test.com");
        emp1.setDepartment("Engineering");
        emp1.setDesignation("Software Engineer");
        emp1.setJoiningDate(LocalDate.now().minusMonths(12));
        emp1 = employeeRepository.save(emp1);

        emp2 = new Employee();
        emp2.setOrganization(org1);
        emp2.setFirstName("Bob");
        emp2.setLastName("Reviewer");
        emp2.setEmail("bob.rev." + ts + "@test.com");
        emp2.setDepartment("Engineering");
        emp2.setDesignation("Team Lead");
        emp2.setJoiningDate(LocalDate.now().minusMonths(24));
        emp2 = employeeRepository.save(emp2);

        Employee hrEmp = new Employee();
        hrEmp.setOrganization(org1);
        hrEmp.setFirstName("Helen");
        hrEmp.setLastName("HR");
        hrEmp.setEmail("hr.val." + ts + "@test.com");
        hrEmp.setDepartment("Human Resources");
        hrEmp.setDesignation("HR Manager");
        hrEmp.setJoiningDate(LocalDate.now().minusMonths(36));
        hrEmp = employeeRepository.save(hrEmp);

        hrUser = new User();
        hrUser.setWorkEmail(hrEmp.getEmail());
        hrUser.setRole(hrRole);
        hrUser.setOrganization(org1);
        hrUser = userRepository.save(hrUser);

        emp1User = new User();
        emp1User.setWorkEmail(emp1.getEmail());
        emp1User.setRole(empRole);
        emp1User.setOrganization(org1);
        emp1User = userRepository.save(emp1User);

        User emp2User = new User();
        emp2User.setWorkEmail(emp2.getEmail());
        emp2User.setRole(hrRole);
        emp2User.setOrganization(org1);
        userRepository.save(emp2User);

        empOtherOrg = new Employee();
        empOtherOrg.setOrganization(org2);
        empOtherOrg.setFirstName("Carol");
        empOtherOrg.setEmail("carol.other." + ts + "@test.com");
        empOtherOrg.setJoiningDate(LocalDate.now().minusMonths(12));
        empOtherOrg = employeeRepository.save(empOtherOrg);

        empOtherOrgUser = new User();
        empOtherOrgUser.setWorkEmail(empOtherOrg.getEmail());
        empOtherOrgUser.setRole(empRole);
        empOtherOrgUser.setOrganization(org2);
        empOtherOrgUser = userRepository.save(empOtherOrgUser);

        validReason = new AppraisalRequestReason();
        validReason.setOrganization(org1);
        validReason.setCode("ANNUAL_REQ");
        validReason.setName("Annual Request");
        validReason.setActive(true);
        validReason.setCreatedAt(LocalDateTime.now());
        validReason = reasonRepository.save(validReason);

        hrToken = jwtService.generateAccessToken(String.valueOf(hrUser.getId()), hrUser.getWorkEmail(), "PLATFORM_ADMIN");
        emp1Token = jwtService.generateAccessToken(String.valueOf(emp1User.getId()), emp1User.getWorkEmail(), "EMPLOYEE");
        otherOrgToken = jwtService.generateAccessToken(String.valueOf(empOtherOrgUser.getId()), empOtherOrgUser.getWorkEmail(), "EMPLOYEE");
    }

    @AfterEach
    public void cleanup() {
        TenantContext.clear();
    }

    // ==========================================
    // 1. CONFIGURATION VALIDATION
    // ==========================================

    @Test
    @DisplayName("VAL-CFG-01: Should reject null initiationMode with 400")
    public void shouldRejectNullInitiationMode() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        String payload = "{\"initiationMode\": null, \"minServiceMonths\": 6}";

        configMockMvc.perform(put("/api/v1/appraisals/configuration")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("VAL-CFG-02: Should reject invalid initiationMode string with 400")
    public void shouldRejectInvalidInitiationMode() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        String payload = "{\"initiationMode\": \"INVALID_MODE\", \"minServiceMonths\": 6}";

        configMockMvc.perform(put("/api/v1/appraisals/configuration")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("VAL-CFG-03: Should reject empty review stages list with 400")
    public void shouldRejectEmptyReviewStagesList() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        SaveAppraisalConfigurationRequest req = new SaveAppraisalConfigurationRequest();
        req.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        req.setReviewStages(List.of());

        configMockMvc.perform(put("/api/v1/appraisals/configuration")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("VAL-CFG-04: Should reject duplicate review stage order with 400")
    public void shouldRejectDuplicateReviewStageOrder() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        SaveAppraisalConfigurationRequest req = new SaveAppraisalConfigurationRequest();
        req.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        req.setReviewStages(List.of(
                new ReviewStageConfigurationDto(1, "Lead Review", "APPRAISAL_REVIEW", true),
                new ReviewStageConfigurationDto(1, "Manager Review", "APPRAISAL_REVIEW", true)
        ));

        configMockMvc.perform(put("/api/v1/appraisals/configuration")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("VAL-CFG-05 & VAL-CFG-06: Should reject stageOrder <= 0 with 400")
    public void shouldRejectZeroOrNegativeStageOrder() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        SaveAppraisalConfigurationRequest req = new SaveAppraisalConfigurationRequest();
        req.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        req.setReviewStages(List.of(
                new ReviewStageConfigurationDto(0, "Invalid Stage", "APPRAISAL_REVIEW", true)
        ));

        configMockMvc.perform(put("/api/v1/appraisals/configuration")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("VAL-CFG-09: Should reject negative minServiceMonths with 400")
    public void shouldRejectNegativeMinServiceMonths() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        SaveAppraisalConfigurationRequest req = new SaveAppraisalConfigurationRequest();
        req.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        req.setMinServiceMonths(-6);

        configMockMvc.perform(put("/api/v1/appraisals/configuration")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("VAL-CFG-10: Should reject negative minGapMonths with 400")
    public void shouldRejectNegativeMinGapMonths() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        SaveAppraisalConfigurationRequest req = new SaveAppraisalConfigurationRequest();
        req.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        req.setMinGapMonths(-3);

        configMockMvc.perform(put("/api/v1/appraisals/configuration")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("VAL-CFG-13: Should reject negative review stage weightage with 400")
    public void shouldRejectNegativeReviewStageWeightage() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        SaveAppraisalConfigurationRequest req = new SaveAppraisalConfigurationRequest();
        req.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        ReviewStageConfigurationDto stage = new ReviewStageConfigurationDto(1, "Lead Review", "APPRAISAL_REVIEW", true);
        stage.setWeightage(-1.5);
        req.setReviewStages(List.of(stage));

        configMockMvc.perform(put("/api/v1/appraisals/configuration")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // 2. CYCLE VALIDATION
    // ==========================================

    @Test
    @DisplayName("VAL-CYC-01 & VAL-CYC-02: Should reject null or blank cycle name with 400")
    public void shouldRejectBlankCycleName() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        CreateAppraisalCycleDto dto = new CreateAppraisalCycleDto();
        dto.setName("   ");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusMonths(3));

        cycleMockMvc.perform(post("/api/v1/appraisals/cycles")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("VAL-CYC-05 & VAL-CYC-06: Should reject null cycle dates with 400")
    public void shouldRejectNullCycleDates() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        CreateAppraisalCycleDto dto = new CreateAppraisalCycleDto();
        dto.setName("Annual Cycle 2026");
        dto.setStartDate(null);
        dto.setEndDate(LocalDate.now().plusMonths(3));

        cycleMockMvc.perform(post("/api/v1/appraisals/cycles")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("VAL-CYC-07: Should reject endDate before startDate with 400")
    public void shouldRejectEndDateBeforeStartDate() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        CreateAppraisalCycleDto dto = new CreateAppraisalCycleDto();
        dto.setName("Annual Cycle 2026");
        dto.setStartDate(LocalDate.of(2026, 12, 31));
        dto.setEndDate(LocalDate.of(2026, 1, 1));

        cycleMockMvc.perform(post("/api/v1/appraisals/cycles")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // 3. EMPLOYEE REQUEST VALIDATION
    // ==========================================

    @Test
    @DisplayName("VAL-REQ-05: Should reject duplicate active employee request")
    public void shouldRejectDuplicateActiveEmployeeRequest() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        // First request created
        CreateEmployeeAppraisalRequestDto dto = new CreateEmployeeAppraisalRequestDto();
        dto.setReasonId(validReason.getId());
        dto.setJustification("First legitimate request");

        requestMockMvc.perform(post("/api/v1/appraisal/requests")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Second request attempted while first is active -> should fail (409 Conflict)
        requestMockMvc.perform(post("/api/v1/appraisal/requests")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("VAL-REQ-07: Should reject employee request when service tenure is not satisfied")
    public void shouldRejectRequestWhenTenureNotSatisfied() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        // Set configuration to require 24 months
        AppraisalConfiguration config = new AppraisalConfiguration();
        config.setOrganization(org1);
        config.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        config.setEmployeeRequestEnabled(true);
        config.setMinServiceMonths(24);
        config.setActive(true);
        configRepository.save(config);

        // emp1 has only 12 months service
        CreateEmployeeAppraisalRequestDto dto = new CreateEmployeeAppraisalRequestDto();
        dto.setReasonId(validReason.getId());
        dto.setJustification("Request before completing tenure");

        requestMockMvc.perform(post("/api/v1/appraisal/requests")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("VAL-REQ-08: Should reject employee request when initiationMode is HR_ONLY")
    public void shouldRejectRequestWhenHrOnly() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        AppraisalConfiguration config = new AppraisalConfiguration();
        config.setOrganization(org1);
        config.setInitiationMode(AppraisalInitiationMode.HR_ONLY);
        config.setEmployeeRequestEnabled(true);
        config.setActive(true);
        configRepository.save(config);

        CreateEmployeeAppraisalRequestDto dto = new CreateEmployeeAppraisalRequestDto();
        dto.setReasonId(validReason.getId());
        dto.setJustification("Trying to request under HR_ONLY mode");

        requestMockMvc.perform(post("/api/v1/appraisal/requests")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // ==========================================
    // 4. SELF-ASSESSMENT & BOUNDARY VALIDATION
    // ==========================================

    @Test
    @DisplayName("VAL-SA-01 to VAL-SA-05: Should reject ratings out of 1.0 - 5.0 range")
    public void shouldRejectSelfAssessmentRatingsOutOfRange() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        // Create an appraisal for emp1
        Appraisal appraisal = new Appraisal();
        appraisal.setOrganization(org1);
        appraisal.setEmployee(emp1);
        appraisal.setStatus(AppraisalStatus.CREATED);
        appraisal = appraisalRepository.save(appraisal);

        // Rating = 0.5 (below 1.0) -> 400
        SelfAssessmentDto lowRating = new SelfAssessmentDto(0.5, "Good", "Goals", "Tech", null);
        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/self-assessment")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lowRating)))
                .andExpect(status().isBadRequest());

        // Rating = 5.5 (above 5.0) -> 400
        SelfAssessmentDto highRating = new SelfAssessmentDto(5.5, "Good", "Goals", "Tech", null);
        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/self-assessment")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(highRating)))
                .andExpect(status().isBadRequest());

        // Boundary rating = 1.0 -> 200 OK
        SelfAssessmentDto minBoundary = new SelfAssessmentDto(1.0, "Good", "Goals", "Tech", null);
        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/self-assessment")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minBoundary)))
                .andExpect(status().isOk());

        // Boundary rating = 5.0 -> 200 OK
        SelfAssessmentDto maxBoundary = new SelfAssessmentDto(5.0, "Good", "Goals", "Tech", null);
        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/self-assessment")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maxBoundary)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("VAL-SA-10: Should reject self-assessment submit after already submitted")
    public void shouldRejectDuplicateSelfAssessmentSubmit() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        Appraisal appraisal = new Appraisal();
        appraisal.setOrganization(org1);
        appraisal.setEmployee(emp1);
        appraisal.setStatus(AppraisalStatus.CREATED);
        appraisal = appraisalRepository.save(appraisal);

        SelfAssessmentDto dto = new SelfAssessmentDto(4.5, "Strengths", "Achievements", "Areas", null);

        // First submit -> 200 OK
        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/self-assessment/submit")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // Second submit -> 409 Conflict
        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/self-assessment/submit")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // ==========================================
    // 5. REVIEW VALIDATION
    // ==========================================

    @Test
    @DisplayName("VAL-REV-10: Should reject appraisee reviewing themselves")
    public void shouldRejectAppraiseeReviewingThemselves() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        Appraisal appraisal = new Appraisal();
        appraisal.setOrganization(org1);
        appraisal.setEmployee(emp1);
        appraisal.setStatus(AppraisalStatus.STAGE_REVIEW);
        appraisal.setCurrentStageOrder(1);
        appraisal = appraisalRepository.save(appraisal);

        ReviewStageDto reviewDto = new ReviewStageDto();
        reviewDto.setStageOrder(1);
        reviewDto.setRating(4.5);
        reviewDto.setComments("Trying to review self");

        // emp1 attempts to review emp1 appraisal
        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/review")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("VAL-REV-12: Should reject review submission before self-assessment is submitted")
    public void shouldRejectReviewBeforeSelfAssessmentSubmitted() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        // Appraisal in CREATED status (self assessment not done)
        Appraisal appraisal = new Appraisal();
        appraisal.setOrganization(org1);
        appraisal.setEmployee(emp1);
        appraisal.setStatus(AppraisalStatus.CREATED);
        appraisal = appraisalRepository.save(appraisal);

        ReviewStageDto reviewDto = new ReviewStageDto();
        reviewDto.setStageOrder(1);
        reviewDto.setRating(4.0);

        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/review")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("VAL-REV-05: Should reject out-of-order review stage submission")
    public void shouldRejectOutOfOrderReviewStage() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        Appraisal appraisal = new Appraisal();
        appraisal.setOrganization(org1);
        appraisal.setEmployee(emp1);
        appraisal.setStatus(AppraisalStatus.STAGE_REVIEW);
        appraisal.setCurrentStageOrder(1);
        appraisal = appraisalRepository.save(appraisal);

        // Attempting Stage 3 while current stage is 1
        ReviewStageDto reviewDto = new ReviewStageDto();
        reviewDto.setStageOrder(3);
        reviewDto.setRating(4.0);

        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/review")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isConflict());
    }

    // ==========================================
    // 6. PUBLISH VALIDATION
    // ==========================================

    @Test
    @DisplayName("VAL-PUB-02 & VAL-PUB-04: Should reject publish when not COMPLETED and when already PUBLISHED")
    public void shouldRejectPublishInInvalidStates() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        Appraisal appraisal = new Appraisal();
        appraisal.setOrganization(org1);
        appraisal.setEmployee(emp1);
        appraisal.setStatus(AppraisalStatus.STAGE_REVIEW);
        appraisal = appraisalRepository.save(appraisal);

        // 1. Publish while in STAGE_REVIEW -> 409 Conflict
        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/publish")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isConflict());

        // 2. Transition to COMPLETED
        appraisal.setStatus(AppraisalStatus.COMPLETED);
        appraisal.setFinalRating(4.75);
        appraisal = appraisalRepository.save(appraisal);

        // 3. Publish -> 200 OK
        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/publish")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.performanceCategory").value("OUTSTANDING"));

        // 4. Publish again -> 409 Conflict
        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/publish")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isConflict());
    }

    // ==========================================
    // 7. INITIATION MODE & BATCH GENERATION
    // ==========================================

    @Test
    @DisplayName("VAL-GEN-04: Should reject HR batch generation when EMPLOYEE_ONLY mode configured")
    public void shouldRejectHrGenerationWhenEmployeeOnly() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        AppraisalConfiguration config = new AppraisalConfiguration();
        config.setOrganization(org1);
        config.setInitiationMode(AppraisalInitiationMode.EMPLOYEE_ONLY);
        config.setActive(true);
        configRepository.save(config);

        AppraisalCycle cycle = new AppraisalCycle();
        cycle.setOrganization(org1);
        cycle.setName("Cycle 2026");
        cycle.setStartDate(LocalDate.now());
        cycle.setEndDate(LocalDate.now().plusMonths(3));
        cycle.setStatus("OPEN");
        cycle = cycleRepository.save(cycle);

        cycleMockMvc.perform(post("/api/v1/appraisals/cycles/" + cycle.getId() + "/generate")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("VAL-GEN-07: Should prevent duplicate appraisals during repeated batch generation")
    public void shouldPreventDuplicateAppraisalsDuringBatchGeneration() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        AppraisalConfiguration config = new AppraisalConfiguration();
        config.setOrganization(org1);
        config.setInitiationMode(AppraisalInitiationMode.HR_ONLY);
        config.setActive(true);
        configRepository.save(config);

        AppraisalCycle cycle = new AppraisalCycle();
        cycle.setOrganization(org1);
        cycle.setName("Cycle 2026 Batch");
        cycle.setStartDate(LocalDate.now());
        cycle.setEndDate(LocalDate.now().plusMonths(3));
        cycle.setStatus("OPEN");
        cycle = cycleRepository.save(cycle);

        // First generation run
        cycleMockMvc.perform(post("/api/v1/appraisals/cycles/" + cycle.getId() + "/generate")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generatedCount", greaterThanOrEqualTo(1)));

        long appraisalCountAfterFirstRun = appraisalRepository.findByCycleId(cycle.getId()).size();

        // Second generation run -> generatedCount must be 0, total count unchanged
        cycleMockMvc.perform(post("/api/v1/appraisals/cycles/" + cycle.getId() + "/generate")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generatedCount").value(0));

        long appraisalCountAfterSecondRun = appraisalRepository.findByCycleId(cycle.getId()).size();
        assertEquals(appraisalCountAfterFirstRun, appraisalCountAfterSecondRun, "Duplicate appraisal records must not be generated");
    }

    // ==========================================
    // 8. SECURITY & MULTI-TENANT ISOLATION
    // ==========================================

    @Test
    @DisplayName("SEC-01: Should reject unauthenticated requests with 401")
    public void shouldRejectUnauthenticatedRequests() throws Exception {
        evalMockMvc.perform(get("/api/v1/appraisals/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-02: Should reject requests with invalid token with 401")
    public void shouldRejectInvalidToken() throws Exception {
        evalMockMvc.perform(get("/api/v1/appraisals/my")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-03: Should reject cross-tenant access to another organization's appraisal with 400/404/403")
    public void shouldPreventCrossTenantAppraisalAccess() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        Appraisal appraisalOrg1 = new Appraisal();
        appraisalOrg1.setOrganization(org1);
        appraisalOrg1.setEmployee(emp1);
        appraisalOrg1.setStatus(AppraisalStatus.CREATED);
        appraisalOrg1 = appraisalRepository.save(appraisalOrg1);

        // Other organization employee attempts to access Org 1's appraisal
        TenantContext.setCurrentTenant(org2.getId());
        evalMockMvc.perform(get("/api/v1/appraisals/" + appraisalOrg1.getId())
                        .header("Authorization", "Bearer " + otherOrgToken))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // 9. ADDITIONAL BUSINESS-RULE BOUNDARY TESTS
    // ==========================================

    @Test
    @DisplayName("VAL-CFG-16: Should reject duplicate review stage names with 400")
    public void shouldRejectDuplicateStageName() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        SaveAppraisalConfigurationRequest req = new SaveAppraisalConfigurationRequest();
        req.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        req.setReviewStages(List.of(
                new ReviewStageConfigurationDto(1, "Manager Review", "APPRAISAL_REVIEW", true),
                new ReviewStageConfigurationDto(2, "Manager Review", "APPRAISAL_REVIEW", true)
        ));

        configMockMvc.perform(put("/api/v1/appraisals/configuration")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("VAL-CFG-17: Should reject blank review stage name with 400")
    public void shouldRejectBlankStageName() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        SaveAppraisalConfigurationRequest req = new SaveAppraisalConfigurationRequest();
        req.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        req.setReviewStages(List.of(
                new ReviewStageConfigurationDto(1, "   ", "APPRAISAL_REVIEW", true)
        ));

        configMockMvc.perform(put("/api/v1/appraisals/configuration")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("VAL-CYC-11: Should reject duplicate cycle name with 409")
    public void shouldRejectDuplicateCycleName() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());
        CreateAppraisalCycleDto dto1 = new CreateAppraisalCycleDto();
        dto1.setName("Q4 Performance Cycle 2026");
        dto1.setStartDate(LocalDate.now());
        dto1.setEndDate(LocalDate.now().plusMonths(3));

        cycleMockMvc.perform(post("/api/v1/appraisals/cycles")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        // Same cycle name again in same organization -> 409 Conflict
        cycleMockMvc.perform(post("/api/v1/appraisals/cycles")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("VAL-CYC-17: Should reject batch generation before cycle activation (when in DRAFT)")
    public void shouldRejectGenerationBeforeCycleActivation() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        AppraisalCycle cycle = new AppraisalCycle();
        cycle.setOrganization(org1);
        cycle.setName("Draft Cycle " + System.currentTimeMillis());
        cycle.setStartDate(LocalDate.now());
        cycle.setEndDate(LocalDate.now().plusMonths(3));
        cycle.setStatus("DRAFT");
        cycle = cycleRepository.save(cycle);

        cycleMockMvc.perform(post("/api/v1/appraisals/cycles/" + cycle.getId() + "/generate")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("VAL-REQ-18: Should reject employee request when active appraisal already exists")
    public void shouldRejectEmployeeRequestWhenAppraisalAlreadyExists() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        // Create existing appraisal for emp1
        Appraisal appraisal = new Appraisal();
        appraisal.setOrganization(org1);
        appraisal.setEmployee(emp1);
        appraisal.setStatus(AppraisalStatus.STAGE_REVIEW);
        appraisalRepository.save(appraisal);

        CreateEmployeeAppraisalRequestDto dto = new CreateEmployeeAppraisalRequestDto();
        dto.setReasonId(validReason.getId());
        dto.setJustification("Attempting request while active appraisal is in progress");

        requestMockMvc.perform(post("/api/v1/appraisal/requests")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("VAL-SA-14 & VAL-SA-15: Should reject self-assessment after COMPLETED or PUBLISHED")
    public void shouldRejectSelfAssessmentAfterCompletedOrPublished() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        Appraisal appraisal = new Appraisal();
        appraisal.setOrganization(org1);
        appraisal.setEmployee(emp1);
        appraisal.setStatus(AppraisalStatus.COMPLETED);
        appraisal = appraisalRepository.save(appraisal);

        SelfAssessmentDto dto = new SelfAssessmentDto(4.0, "S", "A", "D", null);

        // Save after completed -> 409 Conflict
        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/self-assessment")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());

        // Submit after completed -> 409 Conflict
        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/self-assessment/submit")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());

        appraisal.setStatus(AppraisalStatus.PUBLISHED);
        appraisal = appraisalRepository.save(appraisal);

        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/self-assessment")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("VAL-REV-21 & VAL-REV-22: Should reject review after COMPLETED or PUBLISHED")
    public void shouldRejectReviewAfterCompletedOrPublished() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        Appraisal appraisal = new Appraisal();
        appraisal.setOrganization(org1);
        appraisal.setEmployee(emp1);
        appraisal.setStatus(AppraisalStatus.COMPLETED);
        appraisal = appraisalRepository.save(appraisal);

        ReviewStageDto reviewDto = new ReviewStageDto();
        reviewDto.setStageOrder(1);
        reviewDto.setRating(4.5);

        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/review")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isConflict());

        appraisal.setStatus(AppraisalStatus.PUBLISHED);
        appraisal = appraisalRepository.save(appraisal);

        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/review")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("VAL-RAT-BOUNDARIES: Should accurately calculate rating categories at exact boundaries")
    public void shouldVerifyRatingCategoryBoundaries() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        // Test boundary ratings
        Appraisal a1 = new Appraisal();
        a1.setOrganization(org1);
        a1.setEmployee(emp1);
        a1.setStatus(AppraisalStatus.COMPLETED);
        a1.setFinalRating(4.50);
        a1 = appraisalRepository.save(a1);

        evalMockMvc.perform(post("/api/v1/appraisals/" + a1.getId() + "/publish")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.performanceCategory").value("OUTSTANDING"));

        Appraisal a2 = new Appraisal();
        a2.setOrganization(org1);
        a2.setEmployee(emp2);
        a2.setStatus(AppraisalStatus.COMPLETED);
        a2.setFinalRating(4.49);
        a2 = appraisalRepository.save(a2);

        evalMockMvc.perform(post("/api/v1/appraisals/" + a2.getId() + "/publish")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.performanceCategory").value("EXCEEDS_EXPECTATIONS"));

        Appraisal a3 = new Appraisal();
        a3.setOrganization(org1);
        a3.setEmployee(emp1);
        a3.setStatus(AppraisalStatus.COMPLETED);
        a3.setFinalRating(3.80);
        a3 = appraisalRepository.save(a3);

        evalMockMvc.perform(post("/api/v1/appraisals/" + a3.getId() + "/publish")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.performanceCategory").value("EXCEEDS_EXPECTATIONS"));

        Appraisal a4 = new Appraisal();
        a4.setOrganization(org1);
        a4.setEmployee(emp2);
        a4.setStatus(AppraisalStatus.COMPLETED);
        a4.setFinalRating(3.79);
        a4 = appraisalRepository.save(a4);

        evalMockMvc.perform(post("/api/v1/appraisals/" + a4.getId() + "/publish")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.performanceCategory").value("MEETS_EXPECTATIONS"));
    }

    @Test
    @DisplayName("VAL-DEDUP-REVERSE: Should reject employee request after HR has already initiated appraisal for cycle")
    public void shouldRejectEmployeeRequestAfterHrGeneratedAppraisal() throws Exception {
        TenantContext.setCurrentTenant(org1.getId());

        AppraisalConfiguration config = new AppraisalConfiguration();
        config.setOrganization(org1);
        config.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        config.setActive(true);
        configRepository.save(config);

        AppraisalCycle cycle = new AppraisalCycle();
        cycle.setOrganization(org1);
        cycle.setName("Cycle Reverse Dedup " + System.currentTimeMillis());
        cycle.setStartDate(LocalDate.now());
        cycle.setEndDate(LocalDate.now().plusMonths(3));
        cycle.setStatus("OPEN");
        cycle = cycleRepository.save(cycle);

        // HR generates batch
        cycleMockMvc.perform(post("/api/v1/appraisals/cycles/" + cycle.getId() + "/generate")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk());

        // Employee now attempts to request appraisal for same cycle
        CreateEmployeeAppraisalRequestDto dto = new CreateEmployeeAppraisalRequestDto();
        dto.setReasonId(validReason.getId());
        dto.setJustification("Attempting duplicate employee request after HR generation");

        requestMockMvc.perform(post("/api/v1/appraisal/requests")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }
}
