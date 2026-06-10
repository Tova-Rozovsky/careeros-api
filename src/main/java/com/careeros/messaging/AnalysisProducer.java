package com.careeros.messaging;

import com.careeros.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * BE-004 — Analysis Producer
 *
 * Triggered by POST /api/resumes/{id}/analyze
 * 1. Creates a job with unique jobId
 * 2. Stores "pending" status in Redis
 * 3. Publishes job to RabbitMQ queue
 * 4. Returns jobId to frontend for polling
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisProducer {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    // Redis key pattern: analysis:{resumeId}
    private static final String REDIS_KEY_PREFIX = "analysis:";
    // Job expires from Redis after 24 hours
    private static final long JOB_TTL_HOURS = 24;

    /**
     * Enqueue an analysis job for the given resume.
     *
     * @param resumeId   ID of the resume to analyze
     * @param userId     ID of the user who owns the resume
     * @param resumeText Plain text extracted from the resume file
     * @param jdText     Optional job description for keyword matching
     * @return jobId     Unique ID the frontend uses to poll for results
     */
    public String enqueueAnalysis(String resumeId, String userId,
                                   String resumeText, String jdText) {
        String jobId = UUID.randomUUID().toString();
        String redisKey = REDIS_KEY_PREFIX + resumeId;

        // Store initial pending status in Redis
        // Frontend polls this key until status = completed
        redisTemplate.opsForValue().set(
            redisKey,
            "pending:" + jobId,
            JOB_TTL_HOURS,
            TimeUnit.HOURS
        );

        // Build the job message
        AnalysisJob job = AnalysisJob.builder()
                .jobId(jobId)
                .resumeId(resumeId)
                .userId(userId)
                .resumeText(resumeText)
                .jdText(jdText)
                .retryCount(0)
                .build();

        // Publish to RabbitMQ
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ANALYSIS_EXCHANGE,
            RabbitMQConfig.ANALYSIS_ROUTING_KEY,
            job
        );

        log.info("Analysis job enqueued — jobId: {}, resumeId: {}", jobId, resumeId);
        return jobId;
    }

    /**
     * Get current analysis status for a resume.
     * Returns: "pending:{jobId}" | "completed:{json}" | "failed:{reason}" | null
     */
    public String getAnalysisStatus(String resumeId) {
        return redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + resumeId);
    }
}
