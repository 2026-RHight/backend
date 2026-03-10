package com.reverse.hr.internal.application;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3FileService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.s3.bucket}")
    private String bucket;

    @Value("${cloud.s3.endpoint}")
    private String endpoint;

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

            String fileUrl = endpoint + "/" + bucket + "/" + key; // MinIO path-style
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
            String fileUrl = endpoint + "/" + bucket + "/" + key;
            return new UploadResult(key, fileUrl, safeName);
        } catch (Exception e) {
            throw new IllegalStateException("파일 업로드 실패", e);
        }
    }

    private String getExt(String name) {
        if (name == null || !name.contains(".")) return "";
        return name.substring(name.lastIndexOf("."));
    }

    public void delete(String key) {
        try {
            DeleteObjectRequest req = DeleteObjectRequest.builder().bucket(bucket).key(key).build();
            s3Client.deleteObject(req);
        } catch (Exception e) {
            throw new IllegalStateException("파일 삭제 실패", e);
        }
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

    public record UploadResult(String key, String fileUrl, String originalName) {}
}
