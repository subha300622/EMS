package com.example.ems.finance.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MinimalXlsxWriter {

    public static byte[] generateXlsx(List<String> headers, List<List<String>> rows) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Write [Content_Types].xml
            zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
                    "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
                    "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n" +
                    "  <Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>\n" +
                    "  <Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>\n" +
                    "</Types>").getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // Write _rels/.rels
            zos.putNextEntry(new ZipEntry("_rels/.rels"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                    "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>\n" +
                    "</Relationships>").getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // Write xl/workbook.xml
            zos.putNextEntry(new ZipEntry("xl/workbook.xml"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n" +
                    "  <sheets>\n" +
                    "    <sheet name=\"Sheet1\" sheetId=\"1\" r:id=\"rId1\"/>\n" +
                    "  </sheets>\n" +
                    "</workbook>").getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // Write xl/_rels/workbook.xml.rels
            zos.putNextEntry(new ZipEntry("xl/_rels/workbook.xml.rels"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                    "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>\n" +
                    "</Relationships>").getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // Write xl/worksheets/sheet1.xml
            zos.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            StringBuilder sheetXml = new StringBuilder();
            sheetXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
            sheetXml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n");
            sheetXml.append("  <sheetData>\n");

            // Headers
            sheetXml.append("    <row>\n");
            for (String h : headers) {
                sheetXml.append("      <c t=\"inlineStr\"><is><t>").append(escapeXml(h)).append("</t></is></c>\n");
            }
            sheetXml.append("    </row>\n");

            // Rows
            for (List<String> row : rows) {
                sheetXml.append("    <row>\n");
                for (String cell : row) {
                    sheetXml.append("      <c t=\"inlineStr\"><is><t>").append(escapeXml(cell)).append("</t></is></c>\n");
                }
                sheetXml.append("    </row>\n");
            }

            sheetXml.append("  </sheetData>\n");
            sheetXml.append("</worksheet>");
            zos.write(sheetXml.toString().getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
