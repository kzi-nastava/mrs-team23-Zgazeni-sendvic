package com.example.mobile_front_ma.util;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Detects a "shake" gesture from the accelerometer. Used on the registered-user ride
 * history screen to alternately re-sort the list by date (spec 2.9.1).
 *
 * Register/unregister this with the {@link android.hardware.SensorManager} in the
 * Activity's onResume/onPause.
 */
public class ShakeDetector implements SensorEventListener {

    public interface OnShakeListener {
        void onShake();
    }

    // g-force a device must exceed to count as a shake.
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.3f;
    // Ignore shakes that arrive closer together than this (debounce).
    private static final long MIN_INTERVAL_MS = 800;

    private final OnShakeListener listener;
    private long lastShakeTime = 0;

    public ShakeDetector(OnShakeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        // Magnitude of the gravity vector; ~1 at rest, higher while shaking.
        double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            long now = System.currentTimeMillis();
            if (now - lastShakeTime < MIN_INTERVAL_MS) {
                return;
            }
            lastShakeTime = now;
            if (listener != null) {
                listener.onShake();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No-op: accuracy changes don't affect shake detection.
    }
}
