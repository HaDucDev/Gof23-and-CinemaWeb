# showtime-templates.json — Thông tin mặc định khi tạo suất chiếu

[showtime-templates.json](showtime-templates.json) lưu **mẫu dùng cho form “Tạo suất từ mẫu” của admin**. Nhờ file này, form có sẵn tên mẫu, giá vé, phụ thu VIP và danh sách ghế để admin chọn khi tạo suất mới.

Hiện tại file chứa một mẫu tên **“Suất tiêu chuẩn”**, giá cơ bản **80.000 đồng**, phụ thu VIP **20.000 đồng** và **30 ghế A1–E6**.

## Ý nghĩa các thông tin trong file

| Trường JSON | Ví dụ | Dùng để làm gì trong code hiện tại? |
| --- | --- | --- |
| `name` | `"Suất tiêu chuẩn"` | Tên mẫu hiển thị phía trên form admin; tên phim được chọn riêng. |
| `pricing.basePrice` | `80000` | Điền sẵn giá cơ bản cho một ghế trên form. Admin có thể sửa trước khi tạo suất. Đơn vị: đồng. |
| `pricing.vipSurcharge` | `20000` | Điền sẵn khoản cộng thêm cho một ghế VIP. Admin có thể sửa. Đơn vị: đồng. |
| `seats[].code` | `"A1"` | Mã ghế xuất hiện trong danh sách checkbox. Khi tạo suất, code tìm ghế `Seat` có cùng mã trong database. |
| `seats[].openSale` | `true` | Trạng thái mở bán lưu trong mẫu. Khi tạo suất, giá trị này được ghi lại theo các checkbox “Ghế không mở bán” mà admin chọn. |

`seats[]` là danh sách ghế: mỗi phần tử có mã ghế `code` và cờ `openSale`. Trong bản cấu hình dùng để tạo suất, `openSale = true` tạo ghế còn trống (`AVAILABLE`); `false` tạo ghế không mở bán (`DISABLED`).

**Điểm cần chú ý với code hiện tại:** chỉ đổi `openSale` thành `false` trong JSON chưa tự làm checkbox trên form được chọn. `ShowtimeService` đặt lại cờ của từng ghế theo dữ liệu admin gửi. Vì vậy, muốn đóng một ghế trong suất mới, admin cần chọn ghế đó ở mục **“Ghế không mở bán trong suất mới”**.

## Khi nào ứng dụng dùng file này?

1. **Khi khởi động:** `TemplateCatalog` đọc JSON thành một đối tượng `ShowtimeTemplate` và giữ mẫu gốc trong bộ nhớ.
2. **Khi admin mở trang quản lý suất:** controller lấy một bản sao của mẫu để hiển thị tên, điền sẵn giá và tạo danh sách checkbox ghế.
3. **Khi admin bấm “Tạo suất chiếu”:** `ShowtimeService` lấy bản sao, áp dụng giá và lựa chọn ghế từ form, rồi lưu suất mới cùng trạng thái từng ghế vào database.

Đọc JSON lúc khởi động chỉ đưa thông tin mẫu vào bộ nhớ. Các suất chiếu ban đầu trong database do [cinemamini.sql](../../../../../database/cinemamini.sql) tạo khi người triển khai chạy SQL; các suất tạo thêm từ mẫu được lưu khi admin gửi form.

## Ví dụ dễ hình dung

Admin mở form và thấy giá 80.000 đồng, phụ thu VIP 20.000 đồng cùng danh sách 30 ghế lấy từ JSON. Admin chọn phim, nhập giờ chiếu, đổi giá thành 90.000 đồng và đánh dấu ghế A1 không mở bán.

Hệ thống sao chép mẫu, sửa bản sao theo các lựa chọn đó rồi tạo suất mới: giá 90.000 đồng, ghế A1 có trạng thái `DISABLED`, các ghế còn lại là `AVAILABLE`.

Lần mở form tiếp theo vẫn lấy giá 80.000 đồng và phụ thu 20.000 đồng từ mẫu gốc. Suất vừa tạo giữ giá riêng đã lưu trong database.

Đây là cách file JSON hỗ trợ **Prototype**: file chứa dữ liệu mẫu, còn `ShowtimeTemplate.copy()` tạo bản sao để chỉnh cho từng suất mới.

## Nếu sửa file JSON thì sao?

- Sửa giá trong JSON sẽ đổi giá điền sẵn trên form sau khi ứng dụng đọc lại file. Giá thực sự lưu cho suất mới vẫn là giá admin gửi từ form.
- Danh sách mã ghế phải phù hợp với dữ liệu `Seat` đã chuẩn bị trong database.
- `TemplateCatalog` đọc file một lần khi khởi động. Sau khi sửa file, cần khởi động lại ứng dụng; nếu chạy bằng JAR thì đóng gói lại và chạy JAR mới.
- Sửa mẫu chỉ ảnh hưởng các lần dùng mẫu tiếp theo; các suất đã lưu trong database giữ thông tin của chúng.

## Các file Java liên quan

| File | Công việc |
| --- | --- |
| [TemplateCatalog.java](../../java/com/td/CinemaMini/service/showtime/TemplateCatalog.java) | Đọc JSON và giữ mẫu gốc; trả bản sao khi được yêu cầu. |
| [ShowtimeTemplate.java](../../java/com/td/CinemaMini/service/showtime/ShowtimeTemplate.java) | Chứa tên, giá và danh sách ghế; cung cấp hàm `copy()`. |
| [AdminShowtimeController.java](../../java/com/td/CinemaMini/controller/AdminShowtimeController.java) | Đưa thông tin mẫu ra form và nhận dữ liệu admin gửi. |
| [ShowtimeService.java](../../java/com/td/CinemaMini/service/showtime/ShowtimeService.java) | Dùng bản sao để tạo suất mới và các dòng `ShowtimeSeat`. |
