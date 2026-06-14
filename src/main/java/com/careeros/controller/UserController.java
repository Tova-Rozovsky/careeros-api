package com.careeros.controller;

import com.careeros.dto.request.UpdateProfileRequest;
import com.careeros.dto.response.UserResponse;
import com.careeros.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal String userId) {

        return ResponseEntity.ok(userService.getProfile(UUID.fromString(userId)));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(
                userService.updateProfile(UUID.fromString(userId), request)
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteProfile(
            @AuthenticationPrincipal String userId) {

        userService.deleteProfile(UUID.fromString(userId));

        return ResponseEntity.noContent().build();
    }
}