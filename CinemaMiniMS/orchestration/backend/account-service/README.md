# Account Service

Ứng dụng độc lập, cổng mặc định `8091`, role `cinema_account`, schema `account`. PostgreSQL đã khởi tạo phiên bản 3. Thư mục có POM/Maven Wrapper/source/config riêng; không cần source/JAR hoặc POM cha nội bộ của service khác.

## Cấu trúc

`controller -> service -> repository -> PostgreSQL`. Entity ở `entity`, repository dùng Spring Data JPA/JpaRepository. DTO ở `dto/request`, `dto/response`, query projection ở `dto/projection`; trạng thái/giá trị nghiệp vụ ở `model`; ánh xạ ở `mapper`; cấu hình ở `config`; lỗi ở `exception`. CRUD dùng entity/save; query đặc biệt dùng @Query/JPQL/native và projection có tên/kiểu rõ ràng. Không còn JdbcExecutor hoặc repository/jdbc. Map tổng hợp cho API được tạo từ object projection, không đọc raw ResultSet. Controller không mở transaction và không gọi peer trực tiếp. Hibernate `ddl-auto=validate`, `open-in-view=false`, SQL init `never`; không tạo DDL hoặc seed khi khởi động. Chuyển sang JPA không cần nâng lại database phiên bản 3.

## Build và chạy local trực tiếp

Từ thư mục này trong PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Java\jdk-17'
.\mvnw.cmd -B -ntp '-Dmaven.test.skip=true' clean package
& "$env:JAVA_HOME\bin\java.exe" -jar .\target\account-service-1.0.0.jar
```

Nhấn Ctrl+C để dừng nếu chạy trực tiếp. Build không khởi động service hoặc SQL. `local` dùng DB loopback 50102; vẫn ghi đè được bằng `DB_URL`. Các luồng cần peer tương ứng hoạt động, không tự khởi động peer.

## Triển khai riêng

`application.yaml` mặc định coi ứng dụng ở server riêng: không bật `local`; đặt `DB_URL`, `DB_PASSWORD`, `INTERNAL_API_KEY`, tùy chọn `SERVER_PORT`/`SERVICE_BIND_ADDRESS`. Danh sách biến nằm trong `src/main/resources/application.yaml`; đặt biến trong shell, IDE hoặc hệ thống chạy ứng dụng. Chỉ truyền mật khẩu role của service này. Khóa nội bộ tối thiểu 24 ký tự, giống các service giao tiếp và giữ ổn định giữa các lần chạy.

Demo có đúng một file [`.env`](.env) riêng trong thư mục service này, chứa cấu hình local và mật khẩu DB của chính service; không kèm `.env.example`. Launcher BAT và Spring khi chạy trực tiếp đều nạp file này. PostgreSQL tạo role bằng mật khẩu khai báo trực tiếp trong `orchestration/docker-compose.yml`; service tự khai báo `DB_PASSWORD` tương ứng, không đọc hoặc lấy giá trị từ Compose hay service khác. Nếu đổi mật khẩu, cập nhật đúng role trong Compose và `DB_PASSWORD` của service, rồi chạy lại BAT khởi tạo dữ liệu.

Trong IntelliJ, dùng JDK 17 và module của service, chọn **Build → Rebuild Project** rồi chạy lớp `*Application`. `application.yaml` tự tìm `.env` của service khi Working directory ở gốc repository, `orchestration/`, `backend/` hoặc thư mục service; không cần nhập lại biến môi trường. Giữ định dạng `TEN_BIEN=gia_tri` không có dấu nháy bao quanh giá trị hoặc tiền tố `export`. Các biến môi trường hoặc tham số dòng lệnh vẫn có thể ghi đè cấu hình file.

Thiết lập SDK/compiler và cấu hình chạy đúng module: [hướng dẫn IntelliJ](../../../HUONG_DAN_INTELLIJ.md). Source/target 8 vẫn cần JDK 17 cho thư viện và runtime.
