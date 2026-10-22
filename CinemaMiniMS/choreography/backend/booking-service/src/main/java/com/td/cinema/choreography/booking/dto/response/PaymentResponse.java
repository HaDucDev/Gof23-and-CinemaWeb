package com.td.cinema.choreography.booking.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentResponse {
    public String paymentId;
    public String bookingId;
    public BigDecimal amount;
    public String status;
    public String idempotencyKey;
    public String requestHash;
    public String paymentMethod;
    public String simulatedOutcome;
    public String failureReason;
    public String createdAt;
    public String updatedAt;
}
