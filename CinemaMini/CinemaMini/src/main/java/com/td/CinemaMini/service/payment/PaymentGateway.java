package com.td.CinemaMini.service.payment;

import com.td.CinemaMini.dto.*;
import com.td.CinemaMini.model.enums.PaymentMethod;

/**
 * Giao diện lập trình chung (interface/API) để service thu/hoàn tiền qua các adapter.
 * PaymentService nhận cùng kiểu yêu cầu và kết quả, dù client mô phỏng bên dưới khác nhau.
 */
public interface PaymentGateway {
    /** Cho biết adapter có xử lý được mã phương thức thanh toán này hay không. */
    boolean supports(PaymentMethod method);

    /** Thu số tiền của đơn và trả về mã tham chiếu giao dịch. */
    PaymentResult charge(PaymentCommand command);

    /** Hoàn số tiền dương theo thông tin giao dịch gốc và trả mã tham chiếu mới. */
    PaymentResult refund(RefundCommand command);
}
