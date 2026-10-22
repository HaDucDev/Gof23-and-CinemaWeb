package behavioral.chainofresponsibility.solution.handler;

import behavioral.chainofresponsibility.solution.model.RefundRequest;

// Kiểu chung cho một cấp xử lý; giữ tham chiếu tới cấp tiếp theo trong chuỗi.
public abstract class RefundHandler {
    // Tham chiếu object xử lý hoàn tiền ở cấp tiếp theo trong chuỗi; null khi chưa nối hoặc đã cuối
    // chuỗi.
    private RefundHandler next;

    public void setNext(RefundHandler next) {
        this.next = next; // Chỉ nối các object có sẵn, chưa xử lý yêu cầu.
    }

    // Mỗi class con tự kiểm tra hạn mức và quyết định xử lý hoặc chuyển tiếp.
    public abstract void handle(RefundRequest request);

    protected void forwardToNext(RefundRequest request) {
        if (next == null) {
            System.out.println(
                    "[HỆ THỐNG] CHƯA XỬ LÝ: "
                            + request.getFormattedAmount()
                            + " (không có cấp tiếp theo).");
            return;
        }
        next.handle(request); // Chuyển cùng request tới object handler tiếp theo.
    }
}
