package com.example.ems.reports.export;

import com.example.ems.reports.common.ExportFormat;
import com.example.ems.reports.exception.ReportExportException;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;

@Component
public class PdfReportExporter implements ReportExporter {

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.PDF;
    }

    @Override
    public byte[] export(String title, List<String> headers, List<List<String>> rows) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph(title != null ? title : "Report"));
            document.add(new Paragraph("Generated at: " + Instant.now().toString()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(headers.size());
            for (String header : headers) {
                table.addCell(header);
            }

            for (List<String> row : rows) {
                for (String val : row) {
                    table.addCell(val != null ? val : "");
                }
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new ReportExportException("Failed to generate PDF report", e);
        }
    }
}
