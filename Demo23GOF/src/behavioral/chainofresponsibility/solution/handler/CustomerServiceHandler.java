package behavioral.chainofresponsibility.solution.handler;

import behavioral.chainofresponsibility.solution.model.RefundRequest;

// Cấp CS: chỉ biết hạn mức của mình là 1.000.000 VND.
public class CustomerServiceHandler extends RefundHandler {
    @Override
    public void handle(RefundRequest request) {
        System.out.println("[CS] Kiểm tra hạn mức hoàn tiền...");
        if (request.getAmount() <= 1_000_000) {
            System.out.println("[CS] ĐÃ XỬ LÝ: " + request.getFormattedAmount());
            return; // Đã xử lý thì dừng, không chuyển lên cấp tiếp theo.
        }

        System.out.println(
                "[CS] Số tiền vượt hạn mức của nhân viên -> chuyển yêu cầu lên cấp tiếp theo");
        forwardToNext(request); // Hàm ở lớp cha gọi next.handle(request).
    }
}
