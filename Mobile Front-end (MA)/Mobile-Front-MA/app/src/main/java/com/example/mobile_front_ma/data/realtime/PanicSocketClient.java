package com.example.mobile_front_ma.data.realtime;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobile_front_ma.models.dto.PanicResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Minimal STOMP-over-WebSocket client for the two panic topics, built directly on OkHttp
 * (already on the classpath via Retrofit) rather than pulling in a SockJS/STOMP library.
 *
 * <p>The backend registers a SockJS endpoint at {@code /ws}; its raw-WebSocket transport at
 * {@code /ws/websocket} speaks plain STOMP frames (no SockJS array framing), which is what this
 * client talks. It performs the STOMP handshake, subscribes to {@code /topic/panic} and
 * {@code /topic/panic/resolved}, and parses the JSON body of each {@code MESSAGE} frame into a
 * {@link PanicResponse}.
 *
 * <p>STOMP framing: a command line, header lines ({@code name:value}) each ended by a newline,
 * a blank line, the body, then a NUL (0x00) terminator. Callbacks are delivered on the main
 * thread. The client reconnects itself with backoff until {@link #disconnect()} is called.
 */
public class PanicSocketClient {

    public interface Listener {
        void onPanicCreated(PanicResponse panic);

        void onPanicResolved(PanicResponse panic);
    }

    private static final String TAG = "PanicSocket";
    private static final String TOPIC_PANIC = "/topic/panic";
    private static final String TOPIC_RESOLVED = "/topic/panic/resolved";

    /** The STOMP frame terminator: the NUL byte. */
    private static final char NUL = (char) 0;

    private static final long RECONNECT_BASE_MS = 3000;
    private static final long RECONNECT_MAX_MS = 30000;

    private final String wsUrl;
    private final Listener listener;
    private final Gson gson = new Gson();
    private final OkHttpClient client;
    private final Handler main = new Handler(Looper.getMainLooper());

    private WebSocket webSocket;
    private boolean stopped = false;
    private int reconnectAttempts = 0;

    /**
     * @param httpBaseUrl the REST base url (e.g. {@code http://10.0.2.2:8080/}); this is
     *                    translated to the websocket url {@code ws://10.0.2.2:8080/ws/websocket}.
     */
    public PanicSocketClient(String httpBaseUrl, Listener listener) {
        String base = httpBaseUrl.replaceFirst("^http", "ws"); // http->ws, https->wss
        if (!base.endsWith("/")) {
            base += "/";
        }
        this.wsUrl = base + "ws/websocket";
        this.listener = listener;
        // Low-level ping/pong keeps the connection alive without implementing STOMP heartbeats.
        this.client = new OkHttpClient.Builder()
                .pingInterval(20, TimeUnit.SECONDS)
                .build();
    }

    public void connect() {
        stopped = false;
        openSocket();
    }

    private void openSocket() {
        if (stopped) {
            return;
        }
        Log.d(TAG, "connecting to " + wsUrl);
        Request request = new Request.Builder().url(wsUrl).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket ws, @NonNull Response response) {
                Log.d(TAG, "websocket open -> STOMP CONNECT");
                ws.send(connectFrame());
            }

            @Override
            public void onMessage(@NonNull WebSocket ws, @NonNull String text) {
                // A websocket message may carry one or more NUL-terminated STOMP frames.
                for (String frame : splitFrames(text)) {
                    handleFrame(ws, frame);
                }
            }

            @Override
            public void onClosing(@NonNull WebSocket ws, int code, @NonNull String reason) {
                ws.close(1000, null);
            }

            @Override
            public void onClosed(@NonNull WebSocket ws, int code, @NonNull String reason) {
                Log.d(TAG, "websocket closed: " + reason);
                scheduleReconnect();
            }

            @Override
            public void onFailure(@NonNull WebSocket ws, @NonNull Throwable t, @Nullable Response r) {
                Log.w(TAG, "websocket failure: " + t.getMessage());
                scheduleReconnect();
            }
        });
    }

    public void disconnect() {
        stopped = true;
        main.removeCallbacksAndMessages(null);
        if (webSocket != null) {
            try {
                webSocket.send("DISCONNECT\n\n" + NUL);
            } catch (Exception ignored) {
                // best effort
            }
            webSocket.close(1000, "client closing");
            webSocket = null;
        }
    }

    private void scheduleReconnect() {
        if (stopped) {
            return;
        }
        long delay = Math.min(RECONNECT_BASE_MS * (1L << Math.min(reconnectAttempts, 4)),
                RECONNECT_MAX_MS);
        reconnectAttempts++;
        Log.d(TAG, "reconnecting in " + delay + "ms");
        main.postDelayed(this::openSocket, delay);
    }

    private void handleFrame(WebSocket ws, String frame) {
        if (frame == null) {
            return;
        }
        // Drop CRs and any leading newlines (a lone newline is a STOMP heartbeat).
        String f = stripLeading(frame.replace("\r", ""));
        if (f.isEmpty()) {
            return;
        }
        if (f.startsWith("CONNECTED")) {
            reconnectAttempts = 0;
            ws.send(subscribeFrame("sub-panic", TOPIC_PANIC));
            ws.send(subscribeFrame("sub-resolved", TOPIC_RESOLVED));
            Log.d(TAG, "STOMP connected, subscribed to panic topics");
            return;
        }
        if (f.startsWith("MESSAGE")) {
            dispatchMessage(f);
        }
        // ERROR / RECEIPT and anything else: ignored.
    }

    private void dispatchMessage(String frame) {
        String destination = headerValue(frame, "destination");
        String body = body(frame);
        if (body == null || body.isEmpty()) {
            return;
        }
        try {
            final PanicResponse panic = gson.fromJson(body, PanicResponse.class);
            if (panic == null) {
                return;
            }
            final boolean resolved = TOPIC_RESOLVED.equals(destination);
            main.post(() -> {
                if (resolved) {
                    listener.onPanicResolved(panic);
                } else {
                    listener.onPanicCreated(panic);
                }
            });
        } catch (RuntimeException e) {
            Log.w(TAG, "could not parse panic body: " + e.getMessage());
        }
    }

    private String connectFrame() {
        return "CONNECT\naccept-version:1.1,1.2\nheart-beat:0,0\n\n" + NUL;
    }

    private String subscribeFrame(String id, String destination) {
        return "SUBSCRIBE\nid:" + id + "\ndestination:" + destination + "\n\n" + NUL;
    }

    /** Splits a raw text payload into individual STOMP frames on the NUL terminator. */
    private List<String> splitFrames(String text) {
        List<String> frames = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == NUL) {
                frames.add(text.substring(start, i));
                start = i + 1;
            }
        }
        if (start < text.length()) {
            frames.add(text.substring(start));
        }
        return frames;
    }

    /** The header block is everything between the command line and the first blank line. */
    private String headerValue(String frame, String name) {
        int blank = frame.indexOf("\n\n");
        String head = blank >= 0 ? frame.substring(0, blank) : frame;
        for (String line : head.split("\n")) {
            int colon = line.indexOf(':');
            if (colon > 0 && line.substring(0, colon).equals(name)) {
                return line.substring(colon + 1).trim();
            }
        }
        return null;
    }

    private String body(String frame) {
        int blank = frame.indexOf("\n\n");
        if (blank < 0) {
            return null;
        }
        return frame.substring(blank + 2).trim();
    }

    private String stripLeading(String s) {
        int i = 0;
        while (i < s.length() && (s.charAt(i) == '\n' || s.charAt(i) == NUL)) {
            i++;
        }
        return s.substring(i);
    }
}
