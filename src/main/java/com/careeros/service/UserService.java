package com.careeros.service;

import com.careeros.dto.request.UpdateProfileRequest;
import com.careeros.dto.response.UserResponse;
import com.careeros.entity.User;
import com.careeros.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        return toResponse(user);
    }

    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        user.setName(request.getName());
        user.setTargetRole(request.getTargetRole());
        user.setExperienceLevel(request.getExperienceLevel());

        try {
            user.setTargetCompanies(
                    objectMapper.writeValueAsString(request.getTargetCompanies())
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize target companies");
        }

        user = userRepository.save(user);

        return toResponse(user);
    }

    public void deleteProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        userRepository.delete(user);
    }

    private UserResponse toResponse(User user) {
        List<String> companies = Collections.emptyList();

        try {
            if (user.getTargetCompanies() != null) {
                companies = objectMapper.readValue(
                        user.getTargetCompanies(),
                        new TypeReference<List<String>>() {}
                );
            }
        } catch (Exception ignored) {}

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .targetRole(user.getTargetRole())
                .targetCompanies(companies)
                .experienceLevel(
                        user.getExperienceLevel() != null
                                ? user.getExperienceLevel().name()
                                : null
                )
                .build();
    }
}