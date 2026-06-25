package com.example.ems.storage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("mockStorageService")
public class MockStorageServiceImpl implements FirebaseStorageService {

    private static final Logger log = LoggerFactory.getLogger(MockStorageServiceImpl.class);

    private final Map<String, byte[]> fileMap = new ConcurrentHashMap<>();
    private final Map<String, String> contentTypeMap = new ConcurrentHashMap<>();

    @Override
    public String uploadFile(MultipartFile file, String folder, String targetFileName) throws IOException {
        String fullPath = folder + "/" + targetFileName;
        log.info("[MOCK STORAGE] Uploading file to path: {}", fullPath);
        fileMap.put(fullPath, file.getBytes());
        contentTypeMap.put(fullPath, file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        return fullPath;
    }

    @Override
    public void deleteFile(String filePath) {
        log.info("[MOCK STORAGE] Deleting file at path: {}", filePath);
        fileMap.remove(filePath);
        contentTypeMap.remove(filePath);
    }

    @Override
    public InputStream downloadFileAsStream(String filePath) {
        log.info("[MOCK STORAGE] Downloading file stream for path: {}", filePath);
        byte[] data = fileMap.get(filePath);
        if (data == null) {
            throw new IllegalArgumentException("[MOCK STORAGE] File not found: " + filePath);
        }
        return new ByteArrayInputStream(data);
    }

    public byte[] getFileData(String filePath) {
        return fileMap.get(filePath);
    }

    public void clear() {
        fileMap.clear();
        contentTypeMap.clear();
    }
}
