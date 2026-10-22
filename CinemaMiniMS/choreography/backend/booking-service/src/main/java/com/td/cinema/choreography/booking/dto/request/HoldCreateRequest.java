package com.td.cinema.choreography.booking.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

public class HoldCreateRequest {
    @NotBlank public String showtimeId;
    @NotNull public List<String> seatNos;
}
