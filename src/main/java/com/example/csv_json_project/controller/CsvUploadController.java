package com.example.csv_json_project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.csv_json_project.services.CsvProcessorService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/csv")
public class CsvUploadController {

    public CsvUploadController(CsvProcessorService csvProcessorService) {
		super();
		this.csvProcessorService = csvProcessorService;
	}

	@Autowired
    private CsvProcessorService csvProcessorService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsvFiles(@RequestParam("accountFile") MultipartFile accountFile,
                                                 @RequestParam("customerFile") MultipartFile customerFile) {
        try {
            // Save the uploaded files to temporary locations
            Path accountFilePath = saveUploadedFile(accountFile);
            Path customerFilePath = saveUploadedFile(customerFile);

            // Process the CSV files
            csvProcessorService.processCsvFiles(accountFilePath.toString(), customerFilePath.toString());

            // Clean up the temporary files
            Files.deleteIfExists(accountFilePath);
            Files.deleteIfExists(customerFilePath);

            return ResponseEntity.ok("CSV files processed successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing CSV files: " + e.getMessage());
        }
    }

    public Path saveUploadedFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        // Create a temporary file
        Path tempFile = Files.createTempFile("uploaded-", ".csv");
        Files.write(tempFile, file.getBytes());

        return tempFile;
    }
}