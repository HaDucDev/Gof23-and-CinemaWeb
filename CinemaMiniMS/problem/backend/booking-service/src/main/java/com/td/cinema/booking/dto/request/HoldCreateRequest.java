package com.td.cinema.booking.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

public class HoldCreateRequest {
    @NotBlank public String showtimeId;
    @NotNull public List<String> seatNos;
}
