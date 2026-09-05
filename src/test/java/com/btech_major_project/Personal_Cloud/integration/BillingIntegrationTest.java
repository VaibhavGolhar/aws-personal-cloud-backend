package com.btech_major_project.Personal_Cloud.integration;

import com.btech_major_project.Personal_Cloud.dto.LoginRequest;
import com.btech_major_project.Personal_Cloud.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BillingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getBillingCurrent_Success() throws Exception {
        String email = "billing_integration@example.com";
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

        // 3. Get Billing
        mockMvc.perform(get("/api/billing/current")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageBytes").value(0))
                .andExpect(jsonPath("$.storageCost").value(0.0))
                .andExpect(jsonPath("$.writeRequests").exists())
                .andExpect(jsonPath("$.readRequests").exists())
                .andExpect(jsonPath("$.total").value(0.0));
    }
}
