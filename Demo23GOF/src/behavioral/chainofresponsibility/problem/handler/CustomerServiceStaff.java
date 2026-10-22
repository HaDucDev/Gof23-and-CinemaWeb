package behavioral.chainofresponsibility.problem.handler;

import behavioral.chainofresponsibility.problem.model.RefundRequest;

// Chỉ xử lý yêu cầu đã được RefundProcessorBad chọn chuyển tới CS.
public class CustomerServiceStaff {
    public void handle(RefundRequest request) {
        System.out.println("[CS] ĐÃ XỬ LÝ: " + request.getFormattedAmount());
    }
}
