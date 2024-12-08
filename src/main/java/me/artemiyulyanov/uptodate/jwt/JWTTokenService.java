package me.artemiyulyanov.uptodate.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class JWTTokenService {
    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> blacklistedTokens;

    public String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }

    public String extractUsername(HttpServletRequest request) {
        String token = extractToken(request);

        if (token != null && validateToken(token)) {
            return jwtUtil.extractUsername(token);
        }

        return null;
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token) && !isTokenBlacklisted(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(blacklistedTokens.hasKey(token));
    }

    public boolean logout(String token) {
        if (!validateToken(token)) return false;

        SecurityContextHolder.clearContext();
        blacklistedTokens.opsForValue().set(token, "blacklisted", Duration.ofHours(1));
        return true;
    }
}