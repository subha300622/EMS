package com.example.ems.offboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.service.MyExitService;
import com.example.ems.security.service.JwtService;
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
@RequestMapping("/api/v1/my-exit")
@CrossOrigin("*")
@Tag(name = "My Exit")
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

    // 1. Get My Exit Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            MyExitDashboardResponse response = myExitService.getMyExitDashboard(currentUser.getWorkEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "OFB_002"));
        }
    }

    // 2. Submit Resignation Request
    @PostMapping("/resignation")
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
    @PostMapping("/assets/{assetId}/return")
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
    @PostMapping("/interview")
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
    @PostMapping("/agreements/sign")
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

    // 10. Get F&F Settlement Details
    @GetMapping("/settlement")
    public ResponseEntity<?> getSettlement(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            SettlementDetailsResponse response = myExitService.getSettlementDetails(currentUser.getWorkEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "OFB_007"));
        }
    }

    // 11. Get Exit Timeline
    @GetMapping("/timeline")
    public ResponseEntity<?> getTimeline(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            ExitTimelineResponse response = myExitService.getExitTimeline(currentUser.getWorkEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "OFB_002"));
        }
    }

    // 12. Download Experience Letter
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
    @PutMapping("/resignation/cancel")
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
