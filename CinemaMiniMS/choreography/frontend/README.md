# CinemaMini — React Choreography

Bản sao độc lập từ frontend Orchestration hiện tại đã được chỉnh sửa, giữ bố cục và các chức năng chung; phần tiến độ Saga đọc projection sự kiện riêng của Choreography. Màu chủ đạo đổi sang **xanh lá `#198754`**, gồm nút, banner, minh họa phim, trạng thái chọn ghế, form, mã vé và favicon. Phim vẫn phân trang sáu phim, không có khối lọc phim/ngày như bản Orchestration hiện tại.

## Chạy bằng một lần nhấp

Nhấp đúp [chay-frontend.bat](../chay-frontend.bat) trong thư mục `choreography`. File tự cài các thư viện còn thiếu bằng Node/npm cục bộ, chạy Vite và mở **http://127.0.0.1:5173**. Giữ cửa sổ lệnh mở; nhấn `Ctrl+C` để dừng. Thêm `--no-open` khi gọi BAT nếu chỉ muốn chạy server mà không mở trình duyệt.

Từ gốc repository:

```powershell
.\choreography\chay-frontend.bat
```

Hoặc chạy thủ công:

```powershell
cd choreography\frontend
.\npm-project.cmd install
.\npm-project.cmd run dev
```

Node.js `22.23.3`, npm `12.2.0` và các phiên bản thư viện được ghim như bản gốc trong `package.json`/`package-lock.json`. Công cụ, thư viện, cache và cấu hình npm nằm riêng trong frontend này. Lần đầu cần Internet và Node/npm có sẵn để bootstrap; những lần sau dùng Node/npm cục bộ. Không dùng chung `node_modules` hoặc cache với Orchestration.

## Cấu hình hai bản

| Nội dung | Choreography | Orchestration |
|---|---|---|
| Frontend | `http://127.0.0.1:5173` | `http://127.0.0.1:5174` |
| Booking API mặc định | `http://127.0.0.1:8081` | `http://127.0.0.1:8082` |
| Màu chủ đạo | Xanh lá `#198754` | Xanh dương `#1d5ed3` |
| File giao diện màu | `src/styles/green.css` | `src/styles/blue.css` |

File [`.env`](.env) trong frontend này chỉ chứa cấu hình công khai của bản demo. `BOOKING_API_TARGET` trỏ đến Booking Choreography; Vite proxy `/api` và `/ws`, gồm cả SSE/WebSocket. Vite chỉ đọc file `.env` của frontend này; không đọc `.env` PostgreSQL hoặc backend. `VITE_API_BASE_URL` là tùy chọn khi triển khai frontend gọi trực tiếp API công khai ở origin khác.

## Màn hình và contract

Giữ nguyên các trang `/movies`, `/login`, `/register`, `/showtimes/:id/seats`, `/bookings`, `/bookings/:id`, `/bookings/:id/payment`, `/admin/showtimes`, `/admin/bookings`, `/admin/bookings/:id`, `/admin/payments`. Giao diện dùng session/CSRF, giữ 1–6 ghế, thời hạn từ backend, idempotency key, thanh toán riêng, hủy/hoàn, thông báo, SSE/WebSocket và timeline Saga như bản nguồn.

**Trạng thái backend:** đã có bốn service và SQL nhiều ghế phiên bản 3. Chạy backend theo [hướng dẫn](../HUONG_DAN_CHAY_CHOREOGRAPHY.md); Booking cổng 8081, PostgreSQL 50100. Khi API chưa chạy, giao diện hiển thị lỗi kết nối, không dùng dữ liệu/kết quả giả ở frontend.

Chi tiết Saga dùng `operationType`, `paymentStatus`, `seatStatus`, `lastEvent` từ projection của Booking; không dùng `saga.status`/FAILED_NEEDS_REVIEW của Orchestration. Khi lỗi kỹ thuật, xem timeline/log/DLQ của Choreography; không suy ra FAIL từ thời gian chờ. Polling đang xử lý mỗi 1,5 giây, PENDING mỗi 5 giây, hiện chưa đặt timeout tổng. SSE có polling dự phòng 15 giây; thông báo đọc lại mỗi 30 giây và cập nhật qua WebSocket.

## Build và chuyển máy

```powershell
.\npm-project.cmd run build
.\npm-project.cmd run preview
```

Build xuất `dist/`; preview dùng cổng `5173` và proxy giống dev. Triển khai thật cần cấu hình SPA fallback về `index.html` và reverse proxy API/SSE/WebSocket sang Booking.

Giữ mã nguồn, `public/`, `scripts/`, cấu hình demo `.env`, các file `.cmd`/`.bat`, `package.json` và `package-lock.json` khi chuyển máy hoặc đưa lên Git. `node_modules/`, `.npm-cache/`, `.npm-global/`, `dist/` và log đều bị bỏ qua. Có Internet thì để BAT cài lại thư viện trên máy mới; không cần sao chép các thư mục nặng từ Orchestration. Nếu mang Node/npm cục bộ sang máy Windows x64 khác, chỉ cần hai thư mục `node_modules/node/` và `node_modules/npm/`, rồi dùng BAT tải các thư viện còn lại.

Chi tiết hành vi chung tham khảo [README frontend Orchestration](../../orchestration/frontend/README.md). Các sửa đổi sau thời điểm sao chép không tự đồng bộ giữa hai frontend.

## Kết quả kiểm tra

Kịch bản đang dùng: [hủy vé đã thanh toán](../../docs/KICH_BAN_DEMO_SAGA.md). Tra [API của bản này](../API_CHOREOGRAPHY.md) khi đối chiếu request/response; màu giao diện chỉ giúp nhận diện ba bản demo.
