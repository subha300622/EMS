package com.example.ems.storage.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component("prodStorageService")
public class FirebaseStorageServiceImpl implements FirebaseStorageService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseStorageServiceImpl.class);

    @Autowired(required = false)
    private FirebaseApp firebaseApp;

    private Bucket getBucket() {
        if (firebaseApp == null) {
            throw new IllegalStateException("FirebaseApp is not initialized (FIREBASE_KEY_JSON environment variable is missing).");
        }
        return StorageClient.getInstance(firebaseApp).bucket();
    }

    @Override
    public String uploadFile(MultipartFile file, String folder, String targetFileName) throws IOException {
        Bucket bucket = getBucket();
        String fullPath = folder + "/" + targetFileName;

        log.info("Uploading file to Firebase Storage: {}", fullPath);
        bucket.create(fullPath, file.getBytes(), file.getContentType());
        return fullPath;
    }

    @Override
    public void deleteFile(String filePath) {
        Bucket bucket = getBucket();
        Blob blob = bucket.get(filePath);
        if (blob != null) {
            log.info("Deleting file from Firebase Storage: {}", filePath);
            blob.delete();
        } else {
            log.warn("File to delete not found in Firebase Storage: {}", filePath);
        }
    }

    @Override
    public InputStream downloadFileAsStream(String filePath) {
        Bucket bucket = getBucket();
        Blob blob = bucket.get(filePath);
        if (blob == null) {
            throw new IllegalArgumentException("File not found in Firebase Storage: " + filePath);
        }
        log.info("Streaming file from Firebase Storage: {}", filePath);
        return new ByteArrayInputStream(blob.getContent());
    }
}
