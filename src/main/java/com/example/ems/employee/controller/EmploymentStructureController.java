package com.example.ems.employee.controller;

import com.example.ems.employee.dto.EmploymentStructureDtos;
import com.example.ems.employee.service.EmploymentStructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employment-structures")
@CrossOrigin("*")
@Tag(name = "Employment Structures", description = "Designation and employment structure hierarchy APIs")
public class EmploymentStructureController {

    @Autowired
    private EmploymentStructureService employmentStructureService;

    // ── 1. Employment Structure List ──────────────────────────────────────────
    @Operation(summary = "List Employment Structures", description = "Retrieves summary list of designations with job level and employment type counts.")
    @GetMapping
    public ResponseEntity<EmploymentStructureDtos.EmploymentStructurePageResponse> listStructures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        EmploymentStructureDtos.EmploymentStructurePageResponse response = employmentStructureService.listStructures(page, size, search, status);
        return ResponseEntity.ok(response);
    }

    // ── 2. Create Employment Structure ────────────────────────────────────────
    @Operation(summary = "Create Employment Structure", description = "Creates a new designation with nested job levels and employment types.")
    @PostMapping
    public ResponseEntity<EmploymentStructureDtos.EmploymentStructureResponse> createStructure(
            @RequestBody @Valid EmploymentStructureDtos.CreateEmploymentStructureRequest request) {

        EmploymentStructureDtos.EmploymentStructureResponse response = employmentStructureService.createStructure(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── 3. Employment Structure Details ───────────────────────────────────────
    @Operation(summary = "Employment Structure Details", description = "Retrieves complete hierarchy for a designation.")
    @GetMapping("/{employmentStructureId}")
    public ResponseEntity<EmploymentStructureDtos.EmploymentStructureResponse> getStructure(
            @PathVariable("employmentStructureId") String employmentStructureId) {

        EmploymentStructureDtos.EmploymentStructureResponse response = employmentStructureService.getStructure(employmentStructureId);
        return ResponseEntity.ok(response);
    }

    // ── 4. Edit Employment Structure ──────────────────────────────────────────
    @Operation(summary = "Edit Employment Structure", description = "Updates designation and iteratively merges job levels and employment types.")
    @PutMapping("/{employmentStructureId}")
    public ResponseEntity<EmploymentStructureDtos.EmploymentStructureResponse> editStructure(
            @PathVariable("employmentStructureId") String employmentStructureId,
            @RequestBody @Valid EmploymentStructureDtos.EditEmploymentStructureRequest request) {

        EmploymentStructureDtos.EmploymentStructureResponse response = employmentStructureService.editStructure(employmentStructureId, request);
        return ResponseEntity.ok(response);
    }

    // ── 5. Activate / Deactivate Employment Structure ─────────────────────────
    @Operation(summary = "Update Status", description = "Activates or deactivates an employment structure.")
    @PatchMapping("/{employmentStructureId}/status")
    public ResponseEntity<EmploymentStructureDtos.EmploymentStructureResponse> updateStatus(
            @PathVariable("employmentStructureId") String employmentStructureId,
            @RequestBody @Valid EmploymentStructureDtos.StatusRequest request) {

        EmploymentStructureDtos.EmploymentStructureResponse response = employmentStructureService.updateStatus(employmentStructureId, request);
        return ResponseEntity.ok(response);
    }

    // ── 6. Delete Employment Structure ────────────────────────────────────────
    @Operation(summary = "Delete Employment Structure", description = "Deletes an unused employment structure.")
    @DeleteMapping("/{employmentStructureId}")
    public ResponseEntity<EmploymentStructureDtos.EmploymentStructureDeleteResponse> deleteStructure(
            @PathVariable("employmentStructureId") String employmentStructureId) {

        EmploymentStructureDtos.EmploymentStructureDeleteResponse response = employmentStructureService.deleteStructure(employmentStructureId);
        return ResponseEntity.ok(response);
    }
}
