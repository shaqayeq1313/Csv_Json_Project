package com.example.csv_json_project.controllerTesr;

import com.example.csv_json_project.controller.CsvUploadController;
import com.example.csv_json_project.services.CsvProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CsvUploadController.class)
class CsvUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CsvProcessorService csvProcessorService;

    @Test
    void testUploadCsvFiles_Success() throws Exception {
        // Arrange
        MockMultipartFile accountFile = new MockMultipartFile(
                "accountFile", "account.csv", MediaType.TEXT_PLAIN_VALUE, "account data".getBytes());
        MockMultipartFile customerFile = new MockMultipartFile(
                "customerFile", "customer.csv", MediaType.TEXT_PLAIN_VALUE, "customer data".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/csv/upload")
                        .file(accountFile)
                        .file(customerFile))
                .andExpect(status().isOk())
                .andExpect(content().string("CSV files processed successfully."));

        // Verify that the service method was called
        verify(csvProcessorService, times(1)).processCsvFiles(anyString(), anyString());
    }

    @Test
    void testUploadCsvFiles_EmptyFile() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "accountFile", "empty.csv", MediaType.TEXT_PLAIN_VALUE, new byte[0]);
        MockMultipartFile customerFile = new MockMultipartFile(
                "customerFile", "customer.csv", MediaType.TEXT_PLAIN_VALUE, "customer data".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/csv/upload")
                        .file(emptyFile)
                        .file(customerFile))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error processing CSV files: Uploaded file is empty."));

        // Verify that the service method was not called
        verify(csvProcessorService, never()).processCsvFiles(anyString(), anyString());
    }

    @Test
    void testUploadCsvFiles_IOException() throws Exception {
        // Arrange
        MockMultipartFile accountFile = new MockMultipartFile(
                "accountFile", "account.csv", MediaType.TEXT_PLAIN_VALUE, "account data".getBytes());
        MockMultipartFile customerFile = new MockMultipartFile(
                "customerFile", "customer.csv", MediaType.TEXT_PLAIN_VALUE, "customer data".getBytes());

        // Mock the service to throw an IOException
        doThrow(new IOException("Simulated IO error")).when(csvProcessorService).processCsvFiles(anyString(), anyString());

        // Act & Assert
        mockMvc.perform(multipart("/api/csv/upload")
                        .file(accountFile)
                        .file(customerFile))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error processing CSV files: Simulated IO error"));

        // Verify that the service method was called
        verify(csvProcessorService, times(1)).processCsvFiles(anyString(), anyString());
    }

    @Test
    void testSaveUploadedFile_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", MediaType.TEXT_PLAIN_VALUE, "test data".getBytes());

        // Act
        Path tempFile = new CsvUploadController(csvProcessorService).saveUploadedFile(file);

        // Assert
        assert Files.exists(tempFile);
        assert "test data".equals(new String(Files.readAllBytes(tempFile)));

        // Clean up
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testSaveUploadedFile_EmptyFile() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.csv", MediaType.TEXT_PLAIN_VALUE, new byte[0]);

        // Act & Assert
        try {
            new CsvUploadController(csvProcessorService).saveUploadedFile(emptyFile);
        } catch (IllegalArgumentException e) {
            assert "Uploaded file is empty.".equals(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
