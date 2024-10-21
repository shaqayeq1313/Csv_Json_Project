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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CustomerTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setAccountNumber("1234567890123456789012");
        account.setAccountType(1);
        account.setAccountlimit(1000.0);
        account.setBalance(500.0);
        account.setOpenDate(Date.valueOf("2023-01-01"));
        entityManager.persist(account);
        entityManager.flush();
    }

    @Test
    void testSaveAndRetrieveCustomer() {
        Customer customer = new Customer();
        customer.setCustomer_Id(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setAddress("123 Main St");
        customer.setZipCode("12345");
        customer.setNationalId("1234567890");
        customer.setBirthDate(Date.valueOf("1996-01-01"));
        customer.setAccounts(List.of(account));

        // Save the customer
        customerRepository.save(customer);

        // Retrieve the customer
        Customer retrievedCustomer = customerRepository.findById(customer.getCustomer_Id()).orElse(null);

        // Verify the customer
        assertNotNull(retrievedCustomer);
        assertEquals(customer.getCustomer_Id(), retrievedCustomer.getCustomer_Id());
        assertEquals(customer.getName(), retrievedCustomer.getName());
        assertEquals(customer.getSurname(), retrievedCustomer.getSurname());
        assertEquals(customer.getAddress(), retrievedCustomer.getAddress());
        assertEquals(customer.getZipCode(), retrievedCustomer.getZipCode());
        assertEquals(customer.getNationalId(), retrievedCustomer.getNationalId());
        assertEquals(customer.getBirthDate(), retrievedCustomer.getBirthDate());
        assertEquals(customer.getAccounts().size(), retrievedCustomer.getAccounts().size());
    }

    @Test
    void testCustomerEqualsAndHashCode() {
        // Create two customers with the same data
        Customer customer1 = new Customer();
        customer1.setCustomer_Id(1L);
        customer1.setName("John");
        customer1.setSurname("Doe");
        customer1.setAddress("123 Main St");
        customer1.setZipCode("12345");
        customer1.setNationalId("1234567890");
        customer1.setBirthDate(Date.valueOf("1996-01-01"));
        customer1.setAccounts(List.of(account));

        Customer customer2 = new Customer();
        customer2.setCustomer_Id(1L);
        customer2.setName("John");
        customer2.setSurname("Doe");
        customer2.setAddress("123 Main St");
        customer2.setZipCode("12345");
        customer2.setNationalId("1234567890");
        customer2.setBirthDate(Date.valueOf("1996-01-01"));
        customer2.setAccounts(List.of(account));

        // Verify equals and hashCode
        assertEquals(customer1, customer2);
        assertEquals(customer1.hashCode(), customer2.hashCode());
    }

    @Test
    void testCustomerToString() {
        Customer customer = new Customer();
        customer.setCustomer_Id(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setAddress("123 Main St");
        customer.setZipCode("12345");
        customer.setNationalId("1234567890");
        customer.setBirthDate(Date.valueOf("1996-01-01"));
        customer.setAccounts(List.of(account));

      
        String expectedToString = "Customer [customer_Id=1, name=John, surname=Doe, address=123 Main St, zipCode=12345, nationalId=1234567890, birthDate=1996-01-01, accounts=" + customer.getAccounts() + "]";
        assertEquals(expectedToString, customer.toString());
    }
}