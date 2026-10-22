package com.td.CinemaMini.repository;

import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.Role;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmail(String email);

    List<UserAccount> findByRole(Role role);
}
