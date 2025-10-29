package com.btech_major_project.Personal_Cloud;

import com.btech_major_project.Personal_Cloud.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final AppLogger log = AppLogger.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        log.info("POST /api/auth/register email=" + req.getEmail());
        String token = authService.register(req);
        log.info("User registered successfully email=" + req.getEmail());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        log.info("POST /api/auth/login email=" + req.getEmail());
        String token = authService.login(req);
        log.info("User logged in successfully email=" + req.getEmail());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails principal) {
        log.info("GET /api/auth/me principal=" + (principal != null ? principal.getUsername() : "anonymous"));
        if (principal == null) {
            log.warn("/api/auth/me called without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Object() {
                public final String code = "UNAUTHORIZED";
                public final String message = "Authentication required";
            });
        }
        User u = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        log.info("/api/auth/me resolved user id=" + u.getId());
        return ResponseEntity.ok(new Object() {
            public final Long id = u.getId();
            public final String email = u.getEmail();
            public final String fullName = u.getFullName();
            public final String s3Prefix = u.getS3Prefix();
        });
    }
}
