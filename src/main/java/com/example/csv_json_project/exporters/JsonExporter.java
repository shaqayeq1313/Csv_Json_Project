package com.example.csv_json_project.exporters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class JsonExporter {

    private static final Logger logger = LoggerFactory.getLogger(JsonExporter.class);

    public void exportToJson(List<?> data, String fileName) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable pretty print
        File file = new File(fileName);

        try {
            objectMapper.writeValue(file, data);
            logger.info("Data exported to JSON file: {}", fileName);
        } catch (IOException e) {
            logger.error("Error exporting data to JSON file: {}", fileName, e);
            throw e;
        }
    }
}