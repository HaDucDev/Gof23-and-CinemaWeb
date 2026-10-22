package com.td.CinemaMini.service.payment.adapter;

import com.td.CinemaMini.dto.*;
import com.td.CinemaMini.model.enums.PaymentMethod;
import com.td.CinemaMini.service.payment.PaymentGateway;
import com.td.CinemaMini.service.payment.mock.CardClient;
import org.springframework.stereotype.Component;

/** GoF: Structural / Adapter. Chuyển kết quả mảng của bộ mô phỏng thẻ. */
@Component
public class CardPaymentAdapter implements PaymentGateway {
    private final CardClient client;

    public CardPaymentAdapter(CardClient client) {
        this.client = client;
    }

    public boolean supports(PaymentMethod m) {
        // Adapter thẻ dùng cho hai phương thức VISA và MASTERCARD.
        return m == PaymentMethod.VISA || m == PaymentMethod.MASTERCARD;
    }

    /**
     * Client nhận số nguyên đồng; longValueExact() không cho âm thầm bỏ phần lẻ.
     * Mảng trả về có phần tử [1] là mã tham chiếu để tạo PaymentResult chung.
     */
    public PaymentResult charge(PaymentCommand c) {
        return new PaymentResult(
                client.authorize(
                                c.getAmount().longValueExact(),
                                c.getMethod().name(),
                                "PAYMENT",
                                "")[1]);
    }

    /** Gửi thao tác REFUND kèm mã giao dịch gốc, rồi lấy mã hoàn ở phần tử [1]. */
    public PaymentResult refund(RefundCommand c) {
        return new PaymentResult(
                client.authorize(
                                c.getAmount().longValueExact(),
                                c.getMethod().name(),
                                "REFUND",
                                c.getOriginalReference())[1]);
    }
}
