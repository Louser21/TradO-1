package com.example.trado.models;

public class User {

    private String userId;
    private String name;
    private String email;
    private String dob;
    private String phoneCode;
    private String phoneNumber;
    private String profileImageUrl;
    private String userType;
    private String timestamp;

    public User() {}

    public User(String userId, String name, String email, String dob, String phoneCode, String phoneNumber,
                String profileImageUrl, String userType, String timestamp) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.dob = dob;
        this.phoneCode = phoneCode;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.userType = userType;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getDob() { return dob; }
    public String getPhoneCode() { return phoneCode; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getUserType() { return userType; }
    public String getTimestamp() { return timestamp; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setDob(String dob) { this.dob = dob; }
    public void setPhoneCode(String phoneCode) { this.phoneCode = phoneCode; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setUserType(String userType) { this.userType = userType; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
