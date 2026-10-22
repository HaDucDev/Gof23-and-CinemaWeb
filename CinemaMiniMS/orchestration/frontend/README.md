# CinemaMini — React Orchestration

Frontend JavaScript/JSX cho backend REST có sẵn. Giữ bố cục từ templates/CSS CinemaMini MVC: header/footer, banner vé, ba thẻ phim, chọn ghế hai cột, biểu mẫu đăng nhập, thanh toán và quản trị. Đổi bảng màu sang xanh dương; minh họa bằng CSS, không phụ thuộc ảnh hoặc CDN. Bootstrap CSS 5.3.8 được sao chép từ mẫu MVC; không dùng Bootstrap JS, Redux hoặc TypeScript.

## Chạy bằng một lần nhấp

Nhấp đúp [chay-frontend.bat](../chay-frontend.bat) trong thư mục `orchestration`. File BAT tự chuyển tới frontend, cài công cụ/thư viện cục bộ nếu thiếu, chạy Vite và mở trình duyệt tại **http://127.0.0.1:5174**. Giữ cửa sổ lệnh mở; nhấn `Ctrl+C` để dừng. Nếu có lỗi, cửa sổ giữ lại để đọc thông báo. Backend chạy riêng theo hướng dẫn REST.

Cũng có thể gọi từ gốc repository bằng `.\orchestration\chay-frontend.bat`. Thêm `--no-open` nếu chỉ muốn chạy server mà không tự mở trình duyệt. Node/npm hệ thống không bị nâng cấp hoặc thay cấu hình.

Đã chạy thử file BAT với `--no-open`: Vite khởi động thành công tại cổng 5174 và `/movies` trả HTTP 200.

## Node.js và npm riêng cho dự án

Ghim **Node.js 22.23.3** và **npm 12.2.0** trong `devDependencies`. Không nâng cấp Node/npm hệ thống, không cài package global, không đổi PATH hệ thống. Launcher dùng:

- Node: `node_modules/node/bin/node.exe` trên Windows.
- npm: `node_modules/npm/bin/npm-cli.js`.
- Cache: `.npm-cache/` trong frontend; prefix dự phòng `.npm-global/` cũng nằm tại đây.
- `npm-project.cmd`: gọi npm cục bộ bằng Node cục bộ, chặn `-g`/`--global`.

PowerShell, từ thư mục dự án chính:

```powershell
cd orchestration\frontend
.\npm-project.cmd install
.\npm-project.cmd --version
.\npm-project.cmd run dev
```

Lần đầu chưa có công cụ cục bộ, launcher dùng npm đã có trên máy **chỉ để tải công cụ vào thư mục dự án**, sau đó chạy lại cài đặt bằng Node/npm cục bộ để cài đúng các dependency native của Vite. Bootstrap dùng được với Node 14.21.3/npm 8.18.0 hiện có. Những lần sau hoàn toàn dùng bộ cục bộ. Cần mạng trong lần cài đầu tiên; không cần tải npm thủ công. `npm run setup` là lệnh bootstrap thay thế.

Những lần sau dùng `.\npm-project.cmd install`; npm giữ các phiên bản đã chốt trong lockfile. Không dùng `ci` trong launcher vì nó sẽ xóa cả Node/npm đang chạy trong `node_modules` (Windows khóa file thực thi). Muốn cài sạch, dừng frontend, xóa `node_modules` rồi chạy lại lệnh install; giữ `package-lock.json`. `node_modules/`, `dist/`, `.npm-cache/` và `.npm-global/` không đưa vào Git; riêng `.env` của frontend chứa giá trị demo công khai và được giữ cùng mã nguồn. Cấu hình npm cũng dùng file riêng trong `scripts/`, không đọc cấu hình người dùng/hệ thống. Chỉ cho phép install script của package Node đã ghim qua `allowScripts`. Commit `package-lock.json` cùng mã nguồn.

## Chuyển máy và đưa mã nguồn lên GitHub/GitLab

Chỉ chuyển mã nguồn, tài nguyên, cấu hình mẫu và lockfile; máy mới tự cài lại thư viện. Không cần sao chép Node/npm cục bộ vì chúng cũng được cài lại từ các phiên bản đã ghim.

| Giữ trong Git / bản sao mã nguồn | Bỏ khỏi Git / bản sao mã nguồn |
|---|---|
| `src/`, `public/`, `scripts/` | `node_modules/` (gồm thư viện và Node/npm cục bộ) |
| `package.json`, `package-lock.json` | `.npm-cache/`, `.npm-global/` |
| `index.html`, `vite.config.js`, `npm-project.cmd` | `dist/` (build lại khi cần) |
| `.gitignore`, `README.md`, `.env` (cấu hình demo của frontend) | `.env.local`, `.env.production` và cấu hình triển khai riêng |
| `../chay-frontend.bat` | File log |

`package-lock.json` cần giữ để cài lại bộ phiên bản đã chốt. `.gitignore` hiện đã bỏ qua các thư mục/file không cần gửi nêu trên; riêng `frontend/.env` chứa giá trị demo công khai và được giữ trong mã nguồn. `.gitignore` chỉ áp dụng cho Git: sao chép bằng Explorer hoặc nén ZIP thủ công vẫn phải tự loại các mục này. File đã được Git theo dõi từ trước không tự được bỏ theo dõi chỉ vì thêm vào `.gitignore`.

Cách chuyển sang máy Windows khác:

1. Commit và push mã nguồn lên repository GitHub/GitLab, rồi clone trên máy mới; hoặc chép bộ mã nguồn theo bảng trên. Giữ cấu trúc `orchestration/chay-frontend.bat` và `orchestration/frontend/`; không cần giữ đường dẫn ổ đĩa `N:`.
2. Máy mới cần Node/npm có sẵn để bootstrap lần đầu và kết nối Internet. Không cần cài sẵn đúng phiên bản Node/npm cục bộ: launcher tự tải bản đã ghim vào dự án.
3. Giữ file `orchestration/frontend/.env` đi kèm; nếu địa chỉ Booking khác mặc định, sửa `BOOKING_API_TARGET` trong file của frontend.
4. Nhấp đúp `orchestration/chay-frontend.bat`. Thư viện và công cụ được cài lại lần đầu; những lần sau dùng bộ cục bộ.

Khi chuyển cả backend, giữ source Java, POM, Maven Wrapper (gồm `.mvn/`, `mvnw`, `mvnw.cmd`), SQL, script và file `.env` riêng của mỗi project/service; bỏ các thư mục `target/`, log và PID/runtime. Dữ liệu PostgreSQL đang chạy cần sao lưu/khôi phục riêng nếu muốn giữ vé/giao dịch; Git chứa script SQL, không tự chuyển dữ liệu trong Docker volume. Các file `.env` đi kèm chỉ chứa giá trị demo công khai.

`dist/` chỉ cần khi triển khai bản frontend đã build lên web server; không cần commit hoặc mang theo để phát triển trên máy mới. GitHub/GitLab lưu repository mã nguồn; việc triển khai ứng dụng chạy trên cloud là bước riêng.

## Chạy với backend

1. Chuẩn bị PostgreSQL và bốn service theo [hướng dẫn REST](../HUONG_DAN_CHAY_REST.md). Backend không được khởi động hoặc nạp lại dữ liệu bởi frontend.
2. Chạy `.\npm-project.cmd run dev`, mở **http://127.0.0.1:5174**.
3. Vite proxy `/api` và `/ws` tới Booking **http://127.0.0.1:8082**, hỗ trợ SSE và WebSocket. Frontend không gọi trực tiếp Account/Seat/Payment.

Frontend có đúng một file [`.env`](.env) riêng trong thư mục `frontend/`, không kèm `.env.example`. Vite chỉ đọc cấu hình của project này, không đọc `.env` của PostgreSQL hoặc backend. Đổi `BOOKING_API_TARGET` nếu Booking chạy ở địa chỉ khác. Có thể đặt `VITE_API_BASE_URL` trong file này khi triển khai frontend gọi trực tiếp Booking công khai. Chỉ đưa cấu hình công khai vào frontend; khóa nội bộ và mật khẩu DB nằm trong file của backend/hạ tầng.

Tài khoản seed (chỉ dùng khi database đã được khởi tạo): USER `an@cinemamini.test`, `binh@cinemamini.test`, `chi@cinemamini.test`, `dung@cinemamini.test`; ADMIN `admin@cinemamini.test`. Mật khẩu chung `CinemaMini@123`.

## Màn hình và hành vi

| Đường dẫn | Chức năng |
|---|---|
| `/movies` | Banner/thẻ phim như MVC; phân trang 6 phim, suất từ API; không có khối lọc phim/ngày |
| `/login`, `/register` | Đăng nhập session, hiện/ẩn mật khẩu; đăng ký USER/REGULAR có nhập lại mật khẩu rồi chuyển sang đăng nhập |
| `/showtimes/:id/seats` | 30 ghế A1–E6, sáu trạng thái/loại có chú thích; tối đa 6 ghế; giữ ngay sau server xác nhận |
| `/bookings` | Vé/đơn của tài khoản đang đăng nhập |
| `/bookings/:id` | Chi tiết, mã vé, lịch sử thu/hoàn, hủy đơn/vé, timeline Saga |
| `/bookings/:id/payment` | Tám phương thức; chỉ bấm Thanh toán mới gọi Pay; tự cập nhật kết quả |
| `/admin/showtimes` | Tạo suất từ mẫu, giá thường/VIP, đóng ghế, lịch chiếu |
| `/admin/bookings`, `/admin/bookings/:id` | Đơn vé và timeline Saga cho ADMIN |
| `/admin/payments` | Ledger thu/hoàn và cấu hình thanh toán SUCCESS/FAIL |

Session dùng cookie HttpOnly của backend; mọi request gửi `credentials: include`. Lấy CSRF trước thao tác ghi, bỏ token cache sau đăng nhập/đăng xuất. Phân quyền frontend phục vụ điều hướng; backend vẫn quyết định quyền truy cập.

Phiên giữ chỗ chỉ lưu `holdId` theo tài khoản và suất trong sessionStorage. Khi refresh, đọc lại hạn/revision/token từ backend; không cộng thêm 5 phút. Không tự tạo booking khi chọn ghế. Ghế đang chờ giữ/trả và nút checkout bị khóa; giá trước tạo đơn là dự kiến, số tiền và ưu đãi chính thức dùng snapshot backend. Điện thoại tùy chọn, tối đa 30 ký tự, tương ứng MVC; API cũng chấp nhận không gửi hoặc gửi `null` và lưu chuỗi rỗng.

Mỗi thao tác giữ/trả/tạo đơn/Pay/hủy có UUID riêng. Nếu mạng hoặc service lỗi khiến chưa biết kết quả, lưu nguyên key/payload trong sessionStorage và chỉ gửi lại khi người dùng bấm thử lại. Trong lúc đó khóa thao tác mới để tránh tạo yêu cầu khác. Không tự retry thu tiền hoặc giả lập kết quả ở frontend. Payload chờ có thể chứa token phiên giữ chỗ của chính người dùng; không lưu mật khẩu, cookie session hoặc CSRF vào storage.

Ghế cập nhật bằng SSE; mất kết nối thì EventSource kết nối lại và có polling 15 giây. Thông báo đọc qua REST, cập nhật WebSocket, có đọc lại mỗi 30 giây và khi quay lại cửa sổ. Thanh toán/hủy đang xử lý được đọc lại mỗi 1,5 giây; đơn PENDING mỗi 5 giây. Trạng thái `FAILED_NEEDS_REVIEW` hiển thị để kiểm tra, không biến thành FAIL. Mã vé đã hủy hiện rõ đã vô hiệu. Giờ hiển thị luôn là Việt Nam; đồng hồ trình duyệt được điều chỉnh theo HTTP Date khi header này đọc được, còn server quyết định hiệu lực phiên khi xử lý request.

## Build và vận hành

```powershell
.\npm-project.cmd run build
.\npm-project.cmd run preview
```

Build tạo `dist/`; preview cổng 5174 có proxy giống dev. Khi triển khai thật, cấu hình web server fallback các route SPA về `index.html`, reverse proxy `/api` và `/ws` sang Booking, không buffer SSE và cho phép WebSocket upgrade. `vite preview` chỉ dùng kiểm tra bản build.

Nếu frontend/API khác origin, đặt `VITE_API_BASE_URL` trước build, cấu hình `FRONTEND_ORIGINS` ở Booking đúng origin frontend. Khi dùng HTTPS, cấu hình cookie an toàn theo hướng dẫn backend. Ưu tiên reverse proxy cùng origin để session/SSE/WebSocket hoạt động thống nhất.

## Kiểm tra thủ công với backend

Build và kiểm tra mã frontend không thay thế kiểm tra các luồng này với PostgreSQL và bốn service đang chạy. Khi API chưa chạy, giao diện hiển thị lỗi kết nối và nút thử lại; không đưa dữ liệu giả vào màn hình nghiệp vụ.

Kịch bản đang dùng: [hủy vé đã thanh toán](../../docs/KICH_BAN_DEMO_SAGA.md). Tra [API của bản này](../API_REST.md) khi đối chiếu request/response; màu giao diện chỉ giúp nhận diện ba bản demo.
