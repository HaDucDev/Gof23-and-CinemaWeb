package com.td.cinema.choreography.account.dto.request;

import jakarta.validation.constraints.*;

public class RegisterRequest {
    @NotBlank
    @Email
    @Size(max = 254)
    public String email;

    @NotBlank
    @Size(min = 8, max = 72)
    public String password;

    @NotBlank
    @Size(max = 120)
    public String fullName;
}
