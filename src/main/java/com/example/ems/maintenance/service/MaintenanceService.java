package com.example.ems.maintenance.service;

import com.example.ems.maintenance.dto.MaintenanceBlockedResponse;
import com.example.ems.maintenance.dto.MaintenanceEnableRequest;
import com.example.ems.maintenance.dto.MaintenanceResponse;
import com.example.ems.maintenance.dto.MaintenanceUpdateRequest;
import com.example.ems.maintenance.dto.PublicMaintenanceResponse;
import com.example.ems.maintenance.entity.MaintenanceConfig;
import org.springframework.security.core.Authentication;

public interface MaintenanceService {

    MaintenanceResponse getSettings();

    MaintenanceResponse updateSettings(MaintenanceUpdateRequest request);

    MaintenanceResponse enable(MaintenanceEnableRequest request);

    MaintenanceResponse disable();

    boolean isMaintenanceActive();

    boolean canAccessDuringMaintenance(Authentication authentication);

    PublicMaintenanceResponse getPublicStatus();

    MaintenanceConfig getCurrentConfig();

    MaintenanceBlockedResponse getBlockedResponse();
}
