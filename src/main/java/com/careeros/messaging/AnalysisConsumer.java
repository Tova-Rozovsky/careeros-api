package com.careeros.messaging;

import com.careeros.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * BE-004 — Analysis Consumer
 *
 * Listens to RabbitMQ queue, calls careeros-ai, stores result in Redis.
 *
 * Flow:
 * 1. Receives AnalysisJob from queue
 * 2. Calls POST http://careeros-ai:8090/analyze
 * 3. On success → stores result in Redis as "completed:{json}"
 * 4. On failure → retries up to 3 times, then marks as "failed"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisConsumer {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai-service.url:http://localhost:8090}")
    private String aiServiceUrl;

    private static final String REDIS_KEY_PREFIX = "analysis:";
    private static final long RESULT_TTL_HOURS = 24;
    private static final int MAX_RETRIES = 3;

    /**
     * Listen to the analysis queue.
     * Spring AMQP handles deserialization via Jackson2JsonMessageConverter.
     */
    @RabbitListener(queues = RabbitMQConfig.ANALYSIS_QUEUE)
    public void consumeAnalysisJob(AnalysisJob job) {
        log.info("Processing analysis job — jobId: {}, resumeId: {}",
                 job.getJobId(), job.getResumeId());

        String redisKey = REDIS_KEY_PREFIX + job.getResumeId();

        try {
            // Call careeros-ai service
            AnalysisResult result = callAiService(job);

            // Store completed result in Redis as JSON
            String resultJson = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(
                redisKey,
                "completed:" + resultJson,
                RESULT_TTL_HOURS,
                TimeUnit.HOURS
            );

            log.info("Analysis completed — jobId: {}, score: {}",
                     job.getJobId(), result.getOverallScore());

        } catch (Exception e) {
            log.error("Analysis failed — jobId: {}, error: {}",
                      job.getJobId(), e.getMessage());

            // Check if we should retry
            if (job.getRetryCount() < MAX_RETRIES) {
                log.info("Retrying job — attempt {}/{}", job.getRetryCount() + 1, MAX_RETRIES);
                job.setRetryCount(job.getRetryCount() + 1);
                // Re-queue for retry
                // (In production, use exponential backoff with delay queue)
                throw new RuntimeException("Retry job: " + e.getMessage());
            }

            // Max retries reached — mark as failed
            redisTemplate.opsForValue().set(
                redisKey,
                "failed:" + e.getMessage(),
                RESULT_TTL_HOURS,
                TimeUnit.HOURS
            );
        }
    }

    /**
     * Call careeros-ai POST /analyze endpoint.
     * Returns parsed AnalysisResult from the AI service response.
     */
    private AnalysisResult callAiService(AnalysisJob job) {
        RestTemplate restTemplate = new RestTemplate();

        // Build request body
        Map<String, String> requestBody = Map.of(
            "resume_text", job.getResumeText(),
            "jd_text", job.getJdText() != null ? job.getJdText() : ""
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        // POST to careeros-ai
        ResponseEntity<AnalysisResult> response = restTemplate.postForEntity(
            aiServiceUrl + "/analyze",
            request,
            AnalysisResult.class
        );

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("AI service returned: " + response.getStatusCode());
        }

        return response.getBody();
    }
}
