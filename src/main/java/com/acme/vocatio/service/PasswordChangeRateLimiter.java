package com.acme.vocatio.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Controla los intentos fallidos de cambio de contraseña para evitar ataques de fuerza bruta.
 */
@Component
public class PasswordChangeRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final Map<Long, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(Long userId) {
        Attempt attempt = attempts.get(userId);
        if (attempt == null || attempt.blockedUntil == null) {
            return false;
        }

        Instant now = Instant.now();
        if (now.isAfter(attempt.blockedUntil)) {
            attempts.remove(userId);
            return false;
        }
        return true;
    }

    public Duration getRemainingLockDuration(Long userId) {
        Attempt attempt = attempts.get(userId);
        if (attempt == null || attempt.blockedUntil == null) {
            return Duration.ZERO;
        }

        Instant now = Instant.now();
        if (now.isAfter(attempt.blockedUntil)) {
            attempts.remove(userId);
            return Duration.ZERO;
        }
        return Duration.between(now, attempt.blockedUntil);
    }

    public void recordFailure(Long userId) {
        Instant now = Instant.now();
        attempts.compute(userId, (id, current) -> {
            if (current == null) {
                Attempt attempt = new Attempt();
                attempt.failures = 1;
                return attempt;
            }

            if (current.blockedUntil != null) {
                if (now.isAfter(current.blockedUntil)) {
                    current.blockedUntil = null;
                    current.failures = 1;
                }
                return current;
            }

            current.failures += 1;
            if (current.failures >= MAX_ATTEMPTS) {
                current.failures = 0;
                current.blockedUntil = now.plus(LOCK_DURATION);
            }
            return current;
        });
    }

    public void reset(Long userId) {
        attempts.remove(userId);
    }

    private static final class Attempt {
        int failures;
        Instant blockedUntil;
    }
}
