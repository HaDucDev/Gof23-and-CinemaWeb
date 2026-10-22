# Frontend Problem màu tím

Frontend được sao chép từ Orchestration tại thời điểm tạo bản Problem, giữ các màn hình và bố cục; màu tím ở `src/styles/purple.css`, meta theme-color và favicon. Nhật ký thao tác thay cho timeline Saga. Booking API chạy đồng bộ, `saga` là `null`; sự cố kỹ thuật hiện bằng `processingIssue` để quản trị viên kiểm tra.

Nhấp đúp [chay-frontend.bat](../chay-frontend.bat), mở **http://127.0.0.1:5175**. BAT tự cài thư viện nếu thiếu bằng Node/npm riêng. Giữ cửa sổ mở, `Ctrl+C` để dừng. Backend cần chạy theo [README Problem](../README.md).

```powershell
cd problem\frontend
.\npm-project.cmd install
.\npm-project.cmd run dev
.\npm-project.cmd run build
```

Node `22.23.3`, npm `12.2.0` và dependency được ghim như bản gốc. Các công cụ/cache nằm riêng trong frontend này; không thay Node/npm máy tính. Khi chuyển máy có Internet, chỉ cần mã nguồn/lockfile và Node/npm để bootstrap; không đưa `node_modules`, `.npm-cache`, `.npm-global` hoặc `dist` lên Git.

`.env` của frontend chỉ chứa cấu hình demo công khai; `BOOKING_API_TARGET=http://127.0.0.1:8083`. Vite proxy `/api` và `/ws`, hỗ trợ SSE/WebSocket; không đọc `.env` của backend/hạ tầng. Khi triển khai thật cần reverse proxy và SPA fallback, hoặc đặt `VITE_API_BASE_URL` trước build với CORS của Booking phù hợp.

Các chức năng và bố cục chung đã được đồng bộ với hai bản Saga: checkbox hiện mật khẩu ở đăng nhập/đăng ký, nhập lại mật khẩu, điện thoại tùy chọn ở cả form và API, tổng thu/hoàn/ròng và mã giao dịch gốc trong chi tiết, bộ chọn loại giao dịch và nhãn trạng thái đã hoàn tiền. Các trang đăng nhập/đăng ký, chọn ghế, danh mục phim, vé, quản trị suất cùng các component dùng chung có cùng source. Màu tím chỉ dùng để nhận diện bản demo.

Kịch bản đang dùng: [hủy vé đã thanh toán](../../docs/KICH_BAN_DEMO_SAGA.md). Tra [API của bản này](../API_REST.md) khi đối chiếu request/response; màu giao diện chỉ giúp nhận diện ba bản demo.
