package com.reverse.approval.internal.application;

import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class ApprovalFileService {

    @Value("${cloud.s3.bucket}")
    private String bucket;

    @Value("${cloud.s3.endpoint}")
    private String endpoint;

    private final S3Client s3Client;

    public UploadResult upload(MultipartFile file, String dir) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unnamed";
        }

        String ext = getExtension(originalName);
        String key = dir + "/" + UUID.randomUUID() + ext;

        try {
            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            String fileUrl = endpoint + "/" + bucket + "/" + key;

            return new UploadResult(key, fileUrl, originalName);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to upload file", e);
        }
    }

    public byte[] download(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key).build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(request);
            return objectBytes.asByteArray();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Failed to download file", e);
        }
    }

    public byte[] downloadByKey(String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            throw new IllegalStateException("Missing file_key");
        }
        return download(fileKey.trim());
    }

    public void delete(String key) {
        try {
            DeleteObjectRequest request =
                    DeleteObjectRequest.builder().bucket(bucket).key(key).build();
            s3Client.deleteObject(request);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Failed to delete file", e);
        }
    }

    public void deleteByKey(String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            throw new IllegalStateException("Missing file_key");
        }
        delete(fileKey.trim());
    }

    private String getExtension(String name) {
        if (!name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf("."));
    }

    public record UploadResult(String key, String fileUrl, String originalName) {}
}
