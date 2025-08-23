package com.sammedsp.fintrack.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class CookieService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key JWT_SECRET_KEY;
    private final long JWT_COOKIE_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    @PostConstruct
    public void init(){
        this.JWT_SECRET_KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public ResponseCookie getResponseCookie(String userId) {
        String cookie = this.getJwtToken(userId);

        return ResponseCookie.from("fintrack-auth-token", cookie)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(this.JWT_COOKIE_EXPIRATION / 1000)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie getResponseCookieForLogout() {

        return ResponseCookie.from("fintrack-auth-token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    public String extractUserId(String cookie) {
        return Jwts.parserBuilder()
                .setSigningKey(this.JWT_SECRET_KEY)
                .build()
                .parseClaimsJws(cookie)
                .getBody()
                .getSubject();
    }

    public boolean isValidCookie(String cookie){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(this.JWT_SECRET_KEY)
                    .build()
                    .parseClaimsJws(cookie);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getJwtToken(String userId){

        return Jwts
                .builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + this.JWT_COOKIE_EXPIRATION))
                .signWith(this.JWT_SECRET_KEY)
                .compact();
    }
}

