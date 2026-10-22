package com.td.CinemaMini.service.payment.adapter;

import com.td.CinemaMini.dto.*;
import com.td.CinemaMini.model.enums.PaymentMethod;
import com.td.CinemaMini.service.payment.PaymentGateway;
import com.td.CinemaMini.service.payment.mock.WalletClient;
import org.springframework.stereotype.Component;

/** GoF: Structural / Adapter. Command chung -> API ví -> PaymentResult. */
@Component
public class WalletPaymentAdapter implements PaymentGateway {
    private final WalletClient client;

    public WalletPaymentAdapter(WalletClient client) {
        this.client = client;
    }

    public boolean supports(PaymentMethod m) {
        // Ba mã MOMO, ZALOPAY và VNPAY hiện cùng sử dụng client mô phỏng ví.
        return m == PaymentMethod.MOMO || m == PaymentMethod.ZALOPAY || m == PaymentMethod.VNPAY;
    }

    /** Đổi số tiền sang số nguyên đồng và mã phương thức sang chuỗi trước khi gọi ví. */
    public PaymentResult charge(PaymentCommand c) {
        return new PaymentResult(
                client.debit(c.getMethod().name(), c.getAmount().longValueExact()));
    }

    /** Gửi mã ví, mã giao dịch gốc và số tiền hoàn dương để nhận biên nhận hoàn. */
    public PaymentResult refund(RefundCommand c) {
        return new PaymentResult(
                client.reverse(
                        c.getMethod().name(),
                        c.getOriginalReference(),
                        c.getAmount().longValueExact()));
    }
}
