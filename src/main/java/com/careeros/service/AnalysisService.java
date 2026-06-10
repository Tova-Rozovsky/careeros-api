package com.careeros.service;

import com.careeros.dto.response.AnalysisResponse;
import com.careeros.messaging.AnalysisProducer;
import com.careeros.messaging.AnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * BE-004 — Analysis Service
 *
 * Orchestrates the async analysis flow:
 * - triggerAnalysis() → enqueues job, returns jobId
 * - getAnalysisResult() → polls Redis, returns current status
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisProducer analysisProducer;
    private final ObjectMapper objectMapper;

    /**
     * Trigger async ATS analysis for a resume.
     * Called by POST /api/resumes/{id}/analyze
     *
     * @return jobId for frontend polling
     */
    public String triggerAnalysis(String resumeId, String userId,
                                   String resumeText, String jdText) {
        // Validate resume text is not empty
        if (resumeText == null || resumeText.trim().isEmpty()) {
            throw new IllegalArgumentException("Resume text is empty — cannot analyze");
        }

        return analysisProducer.enqueueAnalysis(resumeId, userId, resumeText, jdText);
    }

    /**
     * Get analysis result for a resume.
     * Called by GET /api/resumes/{id}/analysis (frontend polls this)
     *
     * Status flow: pending → completed | failed
     */
    public AnalysisResponse getAnalysisResult(String resumeId) {
        String redisValue = analysisProducer.getAnalysisStatus(resumeId);

        // No job found in Redis
        if (redisValue == null) {
            return AnalysisResponse.builder()
                    .status("not_found")
                    .message("No analysis job found for this resume. Trigger analysis first.")
                    .build();
        }

        // Parse status prefix: "pending:{jobId}" | "completed:{json}" | "failed:{reason}"
        String[] parts = redisValue.split(":", 2);
        String status = parts[0];
        String data = parts.length > 1 ? parts[1] : "";

        return switch (status) {
            case "pending" -> AnalysisResponse.builder()
                    .status("pending")
                    .jobId(data)
                    .message("Analysis in progress. Please poll again in a few seconds.")
                    .build();

            case "completed" -> parseCompletedResult(data);

            case "failed" -> AnalysisResponse.builder()
                    .status("failed")
                    .message("Analysis failed: " + data)
                    .build();

            default -> AnalysisResponse.builder()
                    .status("unknown")
                    .message("Unknown status: " + status)
                    .build();
        };
    }

    /**
     * Parse the completed JSON result from Redis into AnalysisResponse.
     */
    private AnalysisResponse parseCompletedResult(String json) {
        try {
            AnalysisResult result = objectMapper.readValue(json, AnalysisResult.class);

            // Map improvements
            var improvements = result.getImprovements() == null ? null :
                result.getImprovements().stream()
                    .map(imp -> AnalysisResponse.Improvement.builder()
                            .priority(imp.getPriority())
                            .section(imp.getSection())
                            .message(imp.getMessage())
                            .build())
                    .toList();

            // Map category scores
            AnalysisResponse.CategoryScores scores = null;
            if (result.getCategoryScores() != null) {
                var cs = result.getCategoryScores();
                scores = AnalysisResponse.CategoryScores.builder()
                        .keywordMatch(cs.getKeywordMatch())
                        .sectionCompleteness(cs.getSectionCompleteness())
                        .actionVerbs(cs.getActionVerbs())
                        .quantification(cs.getQuantification())
                        .formatting(cs.getFormatting())
                        .contactInfo(cs.getContactInfo())
                        .build();
            }

            return AnalysisResponse.builder()
                    .status("completed")
                    .overallScore(result.getOverallScore())
                    .categoryScores(scores)
                    .keywordGaps(result.getKeywordGaps())
                    .extractedSkills(result.getExtractedSkills())
                    .improvements(improvements)
                    .summary(result.getSummary())
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse analysis result: {}", e.getMessage());
            return AnalysisResponse.builder()
                    .status("failed")
                    .message("Failed to parse analysis result")
                    .build();
        }
    }
}
