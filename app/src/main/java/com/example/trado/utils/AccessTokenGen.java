package com.example.trado.utils;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.util.Collections;

public class AccessTokenGen {
    public static void main(String[] args) throws Exception {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream("service-account.json"))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));
        credentials.refreshIfExpired();
        System.out.println("Access Token: " + credentials.getAccessToken().getTokenValue());
    }
}
