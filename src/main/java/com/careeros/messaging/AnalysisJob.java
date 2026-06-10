package com.careeros.messaging;

import lombok.*;
import java.io.Serializable;

/**
 * Message payload sent to RabbitMQ queue.
 * Contains everything the consumer needs to run the analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisJob implements Serializable {
    private String jobId;       // UUID for tracking
    private String resumeId;    // FK to resumes table
    private String userId;      // FK to users table
    private String resumeText;  // Extracted plain text from resume
    private String jdText;      // Optional job description
    private int retryCount;     // Track retries (max 3)
}
