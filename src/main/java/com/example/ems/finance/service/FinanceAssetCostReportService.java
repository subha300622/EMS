package com.example.ems.finance.service;

import com.example.ems.asset.entity.MyAsset;
import com.example.ems.asset.entity.MyAssetCategory;
import com.example.ems.asset.entity.MyAssetMaintenance;
import com.example.ems.asset.repository.MyAssetCategoryRepository;
import com.example.ems.asset.repository.MyAssetMaintenanceRepository;
import com.example.ems.asset.repository.MyAssetRepository;
import com.example.ems.finance.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FinanceAssetCostReportService {

    @Autowired
    private MyAssetRepository assetRepository;

    @Autowired
    private MyAssetCategoryRepository categoryRepository;

    @Autowired
    private MyAssetMaintenanceRepository maintenanceRepository;

    // Helper: Determine if an asset belongs to a category
    private boolean assetBelongsToCategory(MyAsset asset, MyAssetCategory category) {
        if (asset == null || category == null || asset.getCategory() == null) {
            return false;
        }
        String assetCat = asset.getCategory().trim();
        return assetCat.equalsIgnoreCase(category.getCode()) || assetCat.equalsIgnoreCase(category.getName());
    }

    // Helper: Calculate years in use for an asset
    private int calculateYearsInUse(MyAsset asset) {
        if (asset == null || asset.getPurchaseDate() == null) {
            return 0;
        }
        return Period.between(asset.getPurchaseDate(), LocalDate.now()).getYears();
    }

    // Helper: Calculate replacement priority based on years in use
    private String determineReplacementPriority(int yearsInUse) {
        if (yearsInUse >= 8) {
            return "CRITICAL";
        } else if (yearsInUse >= 6) {
            return "HIGH";
        } else if (yearsInUse >= 4) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    // Helper: Calculate annual depreciation for an asset
    private BigDecimal calculateAnnualDepreciation(MyAsset asset) {
        if (asset == null || asset.getPurchasePrice() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = asset.getDepreciationPercentage();
        if (rate == null) {
            rate = BigDecimal.valueOf(15.00); // default 15%
        }
        return asset.getPurchasePrice().multiply(rate).divide(BigDecimal.valueOf(100.00), 2, RoundingMode.HALF_UP);
    }

    // Helper: Calculate total maintenance cost for an asset
    private BigDecimal calculateAssetMaintenanceCost(MyAsset asset) {
        if (asset == null) {
            return BigDecimal.ZERO;
        }
        List<MyAssetMaintenance> maintenances = maintenanceRepository.findByAssetIdOrderByStartDateDesc(asset.getId());
        BigDecimal total = BigDecimal.ZERO;
        for (MyAssetMaintenance m : maintenances) {
            BigDecimal cost = m.getActualCost();
            if (cost == null) {
                cost = m.getEstimatedCost();
            }
            if (cost != null) {
                total = total.add(cost);
            }
        }
        return total;
    }

    // Helper: Compute current financial year string (e.g. FY2025-26)
    public String getCurrentFinancialYear() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        if (month >= 4) {
            return "FY" + year + "-" + String.valueOf(year + 1).substring(2);
        } else {
            return "FY" + (year - 1) + "-" + String.valueOf(year).substring(2);
        }
    }

    // ── 1. GET DASHBOARD ──────────────────────────────────────────────────────
    public AssetCostDashboardResponse getDashboard() {
        List<MyAsset> assets = assetRepository.findAll();
        BigDecimal totalAssetValue = BigDecimal.ZERO;
        BigDecimal annualDepreciation = BigDecimal.ZERO;
        int replacementDue = 0;
        int assetCount = assets.size();

        for (MyAsset asset : assets) {
            if (asset.getPurchasePrice() != null) {
                totalAssetValue = totalAssetValue.add(asset.getPurchasePrice());
            }
            annualDepreciation = annualDepreciation.add(calculateAnnualDepreciation(asset));
            int years = calculateYearsInUse(asset);
            if (years >= 6) {
                replacementDue++;
            }
        }

        // Sum all maintenances
        BigDecimal maintenanceCost = BigDecimal.ZERO;
        List<MyAssetMaintenance> maintenances = maintenanceRepository.findAll();
        for (MyAssetMaintenance m : maintenances) {
            BigDecimal cost = m.getActualCost();
            if (cost == null) {
                cost = m.getEstimatedCost();
            }
            if (cost != null) {
                maintenanceCost = maintenanceCost.add(cost);
            }
        }

        return new AssetCostDashboardResponse(
                totalAssetValue,
                annualDepreciation,
                maintenanceCost,
                replacementDue,
                assetCount,
                LocalDate.now()
        );
    }

    // ── 2. GET BREAKDOWN (PAGINATED) ──────────────────────────────────────────
    public Map<String, Object> getBreakdown(Pageable pageable) {
        List<MyAssetCategory> categories = categoryRepository.findAll();
        List<MyAsset> allAssets = assetRepository.findAll();

        List<AssetCostBreakdownItem> allItems = new ArrayList<>();
        for (MyAssetCategory category : categories) {
            List<MyAsset> catAssets = allAssets.stream()
                    .filter(a -> assetBelongsToCategory(a, category))
                    .collect(Collectors.toList());

            int assetCount = catAssets.size();
            BigDecimal totalValue = BigDecimal.ZERO;
            BigDecimal annualDepreciation = BigDecimal.ZERO;
            BigDecimal bookValue = BigDecimal.ZERO;

            for (MyAsset asset : catAssets) {
                if (asset.getPurchasePrice() != null) {
                    totalValue = totalValue.add(asset.getPurchasePrice());
                }
                annualDepreciation = annualDepreciation.add(calculateAnnualDepreciation(asset));
                if (asset.getCurrentValue() != null) {
                    bookValue = bookValue.add(asset.getCurrentValue());
                }
            }

            allItems.add(new AssetCostBreakdownItem(
                    category.getId(),
                    category.getName(),
                    assetCount,
                    totalValue,
                    annualDepreciation,
                    bookValue,
                    "ACTIVE"
            ));
        }

        // Apply manual pagination
        int totalElements = allItems.size();
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        if (totalPages == 0) {
            totalPages = 1;
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageSize), totalElements);

        List<AssetCostBreakdownItem> pagedContent = new ArrayList<>();
        if (start < totalElements) {
            pagedContent = allItems.subList(start, end);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", pagedContent);
        result.put("page", pageNumber);
        result.put("size", pageSize);
        result.put("totalElements", totalElements);
        result.put("totalPages", totalPages);

        return result;
    }

    // ── 3. GET CATEGORY DETAILS ───────────────────────────────────────────────
    public CategoryCostDetailsResponse getCategoryDetails(Long categoryId) {
        MyAssetCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + categoryId));

        List<MyAsset> allAssets = assetRepository.findAll();
        List<MyAsset> catAssets = allAssets.stream()
                .filter(a -> assetBelongsToCategory(a, category))
                .collect(Collectors.toList());

        int assetCount = catAssets.size();
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal annualDepreciation = BigDecimal.ZERO;
        BigDecimal bookValue = BigDecimal.ZERO;
        BigDecimal totalMaintenance = BigDecimal.ZERO;

        for (MyAsset asset : catAssets) {
            if (asset.getPurchasePrice() != null) {
                totalValue = totalValue.add(asset.getPurchasePrice());
            }
            annualDepreciation = annualDepreciation.add(calculateAnnualDepreciation(asset));
            if (asset.getCurrentValue() != null) {
                bookValue = bookValue.add(asset.getCurrentValue());
            }
            totalMaintenance = totalMaintenance.add(calculateAssetMaintenanceCost(asset));
        }

        BigDecimal averageAssetValue = BigDecimal.ZERO;
        if (assetCount > 0) {
            averageAssetValue = totalValue.divide(BigDecimal.valueOf(assetCount), 2, RoundingMode.HALF_UP);
        }

        return new CategoryCostDetailsResponse(
                category.getId(),
                category.getName(),
                assetCount,
                totalValue,
                annualDepreciation,
                bookValue,
                "ACTIVE",
                averageAssetValue,
                totalMaintenance
        );
    }

    // ── 4. GET CATEGORY ASSETS ────────────────────────────────────────────────
    public CategoryAssetsResponse getCategoryAssets(Long categoryId) {
        MyAssetCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + categoryId));

        List<MyAsset> allAssets = assetRepository.findAll();
        List<CategoryAssetItem> assetsList = allAssets.stream()
                .filter(a -> assetBelongsToCategory(a, category))
                .map(a -> new CategoryAssetItem(
                        a.getId(),
                        a.getAssetCode(),
                        a.getAssetName(),
                        a.getPurchasePrice(),
                        a.getCurrentValue(),
                        a.getAssignedDate(),
                        a.getStatus()
                ))
                .collect(Collectors.toList());

        return new CategoryAssetsResponse(category.getId(), category.getName(), assetsList);
    }

    // ── 5. GET ASSET FINANCIAL DETAILS ────────────────────────────────────────
    public AssetFinancialDetailsResponse getAssetFinancialDetails(Long assetId) {
        MyAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found with ID: " + assetId));

        int years = calculateYearsInUse(asset);
        boolean replacementDue = years >= 6;
        BigDecimal rate = asset.getDepreciationPercentage();
        if (rate == null) {
            rate = BigDecimal.valueOf(15.00);
        }
        BigDecimal annualDepreciation = calculateAnnualDepreciation(asset);
        BigDecimal maintenanceCost = calculateAssetMaintenanceCost(asset);

        return new AssetFinancialDetailsResponse(
                asset.getId(),
                asset.getAssetCode(),
                asset.getAssetName(),
                asset.getCategory(),
                asset.getBrand(),
                asset.getPurchaseDate(),
                asset.getPurchasePrice(),
                asset.getCurrentValue(),
                asset.getCurrentValue(), // bookValue matches currentValue
                annualDepreciation,
                rate,
                maintenanceCost,
                asset.getWarrantyExpiryDate(),
                replacementDue,
                asset.getStatus()
        );
    }

    // ── 6. GET DEPRECIATION REPORT ────────────────────────────────────────────
    public DepreciationReportResponse getDepreciationReport() {
        List<MyAssetCategory> categories = categoryRepository.findAll();
        List<MyAsset> allAssets = assetRepository.findAll();

        BigDecimal totalDepreciation = BigDecimal.ZERO;
        List<CategoryDepreciationItem> categoryItems = new ArrayList<>();

        for (MyAssetCategory category : categories) {
            List<MyAsset> catAssets = allAssets.stream()
                    .filter(a -> assetBelongsToCategory(a, category))
                    .collect(Collectors.toList());

            int assetCount = catAssets.size();
            BigDecimal depAmount = BigDecimal.ZERO;
            for (MyAsset asset : catAssets) {
                depAmount = depAmount.add(calculateAnnualDepreciation(asset));
            }

            totalDepreciation = totalDepreciation.add(depAmount);
            categoryItems.add(new CategoryDepreciationItem(
                    category.getName(),
                    assetCount,
                    depAmount
            ));
        }

        return new DepreciationReportResponse(
                getCurrentFinancialYear(),
                totalDepreciation,
                categoryItems
        );
    }

    // ── 7. GET MAINTENANCE COST REPORT ────────────────────────────────────────
    public MaintenanceCostReportResponse getMaintenanceCostReport() {
        List<MyAssetCategory> categories = categoryRepository.findAll();
        List<MyAsset> allAssets = assetRepository.findAll();

        BigDecimal totalMaintenanceCost = BigDecimal.ZERO;
        List<CategoryMaintenanceItem> categoryItems = new ArrayList<>();

        for (MyAssetCategory category : categories) {
            List<MyAsset> catAssets = allAssets.stream()
                    .filter(a -> assetBelongsToCategory(a, category))
                    .collect(Collectors.toList());

            BigDecimal catCost = BigDecimal.ZERO;
            for (MyAsset asset : catAssets) {
                catCost = catCost.add(calculateAssetMaintenanceCost(asset));
            }

            totalMaintenanceCost = totalMaintenanceCost.add(catCost);
            categoryItems.add(new CategoryMaintenanceItem(
                    category.getName(),
                    catCost
            ));
        }

        // Count unique assets under maintenance
        List<MyAssetMaintenance> maintenances = maintenanceRepository.findAll();
        long assetsUnderMaintCount = maintenances.stream()
                .filter(m -> "UNDER_MAINTENANCE".equalsIgnoreCase(m.getStatus()))
                .map(m -> m.getAsset().getId())
                .distinct()
                .count();

        return new MaintenanceCostReportResponse(
                totalMaintenanceCost,
                (int) assetsUnderMaintCount,
                categoryItems
        );
    }

    // ── 8. GET REPLACEMENT DUE ASSETS ────────────────────────────────────────
    public ReplacementDueAssetsResponse getReplacementDueAssets() {
        List<MyAsset> allAssets = assetRepository.findAll();
        List<ReplacementDueAssetItem> dueAssets = new ArrayList<>();

        for (MyAsset asset : allAssets) {
            int years = calculateYearsInUse(asset);
            if (years >= 6) {
                dueAssets.add(new ReplacementDueAssetItem(
                        asset.getId(),
                        asset.getAssetCode(),
                        asset.getAssetName(),
                        asset.getCategory(),
                        asset.getPurchaseDate(),
                        asset.getCurrentValue(),
                        years,
                        determineReplacementPriority(years)
                ));
            }
        }

        return new ReplacementDueAssetsResponse(
                dueAssets.size(),
                dueAssets
        );
    }

    // ── 9. EXPORT PDF METADATA ────────────────────────────────────────────────
    public ExportReportResponse exportPdf() {
        String fy = getCurrentFinancialYear();
        String fileName = "asset-cost-report-" + fy + ".pdf";
        String downloadUrl = "/api/v1/files/" + fileName;
        return new ExportReportResponse(fileName, "application/pdf", downloadUrl, LocalDateTime.now());
    }

    // ── 10. EXPORT CSV METADATA ───────────────────────────────────────────────
    public ExportReportResponse exportCsv() {
        String fy = getCurrentFinancialYear();
        String fileName = "asset-cost-report-" + fy + ".csv";
        String downloadUrl = "/api/v1/files/" + fileName;
        return new ExportReportResponse(fileName, "text/csv", downloadUrl, LocalDateTime.now());
    }

    // ── FILE GENERATOR: CSV BYTES ─────────────────────────────────────────────
    public byte[] generateCsvExportBytes() {
        List<MyAsset> assets = assetRepository.findAll();
        StringBuilder csv = new StringBuilder("Asset ID,Asset Tag,Asset Name,Category,Brand,Purchase Date,Purchase Price,Current Value,Depreciation %,Replacement Due\n");
        for (MyAsset a : assets) {
            int years = calculateYearsInUse(a);
            boolean replacementDue = years >= 6;
            csv.append(a.getId()).append(",")
               .append(a.getAssetCode()).append(",")
               .append(a.getAssetName().replace(",", " ")).append(",")
               .append(a.getCategory()).append(",")
               .append(a.getBrand()).append(",")
               .append(a.getPurchaseDate()).append(",")
               .append(a.getPurchasePrice()).append(",")
               .append(a.getCurrentValue()).append(",")
               .append(a.getDepreciationPercentage() != null ? a.getDepreciationPercentage() : "15.00").append(",")
               .append(replacementDue).append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ── FILE GENERATOR: PDF BYTES ─────────────────────────────────────────────
    public byte[] generatePdfExportBytes() {
        List<MyAsset> assets = assetRepository.findAll();
        List<String> lines = new ArrayList<>();
        lines.add("Generated At: " + LocalDateTime.now());
        lines.add("Financial Year: " + getCurrentFinancialYear());
        lines.add("");
        lines.add("----------------------------------------------------------------------------------");
        lines.add(String.format("%-12s %-25s %-12s %-12s %-12s", "Tag", "Name", "Category", "Purchase Val", "Current Val"));
        lines.add("----------------------------------------------------------------------------------");
        for (MyAsset a : assets) {
            lines.add(String.format("%-12s %-25s %-12s %-12.2f %-12.2f",
                    a.getAssetCode(),
                    a.getAssetName().length() > 22 ? a.getAssetName().substring(0, 20) + ".." : a.getAssetName(),
                    a.getCategory(),
                    a.getPurchasePrice() != null ? a.getPurchasePrice().doubleValue() : 0.0,
                    a.getCurrentValue() != null ? a.getCurrentValue().doubleValue() : 0.0
            ));
        }
        lines.add("----------------------------------------------------------------------------------");

        return generatePdfCatalog("Asset Cost Report Summary", lines);
    }

    // Basic PDF 1.4 catalog writer helper
    private byte[] generatePdfCatalog(String title, List<String> lines) {
        StringBuilder contentStream = new StringBuilder();
        contentStream.append("BT\n/F1 12 Tf\n70 720 Td\n(").append(title).append(") Tj\n");
        for (String line : lines) {
            String escapedLine = line.replace("(", "\\(").replace(")", "\\)");
            contentStream.append("0 -20 Td\n(").append(escapedLine).append(") Tj\n");
        }
        contentStream.append("ET\n");

        String pdf = "%PDF-1.4\n" +
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << >> >>\nendobj\n" +
                "4 0 obj\n<< /Length " + contentStream.length() + " >>\nstream\n" +
                contentStream.toString() +
                "endstream\nendobj\nxref\n0 5\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\n0000000212 00000 n\ntrailer\n<< /Size 5 >>\nstartxref\n313\n%%EOF";

        return pdf.getBytes(StandardCharsets.US_ASCII);
    }
}
