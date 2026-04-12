package com.cyberpath.smartlearn.util.notificaciones.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.ui.main.MainActivity;
import com.cyberpath.smartlearn.util.notificaciones.NotificacionHelper;

public class RecordatorioDiarioReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, NotificacionHelper.getCanalId())
                .setSmallIcon(R.drawable.img_logo_notificaciones)
                .setContentTitle("¡Hora de estudiar!")
                .setContentText("Tienes subtemas pendientes. ¡Vamos, tú puedes!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1001, notification);
    }
}
