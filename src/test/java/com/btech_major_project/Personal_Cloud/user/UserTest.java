package com.btech_major_project.Personal_Cloud.user;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserGettersSettersAndLifecycle() throws InterruptedException {
        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("hash");
        user.setFullName("Test User");
        user.setS3Prefix("u-prefix/");

        assertEquals("testuser", user.getUsername());
        assertEquals("hash", user.getPasswordHash());
        assertEquals("Test User", user.getFullName());
        assertEquals("u-prefix/", user.getS3Prefix());
        assertNull(user.getId());

        // Test @PrePersist
        user.onCreate();
        Instant created = user.getCreatedAt();
        Instant updated = user.getUpdatedAt();
        assertNotNull(created);
        assertNotNull(updated);
        assertEquals(created, updated);

        // Test @PreUpdate
        Thread.sleep(10); // Ensure time difference
        user.onUpdate();
        assertTrue(user.getUpdatedAt().isAfter(created));
    }
}
