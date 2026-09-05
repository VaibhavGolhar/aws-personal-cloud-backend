package com.btech_major_project.Personal_Cloud.security;

import com.btech_major_project.Personal_Cloud.auth.AppUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtFilter;
    @Mock
    private AppUserDetailsService userDetailsService;
    @Mock
    private RestAuthenticationEntryPoint authenticationEntryPoint;
    @Mock
    private RestAccessDeniedHandler accessDeniedHandler;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void testAuthenticationManager() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);

        AuthenticationManager manager = securityConfig.authenticationManager(encoder);
        assertNotNull(manager);
    }

    @Test
    void testCorsConfigurationSource() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        assertNotNull(source);
    }

    @Test
    void testFilterChain() throws Exception {
        org.springframework.security.config.annotation.web.builders.HttpSecurity http = mock(org.springframework.security.config.annotation.web.builders.HttpSecurity.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        
        when(http.csrf(any())).thenReturn(http);
        when(http.cors(any())).thenReturn(http);
        when(http.sessionManagement(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.exceptionHandling(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(mock(org.springframework.security.web.DefaultSecurityFilterChain.class));

        org.springframework.security.web.SecurityFilterChain chain = securityConfig.filterChain(http);
        assertNotNull(chain);
    }
}
