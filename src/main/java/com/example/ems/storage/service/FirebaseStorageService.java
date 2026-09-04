package com.example.ems.storage.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

public interface FirebaseStorageService {
    String uploadFile(MultipartFile file, String folder, String targetFileName) throws IOException;
    void deleteFile(String filePath);
    InputStream downloadFileAsStream(String filePath);
}
