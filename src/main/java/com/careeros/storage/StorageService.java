package com.careeros.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String uploadFile(
            MultipartFile file,
            String userId) throws Exception;

    void deleteFile(
            String storagePath) throws Exception;
}