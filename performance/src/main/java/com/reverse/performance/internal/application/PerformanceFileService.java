package com.reverse.performance.internal.application;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class PerformanceFileService {

    private final S3Client s3Client;

    @Value("${cloud.s3.bucket}")
    private String bucket;

    @Value("${cloud.s3.endpoint}")
    private String endpoint;

    public UploadResult upload(MultipartFile file, String dir) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unnamed";
        }

        String key = dir + "/" + UUID.randomUUID() + getExt(originalName);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return new UploadResult(key, endpoint + "/" + bucket + "/" + key, originalName);
        } catch (IOException e) {
            throw new IllegalStateException("파일 업로드 실패", e);
        }
    }

    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (Exception e) {
            throw new IllegalStateException("파일 삭제 실패", e);
        }
    }

    public void deleteByFileUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        delete(extractKeyFromFileUrl(fileUrl));
    }

    private String extractKeyFromFileUrl(String fileUrl) {
        try {
            String path = URI.create(fileUrl).getPath();
            if (path == null || path.isBlank()) {
                throw new IllegalArgumentException("유효하지 않은 파일 URL입니다.");
            }
            String normalized = path.startsWith("/") ? path.substring(1) : path;
            String bucketPrefix = bucket + "/";
            if (!normalized.startsWith(bucketPrefix)) {
                throw new IllegalArgumentException("버킷 경로가 일치하지 않습니다.");
            }
            return normalized.substring(bucketPrefix.length());
        } catch (RuntimeException e) {
            throw new IllegalStateException("파일 URL에서 key 파싱 실패", e);
        }
    }

    private String getExt(String name) {
        if (name == null || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf("."));
    }

    public record UploadResult(String key, String fileUrl, String originalName) {}
}
