package com.ssafy.yumyum.global.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.ssafy.yumyum.domain.user.entity.User;
import com.ssafy.yumyum.domain.admin.entity.AdminUser;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenProvider {
    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + jwtProperties.getAccessExpiredAt()), user);
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + jwtProperties.getRefreshExpiredAt()), user);
    }

    public String generateAdminAccessToken(AdminUser admin) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessExpiredAt());
        try {
            List<String> authorities = List.of("ROLE_" + admin.getRole().name());
            return Jwts.builder()
                    .header().type("JWT").and()
                    .issuer(jwtProperties.getIssuer())
                    .issuedAt(now)
                    .expiration(expiry)
                    .subject(String.valueOf(admin.getId()))
                    .claim("authorities", authorities)
                    .claim("token_kind", "ADMIN")
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception exception) {
            throw new BusinessException(ExceptionType.GENERATE_TOKEN_ERROR);
        }
    }

    public boolean isAdminToken(String token) {
        return "ADMIN".equals(getClaims(token).get("token_kind", String.class));
    }

    private String makeToken(Date expiry, User user) {
        try {
            List<String> authorities = List.of("ROLE_" + user.getRole());
            Date now = new Date();

            return Jwts.builder()
                    .header()
                        .type("JWT")
                        .and()
                    .issuer(jwtProperties.getIssuer())
                    .issuedAt(now)
                    .expiration(expiry)
                    .subject(String.valueOf(user.getId()))
                    .claim("authorities", authorities)
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception e) {
            log.debug("Failed to generate token", e);
            throw new BusinessException(ExceptionType.GENERATE_TOKEN_ERROR);
        }
    }

    public void validToken(String token, TokenType tokenType, HttpServletResponse response) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
                
        } catch (ExpiredJwtException e) {
            if (tokenType == TokenType.ACCESS) {
                clearAccessTokenCookie(response);
                throw new BusinessException(ExceptionType.EXPIRED_ACCESS_TOKEN);
            } else {
                throw new BusinessException(ExceptionType.EXPIRED_REFRESH_TOKEN);
            }
        } catch (JwtException | IllegalArgumentException e) {
            if (tokenType == TokenType.ACCESS) {
                throw new BusinessException(ExceptionType.INVALID_ACCESS_TOKEN);
            } else {
                throw new BusinessException(ExceptionType.INVALID_REFRESH_TOKEN);
            }
        }
    }

    public boolean getUserIdFromToken(String token, Long userId) {
        Long tokenId = getClaims(token).get("id", Long.class);
        return tokenId.equals(userId);
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        String secretKey = jwtProperties.getSecretKey();
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("access-token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh-token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
