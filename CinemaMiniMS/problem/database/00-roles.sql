\set ON_ERROR_STOP on
\getenv account_password ACCOUNT_DB_PASSWORD
\getenv booking_password BOOKING_DB_PASSWORD
\getenv seat_password SEAT_DB_PASSWORD
\getenv payment_password PAYMENT_DB_PASSWORD

BEGIN;
SELECT format('CREATE ROLE %I LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION', role_name)
FROM (VALUES ('cinema_account'), ('cinema_booking'), ('cinema_seat'), ('cinema_payment')) AS r(role_name)
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = r.role_name)
\gexec

SELECT format('ALTER ROLE cinema_account PASSWORD %L', :'account_password')
\gexec
SELECT format('ALTER ROLE cinema_booking PASSWORD %L', :'booking_password')
\gexec
SELECT format('ALTER ROLE cinema_seat PASSWORD %L', :'seat_password')
\gexec
SELECT format('ALTER ROLE cinema_payment PASSWORD %L', :'payment_password')
\gexec

ALTER ROLE cinema_account SET search_path TO account, pg_catalog;
ALTER ROLE cinema_booking SET search_path TO booking, pg_catalog;
ALTER ROLE cinema_seat SET search_path TO seat, pg_catalog;
ALTER ROLE cinema_payment SET search_path TO payment, pg_catalog;
COMMIT;
