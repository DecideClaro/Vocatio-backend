package com.acme.vocatio.security;

import com.acme.vocatio.config.JwtProperties;
import com.acme.vocatio.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        return buildToken(user, jwtProperties.getAccessTokenTtl(), "access");
    }

    public String generateRefreshToken(User user, Duration ttl) {
        return buildToken(user, ttl, "refresh");
    }

    private String buildToken(User user, Duration ttl, String type) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttl);
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .claim("uid", user.getId())
                .claim("type", type)
                .signWith(jwtProperties.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractClaim(token, Claims::getSubject);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractClaim(token, claims -> claims.get("type", String.class)));
    }

    public Instant extractExpiration(String token) {
        return extractClaim(token, claims -> claims.getExpiration().toInstant());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            Jws<Claims> claimsJws =
                    Jwts.parserBuilder().setSigningKey(jwtProperties.getSigningKey()).build().parseClaimsJws(token);
            return claimsJws.getBody();
        } catch (SecurityException ex) {
            // Este catch SÍ es útil porque transforma la excepción y añade contexto.
            throw new IllegalArgumentException("Token inválido", ex);
        }
        // El bloque "catch (ExpiredJwtException ex) { throw ex; }" se ha eliminado.
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).isBefore(Instant.now());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }
}
