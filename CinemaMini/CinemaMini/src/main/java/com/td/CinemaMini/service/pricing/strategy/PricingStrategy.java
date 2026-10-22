package com.td.CinemaMini.service.pricing.strategy;

import com.td.CinemaMini.dto.PriceBreakdown;
import com.td.CinemaMini.model.enums.CustomerType;
import java.math.BigDecimal;

/** GoF: Behavioral / Strategy. Thuật toán giá thay thế theo hồ sơ phía server. */
public interface PricingStrategy {
    /** Nhóm khách mà chính sách này áp dụng: khách thường, sinh viên hoặc thành viên. */
    CustomerType type();

    /** Nhận tổng tiền trước ưu đãi, trả chi tiết tỷ lệ giảm, tiền giảm và số tiền phải trả. */
    PriceBreakdown calculate(BigDecimal subtotal);
}
