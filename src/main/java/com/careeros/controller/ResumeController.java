package com.careeros.controller;

import com.careeros.dto.response.AnalysisResponse;
import com.careeros.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * Resume endpoints.
 * BE-003: upload, list, delete (TODO — assigned to Full Stack contributor)
 * BE-004: analyze, getAnalysis (implemented here)
 */
@RestController
@RequestMapping("/api/resumes")
@Tag(name = "Resumes")
@RequiredArgsConstructor
public class
ResumeController {

    private final AnalysisService analysisService;

    // ── BE-003 (TODO — assigned to Full Stack contributor) ────────────────────

    @PostMapping("/upload")
    @Operation(summary = "Upload resume (PDF/DOCX, max 10MB)")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        throw new UnsupportedOperationException("BE-003 — not yet implemented");
    }

    @GetMapping
    @Operation(summary = "List all resumes for current user")
    public ResponseEntity<?> list() {
        throw new UnsupportedOperationException("BE-003 — not yet implemented");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resume")
    public ResponseEntity<?> delete(@PathVariable String id) {
        throw new UnsupportedOperationException("BE-003 — not yet implemented");
    }

    // ── BE-004 (implemented) ──────────────────────────────────────────────────

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Trigger ATS analysis (async) — returns jobId for polling")
    public ResponseEntity<Map<String, String>> analyze(
            @PathVariable String id,
            @RequestParam(required = false) String jdText,
            @RequestHeader("X-User-Id") String userId) {

        // TODO: Once BE-003 is done, fetch resumeText from database
        // For now, accept resumeText as request param for testing
        // Real flow: resumeId → fetch Resume entity → use parsedText field

        // Temporary: use hardcoded sample text for testing
        String resumeText = "Sample resume text — replace with DB lookup after BE-003";

        String jobId = analysisService.triggerAnalysis(id, userId, resumeText, jdText);

        return ResponseEntity.accepted().body(Map.of(
            "jobId", jobId,
            "status", "pending",
            "message", "Analysis started. Poll GET /api/resumes/" + id + "/analysis for results."
        ));
    }

    @GetMapping("/{id}/analysis")
    @Operation(summary = "Poll ATS analysis result")
    public ResponseEntity<AnalysisResponse> getAnalysis(@PathVariable String id) {
        AnalysisResponse response = analysisService.getAnalysisResult(id);

        // Return 202 if still pending, 200 if completed or failed
        if ("pending".equals(response.getStatus())) {
            return ResponseEntity.accepted().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
