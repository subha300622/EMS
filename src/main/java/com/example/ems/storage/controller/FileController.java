package com.example.ems.storage.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.context.SecurityContextFacade;
import com.example.ems.storage.dto.FileMetadataResponse;
import com.example.ems.storage.entity.FileMetadata;
import com.example.ems.storage.entity.FileType;
import com.example.ems.storage.repository.FileMetadataRepository;
import com.example.ems.storage.service.FileAccessControlService;
import com.example.ems.storage.service.FileService;
import com.example.ems.storage.service.FirebaseStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
@CrossOrigin("*")
@Tag(name = "Secure Storage API")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FirebaseStorageService storageService;

    @Autowired
    private FileAccessControlService accessControlService;

    @Autowired
    private SecurityContextFacade securityContextFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    private ResponseEntity<Object> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private User getAuthenticatedUser() {
        String email = securityContextFacade.getEmail();
        if (email == null) return null;
        return userRepository.findByWorkEmail(email).orElse(null);
    }

    @Operation(summary = "Upload Profile Image", description = "Uploads a profile image for the authenticated user and replaces any previous profile picture.")
    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return unauthorizedResponse();
        }

        try {
            FileMetadata metadata = fileService.uploadProfileImage(file, user);
            return ResponseEntity.ok(ApiResponse.success("Profile image uploaded successfully", new FileMetadataResponse(metadata)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "VAL_004"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to upload profile image: " + e.getMessage(), "STO_001"));
        }
    }

    @Operation(summary = "Upload Document", description = "Uploads an HR document or attendance proof for the authenticated user.")
    @PostMapping(value = "/upload-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileType", defaultValue = "DOCUMENT") String fileType) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return unauthorizedResponse();
        }

        try {
            FileMetadata metadata = fileService.uploadDocument(file, user, fileType);
            return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", new FileMetadataResponse(metadata)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "VAL_004"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to upload document: " + e.getMessage(), "STO_002"));
        }
    }

    @Operation(summary = "Download File (Private Streaming)", description = "Performs RBAC access check on the requested file and streams the private byte content directly to the requester.")
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Object> downloadFile(@PathVariable("fileId") Long fileId) {
        User user = getAuthenticatedUser();

        Optional<FileMetadata> fileOpt = fileMetadataRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("File metadata not found", "STO_003"));
        }

        FileMetadata file = fileOpt.get();

        // Profile images are publicly readable so they can be loaded by browser <img> tags
        if (file.getFileType() != FileType.PROFILE_IMAGE) {
            if (user == null) {
                return unauthorizedResponse();
            }
            // Strict access control check
            if (!accessControlService.canAccessFile(user, file)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.error("Access Denied: You do not have permissions to access this file.", "AUTH_002"));
            }
        }

        try {
            InputStream stream = storageService.downloadFileAsStream(file.getFilePath());
            InputStreamResource resource = new InputStreamResource(stream);

            MediaType mediaType = MediaTypeFactory.getMediaType(file.getFileName()).orElse(MediaType.APPLICATION_OCTET_STREAM);

            String dispositionType = (file.getFileType() == FileType.PROFILE_IMAGE) ? "inline" : "attachment";
            String contentDisposition = String.format("%s; filename=\"%s\"", dispositionType, file.getFileName());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(mediaType)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to download file from storage: " + e.getMessage(), "STO_004"));
        }
    }
}
