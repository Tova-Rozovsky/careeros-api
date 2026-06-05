package com.careeros.dto.response;
import lombok.*;
import java.util.UUID;

@Data @Builder
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String targetRole;
    private String experienceLevel;
}