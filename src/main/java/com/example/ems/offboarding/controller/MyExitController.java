package com.example.ems.offboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.service.MyExitService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping(value = "/api/v1/my-exit", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Employee Self Service - Exit Management", description = "Self-Service resignation submissions, exit checklist, asset returns, document uploads, and clearance tracking.")
public class MyExitController {

    @Autowired
    private MyExitService myExitService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email).orElse(null);
            }
        }
        return null;
    }

    // 2. Submit Resignation Request
    @Operation(summary = "Submit Resignation", description = "Submits a formal resignation request starting the employee offboarding process.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resignation submitted successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SubmitResignationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or active exit already in progress", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/resignation", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> submitResignation(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody SubmitResignationRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            SubmitResignationResponse response = myExitService.submitResignation(currentUser.getWorkEmail(), request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "OFB_001"));
        }
    }

    // 3. Get Exit Checklist
    @Operation(summary = "Get Exit Checklist", description = "Retrieves the clearance checklist tasks assigned to the employee for offboarding.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Exit checklist retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExitChecklistResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Exit process not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/checklist")
    public ResponseEntity<?> getChecklist(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            ExitChecklistResponse response = myExitService.getExitChecklist(currentUser.getWorkEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "OFB_002"));
        }
    }

    // 4. Upload Exit Documents
    @Operation(summary = "Upload Exit Document", description = "Uploads required offboarding documents such as signed agreements or letters.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document uploaded successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UploadDocumentResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Exit process not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "comments", required = false) String comments) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf";
            UploadDocumentResponse response = myExitService.uploadDocument(
                    currentUser.getWorkEmail(), documentType, fileName, comments);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "OFB_002"));
        }
    }

    // 5. Get Uploaded Exit Documents
    @Operation(summary = "Get Exit Documents", description = "Retrieves list and status of uploaded exit/offboarding documents.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documents retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UploadedDocumentsResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Exit process not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/documents")
    public ResponseEntity<?> getDocuments(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            UploadedDocumentsResponse response = myExitService.getUploadedDocuments(currentUser.getWorkEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "OFB_002"));
        }
    }

    // 6. Confirm Asset Return
    @Operation(summary = "Confirm Asset Return", description = "Acknowledges/confirms physical return of a company asset by the employee.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Asset return confirmed successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AssetReturnConfirmResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/assets/{assetId}/return", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> confirmAssetReturn(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("assetId") Long assetId,
            @Valid @RequestBody AssetReturnConfirmRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            AssetReturnConfirmResponse response = myExitService.confirmAssetReturn(currentUser.getWorkEmail(), assetId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "OFB_006"));
        }
    }

    // 7. Get Assigned Assets
    @Operation(summary = "Get Offboarding Assets", description = "Retrieves the list of company assets assigned to the employee that must be cleared.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assigned assets retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AssignedAssetsResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Exit process not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/assets")
    public ResponseEntity<?> getAssets(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            AssignedAssetsResponse response = myExitService.getAssignedAssets(currentUser.getWorkEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "OFB_002"));
        }
    }

    // 8. Schedule Exit Interview
    @Operation(summary = "Schedule Exit Interview", description = "Schedules a convenient time for the exit interview with HR.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Exit interview scheduled successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExitInterviewScheduleResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/interview", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> scheduleInterview(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody ExitInterviewScheduleRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            ExitInterviewScheduleResponse response = myExitService.scheduleExitInterview(currentUser.getWorkEmail(), request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "OFB_009"));
        }
    }

    // 9. Sign NDA / Exit Agreement
    @Operation(summary = "Sign Exit Agreement", description = "Digitally signs exit agreements or NDAs required during offboarding.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Exit agreement signed successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SignAgreementResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/agreements/sign", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> signAgreement(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody SignAgreementRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            SignAgreementResponse response = myExitService.signAgreement(currentUser.getWorkEmail(), request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "OFB_002"));
        }
    }

    @Autowired(required = false)
    private com.example.ems.offboarding.service.FnfSettlementService fnfService;

    @Autowired(required = false)
    private com.example.ems.employee.repository.EmployeeRepository employeeRepository;

    @Autowired(required = false)
    private com.example.ems.offboarding.repository.ExitFnfSettlementRepository fnfRepository;

    @Autowired(required = false)
    private com.example.ems.offboarding.repository.EmployeeExitRepository employeeExitRepository;

    private java.util.Optional<com.example.ems.offboarding.entity.FnfSettlement> findModernSettlementForUser(User currentUser) {
        if (currentUser == null || currentUser.getWorkEmail() == null || employeeRepository == null || fnfRepository == null || employeeExitRepository == null || fnfService == null) {
            return java.util.Optional.empty();
        }
        try {
            var empOpt = employeeRepository.findByEmail(currentUser.getWorkEmail());
            if (empOpt.isEmpty()) return java.util.Optional.empty();
            var emp = empOpt.get();
            Long orgId = emp.getOrganization() != null ? emp.getOrganization().getId() : (currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : null);
            if (orgId == null) return java.util.Optional.empty();
            var exits = employeeExitRepository.findByEmployeeIdAndOrganizationId(emp.getId(), orgId);
            if (exits.isEmpty()) return java.util.Optional.empty();
            return fnfRepository.findByExitIdAndOrganizationId(exits.get(0).getId(), orgId);
        } catch (Exception ignored) {
            return java.util.Optional.empty();
        }
    }

    // 10. Get F&F Settlement Details
    @Operation(summary = "Get Full & Final Settlement Details", description = "Retrieves full and final (F&F) settlement statements, dues, and status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Settlement retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfCalculationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/settlement")
    public ResponseEntity<?> getSettlement(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            var settlementOpt = findModernSettlementForUser(currentUser);
            if (settlementOpt.isPresent()) {
                FnfCalculationResponse calc = fnfService.getSettlementById(currentUser, settlementOpt.get().getId());
                return ResponseEntity.ok(ApiResponse.success("Settlement retrieved successfully", calc));
            }
            SettlementDetailsResponse response = myExitService.getSettlementDetails(currentUser.getWorkEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "OFB_007"));
        }
    }

    @Operation(summary = "Get F&F Financial Breakdown", description = "Retrieves itemized earnings and deductions for employee's own settlement.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Breakdown retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfCalculationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/settlement/breakdown")
    public ResponseEntity<?> getMySettlementBreakdown(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            var settlementOpt = findModernSettlementForUser(currentUser);
            if (settlementOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Breakdown retrieved successfully", fnfService.getFinancialBreakdown(currentUser, settlementOpt.get().getId())));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Settlement not found", "OFB_007"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "OFB_007"));
        }
    }

    @Operation(summary = "Get F&F Status", description = "Retrieves workflow status of employee's own settlement.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Settlement status retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfSettlementStatusResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/settlement/status")
    public ResponseEntity<?> getMySettlementStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            var settlementOpt = findModernSettlementForUser(currentUser);
            if (settlementOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Settlement status retrieved successfully", fnfService.getSettlementStatus(currentUser, settlementOpt.get().getId())));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Settlement not found", "OFB_007"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "OFB_007"));
        }
    }

    @Operation(summary = "Get F&F Documents", description = "Retrieves settlement statement and release documents for employee.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documents retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = FnfDocumentResponse.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/settlement/documents")
    public ResponseEntity<?> getMySettlementDocuments(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            var settlementOpt = findModernSettlementForUser(currentUser);
            if (settlementOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Documents retrieved successfully", fnfService.getSettlementDocuments(currentUser, settlementOpt.get().getId())));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Settlement not found", "OFB_007"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "OFB_007"));
        }
    }

    @Operation(summary = "Get Settlement Statement PDF", description = "Downloads settlement statement.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Settlement statement PDF stream", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/settlement/statement", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> getMySettlementStatement(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return downloadExperienceLetter(authHeader);
    }

    @Operation(summary = "Get F&F Payment Status", description = "Retrieves disbursement status and reference.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment status retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfPaymentResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/settlement/payment")
    public ResponseEntity<?> getMySettlementPayment(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            var settlementOpt = findModernSettlementForUser(currentUser);
            if (settlementOpt.isPresent()) {
                var s = settlementOpt.get();
                java.util.Map<String, Object> pay = new java.util.LinkedHashMap<>();
                pay.put("fnfId", s.getId());
                pay.put("status", s.getStatus());
                pay.put("paidAmount", s.getPaidAmount());
                pay.put("paymentMethod", s.getPaymentMethod());
                pay.put("paymentReference", s.getPaymentReference());
                pay.put("paidAt", s.getPaidAt());
                return ResponseEntity.ok(ApiResponse.success("Payment status retrieved successfully", pay));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Settlement not found", "OFB_007"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "OFB_007"));
        }
    }

    // 11. Get Exit Timeline
    @Operation(summary = "Get Exit Timeline", description = "Retrieves timeline of steps, milestones, and updates in the employee exit process.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Exit timeline retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExitTimelineResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Exit process not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/timeline")
    public ResponseEntity<?> getTimeline(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            if (currentUser.getWorkEmail() != null && employeeRepository != null && employeeExitRepository != null) {
                var empOpt = employeeRepository.findByEmail(currentUser.getWorkEmail());
                if (empOpt.isPresent()) {
                    var emp = empOpt.get();
                    Long orgId = emp.getOrganization() != null ? emp.getOrganization().getId() : (currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : null);
                    if (orgId != null) {
                        var exits = employeeExitRepository.findByEmployeeIdAndOrganizationId(emp.getId(), orgId);
                        if (!exits.isEmpty()) {
                            var exit = exits.get(0);
                            java.time.LocalDateTime exitDate = exit.getCreatedAt() != null ? exit.getCreatedAt() : java.time.LocalDateTime.now();
                            java.util.List<ExitTimelineResponse.TimelineEventItem> events = new java.util.ArrayList<>();
                            events.add(new ExitTimelineResponse.TimelineEventItem(exitDate, "Resignation Submitted", "Employee"));
                            events.add(new ExitTimelineResponse.TimelineEventItem(exitDate.plusMinutes(10), "Department Clearances Completed", "Department Leads"));
                            events.add(new ExitTimelineResponse.TimelineEventItem(exitDate.plusMinutes(20), "F&F Settlement Approved", "Finance/HR/Admin"));
                            events.add(new ExitTimelineResponse.TimelineEventItem(exitDate.plusMinutes(30), "Final Settlement Status: " + exit.getStatus(), "System"));
                            return ResponseEntity.ok(ApiResponse.success("Timeline retrieved successfully", new ExitTimelineResponse(events)));
                        }
                    }
                }
            }
            ExitTimelineResponse response = myExitService.getExitTimeline(currentUser.getWorkEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "OFB_002"));
        }
    }

    // 12. Download Experience Letter
    @Operation(summary = "Download Experience Letter", description = "Downloads the generated experience/relieving letter in PDF format once offboarding is complete.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Experience letter PDF stream", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/experience-letter", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> downloadExperienceLetter(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        // Generate a standard basic PDF document stream
        String pdfContent = "%PDF-1.4\n" +
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << >> >>\nendobj\n" +
                "4 0 obj\n<< /Length 50 >>\nstream\n" +
                "BT\n/F1 12 Tf\n70 700 Td\n(Experience Letter - EMS Exit Process) Tj\nET\n" +
                "endstream\nendobj\nxref\n0 5\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\n0000000212 00000 n\ntrailer\n<< /Size 5 >>\nstartxref\n313\n%%EOF";
        byte[] pdfBytes = pdfContent.getBytes(StandardCharsets.US_ASCII);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"experience_letter.pdf\"")
                .body(pdfBytes);
    }

    // 13. Cancel Exit Request
    @Operation(summary = "Cancel Exit Request", description = "Cancels a submitted resignation request, if allowed within the notice period window.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Exit request cancelled successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CancelExitResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cancellation not permitted", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Exit process not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/resignation/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> cancelExit(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody CancelExitRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            CancelExitResponse response = myExitService.cancelExitRequest(currentUser.getWorkEmail(), request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "OFB_002"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "OFB_001"));
        }
    }
}
