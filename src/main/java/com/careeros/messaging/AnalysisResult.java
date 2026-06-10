package com.careeros.messaging;

import lombok.*;
import java.util.List;

/**
 * Response from careeros-ai /analyze endpoint.
 * Mapped from the JSON response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private int overallScore;
    private CategoryScores categoryScores;
    private List<String> keywordGaps;
    private List<String> extractedSkills;
    private List<Improvement> improvements;
    private String summary;
    private String status;

    @Data
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Improvement {
        private String priority;
        private String section;
        private String message;
    }
}
