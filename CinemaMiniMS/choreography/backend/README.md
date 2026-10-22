# Backend Choreography

Bốn service JPA độc lập, cú pháp/source/target Java 8, JDK 17, Spring Boot 4.1.1, Maven 3.9.6. Mỗi module có POM, Maven Wrapper, source, `.env` riêng; không phụ thuộc module Java dùng chung hoặc parent POM nội bộ. [pom.xml](pom.xml) chỉ là aggregator tùy chọn.

- Account 9091: tài khoản, auth, hồ sơ/nhóm khách; REST nội bộ.
- Seat 9092: catalog, giữ 1–6 ghế, checkout/SSE, consumer quyết định trạng thái ghế.
- Payment 9093: cấu hình SUCCESS/FAIL, thu một lần, ledger hoàn theo khoản gốc; consumer thu/hoàn.
- Booking 8081: API/session/CSRF, giá/vé, thông báo/WebSocket, phát yêu cầu và tổng hợp xác nhận.

Pay: `PaymentRequested → SeatPaymentReady → PaymentSucceeded → SeatConfirmed`; FAIL: `PaymentFailed → SeatReleased`. Hủy đã trả: `BookingCancellationRequested → PaymentRefunded → SeatReleased`. REST chuẩn bị kết thúc ở checkout; sau Pay/hủy không có REST thực hiện bước Saga.

Xem [hướng dẫn SQL/IntelliJ/BAT](../HUONG_DAN_CHAY_CHOREOGRAPHY.md), [API](../API_CHOREOGRAPHY.md) và [kịch bản hiện tại](../../docs/KICH_BAN_DEMO_SAGA.md).
