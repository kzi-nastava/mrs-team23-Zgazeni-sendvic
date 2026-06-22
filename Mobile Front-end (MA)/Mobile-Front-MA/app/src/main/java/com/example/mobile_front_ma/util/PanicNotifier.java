package com.example.mobile_front_ma.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.activities.PanicNotificationsActivity;
import com.example.mobile_front_ma.models.dto.PanicResponse;

/**
 * Posts Android system notifications for panic alerts (spec 2.6.3: "use Android's built-in
 * notification system"). One high-importance channel; tapping a notification opens the admin
 * panic list. The alerts themselves are persisted by the backend, so this is only the heads-up
 * for an admin who has the app open.
 */
public class PanicNotifier {

    private static final String CHANNEL_ID = "panic_alerts";
    private static final int CREATED_ID_BASE = 1000;
    private static final int RESOLVED_ID_BASE = 5000;

    private final Context ctx;

    public PanicNotifier(Context context) {
        this.ctx = context.getApplicationContext();
        createChannel();
    }

    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                ctx.getString(R.string.panic_channel_name),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(ctx.getString(R.string.panic_channel_desc));
        channel.enableVibration(true);
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.createNotificationChannel(channel);
        }
    }

    /** A new panic was raised: high-priority alert. */
    public void showPanic(PanicResponse panic) {
        int slot = slotFor(panic);
        post(CREATED_ID_BASE + slot,
                ctx.getString(R.string.panic_notif_title),
                ctx.getString(R.string.panic_notif_body, safe(panic.callerName), rideLabel(panic)),
                true);
    }

    /** A panic was resolved: clear the matching alert and show a calmer confirmation. */
    public void showResolved(PanicResponse panic) {
        int slot = slotFor(panic);
        NotificationManagerCompat.from(ctx).cancel(CREATED_ID_BASE + slot);
        post(RESOLVED_ID_BASE + slot,
                ctx.getString(R.string.panic_notif_resolved_title),
                ctx.getString(R.string.panic_notif_resolved_body, safe(panic.callerName), rideLabel(panic)),
                false);
    }

    private void post(int id, String title, String text, boolean urgent) {
        Intent open = new Intent(ctx, PanicNotificationsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, id, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_panic_alert)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(urgent ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(urgent ? NotificationCompat.CATEGORY_ALARM : NotificationCompat.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        try {
            NotificationManagerCompat.from(ctx).notify(id, builder.build());
        } catch (SecurityException ignored) {
            // POST_NOTIFICATIONS not granted (Android 13+); the in-app list still updates.
        }
    }

    /** Group a ride's "active" and "resolved" notifications onto the same slot. */
    private int slotFor(PanicResponse panic) {
        long base = panic.rideId != null ? panic.rideId : (panic.id != null ? panic.id : 0L);
        return (int) Math.abs(base % 1000);
    }

    private String rideLabel(PanicResponse panic) {
        return panic.rideId != null ? String.valueOf(panic.rideId) : "?";
    }

    private String safe(String s) {
        return s == null || s.trim().isEmpty() ? "Unknown" : s;
    }
}
