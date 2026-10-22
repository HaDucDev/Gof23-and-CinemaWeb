package behavioral.chainofresponsibility.problem;

import behavioral.chainofresponsibility.problem.model.RefundRequest;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Bản Problem chạy riêng: hệ thống tự chọn đúng người bằng if/else tập trung.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        RefundProcessorBad processor = new RefundProcessorBad();
        RefundRequest[] requests = {
            new RefundRequest(500_000), new RefundRequest(5_000_000), new RefundRequest(20_000_000)
        };

        System.out.println("===== PROBLEM: IF/ELSE TẬP TRUNG =====");
        for (RefundRequest request : requests) {
            System.out.println();
            System.out.println("--- Yêu cầu: " + request.getFormattedAmount() + " ---");
            processor.process(request);
        }

        System.out.println();
        System.out.println(
                "Vấn đề: toàn bộ điều kiện chọn cấp xử lý đang tập trung trong RefundProcessorBad.");
    }
}
