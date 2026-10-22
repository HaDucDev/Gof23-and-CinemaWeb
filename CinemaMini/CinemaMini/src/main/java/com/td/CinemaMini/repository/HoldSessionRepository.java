package com.td.CinemaMini.repository;

import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.HoldStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;

public interface HoldSessionRepository extends JpaRepository<HoldSession, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from HoldSession h where h.id = :id")
    Optional<HoldSession> lockById(@Param("id") Long id);

    @Query(
            "select h.id from HoldSession h where h.status in :statuses and h.expiresAt <= :now order by h.id")
    List<Long> findExpiredIds(
            @Param("statuses") List<HoldStatus> statuses,
            @Param("now") Instant now,
            org.springframework.data.domain.Pageable page);

    List<HoldSession> findByCustomerIdAndShowtimeIdAndStatusInOrderByIdDesc(
            Long customerId, Long showtimeId, List<HoldStatus> statuses);
}
