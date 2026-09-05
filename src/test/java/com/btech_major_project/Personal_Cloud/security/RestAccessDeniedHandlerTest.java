package com.btech_major_project.Personal_Cloud.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RestAccessDeniedHandlerTest {

    @Test
    void handle() throws IOException {
        RestAccessDeniedHandler handler = new RestAccessDeniedHandler();
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/api/test");
        MockHttpServletResponse res = new MockHttpServletResponse();

        AccessDeniedException ex = new AccessDeniedException("Denied");

        handler.handle(req, res, ex);

        assertEquals(403, res.getStatus());
        assertEquals("application/json", res.getContentType());
        assertTrue(res.getContentAsString().contains("FORBIDDEN"));
        assertTrue(res.getContentAsString().contains("Access denied"));
        assertTrue(res.getContentAsString().contains("Denied"));
    }
}
