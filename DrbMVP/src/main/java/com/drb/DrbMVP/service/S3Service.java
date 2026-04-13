package com.drb.DrbMVP.service;

import lombok.extern.slf4j.Slf4j;
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

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class S3Service {

    private static final long MAX_SIZE = 10L * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED = List.of(
            "image/jpeg", "image/png", "image/webp", "image/heic"
    );

    private final S3Client s3Client;
    private final String bucket;
    private final String region;
    private final S3Presigner s3Presigner;

    public S3Service(S3Client s3Client,
                     S3Presigner s3Presigner,
                     @Value("${aws.s3.bucket-name}") String bucket,
                     @Value("${aws.s3.region}") String region) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.region = region;
    }

    public String upload(MultipartFile file, String folder) {
        validate(file);
        log.info("Uploading file: {}, size: {}, type: {}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        log.info("Using bucket={}, region={}", bucket, region);

        String key = folder + "/" + UUID.randomUUID() + "." + extension(file);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }

        return key;
    }

    public void delete(String s3Key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket).key(s3Key).build());
    }

    public String buildUrl(String s3Key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File must not be empty");
        if (file.getSize() > MAX_SIZE)
            throw new IllegalArgumentException("File exceeds maximum size of 10 MB");
        if (!ALLOWED.contains(file.getContentType()))
            throw new IllegalArgumentException(
                    "Unsupported type: " + file.getContentType() + ". Allowed: " + ALLOWED);
    }

    private String extension(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) return "jpg";
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }
}
