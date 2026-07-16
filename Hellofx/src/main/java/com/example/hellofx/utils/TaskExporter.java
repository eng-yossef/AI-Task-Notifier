package com.example.hellofx.utils;

import com.example.hellofx.Task;
import com.itextpdf.text.Font;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskExporter {
    // Define the iText font globally
    static Font helveticaItalic = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.ITALIC);

    public static void exportToPDF(List<Task> tasks, String filePath) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();

        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Task Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); // Add some spacing

        // Add timestamp
        Paragraph timestamp = new Paragraph(
                "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                helveticaItalic
        );
        document.add(timestamp);
        document.add(new Paragraph(" "));

        // Create table
        PdfPTable table = new PdfPTable(5); // 5 columns
        table.setWidthPercentage(100);

        // Add headers
        String[] headers = {"Title", "Description", "Due Date", "Priority", "Status"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);
        }

        // Add data
        for (Task task : tasks) {
            table.addCell(task.getTitle());
            table.addCell(task.getDescription());
            table.addCell(task.getDueDate());
            table.addCell(task.getPriority());
            table.addCell(task.getStatus());
        }

        document.add(table);
        document.close();
    }

    public static void exportToExcel(List<Task> tasks, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Tasks");

        // Create header row
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont(); // Apache POI Font
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] columns = {"Title", "Description", "Due Date", "Priority", "Status"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Add data rows
        int rowNum = 1;
        for (Task task : tasks) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(task.getTitle());
            row.createCell(1).setCellValue(task.getDescription());
            row.createCell(2).setCellValue(task.getDueDate());
            row.createCell(3).setCellValue(task.getPriority());
            row.createCell(4).setCellValue(task.getStatus());
        }

        // Autosize columns
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            workbook.write(outputStream);
        }
        workbook.close();
    }
}
