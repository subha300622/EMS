package com.example.ems.maintenance.controller;

import com.example.ems.maintenance.dto.PublicMaintenanceResponse;
import com.example.ems.maintenance.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/maintenance")
@Tag(name = "Public Maintenance", description = "Public endpoint to query platform maintenance status")
public class PublicMaintenanceController {

    private final MaintenanceService maintenanceService;

    public PublicMaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    @Operation(summary = "Get public maintenance status for frontend banner and screen redirect")
    public ResponseEntity<PublicMaintenanceResponse> getPublicStatus() {
        return ResponseEntity.ok(maintenanceService.getPublicStatus());
    }
}
