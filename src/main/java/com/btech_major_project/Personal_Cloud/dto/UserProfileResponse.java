package com.btech_major_project.Personal_Cloud.dto;

/**
 * Response DTO for the /api/auth/me endpoint.
 * Exposes only the fields a client needs — deliberately excludes
 * sensitive internals such as the user's S3 prefix.
 */
public class UserProfileResponse {

    private final Long id;
    private final String username;
    private final String fullName;

    public UserProfileResponse(Long id, String username, String fullName) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
}
