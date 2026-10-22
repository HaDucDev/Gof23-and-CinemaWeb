package com.td.cinema.booking.service;

import com.td.cinema.booking.exception.ApiException;
import com.td.cinema.booking.model.PriceQuote;
import com.td.cinema.booking.util.Data;

import org.springframework.stereotype.Service;

import java.math.*;
import java.util.*;

/** Tính giá từ dữ liệu ghế và nhóm khách do backend cung cấp, không nhận tổng tiền do trình duyệt tự tính. */
@Service
public class PricingService {
    /** Cộng giá ghế và phụ thu VIP, áp dụng ưu đãi 0/10/15% theo nhóm khách rồi làm tròn đến đồng. */
    public PriceQuote quote(String customerType, List<Map<String, Object>> seats) {
        if (!Arrays.asList("REGULAR", "STUDENT", "MEMBER").contains(customerType))
            throw ApiException.conflict("Nhom khach hang khong hop le.");
        int percent = "STUDENT".equals(customerType) ? 10 : "MEMBER".equals(customerType) ? 15 : 0;
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map<String, Object> seat : seats)
            subtotal =
                    subtotal.add(Data.money(seat, "basePrice"))
                            .add(Data.money(seat, "vipSurcharge"));
        BigDecimal total =
                subtotal.multiply(BigDecimal.valueOf(100 - percent))
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        return new PriceQuote(customerType, percent, subtotal, total);
    }
}
