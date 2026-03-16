package com.reverse.core.service;

import com.reverse.core.exception.BadRequestException;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.s3.bucket}")
    private String bucket;

    @Value("${cloud.s3.endpoint}")
    private String endpoint;

    @Value("${cloud.s3.public-url:}")
    private String publicUrl;

    public UploadResult upload(MultipartFile file, String dir) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unnamed";
        }
        String ext = getExt(originalName);
        String key = dir + "/" + UUID.randomUUID() + ext;

        try {
            PutObjectRequest req =
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build();

            s3Client.putObject(req, RequestBody.fromBytes(file.getBytes()));

            String fileUrl = buildFileUrl(key);
            return new UploadResult(key, fileUrl, originalName);
        } catch (IOException e) {
            throw new IllegalStateException("파일 업로드 실패", e);
        }
    }

    public UploadResult uploadBytes(
            byte[] bytes, String originalName, String contentType, String dir) {
        String safeName =
                (originalName == null || originalName.isBlank()) ? "document.txt" : originalName;
        String ext = getExt(safeName);
        String key = dir + "/" + UUID.randomUUID() + ext;

        try {
            PutObjectRequest req =
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build();

            s3Client.putObject(req, RequestBody.fromBytes(bytes));
            String fileUrl = buildFileUrl(key);
            return new UploadResult(key, fileUrl, safeName);
        } catch (Exception e) {
            throw new IllegalStateException("파일 업로드 실패", e);
        }
    }

    public byte[] download(String key) {
        String normalizedKey = normalizeKey(key);
        try {
            GetObjectRequest request =
                    GetObjectRequest.builder().bucket(bucket).key(normalizedKey).build();
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(request);
            return objectBytes.asByteArray();
        } catch (RuntimeException e) {
            throw new IllegalStateException("파일 다운로드 실패", e);
        }
    }

    public byte[] downloadByKey(String fileKey) {
        return download(fileKey);
    }

    public void delete(String key) {
        try {
            DeleteObjectRequest req = DeleteObjectRequest.builder().bucket(bucket).key(key).build();
            s3Client.deleteObject(req);
        } catch (Exception e) {
            throw new IllegalStateException("파일 삭제 실패", e);
        }
    }

    public void deleteByKey(String fileKey) {
        delete(normalizeKey(fileKey));
    }

    public String generatePresignedUrl(String key, long expireSeconds) {
        long normalizedExpireSeconds = Math.max(1L, expireSeconds);
        try {
            GetObjectRequest getObjectRequest =
                    GetObjectRequest.builder().bucket(bucket).key(key).build();
            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofSeconds(normalizedExpireSeconds))
                            .getObjectRequest(getObjectRequest)
                            .build();
            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            throw new IllegalStateException("파일 다운로드 URL 생성 실패", e);
        }
    }

    private String buildFileUrl(String key) {
        if (publicUrl != null && !publicUrl.isBlank()) {
            return trimTrailingSlash(publicUrl) + "/" + key;
        }
        String normalizedEndpoint = trimTrailingSlash(endpoint);
        String normalizedBucket = trimTrailingSlash(bucket);
        if (!normalizedBucket.isBlank() && normalizedEndpoint.endsWith("/" + normalizedBucket)) {
            return normalizedEndpoint + "/" + key;
        }
        return normalizedEndpoint + "/" + normalizedBucket + "/" + key;
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeKey(String key) {
        if (!hasText(key)) {
            throw new BadRequestException("첨부 파일 키가 없습니다.");
        }
        return key.trim();
    }

    private String getExt(String name) {
        if (name == null || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf("."));
    }

    public record UploadResult(String key, String fileUrl, String originalName) {}
}
