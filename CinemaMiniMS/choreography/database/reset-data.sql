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
DECLARE
    v_schema text;
    v_table text;
BEGIN
    IF current_database() <> 'cinema_choreography'
        OR current_setting('cinema.reset_database') <> current_database() THEN
        RAISE EXCEPTION 'Reset database confirmation does not match this connection.';
    END IF;
    TRUNCATE booking.notifications, booking.booking_timeline, booking.idempotency_requests,
        booking.booking_items, booking.bookings, seat.hold_operations, seat.seat_holds,
        payment.payment_transactions, payment.payments RESTART IDENTITY;
    FOREACH v_schema IN ARRAY ARRAY['booking', 'seat', 'payment'] LOOP
        FOREACH v_table IN ARRAY ARRAY['received_events'] LOOP
            IF to_regclass(format('%I.%I', v_schema, v_table)) IS NOT NULL THEN
                EXECUTE format('TRUNCATE TABLE %I.%I RESTART IDENTITY', v_schema, v_table);
            END IF;
        END LOOP;
    END LOOP;
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
