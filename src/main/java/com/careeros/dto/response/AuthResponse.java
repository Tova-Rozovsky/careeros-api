package com.careeros.dto.response;
import lombok.*;

@Data @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserResponse user;
}