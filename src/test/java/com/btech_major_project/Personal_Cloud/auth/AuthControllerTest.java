package com.btech_major_project.Personal_Cloud.auth;

import com.btech_major_project.Personal_Cloud.dto.AuthResponse;
import com.btech_major_project.Personal_Cloud.dto.LoginRequest;
import com.btech_major_project.Personal_Cloud.dto.RegisterRequest;
import com.btech_major_project.Personal_Cloud.dto.UserProfileResponse;
import com.btech_major_project.Personal_Cloud.user.User;
import com.btech_major_project.Personal_Cloud.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private UserService userService;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthController authController;

    @Test
    void register() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testuser");
        when(authService.register(req)).thenReturn("jwt_token");

        ResponseEntity<AuthResponse> response = authController.register(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt_token", response.getBody().getAccessToken());
    }

    @Test
    void login() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        when(authService.login(req)).thenReturn("jwt_token");

        ResponseEntity<AuthResponse> response = authController.login(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt_token", response.getBody().getAccessToken());
    }

    @Test
    void me_Success() {
        when(userDetails.getUsername()).thenReturn("testuser");
        
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setUsername("testuser");
        user.setFullName("Test User");
        
        when(userService.findByUsername("testuser")).thenReturn(user);

        ResponseEntity<?> response = authController.me(userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof UserProfileResponse);
        UserProfileResponse profile = (UserProfileResponse) response.getBody();
        assertEquals(1L, profile.getId());
        assertEquals("testuser", profile.getUsername());
        assertEquals("Test User", profile.getFullName());
    }

    @Test
    void me_Unauthenticated() {
        ResponseEntity<?> response = authController.me(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
