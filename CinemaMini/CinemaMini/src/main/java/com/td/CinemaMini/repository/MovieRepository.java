package com.td.CinemaMini.repository;

import com.td.CinemaMini.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findAllByOrderByIdAsc();
}
