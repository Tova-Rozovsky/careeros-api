package com.careeros.service;

import com.careeros.dto.response.ResumeResponse;
import com.careeros.dto.response.ResumeUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ResumeService {

    ResumeUploadResponse uploadResume(
            MultipartFile file,
            String userId);

    List<ResumeResponse> getUserResumes(
            String userId);

    void deleteResume(
            UUID resumeId,
            String userId);
}