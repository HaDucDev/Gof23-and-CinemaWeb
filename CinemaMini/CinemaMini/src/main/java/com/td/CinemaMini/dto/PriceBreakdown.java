package com.td.CinemaMini.dto;

import com.td.CinemaMini.model.enums.CustomerType;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PriceBreakdown {
    private final BigDecimal subtotal;
    private final BigDecimal discountPercent;
    private final BigDecimal discountAmount;
    private final BigDecimal total;
    private final CustomerType policy;

    public PriceBreakdown(BigDecimal subtotal, BigDecimal discountPercent, CustomerType policy) {
        this.subtotal = subtotal;
        this.discountPercent = discountPercent;
        this.policy = policy;
        total =
                subtotal.multiply(BigDecimal.ONE.subtract(discountPercent.movePointLeft(2)))
                        .setScale(0, RoundingMode.HALF_UP);
        discountAmount = subtotal.subtract(total);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public CustomerType getPolicy() {
        return policy;
    }
}
