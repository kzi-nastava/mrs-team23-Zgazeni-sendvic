package com.example.mobile_front_ma;

import android.app.Application;
import android.content.Context;

import com.example.mobile_front_ma.data.network.GeoApiClient;
import com.example.mobile_front_ma.data.realtime.PanicRealtimeManager;

import org.osmdroid.config.Configuration;

/**
 * Custom Application: configures osmdroid once for every map screen, and initialises the
 * app-scoped panic realtime manager (spec 2.6.3) so it is available to the admin screens and to
 * {@code PanicForegroundService}, which drives the panic socket's connection lifetime.
 */
public class MobileFrontApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initOsmdroid();
        PanicRealtimeManager.init(this);
    }

    /**
     * osmdroid must know who we are before the first tile request. OpenStreetMap's tile servers
     * answer the default {@code com.example.*} package name with HTTP 403, so we identify the app
     * with the same agent the Nominatim and OSRM clients already send.
     */
    private void initOsmdroid() {
        Configuration.getInstance().load(this,
                getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(GeoApiClient.USER_AGENT);
    }
}
