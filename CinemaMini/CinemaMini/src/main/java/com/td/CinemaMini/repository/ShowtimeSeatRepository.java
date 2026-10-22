package com.td.CinemaMini.repository;

import com.td.CinemaMini.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;

public interface ShowtimeSeatRepository extends JpaRepository<ShowtimeSeat, Long> {
    List<ShowtimeSeat> findByShowtimeIdOrderByIdAsc(Long showtimeId);

    List<ShowtimeSeat> findByHoldIdOrderByIdAsc(Long holdId);

    List<ShowtimeSeat> findByBookingIdOrderByIdAsc(Long bookingId);

    Optional<ShowtimeSeat> findByShowtimeIdAndSeatId(Long showtimeId, Long seatId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ShowtimeSeat s where s.id = :id")
    Optional<ShowtimeSeat> lockById(@Param("id") Long id);
}
