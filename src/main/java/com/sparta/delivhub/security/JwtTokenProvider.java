package com.sparta.delivhub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "auth";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity-ms}")
    private long accessTokenValidityMs;

    @Value("${jwt.refresh-token-validity-ms}")
    private long refreshTokenValidityMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 생성
    public String createAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
        return createToken(username, authorities, accessTokenValidityMs);
    }

    // Refresh Token 생성
    public String createRefreshToken(String username) {
        return createToken(username, Collections.emptyList(), refreshTokenValidityMs);
    }

    // Token 생성
    private String createToken(String username, Collection<? extends GrantedAuthority> authorities, long validityMs) {
        String authoritiesString = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Token 생성 시간
        Date now = new Date();

        // Token 만료 시간
        Date expiry = new Date(now.getTime() + validityMs);

        return Jwts.builder()
                .subject(username)
                .claim(AUTHORITIES_KEY, authoritiesString)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    // Payload 에서 username 추출
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }


    // Token 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | io.jsonwebtoken.MalformedJwtException e) {
            log.warn("잘못된 JWT 서명");
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("만료된 JWT");
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims 비어 있음");
        }
        return false;
    }

    // Token 에서 Payload 추출
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // Token 남은 만료 시간 - Redis RT 저장 TTL 설정용
    public long getRemainingExpiration(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

}
