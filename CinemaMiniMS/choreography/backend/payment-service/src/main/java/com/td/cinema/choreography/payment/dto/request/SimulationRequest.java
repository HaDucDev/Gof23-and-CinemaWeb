package com.td.cinema.choreography.payment.dto.request;

import jakarta.validation.constraints.*;

public class SimulationRequest {
    @NotBlank public String outcome;
}
