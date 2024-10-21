package com.example.csv_json_project.services;

import com.example.csv_json_project.exporters.JsonExporter;
import com.example.csv_json_project.exporters.XmlExporter;
import com.example.csv_json_project.model.Account;
import com.example.csv_json_project.model.Customer;
import com.example.csv_json_project.repositories.AccountRepository;
import com.example.csv_json_project.repositories.CustomerRepository;
import com.example.csv_json_project.springSecurity.EncryptionUtil;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class CsvProcessorService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JsonExporter jsonExporter;

    @Autowired
    private XmlExporter xmlExporter;

    private final List<Map<String, String>> sharedErrors = new ArrayList<>();


    private static final Logger logger = LoggerFactory.getLogger(CsvProcessorService.class);

    public void processCsvFiles(String accountFilePath, String customerFilePath) throws IOException {
    	
        ExecutorService executorService = Executors.newCachedThreadPool();


        // Read the CSV files
        List<String> accountLines = readCsvFile(accountFilePath);
        List<String> customerLines = readCsvFile(customerFilePath);

        int numThreads = Runtime.getRuntime().availableProcessors();
        int accountChunkSize = (int) Math.ceil((double) accountLines.size() / numThreads);
        int customerChunkSize = (int) Math.ceil((double) customerLines.size() / numThreads);

        for (int i = 0; i < numThreads; i++) {
            int accountStart = i * accountChunkSize;
            int accountEnd = Math.min(accountStart + accountChunkSize, accountLines.size());
            int customerStart = i * customerChunkSize;
            int customerEnd = Math.min(customerStart + customerChunkSize, customerLines.size());

            if (accountStart >= accountEnd && customerStart >= customerEnd) {
                break; // No more data to process
            }

            List<String> accountChunk = accountLines.subList(accountStart, accountEnd);
            List<String> customerChunk = customerLines.subList(customerStart, customerEnd);
            executorService.submit(new CsvProcessorTask(accountChunk, customerChunk, accountRepository, customerRepository, sharedErrors));
        }

        executorService.shutdown();

        while (!executorService.isTerminated()) {
            // Wait for all tasks to finish
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("Executor service interrupted", e);
                Thread.currentThread().interrupt();
            }
        }

        // Generate error JSON file for invalid lines
        generateErrorJsonFile();

        // Generate XML and JSON files for customers with account balance > 1000
        generateXmlAndJsonFiles();
    }

    // Read the CSV file and return list of lines
    public List<String> readCsvFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) { // Skip empty lines
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    // Generate error JSON file for invalid lines
    public void generateErrorJsonFile() throws IOException {
        if (sharedErrors.isEmpty()) {
            logger.info("No errors found during CSV processing.");
            return;
        }

        // Generate error JSON file
        jsonExporter.exportToJson(sharedErrors, "errors.json");
    }

    // Generate XML and JSON files for customers with account balance > 1000
    @Transactional
	public void generateXmlAndJsonFiles() throws IOException {
        List<Customer> customers = customerRepository.findAll();
        List<Map<String, Object>> filteredCustomers = customers.stream()
                .filter(customer -> customer.getAccounts().stream()
                        .anyMatch(account -> account.getBalance() > 1000))
                .flatMap(customer -> customer.getAccounts().stream()
                        .filter(account -> account.getBalance() > 1000)
                        .map(account -> createCustomerAccountMap(customer, account)))
                .collect(Collectors.toList());

        if (filteredCustomers.isEmpty()) {
            logger.info("No customers found with account balance greater than 1000.");
            return;
        }

        // Generate JSON file
        jsonExporter.exportToJson(filteredCustomers, "customers_balance_gt_1000.json");

        // Generate XML file
        xmlExporter.exportToXml(filteredCustomers, "customers_balance_gt_1000.xml");
    }
    private Map<String, Object> createCustomerAccountMap(Customer customer, Account account) {
        Map<String, Object> customerAccountMap = new HashMap<>();
        customerAccountMap.put("Customer_Id", customer.getCustomer_Id());
        customerAccountMap.put("Customer_Name", customer.getName());
        customerAccountMap.put("Customer_Surname", customer.getSurname());
        customerAccountMap.put("Customer_National_Id", customer.getNationalId());
        try {
            customerAccountMap.put("Encrypted_Account_Number", EncryptionUtil.encrypt(account.getAccountNumber()));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            customerAccountMap.put("Account_OpenDate", dateFormat.format(account.getOpenDate()));
            customerAccountMap.put("Encrypted_Balance", EncryptionUtil.encrypt(String.valueOf(account.getBalance())));
        } catch (Exception e) {
            logger.error("Error encrypting account number or balance", e);
        }
        return customerAccountMap;
    }
}