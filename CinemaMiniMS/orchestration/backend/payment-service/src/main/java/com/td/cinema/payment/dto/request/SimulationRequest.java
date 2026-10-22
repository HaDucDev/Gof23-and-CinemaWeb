package com.td.cinema.payment.dto.request;

import jakarta.validation.constraints.*;

public class SimulationRequest {
    @NotBlank public String outcome;
}
