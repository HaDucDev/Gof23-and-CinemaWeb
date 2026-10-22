package com.td.cinema.choreography.account.dto.projection;

import com.td.cinema.choreography.account.model.AccountRole;
import com.td.cinema.choreography.account.model.CustomerType;

public class AccountProfileView implements ProjectionRow {
    private final Long accountId;
    private final String email;
    private final String role;
    private final Boolean active;
    private final Long customerId;
    private final String fullName;
    private final String customerType;

    public AccountProfileView(
            Long accountId,
            String email,
            AccountRole role,
            Boolean active,
            Long customerId,
            String fullName,
            CustomerType customerType) {
        this.accountId = accountId;
        this.email = email;
        this.role = role.name();
        this.active = active;
        this.customerId = customerId;
        this.fullName = fullName;
        this.customerType = customerType == null ? null : customerType.name();
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Boolean getActive() {
        return active;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCustomerType() {
        return customerType;
    }
}
