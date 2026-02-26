package com.lucio.financeapp.users.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.lucio.financeapp.users.domain.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

    private static final String USERNAME_CLAIM = "username";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        byte[] secretBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getAccessTokenMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(user.getId().toString())
                .claim(USERNAME_CLAIM, user.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token);
    }

    public UUID extractUserId(Jws<Claims> claims) {
        return UUID.fromString(claims.getPayload().getSubject());
    }

    public String extractUsername(Jws<Claims> claims) {
        Object value = claims.getPayload().get(USERNAME_CLAIM);
        return value == null ? null : value.toString();
    }
}
