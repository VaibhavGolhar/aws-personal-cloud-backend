package com.btech_major_project.Personal_Cloud;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client S3Client() {
        return S3Client.create();
    }
}
