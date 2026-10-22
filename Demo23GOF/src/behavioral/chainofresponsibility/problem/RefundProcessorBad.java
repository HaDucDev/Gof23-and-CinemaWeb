package behavioral.chainofresponsibility.problem;

import behavioral.chainofresponsibility.problem.handler.CustomerServiceStaff;
import behavioral.chainofresponsibility.problem.handler.Manager;
import behavioral.chainofresponsibility.problem.handler.TeamLeader;
import behavioral.chainofresponsibility.problem.model.RefundRequest;

// PROBLEM: một nơi phải biết hạn mức và chọn người xử lý cho tất cả các cấp.
public class RefundProcessorBad {
    // Cấp nhân viên, được chọn khi số tiền <= 1 triệu VND.
    private final CustomerServiceStaff staff = new CustomerServiceStaff();
    // Cấp trưởng nhóm, được chọn khi số tiền > 1 và <= 10 triệu VND.
    private final TeamLeader leader = new TeamLeader();
    // Cấp quản lý xử lý các yêu cầu vượt 10 triệu VND.
    private final Manager manager = new Manager();

    public void process(RefundRequest request) {
        System.out.println("[HỆ THỐNG] Kiểm tra hạn mức để chọn cấp xử lý...");
        long amount = request.getAmount();

        // Đổi hạn mức hoặc thêm cấp xử lý thì phải sửa khối lựa chọn chung này.
        if (amount <= 1_000_000) {
            staff.handle(request);
        } else if (amount <= 10_000_000) {
            leader.handle(request);
        } else {
            manager.handle(request);
        }
    }
}
