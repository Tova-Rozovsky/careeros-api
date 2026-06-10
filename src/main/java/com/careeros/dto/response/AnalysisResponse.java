package com.careeros.dto.response;

import com.careeros.messaging.AnalysisResult;
import lombok.*;
import java.util.List;

/**
 * API response for GET /api/resumes/{id}/analysis
 * Returned to careeros-web frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    private String status;          // pending | completed | failed
    private String jobId;
    private Integer overallScore;
    private CategoryScores categoryScores;
    private List<String> keywordGaps;
    private List<String> extractedSkills;
    private List<Improvement> improvements;
    private String summary;
    private String message;         // Error message if failed

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryScores {
        private int keywordMatch;
        private int sectionCompleteness;
        private int actionVerbs;
        private int quantification;
        private int formatting;
        private int contactInfo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Improvement {
        private String priority;
        private String section;
        private String message;
    }
}
