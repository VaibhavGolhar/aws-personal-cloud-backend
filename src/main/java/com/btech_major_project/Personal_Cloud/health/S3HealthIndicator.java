package com.btech_major_project.Personal_Cloud.health;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Component("s3")
public class S3HealthIndicator implements HealthIndicator {

    private final S3Client s3Client;
    private final String bucket;

    public S3HealthIndicator(@Qualifier("s3Client") S3Client s3Client, 
                             @Value("${app.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public Health health() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            return Health.up().withDetail("bucket", bucket).build();
        } catch (Exception e) {
            return Health.down(e).withDetail("bucket", bucket).build();
        }
    }
}
