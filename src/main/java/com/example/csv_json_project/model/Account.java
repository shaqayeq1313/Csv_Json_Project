package com.example.csv_json_project.model;

import java.sql.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

 @Entity
 @Table(name = "ACCOUNT")
 public class Account {
	
    @Id
    @Column(name = "ACCOUNT_NUMBER", unique = true, nullable = false)
    @JsonProperty("account_number")
    private String accountNumber; // Encrypted field

    @JsonProperty("account_type")
    private int accountType;  // 1: savings, 2: recurring deposit, 3: fixed deposit
    
    @JsonProperty("account_limit")
    private double accountlimit;
    
    @Temporal(TemporalType.DATE)
    @JsonProperty("open_date")
    private Date openDate;

    @JsonProperty("balance")
    private double balance; // Encrypted field

    
    public Account(String accountNumber, int accountType, double accountlimit, Date openDate, double balance,
			Customer customer) {
		super();
		this.accountNumber = accountNumber;
		this.accountType = accountType;
		this.accountlimit = accountlimit;
		this.openDate = openDate;
		this.balance = balance;
		this.customer = customer;
	}

	public Account() {
	}

	@ManyToOne
    @JoinColumn(name = "customer_id")
	@JsonIgnore
    @JsonProperty("customer")
    private Customer customer;


	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public int getAccountType() {
		return accountType;
	}

	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}

	public double getAccountlimit() {
		return accountlimit;
	}

	public void setAccountlimit(double accountlimit) {
		this.accountlimit = accountlimit;
	}

	public Date getOpenDate() {
		return openDate;
	}

	public void setOpenDate(Date openDate) {
		this.openDate = openDate;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@Override
	public int hashCode() {
		return Objects.hash(accountNumber, accountType, balance, customer, accountlimit, openDate);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
		return Objects.equals(accountNumber, other.accountNumber) && accountType == other.accountType
				&& Double.doubleToLongBits(balance) == Double.doubleToLongBits(other.balance)
				&& Objects.equals(customer, other.customer)
				&& Double.doubleToLongBits(accountlimit) == Double.doubleToLongBits(other.accountlimit)
				&& Objects.equals(openDate, other.openDate);
	}

	@Override
	public String toString() {
		return "Account [accountNumber=" + accountNumber + ", accountType=" + accountType + ", accountlimit=" + accountlimit
				+ ", openDate=" + openDate + ", balance=" + balance + ", customer=" + customer + "]";
	}

 }

