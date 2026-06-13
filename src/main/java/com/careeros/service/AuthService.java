package com.careeros.service;

import com.careeros.dto.request.LoginRequest;
import com.careeros.dto.request.RegisterRequest;
import com.careeros.dto.response.AuthResponse;
import com.careeros.dto.response.UserResponse;
import com.careeros.entity.User;
import com.careeros.repository.UserRepository;
import com.careeros.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);
        log.info("User registered: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }
        String userId = jwtUtil.extractUserId(refreshToken);
        String stored = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (!refreshToken.equals(stored)) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return buildAuthResponse(user);
    }

    public void logout(String userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        log.info("User logged out: {}", userId);
    }

    public UserResponse getCurrentUser(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toUserResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String userId = user.getId().toString();
        String accessToken = jwtUtil.generateAccessToken(userId, user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        redisTemplate.opsForValue().set(
            REFRESH_TOKEN_PREFIX + userId, refreshToken,
            jwtUtil.getRefreshTokenExpiry(), TimeUnit.MILLISECONDS);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserResponse(user))
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .targetRole(user.getTargetRole())
                .experienceLevel(user.getExperienceLevel() != null ?
                    user.getExperienceLevel().name().toLowerCase() : null)
                .build();
    }
}
