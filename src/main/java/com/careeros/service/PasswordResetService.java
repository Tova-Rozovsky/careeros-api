package com.careeros.service;

import com.careeros.repository.UserRepository;
import com.careeros.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Password reset service.
 *
 * Flow:
 * 1. User submits email → generateResetToken() stores token in Redis (15 min TTL)
 * 2. Email sent with reset link: /reset-password?token=xxx
 * 3. User clicks link → resetPassword() validates token, updates password, deletes token
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    private static final String RESET_TOKEN_PREFIX = "pwd-reset:";
    private static final long RESET_TOKEN_TTL_MINUTES = 15;

    /**
     * Generate a reset token and send email.
     * Always returns success (don't reveal if email exists — security best practice).
     */
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            String redisKey = RESET_TOKEN_PREFIX + token;

            // Store userId against token in Redis with 15-min TTL
            redisTemplate.opsForValue().set(
                    redisKey,
                    user.getId().toString(),
                    RESET_TOKEN_TTL_MINUTES,
                    TimeUnit.MINUTES
            );

            emailService.sendPasswordResetEmail(email, token);
            log.info("Password reset token generated for user: {}", email);
        });
    }

    /**
     * Validate reset token and update password.
     */
    public void resetPassword(String token, String newPassword) {
        String redisKey = RESET_TOKEN_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(redisKey);

        if (userId == null) {
            throw new IllegalArgumentException("Reset token is invalid or has expired");
        }

        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete token so it can't be reused
        redisTemplate.delete(redisKey);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    /**
     * Check if a reset token is still valid (for frontend to verify before showing form).
     */
    public boolean isTokenValid(String token) {
        return redisTemplate.hasKey(RESET_TOKEN_PREFIX + token);
    }
}
