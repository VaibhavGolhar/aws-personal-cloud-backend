package com.btech_major_project.Personal_Cloud.usage;

import com.btech_major_project.Personal_Cloud.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // Note: using ReflectionTestUtils ONLY to set ID on User for testing. It's an acceptable use in test setups to mock DB identity.

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsageServiceTest {

    @Mock
    private UserUsageRepository repo;

    @InjectMocks
    private UsageService usageService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        ReflectionTestUtils.setField(user, "id", 1L); // Simulating DB identity
    }

    @Test
    void ensureRow_CreatesRowIfMissing() {
        when(repo.findByUserId(1L)).thenReturn(Optional.empty());

        usageService.ensureRow(user);

        verify(repo).save(any(UserUsage.class));
    }

    @Test
    void ensureRow_DoesNothingIfExists() {
        when(repo.findByUserId(1L)).thenReturn(Optional.of(new UserUsage()));

        usageService.ensureRow(user);

        verify(repo, never()).save(any(UserUsage.class));
    }

    @Test
    void onPut_NewObject() {
        when(repo.findByUserId(1L)).thenReturn(Optional.of(new UserUsage()));

        usageService.onPut(user, 1024L, true);

        verify(repo).addPut(1L, 1);
        verify(repo).addBytes(1L, 1024L);
        verify(repo).addObjects(1L, 1);
    }

    @Test
    void onPut_ExistingObject() {
        when(repo.findByUserId(1L)).thenReturn(Optional.of(new UserUsage()));

        usageService.onPut(user, 512L, false);

        verify(repo).addPut(1L, 1);
        verify(repo).addBytes(1L, 512L);
        verify(repo, never()).addObjects(anyLong(), anyLong());
    }

    @Test
    void onList() {
        when(repo.findByUserId(1L)).thenReturn(Optional.of(new UserUsage()));

        usageService.onList(user);

        verify(repo).addList(1L, 1);
    }

    @Test
    void onGet() {
        when(repo.findByUserId(1L)).thenReturn(Optional.of(new UserUsage()));

        usageService.onGet(user);

        verify(repo).addGet(1L, 1);
    }

    @Test
    void onDelete() {
        when(repo.findByUserId(1L)).thenReturn(Optional.of(new UserUsage()));

        usageService.onDelete(user, 1024L);

        verify(repo).addDelete(1L, 1);
        verify(repo).addBytes(1L, -1024L);
        verify(repo).addObjects(1L, -1);
    }

    @Test
    void onCopy() {
        when(repo.findByUserId(1L)).thenReturn(Optional.of(new UserUsage()));

        usageService.onCopy(user);

        verify(repo).addCopy(1L, 1);
    }

    @Test
    void onPost() {
        when(repo.findByUserId(1L)).thenReturn(Optional.of(new UserUsage()));

        usageService.onPost(user);

        verify(repo).addPost(1L, 1);
    }

    @Test
    void onSelect() {
        when(repo.findByUserId(1L)).thenReturn(Optional.of(new UserUsage()));

        usageService.onSelect(user);

        verify(repo).addSelect(1L, 1);
    }

    @Test
    void onOther() {
        when(repo.findByUserId(1L)).thenReturn(Optional.of(new UserUsage()));

        usageService.onOther(user);

        verify(repo).addOther(1L, 1);
    }
}
