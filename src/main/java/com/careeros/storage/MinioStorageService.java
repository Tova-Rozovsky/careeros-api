package com.careeros.storage;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Override
    public String uploadFile(
            MultipartFile file,
            String userId) throws Exception {

        String originalName = file.getOriginalFilename();

        String extension =
                originalName.substring(
                        originalName.lastIndexOf('.') + 1);

        String fileName =
                UUID.randomUUID() + "." + extension;

        String storagePath =
                "resumes/" + userId + "/" + fileName;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(storagePath)
                        .stream(
                                file.getInputStream(),
                                file.getSize(),
                                -1
                        )
                        .contentType(file.getContentType())
                        .build()
        );

        return storagePath;
    }

    @Override
    public void deleteFile(
            String storagePath) throws Exception {

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(storagePath)
                        .build()
        );
    }
}