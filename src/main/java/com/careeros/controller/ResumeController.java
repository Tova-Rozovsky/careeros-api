package com.careeros.controller;

import com.careeros.dto.response.AnalysisResponse;
import com.careeros.service.AnalysisService;
import com.careeros.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

/**
 * Resume endpoints.
 * BE-003: upload, list, delete
 * BE-004: analyze, getAnalysis
 */
@RestController
@RequestMapping("/api/resumes")
@Tag(name = "Resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final AnalysisService analysisService;

    @PostMapping(
        value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
)
    @Operation(summary = "Upload resume (PDF/DOCX, max 10MB)")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String userId) {

        return ResponseEntity.ok(
                resumeService.uploadResume(
                        file,
                        userId
                )
        );
    }

    @GetMapping
    @Operation(summary = "List all resumes for current user")
    public ResponseEntity<?> list(
            @AuthenticationPrincipal String userId) {

        return ResponseEntity.ok(
                resumeService.getUserResumes(userId)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resume")
    public ResponseEntity<?> delete(
            @PathVariable String id,
            @AuthenticationPrincipal String userId) {

        resumeService.deleteResume(
                UUID.fromString(id),
                userId
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Trigger ATS analysis (async) — returns jobId for polling")
    public ResponseEntity<Map<String, String>> analyze(
            @PathVariable String id,
            @RequestParam(required = false) String jdText,
            @AuthenticationPrincipal String userId) {

        String resumeText = "Sample resume text — replace with DB lookup after BE-003";

        String jobId = analysisService.triggerAnalysis(
                id,
                userId,
                resumeText,
                jdText
        );

        return ResponseEntity.accepted().body(Map.of(
                "jobId", jobId,
                "status", "pending",
                "message", "Analysis started. Poll GET /api/resumes/" + id + "/analysis for results."
        ));
    }

    @GetMapping("/{id}/analysis")
    @Operation(summary = "Poll ATS analysis result")
    public ResponseEntity<AnalysisResponse> getAnalysis(
            @PathVariable String id) {

        AnalysisResponse response =
                analysisService.getAnalysisResult(id);

        if ("pending".equals(response.getStatus())) {
            return ResponseEntity.accepted().body(response);
        }

        return ResponseEntity.ok(response);
    }
}