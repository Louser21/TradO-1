package com.example.trado;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.trado.ChatActivity;

import java.util.Random;

public class MyFcmService extends FirebaseMessagingService {

    private static final String TAG = "FCM_SERVICE_TAG";
    private static final String CHANNEL_ID = "CHAT_CHANNEL";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getNotification() != null
                ? remoteMessage.getNotification().getTitle() : "New Message";

        String body = remoteMessage.getNotification() != null
                ? remoteMessage.getNotification().getBody() : "You’ve got a new chat 💬";

        String senderUid = remoteMessage.getData().get("senderUid");
        Log.d(TAG, "Message from: " + senderUid);

        showChatNotification(title, body, senderUid);
    }

    private void showChatNotification(String title, String body, String senderUid) {
        int notificationId = new Random().nextInt(3000);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) return;
        setupNotificationChannel(manager);

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("receiptUid", senderUid);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // replace with your chat icon
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        manager.notify(notificationId, builder.build());
    }

    private void setupNotificationChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chat Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for new chat messages");
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }
    }
}
