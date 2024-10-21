package com.example.csv_json_project.exporters;


import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class XmlExporter {

    private static final Logger logger = LoggerFactory.getLogger(XmlExporter.class);

    public void exportToXml(List<?> data, String fileName) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print the XML

        File file = new File(fileName);

        try {
            xmlMapper.writeValue(file, data);
            logger.info("Data exported to XML file: {}", fileName);
        } catch (IOException e) {
            logger.error("Error exporting data to XML file: {}", fileName, e);
            throw e;
        }
    }
}