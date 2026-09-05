package com.btech_major_project.Personal_Cloud.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RestAuthenticationEntryPointTest {

    @Test
    void commence() throws IOException {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/api/test");
        MockHttpServletResponse res = new MockHttpServletResponse();

        AuthenticationException ex = new org.springframework.security.authentication.InsufficientAuthenticationException("Unauth");

        entryPoint.commence(req, res, ex);

        assertEquals(401, res.getStatus());
        assertEquals("application/json", res.getContentType());
        assertTrue(res.getContentAsString().contains("UNAUTHORIZED"));
        assertTrue(res.getContentAsString().contains("Authentication required"));
        assertTrue(res.getContentAsString().contains("Unauth"));
    }
}
