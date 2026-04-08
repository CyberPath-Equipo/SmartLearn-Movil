package com.cyberpath.smartlearn.ui.acceso;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;
import com.cyberpath.smartlearn.util.notificaciones.AlarmaProgramador;
import com.cyberpath.smartlearn.util.notificaciones.NotificacionHelper;

import java.util.ArrayList;
import java.util.List;

public class AccesoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acceso);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            List<String> permisos = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.POST_NOTIFICATIONS);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.RECORD_AUDIO);
            }

            if (!permisos.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        permisos.toArray(new String[0]),
                        100);
            }
        }
        NotificacionHelper.crearCanal(this);
        AlarmaProgramador.programarRecordatorioDiario(this);
        SalidaAudio.iniciarInstancia(this.getApplicationContext());
        EntradaAudio.iniciarInstancia(this.getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}