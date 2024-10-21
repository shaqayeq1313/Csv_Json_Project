package com.example.csv_json_project.servicesTest;

import com.example.csv_json_project.exporters.JsonExporter;
import com.example.csv_json_project.exporters.XmlExporter;
import com.example.csv_json_project.model.Account;
import com.example.csv_json_project.model.Customer;
import com.example.csv_json_project.repositories.AccountRepository;
import com.example.csv_json_project.repositories.CustomerRepository;
import com.example.csv_json_project.services.CsvProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CsvProcessorServiceTest {

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private JsonExporter jsonExporter;

    @MockBean
    private XmlExporter xmlExporter;

    @Autowired
    private CsvProcessorService csvProcessorService;

    private Path tempAccountFile;
    private Path tempCustomerFile;

    @BeforeEach
    void setUp() throws IOException {
        tempAccountFile = Files.createTempFile("account", ".csv");
        tempCustomerFile = Files.createTempFile("customer", ".csv");

        
        Files.write(tempAccountFile, List.of(
                "ACCOUNT_NUMBER,ACCOUNT_TYPE,ACCOUNT_LIMIT,BALANCE,OPEN_DATE,CUSTOMER_ID",
                "1234567890123456789012,1,1000,500,2023-01-01,1"
        ));
        Files.write(tempCustomerFile, List.of(
                "CUSTOMER_ID,ADDRESS,BIRTH_DATE,NAME,NATIONAL_ID,SURNAME,ZIP_CODE",
                "1,123 Main St,1996-01-01,John,1234567890,Doe,12345"
        ));
    }

    @Test
    void testProcessCsvFiles() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Mock repository methods
        when(customerRepository.findById(1L)).thenReturn(java.util.Optional.of(new Customer()));

       
        csvProcessorService.processCsvFiles(tempAccountFile.toString(), tempCustomerFile.toString());

        verify(accountRepository, times(1)).save(any(Account.class));
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(jsonExporter, times(1)).exportToJson(anyList(), eq("errors.json"));
        verify(jsonExporter, times(1)).exportToJson(anyList(), eq("customers_balance_gt_1000.json"));
        verify(xmlExporter, times(1)).exportToXml(anyList(), eq("customers_balance_gt_1000.xml"));

        
        Field sharedErrorsField = CsvProcessorService.class.getDeclaredField("sharedErrors");
        sharedErrorsField.setAccessible(true);
        List<Map<String, String>> sharedErrors = (List<Map<String, String>>) sharedErrorsField.get(csvProcessorService);

        // Verify sharedErrors
        assertTrue(sharedErrors.isEmpty());
    }

    @Test
    void testReadCsvFile() throws IOException {
        List<String> lines = csvProcessorService.readCsvFile(tempAccountFile.toString());
        assertEquals(2, lines.size());
        assertEquals("ACCOUNT_NUMBER,ACCOUNT_TYPE,ACCOUNT_LIMIT,BALANCE,OPEN_DATE,CUSTOMER_ID", lines.get(0));
        assertEquals("1234567890123456789012,1,1000,500,2023-01-01,1", lines.get(1));
    }

    @Test
    void testGenerateErrorJsonFile() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Mock shared errors
        Field sharedErrorsField = CsvProcessorService.class.getDeclaredField("sharedErrors");
        sharedErrorsField.setAccessible(true);
        List<Map<String, String>> sharedErrors = (List<Map<String, String>>) sharedErrorsField.get(csvProcessorService);
        sharedErrors.add(Map.of("error", "test error"));

    
        csvProcessorService.generateErrorJsonFile();

        
        verify(jsonExporter, times(1)).exportToJson(anyList(), eq("errors.json"));
    }

    @Test
    void testGenerateXmlAndJsonFiles() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Mock repository methods
        Customer customer = new Customer();
        customer.setCustomer_Id(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setNationalId("1234567890");
        Account account = new Account();
        account.setAccountNumber("1234567890123456789012");
        account.setBalance(1500.0);
        account.setOpenDate(Date.valueOf("2023-01-01"));
        customer.getAccounts().add(account);
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        
        csvProcessorService.generateXmlAndJsonFiles();

        // Verify interactions
        verify(jsonExporter, times(1)).exportToJson(anyList(), eq("customers_balance_gt_1000.json"));
        verify(xmlExporter, times(1)).exportToXml(anyList(), eq("customers_balance_gt_1000.xml"));

        Field sharedErrorsField = CsvProcessorService.class.getDeclaredField("sharedErrors");
        sharedErrorsField.setAccessible(true);
        List<Map<String, String>> sharedErrors = (List<Map<String, String>>) sharedErrorsField.get(csvProcessorService);

        // Verify sharedErrors
        assertTrue(sharedErrors.isEmpty());
    }
}