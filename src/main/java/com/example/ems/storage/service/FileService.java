package com.example.ems.storage.service;

import com.example.ems.auth.entity.User;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.storage.entity.FileMetadata;
import com.example.ems.storage.entity.FileType;
import com.example.ems.storage.repository.FileMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private FirebaseStorageService storageService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public FileMetadata uploadProfileImage(MultipartFile file, User user) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        // Validate MIME type is image
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed for profile pictures");
        }

        // 1. Generate unique file name
        String targetFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String folder = "profile-images";

        // 2. Upload to storage
        String filePath = storageService.uploadFile(file, folder, targetFileName);

        // 3. Persist metadata
        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(file.getOriginalFilename());
        metadata.setFilePath(filePath);
        metadata.setFileType(FileType.PROFILE_IMAGE);
        metadata.setUploadedByUserId(user.getUserId());
        metadata.setUploadedByRole(user.getRole() != null ? user.getRole().getName() : "EMPLOYEE");
        metadata.setDepartmentId(user.getDepartment());
        
        FileMetadata savedMetadata = fileMetadataRepository.save(metadata);

        // 4. Update employee profile link (if employee exists)
        employeeRepository.findByEmployeeId(user.getUserId()).ifPresent(emp -> {
            // Secure endpoint link
            String secureDownloadUrl = "/api/files/" + savedMetadata.getId() + "/download";
            emp.setProfileImage(secureDownloadUrl);
            employeeRepository.save(emp);
            log.info("Updated employee profile image path to: {}", secureDownloadUrl);
        });

        return savedMetadata;
    }

    @Transactional
    public FileMetadata uploadDocument(MultipartFile file, User user, String type) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        // Determine target folder based on type
        String folder = "employee-docs";
        if ("ATTENDANCE_PROOF".equalsIgnoreCase(type)) {
            folder = "attendance-proofs";
        }

        // Generate unique file name
        String targetFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Upload to storage
        String filePath = storageService.uploadFile(file, folder, targetFileName);

        // Persist metadata
        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(file.getOriginalFilename());
        metadata.setFilePath(filePath);
        metadata.setFileType(FileType.DOCUMENT);
        metadata.setUploadedByUserId(user.getUserId());
        metadata.setUploadedByRole(user.getRole() != null ? user.getRole().getName() : "EMPLOYEE");
        metadata.setDepartmentId(user.getDepartment());

        return fileMetadataRepository.save(metadata);
    }
}
