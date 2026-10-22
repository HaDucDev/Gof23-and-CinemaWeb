package com.td.cinema.account.entity;

import com.td.cinema.account.model.CustomerType;

import jakarta.persistence.*;

@Entity
@Table(name = "customers", schema = "account")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long value) {
        this.customerId = value;
    }

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long value) {
        this.accountId = value;
    }

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String value) {
        this.fullName = value;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 16)
    private CustomerType customerType = CustomerType.REGULAR;

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType value) {
        this.customerType = value;
    }
}
