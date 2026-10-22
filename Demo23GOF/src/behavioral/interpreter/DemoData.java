package behavioral.interpreter;

import java.util.List;

/** Các phiếu mẫu dùng chung để đối chiếu kết quả diễn giải giữa Problem và Solution. */
public final class DemoData {
    // Danh sách chuỗi phiếu gọi đồ uống theo cấu trúc Món-Size[-Tùy chọn...], ví dụ TS-L-D30-I0-TC.
    public static final List<String> ORDERS =
            List.of("TS-L-D30-I0-TC", "TX-M-D0-I0", "CP-S-HOT", "NC-L-I100");
    // Phiếu mở rộng: ST0 nghĩa là không lấy ống hút.
    public static final String STRAW_ORDER = "TS-L-D30-I0-TC-ST0";

    private DemoData() {
    }
}
