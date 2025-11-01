package com.example.trado;

public class User {
    private String userId; // Optional, if you store it
    private String fullName;
    private String email;
    private String memberSince;
    private String profileImageUrl;
    // Add other user-related fields as per your Firebase database

    public User() {
        // Default constructor required for Firebase
    }

    public User(String userId, String fullName, String email, String memberSince, String profileImageUrl) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.memberSince = memberSince;
        this.profileImageUrl = profileImageUrl;
    }

    // --- Getters ---
    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getMemberSince() {
        return memberSince;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // --- Setters (optional) ---
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMemberSince(String memberSince) {
        this.memberSince = memberSince;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}

//User Java