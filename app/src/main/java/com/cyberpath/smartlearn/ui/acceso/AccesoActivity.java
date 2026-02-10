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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, 100);
            }
        }

        SalidaAudio.iniciarInstancia(this.getApplicationContext());
        EntradaAudio.iniciarInstancia(this.getApplicationContext());

        NotificacionHelper.crearCanal(this);
        AlarmaProgramador.programarRecordatorioDiario(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos de TTS / STT cuando la Activity termine
        SalidaAudio instSalida = SalidaAudio.obtenerInstancia();
        if (instSalida != null) instSalida.liberar();
        EntradaAudio instEntrada = EntradaAudio.obtenerInstancia();
        if (instEntrada != null) instEntrada.liberar();
    }
}