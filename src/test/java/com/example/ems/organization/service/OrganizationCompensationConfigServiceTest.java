package com.example.ems.organization.service;

import com.example.ems.common.exception.ModuleDisabledException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.organization.dto.CompensationConfigRequest;
import com.example.ems.organization.dto.CompensationConfigResponse;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationCompensationConfig;
import com.example.ems.organization.repository.OrganizationCompensationConfigRepository;
import com.example.ems.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationCompensationConfigServiceTest {

    @Mock
    private OrganizationCompensationConfigRepository configRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationCompensationConfigService configService;

    private Organization organization;
    private OrganizationCompensationConfig config;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setId(10L);
        organization.setName("Acme Corp");

        config = new OrganizationCompensationConfig(organization);
        config.setId(1L);
        config.setOvertimeEnabled(true);
        config.setIncentiveEnabled(false);
        config.setBonusEnabled(true);
    }

    @Test
    @DisplayName("getConfig returns configured flags when present")
    void testGetConfig_WhenPresent() {
        when(configRepository.findByOrganizationId(10L)).thenReturn(Optional.of(config));

        CompensationConfigResponse response = configService.getConfig(10L);

        assertThat(response).isNotNull();
        assertThat(response.getOrganizationId()).isEqualTo(10L);
        assertThat(response.isOvertimeEnabled()).isTrue();
        assertThat(response.isIncentiveEnabled()).isFalse();
        assertThat(response.isBonusEnabled()).isTrue();
    }

    @Test
    @DisplayName("getConfig returns defaults (all true) when config row is missing")
    void testGetConfig_WhenMissing_ReturnsDefaults() {
        when(configRepository.findByOrganizationId(10L)).thenReturn(Optional.empty());

        CompensationConfigResponse response = configService.getConfig(10L);

        assertThat(response).isNotNull();
        assertThat(response.getOrganizationId()).isEqualTo(10L);
        assertThat(response.isOvertimeEnabled()).isTrue();
        assertThat(response.isIncentiveEnabled()).isTrue();
        assertThat(response.isBonusEnabled()).isTrue();
    }

    @Test
    @DisplayName("updateConfig updates flags and persists")
    void testUpdateConfig_Success() {
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(configRepository.findByOrganizationId(10L)).thenReturn(Optional.of(config));
        when(configRepository.save(any(OrganizationCompensationConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompensationConfigRequest request = new CompensationConfigRequest(false, true, true);
        CompensationConfigResponse response = configService.updateConfig(10L, request);

        assertThat(response.isOvertimeEnabled()).isFalse();
        assertThat(response.isIncentiveEnabled()).isTrue();
        assertThat(response.isBonusEnabled()).isTrue();

        verify(configRepository).save(config);
    }

    @Test
    @DisplayName("updateConfig throws ResourceNotFoundException when organization does not exist")
    void testUpdateConfig_OrganizationNotFound() {
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());

        CompensationConfigRequest request = new CompensationConfigRequest(true, true, true);
        assertThatThrownBy(() -> configService.updateConfig(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Organization not found with ID: 99");
    }

    @Test
    @DisplayName("requireBonusEnabled throws ModuleDisabledException when bonus is disabled")
    void testRequireBonusEnabled_ThrowsWhenDisabled() {
        config.setBonusEnabled(false);
        when(configRepository.findByOrganizationId(10L)).thenReturn(Optional.of(config));

        assertThatThrownBy(() -> configService.requireBonusEnabled(10L))
                .isInstanceOf(ModuleDisabledException.class)
                .satisfies(e -> {
                    ModuleDisabledException mde = (ModuleDisabledException) e;
                    assertThat(mde.getModuleCode()).isEqualTo("BONUS_MODULE_DISABLED");
                    assertThat(mde.getMessage()).isEqualTo("Bonus module is not enabled for this organization");
                });
    }

    @Test
    @DisplayName("requireIncentiveEnabled throws ModuleDisabledException when incentive is disabled")
    void testRequireIncentiveEnabled_ThrowsWhenDisabled() {
        config.setIncentiveEnabled(false);
        when(configRepository.findByOrganizationId(10L)).thenReturn(Optional.of(config));

        assertThatThrownBy(() -> configService.requireIncentiveEnabled(10L))
                .isInstanceOf(ModuleDisabledException.class)
                .satisfies(e -> {
                    ModuleDisabledException mde = (ModuleDisabledException) e;
                    assertThat(mde.getModuleCode()).isEqualTo("INCENTIVE_MODULE_DISABLED");
                    assertThat(mde.getMessage()).isEqualTo("Incentive module is not enabled for this organization");
                });
    }

    @Test
    @DisplayName("requireOvertimeEnabled throws ModuleDisabledException when overtime is disabled")
    void testRequireOvertimeEnabled_ThrowsWhenDisabled() {
        config.setOvertimeEnabled(false);
        when(configRepository.findByOrganizationId(10L)).thenReturn(Optional.of(config));

        assertThatThrownBy(() -> configService.requireOvertimeEnabled(10L))
                .isInstanceOf(ModuleDisabledException.class)
                .satisfies(e -> {
                    ModuleDisabledException mde = (ModuleDisabledException) e;
                    assertThat(mde.getModuleCode()).isEqualTo("OVERTIME_MODULE_DISABLED");
                    assertThat(mde.getMessage()).isEqualTo("Overtime module is not enabled for this organization");
                });
    }
}
