package com.btech_major_project.Personal_Cloud.config;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AwsConfigTest {

    @Test
    void testS3Client() {
        AwsConfig config = new AwsConfig();
        S3Client client = config.s3Client("us-east-1");
        assertNotNull(client);
    }
}
