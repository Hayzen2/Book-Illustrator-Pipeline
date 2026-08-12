package com.example.BookIllustrator.util;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j // Log the events for debugging and monitoring
public class JWTUtil {
    @Value("${jwt.secret:MySuperSecretKeyForBookIllustratorPipeline2026!@@}")
    private String SECRET_KEY;
    @Value("${jwt.expiration:86400000}") // ms - 1 day
    private long EXPIRATION_TIME;

    // Algorithm: HS256
    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate JWT token
    public String generateJWTToken(String email, Long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    public Long extractUserId(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", Long.class);
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);  

        Long userId = claims.get("userId", Long.class);

        if (userId == null) {
            throw new IllegalArgumentException("JWT token does not contain userId claim");
        }

        return new UsernamePasswordAuthenticationToken(userId, null, null); // No authorities for now
    }

    // Get claims from JWT token
    public Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey()) 
            .build() 
            .parseSignedClaims(token).getPayload(); // Get the claims from the token
    }

    // Get JWT token from the Authorization header
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer "
        }
        return null;
    }

    // Validate JWT token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
            .verifyWith(getSigningKey()) 
            .build() 
            .parseSignedClaims(token);
            return true; 
        } catch (ExpiredJwtException e) {
            log.warn("Token expired");
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT");
        } catch (SecurityException e) {
            log.warn("Invalid signature");
        } catch (IllegalArgumentException e) {
            log.warn("Empty claims string");
        }
        return false;
    }

}