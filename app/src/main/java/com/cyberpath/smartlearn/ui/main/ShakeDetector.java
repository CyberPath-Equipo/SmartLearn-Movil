package com.cyberpath.smartlearn.ui.main;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;

class ShakeDetector implements SensorEventListener {
    private static final float SHAKE_THRESHOLD_G = 2.4f;
    private final long cooldownMs;
    private final OnShakeListener onShakeListener;
    private long lastShakeAt;
    ShakeDetector(long cooldownMs, OnShakeListener onShakeListener) {
        this.cooldownMs = cooldownMs;
        this.onShakeListener = onShakeListener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null || event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gForce = (float) Math.sqrt(x * x + y * y + z * z) / SensorManagerConstants.GRAVITY_EARTH;
        if (gForce < SHAKE_THRESHOLD_G) {
            return;
        }

        long now = SystemClock.elapsedRealtime();
        if (now - lastShakeAt < cooldownMs) {
            return;
        }

        lastShakeAt = now;
        onShakeListener.onShake();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No-op
    }

    interface OnShakeListener {
        void onShake();
    }

    // Isolated constant avoids pulling full SensorManager as dependency in this utility.
    private static final class SensorManagerConstants {
        private static final float GRAVITY_EARTH = 9.80665f;
    }
}

