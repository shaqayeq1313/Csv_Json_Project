package com.example.csv_json_project.repositories;

import com.example.csv_json_project.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
