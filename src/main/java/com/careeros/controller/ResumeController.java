package com.careeros.controller;
// TODO: BE-003 + BE-004
// Issues: github.com/career-os/careeros-api/issues/3 and /4
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
@Tag(name = "Resumes")
public class ResumeController {
    @PostMapping("/upload")
    @Operation(summary = "Upload resume (PDF/DOCX, max 10MB)")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        throw new UnsupportedOperationException("BE-003 not yet implemented");
    }
    @GetMapping
    @Operation(summary = "List all resumes")
    public ResponseEntity<?> list() {
        throw new UnsupportedOperationException("BE-003 not yet implemented");
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resume")
    public ResponseEntity<?> delete(@PathVariable String id) {
        throw new UnsupportedOperationException("BE-003 not yet implemented");
    }
    @PostMapping("/{id}/analyze")
    @Operation(summary = "Trigger ATS analysis")
    public ResponseEntity<?> analyze(@PathVariable String id) {
        throw new UnsupportedOperationException("BE-004 not yet implemented");
    }
    @GetMapping("/{id}/analysis")
    @Operation(summary = "Poll analysis result")
    public ResponseEntity<?> getAnalysis(@PathVariable String id) {
        throw new UnsupportedOperationException("BE-004 not yet implemented");
    }
}