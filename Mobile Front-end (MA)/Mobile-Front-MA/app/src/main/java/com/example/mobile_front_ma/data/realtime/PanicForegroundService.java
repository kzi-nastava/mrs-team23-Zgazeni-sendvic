package com.example.mobile_front_ma.data.realtime;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.activities.PanicNotificationsActivity;
import com.example.mobile_front_ma.data.SessionManager;

/**
 * Foreground service that keeps the panic WebSocket alive while an admin is logged in, including
 * when the app is in the background (spec 2.6.3, background-delivery upgrade).
 *
 * <p>The app-scoped {@link PanicRealtimeManager} still owns the socket, the system notifications
 * and the live fan-out; this service just keeps the process alive and connected. It runs only
 * for a logged-in admin, stops on logout and when the task is removed (see {@code stopWithTask}
 * in the manifest), and is declared as a {@code specialUse} foreground service because it
 * maintains a connection to receive emergency alerts (no other FGS type fits, and unlike
 * {@code dataSync} it isn't subject to a daily time cap).
 */
public class PanicForegroundService extends Service {

    private static final String CHANNEL_ID = "panic_listener";
    private static final int NOTIFICATION_ID = 4242;

    public static void start(Context context) {
        ContextCompat.startForegroundService(context,
                new Intent(context, PanicForegroundService.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, PanicForegroundService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Always enter the foreground first (must happen within the start timeout), then decide.
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                ? ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE : 0;
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), type);

        SessionManager session = new SessionManager(this);
        boolean isAdmin = session.isLoggedIn() && "ADMIN".equalsIgnoreCase(session.getRole());
        if (!isAdmin) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (PanicRealtimeManager.get() != null) {
            PanicRealtimeManager.get().start();
        }
        // Restarted with a null intent after an OOM kill: onStartCommand re-checks the session.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (PanicRealtimeManager.get() != null) {
            PanicRealtimeManager.get().stop();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannel() {
        // Low importance: the ongoing "listening" notice should be quiet; the actual alerts use
        // the separate high-importance channel in PanicNotifier.
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.panic_service_channel_name),
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.panic_service_channel_desc));
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.createNotificationChannel(channel);
        }
    }

    private android.app.Notification buildNotification() {
        Intent open = new Intent(this, PanicNotificationsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_panic_alert)
                .setContentTitle(getString(R.string.panic_service_title))
                .setContentText(getString(R.string.panic_service_text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .setShowWhen(false)
                .setContentIntent(contentIntent)
                .build();
    }
}
