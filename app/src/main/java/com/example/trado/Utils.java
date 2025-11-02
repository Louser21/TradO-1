package com.example.trado;

import android.content.Context;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    public static String formatTimestampDate(long l) {
        Date date = new Date(l);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }
}
