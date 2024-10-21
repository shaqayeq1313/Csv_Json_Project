package com.example.csv_json_project.services;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.csv_json_project.model.Account;
import com.example.csv_json_project.model.Customer;
import com.example.csv_json_project.repositories.AccountRepository;
import com.example.csv_json_project.repositories.CustomerRepository;
import com.example.csv_json_project.springSecurity.EncryptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.stereotype.Component;

@Component
public class CsvProcessorTask implements Runnable {

    private final List<String> accountLines;
    private final List<String> customerLines;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final List<Map<String, String>> sharedErrors;

    private static final String ERROR_FILE_PATH = "Errors.json";
    private static final Logger logger = LoggerFactory.getLogger(CsvProcessorTask.class);

    public CsvProcessorTask(List<String> accountLines, List<String> customerLines, AccountRepository accountRepository,
                            CustomerRepository customerRepository, List<Map<String, String>> sharedErrors) {
        this.accountLines = accountLines;
        this.customerLines = customerLines;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.sharedErrors = sharedErrors;
    }

    @Override
    public void run() {
        List<Map<String, String>> errors = new ArrayList<>();

        // Process Customers
        for (int i = 0; i < customerLines.size(); i++) {
            String customerLine = customerLines.get(i);
            String[] customerFields = customerLine.split(","); // Split by comma
            if (isHeaderRow(customerFields, "CUSTOMER_ID")) continue; // Skip header row if present

            try {
                Customer customer = createCustomerFromFields(customerFields);
                if (validateCustomer(customer)) {
                    customerRepository.save(customer);
                    logger.info("Saved Customer: {}", customer);
                } else {
                    errors.add(createErrorMap("Customer", "400", "Validation Error", "Customer validation failed", customerFields));
                    logger.warn("Validation failed for Customer: {}", customer);
                }
            } catch (DataIntegrityViolationException e) {
                errors.add(createErrorMap("Customer", "409", "Data Integrity Error", "Duplicate Customer ID", customerFields));
                logger.error("Duplicate Customer ID for line: {}", customerLine, e);
            } catch (Exception e) {
                errors.add(createErrorMap("Customer", "500", "Processing Error", "Exception processing Customer line", customerFields));
                logger.error("Exception processing Customer line: {}", customerLine, e);
            }
        }

        // Process Accounts
        for (int i = 0; i < accountLines.size(); i++) {
            String accountLine = accountLines.get(i);
            String[] accountFields = accountLine.split(","); // Split by comma
            if (isHeaderRow(accountFields, "ACCOUNT_NUMBER")) continue; // Skip header row if present

            try {
                Account account = createAccountFromFields(accountFields);
                if (validateAccount(account)) {
                    logger.info("Attempting to save Account: {}", account);
                    accountRepository.save(account);
                    logger.info("Saved Account: {}", account);
                } else {
                    errors.add(createErrorMap("Account", "400", "Validation Error", "Account validation failed", accountFields));
                    logger.warn("Validation failed for Account: {}", account);
                }
            } catch (DataIntegrityViolationException e) {
                logger.error("Data Integrity Violation while processing Account line: {}", accountLine, e);
                errors.add(createErrorMap("Account", "409", "Data Integrity Error", "Data Integrity Violation", accountFields));
            } catch (Exception e) {
                errors.add(createErrorMap("Account", "500", "Processing Error", "Exception processing Account line", accountFields));
                logger.error("Exception processing Account line: {}", accountLine, e);
            }
        }

        // Add errors to the shared list
        synchronized (sharedErrors) {
            sharedErrors.addAll(errors);
        }

        // Save errors to JSON file if there are any
        if (!sharedErrors.isEmpty()) {
            logger.info("Saving {} errors to JSON file", sharedErrors.size());
            saveErrorsToJson(sharedErrors);
        } else {
            logger.info("No errors to save");
        }
    }

    // Check if the row is the CSV header
    private boolean isHeaderRow(String[] fields, String expectedFirstField) {
        return fields.length > 0 && fields[0].trim().equalsIgnoreCase(expectedFirstField);
    }

    // Create Account object from CSV fields
    private Account createAccountFromFields(String[] fields) {
        if (fields.length < 6) {
            throw new IllegalArgumentException("Insufficient fields for Account");
        }

        Account account = new Account();
        try {
            // Decrypt the accountNumber field
            String encryptedAccountNumber = fields[0];
            if (encryptedAccountNumber != null) {
                encryptedAccountNumber = encryptedAccountNumber.trim();
            } else {
                throw new IllegalArgumentException("Account number is missing or null.");
            }
            String decryptedAccountNumber = EncryptionUtil.decrypt(encryptedAccountNumber);
            if (decryptedAccountNumber == null || decryptedAccountNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Decrypted account number is null or empty.");
            }
            account.setAccountNumber(decryptedAccountNumber); // ACCOUNT_NUMBER

            // Handle account type
            String accountTypeStr = fields[1];
            if (accountTypeStr != null) {
                accountTypeStr = accountTypeStr.trim();
            } else {
                throw new IllegalArgumentException("Account type is missing or null.");
            }
            account.setAccountType(Integer.parseInt(accountTypeStr)); // ACCOUNT_TYPE

            // Handle account limit
            String accountLimitStr = fields[2];
            if (accountLimitStr != null) {
                accountLimitStr = accountLimitStr.trim();
            } else {
                throw new IllegalArgumentException("Account limit is missing or null.");
            }
            account.setAccountlimit(Double.parseDouble(accountLimitStr)); // ACCOUNT_LIMIT

            // Decrypt the balance field
            String encryptedBalance = fields[3];
            if (encryptedBalance != null) {
                encryptedBalance = encryptedBalance.trim();
            } else {
                throw new IllegalArgumentException("Balance is missing or null.");
            }
            String decryptedBalance = EncryptionUtil.decrypt(encryptedBalance);
            if (decryptedBalance == null || decryptedBalance.trim().isEmpty()) {
                throw new IllegalArgumentException("Decrypted balance is null or empty.");
            }
            double balance = Double.parseDouble(decryptedBalance); // Convert to double
            account.setBalance(balance); // BALANCE

            // Handle open date
            String openDateStr = fields[4];
            if (openDateStr != null) {
                openDateStr = openDateStr.trim();
            } else {
                throw new IllegalArgumentException("Open date is missing or null.");
            }
            account.setOpenDate(new Date(parseDate(openDateStr).getTime())); // OPEN_DATE

            // Assuming CUSTOMER_ID is a foreign key, handle accordingly
            String customerIdStr = fields[5];
            if (customerIdStr != null) {
                customerIdStr = customerIdStr.trim();
            } else {
                throw new IllegalArgumentException("Customer ID is missing or null.");
            }
            Long customerId = Long.parseLong(customerIdStr);
            Customer customer = customerRepository.findById(customerId).orElseThrow(
                    () -> new IllegalArgumentException("Customer ID not found: " + customerId));
            account.setCustomer(customer); // Link account to customer
        } catch (Exception e) {
            throw new RuntimeException("Error creating account from fields", e);
        }

        return account;
    }

    // Create Customer object from CSV fields
    private Customer createCustomerFromFields(String[] fields) {
        if (fields.length < 7) {
            throw new IllegalArgumentException("Insufficient fields for Customer");
        }

        Customer customer = new Customer();
        try {
            customer.setCustomer_Id(Long.parseLong(fields[0].trim())); // CUSTOMER_ID

            // Decrypt the name field
            String encryptedName = fields[3].trim();
            String decryptedName = EncryptionUtil.decrypt(encryptedName);
            customer.setName(decryptedName); // NAME

            // Decrypt the surname field
            String encryptedSurname = fields[5].trim();
            String decryptedSurname = EncryptionUtil.decrypt(encryptedSurname);
            customer.setSurname(decryptedSurname); // SURNAME

            // Decrypt the nationalId field
            String encryptedNationalId = fields[4].trim();
            String decryptedNationalId = EncryptionUtil.decrypt(encryptedNationalId);
            customer.setNationalId(decryptedNationalId); // NATIONAL_ID

            customer.setAddress(fields[1].trim()); // ADDRESS
            customer.setBirthDate(new Date(parseDate(fields[2].trim()).getTime())); // BIRTH_DATE
            customer.setZipCode(fields[6].trim()); // ZIP_CODE
        } catch (Exception e) {
            throw new RuntimeException("Error creating customer from fields", e);
        }

        return customer;
    }

    // Account validation
    private boolean validateAccount(Account account) {
        boolean isValid = true;

        if (account.getBalance() > account.getAccountlimit()) {
            logger.warn("Account validation failed: balance exceeds limit for {}", account);
            isValid = false;
        }
        if (account.getAccountNumber().length() != 22) {
            logger.warn("Account validation failed: account number length is not 22 for {}", account);
            isValid = false;
        }
        if (account.getCustomer() == null) {
            logger.warn("Account validation failed: Customer is not assigned for {}", account);
            isValid = false;
        }
        if (account.getAccountType() < 1 || account.getAccountType() > 3) {
            logger.warn("Account validation failed: invalid account type for {}", account);
            isValid = false;
        }

        return isValid;
    }

    // Customer validation
    private boolean validateCustomer(Customer customer) {
        boolean isValid = true;

        Calendar cal = Calendar.getInstance();
        cal.setTime(customer.getBirthDate());
        int birthYear = cal.get(Calendar.YEAR);

        //Customers whose birth year is older than 1995
        if (birthYear > 1995) {
            logger.info("Customer validation passed: birth year is after 1995 for {}", customer);
            isValid = true;  // Invalid Customer
        } else {
            logger.warn("Customer validation failed: birth year is on or before 1995 for {}", customer);
            isValid = false;  // Valid Customer
        }

        if (customer.getNationalId().length() != 10) {
            logger.warn("Customer validation failed: national ID length is not 10 for {}", customer);
            isValid = false;
        }

        return isValid;
    }

    // Parse the date from string
    private java.util.Date parseDate(String dateStr) {
        SimpleDateFormat[] formats = {
                new SimpleDateFormat("yyyy-MM-dd"),
                new SimpleDateFormat("d/M/yyyy"),
                new SimpleDateFormat("dd/MM/yyyy")
        };

        for (SimpleDateFormat format : formats) {
            try {
                return format.parse(dateStr);
            } catch (ParseException e) {
                // Ignore and try next format
            }
        }
        throw new IllegalArgumentException("Date format not recognized for date: " + dateStr);
    }

    // Create a map for error logging
    public Map<String, String> createErrorMap(String file_name, String error_code, String error_classification_name, String error_description, String[] fields) {
        Map<String, String> error = new HashMap<>();
        error.put("file_name", file_name);
        error.put("error_code", error_code);
        error.put("error_classification_name", error_classification_name);
        error.put("error_description", error_description);
        error.put("error_date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new java.util.Date())); // Add current date and time

        if (fields != null) {
            StringBuilder decryptedData = new StringBuilder();
            for (String field : fields) {
                String decryptedField = EncryptionUtil.decrypt(field.trim());
                if (decryptedField == null || decryptedField.trim().isEmpty()) {
                    decryptedField = field.trim(); // Use original field if decryption fails
                }
                decryptedData.append(decryptedField).append(",");
            }
            error.put("error_data", decryptedData.toString().replaceAll(",$", "")); // Remove trailing comma
        }

        return error;
    }

    // Save error list to JSON file
    private void saveErrorsToJson(List<Map<String, String>> errors) {
        try (FileWriter fileWriter = new FileWriter(ERROR_FILE_PATH)) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable pretty print
            objectMapper.writeValue(fileWriter, errors);
            logger.info("Errors saved to {}", ERROR_FILE_PATH);
        } catch (IOException e) {
            logger.error("Error saving errors to JSON file", e);
        }
    }
}