package com.btech_major_project.Personal_Cloud.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testApiError() {
        ApiError error = new ApiError("CODE", "Message", "Details");
        assertEquals("CODE", error.getCode());
        assertEquals("Message", error.getMessage());
        assertEquals("Details", error.getDetails());
        assertNotNull(error.getTimestamp());
    }

    @Test
    void testAuthResponse() {
        AuthResponse response = new AuthResponse("token123");
        assertEquals("token123", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void testBillingSummary() {
        BillingSummary summary = BillingSummary.empty();
        assertEquals(0L, summary.getStorageBytes());
        assertEquals(BigDecimal.ZERO.setScale(6), summary.getStorageGb());
        assertEquals("USD", summary.getCurrency());

        summary.setStorageBytes(100L);
        summary.setStorageGb(BigDecimal.ONE);
        summary.setStorageCost(BigDecimal.TEN);
        summary.setWriteRequests(5L);
        summary.setWriteCost(BigDecimal.valueOf(5));
        summary.setReadRequests(10L);
        summary.setReadCost(BigDecimal.valueOf(2));
        summary.setTotal(BigDecimal.valueOf(17));
        summary.setCurrency("EUR");

        assertEquals(100L, summary.getStorageBytes());
        assertEquals(BigDecimal.ONE, summary.getStorageGb());
        assertEquals(BigDecimal.TEN, summary.getStorageCost());
        assertEquals(5L, summary.getWriteRequests());
        assertEquals(BigDecimal.valueOf(5), summary.getWriteCost());
        assertEquals(10L, summary.getReadRequests());
        assertEquals(BigDecimal.valueOf(2), summary.getReadCost());
        assertEquals(BigDecimal.valueOf(17), summary.getTotal());
        assertEquals("EUR", summary.getCurrency());
    }

    @Test
    void testFileDownloadResult() {
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(new byte[0]));
        FileDownloadResult result = new FileDownloadResult(resource, "file.txt", "text/plain");

        assertEquals(resource, result.getResource());
        assertEquals("file.txt", result.getFilename());
        assertEquals("text/plain", result.getContentType());
    }

    @Test
    void testFileInfoResponse() {
        Instant now = Instant.now();
        FileInfoResponse response = new FileInfoResponse(1L, "file.txt", "text/plain", 1024L, now);

        assertEquals(1L, response.getId());
        assertEquals("file.txt", response.getFilename());
        assertEquals("text/plain", response.getContentType());
        assertEquals(1024L, response.getSizeBytes());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    void testLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("pass");

        assertEquals("user", request.getUsername());
        assertEquals("pass", request.getPassword());
    }

    @Test
    void testRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user");
        request.setPassword("pass");
        request.setFullName("Full Name");

        assertEquals("user", request.getUsername());
        assertEquals("pass", request.getPassword());
        assertEquals("Full Name", request.getFullName());
    }

    @Test
    void testUserProfileResponse() {
        UserProfileResponse response = new UserProfileResponse(1L, "user", "Full Name");

        assertEquals(1L, response.getId());
        assertEquals("user", response.getUsername());
        assertEquals("Full Name", response.getFullName());
    }

    @Test
    void testLoginRequestValidation() {
        LoginRequest req = new LoginRequest();
        // Empty fields should violate @NotBlank
        assertFalse(validator.validate(req).isEmpty());
        
        req.setUsername("user");
        req.setPassword("pass");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testRegisterRequestValidation() {
        RegisterRequest req = new RegisterRequest();
        // Empty fields should violate @NotBlank
        assertFalse(validator.validate(req).isEmpty());
        
        req.setUsername("user");
        req.setPassword("short"); // length < 8, should violate @Size
        assertFalse(validator.validate(req).isEmpty());

        req.setPassword("longenoughpass");
        assertTrue(validator.validate(req).isEmpty());
    }
}
