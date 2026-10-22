package com.td.cinema.choreography.account.repository;

import com.td.cinema.choreography.account.dto.projection.AccountProfileView;
import com.td.cinema.choreography.account.dto.projection.AdminAccountView;
import com.td.cinema.choreography.account.entity.UserAccount;
import com.td.cinema.choreography.account.model.AccountRole;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmail(String email);

    @Query(
            "select new"
                + " com.td.cinema.choreography.account.dto.projection.AccountProfileView(a.accountId,a.email,a.role,a.active,c.customerId,c.fullName,c.customerType)"
                + " from UserAccount a left join Customer c on c.accountId=a.accountId where"
                + " a.accountId=:accountId and a.active=true")
    AccountProfileView findActiveProfile(@Param("accountId") Long accountId);

    @Query(
            "select a.accountId as accountId from UserAccount a where a.role=:role and"
                + " a.active=true order by a.accountId")
    List<AdminAccountView> findActiveAdmins(@Param("role") AccountRole role);

    @Query(
            value = "select 1 from pg_advisory_xact_lock(hashtextextended(:operationKey,0))",
            nativeQuery = true)
    Integer acquireTransactionLock(@Param("operationKey") String operationKey);

    default void lockOperation(String key) {
        acquireTransactionLock(key);
    }
}
