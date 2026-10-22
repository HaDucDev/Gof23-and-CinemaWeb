package behavioral.chainofresponsibility.problem.handler;

import behavioral.chainofresponsibility.problem.model.RefundRequest;

// Chỉ xử lý yêu cầu đã được RefundProcessorBad chọn chuyển tới Manager.
public class Manager {
    public void handle(RefundRequest request) {
        System.out.println("[MANAGER] ĐÃ XỬ LÝ: " + request.getFormattedAmount());
    }
}
