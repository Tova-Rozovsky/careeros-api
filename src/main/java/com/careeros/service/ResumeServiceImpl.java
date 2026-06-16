package com.careeros.service;

import com.careeros.dto.response.ResumeResponse;
import com.careeros.dto.response.ResumeUploadResponse;
import com.careeros.repository.ResumeRepository;
import com.careeros.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.careeros.entity.User;
import com.careeros.entity.Resume;
import java.util.List;
import java.util.UUID;
import com.careeros.storage.StorageService;
import org.apache.tika.Tika;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Override
	public ResumeUploadResponse uploadResume(
        MultipartFile file,
        String userId) {

		validateFile(file);

		UUID userUuid = UUID.fromString(userId);

		User user = userRepository.findById(userUuid)
				.orElseThrow(() ->
						new RuntimeException("User not found"));

		try {

			String storagePath =
					storageService.uploadFile(file, userId);
			
			String parsedText = extractText(file);

			Resume resume = Resume.builder()
					.user(user)
					.fileName(file.getOriginalFilename())
					.storagePath(storagePath)
					.parsedText(null)
					.version(1)
					.isActive(true)
					.build();

			Resume savedResume =
					resumeRepository.save(resume);

			return new ResumeUploadResponse(
					savedResume.getId(),
					savedResume.getFileName(),
					savedResume.getStoragePath(),
					savedResume.getCreatedAt()
			);

		} catch (Exception e) {
			throw new RuntimeException(
					"Failed to upload resume", e);
		}
	}
    @Override
    public List<ResumeResponse> getUserResumes(String userId) {

        UUID userUuid = UUID.fromString(userId);

        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return resumeRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(resume -> new ResumeResponse(
                        resume.getId(),
                        resume.getFileName(),
                        resume.getVersion(),
                        resume.getIsActive(),
                        resume.getCreatedAt()
                ))
                .toList();
    }

    @Override
	public void deleteResume(UUID resumeId, String userId) {

		UUID userUuid = UUID.fromString(userId);

		User user = userRepository.findById(userUuid)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Resume resume = resumeRepository
				.findByIdAndUser(resumeId, user)
				.orElseThrow(() -> new RuntimeException("Resume not found"));

		try {
			storageService.deleteFile(
					resume.getStoragePath()
			);

			resumeRepository.delete(resume);

		} catch (Exception e) {
			throw new RuntimeException(
					"Failed to delete resume",
					e
			);
		}
	}

	private void validateFile(MultipartFile file) {

		if (file == null || file.isEmpty()) {
			throw new RuntimeException("File is required");
		}

		if (file.getSize() > 10 * 1024 * 1024) {
			throw new RuntimeException("File size exceeds 10MB");
		}

		String fileName = file.getOriginalFilename();

		if (fileName == null) {
			throw new RuntimeException("Invalid file name");
		}

		String lower = fileName.toLowerCase();

		if (!lower.endsWith(".pdf") && !lower.endsWith(".docx")) {
			throw new RuntimeException(
					"Only PDF and DOCX files are allowed");
		}
	}
	private String extractText(MultipartFile file) {

		try {
			Tika tika = new Tika();
			return tika.parseToString(file.getInputStream());

		} catch (Exception e) {
			throw new RuntimeException(
					"Failed to extract text from resume",
					e
			);
		}
	}
}