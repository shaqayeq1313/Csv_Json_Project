package com.example.csv_json_project.exportersTest;

import com.example.csv_json_project.exporters.XmlExporter;
import com.example.csv_json_project.model.Account;
import com.example.csv_json_project.model.Customer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class XmlExporterTest {

    @Autowired
    private XmlExporter xmlExporter;

    private List<Customer> customers;
    private List<Account> accounts;

    @BeforeEach
    void setUp() {
        Customer customer1 = new Customer(1L, "John", "Doe", "123 Main St", "12345", "1234567890", Date.valueOf("1996-01-01"), null);
        Customer customer2 = new Customer(2L, "Jane", "Smith", "456 Elm St", "67890", "0987654321", Date.valueOf("1990-05-15"), null);
        customers = List.of(customer1, customer2);

        Account account1 = new Account("1234567890123456789012", 1, 1000.0, Date.valueOf("2023-01-01"), 500.0, customer1);
        Account account2 = new Account("2345678901234567890123", 2, 2000.0, Date.valueOf("2023-02-01"), 1500.0, customer2);
        accounts = List.of(account1, account2);
    }

    @Test
    void testExportCustomersToXml() throws IOException {
        String fileName = "test_customers.xml";
        xmlExporter.exportToXml(customers, fileName);

        // Verify the file exists
        File file = new File(fileName);
        assertTrue(file.exists());

        // Verify the content of the file
        XmlMapper xmlMapper = new XmlMapper();
        List<?> exportedCustomers = xmlMapper.readValue(file, List.class);
        assertNotNull(exportedCustomers);
        assertEquals(customers.size(), exportedCustomers.size());

        // Clean up
        file.delete();
    }

    @Test
    void testExportAccountsToXml() throws IOException {
        String fileName = "test_accounts.xml";
        xmlExporter.exportToXml(accounts, fileName);

        // Verify the file exists
        File file = new File(fileName);
        assertTrue(file.exists());

        // Verify the content of the file
        XmlMapper xmlMapper = new XmlMapper();
        List<?> exportedAccounts = xmlMapper.readValue(file, List.class);
        assertNotNull(exportedAccounts);
        assertEquals(accounts.size(), exportedAccounts.size());

        // Clean up
        file.delete();
    }

    @Test
    void testExportToXmlIOException() {
        String fileName = "/invalid/path/test.xml";
        assertThrows(IOException.class, () -> {
            xmlExporter.exportToXml(customers, fileName);
        });
    }
}