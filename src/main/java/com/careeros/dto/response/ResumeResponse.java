package com.careeros.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResumeResponse(
        UUID id,
        String fileName,
        Integer version,
        Boolean isActive,
        LocalDateTime createdAt
) {
}