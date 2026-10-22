package com.td.CinemaMini.repository;

import com.td.CinemaMini.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;

public interface IdempotencyRequestRepository extends JpaRepository<IdempotencyRequest, Long> {
    @Modifying
    @Query(
            value =
                    "insert into idempotency_request(actor_id, request_key, operation_type, payload_fingerprint) values (:actor, :key, :operation, :fingerprint) on conflict (actor_id, request_key) do nothing",
            nativeQuery = true)
    int insertIfMissing(
            @Param("actor") Long actor,
            @Param("key") String key,
            @Param("operation") String operation,
            @Param("fingerprint") String fingerprint);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from IdempotencyRequest r where r.actorId = :actor and r.requestKey = :key")
    Optional<IdempotencyRequest> lockRequest(@Param("actor") Long actor, @Param("key") String key);
}
