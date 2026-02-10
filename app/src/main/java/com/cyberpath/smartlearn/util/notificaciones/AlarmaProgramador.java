package com.cyberpath.smartlearn.util.notificaciones;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.cyberpath.smartlearn.util.notificaciones.receiver.RecordatorioDiarioReceiver;

import java.util.Calendar;

public class AlarmaProgramador {
    public static void programarRecordatorioDiario(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RecordatorioDiarioReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeInMillis(System.currentTimeMillis() + 10_000);
        /*calendar.set(Calendar.HOUR_OF_DAY, 15); //
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);*/

        // Si ya pasó la hora hoy, programa para mañana
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }

    // Para reprogramar al reiniciar el móvil
    public static void programarAlReiniciar(Context context) {
        Intent intent = new Intent(context, RecordatorioDiarioReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + AlarmManager.INTERVAL_DAY,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }
}