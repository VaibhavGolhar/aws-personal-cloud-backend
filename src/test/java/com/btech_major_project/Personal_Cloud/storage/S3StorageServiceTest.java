package com.btech_major_project.Personal_Cloud.storage;

import com.btech_major_project.Personal_Cloud.dto.FileDownloadResult;
import com.btech_major_project.Personal_Cloud.usage.UsageService;
import com.btech_major_project.Personal_Cloud.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private FileMetadataRepository fileRepo;
    @Mock
    private UsageService usageService;

    private S3StorageService s3StorageService;

    private User user;

    @BeforeEach
    void setUp() {
        s3StorageService = new S3StorageService(s3Client, fileRepo, usageService, "test-bucket");
        user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setS3Prefix("u-test/");
    }

    @Test
    void upload_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        PutObjectResponse putRes = (PutObjectResponse) PutObjectResponse.builder().eTag("etag123").versionId("v1").build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(putRes);

        FileMetadata savedMeta = new FileMetadata();
        savedMeta.setSizeBytes(7L);
        when(fileRepo.save(any(FileMetadata.class))).thenReturn(savedMeta);

        FileMetadata result = s3StorageService.upload(user, file, "docs");

        ArgumentCaptor<PutObjectRequest> reqCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(reqCaptor.capture(), any(RequestBody.class));
        PutObjectRequest req = reqCaptor.getValue();

        assertEquals("test-bucket", req.bucket());
        assertEquals("u-test/docs/test.txt", req.key());

        ArgumentCaptor<FileMetadata> metaCaptor = ArgumentCaptor.forClass(FileMetadata.class);
        verify(fileRepo).save(metaCaptor.capture());
        FileMetadata meta = metaCaptor.getValue();
        assertEquals("test.txt", meta.getFilename());
        assertEquals("u-test/docs/test.txt", meta.getS3Key());
        assertEquals("etag123", meta.getEtag());

        verify(usageService).onPut(user, 7L, true);
    }

    @Test
    void upload_NullSubPathAndNullFilename() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getBytes()).thenReturn("content".getBytes());

        PutObjectResponse putRes = (PutObjectResponse) PutObjectResponse.builder().eTag("etag123").versionId("v1").build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(putRes);
        FileMetadata savedMeta = new FileMetadata();
        when(fileRepo.save(any(FileMetadata.class))).thenReturn(savedMeta);

        s3StorageService.upload(user, file, null);

        ArgumentCaptor<PutObjectRequest> reqCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(reqCaptor.capture(), any(RequestBody.class));
        assertEquals("u-test/unnamed", reqCaptor.getValue().key());
    }

    @Test
    void upload_EmptySubPath() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        PutObjectResponse putRes = (PutObjectResponse) PutObjectResponse.builder().eTag("etag123").versionId("v1").build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(putRes);
        FileMetadata savedMeta = new FileMetadata();
        when(fileRepo.save(any(FileMetadata.class))).thenReturn(savedMeta);

        s3StorageService.upload(user, file, "   "); // blank
        
        s3StorageService.upload(user, file, "docs/"); // ends with slash

        ArgumentCaptor<PutObjectRequest> reqCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(2)).putObject(reqCaptor.capture(), any(RequestBody.class));
    }

    @Test
    void upload_EmptyFile() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> s3StorageService.upload(user, file, "docs"));
        assertEquals("File is empty", ex.getMessage());
        
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void upload_S3Exception_WithDetails() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        software.amazon.awssdk.awscore.exception.AwsErrorDetails details = software.amazon.awssdk.awscore.exception.AwsErrorDetails.builder().errorMessage("err").build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(S3Exception.builder().message("S3 error").awsErrorDetails(details).build());

        assertThrows(S3Exception.class, () -> s3StorageService.upload(user, file, "docs"));
    }

    @Test
    void upload_S3Exception_NoDetails() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(S3Exception.builder().message("S3 error").build());

        assertThrows(S3Exception.class, () -> s3StorageService.upload(user, file, "docs"));
    }

    @Test
    void list_Success() {
        when(fileRepo.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(new FileMetadata()));

        List<FileMetadata> result = s3StorageService.list(user);

        assertEquals(1, result.size());
        verify(usageService).onList(user);
    }

    @Test
    void download_Success_NoContentType() {
        FileMetadata meta = new FileMetadata();
        meta.setS3Key("u-test/test.txt");
        meta.setFilename("test.txt");
        meta.setContentType(null); // null content type
        when(fileRepo.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(meta));

        GetObjectResponse getRes = (GetObjectResponse) GetObjectResponse.builder().build();
        ResponseInputStream<GetObjectResponse> stream = new ResponseInputStream<>(getRes, AbortableInputStream.create(new ByteArrayInputStream("data".getBytes())));
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(stream);

        FileDownloadResult result = s3StorageService.download(user, 10L);

        assertNotNull(result);
        assertEquals("test.txt", result.getFilename());
        verify(usageService).onGet(user);
    }

    @Test
    void download_S3Exception() {
        FileMetadata meta = new FileMetadata();
        meta.setS3Key("u-test/test.txt");
        when(fileRepo.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(meta));

        software.amazon.awssdk.awscore.exception.AwsErrorDetails details = software.amazon.awssdk.awscore.exception.AwsErrorDetails.builder().errorMessage("err").build();
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(S3Exception.builder().message("err").awsErrorDetails(details).build());

        assertThrows(S3Exception.class, () -> s3StorageService.download(user, 10L));
    }
    
    @Test
    void download_S3Exception_NoDetails() {
        FileMetadata meta = new FileMetadata();
        meta.setS3Key("u-test/test.txt");
        when(fileRepo.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(meta));

        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(S3Exception.builder().message("err").build());

        assertThrows(S3Exception.class, () -> s3StorageService.download(user, 10L));
    }

    @Test
    void download_NotFound() {
        when(fileRepo.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> s3StorageService.download(user, 10L));
    }

    @Test
    void delete_Success() {
        FileMetadata meta = new FileMetadata();
        meta.setS3Key("u-test/test.txt");
        meta.setSizeBytes(1024L);
        when(fileRepo.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(meta));

        s3StorageService.delete(user, 10L);

        ArgumentCaptor<DeleteObjectRequest> reqCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(reqCaptor.capture());
        assertEquals("u-test/test.txt", reqCaptor.getValue().key());
        
        verify(fileRepo).delete(meta);
        verify(usageService).onDelete(user, 1024L);
    }
    
    @Test
    void delete_S3Exception() {
        FileMetadata meta = new FileMetadata();
        meta.setS3Key("u-test/test.txt");
        when(fileRepo.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(meta));

        software.amazon.awssdk.awscore.exception.AwsErrorDetails details = software.amazon.awssdk.awscore.exception.AwsErrorDetails.builder().errorMessage("err").build();
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(S3Exception.builder().message("err").awsErrorDetails(details).build());

        assertThrows(S3Exception.class, () -> s3StorageService.delete(user, 10L));
    }
    
    @Test
    void delete_S3Exception_NoDetails() {
        FileMetadata meta = new FileMetadata();
        meta.setS3Key("u-test/test.txt");
        when(fileRepo.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(meta));

        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(S3Exception.builder().message("err").build());

        assertThrows(S3Exception.class, () -> s3StorageService.delete(user, 10L));
    }

    @Test
    void delete_NotFound() {
        when(fileRepo.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> s3StorageService.delete(user, 10L));
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void uploadBulk_Success() throws IOException {
        MockMultipartFile file1 = new MockMultipartFile("file", "test1.txt", "text/plain", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "test2.txt", "text/plain", "content2".getBytes());

        PutObjectResponse putRes = (PutObjectResponse) PutObjectResponse.builder().eTag("etag123").versionId("v1").build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(putRes);

        FileMetadata savedMeta = new FileMetadata();
        savedMeta.setSizeBytes(7L);
        when(fileRepo.save(any(FileMetadata.class))).thenReturn(savedMeta);

        List<FileMetadata> result = s3StorageService.uploadBulk(user, List.of(file1, file2), "docs");

        assertEquals(2, result.size());
        verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(fileRepo, times(2)).save(any(FileMetadata.class));
        verify(usageService, times(2)).onPut(user, 7L, true);
    }

    @Test
    void downloadBulk_Success() throws IOException {
        FileMetadata meta = new FileMetadata();
        meta.setS3Key("u-test/test.txt");
        meta.setFilename("test.txt");
        meta.setContentType(null);
        when(fileRepo.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(meta));

        GetObjectResponse getRes = (GetObjectResponse) GetObjectResponse.builder().build();
        ResponseInputStream<GetObjectResponse> stream = new ResponseInputStream<>(getRes, AbortableInputStream.create(new ByteArrayInputStream("data".getBytes())));
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(stream);

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        s3StorageService.downloadBulk(user, List.of(10L), bos);

        assertTrue(bos.size() > 0);
        verify(usageService).onGet(user);
    }
}
