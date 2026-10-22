package com.td.cinema.choreography.seat.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class ShowtimeCreateRequest {
    @Min(1)
    public long movieId;

    @NotBlank public String startsAt;

    @NotNull
    @DecimalMin("1")
    public BigDecimal basePrice;

    @NotNull
    @DecimalMin("0")
    public BigDecimal vipSurcharge;

    public List<String> disabledSeatNos;
}
