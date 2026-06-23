package com.example.mobile_front_ma;

import android.app.Application;

import com.example.mobile_front_ma.data.realtime.PanicRealtimeManager;

/**
 * Custom Application: initialises the app-scoped panic realtime manager (spec 2.6.3) so it is
 * available to the admin screens and to {@code PanicForegroundService}, which drives the panic
 * socket's connection lifetime.
 */
public class MobileFrontApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PanicRealtimeManager.init(this);
    }
}
