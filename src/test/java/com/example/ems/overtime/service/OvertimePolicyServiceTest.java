package com.example.ems.overtime.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.overtime.dto.OvertimePolicyRequest;
import com.example.ems.overtime.dto.OvertimePolicyResponse;
import com.example.ems.overtime.entity.OvertimeAmountBasis;
import com.example.ems.overtime.entity.OvertimePolicy;
import com.example.ems.overtime.entity.OvertimePolicyStatus;
import com.example.ems.overtime.repository.OvertimePolicyRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OvertimePolicyServiceTest {

    @Mock
    private OvertimePolicyRepository policyRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OvertimePolicyService policyService;

    private Organization organization;
    private OvertimePolicyRequest validRequest;
    private final Long orgId = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);

        organization = new Organization();
        organization.setId(orgId);

        validRequest = new OvertimePolicyRequest();
        validRequest.setName("Standard Policy");
        validRequest.setDescription("Standard Org OT Policy");
        validRequest.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        validRequest.setNormalWorkingHours(8);
        validRequest.setMinimumOtMinutes(30);
        validRequest.setMaximumOtMinutes(240);
        validRequest.setAmountBasis(OvertimeAmountBasis.BASIC_SALARY);
        validRequest.setNormalDayMultiplier(BigDecimal.valueOf(1.50));
        validRequest.setWeekendMultiplier(BigDecimal.valueOf(2.00));
        validRequest.setHolidayMultiplier(BigDecimal.valueOf(2.00));
        validRequest.setApprovalRequired(true);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testCreatePolicy_Success() {
        when(policyRepository.existsByOrganizationIdAndName(orgId, "Standard Policy")).thenReturn(false);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(policyRepository.save(any(OvertimePolicy.class))).thenAnswer(inv -> {
            OvertimePolicy p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        OvertimePolicyResponse res = policyService.createPolicy(validRequest);

        assertNotNull(res);
        assertEquals(10L, res.getId());
        assertEquals("Standard Policy", res.getName());
        assertEquals(OvertimePolicyStatus.DRAFT, res.getStatus());
        verify(policyRepository, times(1)).save(any(OvertimePolicy.class));
    }

    @Test
    void testCreatePolicy_DuplicateNameThrowsConflict() {
        when(policyRepository.existsByOrganizationIdAndName(orgId, "Standard Policy")).thenReturn(true);

        assertThrows(ConflictException.class, () -> policyService.createPolicy(validRequest));
        verify(policyRepository, never()).save(any());
    }

    @Test
    void testPolicyLifecycle_ActivateDeactivateArchive() {
        OvertimePolicy policy = new OvertimePolicy();
        policy.setId(10L);
        policy.setStatus(OvertimePolicyStatus.DRAFT);

        when(policyRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(OvertimePolicy.class))).thenAnswer(inv -> inv.getArgument(0));

        // Activate
        OvertimePolicyResponse activated = policyService.activatePolicy(10L);
        assertEquals(OvertimePolicyStatus.ACTIVE, activated.getStatus());

        // Deactivate
        OvertimePolicyResponse deactivated = policyService.deactivatePolicy(10L);
        assertEquals(OvertimePolicyStatus.INACTIVE, deactivated.getStatus());

        // Archive
        OvertimePolicyResponse archived = policyService.archivePolicy(10L);
        assertEquals(OvertimePolicyStatus.ARCHIVED, archived.getStatus());

        // Attempting to update or activate ARCHIVED policy throws BadRequest
        assertThrows(BadRequestException.class, () -> policyService.activatePolicy(10L));
        assertThrows(BadRequestException.class, () -> policyService.updatePolicy(10L, validRequest));
    }

    @Test
    void testEffectiveDateValidation_ThrowsBadRequest() {
        validRequest.setEffectiveTo(LocalDate.of(2025, 12, 31)); // before effectiveFrom 2026-01-01
        assertThrows(BadRequestException.class, () -> policyService.createPolicy(validRequest));
    }

    @Test
    void testFixedHourlyRateValidation_ThrowsBadRequestWhenMissing() {
        validRequest.setAmountBasis(OvertimeAmountBasis.FIXED_HOURLY_RATE);
        validRequest.setFixedHourlyRate(null);
        assertThrows(BadRequestException.class, () -> policyService.createPolicy(validRequest));
    }

    @Test
    void testApplicabilityMatching_PicksMostSpecificMatch() {
        LocalDate workDate = LocalDate.of(2026, 9, 10);

        Employee engineeringEmp = new Employee();
        engineeringEmp.setId(50L);
        engineeringEmp.setDepartment("ENGINEERING");
        engineeringEmp.setDesignation("DEVELOPER");
        engineeringEmp.setEmploymentType("FULL_TIME");
        engineeringEmp.setLocation("BANGALORE");

        // Policy 1: Org Fallback (score = 0)
        OvertimePolicy orgFallback = new OvertimePolicy();
        orgFallback.setId(1L);
        orgFallback.setName("Org Fallback Policy");
        orgFallback.setStatus(OvertimePolicyStatus.ACTIVE);

        // Policy 2: Dept Match (score = 8)
        OvertimePolicy deptPolicy = new OvertimePolicy();
        deptPolicy.setId(2L);
        deptPolicy.setName("Engineering Policy");
        deptPolicy.setDepartmentId("ENGINEERING");
        deptPolicy.setStatus(OvertimePolicyStatus.ACTIVE);

        // Policy 3: Dept + Desig Match (score = 8 + 4 = 12)
        OvertimePolicy deptDesigPolicy = new OvertimePolicy();
        deptDesigPolicy.setId(3L);
        deptDesigPolicy.setName("Engineering Dev Policy");
        deptDesigPolicy.setDepartmentId("ENGINEERING");
        deptDesigPolicy.setDesignationId("DEVELOPER");
        deptDesigPolicy.setStatus(OvertimePolicyStatus.ACTIVE);

        when(policyRepository.findActivePoliciesForDate(orgId, workDate))
                .thenReturn(List.of(orgFallback, deptPolicy, deptDesigPolicy));

        OvertimePolicy resolved = policyService.resolveApplicablePolicy(orgId, engineeringEmp, workDate);

        assertNotNull(resolved);
        assertEquals(3L, resolved.getId());
        assertEquals("Engineering Dev Policy", resolved.getName());
    }

    @Test
    void testApplicabilityMatching_FallsBackToOrgPolicyWhenNoSpecificMatch() {
        LocalDate workDate = LocalDate.of(2026, 9, 10);

        Employee salesEmp = new Employee();
        salesEmp.setId(60L);
        salesEmp.setDepartment("SALES");

        OvertimePolicy orgFallback = new OvertimePolicy();
        orgFallback.setId(1L);
        orgFallback.setName("Org Fallback Policy");
        orgFallback.setStatus(OvertimePolicyStatus.ACTIVE);

        OvertimePolicy engineeringPolicy = new OvertimePolicy();
        engineeringPolicy.setId(2L);
        engineeringPolicy.setName("Engineering Policy");
        engineeringPolicy.setDepartmentId("ENGINEERING");
        engineeringPolicy.setStatus(OvertimePolicyStatus.ACTIVE);

        when(policyRepository.findActivePoliciesForDate(orgId, workDate))
                .thenReturn(List.of(orgFallback, engineeringPolicy));

        OvertimePolicy resolved = policyService.resolveApplicablePolicy(orgId, salesEmp, workDate);

        assertNotNull(resolved);
        assertEquals(1L, resolved.getId());
        assertEquals("Org Fallback Policy", resolved.getName());
    }
}
