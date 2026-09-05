package com.btech_major_project.Personal_Cloud.storage;

import com.btech_major_project.Personal_Cloud.dto.FileDownloadResult;
import com.btech_major_project.Personal_Cloud.dto.FileInfoResponse;
import com.btech_major_project.Personal_Cloud.user.User;
import com.btech_major_project.Personal_Cloud.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private StorageService storageService;
    @Mock
    private UserService userService;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private FileController fileController;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setUsername("testuser");
    }

    @Test
    void upload() throws IOException {
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);

        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        FileMetadata meta = new FileMetadata();
        ReflectionTestUtils.setField(meta, "id", 10L);
        meta.setFilename("test.txt");
        meta.setContentType("text/plain");
        meta.setSizeBytes(7L);
        ReflectionTestUtils.setField(meta, "createdAt", Instant.now());
        when(storageService.upload(user, file, "docs")).thenReturn(meta);

        ResponseEntity<FileInfoResponse> response = fileController.upload(userDetails, file, "docs");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getId());
        assertEquals("test.txt", response.getBody().getFilename());
    }

    @Test
    void list() {
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);

        FileMetadata meta = new FileMetadata();
        ReflectionTestUtils.setField(meta, "id", 10L);
        meta.setFilename("test.txt");
        ReflectionTestUtils.setField(meta, "createdAt", Instant.now());

        when(storageService.list(user)).thenReturn(List.of(meta));

        ResponseEntity<List<FileInfoResponse>> response = fileController.list(userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(10L, response.getBody().get(0).getId());
    }

    @Test
    void download() {
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream("data".getBytes()));
        FileDownloadResult res = new FileDownloadResult(resource, "test.txt", "text/plain");

        when(storageService.download(user, 10L)).thenReturn(res);

        ResponseEntity<InputStreamResource> response = fileController.download(userDetails, 10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getHeaders().containsKey("Content-Disposition"));
        assertEquals("attachment; filename=\"test.txt\"", response.getHeaders().getFirst("Content-Disposition"));
    }

    @Test
    void delete() {
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);

        ResponseEntity<Void> response = fileController.delete(userDetails, 10L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(storageService).delete(user, 10L);
    }
    
    @Test
    void upload_NullPrincipalAndNullPath() {
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        assertThrows(NullPointerException.class, () -> fileController.upload(null, file, null));
    }

    @Test
    void list_NullPrincipal() {
        assertThrows(NullPointerException.class, () -> fileController.list(null));
    }

    @Test
    void download_NullPrincipal() {
        assertThrows(NullPointerException.class, () -> fileController.download(null, 10L));
    }

    @Test
    void delete_NullPrincipal() {
        assertThrows(NullPointerException.class, () -> fileController.delete(null, 10L));
    }
}
