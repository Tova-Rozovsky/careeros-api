package com.careeros.dto.response;
import lombok.*;
import java.util.UUID;
import java.util.List;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String targetRole;
    private List<String> targetCompanies;
    private String experienceLevel;
}