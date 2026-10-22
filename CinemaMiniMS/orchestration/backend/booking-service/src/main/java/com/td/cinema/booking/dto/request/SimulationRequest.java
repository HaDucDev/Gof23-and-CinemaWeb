package com.td.cinema.booking.dto.request;

import jakarta.validation.constraints.*;

public class SimulationRequest {
    @NotBlank public String outcome;
}
