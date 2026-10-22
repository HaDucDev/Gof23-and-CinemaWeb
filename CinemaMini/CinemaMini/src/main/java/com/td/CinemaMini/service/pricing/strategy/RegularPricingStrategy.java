package com.td.CinemaMini.service.pricing.strategy;

import com.td.CinemaMini.dto.PriceBreakdown;
import com.td.CinemaMini.model.enums.CustomerType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Chính sách cho khách thường: không giảm giá vé. */
@Component
public class RegularPricingStrategy implements PricingStrategy {
    public CustomerType type() {
        return CustomerType.REGULAR;
    }

    public PriceBreakdown calculate(BigDecimal subtotal) {
        // Tỷ lệ giảm 0%; PriceBreakdown tính tổng thanh toán và lưu nhóm khách đã áp dụng.
        return new PriceBreakdown(subtotal, BigDecimal.ZERO, type());
    }
}
