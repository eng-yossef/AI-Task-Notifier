package com.example.hellofx;

import com.example.hellofx.utils.TaskExporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskExporterTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void testPDFExport() {
        try {
            // Create sample tasks
            List<Task> tasks = createSampleTasks();
            
            // Create temporary file path
            String pdfPath = tempDir.resolve("tasks.pdf").toString();
            
            // Export to PDF
            TaskExporter.exportToPDF(tasks, pdfPath);
            
            // Verify file exists and has content
            assertTrue(java.nio.file.Files.exists(Path.of(pdfPath)));
            assertTrue(java.nio.file.Files.size(Path.of(pdfPath)) > 0);
            
        } catch (Exception e) {
            fail("PDF export failed: " + e.getMessage());
        }
    }
    
    @Test
    void testExcelExport() {
        try {
            // Create sample tasks
            List<Task> tasks = createSampleTasks();
            
            // Create temporary file path
            String excelPath = tempDir.resolve("tasks.xlsx").toString();
            
            // Export to Excel
            TaskExporter.exportToExcel(tasks, excelPath);
            
            // Verify file exists and has content
            assertTrue(java.nio.file.Files.exists(Path.of(excelPath)));
            assertTrue(java.nio.file.Files.size(Path.of(excelPath)) > 0);
            
        } catch (Exception e) {
            fail("Excel export failed: " + e.getMessage());
        }
    }
    
    private List<Task> createSampleTasks() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(1, "Complete Project", "Finish the project documentation", "2024-12-31", "High", "In Progress"));
        tasks.add(new Task(2, "Review Code", "Review pull requests", "2024-12-20", "Medium", "Pending"));
        tasks.add(new Task(3, "Update Tests", "Add new test cases", "2024-12-25", "Low", "Not Started"));
        return tasks;
    }
}
