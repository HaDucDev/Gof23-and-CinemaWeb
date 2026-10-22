package com.td.CinemaMini.repository;

import com.td.CinemaMini.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingIdOrderByCreatedAtAsc(Long bookingId);

    List<Payment> findAllByOrderByCreatedAtDesc();

    Optional<Payment> findByBookingIdAndOperationType(Long bookingId, String operationType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.bookingId = :bookingId and p.operationType = 'PAYMENT'")
    Optional<Payment> lockOriginal(@Param("bookingId") Long bookingId);
}
