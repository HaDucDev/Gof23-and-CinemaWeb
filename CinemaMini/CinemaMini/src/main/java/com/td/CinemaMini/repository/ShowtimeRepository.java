package com.td.CinemaMini.repository;

import com.td.CinemaMini.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    List<Showtime> findByStartsAtAfterOrderByStartsAtAsc(Instant now);

    List<Showtime> findAllByOrderByStartsAtDesc();

    @Query("select count(s) from Showtime s where s.startsAt < :end and s.roomAvailableAt > :start")
    long countOverlaps(@Param("start") Instant start, @Param("end") Instant end);
}
