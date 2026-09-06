package com.btech_major_project.Personal_Cloud.integration;

import com.btech_major_project.Personal_Cloud.dto.LoginRequest;
import com.btech_major_project.Personal_Cloud.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import software.amazon.awssdk.services.s3.S3Client;

import com.jayway.jsonpath.JsonPath;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private S3Client s3Client; // Mock AWS S3 so we don't hit real buckets

    @Test
    void uploadAndListFiles_Success() throws Exception {
        String email = "file_integration@example.com";
        String password = "StrongPassword123!";

        // 1. Register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(email);
        registerRequest.setPassword(password);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 2. Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(email);
        loginRequest.setPassword(password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = loginResult.getResponse().getContentAsString();
        String token = JsonPath.parse(responseString).read("$.accessToken");

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().eTag("mock-etag").build());

        // 3. Upload File
        MockMultipartFile file = new MockMultipartFile(
                "file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes()
        );

        mockMvc.perform(multipart("/api/files")
                .file(file)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("hello.txt"));

        // 4. List Files
        mockMvc.perform(get("/api/files")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].filename").value("hello.txt"))
                .andExpect(jsonPath("$.content[0].sizeBytes").value(13)); // length of "Hello, World!"
    }
}
