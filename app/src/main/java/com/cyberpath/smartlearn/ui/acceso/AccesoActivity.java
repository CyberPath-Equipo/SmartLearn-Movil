package com.cyberpath.smartlearn.ui.acceso;

import android.Manifest;
import android.content.Intent;
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
import com.cyberpath.smartlearn.ui.main.MainActivity;
import com.cyberpath.smartlearn.util.notificaciones.AlarmaProgramador;
import com.cyberpath.smartlearn.util.notificaciones.NotificacionHelper;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.cyberpath.smartlearn.util.preferences.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class AccesoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        if (PreferencesManager.isSesionActiva(this)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

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
    }
}