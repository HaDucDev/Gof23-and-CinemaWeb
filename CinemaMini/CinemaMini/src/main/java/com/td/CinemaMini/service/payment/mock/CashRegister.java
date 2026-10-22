package com.td.CinemaMini.service.payment.mock;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Mô phỏng quầy tiền mặt bằng cách sinh mã biên nhận. */
@Component
public class CashRegister {
    public UUID receipt(BigDecimal amount, boolean outgoing) {
        // amount là số tiền; outgoing = true là hoàn ra, false là thu vào.
        // Hiện chỉ sinh UUID biên nhận, chưa theo dõi tiền tồn quầy.
        return UUID.randomUUID();
    }
}
