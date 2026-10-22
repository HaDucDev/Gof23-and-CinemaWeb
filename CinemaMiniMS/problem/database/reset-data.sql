-- MANUAL data reset for the fresh demo layout. Stop services first; Payment restarts with SUCCESS.
-- Not called by initialize-data.ps1 or khoi-tao-data.bat.
\set ON_ERROR_STOP on
\if :{?reset_database}
\else
\echo 'Pass --set=reset_database=<exact database name> to confirm the target.'
\quit 3
\endif
\if :{?refresh_schedule}
\else
\set refresh_schedule false
\endif
BEGIN;
SELECT set_config('cinema.reset_database', :'reset_database', true);
SELECT set_config('cinema.refresh_schedule', :'refresh_schedule', true);
DO $reset$
BEGIN
    IF current_database() <> 'cinema_problem'
        OR current_setting('cinema.reset_database') <> current_database() THEN
        RAISE EXCEPTION 'Reset database confirmation does not match this connection.';
    END IF;
    TRUNCATE booking.notifications, booking.booking_timeline, booking.idempotency_requests,
        booking.booking_items, booking.bookings, seat.hold_operations, seat.seat_holds,
        payment.payment_transactions, payment.payments RESTART IDENTITY;
    IF current_setting('cinema.refresh_schedule')::boolean THEN
        TRUNCATE seat.showtime_seats, seat.showtimes RESTART IDENTITY;
        EXECUTE format('ALTER DATABASE %I RESET cinema.seed_start_date', current_database());
    ELSE
        UPDATE seat.showtime_seats SET status = 'AVAILABLE', held_by_booking_id = NULL,
            held_by_hold_id = NULL, hold_expires_at = NULL, version = version + 1 WHERE status <> 'DISABLED';
    END IF;
END;
$reset$;
COMMIT;
\echo 'Reset complete for this database only. Accounts, customers, movies and physical seats were preserved.'
