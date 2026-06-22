package com.example.mobile_front_ma.data.realtime;

import android.content.Context;

import com.example.mobile_front_ma.data.SessionManager;
import com.example.mobile_front_ma.data.network.ApiClient;
import com.example.mobile_front_ma.models.dto.PanicResponse;
import com.example.mobile_front_ma.util.PanicNotifier;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * App-scoped coordinator for live panic alerts (spec 2.6.3, admin side).
 *
 * <p>While an ADMIN is logged in, it keeps a STOMP socket open to the panic topics, raises an
 * Android system notification for every event, and fans the events out to whichever screen
 * (the panic list) is currently observing. The connection's lifetime is driven by
 * {@link PanicForegroundService}, so it survives the app going to the background.
 *
 * <p>An admin who was offline when an alert came in still sees it in the list, which is loaded
 * from the server where the alerts are persisted.
 */
public class PanicRealtimeManager implements PanicSocketClient.Listener {

    /** Implemented by a screen (e.g. the panic list) that wants live updates while visible. */
    public interface Observer {
        void onPanicCreated(PanicResponse panic);

        void onPanicResolved(PanicResponse panic);
    }

    private static PanicRealtimeManager instance;

    private final SessionManager session;
    private final PanicNotifier notifier;
    private final CopyOnWriteArraySet<Observer> observers = new CopyOnWriteArraySet<>();

    private PanicSocketClient socket;

    private PanicRealtimeManager(Context appContext) {
        this.session = new SessionManager(appContext);
        this.notifier = new PanicNotifier(appContext);
    }

    public static synchronized void init(Context appContext) {
        if (instance == null) {
            instance = new PanicRealtimeManager(appContext.getApplicationContext());
        }
    }

    public static PanicRealtimeManager get() {
        return instance;
    }

    // ---- connection lifetime (driven by PanicForegroundService) ----

    /** Connect the panic socket if an admin is logged in. Idempotent. */
    public synchronized void start() {
        ensureConnected();
    }

    /** Drop the panic socket. */
    public synchronized void stop() {
        disconnect();
    }

    private void ensureConnected() {
        if (!isAdmin()) {
            disconnect();
            return;
        }
        if (socket == null) {
            socket = new PanicSocketClient(ApiClient.BASE_URL, this);
            socket.connect();
        }
    }

    private void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
    }

    private boolean isAdmin() {
        return session.isLoggedIn() && "ADMIN".equalsIgnoreCase(session.getRole());
    }

    // ---- observers (the open panic-list screen) ----

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    // ---- socket callbacks (already delivered on the main thread) ----

    @Override
    public void onPanicCreated(PanicResponse panic) {
        notifier.showPanic(panic);
        for (Observer observer : observers) {
            observer.onPanicCreated(panic);
        }
    }

    @Override
    public void onPanicResolved(PanicResponse panic) {
        notifier.showResolved(panic);
        for (Observer observer : observers) {
            observer.onPanicResolved(panic);
        }
    }
}
