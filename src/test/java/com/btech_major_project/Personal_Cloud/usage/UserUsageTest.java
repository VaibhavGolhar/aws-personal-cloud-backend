package com.btech_major_project.Personal_Cloud.usage;

import com.btech_major_project.Personal_Cloud.user.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserUsageTest {

    @Test
    void testUserUsageGettersSettersAndLifecycle() throws InterruptedException {
        UserUsage usage = new UserUsage();
        User user = new User();
        usage.setUser(user);

        usage.setTotalBytesStored(100L);
        usage.setObjectCount(10L);
        usage.setPutCount(1L);
        usage.setCopyCount(2L);
        usage.setPostCount(3L);
        usage.setListCount(4L);
        usage.setGetCount(5L);
        usage.setSelectCount(6L);
        usage.setDeleteCount(7L);
        usage.setOtherCount(8L);

        assertEquals(user, usage.getUser());
        assertEquals(100L, usage.getTotalBytesStored());
        assertEquals(10L, usage.getObjectCount());
        assertEquals(1L, usage.getPutCount());
        assertEquals(2L, usage.getCopyCount());
        assertEquals(3L, usage.getPostCount());
        assertEquals(4L, usage.getListCount());
        assertEquals(5L, usage.getGetCount());
        assertEquals(6L, usage.getSelectCount());
        assertEquals(7L, usage.getDeleteCount());
        assertEquals(8L, usage.getOtherCount());
        assertNull(usage.getId());

        usage.onCreate();
        Instant created = usage.getCreatedAt();
        assertNotNull(created);
        assertNotNull(usage.getUpdatedAt());

        Thread.sleep(10);
        usage.onUpdate();
        assertTrue(usage.getUpdatedAt().isAfter(created));
    }
}
