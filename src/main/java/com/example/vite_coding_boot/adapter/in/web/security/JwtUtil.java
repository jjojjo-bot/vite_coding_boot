package com.example.vite_coding_boot.adapter.in.web.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.example.vite_coding_boot.domain.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtUtil {

    private static final long EXPIRATION_MS = 8 * 60 * 60 * 1000; // 8 hours
    private static final long OTP_TEMP_EXPIRATION_MS = 10 * 60 * 1000; // 10 minutes

    private final SecretKey secretKey = Jwts.SIG.HS256.key().build();

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }

    public String generateOtpTempToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "otp-temp")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + OTP_TEMP_EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }

    public String generateOtpSetupToken(String tempSecret) {
        return Jwts.builder()
                .subject("otp-setup")
                .claim("secret", tempSecret)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + OTP_TEMP_EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }

    public Long extractUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    public String extractOtpSetupSecret(String token) {
        Claims claims = parseClaims(token);
        return claims.get("secret", String.class);
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
