package com.td.cinema.choreography.account.dto.request;

import jakarta.validation.constraints.*;

public class LoginRequest {
    @NotBlank @Email public String email;

    @NotBlank
    @Size(max = 72)
    public String password;
}
