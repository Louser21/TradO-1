package com.example.trado;

import android.content.Context;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static final String NOTIFICATION_TYPE_NEW_MESSAGE = "NEW_MESSAGE";

    // FCM SERVER KEY (handled securely in prod! put yours here for now)
    public static final String MESSAGE_TYPE_TEXT = "TEXT";
    public static final String MESSAGE_TYPE_IMAGE = "IMAGE";

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String chatPath(String uid1, String uid2) {
        if (uid1 == null || uid2 == null) {
            return "invalid_chat";
        }
        String[] arr = {uid1, uid2};
        Arrays.sort(arr);
        return arr[0] + "_" + arr[1];
    }


    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    public static String formatTimestampDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }
}
