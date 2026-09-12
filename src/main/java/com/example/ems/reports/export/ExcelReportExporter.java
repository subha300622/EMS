package com.example.ems.reports.export;

import com.example.ems.reports.common.ExportFormat;
import com.example.ems.reports.exception.ReportExportException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class ExcelReportExporter implements ReportExporter {

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.EXCEL;
    }

    @Override
    public byte[] export(String title, List<String> headers, List<List<String>> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(title != null ? title : "Report");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }

            int rowIdx = 1;
            for (List<String> rowData : rows) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 0; i < rowData.size(); i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(rowData.get(i) != null ? rowData.get(i) : "");
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new ReportExportException("Failed to generate Excel report", e);
        }
    }
}
