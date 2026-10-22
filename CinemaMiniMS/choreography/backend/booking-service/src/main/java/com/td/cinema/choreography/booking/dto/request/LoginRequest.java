package com.td.cinema.choreography.booking.dto.request;

import jakarta.validation.constraints.*;

public class LoginRequest {
    @NotBlank @Email public String email;

    @NotBlank
    @Size(max = 72)
    public String password;
}
