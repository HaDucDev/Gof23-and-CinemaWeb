package com.td.cinema.choreography.account.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountProfileResponse {
    public long accountId;
    public String email;
    public String role;
    public boolean active;
    public Long customerId;
    public String fullName;
    public String customerType;
}
