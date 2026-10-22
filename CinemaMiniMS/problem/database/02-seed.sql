-- Matching fixtures for three independent demos; November 2026 is the earliest sample date.
\set ON_ERROR_STOP on
\if :{?seed_start_date}
\else
\set seed_start_date ''
\endif
BEGIN;
SET LOCAL client_encoding = 'UTF8';
SET LOCAL TIME ZONE 'Asia/Ho_Chi_Minh';
SELECT set_config('cinema.requested_seed_start_date', :'seed_start_date', true);
CREATE TEMPORARY TABLE cinema_seed_schedule (
    seq smallint PRIMARY KEY, title text NOT NULL, day_offset integer NOT NULL,
    start_time time NOT NULL
) ON COMMIT DROP;
INSERT INTO cinema_seed_schedule (seq, title, day_offset, start_time) VALUES
    (1, 'Mùa Hè Bên Sông', 0, time '09:00'),
    (2, 'Người Giữ Ánh Sáng', 0, time '11:30'),
    (3, 'Chuyến Tàu Bình Minh', 0, time '14:00'),
    (4, 'Mật Mã Thành Phố', 0, time '16:30'),
    (5, 'Hành Tinh Nhỏ', 0, time '19:00'),
    (6, 'Bản Nhạc Cuối Thu', 0, time '21:30'),
    (7, 'Đường Đua Ước Mơ', 1, time '09:00'),
    (8, 'Khu Vườn Trên Mây', 1, time '11:30'),
    (9, 'Dấu Vết Trong Mưa', 1, time '14:00'),
    (10, 'Bếp Nhà Có Nắng', 1, time '16:30'),
    (11, 'Hải Trình Xanh', 1, time '19:00'),
    (12, 'Lá Thư Từ Ngày Mai', 1, time '21:30'),
    (13, 'Thành Phố Không Ngủ', 2, time '09:00'),
    (14, 'Chuyến Xe Về Nhà', 2, time '11:30'),
    (15, 'Bí Mật Thung Lũng', 2, time '14:00'),
    (16, 'Chuyến Tàu Bình Minh', 2, time '16:30'),
    (17, 'Mật Mã Thành Phố', 2, time '19:00'),
    (18, 'Hành Tinh Nhỏ', 2, time '21:30'),
    (19, 'Mùa Hè Bên Sông', 3, time '09:00'),
    (20, 'Người Giữ Ánh Sáng', 3, time '11:30'),
    (21, 'Bản Nhạc Cuối Thu', 3, time '14:00'),
    (22, 'Đường Đua Ước Mơ', 3, time '16:30'),
    (23, 'Khu Vườn Trên Mây', 3, time '19:00'),
    (24, 'Dấu Vết Trong Mưa', 3, time '21:30'),
    (25, 'Bếp Nhà Có Nắng', 4, time '09:00'),
    (26, 'Hải Trình Xanh', 4, time '11:30'),
    (27, 'Lá Thư Từ Ngày Mai', 4, time '14:00'),
    (28, 'Thành Phố Không Ngủ', 4, time '16:30'),
    (29, 'Chuyến Xe Về Nhà', 4, time '19:00'),
    (30, 'Chuyến Tàu Bình Minh', 4, time '21:30'),
    (31, 'Mật Mã Thành Phố', 5, time '09:00'),
    (32, 'Hành Tinh Nhỏ', 5, time '11:30'),
    (33, 'Mùa Hè Bên Sông', 5, time '14:00'),
    (34, 'Người Giữ Ánh Sáng', 5, time '16:30'),
    (35, 'Bản Nhạc Cuối Thu', 5, time '19:00'),
    (36, 'Đường Đua Ước Mơ', 5, time '21:30'),
    (37, 'Khu Vườn Trên Mây', 6, time '09:00'),
    (38, 'Dấu Vết Trong Mưa', 6, time '11:30'),
    (39, 'Bếp Nhà Có Nắng', 6, time '14:00'),
    (40, 'Hải Trình Xanh', 6, time '16:30'),
    (41, 'Lá Thư Từ Ngày Mai', 6, time '19:00'),
    (42, 'Thành Phố Không Ngủ', 6, time '21:30'),
    (43, 'Chuyến Tàu Bình Minh', 7, time '09:00'),
    (44, 'Mật Mã Thành Phố', 7, time '11:30'),
    (45, 'Hành Tinh Nhỏ', 7, time '14:00'),
    (46, 'Mùa Hè Bên Sông', 7, time '16:30'),
    (47, 'Người Giữ Ánh Sáng', 7, time '19:00'),
    (48, 'Bản Nhạc Cuối Thu', 7, time '21:30'),
    (49, 'Đường Đua Ước Mơ', 8, time '09:00'),
    (50, 'Khu Vườn Trên Mây', 8, time '11:30'),
    (51, 'Dấu Vết Trong Mưa', 8, time '14:00'),
    (52, 'Bếp Nhà Có Nắng', 8, time '16:30'),
    (53, 'Hải Trình Xanh', 8, time '19:00'),
    (54, 'Chuyến Tàu Bình Minh', 8, time '21:30'),
    (55, 'Mật Mã Thành Phố', 9, time '09:00'),
    (56, 'Hành Tinh Nhỏ', 9, time '11:30'),
    (57, 'Mùa Hè Bên Sông', 9, time '14:00'),
    (58, 'Người Giữ Ánh Sáng', 9, time '16:30'),
    (59, 'Bản Nhạc Cuối Thu', 9, time '19:00');

DO $seed$
DECLARE
    v_saved text := nullif(current_setting('cinema.seed_start_date', true), '');
    v_requested text := nullif(current_setting('cinema.requested_seed_start_date'), '');
    v_start_date date;
BEGIN
    v_start_date := coalesce(v_requested::date, v_saved::date,
        greatest(date '2026-11-01', (clock_timestamp() AT TIME ZONE 'Asia/Ho_Chi_Minh')::date + 1));
    IF v_start_date < date '2026-11-01' THEN
        RAISE EXCEPTION 'Demo showtimes must start on or after 2026-11-01.';
    END IF;
    IF v_saved IS NOT NULL AND v_start_date <> v_saved::date THEN
        RAISE EXCEPTION 'Database is already seeded for %. Reuse this date, or run the explicit schedule reset first.', v_saved;
    END IF;
    IF v_saved IS NULL AND ((v_start_date + time '09:00') AT TIME ZONE 'Asia/Ho_Chi_Minh') <= clock_timestamp() THEN
        RAISE EXCEPTION 'The first showtime at 09:00 must be in the future.';
    END IF;

    INSERT INTO account.user_accounts (email, password_hash, role)
        VALUES
            ('an@cinemamini.test', '$2a$10$1fJXSUTB5FpRZPi.00Nw5.hJe2LkD4ZJCDsNRxcGU2vK6bSidRTC.', 'USER'),
            ('binh@cinemamini.test', '$2a$10$CWyiyCRZ7fL3fnSSbmtNN.1tXrKFZAftOj3pKxp5msTUkVsISqhXW', 'USER'),
            ('chi@cinemamini.test', '$2a$10$K/TPzU0BdfNDIMVIa/YqVuwTIbw6MyDt6hcev9Hryx40m1P7aJIdK', 'USER'),
            ('dung@cinemamini.test', '$2a$10$BF3IRg8NIzT/Gh2tB31lE.A6PewcrHFA/DOOqhWeaT4LoiaGGa2S6', 'USER'),
            ('admin@cinemamini.test', '$2a$10$Sq0dpSeXEk2v.p5OGBkClevEmPQ9aqrmUu4vR5SWfMBoN6dn42Toq', 'ADMIN')
        ON CONFLICT (email) DO NOTHING;

    INSERT INTO account.customers (account_id, full_name, customer_type)
        SELECT a.account_id, seed.full_name, seed.customer_type
        FROM (VALUES
            ('an@cinemamini.test', 'An', 'REGULAR'),
            ('binh@cinemamini.test', 'Bình', 'STUDENT'),
            ('chi@cinemamini.test', 'Chi', 'MEMBER'),
            ('dung@cinemamini.test', 'Dũng', 'REGULAR')
        ) AS seed(email, full_name, customer_type)
        JOIN account.user_accounts a ON a.email = seed.email AND a.role = 'USER'
        ON CONFLICT (account_id) DO NOTHING;

    INSERT INTO seat.movies (title, duration_minutes, description)
        VALUES
            ('Chuyến Tàu Bình Minh', 100, 'Hành trình trên chuyến tàu đầu ngày mở ra những cuộc gặp gỡ và hy vọng mới.'),
            ('Mật Mã Thành Phố', 100, 'Những người bạn cùng giải mã bí ẩn ẩn sau các địa điểm quen thuộc trong thành phố.'),
            ('Hành Tinh Nhỏ', 100, 'Cuộc phiêu lưu khám phá một hành tinh nhỏ, nơi tình bạn giúp vượt qua thử thách.'),
            ('Mùa Hè Bên Sông', 95, 'Nhóm bạn trở về quê trong kỳ nghỉ hè và cùng nhau gìn giữ bến đò gắn với tuổi thơ.'),
            ('Người Giữ Ánh Sáng', 110, 'Một người thợ sửa đèn biển cùng cô con gái tìm cách đưa tàu cá trở về an toàn giữa cơn bão.'),
            ('Bản Nhạc Cuối Thu', 105, 'Nữ nghệ sĩ trẻ tìm lại cảm hứng khi tham gia dàn nhạc của một thị trấn miền núi.'),
            ('Đường Đua Ước Mơ', 115, 'Chàng thợ máy và những người bạn tự chế chiếc xe đua để chinh phục giải đấu địa phương.'),
            ('Khu Vườn Trên Mây', 90, 'Hai chị em khám phá khu vườn kỳ diệu trên đỉnh núi và học cách bảo vệ thiên nhiên.'),
            ('Dấu Vết Trong Mưa', 120, 'Một phóng viên lần theo những bức ảnh cũ để làm sáng tỏ vụ mất tích xảy ra trong đêm mưa.'),
            ('Bếp Nhà Có Nắng', 100, 'Ba thế hệ trong một gia đình cùng mở quán ăn nhỏ và hàn gắn những hiểu lầm qua từng món ăn.'),
            ('Hải Trình Xanh', 108, 'Đội nghiên cứu biển trẻ vượt qua nhiều thử thách để cứu rạn san hô và những sinh vật quý hiếm.'),
            ('Lá Thư Từ Ngày Mai', 112, 'Một sinh viên nhận được những lá thư bí ẩn giúp cô thay đổi lựa chọn và trân trọng hiện tại.'),
            ('Thành Phố Không Ngủ', 118, 'Những con người làm việc về đêm cùng giúp một em nhỏ tìm đường trở về nhà trước bình minh.'),
            ('Chuyến Xe Về Nhà', 102, 'Các hành khách xa lạ trên chuyến xe cuối năm chia sẻ câu chuyện và cùng vượt qua sự cố dọc đường.'),
            ('Bí Mật Thung Lũng', 125, 'Nhóm thám hiểm trẻ giải những câu đố cổ để tìm hiểu truyền thuyết về một thung lũng bị lãng quên.')
        ON CONFLICT (title) DO NOTHING;

    INSERT INTO seat.seats (seat_no, row_label, seat_number, seat_type)
        SELECT r.row_label || n.seat_number::text, r.row_label, n.seat_number,
               CASE WHEN r.row_label IN ('D', 'E') THEN 'VIP' ELSE 'STANDARD' END
        FROM (VALUES ('A'), ('B'), ('C'), ('D'), ('E')) AS r(row_label)
        CROSS JOIN generate_series(1, 6) AS n(seat_number)
        ON CONFLICT (seat_no) DO NOTHING;

    INSERT INTO seat.showtimes (showtime_id, movie_id, starts_at, ends_at, room_available_at,
        duration_minutes, cleanup_minutes, base_price, vip_surcharge)
    SELECT 'SHOW-' || to_char(v_start_date, 'YYYYMMDD') || '-' || lpad(s.seq::text, 3, '0'),
        m.movie_id, t.starts_at,
        t.starts_at + m.duration_minutes * interval '1 minute',
        t.starts_at + (m.duration_minutes + 20) * interval '1 minute',
        m.duration_minutes, 20, 80000, 20000
    FROM cinema_seed_schedule s JOIN seat.movies m ON m.title = s.title
    CROSS JOIN LATERAL (
        SELECT (v_start_date + s.day_offset + s.start_time) AT TIME ZONE 'Asia/Ho_Chi_Minh' AS starts_at
    ) t
    ON CONFLICT (movie_id, starts_at) DO NOTHING;

    INSERT INTO seat.showtime_seats (showtime_id, seat_id, status)
    SELECT st.showtime_id, physical.seat_id, 'AVAILABLE'
    FROM cinema_seed_schedule s
    JOIN seat.showtimes st ON st.showtime_id =
        'SHOW-' || to_char(v_start_date, 'YYYYMMDD') || '-' || lpad(s.seq::text, 3, '0')
    CROSS JOIN seat.seats physical
    ON CONFLICT (showtime_id, seat_id) DO NOTHING;

    EXECUTE format('ALTER DATABASE %I SET cinema.seed_start_date TO %L', current_database(), v_start_date::text);
    RAISE NOTICE 'Seed date: %. Existing accounts, passwords, bookings and seat ownership were preserved.', v_start_date;
END;
$seed$;
COMMIT;
