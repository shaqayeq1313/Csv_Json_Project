package com.example.csv_json_project.repositoriesTest;

import com.example.csv_json_project.model.Customer;
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
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        Customer customer1 = new Customer();
        customer1.setCustomer_Id(1L);
        customer1.setName("John");
        customer1.setSurname("Doe");
        customer1.setNationalId("1234567890");
        customer1.setAddress("123 Main St");
        customer1.setBirthDate(Date.valueOf("1996-01-01"));
        customer1.setZipCode("12345");

        Customer customer2 = new Customer();
        customer2.setCustomer_Id(2L);
        customer2.setName("Jane");
        customer2.setSurname("Smith");
        customer2.setNationalId("0987654321");
        customer2.setAddress("456 Elm St");
        customer2.setBirthDate(Date.valueOf("1990-05-15"));
        customer2.setZipCode("67890");

        // Save customers
        entityManager.persist(customer1);
        entityManager.persist(customer2);
        entityManager.flush();
    }

    @Test
    void testSaveAndRetrieveCustomer() {
        // Retrieve the customer
        Customer retrievedCustomer = customerRepository.findById(1L).orElse(null);

        // Verify the customer
        assertNotNull(retrievedCustomer);
        assertEquals(1L, retrievedCustomer.getCustomer_Id());
        assertEquals("John", retrievedCustomer.getName());
        assertEquals("Doe", retrievedCustomer.getSurname());
        assertEquals("1234567890", retrievedCustomer.getNationalId());
        assertEquals("123 Main St", retrievedCustomer.getAddress());
        assertEquals(Date.valueOf("1996-01-01"), retrievedCustomer.getBirthDate());
        assertEquals("12345", retrievedCustomer.getZipCode());
    }

    @Test
    void testFindAllCustomers() {
        // Retrieve all customers
        List<Customer> customers = customerRepository.findAll();

        // Verify the customers
        assertNotNull(customers);
        assertEquals(2, customers.size());
    }

    @Test
    void testDeleteCustomer() {
        // Delete the customer
        customerRepository.deleteById(1L);

        // Verify the customer is deleted
        assertFalse(customerRepository.findById(1L).isPresent());
    }
}
