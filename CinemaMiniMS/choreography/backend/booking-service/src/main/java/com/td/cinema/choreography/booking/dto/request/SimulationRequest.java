package com.td.cinema.choreography.booking.dto.request;

import jakarta.validation.constraints.*;

public class SimulationRequest {
    @NotBlank public String outcome;
}
