package com.careeros.controller;
// TODO: BE-001 — Implement this controller
// Issue: github.com/career-os/careeros-api/issues/1
import com.careeros.dto.request.*;
import com.careeros.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        throw new UnsupportedOperationException("BE-001 not yet implemented");
    }
    @PostMapping("/login")
    @Operation(summary = "Login and get JWT tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        throw new UnsupportedOperationException("BE-001 not yet implemented");
    }
    @GetMapping("/me")
    @Operation(summary = "Get current user")
    public ResponseEntity<?> me() {
        throw new UnsupportedOperationException("BE-002 not yet implemented");
    }
    @PostMapping("/logout")
    @Operation(summary = "Logout")
    public ResponseEntity<?> logout() {
        throw new UnsupportedOperationException("BE-001 not yet implemented");
    }
}