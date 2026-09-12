package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.policy.AttendancePolicyDto;
import com.example.ems.attendance.dto.policy.CreateAttendancePolicyRequest;
import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.entity.AttendancePolicyStatus;
import com.example.ems.attendance.exception.AttendanceNotFoundException;
import com.example.ems.attendance.repository.AttendancePolicyRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendancePolicyServiceTest {

    @Mock
    private AttendancePolicyRepository policyRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private AttendancePolicyService policyService;

    private Organization organization;
    private AttendancePolicy samplePolicy;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        TenantContext.setCurrentTenant(100L);

        organization = new Organization();
        organization.setId(100L);
        organization.setName("Acme Tech");

        samplePolicy = new AttendancePolicy();
        samplePolicy.setId(10L);
        samplePolicy.setOrganization(organization);
        samplePolicy.setName("Corporate Policy");
        samplePolicy.setOfficeStartTime(LocalTime.of(9, 30));
        samplePolicy.setOfficeEndTime(LocalTime.of(18, 30));
        samplePolicy.setGracePeriodMinutes(15);
        samplePolicy.setMinimumWorkingMinutes(480);
        samplePolicy.setHalfDayThreshold(240);
        samplePolicy.setStatus(AttendancePolicyStatus.DRAFT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Create Policy: Success with valid inputs")
    void testCreatePolicy_Success() {
        CreateAttendancePolicyRequest request = new CreateAttendancePolicyRequest();
        request.setName("Shift A");
        request.setOfficeStartTime(LocalTime.of(9, 0));
        request.setOfficeEndTime(LocalTime.of(18, 0));
        request.setGracePeriodMinutes(15);
        request.setMinimumWorkingMinutes(480);
        request.setHalfDayThreshold(240);
        request.setLateThreshold(15);
        request.setEarlyCheckoutThreshold(15);
        request.setMaximumBreakMinutes(60);

        when(organizationRepository.findById(100L)).thenReturn(Optional.of(organization));
        when(policyRepository.save(any(AttendancePolicy.class))).thenAnswer(i -> {
            AttendancePolicy p = i.getArgument(0);
            p.setId(101L);
            return p;
        });

        AttendancePolicyDto result = policyService.createPolicy(request);

        assertNotNull(result);
        assertEquals(101L, result.getId());
        assertEquals("Shift A", result.getName());
        assertEquals(AttendancePolicyStatus.DRAFT, result.getStatus());
        verify(policyRepository, times(1)).save(any(AttendancePolicy.class));
    }

    @Test
    @DisplayName("Create Policy: Throws exception when work end time is before start time")
    void testCreatePolicy_InvalidTimes_ThrowsException() {
        CreateAttendancePolicyRequest request = new CreateAttendancePolicyRequest();
        request.setName("Invalid Shift");
        request.setOfficeStartTime(LocalTime.of(18, 0));
        request.setOfficeEndTime(LocalTime.of(9, 0));

        when(organizationRepository.findById(100L)).thenReturn(Optional.of(organization));

        assertThrows(IllegalArgumentException.class, () -> policyService.createPolicy(request));
        verify(policyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Activate Policy: Deactivates active policy and sets target to ACTIVE")
    void testActivatePolicy_Success() {
        when(policyRepository.findByIdAndOrganizationId(10L, 100L)).thenReturn(Optional.of(samplePolicy));
        when(policyRepository.findActivePoliciesForOrganization(100L)).thenReturn(Collections.emptyList());
        when(policyRepository.save(any(AttendancePolicy.class))).thenAnswer(i -> i.getArgument(0));

        AttendancePolicyDto result = policyService.activatePolicy(10L);

        assertNotNull(result);
        assertEquals(AttendancePolicyStatus.ACTIVE, result.getStatus());
        verify(policyRepository, times(1)).save(samplePolicy);
    }

    @Test
    @DisplayName("Deactivate Policy: Successfully sets status to INACTIVE")
    void testDeactivatePolicy_Success() {
        samplePolicy.setStatus(AttendancePolicyStatus.ACTIVE);
        when(policyRepository.findByIdAndOrganizationId(10L, 100L)).thenReturn(Optional.of(samplePolicy));
        when(policyRepository.save(any(AttendancePolicy.class))).thenAnswer(i -> i.getArgument(0));

        AttendancePolicyDto result = policyService.deactivatePolicy(10L);

        assertNotNull(result);
        assertEquals(AttendancePolicyStatus.INACTIVE, result.getStatus());
        verify(policyRepository, times(1)).save(samplePolicy);
    }

    @Test
    @DisplayName("Get Active Policy: Returns tenant active policy when present")
    void testGetActivePolicy_TenantActiveExists() {
        samplePolicy.setStatus(AttendancePolicyStatus.ACTIVE);
        when(policyRepository.findActivePoliciesForOrganization(100L))
                .thenReturn(List.of(samplePolicy));

        AttendancePolicy result = policyService.getActivePolicy(100L);

        assertNotNull(result);
        assertEquals("Corporate Policy", result.getName());
        assertEquals(10L, result.getId());
    }

    @Test
    @DisplayName("Get Active Policy: Falls back to System Default (09:00 - 18:00) when none configured")
    void testGetActivePolicy_FallbackToSystemDefault() {
        when(policyRepository.findActivePoliciesForOrganization(100L))
                .thenReturn(Collections.emptyList());

        AttendancePolicy result = policyService.getActivePolicy(100L);

        assertNotNull(result);
        assertEquals("System Standard Policy", result.getName());
        assertEquals(LocalTime.of(9, 0), result.getOfficeStartTime());
        assertEquals(LocalTime.of(18, 0), result.getOfficeEndTime());
        assertEquals(15, result.getGracePeriodMinutes());
        assertEquals(AttendancePolicyStatus.ACTIVE, result.getStatus());
    }

    @Test
    @DisplayName("Get Policy By ID: Throws AttendanceNotFoundException if not found in tenant")
    void testGetPolicyById_NotFound_ThrowsException() {
        when(policyRepository.findByIdAndOrganizationId(999L, 100L)).thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> policyService.getPolicyById(999L));
    }
}
