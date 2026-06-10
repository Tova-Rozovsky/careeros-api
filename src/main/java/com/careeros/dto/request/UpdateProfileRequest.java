package com.careeros.dto.request;

import com.careeros.entity.User;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100)
    private String name;

    private String targetRole;

    private List<String> targetCompanies;

    private User.ExperienceLevel experienceLevel;
}