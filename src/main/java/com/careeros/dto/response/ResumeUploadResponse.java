package com.careeros.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResumeUploadResponse(
        UUID id,
        String fileName,
        String storagePath,
        LocalDateTime createdAt
) {
}