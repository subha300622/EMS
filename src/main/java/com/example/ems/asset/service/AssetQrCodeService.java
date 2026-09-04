package com.example.ems.asset.service;

import com.example.ems.asset.entity.Asset;
import com.example.ems.asset.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class AssetQrCodeService {

    @Autowired
    private AssetRepository assetRepository;

    @Value("${app.asset-verification-url}")
    private String assetVerificationUrl;

    public byte[] generateAssetQrCodePng(Long organizationId, Long assetId) {
        Asset asset = assetRepository.findByIdAndOrganizationIdAndDeletedFalse(assetId, organizationId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with ID: " + assetId));

        String baseUrl = assetVerificationUrl.endsWith("/") ? assetVerificationUrl : assetVerificationUrl + "/";
        String verificationUrl = baseUrl + asset.getAssetCode();
        return createDynamicQrPngImage(verificationUrl, asset.getAssetCode(), asset.getAssetName());
    }

    private byte[] createDynamicQrPngImage(String text, String assetCode, String assetName) {
        int width = 300;
        int height = 300;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // White background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Dark Border
        g.setColor(new Color(30, 41, 59));
        g.drawRect(10, 10, width - 20, height - 20);
        g.drawRect(12, 12, width - 24, height - 24);

        // QR Code Matrix Simulation
        g.setColor(Color.BLACK);
        int size = 200;
        int startX = (width - size) / 2;
        int startY = 40;

        g.fillRect(startX, startY, size, size);

        // Inside white squares
        g.setColor(Color.WHITE);
        g.fillRect(startX + 20, startY + 20, size - 40, size - 40);

        // Center square
        g.setColor(new Color(15, 23, 42));
        g.fillRect(startX + 60, startY + 60, size - 120, size - 120);

        // Text labels
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString(assetCode, startX + 50, startY + size + 25);

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString(verificationUrlShort(text), 20, height - 15);

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to render QR PNG stream");
        }
    }

    private String verificationUrlShort(String url) {
        if (url.length() > 40) {
            return url.substring(0, 37) + "...";
        }
        return url;
    }
}
