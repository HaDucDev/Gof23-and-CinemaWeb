# Problem — payment-service

Thanh toán giả lập SUCCESS/FAIL và ledger thu/hoàn; chỉ truy cập schema payment. FAIL lưu FAILED, không có dòng thu hoặc hoàn.

Project Maven độc lập, cú pháp/source/target Java 8 trên JDK 17. Cổng local 8103, database cinema_problem cổng 50104; tự đọc file .env của service này. Không dùng tài khoản/schema hoặc source của service khác.

Từ gốc repository, chạy problem/khoi-tao-data.bat trước, sau đó problem/chay-services.bat -Service Payment. Bỏ -Service để chạy cả bốn service. Launcher tự tìm JDK 17 hoặc nhận -JavaHome. Build riêng bằng problem/bien-dich-services.bat -Service Payment; dừng bằng problem/dung-services.bat.

Chạy trong IntelliJ bằng class *Application với JDK 17; .env được Spring nạp theo đường dẫn project. Java chỉ validate schema, không tự seed hoặc migration.

Xem [hướng dẫn Problem](../../README.md) và [kịch bản hiện tại](../../../docs/KICH_BAN_DEMO_SAGA.md).

Thiết lập SDK/compiler và cấu hình chạy đúng module: [hướng dẫn IntelliJ](../../../HUONG_DAN_INTELLIJ.md). Source/target 8 vẫn cần JDK 17 cho thư viện và runtime.
