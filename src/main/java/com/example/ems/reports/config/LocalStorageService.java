package com.example.ems.reports.config;

import com.example.ems.reports.exception.StorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalStorageService implements StorageService {

    @Autowired
    private ReportStorageProperties properties;

    private Path rootPath;

    @PostConstruct
    public void init() {
        try {
            rootPath = Paths.get(properties.getStorage().getLocalPath()).toAbsolutePath().normalize();
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new StorageException("Could not initialize local storage path", e);
        }
    }

    @Override
    public String store(byte[] bytes, String fileName) {
        try {
            Path targetFile = rootPath.resolve(fileName);
            Files.write(targetFile, bytes);
            return fileName;
        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + fileName, e);
        }
    }

    @Override
    public InputStream retrieve(String fileKey) {
        try {
            Path targetFile = rootPath.resolve(fileKey);
            if (!Files.exists(targetFile)) {
                throw new StorageException("File not found in local storage: " + fileKey);
            }
            return new FileInputStream(targetFile.toFile());
        } catch (FileNotFoundException e) {
            throw new StorageException("File not found: " + fileKey, e);
        }
    }

    @Override
    public boolean delete(String fileKey) {
        try {
            Path targetFile = rootPath.resolve(fileKey);
            return Files.deleteIfExists(targetFile);
        } catch (IOException e) {
            throw new StorageException("Failed to delete file: " + fileKey, e);
        }
    }
}
