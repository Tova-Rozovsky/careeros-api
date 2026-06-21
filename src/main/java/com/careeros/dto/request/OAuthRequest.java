package com.careeros.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuthRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String redirectUri;
}
