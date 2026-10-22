package com.td.CinemaMini.service.pricing.strategy;

import com.td.CinemaMini.dto.PriceBreakdown;
import com.td.CinemaMini.model.enums.CustomerType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Chính sách cho khách có nhóm STUDENT trong hồ sơ: giảm 10% tổng giá ghế. */
@Component
public class StudentPricingStrategy implements PricingStrategy {
    public CustomerType type() {
        return CustomerType.STUDENT;
    }

    public PriceBreakdown calculate(BigDecimal subtotal) {
        // Giá trị 10 là 10%; PriceBreakdown làm tròn số tiền phải trả đến một đồng.
        return new PriceBreakdown(subtotal, new BigDecimal("10"), type());
    }
}
