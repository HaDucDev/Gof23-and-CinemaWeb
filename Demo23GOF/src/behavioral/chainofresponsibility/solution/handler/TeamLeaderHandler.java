package behavioral.chainofresponsibility.solution.handler;

import behavioral.chainofresponsibility.solution.model.RefundRequest;

// Cấp Trưởng nhóm: chỉ biết hạn mức của mình là 10.000.000 VND.
public class TeamLeaderHandler extends RefundHandler {
    @Override
    public void handle(RefundRequest request) {
        System.out.println("[TRƯỞNG NHÓM] Kiểm tra hạn mức hoàn tiền...");
        if (request.getAmount() <= 10_000_000) {
            System.out.println("[TRƯỞNG NHÓM] ĐÃ XỬ LÝ: " + request.getFormattedAmount());
            return; // Dừng chuỗi khi yêu cầu đã được xử lý.
        }

        System.out.println(
                "[TRƯỞNG NHÓM] Số tiền vượt hạn mức của trưởng nhóm -> chuyển yêu cầu lên cấp tiếp theo");
        forwardToNext(request); // Hàm ở lớp cha gọi next.handle(request).
    }
}
