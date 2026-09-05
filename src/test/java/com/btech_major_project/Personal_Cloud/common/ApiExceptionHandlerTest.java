package com.btech_major_project.Personal_Cloud.common;

import com.btech_major_project.Personal_Cloud.dto.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handleIllegalArgument() {
        ResponseEntity<ApiError> res = handler.handleIllegalArgument(new IllegalArgumentException("test arg error"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("BAD_REQUEST", res.getBody().getCode());
        assertEquals("test arg error", res.getBody().getMessage());
    }

    @Test
    void handleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("obj", "field1", "must not be null")));

        ResponseEntity<ApiError> res = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("VALIDATION_ERROR", res.getBody().getCode());
        assertEquals("field1: must not be null", res.getBody().getMessage());
    }

    @Test
    void handleValidation_EmptyErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<ApiError> res = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("VALIDATION_ERROR", res.getBody().getCode());
        assertEquals("Validation failed", res.getBody().getMessage());
    }

    @Test
    void handleMultipart() {
        ResponseEntity<ApiError> res = handler.handleMultipart(new MultipartException("multipart error"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("MULTIPART_ERROR", res.getBody().getCode());
    }

    @Test
    void handleMissingPart() {
        ResponseEntity<ApiError> res = handler.handleMissingPart(new MissingServletRequestPartException("file"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("MISSING_PART", res.getBody().getCode());
        assertEquals("file part is required", res.getBody().getMessage());
    }

    @Test
    void handleMaxUpload() {
        ResponseEntity<ApiError> res = handler.handleMaxUpload(new MaxUploadSizeExceededException(1024));
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, res.getStatusCode());
        assertEquals("PAYLOAD_TOO_LARGE", res.getBody().getCode());
    }

    @Test
    void handleS3_WithDetails() {
        AwsErrorDetails details = AwsErrorDetails.builder().errorMessage("s3 detail").build();
        S3Exception ex = (S3Exception) S3Exception.builder().message("s3 error").awsErrorDetails(details).build();
        ResponseEntity<ApiError> res = handler.handleS3(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        assertEquals("S3_ERROR", res.getBody().getCode());
        assertEquals("s3 detail", res.getBody().getDetails());
    }

    @Test
    void handleS3_NoDetails() {
        S3Exception ex = (S3Exception) S3Exception.builder().message("s3 error").build();
        ResponseEntity<ApiError> res = handler.handleS3(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        assertEquals("S3_ERROR", res.getBody().getCode());
        assertNull(res.getBody().getDetails());
    }

    @Test
    void handleAuthentication() {
        ResponseEntity<ApiError> res = handler.handleAuthentication(new BadCredentialsException("bad"));
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        assertEquals("UNAUTHORIZED", res.getBody().getCode());
    }

    @Test
    void handleGeneral() {
        ResponseEntity<ApiError> res = handler.handleGeneral(new RuntimeException("general error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        assertEquals("INTERNAL_ERROR", res.getBody().getCode());
        assertEquals("general error", res.getBody().getMessage());
    }
}
