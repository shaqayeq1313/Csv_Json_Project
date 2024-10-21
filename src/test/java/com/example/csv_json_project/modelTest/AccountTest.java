package com.example.csv_json_project.modelTest;

import com.example.csv_json_project.model.Account;
import com.example.csv_json_project.model.Customer;
import com.example.csv_json_project.repositories.AccountRepository;
import com.example.csv_json_project.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setCustomer_Id(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setNationalId("1234567890");
        customer.setAddress("123 Main St");
        customer.setBirthDate(Date.valueOf("1996-01-01"));
        customer.setZipCode("12345");
        entityManager.persist(customer);
        entityManager.flush();
    }

    @Test
    void testSaveAndRetrieveAccount() {
       
        Account account = new Account();
        account.setAccountNumber("1234567890123456789012");
        account.setAccountType(1);
        account.setAccountlimit(1000.0);
        account.setBalance(500.0);
        account.setOpenDate(Date.valueOf("2023-01-01"));
        account.setCustomer(customer);

        // Save the account
        accountRepository.save(account);

        // Retrieve the account
        Account retrievedAccount = accountRepository.findById(account.getAccountNumber()).orElse(null);

        // Verify the account
        assertNotNull(retrievedAccount);
        assertEquals(account.getAccountNumber(), retrievedAccount.getAccountNumber());
        assertEquals(account.getAccountType(), retrievedAccount.getAccountType());
        assertEquals(account.getAccountlimit(), retrievedAccount.getAccountlimit());
        assertEquals(account.getBalance(), retrievedAccount.getBalance());
        assertEquals(account.getOpenDate(), retrievedAccount.getOpenDate());
        assertEquals(account.getCustomer().getCustomer_Id(), retrievedAccount.getCustomer().getCustomer_Id());
    }

    @Test
    void testAccountEqualsAndHashCode() {
        // Create two accounts with the same data
        Account account1 = new Account();
        account1.setAccountNumber("1234567890123456789012");
        account1.setAccountType(1);
        account1.setAccountlimit(1000.0);
        account1.setBalance(500.0);
        account1.setOpenDate(Date.valueOf("2023-01-01"));
        account1.setCustomer(customer);

        Account account2 = new Account();
        account2.setAccountNumber("1234567890123456789012");
        account2.setAccountType(1);
        account2.setAccountlimit(1000.0);
        account2.setBalance(500.0);
        account2.setOpenDate(Date.valueOf("2023-01-01"));
        account2.setCustomer(customer);

        // Verify equals and hashCode
        assertEquals(account1, account2);
        assertEquals(account1.hashCode(), account2.hashCode());
    }

    @Test
    void testAccountToString() {
        Account account = new Account();
        account.setAccountNumber("1234567890123456789012");
        account.setAccountType(1);
        account.setAccountlimit(1000.0);
        account.setBalance(500.0);
        account.setOpenDate(Date.valueOf("2023-01-01"));
        account.setCustomer(customer);

    
        String expectedToString = "Account [accountNumber=1234567890123456789012, accountType=1, accountlimit=1000.0, openDate=2023-01-01, balance=500.0, customer=" + customer + "]";
        assertEquals(expectedToString, account.toString());
    }
}