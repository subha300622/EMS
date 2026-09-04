package com.example.ems.reports.config;

import java.io.InputStream;

public interface StorageService {
    String store(byte[] bytes, String fileName);
    InputStream retrieve(String fileKey);
    boolean delete(String fileKey);
}
