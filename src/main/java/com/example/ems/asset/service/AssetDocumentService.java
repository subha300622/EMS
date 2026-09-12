package com.example.ems.asset.service;

import com.example.ems.asset.dto.AssetDtos.AssetDocumentResponse;
import com.example.ems.asset.entity.Asset;
import com.example.ems.asset.entity.AssetDocument;
import com.example.ems.asset.repository.AssetDocumentRepository;
import com.example.ems.asset.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetDocumentService {

    @Autowired
    private AssetDocumentRepository documentRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Transactional(readOnly = true)
    public List<AssetDocumentResponse> getAssetDocuments(Long organizationId, Long assetId) {
        return documentRepository.findByOrganizationIdAndAssetId(organizationId, assetId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssetDocumentResponse uploadAssetDocument(Long organizationId, Long assetId, String documentType, MultipartFile file, String uploadedBy) {
        Asset asset = assetRepository.findByIdAndOrganizationIdAndDeletedFalse(assetId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with ID: " + assetId));

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read uploaded file");
        }

        AssetDocument doc = new AssetDocument(
                organizationId,
                asset,
                documentType != null ? documentType : "GENERAL",
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf",
                file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                file.getSize(),
                "DATABASE",
                null,
                bytes,
                uploadedBy
        );

        doc = documentRepository.save(doc);
        return mapToResponse(doc);
    }

    @Transactional(readOnly = true)
    public AssetDocument getAssetDocumentEntity(Long organizationId, Long documentId) {
        return documentRepository.findByOrganizationIdAndId(organizationId, documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset document not found with ID: " + documentId));
    }

    @Transactional
    public void deleteAssetDocument(Long organizationId, Long assetId, Long documentId) {
        AssetDocument doc = documentRepository.findByOrganizationIdAndId(organizationId, documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset document not found with ID: " + documentId));
        if (!doc.getAsset().getId().equals(assetId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document does not belong to asset " + assetId);
        }
        documentRepository.delete(doc);
    }

    private AssetDocumentResponse mapToResponse(AssetDocument doc) {
        return new AssetDocumentResponse(
                doc.getId(),
                doc.getAsset().getId(),
                doc.getDocumentType(),
                doc.getFileName(),
                doc.getContentType(),
                doc.getFileSize(),
                doc.getStorageProvider(),
                doc.getUploadedBy(),
                doc.getUploadedAt()
        );
    }
}
