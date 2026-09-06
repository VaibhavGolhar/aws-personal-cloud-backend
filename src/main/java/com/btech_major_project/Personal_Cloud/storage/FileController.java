package com.btech_major_project.Personal_Cloud.storage;

import com.btech_major_project.Personal_Cloud.user.User;
import com.btech_major_project.Personal_Cloud.user.UserService;
import com.btech_major_project.Personal_Cloud.common.AppLogger;

import com.btech_major_project.Personal_Cloud.dto.FileDownloadResult;
import com.btech_major_project.Personal_Cloud.dto.FileInfoResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final AppLogger log = AppLogger.getLogger(FileController.class);

    private final StorageService storageService;
    private final UserService userService;

    public FileController(StorageService storageService, UserService userService) {
        this.storageService = storageService;
        this.userService = userService;
    }

    private User currentUser(UserDetails principal) {
        return userService.findByUsername(principal.getUsername());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FileInfoResponse> upload(@AuthenticationPrincipal UserDetails principal,
                                                   @RequestParam("file") MultipartFile file,
                                                   @RequestParam(value = "path", required = false) String path) throws IOException {
        log.info("POST /api/files upload requested by=" + (principal != null ? principal.getUsername() : "anonymous") +
                ", filename=" + file.getOriginalFilename() + ", path=" + (path == null ? "" : path));
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        User user = currentUser(principal);
        FileMetadata meta = storageService.upload(user, file, path);
        log.info("Upload success userId=" + user.getId() + ", fileId=" + meta.getId() + ", key=" + meta.getS3Key());
        return ResponseEntity.ok(new FileInfoResponse(
                meta.getId(), meta.getFilename(), meta.getContentType(), meta.getSizeBytes(), meta.getCreatedAt()
        ));
    }

    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FileInfoResponse>> uploadBulk(@AuthenticationPrincipal UserDetails principal,
                                                             @RequestParam("files") List<MultipartFile> files,
                                                             @RequestParam(value = "path", required = false) String path) throws IOException {
        log.info("POST /api/files/bulk upload requested by=" + (principal != null ? principal.getUsername() : "anonymous") +
                 ", count=" + files.size() + ", path=" + (path == null ? "" : path));
        if (files.isEmpty()) {
            throw new IllegalArgumentException("Files list is empty");
        }
        User user = currentUser(principal);
        List<FileMetadata> metas = storageService.uploadBulk(user, files, path);
        List<FileInfoResponse> responses = metas.stream()
                .map(meta -> new FileInfoResponse(meta.getId(), meta.getFilename(), meta.getContentType(), meta.getSizeBytes(), meta.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping(value = "/bulk-download", produces = "application/zip")
    public ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody> downloadBulk(@AuthenticationPrincipal UserDetails principal,
                                                                 @RequestBody List<Long> fileIds) {
        log.info("POST /api/files/bulk-download requested by=" + (principal != null ? principal.getUsername() : "anonymous") + ", count=" + fileIds.size());
        User user = currentUser(principal);

        org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody responseBody = outputStream -> {
            storageService.downloadBulk(user, fileIds, outputStream);
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("bulk-download.zip").build());

        return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<FileInfoResponse>> list(@AuthenticationPrincipal UserDetails principal,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "50") int size) {
        log.info("GET /api/files list requested by=" + (principal != null ? principal.getUsername() : "anonymous") + ", page=" + page);
        User user = currentUser(principal);
        Pageable pageable = PageRequest.of(page, size);
        Page<FileInfoResponse> list = storageService.list(user, pageable)
                .map(m -> new FileInfoResponse(m.getId(), m.getFilename(), m.getContentType(), m.getSizeBytes(), m.getCreatedAt()));
        log.info("List success userId=" + user.getId() + ", elements=" + list.getNumberOfElements());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@AuthenticationPrincipal UserDetails principal,
                                                        @PathVariable Long id) {
        log.info("GET /api/files/" + id + "/download requested by=" + (principal != null ? principal.getUsername() : "anonymous"));
        User user = currentUser(principal);
        FileDownloadResult res = storageService.download(user, id);

        String encoded = URLEncoder.encode(res.getFilename(), StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(encoded).build());
        headers.setContentType(res.getContentType() != null ?
                MediaType.parseMediaType(res.getContentType()) :
                MediaType.APPLICATION_OCTET_STREAM);

        log.info("Download prepared userId=" + user.getId() + ", fileId=" + id + ", status=200");
        return new ResponseEntity<>(res.getResource(), headers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails principal,
                                       @PathVariable Long id) {
        log.info("DELETE /api/files/" + id + " requested by=" + (principal != null ? principal.getUsername() : "anonymous"));
        User user = currentUser(principal);
        storageService.delete(user, id);
        log.info("Delete success userId=" + user.getId() + ", fileId=" + id);
        return ResponseEntity.noContent().build();
    }
}
