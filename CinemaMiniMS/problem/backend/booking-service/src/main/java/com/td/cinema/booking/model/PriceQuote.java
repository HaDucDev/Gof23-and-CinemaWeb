package com.td.cinema.booking.model;

import java.math.BigDecimal;

public class PriceQuote {
    public final String customerType;
    public final int discountPercent;
    public final BigDecimal subtotal;
    public final BigDecimal total;

    public PriceQuote(
            String customerType, int discountPercent, BigDecimal subtotal, BigDecimal total) {
        this.customerType = customerType;
        this.discountPercent = discountPercent;
        this.subtotal = subtotal;
        this.total = total;
    }
}
