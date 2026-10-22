package behavioral.chainofresponsibility.solution.handler;

import behavioral.chainofresponsibility.solution.model.RefundRequest;

// Cấp cuối: xử lý yêu cầu còn lại sau khi CS và Trưởng nhóm đã chuyển tiếp.
public class ManagerHandler extends RefundHandler {
    @Override
    public void handle(RefundRequest request) {
        System.out.println("[MANAGER] ĐÃ XỬ LÝ: " + request.getFormattedAmount());
    }
}
