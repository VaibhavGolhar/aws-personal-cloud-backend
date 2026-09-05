package com.btech_major_project.Personal_Cloud.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Component("frontend")
public class FrontendHealthIndicator implements HealthIndicator {

    private final String frontendUrl;
    private final RestTemplate restTemplate;

    public FrontendHealthIndicator(@Value("${app.frontend.url}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public Health health() {
        try {
            restTemplate.getForEntity(frontendUrl, String.class);
            return Health.up().withDetail("url", frontendUrl).build();
        } catch (RestClientException e) {
            return Health.down(e).withDetail("url", frontendUrl).build();
        }
    }
}
