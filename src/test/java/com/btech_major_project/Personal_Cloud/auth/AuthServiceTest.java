package com.btech_major_project.Personal_Cloud.auth;

import com.btech_major_project.Personal_Cloud.dto.LoginRequest;
import com.btech_major_project.Personal_Cloud.dto.RegisterRequest;
import com.btech_major_project.Personal_Cloud.security.JwtService;
import com.btech_major_project.Personal_Cloud.user.User;
import com.btech_major_project.Personal_Cloud.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testuser");
        req.setPassword("pass123");
        req.setFullName("Test User");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded_pass");
        when(jwtService.generateToken("testuser")).thenReturn("jwt_token");

        String token = authService.register(req);

        assertEquals("jwt_token", token);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("Test User", savedUser.getFullName());
        assertEquals("encoded_pass", savedUser.getPasswordHash());
        assertTrue(savedUser.getS3Prefix().startsWith("u-"));
        assertTrue(savedUser.getS3Prefix().endsWith("/"));
    }

    @Test
    void register_FailsWhenUsernameExists() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("existing");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.register(req));
        assertEquals("Username already registered", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("pass123");

        when(jwtService.generateToken("testuser")).thenReturn("jwt_token");

        String token = authService.login(req);

        assertEquals("jwt_token", token);
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        
        assertEquals("testuser", authCaptor.getValue().getPrincipal());
        assertEquals("pass123", authCaptor.getValue().getCredentials());
    }
}
