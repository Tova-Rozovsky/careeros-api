package com.careeros.service;

import com.careeros.dto.response.AuthResponse;
import com.careeros.dto.response.UserResponse;
import com.careeros.entity.User;
import com.careeros.repository.UserRepository;
import com.careeros.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    @Value("${oauth.google.client-id:}")
    private String googleClientId;

    @Value("${oauth.google.client-secret:}")
    private String googleClientSecret;

    @Value("${oauth.github.client-id:}")
    private String githubClientId;

    @Value("${oauth.github.client-secret:}")
    private String githubClientSecret;

    @Value("${oauth.linkedin.client-id:}")
    private String linkedinClientId;

    @Value("${oauth.linkedin.client-secret:}")
    private String linkedinClientSecret;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    public AuthResponse handleOAuth(String provider, String code, String redirectUri) {
        OAuthUserInfo userInfo = switch (provider.toLowerCase()) {
            case "google" -> fetchGoogleUser(code, redirectUri);
            case "github" -> fetchGithubUser(code, redirectUri);
            case "linkedin" -> fetchLinkedinUser(code, redirectUri);
            default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        };
        return loginOrRegister(userInfo, provider);
    }

    private AuthResponse loginOrRegister(OAuthUserInfo userInfo, String provider) {
        Optional<User> existing = userRepository.findByEmail(userInfo.email());
        boolean isNew = existing.isEmpty();

        User user = existing.orElseGet(() -> {
            User newUser = User.builder()
                    .email(userInfo.email())
                    .name(userInfo.name())
                    .passwordHash("")
                    .build();
            log.info("Auto-registering new OAuth user: {} via {}", userInfo.email(), provider);
            return userRepository.save(newUser);
        });

        if (isNew) {
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getName());
            } catch (Exception e) {
                log.warn("Failed to send welcome email: {}", e.getMessage());
            }
        }
        return buildAuthResponse(user);
    }

    private OAuthUserInfo fetchGoogleUser(String code, String redirectUri) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> tokenRequest = Map.of(
                "code", code,
                "client_id", googleClientId,
                "client_secret", googleClientSecret,
                "redirect_uri", redirectUri,
                "grant_type", "authorization_code"
        );

        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                new HttpEntity<>(tokenRequest),
                MAP_TYPE
        );

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                MAP_TYPE
        );

        Map<String, Object> body = userResponse.getBody();
        return new OAuthUserInfo((String) body.get("email"), (String) body.get("name"));
    }

    private OAuthUserInfo fetchGithubUser(String code, String redirectUri) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, String> tokenRequest = Map.of(
                "code", code,
                "client_id", githubClientId,
                "client_secret", githubClientSecret,
                "redirect_uri", redirectUri
        );

        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                "https://github.com/login/oauth/access_token",
                HttpMethod.POST,
                new HttpEntity<>(tokenRequest, tokenHeaders),
                MAP_TYPE
        );

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        userHeaders.set("User-Agent", "CareerOS");

        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                MAP_TYPE
        );

        Map<String, Object> body = userResponse.getBody();
        String email = (String) body.get("email");
        Object nameObj = body.getOrDefault("name", body.get("login"));
        String name = nameObj != null ? nameObj.toString() : "";

        if (email == null) {
            ResponseEntity<List<Map<String, Object>>> emailsResponse = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    new HttpEntity<>(userHeaders),
                    LIST_MAP_TYPE
            );
            email = emailsResponse.getBody().stream()
                    .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                    .map(e -> (String) e.get("email"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No primary email found on GitHub account"));
        }

        return new OAuthUserInfo(email, name);
    }

    private OAuthUserInfo fetchLinkedinUser(String code, String redirectUri) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String tokenBody = "grant_type=authorization_code"
                + "&code=" + code
                + "&redirect_uri=" + redirectUri
                + "&client_id=" + linkedinClientId
                + "&client_secret=" + linkedinClientSecret;

        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                "https://www.linkedin.com/oauth/v2/accessToken",
                HttpMethod.POST,
                new HttpEntity<>(tokenBody, tokenHeaders),
                MAP_TYPE
        );

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);

        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                "https://api.linkedin.com/v2/userinfo",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                MAP_TYPE
        );

        Map<String, Object> body = userResponse.getBody();
        String email = (String) body.get("email");
        Object nameObj = body.getOrDefault("name", body.get("given_name"));
        String name = nameObj != null ? nameObj.toString() : email;

        return new OAuthUserInfo(email, name);
    }

    private AuthResponse buildAuthResponse(User user) {
        String userId = user.getId().toString();
        String accessToken = jwtUtil.generateAccessToken(userId, user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                jwtUtil.getRefreshTokenExpiry(),
                TimeUnit.MILLISECONDS
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .targetRole(user.getTargetRole())
                        .experienceLevel(user.getExperienceLevel() != null ?
                                user.getExperienceLevel().name().toLowerCase() : null)
                        .build())
                .build();
    }

    record OAuthUserInfo(String email, String name) {}
}