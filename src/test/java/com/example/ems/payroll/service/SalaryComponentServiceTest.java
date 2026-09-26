package com.example.ems.payroll.service;

import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.payroll.dto.SalaryComponentCreateRequest;
import com.example.ems.payroll.dto.SalaryComponentResponse;
import com.example.ems.payroll.dto.SalaryComponentUpdateRequest;
import com.example.ems.payroll.entity.SalaryComponent;
import com.example.ems.payroll.entity.SalaryComponentType;
import com.example.ems.payroll.repository.SalaryComponentRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryComponentServiceTest {

    @Mock
    private SalaryComponentRepository salaryComponentRepository;

    @InjectMocks
    private SalaryComponentService salaryComponentService;

    private final Long orgId = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Create Component - Success with uppercase normalization and tenant scoping")
    void testCreateComponent_Success() {
        SalaryComponentCreateRequest request = new SalaryComponentCreateRequest(
                "Night Shift Allowance",
                "night_shift",
                "Allowance for employees working night shifts",
                SalaryComponentType.EARNING,
                true,
                true
        );

        when(salaryComponentRepository.existsByOrganizationIdAndCode(orgId, "NIGHT_SHIFT")).thenReturn(false);
        when(salaryComponentRepository.save(any(SalaryComponent.class))).thenAnswer(invocation -> {
            SalaryComponent saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        SalaryComponentResponse response = salaryComponentService.createComponent(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("NIGHT_SHIFT", response.getCode());
        assertEquals("Night Shift Allowance", response.getName());
        assertEquals(SalaryComponentType.EARNING, response.getComponentType());
        assertTrue(response.getActive());
        assertEquals(orgId, response.getOrganizationId());
        verify(salaryComponentRepository).save(any(SalaryComponent.class));
    }

    @Test
    @DisplayName("Create Component - Duplicate code within tenant throws ConflictException")
    void testCreateComponent_DuplicateCode_ThrowsConflictException() {
        SalaryComponentCreateRequest request = new SalaryComponentCreateRequest(
                "Housing Allowance",
                "HOUSING_ALLOWANCE",
                "Housing allowance",
                SalaryComponentType.EARNING,
                true,
                true
        );

        when(salaryComponentRepository.existsByOrganizationIdAndCode(orgId, "HOUSING_ALLOWANCE")).thenReturn(true);

        assertThrows(ConflictException.class, () -> salaryComponentService.createComponent(request));
        verify(salaryComponentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get Components - Filter by Type and Active status")
    void testGetComponents_WithFilters() {
        SalaryComponent c1 = new SalaryComponent(orgId, "Basic Pay", "BASIC_PAY", null, SalaryComponentType.EARNING, true, true);
        c1.setId(1L);

        when(salaryComponentRepository.findByOrganizationIdAndComponentTypeAndActive(orgId, SalaryComponentType.EARNING, true))
                .thenReturn(List.of(c1));

        List<SalaryComponentResponse> list = salaryComponentService.getComponents(SalaryComponentType.EARNING, true);

        assertEquals(1, list.size());
        assertEquals("BASIC_PAY", list.get(0).getCode());
    }

    @Test
    @DisplayName("Update Component - Update details successfully")
    void testUpdateComponent_Success() {
        SalaryComponent component = new SalaryComponent(orgId, "Old Name", "OLD_CODE", "Old desc", SalaryComponentType.DEDUCTION, false, true);
        component.setId(5L);

        when(salaryComponentRepository.findByIdAndOrganizationId(5L, orgId)).thenReturn(Optional.of(component));
        when(salaryComponentRepository.save(any(SalaryComponent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalaryComponentUpdateRequest updateRequest = new SalaryComponentUpdateRequest("New Name", "New Description", true, true);
        SalaryComponentResponse response = salaryComponentService.updateComponent(5L, updateRequest);

        assertEquals("New Name", response.getName());
        assertEquals("New Description", response.getDescription());
        assertTrue(response.getTaxable());
    }

    @Test
    @DisplayName("Deactivate Component - Soft delete sets active = false")
    void testDeactivateComponent_SoftDelete() {
        SalaryComponent component = new SalaryComponent(orgId, "Travel Allowance", "TRAVEL_ALLOWANCE", null, SalaryComponentType.EARNING, true, true);
        component.setId(7L);

        when(salaryComponentRepository.findByIdAndOrganizationId(7L, orgId)).thenReturn(Optional.of(component));
        when(salaryComponentRepository.save(any(SalaryComponent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        salaryComponentService.deactivateComponent(7L);

        assertFalse(component.getActive());
        verify(salaryComponentRepository).save(component);
    }

    @Test
    @DisplayName("Get Component By ID - Not found throws ResourceNotFoundException")
    void testGetComponentById_NotFound_ThrowsException() {
        when(salaryComponentRepository.findByIdAndOrganizationId(99L, orgId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> salaryComponentService.getComponentById(99L));
    }
}
