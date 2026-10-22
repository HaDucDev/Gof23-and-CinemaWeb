-- Read-only checks. Run after seed; safe to repeat after bookings exist.
\set ON_ERROR_STOP on
BEGIN READ ONLY;
DO $verify$
DECLARE
    v_anchor text := nullif(current_setting('cinema.seed_start_date', true), '');
    v_prefix text;
    v_role text;
    v_schema text;
    v_other text;
    v_entry record;
BEGIN
    IF current_database() <> 'cinema_problem' THEN
        RAISE EXCEPTION 'Run Problem verification only against cinema_problem.';
    END IF;
    IF v_anchor IS NULL THEN RAISE EXCEPTION 'Missing saved seed date; run 02-seed.sql first.'; END IF;
    IF v_anchor::date < date '2026-11-01' THEN
        RAISE EXCEPTION 'Demo seed date must be on or after 2026-11-01.';
    END IF;
    v_prefix := '^SHOW-' || to_char(v_anchor::date, 'YYYYMMDD') || '-[0-9]{3}$';
    IF (SELECT count(*) FROM account.user_accounts) < 5
        OR (SELECT count(*) FROM account.customers) < 4
        OR (SELECT count(*) FROM seat.movies) < 15
        OR (SELECT count(*) FROM seat.seats) <> 30 THEN
        RAISE EXCEPTION 'Account, customer, movie or physical-seat fixture counts are incorrect.';
    END IF;
    IF (SELECT count(*) FROM seat.showtimes WHERE showtime_id ~ v_prefix) <> 59
        OR (SELECT count(*) FROM seat.showtime_seats WHERE showtime_id ~ v_prefix) <> 1770 THEN
        RAISE EXCEPTION 'Expected 59 showtimes and 1770 inventory rows for seed date %.', v_anchor;
    END IF;
    IF EXISTS (SELECT 1 FROM account.customers c LEFT JOIN account.user_accounts a USING (account_id)
        WHERE a.account_id IS NULL OR a.role <> 'USER')
        OR EXISTS (SELECT 1 FROM seat.showtimes st LEFT JOIN seat.movies m USING (movie_id) WHERE m.movie_id IS NULL)
        OR EXISTS (SELECT 1 FROM seat.showtime_seats i
            LEFT JOIN seat.showtimes st USING (showtime_id)
            LEFT JOIN seat.seats s USING (seat_id) WHERE st.showtime_id IS NULL OR s.seat_id IS NULL) THEN
        RAISE EXCEPTION 'An account or catalog reference is missing.';
    END IF;
    IF (SELECT count(*) FROM information_schema.tables
        WHERE table_schema IN ('account','booking','seat','payment') AND table_type='BASE TABLE') <> 15 THEN
        RAISE EXCEPTION 'Expected 15 demo tables; initialize a fresh database with 01-schema.sql.';
    END IF;
    IF EXISTS (SELECT 1 FROM seat.seat_holds WHERE seat_count NOT BETWEEN 1 AND 6)
        OR EXISTS (SELECT 1 FROM booking.booking_items GROUP BY booking_id HAVING count(*)>6) THEN
        RAISE EXCEPTION 'A hold or booking has an invalid seat count.';
    END IF;
    IF EXISTS (SELECT 1 FROM seat.seat_holds h
        WHERE h.status IN ('ACTIVE','CHECKOUT','PAYMENT_PROCESSING','CONSUMED')
          AND h.seat_count <> (SELECT count(*) FROM seat.showtime_seats i WHERE i.held_by_hold_id=h.hold_id)) THEN
        RAISE EXCEPTION 'Hold seat_count does not match its inventory.';
    END IF;
    FOR v_entry IN SELECT * FROM (VALUES
        ('cinema_account', 'account'), ('cinema_booking', 'booking'),
        ('cinema_seat', 'seat'), ('cinema_payment', 'payment')) AS r(role_name, schema_name)
    LOOP
        v_role := v_entry.role_name; v_schema := v_entry.schema_name;
        IF NOT has_schema_privilege(v_role, v_schema, 'USAGE') THEN
            RAISE EXCEPTION 'Role % cannot access its own schema %.', v_role, v_schema;
        END IF;
        FOREACH v_other IN ARRAY ARRAY['account', 'booking', 'seat', 'payment'] LOOP
            IF v_other <> v_schema AND has_schema_privilege(v_role, v_other, 'USAGE') THEN
                RAISE EXCEPTION 'Role % has cross-service schema access to %.', v_role, v_other;
            END IF;
        END LOOP;
    END LOOP;
    IF has_table_privilege('cinema_payment', 'payment.payment_transactions', 'UPDATE')
        OR has_table_privilege('cinema_payment', 'payment.payment_transactions', 'DELETE') THEN
        RAISE EXCEPTION 'Payment application role must not mutate ledger history.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgrelid = 'payment.payment_transactions'::regclass
        AND tgname = 'trg_payment_history_append_only' AND NOT tgisinternal AND tgenabled <> 'D') THEN
        RAISE EXCEPTION 'Missing append-only payment trigger.';
    END IF;
END;
$verify$;
SELECT current_database() AS database_name, current_setting('cinema.seed_start_date') AS seed_start_date;
SELECT 'account.user_accounts' AS data_set, count(*) AS actual_count FROM account.user_accounts
UNION ALL SELECT 'account.customers', count(*) FROM account.customers
UNION ALL SELECT 'seat.movies', count(*) FROM seat.movies
UNION ALL SELECT 'seat.seats', count(*) FROM seat.seats
UNION ALL SELECT 'seat.showtimes', count(*) FROM seat.showtimes
UNION ALL SELECT 'seat.showtime_seats', count(*) FROM seat.showtime_seats
UNION ALL SELECT 'booking.bookings', count(*) FROM booking.bookings
UNION ALL SELECT 'payment.payments', count(*) FROM payment.payments
UNION ALL SELECT 'payment.payment_transactions', count(*) FROM payment.payment_transactions;
SELECT st.showtime_id, m.title, st.starts_at, st.base_price, st.vip_surcharge
FROM seat.showtimes st JOIN seat.movies m USING (movie_id)
ORDER BY st.starts_at LIMIT 6;
COMMIT;
