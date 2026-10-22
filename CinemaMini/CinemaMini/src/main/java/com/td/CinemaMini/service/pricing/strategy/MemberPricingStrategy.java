package com.td.CinemaMini.service.pricing.strategy;

import com.td.CinemaMini.dto.PriceBreakdown;
import com.td.CinemaMini.model.enums.CustomerType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Chính sách cho khách có nhóm MEMBER trong hồ sơ: giảm 15% tổng giá ghế. */
@Component
public class MemberPricingStrategy implements PricingStrategy {
    public CustomerType type() {
        return CustomerType.MEMBER;
    }

    public PriceBreakdown calculate(BigDecimal subtotal) {
        // Giá trị 15 là 15%; PriceBreakdown làm tròn số tiền phải trả đến một đồng.
        return new PriceBreakdown(subtotal, new BigDecimal("15"), type());
    }
}
