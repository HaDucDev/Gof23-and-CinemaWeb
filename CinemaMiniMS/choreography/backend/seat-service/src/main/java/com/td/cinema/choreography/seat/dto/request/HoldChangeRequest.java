package com.td.cinema.choreography.seat.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

public class HoldChangeRequest extends HoldReleaseRequest {
    @NotNull public List<String> seatNos;
}
