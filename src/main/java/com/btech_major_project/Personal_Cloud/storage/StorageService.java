package com.btech_major_project.Personal_Cloud.storage;

import com.btech_major_project.Personal_Cloud.user.User;

import com.btech_major_project.Personal_Cloud.dto.FileDownloadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface StorageService {
    FileMetadata upload(User user, MultipartFile file, String subPath) throws IOException;
    List<FileMetadata> uploadBulk(User user, List<MultipartFile> files, String subPath) throws IOException;
    List<FileMetadata> list(User user);
    FileDownloadResult download(User user, Long fileId);
    void downloadBulk(User user, List<Long> fileIds, java.io.OutputStream outputStream) throws IOException;
    void delete(User user, Long fileId);
}
