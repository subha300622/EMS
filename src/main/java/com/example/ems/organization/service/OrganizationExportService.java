package com.example.ems.organization.service;

import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.Subscription;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Service
public class OrganizationExportService {

    public byte[] exportToCsv(List<Organization> orgs) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Code,Name,Email,Phone,Plan,Status,CreatedAt\n");
        for (Organization org : orgs) {
            Subscription sub = org.getActiveSubscription();
            sb.append(org.getId()).append(",")
                    .append(escapeCsv(org.getOrganizationCode())).append(",")
                    .append(escapeCsv(org.getName())).append(",")
                    .append(escapeCsv(org.getEmail())).append(",")
                    .append(escapeCsv(org.getPhone())).append(",")
                    .append(sub != null ? sub.getPlanCode() : "N/A").append(",")
                    .append(sub != null ? sub.getStatus().name() : "N/A").append(",")
                    .append(org.getCreatedAt().toString()).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportToExcel(List<Organization> orgs) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Organizations");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Code");
            header.createCell(2).setCellValue("Name");
            header.createCell(3).setCellValue("Email");
            header.createCell(4).setCellValue("Phone");
            header.createCell(5).setCellValue("Plan");
            header.createCell(6).setCellValue("Status");
            header.createCell(7).setCellValue("CreatedAt");

            int rowIdx = 1;
            for (Organization org : orgs) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(org.getId());
                row.createCell(1).setCellValue(org.getOrganizationCode());
                row.createCell(2).setCellValue(org.getName());
                row.createCell(3).setCellValue(org.getEmail() != null ? org.getEmail() : "");
                row.createCell(4).setCellValue(org.getPhone() != null ? org.getPhone() : "");
                Subscription sub = org.getActiveSubscription();
                row.createCell(5).setCellValue(sub != null ? sub.getPlanCode() : "N/A");
                row.createCell(6).setCellValue(sub != null ? sub.getStatus().name() : "N/A");
                row.createCell(7).setCellValue(org.getCreatedAt().toString());
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }

    public byte[] exportToPdf(List<Organization> orgs) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Organizations Export"));
            document.add(new Paragraph("Generated at: " + Instant.now().toString()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(8);
            table.addCell("ID");
            table.addCell("Code");
            table.addCell("Name");
            table.addCell("Email");
            table.addCell("Phone");
            table.addCell("Plan");
            table.addCell("Status");
            table.addCell("CreatedAt");

            for (Organization org : orgs) {
                table.addCell(String.valueOf(org.getId()));
                table.addCell(org.getOrganizationCode());
                table.addCell(org.getName());
                table.addCell(org.getEmail() != null ? org.getEmail() : "");
                table.addCell(org.getPhone() != null ? org.getPhone() : "");
                Subscription sub = org.getActiveSubscription();
                table.addCell(sub != null ? sub.getPlanCode() : "N/A");
                table.addCell(sub != null ? sub.getStatus().name() : "N/A");
                table.addCell(org.getCreatedAt().toString());
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF export", e);
        }
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
