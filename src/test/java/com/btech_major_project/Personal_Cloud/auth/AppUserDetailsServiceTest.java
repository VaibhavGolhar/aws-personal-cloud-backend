package com.btech_major_project.Personal_Cloud.auth;

import com.btech_major_project.Personal_Cloud.user.User;
import com.btech_major_project.Personal_Cloud.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("hash");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("testuser");

        assertEquals("testuser", details.getUsername());
        assertEquals("hash", details.getPassword());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_NotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("testuser"));
    }
}
