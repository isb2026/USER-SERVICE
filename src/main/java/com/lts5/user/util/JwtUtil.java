package com.lts5.user.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "sdkjfn134nfds52ofnsudof11n2nu132012nsud0912o";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 3L * 24 * 60 * 60 * 1000;  // 3일
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 7L * 24 * 60 * 60 * 1000; // 7일

    public static String generateAccessToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    public static long getAccessTokenExpirationTime() {
        return ACCESS_TOKEN_EXPIRATION_TIME;
    }

    public static long getRefreshTokenExpirationTime() {
        return REFRESH_TOKEN_EXPIRATION_TIME;
    }

    /**
     * Authorization 헤더에서 JWT 토큰을 추출합니다.
     * @param authHeader Authorization 헤더 값 (예: "Bearer eyJhbGciOiJIUzI1NiJ9...")
     * @return JWT 토큰 문자열, 유효하지 않은 경우 null
     */
    public static String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // "Bearer " 제거
        }
        return null;
    }

    /**
     * HttpServletRequest에서 Authorization 헤더를 읽어 JWT 토큰을 추출합니다.
     * @param request HttpServletRequest 객체
     * @return JWT 토큰 문자열, 유효하지 않은 경우 null
     */
    public static String extractTokenFromRequest(jakarta.servlet.http.HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return extractTokenFromHeader(authHeader);
    }
}
