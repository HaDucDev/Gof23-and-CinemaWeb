package com.td.CinemaMini.service.payment.mock;

import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptee: API ví nhận mã ví, số nguyên đồng và trả chuỗi biên nhận mô phỏng. */
@Component
public class WalletClient {
    public String debit(String wallet, long dong) {
        // wallet là mã ví, dong là số tiền VND nguyên; bản mô phỏng chỉ sinh biên nhận thu.
        return wallet + "-PAY-" + UUID.randomUUID();
    }

    public String reverse(String wallet, String receipt, long dong) {
        // receipt là biên nhận thu gốc; bản mô phỏng sinh mã hoàn mới, không gọi dịch vụ ví thật.
        return wallet + "-REFUND-" + UUID.randomUUID();
    }
}
