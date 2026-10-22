package behavioral.chainofresponsibility.problem.handler;

import behavioral.chainofresponsibility.problem.model.RefundRequest;

// Chỉ xử lý yêu cầu đã được RefundProcessorBad chọn chuyển tới Trưởng nhóm.
public class TeamLeader {
    public void handle(RefundRequest request) {
        System.out.println("[TRƯỞNG NHÓM] ĐÃ XỬ LÝ: " + request.getFormattedAmount());
    }
}
