package com.td.cinema.payment.repository;

import com.td.cinema.payment.dto.projection.PaymentTransactionView;
import com.td.cinema.payment.dto.projection.PaymentView;
import com.td.cinema.payment.entity.Payment;
import com.td.cinema.payment.mapper.RepositoryProjectionMapper;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Transactional(readOnly = true)
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query(
            value = "select 1 from pg_advisory_xact_lock(hashtextextended(:operationKey,0))",
            nativeQuery = true)
    Integer acquireTransactionLock(@Param("operationKey") String operationKey);

    default void lockOperation(String key) {
        acquireTransactionLock(key);
    }

    @Query(
            value =
                    "select to_char(clock_timestamp() AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"')",
            nativeQuery = true)
    String databaseClock();

    default OffsetDateTime currentTime() {
        return OffsetDateTime.parse(databaseClock());
    }

    @Query(
            value =
                    "select CAST(payments.payment_id AS varchar) AS \"paymentId\","
                        + " CAST(payments.booking_id AS varchar) AS \"bookingId\", payments.amount"
                        + " AS \"amount\", payments.status AS \"status\", payments.idempotency_key"
                        + " AS \"idempotencyKey\", payments.request_hash AS \"requestHash\","
                        + " payments.payment_method AS \"paymentMethod\","
                        + " payments.simulated_outcome AS \"simulatedOutcome\","
                        + " payments.failure_reason AS \"failureReason\","
                        + " to_char(payments.created_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\","
                        + " to_char(payments.updated_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"updatedAt\" from"
                        + " payment.payments where booking_id=CAST(:bookingId AS uuid) or"
                        + " idempotency_key=:operationKey",
            nativeQuery = true)
    PaymentView loadFindChargeByBookingOrKey(
            @Param("bookingId") String bookingId, @Param("operationKey") String operationKey);

    default Map<String, Object> findChargeByBookingOrKey(String bookingId, String operationKey) {
        return RepositoryProjectionMapper.optional(
                loadFindChargeByBookingOrKey(bookingId, operationKey));
    }

    @Transactional
    default int insertPayment(
            String paymentId,
            String bookingId,
            BigDecimal amount,
            String status,
            String operationKey,
            String fingerprint,
            String paymentMethod,
            String outcome,
            String failureReason) {
        Payment value = new Payment();
        value.setPaymentId(UUID.fromString(paymentId));
        value.setBookingId(UUID.fromString(bookingId));
        value.setAmount(amount);
        value.setStatus(status);
        value.setIdempotencyKey(operationKey);
        value.setRequestHash(fingerprint);
        value.setPaymentMethod(paymentMethod);
        value.setSimulatedOutcome(outcome);
        value.setFailureReason(failureReason);
        saveAndFlush(value);
        return 1;
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "insert into"
                        + " payment.payment_transactions(payment_id,booking_id,operation_type,amount,payment_method,idempotency_key,reference_code,reason)"
                        + " values(CAST(:paymentId AS uuid),CAST(:bookingId AS"
                        + " uuid),'PAYMENT',:amount,:paymentMethod,:operationKey,:reference,:reason)",
            nativeQuery = true)
    int appendCharge(
            @Param("paymentId") String paymentId,
            @Param("bookingId") String bookingId,
            @Param("amount") BigDecimal amount,
            @Param("paymentMethod") String paymentMethod,
            @Param("operationKey") String operationKey,
            @Param("reference") String reference,
            @Param("reason") String reason);

    @Query(
            value =
                    "select CAST(payments.payment_id AS varchar) AS \"paymentId\","
                        + " CAST(payments.booking_id AS varchar) AS \"bookingId\", payments.amount"
                        + " AS \"amount\", payments.status AS \"status\", payments.idempotency_key"
                        + " AS \"idempotencyKey\", payments.request_hash AS \"requestHash\","
                        + " payments.payment_method AS \"paymentMethod\","
                        + " payments.simulated_outcome AS \"simulatedOutcome\","
                        + " payments.failure_reason AS \"failureReason\","
                        + " to_char(payments.created_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\","
                        + " to_char(payments.updated_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"updatedAt\" from"
                        + " payment.payments where payment_id=CAST(:paymentId AS uuid) for update",
            nativeQuery = true)
    PaymentView loadLockPayment(@Param("paymentId") String paymentId);

    default Map<String, Object> lockPayment(String paymentId) {
        return RepositoryProjectionMapper.required(loadLockPayment(paymentId));
    }

    @Query(
            value =
                    "select CAST(payment_transactions.transaction_id AS varchar) AS"
                        + " \"transactionId\", CAST(payment_transactions.payment_id AS varchar) AS"
                        + " \"paymentId\", CAST(payment_transactions.booking_id AS varchar) AS"
                        + " \"bookingId\", payment_transactions.operation_type AS"
                        + " \"operationType\", payment_transactions.amount AS \"amount\","
                        + " payment_transactions.currency AS \"currency\","
                        + " payment_transactions.payment_method AS \"paymentMethod\","
                        + " CAST(payment_transactions.original_transaction_id AS varchar) AS"
                        + " \"originalTransactionId\", payment_transactions.idempotency_key AS"
                        + " \"idempotencyKey\", payment_transactions.reference_code AS"
                        + " \"referenceCode\", to_char(payment_transactions.created_at AT TIME ZONE"
                        + " 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\","
                        + " payment_transactions.reason AS \"reason\" from"
                        + " payment.payment_transactions where payment_id=CAST(:paymentId AS uuid)"
                        + " and operation_type='REFUND'",
            nativeQuery = true)
    PaymentTransactionView loadFindRefund(@Param("paymentId") String paymentId);

    default Map<String, Object> findRefund(String paymentId) {
        return RepositoryProjectionMapper.optional(loadFindRefund(paymentId));
    }

    @Query(
            value =
                    "select CAST(payment_transactions.transaction_id AS varchar) AS"
                        + " \"transactionId\", CAST(payment_transactions.payment_id AS varchar) AS"
                        + " \"paymentId\", CAST(payment_transactions.booking_id AS varchar) AS"
                        + " \"bookingId\", payment_transactions.operation_type AS"
                        + " \"operationType\", payment_transactions.amount AS \"amount\","
                        + " payment_transactions.currency AS \"currency\","
                        + " payment_transactions.payment_method AS \"paymentMethod\","
                        + " CAST(payment_transactions.original_transaction_id AS varchar) AS"
                        + " \"originalTransactionId\", payment_transactions.idempotency_key AS"
                        + " \"idempotencyKey\", payment_transactions.reference_code AS"
                        + " \"referenceCode\", to_char(payment_transactions.created_at AT TIME ZONE"
                        + " 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\","
                        + " payment_transactions.reason AS \"reason\" from"
                        + " payment.payment_transactions where payment_id=CAST(:paymentId AS uuid)"
                        + " and operation_type='PAYMENT'",
            nativeQuery = true)
    PaymentTransactionView loadFindOriginalCharge(@Param("paymentId") String paymentId);

    default Map<String, Object> findOriginalCharge(String paymentId) {
        return RepositoryProjectionMapper.required(loadFindOriginalCharge(paymentId));
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "insert into"
                        + " payment.payment_transactions(payment_id,booking_id,operation_type,amount,payment_method,original_transaction_id,idempotency_key,reference_code,reason)"
                        + " values(CAST(:paymentId AS uuid),CAST(:bookingId AS"
                        + " uuid),'REFUND',:amount,:paymentMethod,CAST(:originalTransactionId AS"
                        + " uuid),:operationKey,:reference,:reason)",
            nativeQuery = true)
    int appendRefund(
            @Param("paymentId") String paymentId,
            @Param("bookingId") String bookingId,
            @Param("amount") BigDecimal amount,
            @Param("paymentMethod") String paymentMethod,
            @Param("originalTransactionId") String originalTransactionId,
            @Param("operationKey") String operationKey,
            @Param("reference") String reference,
            @Param("reason") String reason);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update payment.payments set status='REFUNDED',updated_at=clock_timestamp()"
                        + " where payment_id=CAST(:paymentId AS uuid)",
            nativeQuery = true)
    int markRefunded(@Param("paymentId") String paymentId);

    @Query(
            value =
                    "select CAST(payments.payment_id AS varchar) AS \"paymentId\","
                        + " CAST(payments.booking_id AS varchar) AS \"bookingId\", payments.amount"
                        + " AS \"amount\", payments.status AS \"status\", payments.idempotency_key"
                        + " AS \"idempotencyKey\", payments.request_hash AS \"requestHash\","
                        + " payments.payment_method AS \"paymentMethod\","
                        + " payments.simulated_outcome AS \"simulatedOutcome\","
                        + " payments.failure_reason AS \"failureReason\","
                        + " to_char(payments.created_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\","
                        + " to_char(payments.updated_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"updatedAt\" from"
                        + " payment.payments where payment_id=CAST(:paymentId AS uuid)",
            nativeQuery = true)
    PaymentView loadFindPayment(@Param("paymentId") String paymentId);

    default Map<String, Object> findPayment(String paymentId) {
        return RepositoryProjectionMapper.required(loadFindPayment(paymentId));
    }

    @Query(
            value =
                    "select CAST(payments.payment_id AS varchar) AS \"paymentId\","
                        + " CAST(payments.booking_id AS varchar) AS \"bookingId\", payments.amount"
                        + " AS \"amount\", payments.status AS \"status\", payments.idempotency_key"
                        + " AS \"idempotencyKey\", payments.request_hash AS \"requestHash\","
                        + " payments.payment_method AS \"paymentMethod\","
                        + " payments.simulated_outcome AS \"simulatedOutcome\","
                        + " payments.failure_reason AS \"failureReason\","
                        + " to_char(payments.created_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\","
                        + " to_char(payments.updated_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"updatedAt\" from"
                        + " payment.payments where booking_id=CAST(:bookingId AS uuid)",
            nativeQuery = true)
    PaymentView loadFindByBooking(@Param("bookingId") String bookingId);

    default Map<String, Object> findByBooking(String bookingId) {
        return RepositoryProjectionMapper.optional(loadFindByBooking(bookingId));
    }

    @Query(
            value =
                    "select CAST(payment_transactions.transaction_id AS varchar) AS"
                        + " \"transactionId\", CAST(payment_transactions.payment_id AS varchar) AS"
                        + " \"paymentId\", CAST(payment_transactions.booking_id AS varchar) AS"
                        + " \"bookingId\", payment_transactions.operation_type AS"
                        + " \"operationType\", payment_transactions.amount AS \"amount\","
                        + " payment_transactions.currency AS \"currency\","
                        + " payment_transactions.payment_method AS \"paymentMethod\","
                        + " CAST(payment_transactions.original_transaction_id AS varchar) AS"
                        + " \"originalTransactionId\", payment_transactions.idempotency_key AS"
                        + " \"idempotencyKey\", payment_transactions.reference_code AS"
                        + " \"referenceCode\", to_char(payment_transactions.created_at AT TIME ZONE"
                        + " 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\","
                        + " payment_transactions.reason AS \"reason\" from"
                        + " payment.payment_transactions where booking_id=CAST(:bookingId AS uuid)"
                        + " order by created_at,transaction_id",
            nativeQuery = true)
    List<PaymentTransactionView> loadFindTransactionsByBooking(
            @Param("bookingId") String bookingId);

    default List<Map<String, Object>> findTransactionsByBooking(String bookingId) {
        return RepositoryProjectionMapper.rows(loadFindTransactionsByBooking(bookingId));
    }

    @Query(
            value =
                    "select CAST(payment_transactions.transaction_id AS varchar) AS"
                        + " \"transactionId\", CAST(payment_transactions.payment_id AS varchar) AS"
                        + " \"paymentId\", CAST(payment_transactions.booking_id AS varchar) AS"
                        + " \"bookingId\", payment_transactions.operation_type AS"
                        + " \"operationType\", payment_transactions.amount AS \"amount\","
                        + " payment_transactions.currency AS \"currency\","
                        + " payment_transactions.payment_method AS \"paymentMethod\","
                        + " CAST(payment_transactions.original_transaction_id AS varchar) AS"
                        + " \"originalTransactionId\", payment_transactions.idempotency_key AS"
                        + " \"idempotencyKey\", payment_transactions.reference_code AS"
                        + " \"referenceCode\", to_char(payment_transactions.created_at AT TIME ZONE"
                        + " 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\","
                        + " payment_transactions.reason AS \"reason\" from"
                        + " payment.payment_transactions order by created_at desc,transaction_id"
                        + " limit :limit",
            nativeQuery = true)
    List<PaymentTransactionView> loadFindLedger(@Param("limit") int limit);

    default List<Map<String, Object>> findLedger(int limit) {
        return RepositoryProjectionMapper.rows(loadFindLedger(limit));
    }
}
