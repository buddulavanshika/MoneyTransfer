package com.mts.application.repository;
import com.mts.application.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String>{

    @Query("select a from Account a where a.id = :id")
    Optional<Account> findWithLockById(@Param("id") String id);

}
