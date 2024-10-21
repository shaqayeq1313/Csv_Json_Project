package com.example.csv_json_project.servicesTest;

import com.example.csv_json_project.model.Account;
import com.example.csv_json_project.model.Customer;
import com.example.csv_json_project.repositories.AccountRepository;
import com.example.csv_json_project.repositories.CustomerRepository;
import com.example.csv_json_project.services.CsvProcessorTask;
import com.example.csv_json_project.springSecurity.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Method;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CsvProcessorTaskTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CsvProcessorTask csvProcessorTask;

    private List<String> accountLines;
    private List<String> customerLines;
    private List<Map<String, String>> sharedErrors;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountLines = new ArrayList<>();
        customerLines = new ArrayList<>();
        sharedErrors = new ArrayList<>();
        csvProcessorTask = new CsvProcessorTask(accountLines, customerLines, accountRepository, customerRepository, sharedErrors);
    }

    @Test
    void testRun_ValidCustomersAndAccounts() {
        
        customerLines.add("CUSTOMER_ID,ADDRESS,BIRTH_DATE,NAME,NATIONAL_ID,SURNAME,ZIP_CODE");
        customerLines.add("1,123 Main St,1996-01-01,encryptedName,encryptedNationalId,encryptedSurname,12345");

        accountLines.add("ACCOUNT_NUMBER,ACCOUNT_TYPE,ACCOUNT_LIMIT,BALANCE,OPEN_DATE,CUSTOMER_ID");
        accountLines.add("encryptedAccountNumber,1,1000,encryptedBalance,2023-01-01,1");

        // Mock decryption
        when(EncryptionUtil.decrypt("encryptedAccountNumber")).thenReturn("1234567890123456789012");
        when(EncryptionUtil.decrypt("encryptedBalance")).thenReturn("500");
        when(EncryptionUtil.decrypt("encryptedName")).thenReturn("John");
        when(EncryptionUtil.decrypt("encryptedNationalId")).thenReturn("1234567890");
        when(EncryptionUtil.decrypt("encryptedSurname")).thenReturn("Doe");

        // Mock customer repository
        when(customerRepository.findById(1L)).thenReturn(java.util.Optional.of(new Customer()));

        // Run the task
        csvProcessorTask.run();

        // Verify interactions
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(accountRepository, times(1)).save(any(Account.class));

        // Verify no errors
        assertTrue(sharedErrors.isEmpty());
    }

    @Test
    void testRun_InvalidCustomer() {
        // Prepare invalid customer data (birth year before 1995)
        customerLines.add("CUSTOMER_ID,ADDRESS,BIRTH_DATE,NAME,NATIONAL_ID,SURNAME,ZIP_CODE");
        customerLines.add("1,123 Main St,1994-01-01,encryptedName,encryptedNationalId,encryptedSurname,12345");

        // Mock decryption
        when(EncryptionUtil.decrypt("encryptedName")).thenReturn("John");
        when(EncryptionUtil.decrypt("encryptedNationalId")).thenReturn("1234567890");
        when(EncryptionUtil.decrypt("encryptedSurname")).thenReturn("Doe");

        // Run the task
        csvProcessorTask.run();

        // Verify no customer saved
        verify(customerRepository, never()).save(any(Customer.class));

        // Verify error added
        assertFalse(sharedErrors.isEmpty());
        assertEquals(1, sharedErrors.size());
        assertEquals("Customer", sharedErrors.get(0).get("file_name"));
        assertEquals("400", sharedErrors.get(0).get("error_code"));
    }

    @Test
    void testRun_InvalidAccount() {
        // Prepare invalid account data (balance exceeds limit)
        accountLines.add("ACCOUNT_NUMBER,ACCOUNT_TYPE,ACCOUNT_LIMIT,BALANCE,OPEN_DATE,CUSTOMER_ID");
        accountLines.add("encryptedAccountNumber,1,1000,encryptedBalance,2023-01-01,1");

        // Mock decryption
        when(EncryptionUtil.decrypt("encryptedAccountNumber")).thenReturn("1234567890123456789012");
        when(EncryptionUtil.decrypt("encryptedBalance")).thenReturn("1500");

        // Mock customer repository
        when(customerRepository.findById(1L)).thenReturn(java.util.Optional.of(new Customer()));

        // Run the task
        csvProcessorTask.run();

        // Verify no account saved
        verify(accountRepository, never()).save(any(Account.class));

        // Verify error added
        assertFalse(sharedErrors.isEmpty());
        assertEquals(1, sharedErrors.size());
        assertEquals("Account", sharedErrors.get(0).get("file_name"));
        assertEquals("400", sharedErrors.get(0).get("error_code"));
    }

    @Test
    void testRun_DataIntegrityViolation() {
        // Prepare valid data but mock DataIntegrityViolationException
        customerLines.add("CUSTOMER_ID,ADDRESS,BIRTH_DATE,NAME,NATIONAL_ID,SURNAME,ZIP_CODE");
        customerLines.add("1,123 Main St,1996-01-01,encryptedName,encryptedNationalId,encryptedSurname,12345");

        // Mock decryption
        when(EncryptionUtil.decrypt("encryptedName")).thenReturn("John");
        when(EncryptionUtil.decrypt("encryptedNationalId")).thenReturn("1234567890");
        when(EncryptionUtil.decrypt("encryptedSurname")).thenReturn("Doe");

        // Mock customer repository to throw DataIntegrityViolationException
        doThrow(DataIntegrityViolationException.class).when(customerRepository).save(any(Customer.class));

        // Run the task
        csvProcessorTask.run();

        // Verify error added
        assertFalse(sharedErrors.isEmpty());
        assertEquals(1, sharedErrors.size());
        assertEquals("Customer", sharedErrors.get(0).get("file_name"));
        assertEquals("409", sharedErrors.get(0).get("error_code"));
    }

    @Test
    void testIsHeaderRow() throws Exception {
        String[] headerFields = {"CUSTOMER_ID", "ADDRESS", "BIRTH_DATE", "NAME", "NATIONAL_ID", "SURNAME", "ZIP_CODE"};
        String[] nonHeaderFields = {"1", "123 Main St", "1996-01-01", "John", "1234567890", "Doe", "12345"};

        Method isHeaderRowMethod = CsvProcessorTask.class.getDeclaredMethod("isHeaderRow", String[].class, String.class);
        isHeaderRowMethod.setAccessible(true);

        assertTrue((boolean) isHeaderRowMethod.invoke(csvProcessorTask, (Object) headerFields, "CUSTOMER_ID"));
        assertFalse((boolean) isHeaderRowMethod.invoke(csvProcessorTask, (Object) nonHeaderFields, "CUSTOMER_ID"));
    }

    @Test
    void testCreateAccountFromFields() throws Exception {
        String[] accountFields = {"encryptedAccountNumber", "1", "1000", "encryptedBalance", "2023-01-01", "1"};

        // Mock decryption
        when(EncryptionUtil.decrypt("encryptedAccountNumber")).thenReturn("1234567890123456789012");
        when(EncryptionUtil.decrypt("encryptedBalance")).thenReturn("500");

        // Mock customer repository
        when(customerRepository.findById(1L)).thenReturn(java.util.Optional.of(new Customer()));
        
        Method createAccountFromFieldsMethod = CsvProcessorTask.class.getDeclaredMethod("createAccountFromFields", String[].class);
        createAccountFromFieldsMethod.setAccessible(true);
        Account account = (Account) createAccountFromFieldsMethod.invoke(csvProcessorTask, (Object) accountFields);

        assertNotNull(account);
        assertEquals("1234567890123456789012", account.getAccountNumber());
        assertEquals(1, account.getAccountType());
        assertEquals(1000.0, account.getAccountlimit());
        assertEquals(500.0, account.getBalance());
        assertEquals(Date.valueOf("2023-01-01"), account.getOpenDate());
        assertNotNull(account.getCustomer());
    }

    @Test
    void testCreateCustomerFromFields() throws Exception {
        String[] customerFields = {"1", "123 Main St", "1996-01-01", "encryptedName", "encryptedNationalId", "encryptedSurname", "12345"};

        // Mock decryption
        when(EncryptionUtil.decrypt("encryptedName")).thenReturn("John");
        when(EncryptionUtil.decrypt("encryptedNationalId")).thenReturn("1234567890");
        when(EncryptionUtil.decrypt("encryptedSurname")).thenReturn("Doe");

        Method createCustomerFromFieldsMethod = CsvProcessorTask.class.getDeclaredMethod("createCustomerFromFields", String[].class);
        createCustomerFromFieldsMethod.setAccessible(true);
        Customer customer = (Customer) createCustomerFromFieldsMethod.invoke(csvProcessorTask, (Object) customerFields);

        assertNotNull(customer);
        assertEquals(1L, customer.getCustomer_Id());
        assertEquals("123 Main St", customer.getAddress());
        assertEquals(Date.valueOf("1996-01-01"), customer.getBirthDate());
        assertEquals("John", customer.getName());
        assertEquals("1234567890", customer.getNationalId());
        assertEquals("Doe", customer.getSurname());
        assertEquals("12345", customer.getZipCode());
    }

    @Test
    void testValidateAccount() throws Exception {
        Account validAccount = new Account();
        validAccount.setAccountNumber("1234567890123456789012");
        validAccount.setAccountType(1);
        validAccount.setAccountlimit(1000.0);
        validAccount.setBalance(500.0);
        validAccount.setOpenDate(Date.valueOf("2023-01-01"));
        validAccount.setCustomer(new Customer());

        Account invalidAccount = new Account();
        invalidAccount.setAccountNumber("1234567890123456789012");
        invalidAccount.setAccountType(1);
        invalidAccount.setAccountlimit(1000.0);
        invalidAccount.setBalance(1500.0);
        invalidAccount.setOpenDate(Date.valueOf("2023-01-01"));
        invalidAccount.setCustomer(null);

        // Use reflection to call the private method
        Method validateAccountMethod = CsvProcessorTask.class.getDeclaredMethod("validateAccount", Account.class);
        validateAccountMethod.setAccessible(true);

        assertTrue((boolean) validateAccountMethod.invoke(csvProcessorTask, validAccount));
        assertFalse((boolean) validateAccountMethod.invoke(csvProcessorTask, invalidAccount));
    }

    @Test
    void testValidateCustomer() throws Exception {
        Customer validCustomer = new Customer();
        validCustomer.setCustomer_Id(1L);
        validCustomer.setAddress("123 Main St");
        validCustomer.setBirthDate(Date.valueOf("1996-01-01"));
        validCustomer.setName("John");
        validCustomer.setNationalId("1234567890");
        validCustomer.setSurname("Doe");
        validCustomer.setZipCode("12345");

        Customer invalidCustomer = new Customer();
        invalidCustomer.setCustomer_Id(1L);
        invalidCustomer.setAddress("123 Main St");
        invalidCustomer.setBirthDate(Date.valueOf("1994-01-01"));
        invalidCustomer.setName("John");
        invalidCustomer.setNationalId("1234567890");
        invalidCustomer.setSurname("Doe");
        invalidCustomer.setZipCode("12345");

        Method validateCustomerMethod = CsvProcessorTask.class.getDeclaredMethod("validateCustomer", Customer.class);
        validateCustomerMethod.setAccessible(true);

        assertTrue((boolean) validateCustomerMethod.invoke(csvProcessorTask, validCustomer));
        assertFalse((boolean) validateCustomerMethod.invoke(csvProcessorTask, invalidCustomer));
    }

    @Test
    void testParseDate() throws Exception {
        String dateStr1 = "2023-01-01";
        String dateStr2 = "1/1/2023";
        String dateStr3 = "01/01/2023";

        Method parseDateMethod = CsvProcessorTask.class.getDeclaredMethod("parseDate", String.class);
        parseDateMethod.setAccessible(true);

        try {
            java.util.Date date1 = (java.util.Date) parseDateMethod.invoke(csvProcessorTask, dateStr1);
            java.util.Date date2 = (java.util.Date) parseDateMethod.invoke(csvProcessorTask, dateStr2);
            java.util.Date date3 = (java.util.Date) parseDateMethod.invoke(csvProcessorTask, dateStr3);

            assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse(dateStr1), date1);
            assertEquals(new SimpleDateFormat("d/M/yyyy").parse(dateStr2), date2);
            assertEquals(new SimpleDateFormat("dd/MM/yyyy").parse(dateStr3), date3);
        } catch (ParseException e) {
            fail("ParseException should not be thrown");
        }
    }

    @Test
    void testCreateErrorMap() throws Exception {
        String[] fields = {"1", "123 Main St", "1996-01-01", "John", "1234567890", "Doe", "12345"};

        Method createErrorMapMethod = CsvProcessorTask.class.getDeclaredMethod("createErrorMap", String.class, String.class, String.class, String.class, String[].class);
        createErrorMapMethod.setAccessible(true);

        Map<String, String> errorMap = (Map<String, String>) createErrorMapMethod.invoke(csvProcessorTask, "Customer", "400", "Validation Error", "Customer validation failed", fields);

        assertNotNull(errorMap);
        assertEquals("Customer", errorMap.get("file_name"));
        assertEquals("400", errorMap.get("error_code"));
        assertEquals("Validation Error", errorMap.get("error_classification_name"));
        assertEquals("Customer validation failed", errorMap.get("error_description"));
        assertNotNull(errorMap.get("error_date"));
        assertNotNull(errorMap.get("error_data"));
    }

    @Test
    void testSaveErrorsToJson() throws Exception {
        List<Map<String, String>> errors = new ArrayList<>();
        errors.add(csvProcessorTask.createErrorMap("Customer", "400", "Validation Error", "Customer validation failed", new String[]{"1", "123 Main St", "1996-01-01", "John", "1234567890", "Doe", "12345"}));

       
        Method saveErrorsToJsonMethod = CsvProcessorTask.class.getDeclaredMethod("saveErrorsToJson", List.class);
        saveErrorsToJsonMethod.setAccessible(true);

        saveErrorsToJsonMethod.invoke(csvProcessorTask, errors);

   
        assertFalse(sharedErrors.isEmpty());
        assertEquals(1, sharedErrors.size());
    }
}