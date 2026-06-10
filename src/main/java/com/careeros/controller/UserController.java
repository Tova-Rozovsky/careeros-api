package com.careeros.controller;

import com.careeros.dto.request.UpdateProfileRequest;
import com.careeros.dto.response.UserResponse;
import com.careeros.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // TODO: Replace with real JWT auth once BE-001 is merged

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(
            @RequestHeader("X-User-Id") UUID userId) {

        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(
                userService.updateProfile(userId, request)
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteProfile(
            @RequestHeader("X-User-Id") UUID userId) {

        userService.deleteProfile(userId);

        return ResponseEntity.noContent().build();
    }
}