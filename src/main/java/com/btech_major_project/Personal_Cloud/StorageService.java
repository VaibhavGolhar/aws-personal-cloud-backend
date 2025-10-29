package com.btech_major_project.Personal_Cloud;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class StorageService {

    private static final AppLogger log = AppLogger.getLogger(StorageService.class);

    private final S3Client s3;
    private final String bucket;
    private final FileMetadataRepository fileRepo;
    private final UsageService usageService;

    public StorageService(@Qualifier("s3Client") S3Client s3,
                          @Value("${app.s3.bucket}") String bucket,
                          FileMetadataRepository fileRepo,
                          UsageService usageService) {
        this.s3 = s3;
        this.bucket = bucket;
        this.fileRepo = fileRepo;
        this.usageService = usageService;
    }

    public FileMetadata upload(User user, MultipartFile file, String subPath) throws IOException {
        String safeName = file.getOriginalFilename() == null ? "unnamed" : file.getOriginalFilename();
        String normalizedPath = (subPath == null || subPath.isBlank()) ? "" : subPath.trim();
        if (!normalizedPath.endsWith("/") && !normalizedPath.isEmpty()) normalizedPath += "/";
        String key = user.getS3Prefix() + normalizedPath + safeName;

        log.info("S3 PUT start bucket=" + bucket + ", key=" + key + ", size=" + file.getSize());
        try {
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            PutObjectResponse putRes = s3.putObject(putReq, RequestBody.fromBytes(file.getBytes()));

            FileMetadata meta = new FileMetadata();
            meta.setUser(user);
            meta.setS3Key(key);
            meta.setFilename(safeName);
            meta.setContentType(file.getContentType());
            meta.setSizeBytes(file.getSize());
            meta.setEtag(putRes.eTag());
            meta.setVersionId(putRes.versionId());

            FileMetadata saved = fileRepo.save(meta);
            usageService.onPut(user, saved.getSizeBytes(), true);
            log.info("S3 PUT ok bucket=" + bucket + ", key=" + key + ", fileId=" + saved.getId());
            return saved;
        } catch (S3Exception e) {
            String details = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : null;
            log.error("S3 PUT failed bucket=" + bucket + ", key=" + key + ", msg=" + e.getMessage() + (details != null ? (" details=" + details) : ""), e);
            throw e;
        }
    }

    public List<FileMetadata> list(User user) {
        log.info("List files userId=" + user.getId());
        usageService.onList(user);
        List<FileMetadata> out = fileRepo.findByUserIdOrderByCreatedAtDesc(user.getId());
        log.info("List files ok userId=" + user.getId() + ", count=" + out.size());
        return out;
    }

    public ResponseEntity<InputStreamResource> download(User user, Long fileId) {
        FileMetadata meta = fileRepo.findByIdAndUserId(fileId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(meta.getS3Key())
                .build();

        log.info("S3 GET start bucket=" + bucket + ", key=" + meta.getS3Key() + ", fileId=" + fileId);
        try {
            ResponseInputStream<GetObjectResponse> stream = s3.getObject(getReq);
            usageService.onGet(user);

            String encoded = URLEncoder.encode(meta.getFilename(), StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename(encoded).build());
            headers.setContentType(meta.getContentType() != null ?
                    MediaType.parseMediaType(meta.getContentType()) :
                    MediaType.APPLICATION_OCTET_STREAM);

            InputStreamResource body = new InputStreamResource(stream);
            log.info("S3 GET ok bucket=" + bucket + ", key=" + meta.getS3Key() + ", fileId=" + fileId);
            return new ResponseEntity<>(body, headers, HttpStatus.OK);
        } catch (S3Exception e) {
            String details = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : null;
            log.error("S3 GET failed bucket=" + bucket + ", key=" + meta.getS3Key() + ", msg=" + e.getMessage() + (details != null ? (" details=" + details) : ""), e);
            throw e;
        }
    }

    public void delete(User user, Long fileId) {
        FileMetadata meta = fileRepo.findByIdAndUserId(fileId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        log.info("S3 DELETE start bucket=" + bucket + ", key=" + meta.getS3Key() + ", fileId=" + fileId);
        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(meta.getS3Key())
                    .build());
            fileRepo.delete(meta);
            usageService.onDelete(user, meta.getSizeBytes());
            log.info("S3 DELETE ok bucket=" + bucket + ", key=" + meta.getS3Key() + ", fileId=" + fileId);
        } catch (S3Exception e) {
            String details = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : null;
            log.error("S3 DELETE failed bucket=" + bucket + ", key=" + meta.getS3Key() + ", msg=" + e.getMessage() + (details != null ? (" details=" + details) : ""), e);
            throw e;
        }
    }
}