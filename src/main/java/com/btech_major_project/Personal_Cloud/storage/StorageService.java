package com.btech_major_project.Personal_Cloud.storage;

import com.btech_major_project.Personal_Cloud.user.User;

import com.btech_major_project.Personal_Cloud.dto.FileDownloadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StorageService {
    FileMetadata upload(User user, MultipartFile file, String subPath) throws IOException;
    List<FileMetadata> uploadBulk(User user, List<MultipartFile> files, String subPath) throws IOException;
    Page<FileMetadata> list(User user, Pageable pageable);
    FileDownloadResult download(User user, Long fileId);
    void downloadBulk(User user, List<Long> fileIds, java.io.OutputStream outputStream) throws IOException;
    void delete(User user, Long fileId);
}
