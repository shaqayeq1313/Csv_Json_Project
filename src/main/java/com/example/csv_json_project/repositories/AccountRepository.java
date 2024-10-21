package com.example.csv_json_project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.csv_json_project.model.Account;

public interface AccountRepository extends JpaRepository<Account, String> {
}

