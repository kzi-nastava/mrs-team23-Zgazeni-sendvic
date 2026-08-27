package com.example.mobile_front_ma.data.realtime;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobile_front_ma.data.SessionManager;
import com.example.mobile_front_ma.models.dto.RideTrackingUpdateDto;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RideTrackingSocketClient {

    public interface Listener {
        void onRideUpdate(RideTrackingUpdateDto update);
        void onConnected();
        void onError(String message);
    }

    private static final String TAG = "RideTrackingSocket";
    private static final char NUL = (char) 0;

    private final String wsUrl;
    private final long userId;
    private final String token;
    private final Listener listener;
    private final Gson gson = new Gson();
    private final OkHttpClient client;
    private final Handler main = new Handler(Looper.getMainLooper());

    private WebSocket webSocket;
    private boolean stopped = false;

    public RideTrackingSocketClient(Context context, String httpBaseUrl, long userId, Listener listener) {
        String base = httpBaseUrl.replaceFirst("^http", "ws");
        if (!base.endsWith("/")) base += "/";
        this.wsUrl = base + "ws/websocket";
        this.userId = userId;
        this.token = new SessionManager(context).getToken();
        this.listener = listener;
        this.client = new OkHttpClient.Builder()
                .pingInterval(20, TimeUnit.SECONDS)
                .build();
    }

    public void connect() {
        if (webSocket != null) return;
        stopped = false;
        Log.d(TAG, "Connecting to " + wsUrl + " for user " + userId);
        
        Request.Builder rb = new Request.Builder().url(wsUrl);
        if (token != null) rb.addHeader("Authorization", "Bearer " + token);
        
        webSocket = client.newWebSocket(rb.build(), new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket ws, @NonNull Response response) {
                Log.d(TAG, "Socket Open. Sending STOMP CONNECT...");
                String connectFrame = "CONNECT\n" +
                        "accept-version:1.1,1.2\n" +
                        "heart-beat:10000,10000\n" +
                        (token != null ? "Authorization: Bearer " + token + "\n" : "") +
                        "\n" + NUL;
                Log.d(TAG, "CONNECT frame:\n" + connectFrame);
                ws.send(connectFrame);
            }

            @Override
            public void onMessage(@NonNull WebSocket ws, @NonNull String text) {
                for (String frame : splitFrames(text)) {
                    handleFrame(ws, frame);
                }
            }

            @Override
            public void onClosed(@NonNull WebSocket ws, int code, @NonNull String reason) {
                Log.d(TAG, "Socket closed: " + reason);
                webSocket = null;
                if (!stopped) scheduleReconnect();
            }

            @Override
            public void onFailure(@NonNull WebSocket ws, @NonNull Throwable t, @Nullable Response r) {
                Log.e(TAG, "Socket failure: " + t.getMessage());
                webSocket = null;
                if (!stopped) scheduleReconnect();
            }
        });
    }

private void handleFrame(WebSocket ws, String frame) {

    String f = frame.trim();

    if (f.isEmpty()) {
        return;
    }

    Log.d(TAG, "STOMP FRAME RECEIVED:\n" + frame);

    if (f.startsWith("CONNECTED")) {

        Log.d(TAG, "STOMP CONNECTED. Subscribing...");

        String subscribeFrame =
                "SUBSCRIBE\n" +
                        "id:sub-0\n" +
                        "destination:/user/" + userId + "/queue/ride-tracking\n" +
                        "\n" +
                        NUL;

        Log.d(TAG, "Sending SUBSCRIBE:\n" + subscribeFrame);

        ws.send(subscribeFrame);

        // Isto kao Angular aplikacija
        String subscribeMessage =
                "SEND\n" +
                        "destination:/app/ride-tracking/subscribe\n" +
                        "\n" +
                        userId +
                        NUL;

        Log.d(TAG, "Sending ride tracking subscribe:\n" + subscribeMessage);

        ws.send(subscribeMessage);

        main.post(listener::onConnected);

    } else if (f.startsWith("MESSAGE")) {

        Log.d(TAG, "MESSAGE FRAME RECEIVED:\n" + frame);

        String body = extractBody(frame);

        if (body != null) {

            Log.d(TAG, "Update body: " + body);

            try {

                RideTrackingUpdateDto update =
                        gson.fromJson(body, RideTrackingUpdateDto.class);

                if (update != null) {
                    main.post(() -> listener.onRideUpdate(update));
                }

            } catch (Exception e) {
                Log.e(TAG, "GSON error", e);
            }
        }
    }
}


    private String extractBody(String frame) {

        int separator = frame.indexOf("\n\n");

        if (separator == -1) {
            separator = frame.indexOf("\r\n\r\n");
        }

        if (separator == -1) {
            return null;
        }

        // Preskačemo praznu liniju između headera i body-ja
        int bodyStart = separator + 2;

        String body = frame.substring(bodyStart);

        // Uklanjamo STOMP NUL terminator
        int nulIndex = body.indexOf(NUL);

        if (nulIndex >= 0) {
            body = body.substring(0, nulIndex);
        }

        return body.trim();
    }

    private void scheduleReconnect() {
        if (stopped) return;
        main.postDelayed(this::connect, 5000);
    }

    public void disconnect() {
        stopped = true;
        if (webSocket != null) {
            webSocket.close(1000, "Disconnect");
            webSocket = null;
        }
    }

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
            String last = text.substring(start).trim();
            if (!last.isEmpty()) frames.add(last);
        }
        return frames;
    }
}
