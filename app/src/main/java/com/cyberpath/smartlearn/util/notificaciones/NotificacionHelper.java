package com.cyberpath.smartlearn.util.notificaciones;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificacionHelper {
    private static final String CANAL_ID = "smartlearn_canal";
    private static final String CANAL_NOMBRE = "Recordatorios SmartLearn";
    private static final String CANAL_DESCRIPCION = "Notificaciones de estudio y progreso";

    public static void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    CANAL_NOMBRE,
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription(CANAL_DESCRIPCION);
            canal.enableLights(true);
            canal.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(canal);
        }
    }

    public static String getCanalId() {
        return CANAL_ID;
    }
}