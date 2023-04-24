package com.helenaramyan.cardauth.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<CardEntity, String> {

    /**
     * Loads the card with a row lock so that concurrent authorizations on the same
     * card are decided one after the other and cannot both spend the same balance.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CardEntity c where c.token = :token")
    Optional<CardEntity> lockByToken(@Param("token") String token);
}
