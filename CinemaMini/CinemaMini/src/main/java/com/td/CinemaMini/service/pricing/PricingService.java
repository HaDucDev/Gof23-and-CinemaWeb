package com.td.CinemaMini.service.pricing;

import com.td.CinemaMini.dto.PriceBreakdown;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.CustomerType;
import com.td.CinemaMini.model.enums.SeatType;
import com.td.CinemaMini.service.pricing.strategy.PricingStrategy;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;

/** Tính giá ghế của suất chiếu và áp dụng chính sách ưu đãi từ hồ sơ khách hàng. */
@Service
public class PricingService {
    private final Map<CustomerType, PricingStrategy> strategies =
            new EnumMap<CustomerType, PricingStrategy>(CustomerType.class);

    public PricingService(List<PricingStrategy> strategies) {
        // Lập bảng tra theo nhóm khách để mỗi lần tính giá chọn đúng chính sách đã đăng ký.
        for (PricingStrategy strategy : strategies) {
            this.strategies.put(strategy.type(), strategy);
        }
    }

    public BigDecimal unitPrice(Showtime showtime, Seat seat) {
        // Giá một ghế trước ưu đãi bằng giá cơ bản của suất, cộng phụ thu nếu ghế là VIP.
        return showtime.getBasePrice()
                .add(
                        seat.getSeatType() == SeatType.VIP
                                ? showtime.getVipSurcharge()
                                : BigDecimal.ZERO);
    }

    public PriceBreakdown calculate(Customer customer, Showtime showtime, List<Seat> seats) {
        // Cộng giá từng ghế trước, sau đó áp dụng ưu đãi một lần trên tổng để thống nhất làm tròn.
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Seat seat : seats) {
            subtotal = subtotal.add(unitPrice(showtime, seat));
        }
        // GoF: Strategy được chọn từ hồ sơ DB, không nhận nhóm khách/giá từ form.
        return strategies.get(customer.getCustomerType()).calculate(subtotal);
    }
}
