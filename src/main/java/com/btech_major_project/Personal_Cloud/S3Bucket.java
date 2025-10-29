package com.btech_major_project.Personal_Cloud;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

public class S3Bucket {

    private final S3Client s3Client;

    public S3Bucket(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    private static final AppLogger log = AppLogger.getLogger(S3Bucket.class);


}
