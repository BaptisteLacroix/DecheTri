package etu.seinksansdoozebank.dechetri.ui.notifications;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import etu.seinksansdoozebank.dechetri.MainActivity;
import etu.seinksansdoozebank.dechetri.R;

public class TaskCompletedNotification implements INotification {
    private int notificationId = 0;
    private static final long NOTIFICATION_DURATION = 5000;

    @Override
    public void sendNotification(Activity activity, Context context, String title, String message, String channelId, int priority) {
        askNotificationPermission(activity, context, new NotificationPermissionCallback() {
            @Override
            public void onPermissionGranted() {
                try {
                    // Create an intent that opens the MainActivity
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    // Build the notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setPriority(priority)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    int currentNotificationId = notificationId;
                    notificationManager.notify(currentNotificationId, builder.build());

                    // Remove the notification after a certain duration
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> notificationManager.cancel(currentNotificationId), NOTIFICATION_DURATION);
                    notificationId++;
                } catch (SecurityException e) {
                    // Handle security exception
                    e.printStackTrace();
                }
            }

            @Override
            public void onPermissionDenied() {
                // Do nothing
            }
        });
    }
}
