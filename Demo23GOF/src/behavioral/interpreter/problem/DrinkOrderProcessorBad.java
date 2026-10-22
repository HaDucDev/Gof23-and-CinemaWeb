package behavioral.interpreter.problem;

import behavioral.interpreter.model.OrderResult;
import java.util.Map;
import java.util.Set;

/** Toàn bộ nhận diện, diễn giải và kiểm tra quy tắc nằm trong bộ xử lý này. */
public final class DrinkOrderProcessorBad {
    // Bảng tra mã đồ uống -> tên đồ uống, ví dụ TS -> Trà sữa; nằm ngay trong bộ xử lý.
    private static final Map<String, String> DRINKS =
            Map.of("TS", "Trà sữa", "TX", "Trà xanh", "CP", "Cà phê", "NC", "Nước cam");
    // Tập hợp các mã kích cỡ đồ uống hợp lệ.
    private static final Set<String> SIZES = Set.of("S", "M", "L");
    // Khóa là mã lượng đường (D0, D30...); giá trị là mô tả lượng đường (Không đường, 30%
    // đường...).
    private static final Map<String, String> SUGARS =
            Map.of(
                    "D0",
                    "Không đường",
                    "D30",
                    "30% đường",
                    "D50",
                    "50% đường",
                    "D70",
                    "70% đường",
                    "D100",
                    "100% đường");
    // Khóa là mã lượng đá (I0, I50, I100); giá trị là mô tả lượng đá (Không đá, Ít đá, Nhiều đá).
    private static final Map<String, String> ICE =
            Map.of("I0", "Không đá", "I50", "Ít đá", "I100", "Nhiều đá");

    // Một phương thức đảm nhiệm cả tách phiếu, nhận diện nhóm, dịch mã và kiểm tra toàn đơn.
    public OrderResult process(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            throw new IllegalArgumentException("Phiếu không được để trống");
        }
        // -1 giữ cả thành phần rỗng cuối phiếu để phát hiện dấu '-' thừa.
        // Mảng mã trên phiếu: mã đồ uống, mã kích cỡ, rồi các mã tùy chọn.
        String[] tokens = ticket.split("-", -1);
        if (tokens.length < 2) {
            throw new IllegalArgumentException(
                    "Phiếu phải có món và size theo cấu trúc Món-Size[-Tùy chọn...]");
        }
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].isEmpty()) {
                throw new IllegalArgumentException(
                        "Phiếu chứa thành phần rỗng ở vị trí " + (i + 1));
            }
        }

        OrderResult result = new OrderResult();
        System.out.println("[PROCESSOR] Kiểm tra mã đồ uống: " + tokens[0]);
        result.setDrinkName(requireCode(DRINKS, tokens[0], "Mã đồ uống"));
        System.out.println("[PROCESSOR] Kiểm tra kích cỡ: " + tokens[1]);
        if (!SIZES.contains(tokens[1])) {
            throw new IllegalArgumentException("Size không hợp lệ hoặc sai vị trí: " + tokens[1]);
        }
        result.setSize(tokens[1]);

        for (int i = 2; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.startsWith("D")) {
                System.out.println("[PROCESSOR] Kiểm tra lượng đường: " + token);
                String value = requireCode(SUGARS, token, "Lượng đường");
                requireAbsent(result.getSugar(), "đường");
                result.setSugar(value);
            } else if (token.startsWith("I")) {
                System.out.println("[PROCESSOR] Kiểm tra lượng đá: " + token);
                String value = requireCode(ICE, token, "Lượng đá");
                requireAbsent(result.getIce(), "đá");
                result.setIce(value);
            } else if (token.equals("HOT")) {
                System.out.println("[PROCESSOR] Kiểm tra nhiệt độ: " + token);
                requireAbsent(result.getTemperature(), "nhiệt độ");
                result.setTemperature("Nóng");
            } else if (token.equals("TC")) {
                System.out.println("[PROCESSOR] Kiểm tra topping: " + token);
                requireAbsent(result.getTopping(), "topping");
                result.setTopping("Trân châu");
            } else if (token.startsWith("ST")) {
                // Quy tắc mới: phải mở rộng chính bộ xử lý tập trung.
                System.out.println("[PROCESSOR] Kiểm tra ống hút: " + token);
                String value;
                if (token.equals("ST0")) {
                    value = "Không lấy ống hút";
                } else if (token.equals("ST1")) {
                    value = "Có ống hút";
                } else {
                    throw new IllegalArgumentException("Mã ống hút không hợp lệ: " + token);
                }
                requireAbsent(result.getStraw(), "ống hút");
                result.setStraw(value);
            } else {
                throw new IllegalArgumentException("Mã tùy chọn không hợp lệ: " + token);
            }
        }
        // Quy tắc kết hợp: phiếu không được vừa chọn HOT vừa chỉ định lượng đá.
        if (result.getTemperature() != null && result.getIce() != null) {
            throw new IllegalArgumentException(
                    "Không thể chọn đồ uống nóng (HOT) cùng tùy chọn đá");
        }
        return result;
    }

    // codes ánh xạ mã sang mô tả; token là mã cần tra; label là tên nhóm mã dùng trong thông báo
    // lỗi.
    private String requireCode(Map<String, String> codes, String token, String label) {
        String value = codes.get(token);
        if (value == null) {
            throw new IllegalArgumentException(label + " không hợp lệ: " + token);
        }
        return value;
    }

    // current là mô tả tùy chọn đã chọn (null nếu chưa chọn); group là tên nhóm như đường, đá hoặc
    // ống hút.
    // Mỗi nhóm tùy chọn chỉ được xuất hiện một lần trong phiếu.
    private void requireAbsent(String current, String group) {
        if (current != null) {
            throw new IllegalArgumentException(
                    "Trùng tùy chọn " + group + ": chỉ được chọn một giá trị trong nhóm");
        }
    }
}
