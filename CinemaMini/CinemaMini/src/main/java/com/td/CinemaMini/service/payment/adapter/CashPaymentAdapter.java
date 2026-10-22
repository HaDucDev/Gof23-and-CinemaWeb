package com.td.CinemaMini.service.payment.adapter;

import com.td.CinemaMini.dto.*;
import com.td.CinemaMini.model.enums.PaymentMethod;
import com.td.CinemaMini.service.payment.PaymentGateway;
import com.td.CinemaMini.service.payment.mock.CashRegister;
import org.springframework.stereotype.Component;

/** GoF: Structural / Adapter. Biên nhận tiền mặt cũng được xác nhận ngay trong demo. */
@Component
public class CashPaymentAdapter implements PaymentGateway {
    private final CashRegister client;

    public CashPaymentAdapter(CashRegister client) {
        this.client = client;
    }

    public boolean supports(PaymentMethod m) {
        // Adapter này chỉ dùng cho phương thức tiền mặt.
        return m == PaymentMethod.CASH;
    }

    public PaymentResult charge(PaymentCommand c) {
        // false là thu tiền vào quầy; ghép UUID biên nhận thành mã giao dịch thu.
        return new PaymentResult("CASH-PAY-" + client.receipt(c.getAmount(), false));
    }

    public PaymentResult refund(RefundCommand c) {
        // true là tiền đi ra để hoàn cho khách; tạo mã biên nhận hoàn riêng.
        return new PaymentResult("CASH-REFUND-" + client.receipt(c.getAmount(), true));
    }
}
