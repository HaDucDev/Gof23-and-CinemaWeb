package com.td.CinemaMini.service.payment.mock;

import java.util.UUID;
import org.springframework.stereotype.Component;

/** Mô phỏng hệ thống thẻ, trả kết quả ở dạng mảng để adapter chuyển đổi. */
@Component
public class CardClient {
    public String[] authorize(long amount, String brand, String action, String original) {
        // amount là số tiền đồng; brand là VISA/MASTERCARD; original là mã giao dịch gốc.
        // Bản mô phỏng luôn trả APPROVED ở [0] và mã tham chiếu mới ở [1].
        return new String[] {"APPROVED", brand + "-" + action + "-" + UUID.randomUUID()};
    }
}
