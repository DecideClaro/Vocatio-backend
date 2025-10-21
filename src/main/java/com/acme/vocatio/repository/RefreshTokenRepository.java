package com.acme.vocatio.repository;

import com.acme.vocatio.model.RefreshToken;
import com.acme.vocatio.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user AND rt.revoked = false")
    int revokeTokensForUser(@Param("user") User user);

    List<RefreshToken> findAllByUserAndRevokedFalse(User user);

    Optional<RefreshToken> findByUserAndRevokedFalseAndExpiresAtAfter(User user, Instant instant);

    void deleteByUser(User user);
}
