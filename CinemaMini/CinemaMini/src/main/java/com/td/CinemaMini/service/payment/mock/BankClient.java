package com.td.CinemaMini.service.payment.mock;

import java.util.*;
import org.springframework.stereotype.Component;

/** Adaptee khác ví: nhận một bản tin và trả mã tham chiếu trong Map. */
@Component
public class BankClient {
    public Map<String, String> submit(Map<String, String> message) {
        // Bộ mô phỏng tạo mã bankTrace theo thao tác PAYMENT/REFUND; không gọi ngân hàng thật.
        Map<String, String> result = new HashMap<String, String>();
        result.put("bankTrace", "BANK-" + message.get("operation") + "-" + UUID.randomUUID());
        return result;
    }
}
