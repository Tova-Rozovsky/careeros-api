package com.careeros.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Size(min = 2, max = 100) private String name;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 8) private String password;
}