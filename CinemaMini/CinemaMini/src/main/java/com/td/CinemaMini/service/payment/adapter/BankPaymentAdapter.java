package com.td.CinemaMini.service.payment.adapter;

import com.td.CinemaMini.dto.*;
import com.td.CinemaMini.model.enums.PaymentMethod;
import com.td.CinemaMini.service.payment.PaymentGateway;
import com.td.CinemaMini.service.payment.mock.BankClient;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Component;

/** GoF: Structural / Adapter. Command chung -> bản tin ngân hàng -> PaymentResult. */
@Component
public class BankPaymentAdapter implements PaymentGateway {
    private final BankClient client;

    public BankPaymentAdapter(BankClient client) {
        this.client = client;
    }

    public boolean supports(PaymentMethod m) {
        // Adapter ngân hàng xử lý chuyển khoản và ATM nội địa.
        return m == PaymentMethod.BANK_TRANSFER || m == PaymentMethod.DOMESTIC_ATM;
    }

    public PaymentResult charge(PaymentCommand c) {
        // Dòng thu mới chưa có giao dịch gốc để tham chiếu nên truyền chuỗi rỗng.
        return submit(c.getAmount(), c.getMethod(), "PAYMENT", "");
    }

    public PaymentResult refund(RefundCommand c) {
        // Dòng hoàn gửi kèm mã giao dịch đã thu để phía ngân hàng nhận diện khoản cần hoàn.
        return submit(c.getAmount(), c.getMethod(), "REFUND", c.getOriginalReference());
    }

    private PaymentResult submit(
            BigDecimal amount, PaymentMethod method, String operation, String original) {
        // Client ngân hàng nhận bản tin Map: số tiền, phương thức, thao tác và mã gốc.
        Map<String, String> message = new HashMap<String, String>();
        message.put("amountVnd", amount.toPlainString());
        message.put("channel", method.name());
        message.put("operation", operation);
        message.put("original", original);
        // Lấy bankTrace từ kết quả Map và chuyển thành PaymentResult chung cho service.
        return new PaymentResult(client.submit(message).get("bankTrace"));
    }
}
