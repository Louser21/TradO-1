package com.example.trado.utils;

import android.util.Log;
import com.google.auth.oauth2.GoogleCredentials;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.Collections;

public class FcmV1Sender {
    private static final String TAG = "FcmV1Sender";
    private static final String PROJECT_ID = "trado-1"; // from service-account.json

    public static void send(JSONObject notificationJo) {
        new Thread(() -> {
            try {
                GoogleCredentials credentials = GoogleCredentials
                        .fromStream(new FileInputStream("service-account.json"))
                        .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));
                credentials.refreshIfExpired();
                String token = credentials.getAccessToken().getTokenValue();

                URL url = new URL("https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json; UTF-8");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(notificationJo.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "FCM v1 Response: " + responseCode);

            } catch (Exception e) {
                Log.e(TAG, "send: ", e);
            }
        }).start();
    }
}
