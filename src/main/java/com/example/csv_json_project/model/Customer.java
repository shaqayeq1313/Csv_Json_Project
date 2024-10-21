package com.example.csv_json_project.model;

import java.sql.Date;
import java.util.List;
import java.util.Objects;

import com.example.csv_json_project.springSecurity.EncryptionUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "CUSTOMER")
public class Customer {

    @Id
    @Column(name = "CUSTOMER_ID", unique = true, nullable = false)
    @JsonProperty("customer_id")
    private Long customer_Id;

    @JsonProperty("name")
    private String name; // Encrypted field

    @JsonProperty("surname")
    private String surname; // Encrypted field

    @JsonProperty("address")
    private String address;

    @JsonProperty("zipCode")
    private String zipCode;

    @JsonProperty("national_id")
    private String nationalId; // Encrypted field

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JacksonXmlProperty(localName = "birthDate")
    private Date birthDate;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    @JsonProperty("accounts")
    private List<Account> accounts;

    public Customer(Long customer_Id, String name, String surname, String address, String zipCode, String nationalId,
                    Date birthDate, List<Account> accounts) {
        super();
        this.customer_Id = customer_Id;
        this.name = name;
        this.surname = surname;
        this.address = address;
        this.zipCode = zipCode;
        this.nationalId = nationalId;
        this.birthDate = birthDate;
        this.accounts = accounts;
    }

    public Customer() {
    }

	public Long getCustomer_Id() {
		return customer_Id;
	}

	public void setCustomer_Id(Long customer_Id) {
		this.customer_Id = customer_Id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getNationalId() {
		return nationalId;
	}

	public void setNationalId(String nationalId) {
		this.nationalId = nationalId;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public List<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}

	@Override
	public int hashCode() {
		return Objects.hash(accounts, address, birthDate, customer_Id, name, nationalId, surname, zipCode);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Customer other = (Customer) obj;
		return Objects.equals(accounts, other.accounts) && Objects.equals(address, other.address)
				&& Objects.equals(birthDate, other.birthDate) && Objects.equals(customer_Id, other.customer_Id)
				&& Objects.equals(name, other.name) && Objects.equals(nationalId, other.nationalId)
				&& Objects.equals(surname, other.surname) && Objects.equals(zipCode, other.zipCode);
	}

	@Override
	public String toString() {
		return "Customer [customer_Id=" + customer_Id + ", name=" + name + ", surname=" + surname + ", address="
				+ address + ", zipCode=" + zipCode + ", nationalId=" + nationalId + ", birthDate=" + birthDate
				+ ", accounts=" + accounts + "]";
	}
}