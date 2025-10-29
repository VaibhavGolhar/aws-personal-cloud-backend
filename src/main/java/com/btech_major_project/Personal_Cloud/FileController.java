package com.btech_major_project.Personal_Cloud;

import com.btech_major_project.Personal_Cloud.dto.FileInfoResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final AppLogger log = AppLogger.getLogger(FileController.class);

    private final StorageService storageService;
    private final UserRepository userRepository;

    public FileController(StorageService storageService, UserRepository userRepository) {
        this.storageService = storageService;
        this.userRepository = userRepository;
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FileInfoResponse> upload(@AuthenticationPrincipal UserDetails principal,
                                                   @RequestParam("file") MultipartFile file,
                                                   @RequestParam(value = "path", required = false) String path) throws IOException {
        log.info("POST /api/files upload requested by=" + (principal != null ? principal.getUsername() : "anonymous") +
                ", filename=" + file.getOriginalFilename() + ", path=" + (path == null ? "" : path));
        User user = currentUser(principal);
        FileMetadata meta = storageService.upload(user, file, path);
        log.info("Upload success userId=" + user.getId() + ", fileId=" + meta.getId() + ", key=" + meta.getS3Key());
        return ResponseEntity.ok(new FileInfoResponse(
                meta.getId(), meta.getFilename(), meta.getContentType(), meta.getSizeBytes(), meta.getCreatedAt()
        ));
    }

    @GetMapping
    public ResponseEntity<List<FileInfoResponse>> list(@AuthenticationPrincipal UserDetails principal) {
        log.info("GET /api/files list requested by=" + (principal != null ? principal.getUsername() : "anonymous"));
        User user = currentUser(principal);
        List<FileInfoResponse> list = storageService.list(user).stream()
                .map(m -> new FileInfoResponse(m.getId(), m.getFilename(), m.getContentType(), m.getSizeBytes(), m.getCreatedAt()))
                .toList();
        log.info("List success userId=" + user.getId() + ", count=" + list.size());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@AuthenticationPrincipal UserDetails principal,
                                                        @PathVariable Long id) {
        log.info("GET /api/files/" + id + "/download requested by=" + (principal != null ? principal.getUsername() : "anonymous"));
        User user = currentUser(principal);
        ResponseEntity<InputStreamResource> res = storageService.download(user, id);
        log.info("Download prepared userId=" + user.getId() + ", fileId=" + id + ", status=" + res.getStatusCode());
        return res;
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
