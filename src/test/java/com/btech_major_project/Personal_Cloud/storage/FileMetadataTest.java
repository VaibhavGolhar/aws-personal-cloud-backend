package com.btech_major_project.Personal_Cloud.storage;

import com.btech_major_project.Personal_Cloud.user.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FileMetadataTest {

    @Test
    void testFileMetadataGettersSettersAndLifecycle() throws InterruptedException {
        FileMetadata meta = new FileMetadata();
        User user = new User();
        meta.setUser(user);
        meta.setS3Key("key");
        meta.setFilename("file.txt");
        meta.setContentType("text/plain");
        meta.setSizeBytes(1024L);
        meta.setEtag("etag123");
        meta.setVersionId("v1");

        assertEquals(user, meta.getUser());
        assertEquals("key", meta.getS3Key());
        assertEquals("file.txt", meta.getFilename());
        assertEquals("text/plain", meta.getContentType());
        assertEquals(1024L, meta.getSizeBytes());
        assertEquals("etag123", meta.getEtag());
        assertEquals("v1", meta.getVersionId());
        assertNull(meta.getId());

        meta.onCreate();
        Instant created = meta.getCreatedAt();
        assertNotNull(created);
        assertNotNull(meta.getUpdatedAt());

        Thread.sleep(10);
        meta.onUpdate();
        assertTrue(meta.getUpdatedAt().isAfter(created));
    }
}
